# 002 — 서술형 QA 선행 작업: 문제은행 목록 + 세션 저장 사용자 식별

> 브랜치: `feature/essay-question-list`
> 근거 문서: `plans/001-essay-solved-session.md`(§목표 시나리오 / D7), `docs/API.md`(공통 규칙),
> `docs/ARCHITECTURE.md`, `docs/CONVENTION.md`, `docs/TEST.md`

`plans/001-essay-solved-session.md`의 목표 시나리오를 실제로 QA 하려다 발견한 두 개의 갭을 메운다.
**백엔드 전용 PR**이며, 화면 연결(`front/`)은 후속 PR로 분리한다.

## 목표 시나리오 (QA 흐름)

1. 사용자가 **문제은행 목록에서 서술형 문제를 찾아** 연다. **← 이번 작업**
2. 답변1 제출 → 채점 + 꼬리질문1. *(구현됨)*
3. 답변2 제출 → 채점 + 꼬리질문2. *(구현됨)*
4. 답변3 제출 → 채점만(`nextFollowup = null`). *(구현됨)*
5. "저장하기" → 3문항 스냅샷을 하나의 세션으로 저장. *(구현됨, 단 **저장 주체가 누락**)* **← 이번 작업**
6. 세션이 `COMPLETED`로 남고 오답이 있으면 오답노트가 생성된다. *(구현됨)*

> 1번에서 **서술형이 목록에 아예 나오지 않고**, 5번이 **`userId`를 못 받아 500으로 실패**하는 것이 이번 갭이다.

### 저장되는 곳 (5·6번의 결과)

`EssaySolvedSessionService.create`가 한 트랜잭션에서 세 테이블에 쓴다. FK 없이 `solved_session.id`를 `Long`으로 참조한다.

| 테이블 | 행 수 | 내용 |
| --- | --- | --- |
| `solved_session` | 1 | `userId`, `type=ESSAY`, `status=COMPLETED`, `totalCount=3`, `correctCount`(relay된 `isCorrect` 합), `solvedAt`, `createdAt` |
| `essay_solved` | 3 | 문항 스냅샷 — `sequence 1~3`, `type`(1번 `MAIN`·2·3번 `FOLLOWUP`), `questionId`(MAIN만 값), `questionText`·`userAnswer`·`feedback`·`modelAnswer`(각 `TEXT`), `isCorrect`, `solvedAt` |
| `wrong_note` | 0 또는 1 | 오답이 하나라도 있으면 `(userId, solvedSessionId, isBookmarked=false)`. `unique(user_id, solved_session_id)`로 세션당 최대 1행 |

> 로컬 DB는 H2 인메모리(`jdbc:h2:mem:whynago`, `ddl-auto: create-drop`)라 **앱 재시작 시 저장 결과가 사라진다.**
> QA 중 확인은 앱이 떠 있는 동안 해야 하고, 유지가 필요하면 `DB_URL`·`DB_USERNAME`·`DB_PASSWORD`로 MySQL을 붙인다.

## 현재 상태 요약

### 이미 구현됨
- **서술형 풀이 전 구간**: 본질문 조회(`GET /api/questions/{id}/essay`), 세션 시작(`POST .../essay/sessions`), 채점·꼬리질문(`POST .../essay/answers`), 완료 세션 저장(`POST /api/solved-sessions/essay`).
- **목록 조회 골격**: `GET /api/questions` + 유형·난이도·카테고리·키워드 필터, 꼬리질문(다른 문제의 선택지에서 참조되는 문제) 제외 규칙.
- **인증 인프라**: `AuthInterceptor` + `@LoginUser AuthContext` 리졸버. 객관식 저장(`SolvedSessionController`)이 이미 이 방식으로 `userId`를 받는다. **서술형이 그대로 대칭 참조할 레퍼런스.**
- **로컬 서술형 시드**: `src/main/resources/data.sql`에 `id 101~104` 서술형 4문항.
- **프론트 목록 화면**: `front/src/components/solve/ProblemBank.tsx`가 이미 유형 칩(`전체`/`객관식`/`서술형`)으로 같은 엔드포인트에 `type`을 보낸다.

### 갭 (이번에 개발할 것)

| # | 갭 | 위치(수정) |
| --- | --- | --- |
| G1 | 목록 쿼리가 `q.type = MULTIPLE_CHOICE`를 **하드코딩**해 서술형이 절대 조회되지 않음. `?type=ESSAY`는 `MULTIPLE_CHOICE AND ESSAY`가 되어 **항상 빈 배열** | `question/infra/QuestionRepository.java:17` |
| G2 | 목록 조회 메서드명이 객관식 전용 의미(`findRootMultipleChoices`·`readRootMultipleChoices`)로 굳어 있음 | `question/infra/QuestionRepository.java`, `question/implement/QuestionReader.java` |
| G3 | 서술형 항목은 선택지가 없는데 목록 조립이 유형 구분 없이 선택지를 조회 | `question/service/QuestionService.java:42` |
| G4 | 서술형 세션 저장이 `userId`를 인증 계층에서 받지 않음(`Long userId` 무애노테이션) → `@RequestParam` 취급이라 `?userId=`를 붙이지 않으면 `null`. `essay_solved.user_id`가 NOT NULL이라 문항 저장에서 터져 **500 `SERVER_ERROR` + 전체 롤백**(= 저장 불가) | `solvedsession/presentation/EssaySolvedSessionController.java:24` |
| G5 | 목록 조회 API가 `docs/API.md`에 아예 문서화되어 있지 않음 | `docs/API.md`(Question API) |

## 설계 결정 (핵심 선택과 이유)

- **D1. 신규 엔드포인트를 만들지 않고 기존 `GET /api/questions`가 서술형까지 반환하게 한다.** — 문제은행은 한 표에 두 유형을 섞어 보여주는 화면이고(`ProblemBank.tsx`), 프론트가 이미 `type` 파라미터로 필터링한다. 엔드포인트를 유형별로 쪼개면 "전체" 필터에서 클라이언트가 두 API를 호출해 병합·정렬해야 한다. (대안: `GET /api/questions/essay` 신설 — 서버는 단순해지지만 목록 화면 책임이 프론트로 넘어감)
- **D2. JPQL에서 하드코딩된 타입 조건만 제거하고, 꼬리질문 제외 조건은 그대로 둔다.** — 서술형 꼬리질문은 세션마다 AI가 생성해 `Question` 행이 없다(→ `docs/DOMAIN.md` §서술형 꼬리질문 생성 정책). 즉 서술형은 `AnswerChoice.relatedQuestionId`에 등장할 수 없어 기존 제외 조건에 걸리지 않고 **전부 루트로 조회된다.** 유형별 분기 쿼리가 필요 없다.
- **D3. 메서드명을 실제 의미로 정정한다(`findRootMultipleChoices` → `findRootQuestions`, `readRootMultipleChoices` → `readRootQuestions`).** — 이름이 "객관식만"을 약속하면 다음 사람이 같은 오해를 반복한다. 호출부가 3곳뿐이라 비용이 낮다.
- **D4. 응답 DTO는 `QuestionResponse`를 그대로 쓰고, 서술형은 `choices: []`로 내린다.** — 유형별 응답 스키마를 나누면 프론트 타입·렌더링이 갈라진다. 대신 **서술형은 선택지 조회 쿼리 자체를 생략**한다(`Question.isEssay()`). 트레이드오프: service에 유형 분기 한 줄이 생기지만, 의미 없는 쿼리를 문항 수만큼 날리지 않는다.
- **D5. `userId`는 `@LoginUser AuthContext`로 받는다.** — `plans/001-essay-solved-session.md` D7("userId는 인증 계층에서 컨트롤러로 전달")과 `docs/API.md` 공통 규칙을 지키고, 객관식(`SolvedSessionController`)과 형태를 맞춘다.
- **D6. 컨트롤러 테스트의 `queryParam("userId", 10L)` 우회를 제거한다.** — 현재 테스트가 쿼리 파라미터로 값을 넣어주고 있어 버그를 가리고 있다(그래서 서비스를 목으로 세운 컨트롤러 테스트는 통과하고, 실제 DB 저장이 걸리는 QA에서만 500으로 드러난다). 제거하면 `eq(10L)` 검증이 "토큰에서 해석된 userId"를 실제로 확인한다.
- **D7. 목록 응답의 `explanation`(해설) 노출은 이번에 건드리지 않는다.** — 풀기 전 목록에 해설이 실려 나가는 건 객관식에서 이미 그런 동작이라, 유형 확장과 별개 문제다. 범위를 섞지 않고 별도 이슈로 넘긴다.

---

## Phase 1 — 세션 저장 사용자 식별 (G4)

- [x] `EssaySolvedSessionController` — `Long userId` → `@LoginUser AuthContext authContext`, 서비스 호출에 `authContext.id()` 전달 *(D5; `SolvedSessionController`와 동일 형태)*
- [x] `EssaySolvedSessionControllerTest` — `queryParam("userId", 10L)` 4곳 제거 *(D6; 우회 제거로 `eq(10L)` 검증이 실제 의미를 갖게 됨)*

> 커밋 단위: `fix:` — 데이터가 잘못 쌓이는 버그라 목록 기능과 분리한다.

## Phase 2 — 목록 조회에 서술형 포함 (G1, G2, G3)

- [x] `QuestionRepository` — JPQL에서 `q.type = MULTIPLE_CHOICE` 제거, 메서드명 `findRootQuestions`로 변경 *(D1·D2·D3)*
  ```
  where q.id not in (select ac.relatedQuestionId from AnswerChoice ac where ac.relatedQuestionId is not null)
    and (:type is null or q.type = :type)
    and (:difficulty is null or ...) and (:category is null or ...) and (:keyword is null or ...)
  order by q.id desc
  ```
- [x] `QuestionReader.readRootQuestions(...)` — 이름 변경 *(D3)*
- [x] `QuestionService.findQuestions` — 서술형이면 선택지 조회를 생략하고 빈 목록으로 조립 *(D4; `readChoices(Question)` private 메서드로 분리)*

## Phase 3 — 테스트 (G1~G4)

`docs/TEST.md` 준수(`@DisplayName` 한글 `~다.`, Given-When-Then, AssertJ, RestAssuredMockMvc, Testcontainers).

- [x] `QuestionRepositoryTest` — 유형 미지정 시 **객관식 루트 + 서술형**이 함께 조회되고 꼬리질문은 제외된다 / `type = ESSAY`면 서술형만 조회된다 *(G1·D2 핵심. 기존 `findRootMultipleChoices` 테스트는 이름·호출 수정)*
- [x] `QuestionServiceTest` — 서술형 목록 항목은 `choices`가 비어 있고 태그는 매핑된다 *(D4)*
- [x] `QuestionControllerTest` — `GET /api/questions?type=ESSAY` 200, `choices` 빈 배열 응답 계약 *(응답 형태 회귀 방지)*
- [x] `QuestionFixture` — **추가하지 않음.** 기존 `essayRoot()` 하나로 유형 분기·필터 검증이 모두 가능해 불필요했다.

## Phase 4 — 문서 & 마무리 (G5)

- [x] `docs/API.md` Question API에 **문제 목록 조회** 섹션 신설 — 쿼리 파라미터, 서술형 포함 규칙, 서술형의 `choices: []`, 꼬리질문 제외 규칙 명시 *(현재 미문서화 상태 해소 + 계약 변경 기록)*
- [x] `./gradlew test` 전체 그린 확인 — **27개 클래스 / 118개 테스트, 실패·스킵 0**
  - `RepositoryTestSupport`(`@DataJpaTest` + `TestcontainersConfiguration`)는 Docker(Testcontainers MySQL)를 쓰고, 이 환경에서 정상 실행됐다
  - `IntegrationTestSupport`·`ControllerTestSupport`는 H2(`MODE=MySQL`)·목 기반이라 Docker 없이 실행된다
- [ ] PR 생성 — 백엔드 단독 PR. 서술형이 목록에 노출되는 계약 변경이라 **후속 프론트 PR 선행 조건**임을 본문에 명시

---

## 범위 제외 (이번 작업 아님)

- **화면 연결**: `front/src/components/solve/EssayQuiz.tsx` mock → 실서버 전환, `fetchQuestions` 주석·`front/CLAUDE.md` 연동 범위 갱신 → **후속 프론트 PR**.
- **세션 조회/목록/결과 API**: 여전히 없다. 이번 PR 단독으로는 시나리오 6번(`COMPLETED`·`correctCount`·오답노트 생성)을 HTTP로 확인할 수 없어 **통합 테스트와 DB 직접 확인으로 검증**한다. 조회 API는 별도 이슈.
- **목록 응답의 해설 노출**: D7 — 별도 이슈.
- **요청 바인딩 예외의 400 변환**: 작업 중 확인된 별개 문제. `?type=FOO`처럼 enum에 없는 값을 보내면 `MethodArgumentTypeMismatchException`이, 잘못된 JSON 본문을 보내면 `HttpMessageNotReadableException`이 `GlobalExceptionHandler`의 catch-all로 떨어져 **500 `SERVER_ERROR`** 가 된다. `docs/EXCEPTION.md` 기준으로는 `400 INVALID_INPUT`이어야 한다. 전 엔드포인트에 영향을 주는 변경이라 이번 PR과 분리하고, `docs/API.md`에 알려진 문제로 남겼다.
- **QA 요청 파일(`.http`) 커밋**: 수동 확인용으로만 쓰고 저장소에 넣지 않는다.
- **페이징·정렬 옵션**: 현재 `order by q.id desc` 고정. 프론트 "최신순" 라벨도 고정값이라 이번 범위 아님.
- **`SolvedSession.userId` NOT NULL 제약 추가**: `essay_solved.user_id`는 NOT NULL인데 `solved_session.user_id`는 nullable이라 제약이 비대칭이다. 인증 연결로 null 유입 경로는 막히므로, 스키마 제약 강화는 기존 데이터 영향 검토와 함께 별도 이슈로 분리.

## 커밋 계획

| 순서 | 커밋 | 범위 |
| --- | --- | --- |
| 1 | `fix: 서술형 세션 저장 사용자 식별을 인증 토큰 기반으로 연결` | Phase 1 |
| 2 | `feat: 문제은행 목록 조회에 서술형 문제 포함` | Phase 2 + Phase 3 |
| 3 | `docs: 문제 목록 조회 API 명세 추가` | Phase 4 |

---

# 트랙 F — 서술형 화면 연결 (별도 PR)

> 위(트랙 A)는 백엔드, 트랙 F는 `front/` 화면 연결이다.
> **브랜치·PR을 나눈다**: 브랜치 `feature/essay-solving-api-integration`.
> 트랙 A가 머지된 뒤 `main`에서 분기하는 것이 기본이고, 병행이 필요하면 트랙 A 브랜치에서 분기해
> 머지 후 base를 `main`으로 바꾼다. 저장소 선례(PR #16 백엔드 → PR #23 프론트)와 같은 형태다.
> 트랙 F는 **트랙 A의 목록 확장에 의존**한다(목록에 서술형이 나와야 진입 지점이 생긴다).

## 목표 시나리오 변화

현재: 문제은행에서 **서술형을 고를 수 없고**, 억지로 서술형 화면에 들어가도 목데이터 1번 문항이 뜬다.
3문항·피드백·모범답안이 처음부터 하드코딩돼 있어 AI 채점 없이 "제출"만 하면 미리 준비된 피드백이 펼쳐진다.
"저장하기"는 아무것도 저장하지 않고 목록으로 돌아간다.

변경 후:
1. 목록에서 서술형 문제를 고르면 **그 문제로** 서술형 화면에 진입하고, 서버가 발급한 `conversationId`로 세션이 시작된다.
2. 답변을 제출하면 서버 LLM 채점 결과(피드백·모범답안·통과 여부)가 표시되고, **다음 꼬리질문은 그 응답으로 처음 도착한다.**
3. 3턴째 응답에 `nextFollowup`이 없으면 면접이 끝난다.
4. "저장하기"를 누르면 누적한 3문항 스냅샷을 저장하고 결과 화면으로 이동한다.

## 현재 상태 요약

### 이미 구현됨
- **화면 골격**: `EssayQuiz`의 좌(질문 목록)·우(답변·피드백 아코디언) 레이아웃, 진행 표시, 완료 배너.
- **객관식 연동 레퍼런스**: `MultipleChoiceQuiz`가 `current`/`solvedItems` 상태로 문항을 응답에서 이어받고, `grading`/`saving`/`error` 상태와 "저장하기 → `onFinish`" 흐름을 이미 갖췄다. **서술형이 그대로 대칭 참조할 레퍼런스.**
- **공통 API 클라이언트**: `apiFetch`(base URL·Bearer 자동 부착·`ApiError` 변환), 도메인 API 함수 모음 `lib/questions.ts`, enum↔라벨 매핑(`CATEGORY_LABELS`·`DIFFICULTY_LABELS`).
- **결과 화면**: `QuizResult`가 `type: "서술형"`을 이미 지원한다.
- **목록 화면**: `ProblemBank`의 유형 칩이 이미 `서술형`을 보내므로 **트랙 A만 끝나면 목록·필터는 수정 없이 동작한다.**

### 갭 (이번에 개발할 것)

| # | 갭 | 위치(수정/제거) |
| --- | --- | --- |
| F1 | 클릭한 문제와 무관하게 목데이터 `essayQuestions[0]`을 넘김 | 수정 `app/solve/page.tsx:53` |
| F2 | `EssayQuiz`가 목 타입 `EssayQuestion`을 받고, **3문항·피드백·모범답안을 처음부터 다 안다고 가정** | 수정 `components/solve/EssayQuiz.tsx` |
| F3 | 서술형 API 응답/요청 타입 없음(세션 시작·채점·저장) | 수정 `types/index.ts` |
| F4 | 서술형 API 함수 없음 | 수정 `lib/questions.ts` |
| F5 | "저장하기"가 `onQuit()`만 호출 — 저장 미연동, 결과 화면으로도 가지 않음 | 수정 `EssayQuiz`·`app/solve/page.tsx` |
| F6 | 통과 여부가 하드코딩(`"정답입니다"`) — 채점 결과와 무관 | 수정 `EssayQuiz.tsx:178` |
| F7 | 목 데이터·타입 잔존 | 제거 `mocks/questions.ts`, `types/index.ts`의 `EssayQuestion` |
| F8 | 연동 범위 문서가 "서술형 미구현"으로 남아 있음 | 수정 `front/CLAUDE.md`, `lib/questions.ts` 주석, `app/solve/page.tsx:50` 주석 |

## 설계 결정 (핵심 선택과 이유)

- **DF1. `EssayQuiz`도 목록 응답 `QuestionResponse`를 그대로 받고, `GET /api/questions/{id}/essay`는 호출하지 않는다.** — 목록 응답에 발문·난이도·카테고리·태그가 모두 있어 추가 왕복이 불필요하다. `MultipleChoiceQuiz`와 프롭 타입이 같아져 `page.tsx`의 분기가 "어느 컴포넌트를 띄울지"만 남는다. 트레이드오프: 서술형 상세 조회 엔드포인트가 프론트 미사용으로 남는다(딥링크·새로고침 복구 도입 시 사용처가 생긴다). *(관련: 트랙 A D4 — 서술형은 `choices: []`)*
- **DF2. 문항 시퀀스를 채점 응답으로 성장시킨다.** — 현재는 `seq`를 목데이터로 미리 3개 만들어 두지만, 실제로는 꼬리질문이 `nextFollowup`으로만 도착한다. `MultipleChoiceQuiz`처럼 "채점 완료 문항 배열 + 진행 중 문항"으로 관리한다. *(백엔드 D1 — 저장 시 재채점 없음, 화면이 누적 보관 주체)*
- **DF3. 세션 시작은 화면 진입 시 1회 호출하고 `conversationId`를 상태에 보관한다.** — 서버 발급 방식(백엔드 DB2). 실패하면 답변 입력을 막고 재시도를 노출한다.
- **DF4. 저장 스냅샷은 채점 응답을 누적한 값으로 만들고, 클라이언트에서 재계산하지 않는다.** — 본질문은 `questionId = question.id`, 꼬리질문은 `questionId: null`, `isCorrect`는 채점 응답값을 그대로 relay. *(백엔드 D1·D2·D5·D9 계약)*
- **DF5. 저장 성공 후 `onFinish(correctCount, 3)`로 결과 화면으로 간다.** — 객관식과 동일 흐름이고 `QuizResult`가 이미 서술형을 지원한다. 저장 실패 시에는 화면에 남아 재시도한다.
- **DF6. 통과 여부는 `grading.isCorrect`로 표시하고 하드코딩 문구를 제거한다.** — 오답이면 오답노트 자동 저장 안내를 띄워 객관식 문구와 대칭을 맞춘다. *(백엔드 D9/DB9 — 서버 산출값)*
- **DF7. 목 데이터·타입(`essayQuestions`, `EssayQuestion`)을 완전히 제거한다.** — 사용처가 이 화면뿐이다(확인 완료). 객관식 전환 때와 동일한 정리(`fc3206c refactor: 객관식 mock 데이터와 타입 제거`). `mocks/questions.ts`는 파일 전체가 이 목데이터라 파일째 삭제한다.
- **DF8. 중도 이탈은 저장하지 않는다.** — 완료 세션만 저장한다(`docs/DOMAIN.md` §세션 집계 정책). 객관식과 동일하게 진행 중에는 "종료하기"를 노출한다.
- **DF9. 채점 대기·실패를 인라인으로 처리한다.** — 채점은 LLM 왕복이라 수 초가 걸린다. 제출 버튼을 "채점 중…"으로 비활성화하고, 실패 시 `ApiError.message`를 인라인 표시해 같은 답변으로 재시도할 수 있게 한다(`503 ESSAY_AI_UNAVAILABLE` 포함).

---

## Phase F1 — 타입·API 함수 (F3, F4)

`front/CLAUDE.md` 준수(서버 스펙 그대로 `types/index.ts`, 도메인 API는 `lib/questions.ts`, `apiFetch` 경유).

- [x] `types/index.ts` — 서술형 API 타입 추가: 세션 시작 응답(`conversationId`), 채점 요청(`conversationId`·`question`·`answer`), 채점 응답(`grading{feedback, modelAnswer, isCorrect}`·`nextFollowup{question} | null`), 저장 요청 문항(`questionId | null`·`questionText`·`userAnswer`·`feedback`·`modelAnswer`·`isCorrect`), 저장 요청
  - 저장 **응답**은 객관식과 스키마가 같아(`{sessionId}`) `CreateSolvedSessionResponse`를 재사용하고 주석만 "객관식·서술형 공용"으로 정정했다. 동일 스키마를 이름만 바꿔 복제하지 않는다.
- [x] `lib/questions.ts` — `startEssaySession(questionId)`, `evaluateEssayAnswer(questionId, request)`, `saveEssaySolvedSession(request)` 추가
- [x] `lib/questions.ts` — `fetchQuestions` 주석의 "루트 객관식 문제만 반환됨" 정정 *(트랙 A로 서술형 포함됨)*

## Phase F2 — EssayQuiz 실서버 전환 (F2, F5, F6)

- [x] 프롭 타입을 `QuestionResponse`로 변경, 라벨은 `CATEGORY_LABELS`·`DIFFICULTY_LABELS`로 매핑 *(DF1; 현재 목 타입은 `cat: "네트워크"`처럼 한글 라벨을 직접 들고 있음)*
- [x] 진입 시 세션 시작 호출 → `conversationId` 보관, 실패 시 입력 차단 + 재시도 *(DF3; StrictMode 이펙트 이중 실행으로 대화가 두 개 발급되지 않도록 `startedRef` 가드)*
- [x] 답변 제출 → 채점 API 호출 → `{questionText, userAnswer, feedback, modelAnswer, isCorrect}` 누적, `nextFollowup`이 있으면 다음 문항으로 추가 *(DF2·DF4)*
- [x] `nextFollowup === null`이면 면접 완료 상태로 전환 *(3문항 고정은 서버가 판단 — 화면은 응답만 따른다. 제출 버튼 문구도 "마지막 답변 제출"로 분기하지 않고 "답변 제출" 고정)*
- [x] 문항별 통과/미통과 표시 + 하드코딩된 `"정답입니다"` 제거 *(DF6; 답변 카드 헤더에 `✓ 통과`/`✕ 미통과`, 완료 배너에 오답노트 자동 저장 안내, 푸터에 `통과 n / m`)*
- [x] "저장하기" → 저장 API 호출 → 성공 시 `onFinish(correctCount, 총 문항 수)` *(DF5)*
- [x] 채점·저장 중 버튼 비활성화 + 인라인 에러 *(DF9; `MultipleChoiceQuiz`의 `grading`/`saving`/`error` 패턴 재사용. 채점 실패 시 `draft`를 지우지 않아 같은 답변으로 재시도 가능)*

## Phase F3 — 페이지·목데이터 정리 (F1, F7)

- [x] `app/solve/page.tsx` — `essayQuestions` import·하드코딩 제거, 선택한 `question` 전달, `onFinish={finish}` 연결, "백엔드 미구현" 주석 제거 *(F1·F5)*
- [x] `mocks/questions.ts` 삭제 + `types/index.ts`의 `EssayQuestion` 제거 *(DF7)*

## Phase F4 — 문서 & 수동 QA (F8)

- [x] `front/CLAUDE.md` — "현재 연동된 기능"에 서술형 풀이 추가, 더미 유지 목록에서 제거
- [x] `npm run lint`, `npm run build` 그린 확인 — 경고 0, 15개 라우트 정적 생성 성공
- [ ] 수동 QA (백엔드 `./gradlew bootRun` + `NEXT_PUBLIC_API_BASE_URL` 설정): **미실행 — 앱 실행·브라우저 조작이 필요해 사용자 확인 몫으로 남긴다**
  - [ ] 목록 유형 칩 `서술형` → 시드 문항(101~104)이 보이고, 클릭하면 **그 문항**으로 진입
  - [ ] 3턴 진행: 매 턴 피드백·모범답안·통과 여부 표시, 꼬리질문이 응답에서 이어짐, 3턴째 꼬리질문 없음
  - [ ] "저장하기" → 결과 화면의 정답 수/총 문항 수가 채점 결과와 일치
  - [ ] 오답(`isCorrect: false`) 포함 시 저장 후 오답노트 생성 *(조회 API가 없어 DB로 확인)*
  - [ ] AI 실패(503) 시 인라인 에러 + 같은 답변으로 재시도 가능
  - [ ] 진행 중 "종료하기" → 저장되지 않음

---

## 범위 / 리스크 (트랙 F)

- **트랙 A 선행 의존**: 목록에 서술형이 나오지 않으면 진입 지점이 없다. 트랙 A 머지가 사실상 전제다.
- **대화 메모리 유실**(백엔드 DB7): `conversationId`가 인메모리라 서버 재시작·스케일아웃 시 대화가 사라진다. 이때 서버는 턴 수를 0으로 보고 **꼬리질문을 처음부터 다시 생성**할 수 있어 화면 진행과 서버 상태가 어긋난다. MVP에서는 감수하고, 이상 동작 시 새로 시작하도록 안내만 한다.
- **새로고침 복구 없음**: 진행 상태를 컴포넌트 상태로만 들고 있어 새로고침하면 풀이가 사라진다(저장은 완료 시 1회이므로 중간 데이터도 서버에 없다). 복구·이어풀기는 별도 이슈.
- **채점 지연**: LLM 왕복이라 수 초 소요. 스피너·비활성화로만 대응하고 타임아웃·취소는 이번 범위가 아니다.
- **결과 검증 한계**: 세션 조회 API가 없어 저장 결과는 결과 화면의 정답 수/총 문항 수와 DB 확인으로만 검증한다.
- **범위 제외**: 서술형 오답노트 화면 연동, 딥링크(`/solve/{questionId}`), 이어풀기, 페이징·정렬.

## 커밋 계획 (트랙 F)

| 순서 | 커밋 | 범위 |
| --- | --- | --- |
| 1 | `feat: 서술형 풀이 API 클라이언트와 서버 응답 타입 추가` | Phase F1 |
| 2 | `feat: 서술형 풀이 화면을 API 연동으로 전환` | Phase F2 + Phase F3(page.tsx) |
| 3 | `refactor: 서술형 mock 데이터와 타입 제거` | Phase F3 |
| 4 | `docs: 프론트 연동 범위에 서술형 풀이 반영` | Phase F4 |
