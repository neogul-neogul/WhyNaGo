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
- 화면(UI) 구현 단계이며, **모든 데이터는 더미 데이터**로 처리한다.
- 백엔드 API 연동, 실제 인증(토큰/세션), 데이터 영속화 로직은 **작성하지 않는다.**
- 인증(회원가입/로그인/로그아웃)은 화면과 클라이언트 측 더미 동작까지만 구현한다.

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
- 더미 데이터: `src/mocks/`
- 공용 타입 정의: `src/types/`

## 컨벤션
- 컴포넌트 파일/이름: PascalCase (예: `Header.tsx`)
- 폴더명: 라우트 폴더는 소문자 (예: `login/`)
- import는 `@/` 별칭 사용 (예: `@/components/layout/Header`)
- Tailwind 유틸리티 클래스 사용, 인라인 style 지양
- 더미 데이터에는 반드시 TypeScript 타입을 붙인다.

## 하지 말 것
- 백엔드/API/DB 관련 코드 추가 금지
- 요청하지 않은 라이브러리 임의 설치 금지
- 여러 페이지를 한꺼번에 만들지 말 것 (요청 단위 준수)