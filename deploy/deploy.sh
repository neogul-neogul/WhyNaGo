#!/usr/bin/env bash
#
# EC2에서 app 컨테이너를 새 이미지로 교체한다.
# GitHub Actions(backend-cd.yml)와 수동 배포가 같은 경로를 타도록 스크립트로 묶었다.
#
# 사용법:
#   ./deploy.sh <이미지 URI>
#
# 예시:
#   # 이미 docker login 이 되어 있는 경우
#   ./deploy.sh 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com/whynago-repo:a1b2c3d
#
#   # 로그인부터 해야 하는 경우 (ECR_TOKEN 이 있으면 자동으로 로그인한다)
#   ECR_TOKEN="$(aws ecr get-login-password --region ap-northeast-2)" \
#     ./deploy.sh <이미지 URI>
#
# 롤백:
#   이전 이미지 태그로 다시 실행하면 된다. 직전 이미지는 아래 로그에 출력된다.
#   ./deploy.sh <이전 이미지 URI>
#
# 환경변수:
#   ECR_TOKEN   설정되어 있으면 docker login 을 먼저 수행한다.
#   HEALTH_URL  헬스체크 대상. 기본값 https://whynago.kro.kr/health
#   NO_HEALTH   1 이면 헬스체크를 건너뛴다.

set -euo pipefail

IMAGE="${1:-}"
HEALTH_URL="${HEALTH_URL:-https://whynago.kro.kr/health}"

if [ -z "$IMAGE" ]; then
    echo "오류: 이미지 URI를 인자로 넘겨야 한다." >&2
    echo "사용법: $0 <이미지 URI>" >&2
    exit 1
fi

# 이 스크립트가 있는 디렉터리(= docker-compose.yml 이 있는 곳)에서 동작한다.
# 어디서 실행하든 결과가 같도록 고정한다.
cd "$(dirname "$(readlink -f "$0")")"

if [ ! -f .env ]; then
    echo "오류: .env 가 없다. 이 디렉터리가 배포 디렉터리가 맞는지 확인할 것." >&2
    exit 1
fi

# ─────────── ECR 로그인 ───────────
# EC2에는 AWS 자격증명을 두지 않는다.
# 토큰은 호출하는 쪽(GitHub Actions 러너 또는 로컬)에서 발급해 넘긴다.
if [ -n "${ECR_TOKEN:-}" ]; then
    # 이미지 URI 의 첫 / 앞부분이 레지스트리 주소다.
    REGISTRY="${IMAGE%%/*}"
    echo "==> ECR 로그인: ${REGISTRY}"
    printf '%s' "$ECR_TOKEN" | docker login --username AWS --password-stdin "$REGISTRY"
fi

# ─────────── 이미지 교체 ───────────
PREV="$(grep '^APP_IMAGE=' .env | cut -d= -f2- || true)"
echo "==> 이전 이미지: ${PREV:-(없음)}"
echo "==> 새 이미지:   ${IMAGE}"

if [ "$PREV" = "$IMAGE" ]; then
    echo "    (같은 이미지다. 그대로 재기동한다.)"
fi

# .env 의 APP_IMAGE 를 갱신한다. 줄이 없으면 추가한다.
# 이미지 URI 에 / 가 들어가므로 sed 구분자로 | 를 쓴다.
if grep -q '^APP_IMAGE=' .env; then
    sed -i "s|^APP_IMAGE=.*|APP_IMAGE=${IMAGE}|" .env
else
    echo "APP_IMAGE=${IMAGE}" >> .env
fi

echo "==> 이미지 pull"
docker compose pull app

# nginx.conf 는 bind mount 라 컨테이너를 건드리지 않고도 검사할 수 있다.
# 컨테이너 교체는 되돌릴 수 없으므로, 설정 오류는 그 전에 걸러낸다.
echo "==> nginx 설정 검사"
docker compose exec -T nginx nginx -t

# ─────────── 여기부터 되돌릴 수 없다 ───────────
echo "==> app 컨테이너 교체"
docker compose up -d app

# reload 는 기존 커넥션을 끊지 않는다.
# app 컨테이너가 생성된 뒤에 해야 upstream 의 app 이름이 해석된다.
echo "==> nginx reload"
docker compose exec -T nginx nginx -s reload

# ─────────── 헬스체크 ───────────
if [ "${NO_HEALTH:-}" = "1" ]; then
    echo "==> 헬스체크 생략(NO_HEALTH=1)"
    exit 0
fi

# --resolve 로 DNS 를 거치지 않고 로컬 nginx 를 직접 친다.
# EC2 가 자기 공인 IP 로 되돌아오지 못하는 구성에서도 동작하고,
# 인증서 검증(-k 없이)도 그대로 수행된다.
HOSTNAME_ONLY="$(printf '%s' "$HEALTH_URL" | sed -E 's|^https?://([^/:]+).*|\1|')"
echo "==> 헬스체크: ${HEALTH_URL}"

for i in $(seq 1 30); do
    code="$(curl -s -o /tmp/whynago-health.json -w '%{http_code}' --max-time 10 \
        --resolve "${HOSTNAME_ONLY}:443:127.0.0.1" "$HEALTH_URL" || echo 000)"

    if [ "$code" = "200" ]; then
        echo "    통과 (${i}회차): $(cat /tmp/whynago-health.json)"
        # 배포가 확정된 뒤에만 정리한다. 실패 시 롤백 대상을 건드리지 않기 위함이다.
        # 태그가 바뀌면 이전 이미지가 dangling 으로 남는다. 디스크가 작아 방치하면 쌓인다.
        docker image prune -f > /dev/null
        exit 0
    fi
    echo "    ${i}/30 대기 중... HTTP ${code}"
    sleep 5
done

echo "오류: 헬스체크 실패. 앱이 정상 기동하지 않았다." >&2
echo "" >&2
echo "컨테이너 상태:" >&2
docker compose ps >&2
echo "" >&2
echo "app 로그 (최근 50줄):" >&2
docker compose logs app --tail 50 >&2
echo "" >&2
echo "롤백하려면:" >&2
echo "  ./deploy.sh ${PREV:-<이전 이미지 URI>}" >&2
exit 1
