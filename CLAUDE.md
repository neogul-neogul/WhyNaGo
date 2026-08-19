# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로덕트

왜냐고(WhyNaGo) — 개발자 지망생을 위한 문제 풀이 서비스. 객관식/주관식 문제를 풀고, 답할 때마다 그 답에 대한 **꼬리 질문**을 이어가는 것이 핵심 특징이다. 오답노트, 학습 지속성을 위한 공부 기록표/스트릭을 제공하며, 서술형 문제의 꼬리 질문은 AI로 생성한다.

## 저장소 구성

하나의 저장소에 독립적으로 빌드되는 두 개의 앱이 있다.

- **백엔드** (저장소 루트): Spring Boot 3.5 / Java 21 / Gradle. 패키지 루트는 `com.neogul.whynago`.
- **프론트엔드** (`front/`): Next.js 16 (App Router) / React 19 / TypeScript / Tailwind CSS v4.

## 명령어

백엔드 (저장소 루트에서 실행):
```bash
./gradlew bootRun        # 앱 실행 (8080 포트)
./gradlew build          # 컴파일 + 테스트 + jar
./gradlew test           # 전체 테스트
./gradlew test --tests "com.neogul.whynago.WhynagoApplicationTests"          # 단일 클래스
./gradlew test --tests "com.neogul.whynago.PaymentServiceTest.pay"           # 단일 메서드
./gradlew bootJar        # 실행 가능한 jar 빌드 (build/libs/*.jar)
```

프론트엔드 (`front/`에서 실행): `front/CLAUDE.md` 참고.

## 백엔드 문서

이 프로젝트는 **도메인 중심 레이어드 아키텍처**를 따른다. 백엔드 코드를 작성/수정하기 전에 반드시 `docs/`의 해당 문서를 먼저 읽는다. 아래는 각 문서가 무엇을 규정하는지에 대한 요약이며, 구체적인 규칙·예시·리뷰 체크리스트는 각 파일을 참고한다.

- **`docs/ARCHITECTURE.md`** — 아키텍처 컨벤션.
  도메인을 최상위로 두고 그 안에서 `presentation / service / implement / infra / domain` 레이어를 나누는 패키지 구조, 레이어 간 의존성 방향(항상 아래로만, 건너뛰기 금지), service/implement/infra/domain 각 레이어의 작성 규칙과 트랜잭션 경계를 규정한다. 핵심은 "service가 상세 구현을 몰라도 비즈니스 흐름을 읽을 수 있게" 만드는 것.

- **`docs/CONVENTION.md`** — 공통 개발 컨벤션.
  특정 레이어에 종속되지 않고 코드 전반에 적용되는 규칙(Command·Result 등 모델의 파일 분리와 inner class/record 지양, record 기반 불변 모델, 정적 팩토리 메서드(`from`/`of`) 변환, `@RequiredArgsConstructor` 생성자 주입)을 규정한다.

- **`docs/EXCEPTION.md`** — 예외 처리 컨벤션.
  예외 분류, 일관된 에러 응답 형식(`{code, message}`), 도메인 단위 `ErrorCode` 네이밍(`{DOMAIN}_{REASON}`), `BusinessException`, `GlobalExceptionHandler`의 처리 우선순위와 로깅 규칙을 규정한다. 실제 구현 스캐폴딩은 `src/main/java/com/neogul/whynago/common/exception/`에 있다.

- **`docs/API.md`** — API 규격 컨벤션.
  백엔드가 제공하는 HTTP API의 요청/응답 규격 기준 문서. `/api` 접두사·JSON 본문·성공 상태코드(생성 201/조회 200)·에러 응답 형식·`userId`는 인증 계층에서 해석 등 공통 규칙과 엔드포인트별 요청/응답 DTO를 규정한다.

- **`docs/TEST.md`** — 테스트 컨벤션.
  테스트 계층(Unit/Service/Repository/Controller/Integration), 네이밍(`@DisplayName` 한글 `~다.` 형식, 메서드 `{method}_{실패이유}`), Given-When-Then, Fixture/Builder, AssertJ 우선 사용, Controller 테스트의 RestAssuredMockMvc 표준, Testcontainers(MySQL) 기반 DB 테스트를 규정한다.

- **`docs/DOMAIN.md`** - 도메인 문서.
  도메인 관련 정보가 담겨있다.

- **`docs/RECOMMENDATION.md`** — 맞춤 문제 추천 설계.
  약점을 진단해 그에 맞는 서술형 문항을 AI로 생성해 추천하는 파이프라인(`recommendation` 도메인)을 규정한다. 숙련도 판정·약점 프로필·취약 주제 선정은 결정적으로 처리하고 문항 생성만 AI에 맡긴다. 생성 문항은 `source = GENERATED`·`review_status = PENDING`으로 저장돼 검수 승인 전에는 문제은행 목록에 노출되지 않는다. 추천을 건드리기 전에 읽는다.

- **`docs/SCRIPT_RECOMMENDATION.md`** — 맞춤 문제 생성 프롬프트.
  추천 파이프라인이 **런타임에** 서술형 문항 1개를 생성할 때 LLM에 주는 프롬프트의 원본. 시드 생성(`SCRIPT.md`·`SCRIPT_ESSAY.md`)과 달리 출력이 SQL이 아니라 JSON 1건이고, 난이도가 약점도에서 계산돼 내려온다. 실행본은 `src/main/resources/prompts/essay-question-generation.st`이며 둘을 함께 고친다.

- **`docs/TAG.md`** — 문제 태그 사전.
  `question_tag.name`에 넣을 수 있는 태그의 전체 목록(8개 카테고리 총 238개)과 태그 명명·부여 규칙을 규정한다. 카테고리당 개수는 고정하지 않으며, 태그 이름은 8개 카테고리를 통틀어 유일하다. `SCRIPT.md`·`SCRIPT_ESSAY.md`가 공통으로 참조하며, 두 프롬프트를 쓸 때 **반드시 함께 제공**한다. 사전에 없는 태그는 생성 중에 만들지 않는다.

- **`docs/SCRIPT.md`** — 객관식 문제 시드 생성 프롬프트.
  객관식 문항 시드 데이터를 SQL INSERT문으로 생성하기 위해 LLM에 주는 규칙. **태그 사전 확정 → 태그를 축으로 문항 생성**의 2단계 절차(이번 회차에 소비할 태그 25개를 5개 클러스터로 나눠 태그 1개당 문항 1개, 카테고리 전체는 회차를 누적해 커버), 세션 변수 기반 순환 연결(`related_question_id` NULL 없음), 정답 위치 무작위 분산, 서술형 보기 등을 규정한다.

- **`docs/SCRIPT_ESSAY.md`** — 서술형 문제 시드 생성 프롬프트.
  서술형(`type = 'ESSAY'`) 문항 시드를 생성하는 규칙. 객관식과 달리 `answer_choice`·`related_question_id`가 없고 본 질문만 저장하며(꼬리질문은 런타임에 AI가 생성), 태그 사전의 태그 1개당 문항 1개를 만든다.

- **`docs/SEED_GENERATION.md`** — 문제 시드 생성·검증 가이드.
  위 `SCRIPT.md` 프롬프트를 실제로 굴려 시드를 만들고 검증하는 절차 핸드오프 문서. 도메인 핵심(본질문/꼬리질문 구분 없음), 생성 워크플로(카테고리별 병렬 생성 → 결합 → 실제 스키마에 넣어 검증), 관련 백엔드 사실·파일 위치를 정리한다. 새 시드를 만들 때 먼저 읽는다.

## 프론트엔드

프론트엔드 작업은 `front/CLAUDE.md`를 참고한다.
