# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트

WhyNaGo (`com.neogul.whynago`) — Spring Boot 3.5 / Java 21 기반 웹 API. 빌드 도구는 Gradle(wrapper 포함). 런타임 DB는 H2이며, 통합 테스트는 Testcontainers를 사용한다.

## 명령어

Git Bash에서는 `./gradlew`, PowerShell에서는 `gradlew.bat`을 사용한다.

- 빌드: `./gradlew build`
- 앱 실행: `./gradlew bootRun`
- 전체 테스트: `./gradlew test`
- 단일 테스트 클래스: `./gradlew test --tests 'com.neogul.whynago.auth.implement.JwtProviderTest'`
- 단일 테스트 메서드: `./gradlew test --tests 'com.neogul.whynago.auth.implement.JwtProviderTest.parseToken'`
- 컴파일만 (리팩터링 후 빠른 확인): `./gradlew compileJava`

## 참고 문서

- `docs/TEST.md` — 테스트 규약 (계층별 테스트, 네이밍, Given-When-Then, AssertJ, RestAssuredMockMvc, Fixture, DbCleaner)
- `docs/ARCHITECTURE.md` — 아키텍처 문서

