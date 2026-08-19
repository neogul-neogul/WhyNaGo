# **맞춤 문제 추천 전략**

## **목표**

이 문서는 맞춤 문제 추천을 **왜 이렇게 설계했는지**를 남기는 전략 문서다. 결정과 그 근거, 아직 못 정한 것과 알고 있는 리스크를 적는다. 무엇을 언제 구현했는지는 git 로그를 본다.

사용자별 약점을 진단하고, **그 약점에 맞는 서술형 문제를 AI로 생성해** 추천한다. 관련 도메인은 `recommendation`이다.

**생성 대상은 서술형(`ESSAY`)만이다.** 객관식은 생성하지 않는다. 객관식은 보기 4개·오답 해설·꼬리질문 연결까지 갖춘 구조라 AI 생성 시 검증 부담과 오류 표면이 지나치게 크고, 기존 문제은행 문항으로 충분히 커버된다.

취약 주제를 결정하는 단계까지는 결정적으로 처리하고, 마지막 문제 생성만 AI에 맡긴다.

레이어 구조·의존성 방향은 `docs/ARCHITECTURE.md`, 공통 코드 규칙은 `docs/CONVENTION.md`, 예외는 `docs/EXCEPTION.md`, 테스트는 `docs/TEST.md`를 따른다. 서술형 문항의 품질 기준과 톤은 `docs/SCRIPT_ESSAY.md`를 준용한다.

## **역할 분담**

| 단계 | 방식 | 이유 |
| --- | --- | --- |
| 숙련도 판정(서술형) | **AI (채점 응답에 포함)** | 답변 내용에 오개념·설명 깊이가 드러난다. 근거도 함께 받는다 |
| 숙련도 판정(객관식) | 결정적 (`MasteryPolicy`) | 정답이 이산값이라 규칙이 명확하고 AI를 호출할 이유가 없다 |
| 약점 집계 | SQL 집계 (`WeaknessProfileCalculator`) | 집계는 DB가 가장 잘한다 |
| 취약 주제 선정 | 결정적 (`RecommendationTopicPolicy`) | 같은 입력이면 같은 주제가 나와야 한다 |
| **서술형 문제 생성** | **AI (Spring AI `ChatClient`)** | 대체 불가능한 유일한 단계 |

## **입력 신호**

추천은 다음 신호만 사용한다. 신호는 객관식·서술형 풀이 이력 모두에서 수집한다(생성만 서술형 전용이고, 진단은 양쪽을 본다).

| 신호 | 출처 | 쓰이는 곳 |
| --- | --- | --- |
| 카테고리 | `question.category` | 두 트랙 공통 |
| 태그 | `tag.name`(사전) + `question_tag.tag_id` | 두 트랙 공통 |
| **AI 숙련도 판정과 근거** | `mastery_record.level`, `mastery_record.reason` | **서술형 트랙** |
| 점수 | `essay_solved.score` | 서술형 폴백 |
| 푼 시간 | `solved_multiple_choice.elapsed_seconds`, `essay_solved.elapsed_seconds` | 객관식 트랙 |
| 문제별 평균 풀이 시간 | `question_stat.avg_elapsed_seconds` | 객관식 트랙 |

태그는 `question_tag`가 이름을 직접 갖는 반정규화 구조였으나 `tag` 테이블로 정규화했다. 사용자별 숙련도를 태그 단위로 붙일 대상(`tag_id`)이 필요했기 때문이다. 사전에 없는 태그는 런타임에 만들 수 없다(`TAG_NOT_FOUND`).

`question_stat` 집계는 `solvedsession`에 둔다(`QuestionStatAggregator`). 집계 대상이 `question`이라 `question` 쪽이 자연스러워 보이지만, `solvedsession -> question` 의존이 이미 있어 반대 방향으로 두면 도메인 간 순환이 된다.

> 세션 단위 `startedAt ~ solvedAt` 차분으로 문항별 시간을 도출하지 않는다. 이탈·창 전환에 오염되며, 세션 내 문항별 배분이 불가능하다.

## **파이프라인**

```
풀이 이력 + QuestionStat + MasteryRecord(AI 판정)
  -> MasteryPolicy               (mastery)    객관식 숙련도 판정 (서술형은 AI 판정을 그대로 사용)
  -> WeaknessProfileCalculator   (implement)  카테고리·태그 2계층 집계
  -> RecommendationTopicPolicy   (domain)     취약 주제 상위 N개 + 목표 난이도 결정
  -> EssayQuestionGenerator      (implement)  AI 서술형 문항 생성 (구조화 출력)
  -> GeneratedQuestionAppender   (implement)  검증 통과분 저장
  -> 추천 문제 N개
```

`domain` 정책 객체는 LLM·DB·Spring 의존이 없어 전수 단위테스트가 가능하다. 빈 등록만 `config/RecommendationPolicyConfig`에서 담당한다.

### **service가 보는 흐름**

`RecommendationService`는 위 파이프라인을 직접 조립하지 않는다. 서비스에 남는 흐름은 두 갈래뿐이다.

```
프로필 계산 -> 콜드스타트인가?
             ├ 예   -> ColdStartQuestionReader     (난이도 하 문항을 카테고리별로 고르게)
             └ 아니오 -> PersonalizedQuestionProvider (캐시 -> 생성 -> 폴백 -> 캐시 저장)
          -> RecommendedQuestionAssembler (태그를 붙여 응답 모델로)
```

| 옮긴 상세 | 옮긴 곳 |
| --- | --- |
| 캐시 조회 → 주제 선정 → 생성 → 폴백 채움 → 캐시 저장 | `PersonalizedQuestionProvider` (implement) |
| 취약 태그 정렬·상위 4개 절단·응답 모델 변환 | `WeakTagAssembler` (implement) |
| 콜드스타트 판정 기준(이력 3건) | `WeaknessProfile.isColdStart()` (domain) |
| 한 응답에 담을 문항 수(3) | `RecommendationSize.TARGET_QUESTION_COUNT` (domain) |

캐시 히트냐 생성이냐 폴백이냐는 서비스가 알 필요가 없다. 어느 경로로 왔든 "이 사용자에게 줄 맞춤 문항"이라는 결과는 같고, 경로가 갈리는 사정은 `PersonalizedQuestionProvider` 안에서 끝난다.

## **숙련도 판정 정책**

숙련도 분류는 하나(`common.domain.MasteryLevel`, 6분류)이고 **판정 주체는 두 트랙**이다.

| 트랙 | 판정 주체 | 근거 | 기록 출처 |
| --- | --- | --- | --- |
| 서술형 | **채점 AI**(`EssayPromptV4`) | 답변 **내용** | `AI_ESSAY` |
| 객관식 | 서버(`MasteryPolicy`) | 정답 여부 × 소요시간 비율 | `RULE_CHOICE` |

판정 규칙(`MasteryPolicy`·`SolvedSignal`)은 `mastery.domain`에 둔다. 기록(객관식 채점)과 조회(추천의 약점 프로필)가 함께 쓰는데, `recommendation`에 두면 `solvedsession -> recommendation -> solvedsession` 순환이 생긴다. 약점 가중치(`MasteryWeight`)는 추천만의 개념이라 `recommendation.domain`에 남는다.

서술형을 AI에 맡기는 이유는, 답변 내용에 오개념과 설명 깊이가 그대로 드러나는데 시간·정답 여부만 보면 그 신호를 전부 버리기 때문이다. 객관식은 정답이 이산값이고 AI를 호출하지 않으므로 규칙 판정을 유지한다.

### **서술형 — AI 판정 (근거 필수)**

채점 응답에 `mastery`와 `masteryReason`을 함께 받는다. 판정 기준은 프롬프트에서 **답변 내용 기준으로** 정의한다(소요시간을 프롬프트에 넣지 않으므로 "빠름/느림"으로 판정할 수 없다).

| 숙련도 | 답변이 보인 상태 |
| --- | --- |
| `MASTERED` | 결론·근거가 정확하고 인접 개념까지 스스로 정리 |
| `SOLID` | 결론이 정확하고 필요한 근거를 갖춤 |
| `UNSTABLE` | 결론은 맞지만 근거가 틀렸거나 흔들림 |
| `GUESSED` | 핵심 용어만 나열, 근거 없음 |
| `WEAK` | 설명을 시도했지만 결론이 틀림 |
| `NOT_LEARNED` | 개념 자체가 없음 |

- **근거(`masteryReason`)는 반드시 채운다.** 답변에서 근거가 된 부분을 짚어 두 문장 이내로 쓰고, 일반론("더 공부가 필요합니다")은 금지한다. 근거 없이 숙련도만 남기면 사용자가 판정을 납득할 수 없고, 잘못된 판정을 사후에 검증할 수도 없다.
- `score`와 `mastery`가 모순되지 않게 지시한다(9점인데 `NOT_LEARNED` 금지).
- AI가 판정을 빠뜨리면 **채점은 그대로 응답하고 기록만 건너뛴다.** 그 이력은 추천 집계에서 규칙 판정으로 폴백한다.

### **객관식 — 규칙 판정**

`ratio = 내 소요시간 / 문제 평균 소요시간`을 정답 여부와 교차한다. 정답률만으로는 "알지만 헤맴"과 "찍어서 틀림"을 구분할 수 없다.

| | 빠름 (`ratio < 0.7`) | 보통 | 느림 (`ratio > 1.5`) |
| --- | --- | --- | --- |
| 정답 | `MASTERED` | `SOLID` | `UNSTABLE` |
| 오답 | `GUESSED` | `WEAK` | `NOT_LEARNED` |

- 서술형 점수가 3점 이하면 시간과 무관하게 `NOT_LEARNED`로 판정한다(AI 판정이 없는 이력의 폴백에서만 쓰인다).
- `question_stat.sample_count`가 5 미만이면 평균을 신뢰하지 않고 기본값 180초를 사용한다.
- 문항을 제외 대상으로 표시하는 용도는 없다. `MASTERED`는 약점 가중치 0.0으로만 작동한다.

### **숙련도 기록·조회**

판정은 `mastery` 도메인에 남긴다. 두 트랙 모두 **서버가 채점한 시점**에 기록한다.

| 트랙 | 기록 시점 | 기록 주체 |
| --- | --- | --- |
| 서술형 | 채점 응답을 내릴 때 | `EssayMasteryRecorder` (연습 채점 · 1일 1면접) |
| 객관식 | 풀이 세션을 저장할 때(`POST /api/solved-sessions`) | `ChoiceMasteryRecorder` |

클라이언트가 채점 결과를 되돌려주는 저장 경로(`essay_solved`)에 맡기면 값이 조작될 수 있고, 세션을 중도 이탈한 답변의 판정은 아예 남지 않는다. 그래서 연습 채점 API에 인증이 필요하다. 객관식은 세션 저장이 곧 채점 시점이다 — `MultipleChoiceAnswerScorer`가 클라이언트가 보고한 정답 여부를 믿지 않고 보기를 다시 채점하므로 같은 이유의 조작 위험이 없다. 근거(`reason`)는 답변 내용이 없으므로 AI가 쓰지 않고, 판정에 실제로 쓴 두 신호를 `ChoiceMasteryReason`이 문장으로 옮긴다.

| 테이블 | 역할 |
| --- | --- |
| `mastery_record` | 판정 이력. 문항의 태그 개수만큼 행을 만들고, 태그가 없으면 `tag_id = NULL` 1행. `category`는 항상 채운다 |
| `user_tag_mastery` | 사용자 × 태그의 **현재값**. 새 판정이 오면 누적하지 않고 덮어쓴다 |

- 현재값을 덮어쓰는 이유는, 숙련도가 "지금 이 주제를 얼마나 아는가"이므로 과거 판정을 평균하면 최근 학습이 묻히기 때문이다. 대신 이력을 따로 남겨 "언제 어떤 근거로 받았는지"를 되짚을 수 있게 한다.
- 카테고리 요약은 별도 테이블 없이 `mastery_record`에서 집계한다(태그 없는 문항의 판정을 잃지 않기 위해).
- **서술형 꼬리질문 턴은 기록하지 않는다.** `questionId`가 없어 태그를 붙일 수 없다(태그 프로필이 꼬리질문을 제외하는 것과 같은 이유). 객관식 꼬리질문은 그 자체가 문항이라 `questionId`와 태그가 있으므로 본질문과 똑같이 기록한다.
- 트랙이 달라도 `user_tag_mastery`는 태그당 한 행이다. 같은 태그를 서술형으로도 객관식으로도 풀면 **나중 판정이 이전 판정을 덮어쓴다** — 판정 출처가 아니라 시점이 기준이다.
- 추천의 약점 프로필은 저장된 `RULE_CHOICE` 판정을 읽지 않고 객관식을 매번 다시 계산한다(`MasteryReader.readLatestAiLevelsByQuestion`이 `AI_ESSAY`만 고른다). 문항 평균 소요시간(`question_stat`)이 매일 재집계되므로, 푼 시점의 기준이 아니라 **현재 기준**으로 판정해야 프로필이 최신 표본을 반영한다. 저장된 값은 `GET /api/mastery`의 사용자 노출용이다.
- 조회는 `GET /api/mastery`다. 카테고리별 판정 분포와 태그별 현재 숙련도·근거를 함께 내려준다.
- **도메인 의존 방향** — 기록은 `question -> mastery`(채점 흐름이 `tagId`·`category`를 담아 넘긴다), 조회는 `mastery -> question`(태그 이름을 사전에서 읽는다). `mastery`는 기록 경로에서 `question` 저장소를 조회하지 않으므로 클래스 수준 의존은 한 방향으로만 흐른다. 객관식 기록도 같은 방향이다 — `solvedsession`이 `question.implement.ChoiceMasteryRecorder`를 부르고, 태그·카테고리 해석은 `question`이 한다.

## **약점 프로필 정책**

카테고리 레이어와 태그 레이어를 2계층으로 둔다.

- **카테고리(8개)** — 표본이 항상 충분하다. 큰 방향을 잡는 데 사용한다.
- **태그** — 표본 2건 이상만 신뢰한다. 미달이면 소속 카테고리 값으로 폴백한다.

태그를 1급 축으로 쓰지 않는 이유는 현재 문제은행 규모(175문항)에서 태그당 표본이 1건 수준이라, 우연한 오답 하나가 프로필 전체를 지배하기 때문이다.

`weaknessScore` = 해당 주제 문항들의 약점 가중치(`MasteryWeight`) 평균 (0.0 ~ 1.0).

문항별 가중치를 고를 때 **AI 판정이 규칙 판정보다 우선한다.** 서술형에 `AI_ESSAY` 기록이 있으면 그 값을 쓰고, 없으면(프롬프트 v4 이전에 푼 이력) 규칙 판정으로 폴백한다. 가중치 매핑은 `MasteryLevel`이 아니라 `recommendation.domain.MasteryWeight`가 갖는다 — 가중치는 추천이 약점을 집계하는 방식이라 공용 enum이 알 필요가 없다.

> 서술형 꼬리질문(`essay_solved.type = FOLLOWUP`)은 `question_id`가 `null`이라 태그를 붙일 수 없다. 태그 프로필과 숙련도 기록은 객관식과 서술형 본질문만 반영한다.

## **취약 주제 선정 정책**

`RecommendationTopicPolicy`는 프로필을 받아 생성 요청 단위인 `GenerationTopic` 목록을 만든다. 순수 도메인 객체이며 LLM을 모른다.

```
GenerationTopic(category, tags, targetDifficulty, weaknessScore, reason)
```

- 카테고리 약점도 내림차순으로 상위 3개 카테고리를 고른다.
- 각 카테고리에서 신뢰 가능한 태그를 약점도 순으로 최대 2개까지 붙인다. 태그가 없으면 카테고리만으로 생성한다.
- **목표 난이도** — 약점도 0.7 이상이면 `LOW`, 0.35 이상이면 `MEDIUM`, 그 미만이면 `HIGH`. 개념이 안 잡힌 주제에 상 난이도를 주지 않는다.
- **다양성** — 카테고리 1개당 `GenerationTopic` 1개(=문항 1개)를 만든다. 따라서 요청당 최대 3문항이다.
  문서 초안의 "카테고리당 2문항, 요청당 6문항"은 정책상 상한이고, 실제 서비스 상한은 3문항으로 낮춰 두었다.
  문항 하나가 생성 1회 + 채점 최대 3회의 AI 호출을 유발하므로(리스크 5) 보수적으로 잡는 편이 맞다.
  상한은 `RecommendationService.TARGET_QUESTION_COUNT`에 있다.
- **tie-break** — 약점도가 같으면 카테고리 `ordinal`, 태그명 오름차순 고정. 같은 프로필이면 항상 같은 주제 목록이 나와야 한다.

## **서술형 문제 생성 정책**

Spring AI `ChatClient` + `BeanOutputConverter`로 구조화 출력을 받는다. 프롬프트는 `resources/prompts/essay-question-generation.st`에 외부화해 재컴파일 없이 수정할 수 있게 둔다.

**프롬프트 원본은 [`docs/SCRIPT_RECOMMENDATION.md`](./SCRIPT_RECOMMENDATION.md)다.** 시드 생성 프롬프트(`SCRIPT.md`·`SCRIPT_ESSAY.md`)와 달리 SQL이 아니라 JSON 1건을 받고, 난이도가 약점도에서 계산돼 내려온다. 변수 매핑과 "실행본에 중괄호를 쓰지 않는다"는 제약도 그 문서에 있다.

### **생성 산출물**

문항 하나당 다음을 받는다.

| 필드 | 설명 |
| --- | --- |
| `title` | 문항 제목 |
| `content` | 실제 발문 |
| `modelAnswer` | 모범답안 |
| `gradingCriteria` | 채점 시 반드시 언급돼야 하는 핵심 키워드·논점 목록 |
| `difficulty` | 요청한 목표 난이도와 일치해야 한다 |
| `tags` | 1~2개 |

`gradingCriteria`를 함께 생성하는 이유는 채점 단계에서 모범답안만으로 판정하면 기준이 매번 흔들리기 때문이다. 생성 시점에 채점 기준을 고정해 저장한다.

### **프롬프트에 넣는 것**

- `GenerationTopic`의 카테고리·태그·목표 난이도
- 해당 태그의 **기존 객관식 오답 해설 3~5개** — 문제은행의 오답 해설 약 525건은 오개념 카탈로그다. SQL로 뽑아 "이 오개념을 파고드는 발문을 만들라"는 지시와 함께 주입한다. 벡터 검색은 쓰지 않는다.
- 해당 태그의 **기존 서술형 문항 제목 목록** — 중복 생성을 막기 위한 네거티브 컨텍스트
- `docs/SCRIPT_ESSAY.md`의 품질 기준 요약

집계된 요약만 넘긴다. 원본 풀이 이력을 그대로 넣지 않는다.

### **생성 결과 검증**

AI 출력은 신뢰하지 않는다. `GeneratedEssayValidator`(domain)가 다음을 전수 검사하고, 하나라도 어기면 그 문항을 버린다.

- `content`, `modelAnswer` 비어 있지 않음, 각각 최소 길이 충족
- `gradingCriteria` 2개 이상
- `category`가 요청한 카테고리와 일치, `difficulty`가 요청한 목표 난이도와 일치
- 태그 1~2개, 요청한 태그를 최소 1개 포함
- 요청한 태그 이외의 태그가 생성된 경우, db의 `question_tag`에 존재하지 않은 태그인 경우 저장하지 않는다.
- 기존 문항 제목·발문과 완전 일치하지 않음

검증 통과분이 요청 수보다 적어도 재시도하지 않는다. 부족한 만큼 폴백으로 채운다.

### **저장 정책**

생성 문항은 `question`(`type = ESSAY`)과 `question_tag`에 **저장한다.** `answer_choice`는 생성하지 않는다.

판단 근거는 다음이다.

- 저장하지 않으면 `question_stat`이 쌓이지 않고, 따라서 그 문항은 영구히 `sample_count = 0` 폴백을 받는다. 추천 루프가 자기 출력을 학습하지 못한다.
- `essay_solved.question_id`가 FK를 요구한다. 휘발성 문항으로 두면 풀이 이력이 꼬리질문과 동일하게 `question_id = null`이 되어 태그 프로필에서 빠진다.

단, 문제은행 목록에 노출하면 검수되지 않은 문항이 전체 사용자에게 퍼진다. 그래서 **축을 두 개로 나눈다.**

| 컬럼 | 값 | 성질 | 역할 |
| --- | --- | --- | --- |
| `question.source` | `SEEDED` \| `GENERATED` | 생성 시점에 확정되는 **불변 출신** | 검수 큐 조회, 통계·프로필 분리 집계 |
| `question.review_status` | `APPROVED` \| `PENDING` \| `REJECTED` | 관리자가 전이시키는 **가변 상태** | 노출 게이트 |

노출 여부를 판단하는 조건은 `review_status = APPROVED`다. `source`는 노출 필터에 쓰지 않는다. 두 축을 하나의 enum(`SEEDED` | `PENDING` | `APPROVED`)으로 합치면 승인된 순간 그 문항이 AI 생성이었다는 사실이 사라져, 리스크 4·6 대응이 불가능해진다.

`review_status`는 `nullable = false`에 DB 기본값 `APPROVED`로 둔다. NULL을 허용하면 3-valued logic 때문에 `review_status = 'APPROVED'` 조건이 기존 문항을 전부 걸러낸다(`source`도 같은 이유로 `@ColumnDefault("'SEEDED'")`를 걸었다). 생성 경로는 `Question.generated()` 팩토리가 `PENDING`을 세팅해 불변식을 구조적으로 강제한다.

`modelAnswer`와 `gradingCriteria`는 `question`에 컬럼으로 붙인다. 채점 기준은 여러 줄이지만 별도 테이블을 두지 않고 `GradingCriteriaConverter`로 한 컬럼에 줄바꿈 저장한다 — 조건 검색 대상이 아니라 문항을 읽을 때 늘 함께 읽는 값이기 때문이다. 시드 문항은 해설(`explanation`)만 갖고 채점 때 AI가 모범답안을 만들지만, 생성 문항은 만들어질 때 모범답안이 함께 나오므로 문항에 붙여 보관한다.

시드 쪽에서는 `data3.sql`이 정규화된 태그 사전을 참조하는 현행 시드다. `data2.sql`은 사전 밖 태그가 533종이라 변환하지 않고 레거시로 남겼다.

### **검수 승인 정책**

생성 문항은 `PENDING`으로 저장되고, 관리자가 승인하면 `APPROVED`가 되어 **시드 문항과 완전히 동등하게** 노출된다.

```
생성 -> PENDING --승인--> APPROVED
               \--거절--> REJECTED
```

역방향 전이는 허용하지 않는다. `Question.approve()`/`reject()`는 `PENDING`에서만 전이하고, 이미 결정된 문항에 다시 호출하면 `QUESTION_REVIEW_ALREADY_DECIDED`(409)를 던진다. 승인 후에 문제가 발견되면 상태를 되돌리는 것이 아니라 별도 회수 절차로 처리한다.

**상태별 도달 경로**

| 경로 | `APPROVED` | `PENDING` | `REJECTED` |
| --- | --- | --- | --- |
| `GET /api/questions` 목록 | O | X | X |
| 오늘의 면접 질문 후보 | O | X | X |
| 진척도 카테고리 분모 | O | X | X |
| 추천 응답 | O | O (생성 요청자) | X |
| 단건 조회 | O | O | O |

- **`PENDING`이 추천 응답에 나가는 것은 의도된 동작이다.** 승인을 기다리게 하면 추천 자체가 성립하지 않는다. 따라서 승인 게이트가 막는 것은 **재사용·확산**이며, 검수되지 않은 문항이 최초 1명에게 도달하는 것은 막지 못한다.
- **`REJECTED`는 삭제가 아니다.** 이미 쌓인 `essay_solved.question_id`가 FK를 잡고 있으므로 행을 지울 수 없다. 상태 전이로만 처리하고, 추천 재조회에서도 제외한다 — 캐시 키가 `userId + 프로필 해시`라서 프로필이 바뀌지 않으면 거절된 문항이 다시 나갈 수 있다.
- **승인 후에도 `source = GENERATED`는 남는다.** 리스크 4(자기 참조)·6(통계 오염) 완화는 승인 여부와 무관하게 "AI가 만든 문항인가"를 물어야 하기 때문이다.
- **승인 시 진척도 분모가 늘어난다.** 시드와 동등하게 취급한다는 결정의 대가이며 수용한다.
- **선행 조건** — 현재 인증 계층은 `userId`만 해석한다(`docs/API.md`). 관리자 승인 API·페이지는 역할 체계가 먼저 있어야 하며, 추천 파이프라인과 독립적인 작업이다.

## **쿼터·캐시 정책**

- **쿼터 버킷을 분리한다.** 추천 문제 생성이 서술형 채점 쿼터(`ESSAY_AI_QUOTA_EXCEEDED`)를 잠식하면 안 된다. `RECOMMENDATION_AI_QUOTA_EXCEEDED`, `RECOMMENDATION_AI_DAILY_QUOTA_EXCEEDED`를 별도로 둔다.
- **캐시 TTL 24시간(KST 자정 기준).** 같은 사용자가 하루에 여러 번 조회해도 생성은 한 번이다. 캐시 키는 `userId + 프로필 해시`로, 프로필이 바뀌면 자동 무효화된다. `RecommendationCache`가 KST `Clock`으로 날짜를 비교한다.
  - 캐시는 **프로세스 메모리**에 둔다. 인스턴스를 여럿 띄우면 인스턴스당 한 번씩 생성될 수 있고 재시작하면 사라진다. 생성 문항 자체는 DB에 남으므로 손실이 아니라 중복 생성 비용 문제다. 다중 인스턴스로 가면 캐시를 외부 저장소로 옮겨야 한다.
  - 캐시에 담긴 문항이 그 사이 거절(`REJECTED`)됐으면 캐시를 버리고 새로 만든다. 반환 순서는 처음 추천했던 순서를 유지한다.
- **폴백** — 쿼터 소진 또는 생성 실패 시, 취약 주제에 해당하는 **기존 문제은행 문항**을 SQL로 조회해 반환한다. 이때는 객관식·서술형을 모두 후보로 삼는다. 응답의 `generated`를 `false`로 내린다. 다만 이 값은 사용자에게 노출하지 않는다(운영·디버깅용) — 폴백인지 아닌지가 화면 문구를 바꾸지 않는다. 추천 자체가 실패하지 않는 것이 원칙이다.

## **콜드스타트 정책**

풀이 이력이 3건 미만이면 프로필이 무의미하므로 **AI를 호출하지 않는다.** 난이도 `LOW` 기존 문항을 카테고리별로 고르게 반환하고, `personalized`와 `generated`를 모두 `false`로 내린다.

## **알려진 리스크**

이 설계는 다음 위험을 안고 있다. 구현 전에 완화 방법을 정하거나, 최소한 감수한다는 사실을 인지한 상태로 진행한다.

### **1. 오개념 자기 강화**

기존 오답 해설을 few-shot으로 주입하면, AI가 그 오개념을 **정답으로 뒤집은** 문항이나 모범답안을 만들 수 있다. 프롬프트가 "이 오개념을 파고드는 문제를 만들라"는 의도인지 "이 내용을 참고하라"는 의도인지 모델이 혼동하기 쉽다. 주입 시 오답 해설임을 명시적으로 라벨링하고, 절대 정답 근거로 쓰지 말라는 지시를 넣는다.

### **2. 검증기는 형식만 본다**

`GeneratedEssayValidator`는 필드 존재·길이·일치 여부만 검사한다. **내용의 사실 정확성은 검증하지 못한다.** 잘못된 모범답안도 형식이 맞으면 통과한다.

완화책은 검수 승인 게이트다. 생성 문항은 `PENDING`으로 저장되고 관리자가 승인한 것만 문제은행·오늘의 면접 질문에 노출된다(위 "검수 승인 정책").

**단, 이것으로 리스크가 해소되지는 않는다.** 승인 게이트는 확산만 막는다. 생성 요청자는 승인 전에 그 문항을 받아 풀기 때문에 잘못된 문항이 최초 1명에게 도달하는 것은 그대로이고, 그 결과 리스크 3도 그대로 남는다.

### **3. 서술형은 오류가 두 번 증폭된다**

생성된 모범답안이 틀리면, 그 문항을 채점하는 단계에서도 틀린 기준으로 채점된다. 사용자가 맞게 써도 오답 처리될 수 있고, 그 오답이 다시 약점 프로필에 반영된다. 객관식은 정답이 이산값이라 검증이 쉽지만 서술형은 그렇지 않다. **생성 대상을 서술형으로 한정한 결정의 대가**이며, 이 설계에서 가장 큰 위험이다.

### **4. 추천 루프의 자기 참조**

생성 문항의 풀이 결과가 다시 약점 프로필에 반영되고, 그 프로필이 다음 생성의 근거가 된다. 특정 주제에서 잘못된 문항이 반복 생성되면 사용자는 그 주제에 계속 약한 것으로 판정돼 같은 문제가 계속 나온다. `source = GENERATED` 문항의 풀이 이력에 가중치를 낮게 주거나, 프로필 집계에서 제외하는 옵션을 고려한다. **승인된 문항도 포함해서 판단한다** — 승인은 노출 자격일 뿐 자기 참조 루프를 끊지 않으므로, 판별 기준은 `review_status`가 아니라 `source`다.

### **5. 쿼터 소비가 두 배다**

생성 시 1회, 그 문항을 사용자가 풀 때 채점으로 1회 이상. 서술형 채점은 꼬리질문까지 최대 3턴이므로 문항 하나가 최대 4회의 AI 호출을 유발한다. 요청당 6문항이면 최악의 경우 24회다. 쿼터 버킷 분리만으로는 부족하고, 1회 생성 문항 수 상한을 보수적으로 잡아야 한다.

### **6. 검수되지 않은 문항의 통계 오염**

`question_stat`은 사용자 무관 전역 집계인데, 특정 사용자만 푼 생성 문항이 여기 섞이면 `sample_count`가 낮은 상태로 평균이 형성된다. 표본 5건 미만 폴백이 이를 일부 막지만, 생성 문항은 애초에 표본이 쌓이기 어렵다. `question_stat` 집계 시 `source = GENERATED`를 분리 집계할지 결정한다. 승인되어 문제은행에 올라간 뒤에도 출신은 `GENERATED`로 남으므로 이 분리는 사후에도 가능하다.

## **남은 작업**

1. 관리자 승인 API·페이지 — `PENDING` 목록 조회와 `Question.approve()`/`reject()`를 호출하는 service·엔드포인트. 도메인 전이는 이미 있으므로 남은 것은 조회·엔드포인트·권한이다. **역할 체계 선행 필요**(현재 인증 계층은 `userId`만 해석).
2. 운영 배포 시 기존 행 백필 — 두 가지가 필요하다.
   - `question.source`·`question.review_status`: `UPDATE question SET source = 'SEEDED', review_status = 'APPROVED' WHERE source IS NULL OR review_status IS NULL`
   - `question_tag.tag_id`: 사전을 적재하고 이름으로 매칭해 채운 뒤 `name` 컬럼을 제거한다.
     ```sql
     INSERT INTO tag (name, category)
     SELECT DISTINCT qt.name, q.category FROM question_tag qt JOIN question q ON q.id = qt.question_id
     WHERE NOT EXISTS (SELECT 1 FROM tag t WHERE t.name = qt.name);
     UPDATE question_tag qt SET qt.tag_id = (SELECT t.id FROM tag t WHERE t.name = qt.name) WHERE qt.tag_id IS NULL;
     ```
3. 생성 문항의 저장된 채점 기준을 서술형 채점에 실제로 사용하기 — 현재는 저장까지만 하고, 채점은 여전히 AI가 그 자리에서 만든 모범답안을 쓴다. 리스크 3을 줄이려면 `gradingCriteria`를 채점 프롬프트에 주입해야 한다.
4. 다중 인스턴스 전환 시 추천 캐시를 외부 저장소로 이동.

## **테스트 기준**

- `MasteryPolicy` — 6개 판정 전수. 경계값(`ratio = 0.7`, `1.5`), 표본 미달 폴백, 서술형 저점수 우선순위.
- 객관식 기록 — 세션 저장이 문항마다 `RULE_CHOICE` 이력을 남기는지, 꼬리질문도 남기는지, 태그가 붙은 문항이 현재값을 갱신하는지(`SolvedSessionServiceTest`).
- `RecommendationTopicPolicy` — 카테고리 상위 3개 선정, 태그 최대 2개 부착, 목표 난이도 전환 경계, tie-break 결정성.
- `GeneratedEssayValidator` — 위반 항목별 케이스 전수. AI 응답을 고정 문자열 픽스처로 둔다.
- `WeaknessProfileCalculator` — 태그 표본 2건 미달 시 카테고리 폴백.
- 위 정책 클래스는 모두 Spring 컨텍스트와 LLM 호출 없이 순수 단위테스트로 작성한다.
- `EssayQuestionGenerator`는 `ChatClient`를 모킹해 프롬프트 조립과 폴백 분기만 검증한다. 생성 품질은 테스트 대상이 아니다.
- 숙련도 — 태그가 여러 개면 태그마다 이력·현재값이 남는지, 재판정 시 현재값이 덮어써지는지, 태그 없는 문항은 카테고리 신호로만 남는지, AI가 판정을 빠뜨리면 채점은 성공하고 기록만 생략되는지(`MasteryServiceTest`, `EssayAnswerServiceTest`).
- 약점 프로필 2트랙 — 서술형에서 AI 판정이 시간 기반 판정을 이기는지, 판정이 없으면 폴백하는지, 다른 사용자 판정이 섞이지 않는지(`WeaknessProfileCalculatorTest`).
- 생성 프롬프트 — 모든 변수로 렌더되고 미치환 중괄호가 없는지, 변수를 빼면 렌더가 실패하는지, 오답 해설 라벨링과 검증기 계약 문구가 들어 있는지(`EssayQuestionGenerationPromptTest`).
- 채점 프롬프트 — `EssayPromptV4`가 6분류 기준과 근거 요구를 담고 v3 규칙을 승계하는지(`EssayPromptV4Test`).
- 추천 서비스 — 콜드스타트, 생성 문항 저장(`PENDING` 확인), 캐시 재사용(문항 수 증가 없음), 검증 실패 시 폴백을 통합 테스트로 확인한다(`RecommendationServiceTest`).
- 문항 통계 집계 — 정답률·평균 계산, 시간 미측정 표본 제외, 꼬리질문 제외, 재집계 시 덮어쓰기(`QuestionStatAggregatorTest`).
- 노출 필터 — `PENDING`·`REJECTED` 문항이 목록·오늘의 면접 질문·진척도 분모에서 빠지고 단건 조회로는 잡히는지 확인한다. `findQuestions`는 페이징이므로 **countQuery에도 같은 조건이 들어가야 한다**(빠뜨리면 행은 맞고 총 개수만 틀린다). 기존 `QuestionRepositoryTest.findQuestions_excludesGeneratedSource`가 이 함정을 잡는 패턴이므로 그대로 준용한다.
