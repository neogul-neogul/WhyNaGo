# AI 기반 맞춤형 취약점 진단 — 실행 계획

> **역할** — 이 문서는 "무엇을 어떤 순서로 하고 무엇이 통과 조건인지"를 담는 **실행 문서**다.
> "왜 이렇게 설계했는지"는 [`docs/WEAKNESS.md`](./docs/WEAKNESS.md)(취약점 탐지 전략)와
> [`docs/RECOMMENDATION.md`](./docs/RECOMMENDATION.md)(추천 파이프라인)에 있다. 합치지 않는다.
> **작업이 끝나면 이 파일은 지운다.** 영구 보존할 결정은 `docs/`로 옮긴다.
>
> 기준 브랜치: `feature/essay-rubric-grading` (V6 · `Rubric` · `RubricGradingResolver` · `SolvingTime` 존재)

## 배경

질문은 "어떻게 하면 사용자 취약점을 AI를 통해 맞춤형으로 찾을 수 있을까"였다. 조사 결과 **그 기능은 이미 만들어져 돌아가고 있고, 아무 데도 연결되어 있지 않으며, 두 개의 버그가 그 값을 오염시키고 있다.**

`EssayPromptV6`은 채점할 때마다 `mastery`(6분류)와 `masteryReason`을 받는다. 프롬프트는 근거를 **강제**한다 — "답변에서 근거가 된 부분을 짚어 두 문장 이내", "'더 공부가 필요합니다' 같은 일반론은 금지". 이것이 바로 맞춤형 AI 취약점 진단이다. 그런데:

| 층 | 상태 |
| --- | --- |
| 답변 단위 `masteryReason` | `docs/API.md`와 `front/src/types/index.ts:359-361`에 있고 **렌더링되는 곳이 없다.** `EssayQuiz.tsx:125-127`은 `feedback`/`modelAnswer`/`isCorrect`만 구조분해한다 |
| 태그 단위 현재값 | `GET /api/mastery`가 구현·테스트·약 60줄 문서화되어 있고 **프론트 소비자가 0개다**(`grep api/mastery front/src` → 없음). `front/src/lib/mastery.ts`가 없다 |
| 이력 | `mastery_record.reason`이 매 채점마다 쓰이고 **읽는 코드 경로가 없다.** `MasteryRecordRepository`의 finder 2개는 호출자가 0이다 |

그리고 UI는 이미 없는 기능을 약속한다 — `RecommendationFlow.tsx:13`의 `"오답 이력에서 개념 추출 중"`, `weekly/page.tsx`의 `"매주 월요일 이메일로도 발송돼요"`. (두 mock 화면은 `COMING_SOON_ROUTES`로 차단돼 사용자 도달은 막혀 있다.)

**그래서 순서를 뒤집는다.** 진단을 더 똑똑하게 만들기 전에, 이미 돈을 낸 진단이 볼 만한지부터 확인한다. 지금은 아무도 — 우리 자신도 — 모른다. 한 번도 본 적이 없기 때문이다.

## 확인된 버그 2개

### 버그 1 — 꼬리질문 판정이 본질문을 덮어쓴다

`POST /api/questions/{questionId}/essay/answers`는 본답변과 모든 꼬리답변을 **같은 엔드포인트**로 처리한다. `EssayAnswerService.evaluate`와 `InterviewService.answer`는 턴 검사 없이 루트 `question`으로 `essayMasteryRecorder.record(...)`를 호출한다. 결과:

- `mastery_record`에 태그당 3행(`MAX_TURNS = 3`), 전부 `questionId = 루트`
- `UserTagMastery.refresh`가 덮어쓰므로 **가장 깊은 마지막 프로브가 사용자의 "현재 숙련도"가 된다**
- `MasteryReader.readLatestAiLevelsByQuestion`이 `(older, newer) -> newer`로 병합 → **마지막 꼬리질문이 약점 프로필을 지배한다**
- `docs/RECOMMENDATION.md:141`은 정반대를 적어 놨다: "**서술형 꼬리질문 턴은 기록하지 않는다.**"

즉 본질문을 완벽히 답하고 가장 깊은 프로브에서 흔들린 사용자가 **그 주제를 아예 모르는 것으로 기록된다.** 모든 약점 프로필이 체계적으로 "실제보다 약하게" 편향돼 있다.

### 버그 2 — `levelCounts`가 태그 수만큼 부풀려진다

`MasteryRecordAppender.append`는 **태그당 1행**을 쓴다(`saveAll(command.tagIds().stream()...)`). `MasteryRecordRepository.countByCategoryAndLevel`은 `count(r) group by r.category, r.level`로 `questionId` distinct가 없다. 태그 3개 문항을 한 번 풀면 `levelCounts`에 **3**이 더해진다. `docs/API.md`는 이 필드를 "각 숙련도를 **몇 번** 받았는지"로 설명하는데, 사용자는 이걸 답변 횟수로 읽는다. 버그 1과 겹치면 **3턴 × 3태그 = 9행**, 히스토그램이 약 9배로 부푼다. 화면에 그리는 순간 눈에 띈다.

> 이 두 버그는 "관찰되지 않는 경로는 썩는다"의 증거다. 연결부터 하는 이유가 여기 있다.

## 설계 원칙 — AI를 어디에 둘 것인가

```
AI가 증거를 추출한다 (상류)  →  결정적으로 집계한다 (중간)  →  AI가 서사를 만든다 (하류)
```

**AI를 취약 태그·주제 선정에 넣지 않는다.** `docs/RECOMMENDATION.md`가 명시적으로 결정적이라고 약속한 자리이고, 넣으면 정확히 이렇게 깨진다:

| 자리 | 깨지는 것 |
| --- | --- |
| 취약 태그·주제 선정 | `RecommendationCache`는 `profile.hashCode()`로만 유효성을 본다. AI 산출이 해시에 **안** 들어가면 다른 진단으로 만든 문항을 그대로 서빙한다. **들어가면** 비결정 출력이라 매 요청 캐시 미스 → 요청당 최대 3회 생성 → `RECOMMENDATION_AI_QUOTA_EXCEEDED`. `WeaknessProfile`은 record라 **추가하는 컴포넌트가 자동으로 hashCode에 들어간다** — 이게 함정이다 |
| 약점 가중치 | `MasteryWeight.of`가 순수 함수가 아니면 `RecommendationTopicPolicy`의 `thenComparing(name)` tie-break이 매 요청 뒤집힌다 |
| 점수 | 점수는 서버 소유다(`RubricGradingResolver` + `SolvingTime`). 오개념 신호를 점수에 먹이면 같은 신호가 두 번 먹는다 |
| 요청 경로의 추가 호출 | 서술형 채점은 이미 문항당 최대 3회다. GET에 합성 호출을 얹으면 **사용자가 기다리는 채점 버킷을 429로 만든다** |

**하드 규칙:** `misconception`은 `WeaknessProfile`에서 도달 불가여야 한다. tie-break으로도, 보너스 가중치로도, "계산기 안에서 로깅만"으로도 안 된다. 오개념 텍스트만 다른 두 프로필의 `hashCode()`가 같음을 단정하는 테스트로 고정한다.

---

## 왜 취약점 판별을 AI에 맡기지 않는가

먼저 사실을 바로잡는다. **취약점 판별은 이미 AI가 하고 있다.** `WeaknessProfileCalculator.weightOf`가 문항별 가중치를 고르는 방식이 그 증거다.

```java
// AI가 답변 내용을 보고 판정한 값이 있으면 그것이 우선이다. 시간 기반 판정보다 신호가 강하다.
if (aiLevel != null) {
    return MasteryWeight.of(aiLevel);
}
// 없으면 MasteryPolicy(정답 여부 x 소요시간)로 폴백
```

서술형에 `AI_ESSAY` 판정이 있으면 **AI가 규칙을 이긴다.** 그러니 "AI를 안 쓴다"가 아니라 **어느 층에서 쓰는가**의 문제다.

| 층 | 누가 | 상태 |
| --- | --- | --- |
| 판정 — 이 답변이 무엇을 모르는가 | **AI** (`EssayPromptV6` → `mastery`, `masteryReason`) | 이미 씀 |
| 증거 추출 — 무엇을 무엇으로 착각했는가 | **AI** (`misconception`) | Stage 4 |
| **집계·순위·주제 선정** | **결정적 산술** | **여기만 AI를 쓰지 않는다** |
| 문항 생성 / 서사 | **AI** | 생성은 이미 씀, 서사는 Stage 6 |

아래는 세 번째 층에만 해당하는 이유다. 추상적인 "AI를 믿을 수 없다"가 아니라, 이 저장소의 구체적인 코드가 깨지는 방식이다.

### 1. 캐시 키가 프로필 해시다 — 빠져나갈 구멍이 없다

`RecommendationCache.find`는 유효성을 이렇게만 판단한다.

```java
if (cached == null || !cached.isValid(profile.hashCode(), LocalDate.now(clock))) {
```

`WeaknessProfile`은 record이므로 **컴포넌트를 추가하면 자동으로 `hashCode()`에 들어간다.** 그래서 선택지가 둘뿐이고 **둘 다 깨진다.**

- **해시에 안 들어가면** — 진단이 바뀌었는데 캐시는 유효하다고 판단해, **다른 진단으로 만든 문항을 그대로 서빙한다.** 사용자는 갱신된 약점과 무관한 문제를 받는다.
- **해시에 들어가면** — LLM 출력은 같은 입력에도 문자열이 달라지므로 활동이 없어도 매 요청 해시가 바뀐다. **영구 캐시 미스 → 요청당 최대 3문항 생성 → `RECOMMENDATION_AI_QUOTA_EXCEEDED`.** 게다가 생성 문항은 DB에 계속 쌓인다(`PENDING`).

> 이 함정은 이미 한 발 걸쳐 있다. `tagWeaknesses`는 `List`라 **순서에 따라 `hashCode()`가 달라지는데**, 프로필을 만드는 `EssaySolvedRepository.findByUserIdAndType`과 `SolvedMultipleChoiceRepository.findByUserId`에 **`ORDER BY`가 없다.** 지금도 이론상 캐시 키가 흔들릴 수 있다. 비결정 값을 더 얹기 전에 이걸 먼저 막아야 한다(Stage 5 규칙 2의 양자화·정렬 고정).

### 2. tie-break이 결정성을 전제로 설계돼 있다

`RecommendationTopicPolicy`의 주석이 요구사항을 명시한다 — *"같은 프로필이면 항상 같은 목록이 나와야 하므로 정렬 기준을 끝까지 고정한다."*

```java
.sorted(Comparator
        .comparingDouble((TagWeakness tag) -> -tag.weaknessScore())
        .thenComparing(TagWeakness::name))
```

약점도가 같을 때 태그명으로 가르는 이 마지막 기준이 결정성의 마지막 방어선이다. 순위를 LLM이 정하면 동점 처리가 매번 달라져 **같은 실력의 사용자가 새로고침마다 다른 주제를 받고**, 그만큼 쿼터를 태운다.

### 3. 정보가 늘지 않는다 — 이 층의 입력은 이미 숫자다

순위 층이 보는 것은 SQL과 산술이 이미 계산해 둔 **카테고리 8개의 평균과 태그별 평균**이다. 그 숫자들을 LLM에게 다시 읽혀 내림차순으로 정렬시키는 것은 정보를 추가하지 않고 분산만 추가한다.

AI가 대체 불가능한 곳은 그 숫자를 **만드는** 자리다 — 한국어 서술에서 인과가 뒤집혔음을 알아보는 일. 그건 이미 `mastery`·`masteryReason`이 하고 있고, Stage 4가 한 걸음 더 나간다.

### 4. 검증할 방법이 없다

`docs/RECOMMENDATION.md`가 정책 객체를 domain에 둔 이유를 적어 놨다 — *"`domain` 정책 객체는 LLM·DB·Spring 의존이 없어 전수 단위테스트가 가능하다."* 실제로 `RecommendationTopicPolicyTest`가 경계값과 tie-break을 전수로 고정한다.

LLM 순위 결정기는 그 테스트를 쓸 수 없다. 그리고 **A/B로 확인할 수도 없다** — 정답 레이블이 없고 실사용자가 사실상 1명이다(`docs/WEAKNESS.md`의 같은 한계). 즉 **틀렸는지 알 방법이 없는 코드**를 추천 파이프라인의 중심에 놓는 셈이다.

### 5. 쿼터와 지연이 사용자가 기다리는 경로를 때린다

서술형 채점은 이미 문항당 최대 3회 호출이다(`MAX_TURNS = 3`). 추천 조회 GET에 합성 호출을 얹으면 p99가 초 단위로 늘고, 더 나쁘게는 **같은 미분화 쿼터를 쓰는 채점 경로를 429로 만든다.**

그리고 앱은 남은 예산을 모른다. `common/ai/AiFailureClassifier`는 제공자 에러 문자열(`RESOURCE_EXHAUSTED`, `\b429\b`, `per day`)을 **사후에** 파싱하는 것이 전부다. 카운터도, 리미터도, 재시도도, bulkhead도 없다. 사전 방어 없이 호출을 늘리는 것은 눈을 감고 예산을 쓰는 것이다.

### 6. 집계 키가 자유 텍스트면 애초에 집계가 안 된다

이건 순위 층뿐 아니라 Stage 4의 설계 근거이기도 하다. LLM이 뱉은 개념 이름을 그대로 키로 쓰면 `인덱스` / `B-트리 인덱스` / `클러스터드 인덱스`는 **절대 같은 그룹이 되지 않는다.** 문자열 동등성으로는 묶이지 않기 때문이다. 그래서 Stage 4는 `concept`를 **닫힌 태그 사전으로 강제**한다. 자유 텍스트를 집계 축으로 쓰는 설계는 "prose를 집계 가능한 증거로 바꾼다"는 명분 자체를 무너뜨린다.

### 7. 자기 참조가 한 단계 더 증폭된다

`docs/RECOMMENDATION.md` 리스크 3·4가 이미 경고한다 — 생성 문항의 풀이 결과가 다시 프로필에 반영되고, 그 프로필이 다음 생성의 근거가 된다. 순위까지 AI가 정하면 **진단 자체가 생성 콘텐츠에 의존**하게 되어, 잘못된 문항이 만든 오답이 잘못된 순위를 만들고 그 순위가 또 그 주제의 문항을 생성한다. 끊을 지점이 없어진다.

---

### 무엇이 이 결정을 뒤집는가

이건 취향이 아니라 조건부 판단이다. 아래가 참이 되면 다시 본다.

| 조건 | 그러면 무엇이 가능해지는가 |
| --- | --- |
| 캐시 키를 프로필 해시에서 분리하고(명시적 버전·양자화 키) `ORDER BY`를 고정한다 | 비결정 신호를 프로필에 넣어도 캐시가 무너지지 않는다 |
| 사전 예산 회계(`AiDailyBudget`)와 버킷 분리가 들어온다 | 요청 경로에 호출을 얹는 논의가 시작될 수 있다 |
| 사용자가 늘고 leave-one-out에 유의성이 생긴다 | 순위 방식을 실제로 비교·검증할 수 있다 |
| Stage 3의 재현성 게이트를 통과한다 | AI 증거를 **집계 축으로** 쓸 수 있다 |

### 다만 — 순위를 건드리지 않고 생성만 개인화하는 중간 길이 있다

`EssayQuestionGenerator`는 이미 `AnswerChoiceReader.readWrongExplanations(...)`로 문제은행의 오답 해설을 `[오개념 카탈로그]`로 프롬프트에 주입한다. 즉 **남의 오개념**을 넣고 있다. 같은 자리에 **그 사용자 본인의 오개념**(Stage 4의 `misconception`)을 넣으면, 취약 태그 선정은 결정적으로 유지하면서 문항 내용만 개인화된다. 순위에 손대지 않으므로 위 1·2번이 발생하지 않는다.

정직한 대가: 캐시 키가 프로필 해시이므로 **오개념이 새로 쌓여도 프로필 점수가 안 바뀌면 최대 24시간은 옛 문항이 나간다.** 지연이 생기는 것이지 틀리는 것은 아니다.

이것이 "AI로 취약점을 찾아 맞춤 문제를 만든다"에 가장 가까운 형태이며, **Stage 3 게이트 통과를 전제로 Stage 4.5로 둔다.**

---

## Stage 0 — 버그 2개 수정 · **AI 호출 0, 프롬프트 변경 0** ✅ 적용됨

핵심 통찰: `MasteryReader.readLatestAiLevelsByQuestion`은 **이미 `source = AI_ESSAY`로 정확히 필터한다.** 꼬리질문 판정을 새 source 값으로 쓰기만 하면 그 쿼리는 **구조적으로** 옳아진다. 쿼리를 고칠 필요가 없다.

- [x] `mastery/domain/MasterySource.java` — `AI_ESSAY_FOLLOWUP` 추가(17자, `length = 20` 안) + `isFollowup()`
- [x] `question/implement/EssayAnswerEvaluator.java` — 이미 `completedTurns`를 계산한다(29–30행). `turn = completedTurns + 1`을 결과에 실어 보낸다. **턴 정보의 출처를 클라이언트로 만들지 않는다**
- [x] `question/implement/dto/EssayEvaluation.java` — `turn` 추가
- [x] `question/implement/EssayMasteryRecorder.java` — turn에 따라 source를 고른다. **이 한 곳이 두 호출자(`EssayAnswerService.evaluate`, `InterviewService.answer`)를 동시에 고친다.** 서비스에 턴 검사를 넣지 않는다
- [x] `mastery/implement/MasteryRecordAppender.java` — **핵심 한 줄**: follow-up source면 이력만 append하고 `user_tag_mastery` upsert를 건너뛴다. 이걸로 "가장 깊은 프로브가 현재 숙련도가 되는" 오염이 사라진다
- [x] `mastery/infra/MasteryRecordRepository.java` — `countByCategoryAndLevel`의 의미를 **답변 횟수**로 확정하고 그렇게 쿼리를 고친다(`AI_ESSAY_FOLLOWUP` 제외 + 문항 단위 distinct). 죽은 finder 2개(`findFirstByUserIdAndQuestionIdOrderByIdDesc`, `findByUserIdOrderByIdDesc`) 삭제
- [x] `mastery_record.turn` nullable 컬럼 추가. **NULL은 "본질문"이 아니라 "미지"** 로 취급한다
- [x] `SolvingTimeReader` 호출 — 꼬리질문 턴에 루트 문항의 평균을 쓰지 않는다. baseline `null`로 넘겨 `ElapsedPacePolicy`의 기본 180초를 쓰게 한다
- [x] `docs/RECOMMENDATION.md:141`을 코드와 일치시킨다 — "꼬리질문 턴은 별도 source로 기록하고 현재값을 덮어쓰지 않는다"
- [x] `docs/API.md`의 `levelCounts` 설명을 확정한 의미에 맞춘다

**턴 0/미지는 항상 MAIN으로 처리한다.** in-memory `ChatMemory`는 재시작·다중 인스턴스에서 0으로 리셋되므로, "본질문을 꼬리질문으로 오인해 판정을 조용히 버리는" 쪽이 반대보다 나쁘다.

**기존 데이터 정리:** 이미 쌓인 `user_tag_mastery` 행에는 꼬리질문 유래 레벨이 남아 있다. 사용자가 사실상 1명이므로 **자기 행을 지우는 수동 `DELETE` 한 줄**이 정직한 정리다. 백필 코드를 만들지 않는다.

## Stage 1 — 이미 있는 것을 화면에 연결한다 · **AI 호출 0, 스키마 변경 0** ✅ 적용됨(면접 결과 화면 제외)

이게 첫 릴리스의 본체다. 새 AI 호출·새 테이블·프롬프트 변경·쿼터 노출·캐시 무효화·비결정성이 전부 0이다. 프롬프트를 건드리지 않으므로 **채점 품질을 떨어뜨릴 수 없다.**

- [x] `front/src/lib/mastery.ts` (신규) — `fetchMastery()`. `front/src/lib/recommendations.ts`(15줄)의 형태를 그대로 복사. **`apiFetch` 경유 필수**
- [x] `front/src/types/index.ts` — `MasteryResponse`/`CategoryMasteryResponse`/`TagMasteryResponse` 추가. `MasteryLevel`은 345행에 이미 있다
- [x] `front/src/app/progress/page.tsx` — **새 라우트를 만들지 않는다.** 이미 `Promise.all([fetchProgressSummary(), fetchProgress()])`에 loading/error/loaded 삼단 처리가 있으니 세 번째 다리로 붙인다. 라우팅·nav·`COMING_SOON_ROUTES` 편집 0
- [x] `front/src/components/progress/` — `CategoryProgressList.tsx`의 카드·라벨 관용구와 `lib/progress.ts`의 `CATEGORY_ORDER`·`categoryLabel`·`TIER_COLORS` 패턴을 따라, 카테고리별 `levelCounts` 히스토그램 + 태그별 `level`·`reason`·`updatedAt`
- [x] `front/src/components/solve/EssayQuiz.tsx`(125–127행에서 버려진다, 274행이 렌더 슬롯) + `front/src/components/interview/InterviewSession.tsx` — 채점 시점에 `mastery` + `masteryReason` 노출. **`null`은 빈 박스가 아니라 "판정 없음"** 으로 처리한다(프롬프트 누락 경로)
- [x] 거짓 문구 정리 — `RecommendationFlow.tsx:13`, `weekly/page.tsx`. **문구를 참으로 만들려고 기능을 구현하지 않는다. 문구를 지운다**

**완료 기준:** 자기 실제 `/api/mastery` 출력을 화면에서 직접 보고, 그 `reason`들이 쓸모 있는지 말할 수 있다.

> **면접 결과 화면은 적용하지 않았다.** 이 계획은 `InterviewSession.tsx`를 적었지만 그 화면은 **의도적으로** 세션 중 피드백을 감춘다 — 코드 주석이 이유를 적어 놨다: *"피드백을 읽고 답하면 이해도가 아니라 힌트 반영도를 재게 된다."* 판정 근거는 더 직접적인 힌트라 같은 이유로 감춰야 한다.
> 올바른 자리는 `InterviewResult`인데 그 화면은 백엔드 `InterviewResultItem`으로 렌더되고 **거기에 mastery가 없다.** 넣으려면 `mastery_record`를 `turn` ↔ 항목 순번으로 매칭해 조인해야 하고(같은 일일 문항 재응시 같은 경계가 붙는다) 이는 "스키마 변경 0 · 읽기만"의 범위를 넘는다. 별 단계로 분리한다.

## Stage 2 — Experiment 0 · **비용 0 (SQL 한 줄, AI 호출 0)**

```sql
SELECT category, level, reason FROM mastery_record WHERE source = 'AI_ESSAY' ORDER BY id;
```

각 행에 손으로 예/아니오 세 개:

1. `reason`이 **구체적인** 개념을 지목하는가("설명이 부족했습니다"가 아니라)?
2. `reason`만 읽고 `{concept, mistakenFor}`를 내가 채울 수 있는가?
3. 인용된 근거가 사용자 답변에 실제로 있는가?

**게이트: 손으로 구조화할 수 없으면 모델도 못 한다 → Stage 4를 영구 기각하고, 대신 `MASTERY_INSTRUCTION` 문구를 개선한다(무료).**

주의: 버그 1 때문에 기존 `mastery_record` 행 상당수가 꼬리질문 턴이고, 그 `reason`은 태그와 무관한 질문을 가리킨다. 걸러내거나 표시하며 읽는다 — 이 자체가 버그가 코퍼스를 얼마나 오염시켰는지의 증거다.

## Stage 3 — Experiment 1 · **AI 호출 약 60회, 하루**

집계 가능성에 필요한 성질은 그럴듯함이 아니라 **재현성**이다.

- `essay_solved.user_answer`에서 좋음·중간·틀림에 걸친 실제 답변 10건
- `tools/question-pipeline`에 붙인다(`replay.py`·`tests/`·`similarity/embeddings.py`의 로컬 `bge-m3` + `cosine`이 무료·캐시됨). `duplicates.py`의 `calibrate` 관례를 따라 `판정:` 한 줄을 출력한다
- **처치군** — `misconception` 필드를 넣은 프롬프트로 10건 × 3회
- **대조군 (반드시 같이)** — **현재** 프롬프트로 같은 10건 × 3회. 지금 `mastery` 자체가 재실행에 흔들리면, 오개념 추출보다 훨씬 중요한 것을 발견한 것이고 `user_tag_mastery`의 덮어쓰기 의미론 전체가 잡음 위에 서 있다. **프롬프트 변경 전에 재지 않으면 회귀를 사후에 감지할 수 없다**

**통과선을 보기 전에 적어 둔다:**
- `concept`가 10건 중 **7건 이상**에서 3회 중 2회 이상 의미적으로 일치 (exact-string 일치율 ~0은 정상이며 그 자체가 발견이다. `bge-m3` 코사인으로 잰다)
- 인용류 필드를 쓸 경우 사용자 답변의 literal substring인 비율이 10건 중 **9건 이상**

**게이트:** 일치 기준 미달 ⇒ 집계 불가. 답변 단위 코멘트로는 보여줘도 되지만(단일 피드백 버블에서 비결정성은 무해하다) **`WeaknessProfile`에는 절대 넣지 않는다.** substring 기준 미달 ⇒ 증거가 조작된 것이므로 전면 기각.

## Stage 4 — `misconception` 2필드 · **표시 전용**

```
misconception { concept, mistakenFor }
```

**4필드가 아니라 2필드다.** `evidenceQuote`는 `feedback`이 이미 하는 일이고 인용 환각을 초대한다. `tagHint`는 **자른다** — `QuestionTagIdReader`가 이미 그 문항의 큐레이션된 태그를 알고 있는데 모델에게 태그 이름을 발명하게 할 이유가 없다.

**최대 결함과 그 해결:** `concept`를 자유 문자열로 받으면 집계 키가 LLM 자유 텍스트의 문자열 동등성이 된다. `인덱스`/`B-트리 인덱스`/`클러스터드 인덱스`는 절대 같은 그룹이 안 되고, "prose를 집계 가능한 증거로 바꾼다"는 명분 자체가 무너진다. → **`concept`를 닫힌 어휘로 강제한다.** 프롬프트에 그 문항 카테고리의 태그 사전을 내려주고 **그중 하나 또는 `null`만** 허용한다. 그러면 `tag`에 조인 가능한 컬럼이 되고, 이것이 이 제안의 유일한 정당화다. 대가는 채점 호출당 +~100토큰과 쿼리 1회. false precision(닫힌 목록에 없는 오개념을 가장 가까운 라벨로 밀어붙임)은 `null` 허용을 명시하고 자유 텍스트 `mistakenFor`를 탈출구로 남겨 완화한다.

> ⚠️ **Spring AI 함정 — 반드시 대응.** `GeminiEssayAiClient`는 `.responseEntity(GradeAndFollowupResult.class)`를 쓰고 Spring AI는 **record 컴포넌트 목록에서 스키마를 파생한다.** 컴포넌트를 추가하는 순간 모델은 **스키마상 그 필드를 채울 의무**를 지는데 `EssayPromptV6`은 그것에 대해 한 마디도 하지 않는다. 결과: 지침 0인 상태로 자신만만하게 채워진 필드가 즉시 프로드 전 채점 호출에 나타난다. 이 코드베이스는 두 번 다 관례를 지켰다 — `mastery`는 `MASTERY_INSTRUCTION`과 함께, `criteriaResults`는 `RUBRIC_INSTRUCTION_TEMPLATE` **및** 비울 때를 위한 `NO_RUBRIC_INSTRUCTION`과 함께 나갔다.

- [ ] `question/infra/ai/prompt/EssayPromptV7.java` — V6 규칙 승계 + `[오개념 지목]` 블록 + 닫힌 어휘 목록 + **"해당 없으면 null"** 지시. `MASTERED`에는 오개념이 없다 — **이 지시가 없으면 정답에 대해 오개념을 조작해 내고 우리는 그것을 집계한다. 가장 유력한 실패 모드다.** V6은 롤백 자산으로 남긴다
- [ ] `question/infra/ai/MisconceptionGrading.java` — `record (String concept, String mistakenFor)`
- [ ] `mastery_record` 추가 컬럼(전부 nullable): `misconception_concept VARCHAR(60)`, `misconception_target VARCHAR(200)`, `prompt_version VARCHAR(10)`
- [ ] **`prompt_version`을 반드시 같이 저장한다.** 지금 `promptVersion`은 로그만 남아 V4/V5/V6 품질을 사후 비교할 수 없다. 이걸 안 하면 오개념 컬럼도 3개월 뒤 똑같이 감사 불가 데이터가 된다

**`essay_solved`에는 아무것도 추가하지 않는다.** 그 경로는 클라이언트 중계라 위조 가능하다 — 숙련도를 채점 시점에 기록하는 이유와 같다.

**닫힌 어휘 미스는 로그 + 드롭.** 절대 `TAG_NOT_FOUND`를 던지지 않는다(진단 부산물 때문에 채점이 실패하면 안 된다 — `EssayMasteryRecorder`가 mastery 누락을 건너뛰는 태도와 동일), 절대 태그를 만들지 않는다, 절대 유사도로 fuzzy 매칭하지 않는다(`docs/TAG.md`가 의도적으로 분리한 개념을 되붙인다).

`mastery_record`는 태그 수만큼 fan-out하므로 오개념이 중복 저장된다. 정규화 대신 비정규화를 택한다 — **중복이 곧 `GROUP BY tag_id`를 공짜로 만든다.** 대가를 문서에 남긴다.

## Stage 5 — 깊이 축을 프로필에 넣는다

본질문 정답 + 프로브 붕괴 = **암기했지 이해하지 않았다.** 제품의 핵심 차별점이 만들어 내는 가장 강한 개인화 신호인데 지금은 버려지거나 오염된다.

깊이 축에 **컬럼을 만들지 않는다.** `mastery_record`에서 `source = AI_ESSAY_FOLLOWUP`으로 읽어 파생한다(비정규화 드리프트 없음, 프로필 계산이 이미 전체 이력을 읽는다).

- [ ] `recommendation/domain/TagDepth.java` — `UNKNOWN / CONSISTENT / COLLAPSED`
- [ ] `recommendation/domain/DepthPenaltyPolicy.java` — 순수. 유계·양자화된 가산치
- [ ] `mastery/implement/MasteryReader` — `readFollowupLevelsByQuestion(...)`. **기존 `findByUserIdAndSourceAndQuestionIdInOrderByIdAsc`에 source만 다르게 넘기면 되고 새 finder가 필요 없다**

결정성·캐시를 지키는 규칙 5개:

1. **시간 파생 값 금지.** 최근성 감쇠·타임스탬프를 프로필에 넣는 순간 활동이 없어도 매 요청 해시가 바뀌어 영구 캐시 미스가 된다
2. **양자화를 선행 조건으로.** `Math.round(x * 100) / 100.0`. 새 가산치가 새 float 말단을 만들기 때문이고, **`EssaySolvedRepository.findByUserIdAndType`에 `ORDER BY`가 없어 double 합 순서가 흔들릴 수 있는 기존 구멍까지 같이 막는다**(지금도 이론상 캐시 키가 흔들린다)
3. **유계.** 가산치 상한(예: +0.15)을 두고 **`MASTERED` 본질문이 `WEAK` 본질문을 추월하지 못하게 클램프**한다. 안 하면 오늘의 편향을 방향만 반대로 재현한다 — "본질문을 잘 풀고 프로브에서 흔들린 사람"이 "본질문부터 모르는 사람"보다 취약하다고 나온다
4. `sampleCount < MIN_TRUSTED_SAMPLE`이면 적용하지 않는다(그 점수는 카테고리에서 빌려온 값이다)
5. **플래그 off면 숫자가 오늘과 완전히 동일.** 테스트로 고정한다

`RecommendationTopicPolicy`는 `COLLAPSED`를 **점수가 아니라 tie-break**에 쓴다. 점수 스케일을 건드리지 않으면서 암기 태그를 우선 조준할 수 있다.

## Stage 6 — 온디맨드 진단 서사 · **`GET /api/diagnosis` 1개, 스케줄러·메일 없음**

이미 배포된 mock 화면(`/weekly`, `/mock`)을 채운다. 코퍼스는 **이미 DB에 있다** — 꼬리질문의 발문·답변·피드백·모범답안이 `essay_solved`(type=FOLLOWUP)에 영속된다. JDBC ChatMemory 도입은 비목표다.

- [ ] `diagnosis/` 신규 도메인. `resources/prompts/diagnosis-narration.st`로 외부화(생성 경로의 관례를 따르고, 채점 프롬프트의 하드코딩 관례를 따르지 않는다)
- [ ] `diagnosis/domain/TemplateDiagnosisWriter` — **AI 실패 시 같은 증거로 AI 없이 서술을 만든다.** 이미 배포된 화면이 500을 띄우면 안 되고, 그 화면은 지금도 사용자에게 "AI 없이 템플릿으로 생성됩니다"라고 약속하고 있다
- [ ] `DiagnosisErrorCode` — 세 번째 쿼터 버킷. `AiFailureKind` → `DIAGNOSIS_AI_*` 매핑은 기존 두 클라이언트의 `errorCodeOf` 패턴 그대로
- [ ] **재생성 조건이 카운터보다 강한 가드다.** `user_diagnosis.generated_at` 이후 새 세션이 없으면 호출하지 않고 저장된 행을 돌려준다 → 호출 수가 요청 수가 아니라 **활동량에 비례**한다

**쿼터 가드** (Stage 6에서만 필요 — Stage 0·1은 호출이 0이고 Stage 4는 호출 수가 그대로다):

- [ ] `common/ai/AiBucket` — `GRADING / GENERATION / DIAGNOSIS`
- [ ] `common/ai/AiDailyBudget` — `Clock` 주입, 버킷별 `AtomicInteger`, KST 롤오버
- **`DIAGNOSIS`만 사전 한도를 체크한다.** 채점을 잘못 막는 사전 체크는 429보다 나쁘다(사용자가 재시도할 수도 없다)
- **래치가 지금 없는 유일하게 중요한 것이다** — `AiFailureClassifier`가 `DAILY_QUOTA_EXCEEDED`를 주면 그날 **모든 버킷**을 닫는다(제공자 쿼터는 공유 자원). 지금은 N명을 도는 배치의 첫 호출이 일일 쿼터를 터뜨려도 **나머지 N-1회를 똑같이 시도한다**
- 정직한 한계: 프로세스 로컬이라 인스턴스별이고 재시작하면 리셋된다(`RecommendationCache`가 이미 감수한 것과 동일). 회계 시스템이 아니라 예의 있는 리미터다

---

## 자르는 것

| 항목 | 판정 | 이유 |
| --- | --- | --- |
| **적응적 프로빙** (가설 분별 문항, 데일리 면접 AI 개인화) | **기각** | 상호작용당 AI 호출이 2배가 되는데 **쿼터 인프라가 아예 없다**(`AiFailureClassifier`는 제공자 에러 문자열 사후 파싱뿐). "경쟁하는 두 가설"을 만들려면 confidence 있는 숙련도 모델이 선행돼야 하는데 `user_tag_mastery`는 confidence 없는 단일 레벨 덮어쓰기다. 검증도 불가능(사용자 1명·레이블 0). `docs/RECOMMENDATION.md` 리스크 3·4를 증폭한다 — 진단 자체가 생성 콘텐츠에 의존하게 된다. 그리고 `DailyQuestionResolver`의 전역 일일 핀은 게으름이 아니라 **면접을 멱등·저렴하게 만드는 설계**(`insertIfAbsent`, 날짜당 1행)인데 사용자별 해석은 그 성질을 파괴한다 |
| **주간 메일 발송·스케줄러** | **기각** | `StudyReminderScheduler`는 `for (setting : settings)` 동기 fan-out에 rate limit·동시성 상한이 없다 — 메일에는 무해하고 LLM에는 치명적이다. 500명이면 한 tick에 500회 순차 호출이고, 21:00 KST에 **사용자가 기다리는 채점 경로와 같은 미분화 쿼터를 다툰다**(bulkhead 없음). 더 중요하게, **자신만만하게 틀린 서사는 없는 것보다 나쁘다** — 답변 단위 피드백은 사용자가 방금 그 문제를 읽었으니 반증 가능한데, 몇 주치를 종합한 서사는 반증 불가라 사용자는 조작을 믿거나 제품 전체에 대한 신뢰를 잃는다. 게다가 메일은 **철회 불가**다. 온디맨드 엔드포인트(Stage 6)로 대체한다 |
| `misconception.tagHint` | **자른다** | `question_tag`가 이미 태그를 안다 |
| `misconception.evidenceQuote` | **자른다** | `feedback`이 이미 하는 일 + 인용 환각 |
| `criteriaResults` 영속화 | **보류** | 시드 375문항 전부 `rubric = NULL`(`grep -c rubric data*.sql` = 0). 모든 행이 비어 있을 텐데 영속화하면 안 된다. `ddl-auto: update` + Flyway 없음이라 **추가한 컬럼은 수동 DDL 없이 못 지운다.** 실제 문항에 루브릭이 생긴 다음에 다시 본다. **오개념 추출은 루브릭에 의존해서는 안 된다** |
| 태그 임베딩 그래프 · `MasteryLevel` 10분류 | **`docs/WEAKNESS.md` 소관** | 사용자에게 안 보이고 사용자 1명에서 측정 불가. 이미 만들어진 AI 진단이 안 보이는 상태에서 먼저 할 일이 아니다 |
| JDBC ChatMemory | **비목표** | 꼬리질문 텍스트는 이미 `essay_solved`에 있다 |

### 곁다리로 같이 볼 것 (한 줄, 이 계획과 무관하게 이득)

`MasteryWeight`에서 `GUESSED`(0.7) < `WEAK`(0.85)이므로 **"빠르고 자신 있게 틀린다"는 가장 선명한 학습 신호가 구조적으로 후순위**이고, 문항을 아무리 풀어도 가중치 평균의 상한 0.7 때문에 순위가 뒤집히지 않는다(`docs/SCRIPT_PERSONA.md`가 이미 적어 둔 성질). 6개 enum의 상수 하나이고 임베딩도 AI 호출도 필요 없다. 사용자에게 보이는 것이 바뀐다.

---

## 검증

### 게이트

| 게이트 | 무엇을 시험 | 통과 조건 |
| --- | --- | --- |
| Stage 1 완료 | 진단이 볼 만한가 | 자기 `/api/mastery`를 화면에서 직접 보고 판단 |
| Stage 2 (무료) | 손으로 구조화 가능한가 | 미달 ⇒ Stage 4 영구 기각, 프롬프트 문구 개선으로 대체 |
| Stage 3 (약 60회) | 재현되는가 + **회귀 안 했는가** | 의미 일치 7/10, substring 9/10. **대조군 필수** |
| Stage 5 진입 | 사용자가 1명 초과인가 | 여전히 1명이면 **더 하지 않고 사용자를 구한다** |

### 테스트 (`docs/TEST.md` 준수)

**순수 단위 (Spring · LLM 없이)**

- `MasterySource.isFollowup` 전수
- `DepthPenaltyPolicy` — 상한 클램프, **밴드 추월 금지**(`MASTERED` 본질문 + `NOT_LEARNED` 프로브가 `WEAK` 본질문을 못 넘는다), 프로브 목록이 비면 항등
- `TagWeakness` — 양자화가 소수 셋째 자리를 흡수하는지, `observed(...)` 팩토리
- `EssayPromptV7Test` — 오개념 블록 존재, **닫힌 어휘 목록 렌더링**, **"해당 없으면 null" 지시 존재**, V6 규칙(6분류·근거 요구·시간은 보조) 승계, 후보 목록이 비어도 렌더 안 깨짐
- `EssayPromptContractTest` — `PROMPTS`에 V7 추가(미치환 `%s` 없음 등 기존 계약 자동 상속)
- **캐시 격리 테스트** — 오개념 텍스트만 다른 두 `WeaknessProfile`의 `hashCode()`가 같음

**Implement / Service**

- `MasteryServiceTest` — follow-up source가 `mastery_record`만 남기고 **`user_tag_mastery`를 건드리지 않는지**
- `EssayAnswerEvaluatorTest` — turn이 1·2·3으로 실려 나가는지, 꼬리질문 턴이 루트 baseline을 쓰지 않는지, 오개념 누락 응답에도 채점이 성공하는지
- `WeaknessProfileCalculatorTest` — **플래그 off면 오늘과 숫자 동일**(형제 테스트로 on 추가), `COLLAPSED` 케이스, `turn = NULL`을 본질문으로 취급하지 않는지, `MIN_TRUSTED_SAMPLE` 미달이면 가산치 미적용
- `DiagnosisServiceTest` — AI 실패 시 템플릿 폴백으로 200, 새 세션 없으면 재호출 없음, 래치가 닫혔으면 `verify(never())`
- `AiDailyBudgetTest` — KST 롤오버, DAILY 실패가 전 버킷을 닫는지, QUOTA 실패는 쿨다운만인지

**Integration — 값을 증명하는 테스트**

`EssayGradingIntegrationTest`: 본질문 `SOLID` + 프로브 `NOT_LEARNED` 3턴 세션에서 `mastery_record` 3행(turn 1·2·3), `user_tag_mastery` 1행이 **본질문 판정을 유지**. **오늘은 `NOT_LEARNED`가 남고, 수정 후에는 `SOLID`가 남는다.**

**손댈 기존 테스트 (컴파일 실패 포함)**

| 파일 | 이유 |
| --- | --- |
| `fixture/GradeAndFollowupResultFixture.java` | `new GradeAndFollowupResult(...)` 3곳, 컴포넌트 7→8 |
| `question/infra/ai/MockEssayAiClient.java` | **운영 코드.** 오개념 mock 값 추가 |
| `question/implement/EssayAnswerEvaluator.java` | `new EssayEvaluation(...)` |
| `recommendation/domain/RecommendationTopicPolicyTest.java` | `new TagWeakness(...)` 6곳 → `observed(...)` |
| `EssayPromptContractTest.java` | `PROMPTS` 목록 |
| `EssayAnswerServiceTest` / `InterviewServiceTest` | `mastery_record` 행 수·레벨 단정. **느슨하게 풀지 말고 의도적으로 갱신** |

### 실행

```bash
./gradlew test --tests "com.neogul.whynago.mastery.*"
./gradlew test --tests "com.neogul.whynago.question.implement.*"
./gradlew test --tests "com.neogul.whynago.recommendation.*"
./gradlew build
cd front && npm run build
```

---

## 최소 첫 슬라이스 (AI 호출 0, 프롬프트 변경 0, 새 도메인 0)

**Stage 0 + Stage 1.** 버그 2개를 고치고, 이미 생산되는 AI 진단을 화면에 연결하고, 거짓 문구 2개를 지운다.

이것이 다른 무엇보다 먼저인 이유는 하나다 — **숫자는 이미 틀렸고, 그건 화면에 그려 봐야만 알 수 있다.** Stage 4·5·6은 전부 그 신호가 신뢰할 만하다고 가정하는데, 아무도 확인한 적이 없다. **표시하는 것이 곧 그 확인이다.**

> 한 줄 요약: **AI 취약점 진단 기능은 이미 완성돼 있다. 연결만 안 돼 있다. 연결하고, 그것을 오염시키는 버그를 고치고, 출력을 보고, 그 다음에 더 똑똑하게 만들 필요가 있는지 결정한다.**
