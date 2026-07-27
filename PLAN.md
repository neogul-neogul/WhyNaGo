# 객관식 문제 풀이 기능 개발 계획

## 목표 시나리오

1. 사용자가 문제은행에서 객관식 문제를 선택한다.
2. 문제를 푼다 (보기 선택 → 정답 확인/채점).
3. **고른 보기**의 `relatedQuestionId`에 해당하는 꼬리 질문이 다음 문제로 나온다 (보기별 분기, 정답/오답 무관).
4. 2–3을 반복해 본질문 1개 + 꼬리질문 2개를 푼다.
5. 다 풀면 **저장하기** 버튼으로 세션을 저장한다 (`SolvedSession` + `SolvedMultipleChoice` 저장, 오답 포함 시 `WrongNote` 자동 생성).

## 현재 상태 요약

### 이미 구현됨
- **백엔드 데이터 모델 완비**: `Question`, `AnswerChoice`(`isCorrect`, `relatedQuestionId`) — 보기별 분기 모델 존재.
- **세션 저장 파이프라인 완성** (`solvedsession` 도메인): `POST /api/solved-sessions` → 체인 무결성 검증(`SolvedSessionValidator`) → 채점(`MultipleChoiceAnswerScorer`) → `SolvedSession`/`SolvedMultipleChoice` 저장 → 오답 시 `WrongNote` 자동 생성. 서비스 레벨 통합 테스트(`MultipleChoiceSolvingIntegrationTest`)도 통과.
- **문제은행 목록 API**: `GET /api/questions` (루트 객관식만, 필터 지원).
- **인증 인프라**: JWT + `AuthInterceptor` + `@LoginUser AuthContext` 리졸버. 프론트 `apiFetch`도 Bearer 자동 부착.
- **프론트 UI 골격**: `setup(ProblemBank) → quiz(MultipleChoiceQuiz) → result(QuizResult)` 3단계 화면.

### 갭 (이번에 개발할 것)
| # | 갭 | 위치 |
| --- | --- | --- |
| G1 | **채점 API 없음** — `ChoiceResponse`가 `isCorrect`를 노출하지 않으므로(의도된 설계) 프론트가 채점 결과·해설·다음 꼬리질문을 받을 방법이 없음 | 백엔드 |
| G2 | **꼬리질문 조회 API 없음** — `GET /api/questions`는 루트만 반환 | 백엔드 (G1에서 함께 해결) |
| G3 | `SolvedSessionController.create(Long userId, ...)`의 `userId`에 `@LoginUser` 미적용 → 인증 사용자와 미연결 | 백엔드 |
| G4 | 프론트가 **전부 mock** — `ProblemBank`/`MultipleChoiceQuiz`가 API를 한 번도 호출하지 않음 | 프론트 |
| G5 | 꼬리질문이 **보기별 분기가 아닌 순차 공개** — mock 구조(`followups` 고정 배열)와 `revealed++` 로직이 도메인 정책과 불일치 | 프론트 |
| G6 | **저장하기 버튼이 저장하지 않음** — `onClick={onQuit}`으로 화면 복귀만 함 | 프론트 |

---

## 설계 결정: 채점 API가 꼬리질문을 함께 반환

정답(`isCorrect`)을 클라이언트에 내려주지 않는 현재 설계를 유지하고, **서버 채점 API 1개**로 G1·G2를 함께 해결한다. 도메인 정책("채점 즉시 해설 노출", "채점하면 꼬리질문 공개")과 정확히 일치한다. 아무것도 저장하지 않는 순수 조회이므로 GET으로 설계한다 — "이 문제에서 이 보기를 골랐을 때의 결과"를 조회하는 리소스.

```
GET /api/questions/{questionId}/choices/{choiceId}
응답:  {
  "correct": false,
  "correctChoiceId": 11,              // 정답 보기 하이라이트용
  "explanation": "...",               // Question.explanation (항상)
  "choiceExplanation": "...",         // 고른 보기의 오답 해설 (정답이면 null)
  "nextQuestion": {                   // 고른 보기의 relatedQuestionId 문제. 없으면 null → 세션 종료 지점
    "id": 5, "title": "...", "content": "...",
    "choices": [ { "id", "content", "sequence", "relatedQuestionId" } ]
  }
}
```

- 조회+계산만 하고 아무것도 저장하지 않는다 (저장은 5단계 "저장하기"에서 일괄).
- `nextQuestion.choices`는 기존 `ChoiceResponse` 재사용 (`isCorrect` 미노출 유지).
- 존재하지 않는 questionId/choiceId, 문제-보기 불일치 시 기존 `QuestionErrorCode` 체계로 400/404.

프론트는 채점할 때마다 `{questionId, choiceId, relationQuestionId}`를 체인으로 쌓아두고, 마지막에 `POST /api/solved-sessions`의 `rootQuestion`/`followupQuestions` payload로 그대로 전송한다.

---

## Phase 1 — 백엔드

### 1-1. 보기 선택 결과(채점) 조회 API 추가 (question 도메인)
- [x] `QuestionController`에 `GET /api/questions/{questionId}/choices/{choiceId}` 추가 — "이 문제에서 이 보기를 골랐을 때의 결과"를 조회하는 리소스. 요청 본문 없음(path variable 2개). `@LoginUser` 불필요(상태 변경 없음, 단 인터셉터에 의해 인증은 요구됨)
- [x] presentation dto: `ChoiceGradingResponse` (record, 파일 분리 — CONVENTION.md). ⚠️ service dto에 이미 `ChoiceResult`(보기 목록용)가 있으므로 `Grade*`/`Grading*` 계열로 이름 충돌 회피
- [x] service dto: `ChoiceGradingResult` (조회라 Command는 두지 않고 path variable 2개를 파라미터로 받음)
- [x] `QuestionService.getChoiceGrading(questionId, choiceId)`: 흐름만 읽히게 —
  1. `AnswerChoiceReader`로 문제의 보기 조회 + 요청 보기가 그 문제에 속하는지 검증
  2. 정답 보기 판별 (`AnswerChoice.correct()`)
  3. 고른 보기의 `nextQuestionId()`가 있으면 `QuestionReader`로 꼬리질문(+보기) 조회
- [x] implement 계층: 기존 `QuestionReader`/`AnswerChoiceReader`/`AnswerChoiceValidator` 재사용, `QuestionReader.read(questionId)` + `QuestionErrorCode.QUESTION_NOT_FOUND` 추가

### 1-2. 세션 저장 API 인증 연결
- [x] `SolvedSessionController.create`의 `Long userId` → `@LoginUser AuthContext` 로 교체

### 1-3. 테스트 (TEST.md 컨벤션)
- [x] `QuestionServiceTest` — 보기 선택 결과 조회 단위/서비스 테스트: 정답/오답, 오답 해설 노출, 꼬리질문 반환, `relatedQuestionId` 없는 보기 → `nextQuestion: null`, 문제-보기 불일치 예외, 미존재 문제 예외
- [x] `QuestionControllerTest` — `GET /api/questions/{questionId}/choices/{choiceId}` (RestAssuredMockMvc): 성공, 문제-보기 불일치(400), 미존재(404)
- [x] `SolvedSessionControllerTest` — `@LoginUser` 적용 후 요청 스펙 갱신 (`userId` 쿼리파라미터 제거, 토큰 기반)
- [x] `MultipleChoiceSolvingIntegrationTest` 확장 — "보기 선택 결과 조회로 꼬리질문 2개를 따라가며 풀고 저장" 전체 시나리오 (본질문 채점 조회 → 꼬리1 채점 조회 → 꼬리2 채점 조회 → 세션 저장 → SolvedSession/SolvedMultipleChoice/WrongNote 검증)

### 1-4. 시드 데이터
- [x] 로컬 개발/프론트 연동용: 본질문 1 + 보기별 꼬리질문 체인(깊이 2)이 구성된 객관식 데이터 (`data.sql`). 본질문의 1번 보기 → Q2 → Q4, 2번 보기 → Q3 → Q5로 보기별 분기. 기존 `data.sql`의 존재하지 않는 `is_root` 컬럼 참조 제거, 기본 프로파일 `sql.init.mode: never` 설정(시드는 local 프로파일 전용)

## Phase 2 — 프론트 (`front/`)

### 2-1. API 클라이언트 & 타입
- [ ] `lib/` 또는 `src/api/`에 solve 관련 API 함수 추가 (`apiFetch` 사용):
  - `fetchQuestions(filters)` → `GET /api/questions`
  - `gradeQuestion(questionId, choiceId)` → `GET /api/questions/{questionId}/choices/{choiceId}`
  - `saveSolvedSession(payload)` → `POST /api/solved-sessions`
- [ ] `types/index.ts`에 서버 응답 기준 타입 추가: `QuestionResponse`, `ChoiceResponse`, `ChoiceGradingResponse`, `CreateSolvedSessionRequest`. 기존 mock 타입(`MultipleChoiceQuestion.followups`, `options`, `answer` 인덱스)은 단계적으로 제거

### 2-2. ProblemBank — 목록 API 연동 (시나리오 1)
- [ ] `mocks/problemBank.ts` 대신 `GET /api/questions` 호출로 목록 렌더 (필터/검색 파라미터 매핑)
- [ ] 문제 선택 시 `qi` 인덱스가 아니라 선택한 `QuestionResponse` 객체를 quiz 단계로 전달

### 2-3. MultipleChoiceQuiz — 보기별 분기로 재설계 (시나리오 2–4)
- [ ] 상태 모델 교체: `seq = [question, ...followups]` 고정 배열 → **채점 응답으로 자라나는 체인**
  ```
  solvedItems: { question, selectedChoiceId, gradeResult }[]   // 채점 완료된 문항들
  currentQuestion: QuestionResponse                            // 현재 풀이 중 문항
  ```
- [ ] "정답 확인" = `gradeQuestion()` 호출 → 응답으로 정답 표시·해설(`explanation`, 오답 시 `choiceExplanation`) 렌더 → `nextQuestion`을 다음 문항으로 세팅
- [ ] `nextQuestion: null`이면 진행 종료 → 저장하기 버튼 노출 (기존 revealed/탭 UI는 solvedItems 기반으로 유지)
- [ ] 채점 후 선택 변경 불가(1문항 1회 응답) 정책 유지
- [ ] 채점 API 실패 시 에러 표시(선택 상태 유지, 재시도 가능)

### 2-4. 저장하기 (시나리오 5)
- [ ] "저장하기" 클릭 시 solvedItems 체인으로 `CreateSolvedSessionRequest` 구성:
  - `rootQuestion` = 첫 문항, `followupQuestions` = 이후 문항들
  - 각 항목 `{ questionId, choiceId, relationQuestionId: 고른 보기의 relatedQuestionId }`
- [ ] `POST /api/solved-sessions` 성공 후 result 화면 이동 (정답/오답 수는 gradeResult 집계로 표시)
- [ ] 저장 실패 시 재시도 가능하게 (풀이 상태 유지), 중복 클릭 방지
- [ ] "종료하기"(중도 이탈)는 저장하지 않고 복귀 — 도메인 정책 그대로

### 2-5. 정리
- [ ] `mocks/questions.ts`·`mocks/problemBank.ts`의 객관식 데이터 및 관련 mock 타입 제거 (서술형 mock은 유지)

## Phase 3 — 검증
- [ ] `./gradlew test` 전체 통과
- [ ] 백엔드 `bootRun` + 프론트 dev 서버로 E2E 수동 검증: 로그인 → 문제은행 → 본질문 → (보기별로 다른) 꼬리질문 2개 → 저장 → DB에 `SolvedSession`(COMPLETED, total=3) / `SolvedMultipleChoice` 3건 / 오답 시 `WrongNote` 생성 확인
- [ ] 분기 확인: 같은 문제에서 다른 보기를 골랐을 때 다른 꼬리질문이 나오는지

---

## 작업 순서 & 브랜치

1. `feature/mc-grading-api` — Phase 1 (백엔드 채점 API + `@LoginUser` 연결 + 테스트 + 시드)
2. `feature/mc-solving-api-integration` — Phase 2 (프론트 API 연동 + 분기 재설계 + 저장)
3. Phase 3 검증 후 머지

## 범위 제외 (추후)
- 세션 조회/오답노트 조회 API (QuizResult·오답 페이지의 mock 해소)
- 중단 세션 저장, 서술형(ESSAY) 풀이, AI 꼬리질문 생성
