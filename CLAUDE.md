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

- **`docs/EXCEPTION.md`** — 예외 처리 컨벤션.
  예외 분류, 일관된 에러 응답 형식(`{code, message}`), 도메인 단위 `ErrorCode` 네이밍(`{DOMAIN}_{REASON}`), `BusinessException`, `GlobalExceptionHandler`의 처리 우선순위와 로깅 규칙을 규정한다. 실제 구현 스캐폴딩은 `src/main/java/com/neogul/whynago/common/exception/`에 있다.

- **`docs/TEST.md`** — 테스트 컨벤션.
  테스트 계층(Unit/Service/Repository/Controller/Integration), 네이밍(`@DisplayName` 한글 `~다.` 형식, 메서드 `{method}_{실패이유}`), Given-When-Then, Fixture/Builder, AssertJ 우선 사용, Controller 테스트의 RestAssuredMockMvc 표준, Testcontainers(MySQL) 기반 DB 테스트를 규정한다.

- **`docs/DOMAIN.md`** - 도메인 문서.
  도메인 관련 정보가 담겨있다.

## 프론트엔드

프론트엔드 작업은 `front/CLAUDE.md`를 참고한다.
