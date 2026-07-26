# 001 — 서술형 완료 세션 저장 (이슈 #20)

> 브랜치: `feature/essay-solved-session`
> 근거 문서: `docs/DOMAIN.md`(§EssaySolved / §서술형 풀이 흐름 정책 / §세션 집계 정책 / §보류),
> `docs/ARCHITECTURE.md`, `docs/CONVENTION.md`, `docs/API.md`

## 목표 시나리오 (사용자 흐름)

서술형은 본 질문 + 꼬리질문2까지 총 3문항을 이어 푸는 하나의 세션이다.

1. 사용자가 문제은행에서 서술형 본 질문(`type = ESSAY`)을 연다.
2. 답변1 제출 → 서버가 LLM으로 채점(피드백·모범답안) 후 꼬리질문1 생성. *(이미 구현됨)*
3. 답변2 제출 → 채점 후 꼬리질문2 생성. *(이미 구현됨)*
4. 답변3 제출 → 채점만 하고 꼬리질문 없음(`nextFollowup = null`). *(이미 구현됨)*
5. 사용자가 **"저장하기"** 를 누르면, 전체 문답 + 채점 결과(3문항)를 하나의 세션으로 저장한다. **← 이번 작업**
6. 저장 결과로 세션이 `COMPLETED` 상태로 남고, 오답이 있으면 오답노트가 생성된다. **← 이번 작업**

> 2~4는 `POST /api/questions/{id}/essay/answers`(Question 도메인)로 이미 동작한다.
> 5~6의 **완료 세션 저장 엔드포인트가 부재**한 것이 이번 이슈의 갭이다.

## 현재 상태 요약

### 이미 구현됨
- **서술형 채점·꼬리질문 생성**: `POST /api/questions/{questionId}/essay/answers` (`EssayAnswerService`, LLM 연동). 저장은 하지 않고 매 턴 처리만 한다.
- **세션 인프라 뼈대**: `SolvedSession` 엔티티가 `type`(`MULTIPLE_CHOICE`\|`ESSAY`)·`status`(`COMPLETED`)·`totalCount`·`correctCount` 를 이미 지원. `ItemType`(MAIN/FOLLOWUP)·`SessionStatus` enum 존재.
- **객관식 완료 세션 저장 플로우 전체**: `SolvedSessionController` → `SolvedSessionService` → `SolvedSessionAppender`/`SolvedMultipleChoiceAppender`/`WrongNoteAppender`. **서술형이 그대로 대칭 참조할 레퍼런스.**
- **오답노트 연동**: `WrongNoteAppender.appendIfWrongAnswer(userId, sessionId, hasWrongAnswer)` 재사용 가능.
- **DOMAIN 명세**: `EssaySolved` 엔티티 필드가 `docs/DOMAIN.md` §EssaySolved 에 확정 명세됨.

### 갭 (이번에 개발할 것)

| # | 갭 | 위치(신규/수정) |
| --- | --- | --- |
| G1 | `EssaySolved` 엔티티 없음 | 신규 `solvedsession/domain/EssaySolved.java` |
| G2 | `EssaySolved` 저장소 없음 | 신규 `solvedsession/infra/EssaySolvedRepository.java` |
| G3 | 서술형 스냅샷 매핑·집계·검증 도구 없음 | 신규 `solvedsession/implement/*` (+`implement/dto/*`) |
| G4 | 서술형용 `SolvedSession`(type=ESSAY) 저장 도구 없음 | 신규 `solvedsession/implement/EssaySessionAppender.java` (**MC `SolvedSessionAppender`·서비스 무수정**) |
| G5 | 서술형 세션 저장 유스케이스 없음 | 신규 `solvedsession/service/EssaySolvedSessionService.java` (+`service/dto/*`) |
| G6 | 서술형 세션 저장 API 없음 | 신규 `solvedsession/presentation/*` |
| G7 | 전 계층 테스트 없음 | 신규 domain/infra/service/controller/integration 테스트 |
| G8 | API 문서 없음 | 수정 `docs/API.md` (SolvedSession API에 서술형 저장 추가) |

## 설계 결정 (핵심 선택과 이유)

- **D1. 저장 시 재채점하지 않고 스냅샷을 그대로 받는다.** — 꼬리질문·채점은 세션마다 AI가 동적 생성해 재사용 `Question`이 없으므로, 저장 시 재실행하면 비결정적·고비용. (`docs/DOMAIN.md` §서술형 꼬리질문 생성 정책)
- **D2. 발문·답변·피드백·모범답안을 `EssaySolved` 행에 스냅샷 저장한다.** — FOLLOWUP은 원본 `Question`이 없어 발문을 남길 곳이 행뿐이기 때문. (`docs/DOMAIN.md` §EssaySolved)
- **D3. 객관식 `solvedsession` 플로우와 대칭으로, 같은 도메인 안에 병렬 클래스로 만든다.** — 세션·오답노트 인프라를 재사용하고 흐름을 한눈에 대응시켜 유지보수성을 높이기 위함. (`docs/ARCHITECTURE.md`)
- **D4. 서술형은 전용 `EssaySessionAppender`(신규)로 `SolvedSession`(type=ESSAY)을 저장한다. 기존 `SolvedSessionAppender`·MC 서비스는 건드리지 않는다.** — **객관식 코드 무수정이 목표.** `SolvedSession`은 공용 엔티티지만 저장 도구를 유형별로 분리해 MC 회귀 위험을 0으로 만든다. 트레이드오프: `SolvedSessionRepository`를 두 appender가 주입하고 `type` 지정이 한 줄씩 중복되나, 결합 제거 이득이 크다. (대안: 기존 클래스에 `append(userId, QuestionType, ...)` 오버로드만 추가하고 기존 4-인자 메서드·MC 호출부는 그대로 두기 — 파일은 공유하되 MC 코드 경로 무변경)
- **D5. `questionId`는 MAIN만 값, FOLLOWUP은 `null`.** — 꼬리질문은 AI 생성이라 참조할 `Question`이 없기 때문. (`docs/DOMAIN.md` §EssaySolved)
- **D6. 문항 수 3 고정을 요청 검증으로 강제한다(본질문1 + 꼬리질문2).** — 서술형 깊이가 고정이므로. (`docs/DOMAIN.md` §서술형 꼬리질문 생성 정책)
- **D7. `userId`는 요청 본문이 아니라 인증 계층에서 컨트롤러로 전달한다.** — MC 컨트롤러와 동일, API 공통 규칙 준수. (`docs/API.md` 공통 규칙)
- **D8. 신규 엔드포인트는 `POST /api/solved-sessions/essay`.** — 기존 객관식 저장(`/api/solved-sessions`)과 같은 리소스 계열이되 유형을 경로로 구분. 컨트롤러는 단일 책임·테스트 격리 위해 신규 분리.
- **D9. (확정) `isCorrect`는 서버가 LLM 채점 결과로 산출한다. 산출은 트랙 B(채점), 저장(A)은 그 값을 스냅샷으로 relay 받는다.** — LLM이 0~10 `score`를 반환하고 서버가 도메인 임계값(7 이상 통과)으로 통과/미통과를 산출한다(→ 트랙 B의 DB9). 산출된 `isCorrect`는 채점 응답으로 반환되고, 클라이언트가 저장 요청 스냅샷에 실어 되돌린다(①·D1·D2 유지, 저장 시 LLM 재호출 없음). 저장 서비스는 relay된 `isCorrect` 합으로 `correctCount`를 집계한다. 출처가 서버이므로 클라 위·변조는 자기 학습 기록에 한정(저위험). **판정 임계값은 `docs/DOMAIN.md` §보류를 해소하는 항목** → 도메인 상수로 명시하고 문서 갱신(Phase 0).

---

## Phase 0 — 판정 기준 확정 (코드 없음)

- [x] (확정) `isCorrect` = 서버가 LLM `score`(0~10)를 **도메인 임계값 7**로 환산한 통과/미통과(`score >= 7` → 통과) *(D9/DB9; 트랙 B에서 산출·A에서 relay)*
- [x] `docs/DOMAIN.md` §보류의 '서술형 정답 판정 기준'을 확정 내용으로 갱신 *(§보류 해소; 트랙 B에서 score/threshold 구현과 함께 '서술형 정답 판정 정책'으로 추가)*

## Phase 1 — 도메인 & 인프라 (G1, G2)

- [x] `domain/EssaySolved.java` — `docs/DOMAIN.md` §EssaySolved 필드대로 엔티티 작성 *(명세가 이미 확정되어 그대로 반영)*
  - 필드: `id, solvedSessionId, userId, type(ItemType), sequence, questionId(nullable), questionText, userAnswer, feedback, modelAnswer, isCorrect, solvedAt`
  - `@NoArgsConstructor(PROTECTED)` + private 생성자 + 정적 팩토리 `create(...)` *(`SolvedMultipleChoice`와 동일 패턴, CONVENTION 준수)*
  - 긴 텍스트는 `@Column(columnDefinition = "TEXT")` *(답변·피드백·모범답안 길이가 큼)*
- [x] `infra/EssaySolvedRepository.java` — `JpaRepository` + `findBySolvedSessionIdOrderBySequence` *(MC 저장소와 대칭, 조회·테스트 검증용)*

## Phase 2 — implement 레이어 (G3)

- [x] `implement/dto/EssaySolvedPayload.java` (record) — `(questionId, questionText, userAnswer, feedback, modelAnswer, isCorrect)` *(레이어 간 값 전달 모델은 별도 파일 record, CONVENTION 준수)*
- [x] `implement/dto/GradedEssayQuestions.java` (record) — `(items, totalCount, correctCount)` + `from(List<EssaySolvedPayload>)` + `hasWrongAnswer()` *(객관식 `ScoredQuestions` 대칭, `correctCount` 집계 지점; `isCorrect` 자체는 relay값=D9, 산출은 B)*
- [x] `implement/EssaySolvedAppender.java` — `appendAll(userId, sessionId, items, solvedAt)`: index 0=MAIN·이후 FOLLOWUP, sequence=index+1 → `saveAll` *(`SolvedMultipleChoiceAppender`와 동일 규칙)*
- [x] `implement/EssaySolvedSessionValidator.java` — 본질문이 존재·`ESSAY`인지 `QuestionReader.readEssayQuestion` 재사용 검증 *(question 도메인 기존 도구 재사용, 중복 구현 회피)*

## Phase 3 — service 레이어 (G4, G5)

- [x] `service/dto/EssaySolvedQuestionCommand.java` (record) + `toPayload()` *(presentation→implement 값 변환, CONVENTION)*
- [x] `service/dto/CreateEssaySolvedSessionCommand.java` (record) — `(rootQuestion, followupQuestions)` + `toPayloads()`, `rootQuestionId()` *(본질문+꼬리질문을 순서 리스트로 평탄화, MC `CreateSolvedSessionCommand`와 대칭)*
- [x] `service/dto/CreateEssaySolvedSessionResult.java` (record) — `(sessionId)` + `from(SolvedSession)` *(응답은 세션 ID만, MC와 동일)*
- [x] `service/EssaySolvedSessionService.java` — 검증 → 집계 → 세션 저장 → 문항 저장 → 오답노트 순으로 흐름만 조립 *(service는 흐름 표현만, ARCHITECTURE 준수)*
  ```
  validate(rootQuestionId) → GradedEssayQuestions.from(payloads)
  → EssaySessionAppender.append(total, correct)
  → EssaySolvedAppender.appendAll(...) → WrongNoteAppender.appendIfWrongAnswer(...)
  ```
- [x] `implement/EssaySessionAppender.java` (신규) — `append(userId, total, correct, solvedAt)`가 `SolvedSession.completed(userId, ESSAY, ...)` 저장. **기존 `SolvedSessionAppender`·`SolvedSessionService` 무수정** *(D4 — 객관식 코드 격리)*

## Phase 4 — presentation 레이어 (G6)

- [x] `presentation/dto/EssaySolvedQuestionRequest.java` — `questionId(nullable), @NotBlank questionText/userAnswer/feedback/modelAnswer, isCorrect` + `toCommand()` *(FOLLOWUP은 questionId null 허용; feedback·modelAnswer도 not-null 컬럼이라 @NotBlank로 400 처리)*
- [x] `presentation/dto/CreateEssaySolvedSessionRequest.java` — `@NotNull @Valid rootQuestion`, `@Size(min=2,max=2) followupQuestions` + `toCommand()` *(3문항 고정 강제 = D6)*
- [x] `presentation/dto/CreateEssaySolvedSessionResponse.java` — `(sessionId)` + `from(result)` *(MC 응답과 동일 형태)*
- [x] `presentation/EssaySolvedSessionController.java` — `POST /api/solved-sessions/essay`, `ResponseEntity.status(CREATED)` *(생성은 201·ResponseEntity 명시, CONVENTION/API 준수; userId는 인증 계층 전달=D7)*

## Phase 5 — 전 계층 테스트 (G7)

`docs/TEST.md` 준수(@DisplayName 한글 `~다.`, Given-When-Then, AssertJ, RestAssuredMockMvc, Testcontainers).

- [x] `domain/EssaySolvedTest` (단위) — `create` 팩토리 필드/타입 매핑 *(도메인 규칙 단위 검증)*
- [x] `implement/GradedEssayQuestionsTest` (단위) — total/correct 집계·`hasWrongAnswer` 경계값 *(D9 집계 로직 핵심)*
- [x] `infra/EssaySolvedRepositoryTest` (`RepositoryTestSupport`) — 저장·sequence 정렬 조회·TEXT 컬럼 생성 *(DB 매핑 검증)*
- [x] `service/EssaySolvedSessionServiceTest` (`IntegrationTestSupport`) — 정상 저장(ESSAY/COMPLETED/total3/correct집계 + 3행 + 오답노트), 본질문 비ESSAY 예외, 전부 정답 시 오답노트 미생성 *(유스케이스 통합 검증)*
- [x] `presentation/EssaySolvedSessionControllerTest` (`ControllerTestSupport`) — 201 정상, rootQuestion 누락/followup 크기 위반/필수 공백 → 400 `INVALID_INPUT` *(요청 검증·응답 계약; 지원 클래스에 컨트롤러·목빈 등록)*
- [x] `integration/EssaySolvingIntegrationTest` (`IntegrationTestSupport`) — ESSAY Question 저장→서비스→DB 저장 끝단 검증 *(전 계층 연결 확인)*
- [x] `QuestionFixture.essayRoot()` 재사용 *(이미 존재 — 신규 추가 불필요)*

## Phase 6 — 문서 & 마무리 (G8)

- [x] `docs/API.md` SolvedSession API에 서술형 저장 엔드포인트 추가 *(연동·리뷰 기준 문서 최신화)*
- [x] `./gradlew test` 전체 그린 확인 *(회귀 없음 보장)*
- [ ] PR 생성 — 이슈 #20 연결, D9 판정 기준 방침·교체 지점 명시 *(트랙 B까지 끝난 뒤 마지막에)*
- [x] `docs/DOMAIN.md` §보류(정답 판정 기준) 갱신 — 트랙 B에서 '서술형 정답 판정 정책'으로 확정 반영

---

## 범위 제외 (이번 작업 아님)

- ~~서술형 정답 판정 기준 확정~~ → **이번 작업에 포함**(D9/DB9): LLM `score` + 서버 임계값으로 확정하고 `docs/DOMAIN.md` §보류를 갱신한다. (단, 임계값 튜닝·다단계 등급 등 고도화는 후속)
- **세션 조회/목록/결과 화면 API**: 저장만 담당. 조회는 별도 이슈.
- **중단(미완료) 세션 저장**: `COMPLETED`만 저장. 중단 세션은 `docs/DOMAIN.md` §세션 집계 정책상 별도 논의.
- **프론트 연동**: `EssayQuiz`의 mock→실서버 전환은 프론트 작업 범위.
- **1일 1면접 세션 통합**: `docs/DOMAIN.md` §보류(추후 MVP).

---

# 트랙 B — 서술형 채점/꼬리질문 생성을 ChatMemory 세션 방식으로 전환

> 트랙 A(이슈 #20)는 **완료 세션 저장**이고, 트랙 B는 그 앞단인 **채점·꼬리질문 생성 흐름**의 리팩터링이다.
> 대상 도메인이 `question`(essay 답변)으로 트랙 A(`solvedsession`)와 다르다.
> **한 브랜치(`feature/essay-solved-session`)에서 둘 다 진행**한다. 코드 수준에서 A·B는 독립이나, `isCorrect`는 B가 산출한 값을 A가 relay하므로 데이터상으론 B에 의존한다(DB9). 강제 순서는 없으며 A→B 권장.

## 목표 시나리오 변화

현재(2~4턴): 클라이언트가 **매 요청에 지금까지의 전체 문답(`thread`)** 을 실어 보내고, 서버가 그걸 프롬프트에 통째로 렌더링해 채점·꼬리질문을 만든다.

변경 후:
1. 서술형 세션을 시작하면 **대화 식별자(conversationId)** 가 발급된다.
2. 답변 제출 시 클라이언트는 **conversationId + 이번 답변만** 보낸다.
3. 서버 `ChatClient`가 `MessageChatMemoryAdvisor`로 그 대화의 이전 문답을 **자동으로 맥락에 주입**해 채점·꼬리질문을 생성한다.
4. **3턴째 채점 직후** 해당 대화 메모리를 비운다(저장 단계와 무관, DB6).

## 현재 상태 요약

### 이미 구현됨
- `GeminiEssayAiClient`: `ChatClient.Builder`로 빌드, `.prompt().user(prompt).call().entity(GradeAndFollowupResult.class)` 구조화 출력. Spring AI `1.1.8`(BOM), OpenAI 호환 스타터를 Gemini에 연결(`application.yml`).
- `renderThread(...)`: 전체 문답을 프롬프트 문자열로 직렬화(← **제거 대상**).
- `EssayAnswerEvaluator`: `thread.size() < 3` 으로 꼬리질문 생성 여부 판단(← **판단 근거 교체 대상**).

### 갭 (이번에 개발할 것)

| # | 갭 | 위치(신규/수정) |
| --- | --- | --- |
| B1 | ChatMemory·메모리 어드바이저 설정 없음 | 신규 config + `GeminiEssayAiClient` 수정 |
| B2 | AI 클라이언트가 `thread` 전체를 받음 | 수정 `EssayAiClient`/`GeminiEssayAiClient` 시그니처 |
| B3 | 꼬리질문 생성 여부를 클라이언트 thread 크기로 판단 | 수정 `EssayAnswerEvaluator`(메모리 턴 수 기반) |
| B4 | 대화 식별자·세션 시작 개념 없음 | 신규 conversationId 발급/전달 |
| B5 | 요청이 전체 thread를 담음 | 수정 `EvaluateEssayAnswerRequest`(현재 답변만) |
| B6 | 세션 종료 시 메모리 정리 없음 | 신규 `ChatMemory.clear(conversationId)` |
| B7 | API 문서가 thread 기반 | 수정 `docs/API.md` |
| B8 | 채점 결과에 통과/판정값 없음(feedback·modelAnswer만) | 수정 `GradeAndFollowupResult`(+`score`) · 채점 응답 DTO(+`isCorrect`) · `EssayAnswerEvaluator`(임계값 산출) |

## 설계 결정 (핵심 선택과 이유)

- **DB0. (확정) 한 브랜치에서 A·B 모두 진행, 상호 독립.** — ①(DB8) 확정으로 저장이 conversationId를 참조하지 않아 강제 순서가 없다. 커밋만 트랙·레이어 단위로 분리해 리뷰·되돌리기를 쉽게 한다.
- **DB1. `ChatClient`에 `MessageChatMemoryAdvisor`를 붙이고 대화 단위로 히스토리를 유지한다.** — 매 요청 전체 문답 재전송·프롬프트 재렌더링을 제거해 토큰·대역을 절감하고 맥락 관리를 인프라로 위임. (Spring AI `ChatMemory`/advisor)
- **DB2. 대화 식별자(conversationId=UUID)를 도입한다. 서술형 세션 시작 시 서버가 발급.** — 3턴을 하나의 메모리 대화로 묶기 위함. (대안: 클라이언트 생성 UUID — 서버 단순하나 신뢰 문제. 서버 발급 권장)
- **DB3. 요청을 현재 턴 답변만으로 축소한다(`thread` 제거, conversationId+answer).** — 이전 맥락은 메모리가 보유하므로 재전송 불필요.
- **DB4. 꼬리질문 생성 여부를 서버가 대화 메모리의 턴 수로 판단한다.** — 클라이언트가 더 이상 전체 thread를 보내지 않아 크기로 못 셈. (턴 수 = 저장된 메시지/응답 수, 3턴째면 생성 안 함)
- **DB5. 면접관 역할·출력 규칙은 default system 메시지로 1회 고정, 유저 메시지는 새 답변만.** — 규칙 중복 전송 제거, 구조화 출력(`.entity(GradeAndFollowupResult.class)`)은 유지.
- **DB6. 3턴째 채점 직후 `ChatMemory.clear(conversationId)`로 대화를 비운다.** — 정리 시점을 저장(A)이 아니라 채점 흐름(B) 안에 둬 트랙 A와의 결합을 없앤다(DB8). 메모리 누수 방지·짧은 수명 대화 정리.
- **DB7. 메모리 저장소는 MVP에서 `InMemoryChatMemoryRepository`(기본)를 쓴다.** — 3턴·수 분 수명이라 허용. ⚠️ **재시작/다중 인스턴스 유실 한계** → 확장 시 `JdbcChatMemoryRepository`(전용 테이블 추가)로 교체. 한계를 PR에 명시.
- **DB8. (확정 ①) 트랙 A 저장은 스냅샷만 받고 conversationId를 넘기지 않는다.** — 저장은 클라이언트가 누적한 채점 스냅샷으로 자기완결적으로 수행(트랙 A의 D1·D2 유지). 저장을 휘발성 메모리(DB7)에 의존시키지 않아 재시작·스케일아웃에도 안전. 트랙 A는 **코드 수준에서 B와 독립**하나, `isCorrect`는 B 채점 산출값을 relay하므로 **데이터상으론 B에 의존**한다(DB9). 메모리 정리는 저장이 아니라 **B의 3턴째 채점 직후**(DB6)에서 수행. *(위·변조 방지가 필요하거나 메모리를 DB로 영속화하는 경우의 conversationId 기반 저장은 post-MVP 재검토)*
- **DB9. (확정) LLM 채점 출력에 `score`(0~10)를 추가하고, 서버가 도메인 임계값 7(`score >= 7` → 통과)로 `isCorrect`를 산출해 채점 응답에 포함한다.** — D9 확정 반영. 판정 정책(임계값)을 서버/도메인이 소유해 프롬프트에 비종속·튜닝 용이. `EssaySolved` 스키마는 그대로(`score` 비영속, `isCorrect`만 저장·relay). 채점 응답에 실린 `isCorrect`를 클라가 저장까지 relay(①·D9).

## Phase별 작업

`docs/ARCHITECTURE.md`(infra에 기술 격리) · `docs/CONVENTION.md`(모델 별도 파일·record) 준수.

### Phase B0 — 의존성 확정
- [x] Spring AI chat-memory/advisor 의존성 확인 — 스타터가 `spring-ai-client-chat` + `spring-ai-autoconfigure-model-chat-memory`를 이미 포함(별도 추가 불필요). MVP는 인메모리(DB7).

### Phase B1 — ChatMemory 인프라 설정
- [x] `ChatMemory` 빈은 **자동설정**(MessageWindowChatMemory + InMemory) 사용 — 별도 @Bean 불필요
- [x] `GeminiEssayAiClient` 빌더에 `MessageChatMemoryAdvisor`를 default advisor로, 면접관 역할을 default system으로 설정 *(맥락 주입·규칙 고정을 인프라에 위임=DB1·DB5)*

### Phase B2 — AI 클라이언트 시그니처 변경 (infra)
- [x] `EssayAiClient.gradeAndGenerateFollowup(String conversationId, String question, String answer, boolean generateFollowup)` 로 변경, `renderThread` 제거 *(thread 전체 대신 현재 턴만=DB3)*
- [x] 호출 시 `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId)).user(현재 질문·답변)` *(대화별 메모리 라우팅)*
- [x] `GradeAndFollowupResult`에 `score`(0~10) 필드 추가 + 프롬프트에 0~10 점수 산출 지시 *(서버가 판정할 원천 신호=DB9)*
- [x] `completedTurns`/`clearSession`을 포트에 추가(ChatMemory 위임) *(누수 방지=DB6)*

### Phase B3 — 턴 판단·판정·정리 로직 (implement)
- [x] `EssayAnswerEvaluator`가 conversationId+현재 질문·답변을 받고, **꼬리질문 생성 여부를 메모리 턴 수로 판단** *(클라이언트 thread 크기 제거=DB4)*
- [x] LLM `score`를 **도메인 임계값 상수(7)**로 환산해 `isCorrect` 산출 → `EssayEvaluation`에 포함 *(판정 정책을 서버/도메인이 소유=D9/DB9)*
- [x] 3턴째 처리 후 메모리 clear 호출 *(대화 종료 정리=DB6)*

### Phase B4 — service / presentation / API
- [x] `EssayAnswerService.evaluate`가 conversationId를 받도록 커맨드 확장 + `startSession` 추가(ConversationIdGenerator) *(대화 라우팅·서버 발급=DB2)*
- [x] 세션 시작 엔드포인트 `POST /api/questions/{id}/essay/sessions` → `{ conversationId }` *(서버 발급 conversationId=DB2)*
- [x] `EvaluateEssayAnswerRequest`를 `{ conversationId, question, answer }`로 축소 + command/response 조정 *(요청 축소=DB3, 현행 설계)*
- [x] 채점 응답(`GradingResponse`)에 `isCorrect` 추가 *(클라가 저장까지 relay=D9/DB9)*
- [x] `docs/API.md` 세션 시작·축소 요청·`isCorrect` 반영 + `docs/DOMAIN.md` §보류(판정 기준) 해소 *(계약 변경 문서화; 프론트 연동 브레이킹 명시)*

### Phase B5 — 테스트
- [x] `EssayAnswerEvaluator` 턴 판단 테스트: 1·2턴 생성, 3턴 미생성+clear *(DB4·DB6 로직)*
- [x] `isCorrect` 산출 테스트: 임계값 경계(6→미통과, 7→통과) *(D9/DB9 판정 로직)*
- [x] 기존 `GeminiEssayAiClientTest`·`EssayAnswerServiceTest`·`QuestionControllerTest` 를 새 시그니처/요청·응답 형식에 맞게 수정 + `startSession`/세션 시작 엔드포인트 테스트 *(계약 변경 반영)*

## 범위 / 리스크 (트랙 B)
- **API 브레이킹 체인지**: 기존 서술형 채점 엔드포인트 요청 형식이 바뀐다 → 프론트와 연동 시점 합의 필요.
- **메모리 유실**(DB7): MVP 인메모리는 재시작·스케일아웃에서 대화 유실. 운영 전 JDBC 저장소 전환 판단.
- **동시성**: 같은 사용자가 여러 서술형을 병행하면 conversationId로 분리되어야 함(발급 정책이 이를 보장).
