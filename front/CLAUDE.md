# WhyNaGo Frontend

## 프로젝트 개요
왜냐고(WhyNaGo) - 신입 웹 개발자 대상 학습/면접 준비 서비스의 프론트엔드.
핵심 기능: 문제풀이, 1일 1면접, 모의진단.

## 기술 스택
- Next.js (App Router)
- TypeScript
- Tailwind CSS
- 상태관리/데이터 페칭 라이브러리는 아직 도입하지 않음 (필요 시 논의 후 결정)

## 현재 작업 범위 (매우 중요)
- **백엔드가 구현된 기능은 실제 API를 연동한다.** 현재 연동된 기능: 인증(회원가입/로그인), 객관식 문제 풀이(문제은행 목록·채점 조회·세션 저장).
- 백엔드가 아직 없는 기능(서술형 풀이, 오답노트, 학습 기록, 진척도 등)은 **더미 데이터로 화면만** 유지한다.
- API 호출은 `src/lib/api.ts`의 `apiFetch`를 통해서만 한다 (base URL·JSON 직렬화·Authorization 헤더·에러 변환 공통 처리). 도메인별 API 함수는 `src/lib/<도메인>.ts`에 모은다 (예: `lib/auth.ts`, `lib/questions.ts`).
- 백엔드 응답 모델 타입은 `src/types/index.ts`에 서버 스펙 그대로 정의하고, enum ↔ 화면 라벨 변환은 도메인 lib에 둔다.
- 로컬 개발 시 `.env.local`에 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`을 설정한다 (`.env.example` 참고).

## 작업 방식
- 전체 화면을 순차적으로 구현하되, **한 번에 한 페이지(또는 컴포넌트) 단위**로 진행한다.
- 요청받은 페이지 외의 파일은 임의로 수정하지 않는다.
- 새 페이지를 만들 때 기존 페이지의 구조/네이밍 컨벤션을 따른다.

## 라우트 구조
- 메인(오늘의 학습): `/` → `src/app/page.tsx`
- 로그인: `/login` → `src/app/login/page.tsx`
- 회원가입: `/signup` → `src/app/signup/page.tsx`

## 폴더 구조 규칙
- 페이지: `src/app/<route>/page.tsx`
- 공통 레이아웃: `src/app/layout.tsx`
- 재사용 컴포넌트: `src/components/`
  - 공통 UI 컴포넌트(버튼, 인풋 등): `src/components/ui/`
  - 레이아웃 컴포넌트(헤더 등): `src/components/layout/`
  - 도메인 섹션 컴포넌트: `src/components/<도메인>/` (예: `today/`, `solve/`, `interview/`)
    - 페이지(`page.tsx`)는 데이터(mock) 조합과 화면 흐름 상태만 갖고, 화면 섹션은 도메인 컴포넌트로 분리한다.
    - 한 섹션 안에서만 쓰이는 상태(예: 잔디 선택, 차트 카테고리)는 해당 컴포넌트 내부에 둔다.
- 더미 데이터: `src/mocks/`
- 공용 타입 정의: `src/types/`

## 컨벤션
- 컴포넌트 파일/이름: PascalCase (예: `Header.tsx`)
- 폴더명: 라우트 폴더는 소문자 (예: `login/`)
- import는 `@/` 별칭 사용 (예: `@/components/layout/Header`)
- Tailwind 유틸리티 클래스 사용, 인라인 style 지양
- 더미 데이터에는 반드시 TypeScript 타입을 붙인다.

## 하지 말 것
- `apiFetch`를 우회한 직접 `fetch` 호출 금지 (공통 에러/인증 처리를 태운다)
- 백엔드가 없는 기능을 임의로 API 연동하지 말 것 (백엔드 스펙 확정 후 진행)
- 요청하지 않은 라이브러리 임의 설치 금지
- 여러 페이지를 한꺼번에 만들지 말 것 (요청 단위 준수)