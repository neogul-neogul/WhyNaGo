# 페르소나 더미데이터 생성 프롬프트

> 이 문서는 **성격이 뚜렷한 가상 사용자 1명의 풀이 이력·숙련도 더미데이터를 SQL로 생성**하기 위해 LLM에 주는 규칙이다.
> 예: "운영체제는 강하지만 네트워크가 약하고, 특히 TCP에는 잘못된 정보를 많이 가진 사용자".
> 문항 시드를 만드는 문서(`SCRIPT.md`·`SCRIPT_ESSAY.md`)와 달리 **문항이 아니라 그 문항을 푼 흔적**을 만든다.
> 도메인 정의는 `docs/DOMAIN.md`, 숙련도·추천 정책은 `docs/RECOMMENDATION.md`를 따른다.

## [왜 필요한가]

추천·숙련도 화면은 풀이 이력이 있어야 의미가 생긴다. 로컬 시드(`data3.sql`)는 문항 600개만 넣고 이력은 0건이라, 로그인해도 콜드스타트 분기(난이도 하 문항 나열)만 보인다. 약점 프로필·취약 태그·맞춤 생성이 **의도대로 동작하는지 눈으로 확인**하려면 성격이 있는 사용자 데이터가 필요하다.

로컬 DB는 앱 프로세스 안의 H2 in-memory(`jdbc:h2:mem:whynago`)다. 외부 프로그램이 붙어 INSERT할 수 없다. 그래서 **부팅 시 `spring.sql.init`이 읽는 SQL 파일**로 만든다.

## [전제와 적용 방법]

- 전제: `data-tag.sql`(태그 사전 238개) → `data.sql`(테스트 유저) → `data3.sql`(문항 600개)이 **이미 로드된** DB. 생성물은 그 **뒤에** 로드한다.
- 적용: `src/main/resources/config/application-db-local.yml`의 `data-locations` 끝에 임시로 추가한다.
  ```yaml
  data-locations: classpath:data-tag.sql,classpath:data.sql,classpath:data3.sql,classpath:data-persona-net.sql
  ```
- **팀 공용 설정에 상시 등록하지 않는다.** 모두의 로컬 DB가 그 페르소나가 되어 버린다. 쓰고 나면 되돌린다.
- 생성물은 저장소에 커밋하지 않는다(개인 로컬 확인용 데이터다).

> ⚠️ 현재 `data.sql`은 `users.role`을 채우지 않아 **그 자체로 로드가 실패한다**(`NULL not allowed for column "ROLE"`). 페르소나 SQL은 자기 사용자를 직접 만들므로 영향을 받지 않지만, 로컬을 띄우려면 `data.sql`을 먼저 고쳐야 한다(별건).

## [페르소나 명세 형식]

입력은 아래 형식이면 충분하다. **태그 지정이 카테고리 지정을 덮어쓴다.**

```
페르소나: os-strong-network-weak
사용자: persona-net@test.test / 닉네임 netweak     (닉네임 4~8자 제약)
기간: 2026-07-28 ~ 2026-08-18 (최근 30일)
카테고리: OS=strong, ALGORITHM=average, NETWORK=weak
태그: TCP=misconception, 3-way handshake=misconception, 혼잡 제어=weak
분량: 객관식 세션 4개(문항 12), 서술형 세션 3개(본질문 3)
```

## [레벨 → 신호 매핑 — 이 문서의 핵심]

숙련 레벨 4종을 정답률·소요시간·서술형 점수·AI 판정으로 옮긴다.

| 레벨 | 정답률 | 내 소요시간 / 문항 평균 | 서술형 점수 | AI 숙련도 판정 |
| --- | --- | --- | --- | --- |
| `strong` | 0.9 | 0.5 ~ 0.7 (빠름) | 8 ~ 10 | `MASTERED` / `SOLID` |
| `average` | 0.65 | 0.9 ~ 1.2 (보통) | 6 ~ 8 | `SOLID` / `UNSTABLE` |
| `weak` | 0.35 | 1.4 ~ 2.0 (느림) | 3 ~ 5 | `WEAK` / `NOT_LEARNED` |
| `misconception` | 0.25 | **0.5 ~ 0.8 (빠름)** | 4 ~ 6 | `GUESSED` / `UNSTABLE` |

### `misconception`과 `weak`을 반드시 구분할 것

이 문서가 존재하는 이유의 절반이 이 구분이다.

- **`weak`** = 모른다. 오래 붙잡고도 틀린다. → 규칙 판정에서 `NOT_LEARNED`(느림 + 오답).
- **`misconception`** = 잘못 안다. **틀리는데 빠르다.** 확신이 있어 망설이지 않는다. → 규칙 판정에서 `GUESSED`(빠름 + 오답).

서술형에서도 성격이 다르다. `weak`은 답을 못 써서 짧고 점수가 낮다. `misconception`은 **길게 잘 쓰지만 인과가 뒤집혀 있어** 점수가 중간(4~6)에 걸린다.

`masteryReason`도 그 성격대로 쓴다.

| 레벨 | 근거 문장 예 |
| --- | --- |
| `strong` | `동작 원리와 실무에서의 영향을 순서대로 설명하고, 묻지 않은 예외 상황까지 스스로 짚었다.` |
| `average` | `결론과 근거는 맞지만 경계 조건을 언급하지 않아 적용 범위가 모호하다.` |
| `weak` | `개념 이름만 반복하고 동작 순서를 설명하지 못했다. 무엇을 모르는지도 특정하지 못했다.` |
| `misconception` | `흐름 제어의 목적을 네트워크 혼잡 완화로 설명했다. 수신 버퍼 보호와 혼잡 제어를 뒤바꿔 이해하고 있다.` |

**`misconception`의 근거에 "개념이 없다", "더 공부가 필요하다"를 쓰지 않는다.** 무엇을 무엇으로 착각했는지를 지목해야 그 데이터가 쓸모 있다.

### 알고 있어야 할 성질: `misconception`은 `weak`보다 약점도가 낮다

약점 가중치는 `GUESSED` 0.7 < `WEAK` 0.85 < `NOT_LEARNED` 1.0이다. 추천은 "아예 모르는 주제"를 오개념보다 먼저 조준한다.

그래서 **같은 카테고리에 `weak` 태그와 `misconception` 태그를 함께 두면, 생성 문항이 조준하는 상위 2개 태그는 `weak` 쪽이 차지한다.** 오개념 태그를 조준 대상으로 올리고 싶다면 그 카테고리에 `weak` 태그를 두지 않거나, 조준에서 밀린다는 것을 알고 쓴다. 문항 수를 늘려도 순위는 뒤집히지 않는다(가중치 평균이라 상한이 0.7이다).

## [문항 고르기]

문항 `id`는 AUTO_INCREMENT라 환경마다 다를 수 있으므로 **제목이나 id를 하드코딩하지 않는다.** 태그 또는 카테고리로 찾는다.

```sql
-- 태그로 고르기 (취약 태그를 정확히 겨냥할 때)
SET @q = (SELECT q.id FROM question q
          JOIN question_tag qt ON qt.question_id = q.id
          JOIN tag t ON t.id = qt.tag_id
          WHERE t.name = 'TCP' AND q.type = 'MULTIPLE_CHOICE'
          ORDER BY q.id LIMIT 1 OFFSET 1);

-- 카테고리로 고르기 (그 영역 아무 문항이나 필요할 때)
SET @q = (SELECT id FROM question
          WHERE category = 'OS' AND type = 'MULTIPLE_CHOICE'
          ORDER BY id LIMIT 1 OFFSET 0);
```

지켜야 할 것:

- `OFFSET`이 실제 개수를 넘으면 `@q`가 `NULL`이 되고 `question_id NOT NULL` 위반으로 로드가 깨진다. 아래 실측 표 범위 안에서 고른다.
- **같은 문항을 두 번 고르지 않는다.** `question_stat`의 PK가 `question_id`라 중복이면 PK 충돌이 난다.
- **제목이 3건 중복**이므로(`SOLID의 핵심 개념` 등) 제목으로 찾지 않는다.
- `TCP` 같은 태그는 관련 태그로도 널리 붙어 있어(`OSI 7계층의 핵심 개념`에도 `TCP`가 있다) 태그로 고른 문항이 그 주제의 문항이 아닐 수 있다. 아래 표의 `offset`을 보고 의도한 개념의 문항을 고른다.

### 실측: 자주 쓰는 태그·카테고리의 문항 수 (data3.sql 기준)

| 기준 | 객관식 | 서술형 |
| --- | --- | --- |
| 카테고리 `OS` / `NETWORK` / `ALGORITHM` 등 8개 카테고리 | 각 25 | 각 50 |
| 태그 `TCP` | 9 | 18 |
| 태그 `혼잡 제어` / `흐름 제어` | 2 / 3 | 4 / 6 |
| 태그 `3-way handshake` | 2 | 4 |
| 태그 `페이지 교체 알고리즘` | 3 | 6 |
| 태그 `인덱스` | 5 | 11 |

태그 `TCP` 객관식 `offset`별 문항(주 태그가 앞에 온다):

| offset | 문항 | 주 태그 |
| --- | --- | --- |
| 0 | OSI 7계층의 핵심 개념 | OSI 7계층 |
| 1 | **TCP의 핵심 개념** | TCP |
| 2 | UDP의 핵심 개념 | UDP |
| 3 | 3-way handshake의 핵심 개념 | 3-way handshake |
| 4 | 흐름 제어의 핵심 개념 | 흐름 제어 |
| 5 | 혼잡 제어의 핵심 개념 | 혼잡 제어 |

## [채울 테이블과 순서]

`users` → `solved_session` → (`solved_multiple_choice` | `essay_solved`) → `wrong_note` → `mastery_record` → `user_tag_mastery` → `question_stat`

| 테이블 | 채우는 규칙 |
| --- | --- |
| `users` | `role`은 **NOT NULL이고 DB 기본값이 없다** — 반드시 `'USER'`를 넣는다. `nickname` 4~8자·유일, `email` 유일. 비밀번호는 `data.sql`의 BCrypt 해시를 그대로 재사용하면 `test`로 로그인된다 |
| `solved_session` | 세션 1건 = 한 번의 풀이. `status`는 `'COMPLETED'`만 존재(완료 세션만 저장한다). `total_count`·`correct_count`가 **실제 문항 행과 맞아야 한다** — 진척도·기록 화면이 이 값을 쓴다. `started_at < solved_at` |
| `solved_multiple_choice` | 세션당 3행(`sequence` 1~3, `type`은 첫 행 `MAIN`·나머지 `FOLLOWUP`). `user_choice_id`·`answer_choice_id` 둘 다 NOT NULL이라 서브셀렉트로 채운다 |
| `essay_solved` | 세션당 3행(`MAIN` 1 + `FOLLOWUP` 2). **꼬리질문 행은 `question_id`가 `NULL`**이다(런타임에 AI가 만든 질문이라 참조할 문항이 없다). `feedback`·`model_answer`·`question_text`·`user_answer`는 NOT NULL |
| `wrong_note` | **오답이 하나라도 있는 세션당 1건**이다. 문항 단위가 아니며 `(user_id, solved_session_id)`가 유일 |
| `mastery_record` | **서술형 본질문에만** 남긴다(`source = 'AI_ESSAY'`). 그 문항의 태그 개수만큼 행이 생긴다 |
| `user_tag_mastery` | 태그당 1행. **가장 마지막 판정이 이긴다** |
| `question_stat` | 아래 [모집단 평균] 참고 |

### 객관식 숙련도를 기록하지 않는 이유

운영에서 `mastery_record`는 **서술형 채점 AI가 판정할 때만** 생긴다. 객관식은 AI를 호출하지 않고 추천이 조회 시점에 시간 기반으로 계산한다(`MasteryPolicy`). 그래서 더미데이터도 객관식 판정을 `RULE_CHOICE`로 넣지 않는다. 넣으면 숙련도 화면이 실제 서비스와 다르게 보인다.

### `user_tag_mastery`는 시각 순서에 좌우된다

한 태그가 여러 서술형 문항에 걸리면 **가장 나중 판정만 현재값으로 남는다.** 예를 들어 `TCP` 태그가 `혼잡 제어의 원리와 목적`(WEAK)과 `TCP의 원리와 목적`(GUESSED)에 모두 붙어 있다면, TCP를 `GUESSED`(오개념)로 보이게 하려면 **TCP 문항 세션을 더 나중 시각에 배치**해야 한다. 페르소나 의도가 시각 배치에 달려 있다.

### [모집단 평균] `question_stat`은 페르소나와 무관한 기준선으로 넣는다

숙련도 규칙 판정은 `내 소요시간 / question_stat.avg_elapsed_seconds` 비율을 본다. **페르소나의 소요시간으로 이 평균을 만들면 비율이 항상 1 근처가 되어 "빠르게 틀림(`GUESSED`)"이 표현되지 않는다.**

그래서 순서를 뒤집는다.

1. 문항 평균을 **먼저** 정한다: `avg_elapsed_seconds` 120~180초, `sample_count` 8 이상(신뢰 임계가 5라서 이보다 커야 평균이 쓰인다), `correct_rate` 0.4~0.7.
2. 페르소나의 `elapsed_seconds`를 **그 평균 × 레벨 비율**로 계산한다. 예: 평균 150초 × 0.52 = 78초(빠름), × 1.6 = 240초(느림).

> ⚠️ 야간 배치(`QuestionStatScheduler`, KST 03:30)가 돌면 이 기준선이 페르소나 자신의 기록으로 덮어써진다. 확인 중에는 배치를 돌리지 않거나, 덮어써진 뒤에는 페르소나 SQL을 다시 로드한다.

### 서술형의 소요시간도 레벨 비율을 지킨다

서술형은 AI 판정을 그대로 저장하니 시간이 상관없어 보이지만, 채점 프롬프트(`EssayPromptV6`)가 **소요시간과 문항 평균을 함께 받아 mastery 판정의 보조 근거로 쓴다.** 그래서 더미데이터도 서술형 `elapsed_seconds`를 레벨 비율에 맞춰야 저장된 판정과 시간이 어긋나지 않는다.

예시 1은 서술형 평균을 180초로 두고 `strong` 96초(0.53), `misconception` 92초(0.51), `weak` 288초(1.6)로 맞췄다. 오개념 페르소나가 **길게 쓰면서도 빨리 제출하는** 모습이 시간에도 드러난다.

## [ID·SQL 규칙]

- `id`는 AUTO_INCREMENT다. INSERT문에 `id`를 쓰지 않고 `SET @u = LAST_INSERT_ID();`로 캡처한다.
- **대입은 `=`를 쓴다.** H2 MySQL 호환모드에서 `SET @v := ...`는 문법 오류다.
- `users`·`solved_session`은 **한 행씩** INSERT해야 `LAST_INSERT_ID()`가 그 행을 가리킨다.
- 변수 이름: 사용자 `@u`, N번째 세션 `@sN`, 문항 `@qXxx`.
- 작은따옴표는 `''`로 이스케이프한다.

---

# few-shot 예시 1 — 완성형 (그대로 실행된다)

**페르소나**: `OS=strong`, `ALGORITHM=average`, `NETWORK=weak`, `TCP=misconception`
**측정된 결과**: 최취약 카테고리 `NETWORK`(약점도 0.769) → 목표 난이도 `LOW`, 신뢰 태그 상위는 `혼잡 제어`(0.95) · `흐름 제어`(0.85) · `TCP`(0.769)

세션은 **시각 순서대로** 번호를 붙였다. 마지막(@s7)이 TCP 서술형이라 `TCP`의 현재 숙련도가 `GUESSED`로 남는다.

```sql
-- ============================================================
-- 페르소나: os-strong-network-weak (TCP 오개념)
-- 전제: data-tag.sql, data.sql, data3.sql 로드 후 실행
-- ============================================================

-- 1) 사용자 (비밀번호는 data.sql과 같은 해시 = "test")
INSERT INTO users (email, password, nickname, provider, provider_id, position, daily_goal, role) VALUES
('persona-net@test.test', '$2a$10$QnOWMKP6UpzZHYzphdiuaOyg.Ei2ihHclJ1r5YmU0WYsvxQxSi/8q',
 'netweak', 'LOCAL', NULL, 'BACKEND', 10, 'USER');
SET @u = LAST_INSERT_ID();

-- 2) 이 페르소나가 푼 문항 15개를 잡아 둔다 (태그/카테고리 + OFFSET, 중복 없음)
SET @qOs1 = (SELECT id FROM question WHERE category = 'OS' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 0);
SET @qOs2 = (SELECT id FROM question WHERE category = 'OS' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 2);
SET @qOs3 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = '페이지 교체 알고리즘' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 1);
SET @qOsEssay = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = '페이지 교체 알고리즘' AND q.type = 'ESSAY' ORDER BY q.id LIMIT 1 OFFSET 2);

SET @qAl1 = (SELECT id FROM question WHERE category = 'ALGORITHM' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 0);
SET @qAl2 = (SELECT id FROM question WHERE category = 'ALGORITHM' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 1);
SET @qAl3 = (SELECT id FROM question WHERE category = 'ALGORITHM' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 3);

-- 느리게 틀릴 문항 (NETWORK weak): 흐름 제어 / 혼잡 제어 / OSI 7계층
SET @qNw1 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = '혼잡 제어' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 0);
SET @qNw2 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = '혼잡 제어' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 1);
SET @qNw3 = (SELECT id FROM question WHERE category = 'NETWORK' AND type = 'MULTIPLE_CHOICE' ORDER BY id LIMIT 1 OFFSET 0);
SET @qNwEssay = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = '혼잡 제어' AND q.type = 'ESSAY' ORDER BY q.id LIMIT 1 OFFSET 2);

-- 빠르게 틀릴 문항 (TCP misconception): TCP / UDP / 3-way handshake
SET @qTcp1 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = 'TCP' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 1);
SET @qTcp2 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = 'TCP' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 2);
SET @qTcp3 = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = 'TCP' AND q.type = 'MULTIPLE_CHOICE' ORDER BY q.id LIMIT 1 OFFSET 3);
SET @qTcpEssay = (SELECT q.id FROM question q JOIN question_tag qt ON qt.question_id = q.id JOIN tag t ON t.id = qt.tag_id
             WHERE t.name = 'TCP' AND q.type = 'ESSAY' ORDER BY q.id LIMIT 1 OFFSET 2);

-- 3) 모집단 평균 (페르소나와 무관한 기준선. 이걸 먼저 정하고 소요시간을 역산한다)
INSERT INTO question_stat (question_id, avg_elapsed_seconds, correct_rate, sample_count, updated_at) VALUES
(@qOs1, 150, 0.62, 12, '2026-08-19 03:30:00'),
(@qOs2, 150, 0.55, 11, '2026-08-19 03:30:00'),
(@qOs3, 150, 0.48, 9,  '2026-08-19 03:30:00'),
(@qOsEssay, 180, 0.51, 8, '2026-08-19 03:30:00'),
(@qAl1, 150, 0.66, 14, '2026-08-19 03:30:00'),
(@qAl2, 150, 0.58, 10, '2026-08-19 03:30:00'),
(@qAl3, 150, 0.52, 9,  '2026-08-19 03:30:00'),
(@qNw1, 150, 0.47, 12, '2026-08-19 03:30:00'),
(@qNw2, 150, 0.44, 11, '2026-08-19 03:30:00'),
(@qNw3, 150, 0.61, 13, '2026-08-19 03:30:00'),
(@qNwEssay, 180, 0.43, 8, '2026-08-19 03:30:00'),
(@qTcp1, 150, 0.45, 12, '2026-08-19 03:30:00'),
(@qTcp2, 150, 0.53, 10, '2026-08-19 03:30:00'),
(@qTcp3, 150, 0.49, 9,  '2026-08-19 03:30:00'),
(@qTcpEssay, 180, 0.46, 8, '2026-08-19 03:30:00');

-- ============================================================
-- 세션 1 (07-28) OS 객관식 — strong: 3문항 전부 정답, 78초(150 x 0.52 = 빠름)
--   빠름 + 정답 -> MASTERED (약점 가중치 0.0)
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'MULTIPLE_CHOICE', 'COMPLETED', 3, 3, '2026-07-28 21:00:00', '2026-07-28 21:04:30', '2026-07-28 21:04:30');
SET @s1 = LAST_INSERT_ID();

INSERT INTO solved_multiple_choice
  (solved_session_id, user_id, question_id, type, sequence, user_choice_id, answer_choice_id, is_correct, elapsed_seconds, solved_at) VALUES
(@s1, @u, @qOs1, 'MAIN', 1,
 (SELECT id FROM answer_choice WHERE question_id = @qOs1 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qOs1 AND is_correct = TRUE LIMIT 1), TRUE, 78, '2026-07-28 21:01:20'),
(@s1, @u, @qOs2, 'FOLLOWUP', 2,
 (SELECT id FROM answer_choice WHERE question_id = @qOs2 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qOs2 AND is_correct = TRUE LIMIT 1), TRUE, 72, '2026-07-28 21:02:40'),
(@s1, @u, @qOs3, 'FOLLOWUP', 3,
 (SELECT id FROM answer_choice WHERE question_id = @qOs3 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qOs3 AND is_correct = TRUE LIMIT 1), TRUE, 80, '2026-07-28 21:04:30');

-- ============================================================
-- 세션 2 (08-02) OS 서술형 — strong: 점수 9, AI 판정 MASTERED
--   꼬리질문 2개는 question_id가 NULL이고 숙련도 기록 대상이 아니다.
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'ESSAY', 'COMPLETED', 3, 3, '2026-08-02 22:00:00', '2026-08-02 22:07:10', '2026-08-02 22:07:10');
SET @s2 = LAST_INSERT_ID();

INSERT INTO essay_solved
  (solved_session_id, user_id, type, sequence, question_id, question_text, user_answer, feedback, model_answer,
   is_correct, score, elapsed_seconds, solved_at) VALUES
(@s2, @u, 'MAIN', 1, @qOsEssay,
 (SELECT content FROM question WHERE id = @qOsEssay),
 'LRU는 가장 오래 참조되지 않은 페이지를 교체합니다. 참조 지역성 덕분에 최근에 쓰인 페이지가 다시 쓰일 확률이 높아 히트율이 높지만, 순차 스캔처럼 지역성이 없는 접근 패턴에서는 캐시가 통째로 밀려납니다. 구현은 참조 시각을 갱신해야 해서 오버헤드가 있어 실제로는 근사 알고리즘을 씁니다.',
 '교체 기준과 지역성의 관계, 순차 스캔에서의 한계까지 짚었습니다. 근사 구현의 이유도 정확합니다.',
 '페이지 교체 알고리즘은 어떤 페이지를 내릴지 정하는 규칙이며, LRU는 참조 지역성을 근거로 가장 오래 참조되지 않은 페이지를 고릅니다...',
 TRUE, 9, 96, '2026-08-02 22:03:40'),
(@s2, @u, 'FOLLOWUP', 2, NULL,
 'LRU를 실제 구현할 때 어떤 비용이 생기는지 설명해 보세요.',
 '참조될 때마다 순서를 갱신해야 하므로 연결 리스트와 해시를 함께 써야 하고, 하드웨어 지원이 없으면 비용이 커서 클럭 알고리즘 같은 근사를 씁니다.',
 '자료구조 조합과 근사 알고리즘의 등장 이유를 함께 설명했습니다.',
 'LRU는 참조 순서를 유지해야 하므로...',
 TRUE, 9, 88, '2026-08-02 22:05:30'),
(@s2, @u, 'FOLLOWUP', 3, NULL,
 '지역성이 거의 없는 워크로드에서는 어떤 선택이 나은가요?',
 '지역성이 없으면 LRU의 예측이 빗나가므로 스캔 저항성이 있는 정책이나 용량 분리를 고려합니다.',
 '워크로드 특성에 따라 정책을 바꾼다는 판단이 정확합니다.',
 '스캔 위주 워크로드에서는 LRU가 캐시를 오염시키므로...',
 TRUE, 8, 92, '2026-08-02 22:07:10');

-- OS 서술형 판정: 이 문항의 태그(페이지 교체 알고리즘/페이징/캐시와 지역성)마다 1행
INSERT INTO mastery_record (user_id, question_id, tag_id, category, level, reason, source, created_at)
SELECT @u, @qOsEssay, qt.tag_id, 'OS', 'MASTERED',
       '교체 기준과 참조 지역성의 관계를 순서대로 설명하고, 순차 스캔에서의 한계까지 스스로 짚었다.',
       'AI_ESSAY', '2026-08-02 22:03:40'
FROM question_tag qt WHERE qt.question_id = @qOsEssay;

-- ============================================================
-- 세션 3 (08-06) 알고리즘 객관식 — average: 2정답 1오답, 150초(비율 1.0 = 보통)
--   보통 + 정답 -> SOLID(0.2), 보통 + 오답 -> WEAK(0.85)
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'MULTIPLE_CHOICE', 'COMPLETED', 3, 2, '2026-08-06 20:30:00', '2026-08-06 20:38:00', '2026-08-06 20:38:00');
SET @s3 = LAST_INSERT_ID();

INSERT INTO solved_multiple_choice
  (solved_session_id, user_id, question_id, type, sequence, user_choice_id, answer_choice_id, is_correct, elapsed_seconds, solved_at) VALUES
(@s3, @u, @qAl1, 'MAIN', 1,
 (SELECT id FROM answer_choice WHERE question_id = @qAl1 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qAl1 AND is_correct = TRUE LIMIT 1), TRUE, 150, '2026-08-06 20:32:30'),
(@s3, @u, @qAl2, 'FOLLOWUP', 2,
 (SELECT id FROM answer_choice WHERE question_id = @qAl2 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qAl2 AND is_correct = TRUE LIMIT 1), TRUE, 144, '2026-08-06 20:35:00'),
(@s3, @u, @qAl3, 'FOLLOWUP', 3,
 (SELECT id FROM answer_choice WHERE question_id = @qAl3 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qAl3 AND is_correct = TRUE LIMIT 1), FALSE, 158, '2026-08-06 20:38:00');

INSERT INTO wrong_note (user_id, solved_session_id, is_bookmarked) VALUES (@u, @s3, FALSE);

-- ============================================================
-- 세션 4 (08-10) 네트워크 객관식 — weak: 1정답 2오답, 240초(150 x 1.6 = 느림)
--   느림 + 오답 -> NOT_LEARNED(1.0), 느림 + 정답 -> UNSTABLE(0.5)
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'MULTIPLE_CHOICE', 'COMPLETED', 3, 1, '2026-08-10 21:20:00', '2026-08-10 21:33:00', '2026-08-10 21:33:00');
SET @s4 = LAST_INSERT_ID();

INSERT INTO solved_multiple_choice
  (solved_session_id, user_id, question_id, type, sequence, user_choice_id, answer_choice_id, is_correct, elapsed_seconds, solved_at) VALUES
(@s4, @u, @qNw1, 'MAIN', 1,
 (SELECT id FROM answer_choice WHERE question_id = @qNw1 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qNw1 AND is_correct = TRUE LIMIT 1), FALSE, 240, '2026-08-10 21:24:00'),
(@s4, @u, @qNw2, 'FOLLOWUP', 2,
 (SELECT id FROM answer_choice WHERE question_id = @qNw2 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qNw2 AND is_correct = TRUE LIMIT 1), FALSE, 252, '2026-08-10 21:28:30'),
(@s4, @u, @qNw3, 'FOLLOWUP', 3,
 (SELECT id FROM answer_choice WHERE question_id = @qNw3 AND is_correct = TRUE LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qNw3 AND is_correct = TRUE LIMIT 1), TRUE, 246, '2026-08-10 21:33:00');

INSERT INTO wrong_note (user_id, solved_session_id, is_bookmarked) VALUES (@u, @s4, FALSE);

-- ============================================================
-- 세션 5 (08-13) 네트워크 서술형(혼잡 제어) — weak: 점수 4, AI 판정 WEAK
--   TCP 세션(@s7)보다 앞에 둔다. 뒤에 두면 TCP 현재 숙련도가 WEAK로 덮인다.
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'ESSAY', 'COMPLETED', 3, 0, '2026-08-13 22:10:00', '2026-08-13 22:21:00', '2026-08-13 22:21:00');
SET @s5 = LAST_INSERT_ID();

INSERT INTO essay_solved
  (solved_session_id, user_id, type, sequence, question_id, question_text, user_answer, feedback, model_answer,
   is_correct, score, elapsed_seconds, solved_at) VALUES
(@s5, @u, 'MAIN', 1, @qNwEssay,
 (SELECT content FROM question WHERE id = @qNwEssay),
 '혼잡 제어는 수신자가 처리할 수 있는 양을 넘지 않게 조절하는 것입니다. 윈도우 크기를 수신자가 알려주면 그만큼만 보냅니다.',
 '설명한 내용은 혼잡 제어가 아니라 흐름 제어입니다. 혼잡 제어는 네트워크 경로 전체의 혼잡을 다루며, 느린 시작과 혼잡 회피로 전송률을 조절합니다.',
 '혼잡 제어는 네트워크 전체의 혼잡을 막기 위해 송신률을 조절하는 기법으로...',
 FALSE, 4, 288, '2026-08-13 22:15:00'),
(@s5, @u, 'FOLLOWUP', 2, NULL,
 '느린 시작(Slow Start)이 필요한 이유는 무엇인가요?',
 '처음부터 많이 보내면 수신 버퍼가 넘치기 때문입니다.',
 '수신 버퍼 보호는 흐름 제어의 목적입니다. 느린 시작은 경로의 여유를 모르는 상태에서 혼잡을 피하려는 장치입니다.',
 '느린 시작은 경로 용량을 모르는 초기에 전송률을 지수적으로 늘려 탐색하는 단계로...',
 FALSE, 3, 264, '2026-08-13 22:18:30'),
(@s5, @u, 'FOLLOWUP', 3, NULL,
 '혼잡이 감지되면 송신 측은 무엇을 근거로 판단하나요?',
 '수신자가 윈도우를 줄여서 알려줍니다.',
 '수신 윈도우는 흐름 제어 신호입니다. 혼잡 판단은 중복 ACK와 타임아웃 같은 손실 신호로 합니다.',
 '송신 측은 중복 ACK와 재전송 타임아웃을 혼잡 신호로 해석해...',
 FALSE, 3, 258, '2026-08-13 22:21:00');

INSERT INTO wrong_note (user_id, solved_session_id, is_bookmarked) VALUES (@u, @s5, TRUE);

INSERT INTO mastery_record (user_id, question_id, tag_id, category, level, reason, source, created_at)
SELECT @u, @qNwEssay, qt.tag_id, 'NETWORK', 'WEAK',
       '혼잡 제어를 수신 버퍼 보호로 설명해 흐름 제어와 뒤바꿨다. 손실 신호로 혼잡을 판단하는 부분은 언급하지 못했다.',
       'AI_ESSAY', '2026-08-13 22:15:00'
FROM question_tag qt WHERE qt.question_id = @qNwEssay;

-- ============================================================
-- 세션 6 (08-16) 네트워크 객관식(TCP) — misconception: 3문항 전부 오답인데 82초(150 x 0.55 = 빠름)
--   빠름 + 오답 -> GUESSED(0.7). 확신이 있어 망설이지 않는 상태를 이렇게 표현한다.
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'MULTIPLE_CHOICE', 'COMPLETED', 3, 0, '2026-08-16 21:05:00', '2026-08-16 21:09:20', '2026-08-16 21:09:20');
SET @s6 = LAST_INSERT_ID();

INSERT INTO solved_multiple_choice
  (solved_session_id, user_id, question_id, type, sequence, user_choice_id, answer_choice_id, is_correct, elapsed_seconds, solved_at) VALUES
(@s6, @u, @qTcp1, 'MAIN', 1,
 (SELECT id FROM answer_choice WHERE question_id = @qTcp1 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qTcp1 AND is_correct = TRUE LIMIT 1), FALSE, 82, '2026-08-16 21:06:25'),
(@s6, @u, @qTcp2, 'FOLLOWUP', 2,
 (SELECT id FROM answer_choice WHERE question_id = @qTcp2 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qTcp2 AND is_correct = TRUE LIMIT 1), FALSE, 76, '2026-08-16 21:07:50'),
(@s6, @u, @qTcp3, 'FOLLOWUP', 3,
 (SELECT id FROM answer_choice WHERE question_id = @qTcp3 AND is_correct = FALSE ORDER BY sequence LIMIT 1),
 (SELECT id FROM answer_choice WHERE question_id = @qTcp3 AND is_correct = TRUE LIMIT 1), FALSE, 88, '2026-08-16 21:09:20');

INSERT INTO wrong_note (user_id, solved_session_id, is_bookmarked) VALUES (@u, @s6, FALSE);

-- ============================================================
-- 세션 7 (08-18) 네트워크 서술형(TCP) — misconception: 점수 5, AI 판정 GUESSED
--   길게 쓰지만 인과가 뒤집혀 있어 점수가 중간에 걸린다. 가장 나중 세션이라
--   TCP/3-way handshake/흐름 제어의 현재 숙련도가 GUESSED로 남는다.
-- ============================================================
INSERT INTO solved_session (user_id, type, status, total_count, correct_count, started_at, solved_at, created_at) VALUES
(@u, 'ESSAY', 'COMPLETED', 3, 0, '2026-08-18 22:15:00', '2026-08-18 22:24:40', '2026-08-18 22:24:40');
SET @s7 = LAST_INSERT_ID();

INSERT INTO essay_solved
  (solved_session_id, user_id, type, sequence, question_id, question_text, user_answer, feedback, model_answer,
   is_correct, score, elapsed_seconds, solved_at) VALUES
(@s7, @u, 'MAIN', 1, @qTcpEssay,
 (SELECT content FROM question WHERE id = @qTcpEssay),
 'TCP는 3-way handshake로 연결을 맺고 흐름 제어로 네트워크 혼잡을 조절합니다. 흐름 제어는 라우터가 붐비면 송신 속도를 낮추는 장치이고, 혼잡 제어는 수신자의 버퍼 크기에 맞추는 장치입니다. 그래서 혼잡한 구간에서는 흐름 제어가 먼저 동작하고, 수신자가 느리면 혼잡 제어가 개입합니다.',
 '흐름 제어와 혼잡 제어의 역할이 서로 바뀌어 있습니다. 흐름 제어는 수신 버퍼 보호, 혼잡 제어는 경로 혼잡 대응입니다. 연결 수립 절차 설명은 정확합니다.',
 'TCP는 연결 지향 프로토콜로 3-way handshake로 연결을 수립하고, 흐름 제어는 수신자의 처리 속도에 맞춰...',
 FALSE, 5, 92, '2026-08-18 22:19:00'),
(@s7, @u, 'FOLLOWUP', 2, NULL,
 '수신 윈도우(rwnd)는 어떤 목적으로 쓰이나요?',
 '경로가 혼잡한지 알려주는 값입니다. 값이 작아지면 네트워크가 붐빈다는 뜻입니다.',
 'rwnd는 수신자의 남은 버퍼 크기이며 흐름 제어 신호입니다. 경로 혼잡은 손실 신호로 판단합니다.',
 'rwnd는 수신자가 지금 더 받을 수 있는 양을 알리는 값으로...',
 FALSE, 4, 78, '2026-08-18 22:22:10'),
(@s7, @u, 'FOLLOWUP', 3, NULL,
 '3-way handshake에서 SYN을 두 번 주고받는 이유는 무엇인가요?',
 '양쪽이 서로의 시작 순서 번호를 알아야 하기 때문입니다.',
 '양방향 순서 번호 동기화라는 핵심은 맞게 짚었습니다.',
 '연결은 양방향이므로 각 방향의 초기 순서 번호를 서로 확인해야 하며...',
 TRUE, 7, 84, '2026-08-18 22:24:40');

INSERT INTO wrong_note (user_id, solved_session_id, is_bookmarked) VALUES (@u, @s7, FALSE);

INSERT INTO mastery_record (user_id, question_id, tag_id, category, level, reason, source, created_at)
SELECT @u, @qTcpEssay, qt.tag_id, 'NETWORK', 'GUESSED',
       '흐름 제어를 경로 혼잡 대응으로, 혼잡 제어를 수신 버퍼 보호로 설명해 두 개념을 맞바꿨다. rwnd도 혼잡 신호로 오해하고 있다.',
       'AI_ESSAY', '2026-08-18 22:19:00'
FROM question_tag qt WHERE qt.question_id = @qTcpEssay;

-- ============================================================
-- 태그별 현재 숙련도 (마지막 판정이 이긴다)
--   OS 서술형(08-02) -> 페이징 계열 MASTERED
--   혼잡 서술형(08-13) -> 혼잡 제어 WEAK
--   TCP 서술형(08-18, 최신) -> TCP / 3-way handshake / 흐름 제어 GUESSED
-- ============================================================
INSERT INTO user_tag_mastery (user_id, tag_id, level, reason, updated_at) VALUES
(@u, (SELECT id FROM tag WHERE name = '페이지 교체 알고리즘'), 'MASTERED',
 '교체 기준과 참조 지역성의 관계를 순서대로 설명했다.', '2026-08-02 22:03:40'),
(@u, (SELECT id FROM tag WHERE name = '페이징'), 'MASTERED',
 '교체 기준과 참조 지역성의 관계를 순서대로 설명했다.', '2026-08-02 22:03:40'),
(@u, (SELECT id FROM tag WHERE name = '캐시와 지역성'), 'MASTERED',
 '순차 스캔에서 지역성이 깨지는 경우까지 스스로 짚었다.', '2026-08-02 22:03:40'),
(@u, (SELECT id FROM tag WHERE name = '혼잡 제어'), 'WEAK',
 '혼잡 제어를 수신 버퍼 보호로 설명해 흐름 제어와 뒤바꿨다.', '2026-08-13 22:15:00'),
(@u, (SELECT id FROM tag WHERE name = 'TCP'), 'GUESSED',
 '흐름 제어와 혼잡 제어의 역할을 맞바꿔 설명했다. 확신 있게 틀린 서술을 이어갔다.', '2026-08-18 22:19:00'),
(@u, (SELECT id FROM tag WHERE name = '3-way handshake'), 'GUESSED',
 '연결 수립 절차는 맞게 설명했지만 그 뒤의 제어 개념을 뒤바꿨다.', '2026-08-18 22:19:00'),
(@u, (SELECT id FROM tag WHERE name = '흐름 제어'), 'GUESSED',
 '흐름 제어를 경로 혼잡 대응으로 설명했다. rwnd도 혼잡 신호로 오해하고 있다.', '2026-08-18 22:19:00');
```

## 예시 1의 기대 결과 (실제로 로드해 측정한 값)

문항 15개 · 세션 7개 · 풀이 행 21개(객관식 12 + 서술형 9) · 오답노트 5건 · 판정 이력 9행 · 태그별 현재값 7행.

**카테고리 약점도** — 아래는 `WeaknessProfileCalculator`가 실제로 계산한 값이다.

| 카테고리 | 계산 | 약점도 | 목표 난이도 |
| --- | --- | --- | --- |
| `NETWORK` | (GUESSED 0.7×3 + NOT_LEARNED 1.0×2 + UNSTABLE 0.5 + WEAK 0.85 + GUESSED 0.7) / 8 | **0.769** | `LOW` |
| `ALGORITHM` | (SOLID 0.2×2 + WEAK 0.85) / 3 | 0.417 | `MEDIUM` |
| `OS` | MASTERED 0.0×4 | 0.000 | `HIGH` |

**태그 약점도** (신뢰 = 표본 2건 이상)

| 태그 | 약점도 | 표본 | 신뢰 |
| --- | --- | --- | --- |
| 혼잡 제어 | 0.950 | 3 | O |
| 흐름 제어 | 0.850 | 5 | O |
| TCP | 0.769 | 8 | O |
| 3-way handshake | 0.700 | 3 | O |
| OSI 7계층 · UDP 등 | 0.769 | 1 | X (카테고리 값으로 폴백) |

- 최취약 카테고리는 `NETWORK`이고 약점도가 0.7을 넘어 목표 난이도가 `LOW`로 내려간다(개념 회복이 목적).
- 프로필의 `solvedCount`는 **15**다. 객관식 12 + 서술형 본질문 3만 세고, 꼬리질문 6행은 참조할 문항이 없어 빠진다. 콜드스타트 기준(3건 미만)에 걸리지 않는다.
- **조준 태그는 `혼잡 제어`·`흐름 제어`다.** `TCP`가 "특히 약한 태그"로 지정됐지만 오개념(`GUESSED` 0.7)이 개념 없음(`WEAK`·`NOT_LEARNED`)보다 가중치가 낮아 3위로 밀린다(위 [성질] 참고). TCP를 조준 대상으로 만들려면 같은 카테고리에서 `weak` 지정을 빼야 한다.
- `TCP`의 약점도가 0.769로 카테고리 값과 같은 것은 우연이 아니다 — `TCP` 태그가 이 카테고리에서 푼 8문항 전부에 붙어 있어 평균이 카테고리 평균과 같아진다. **관련 태그로 널리 쓰이는 태그는 약점도가 카테고리 평균으로 수렴한다.**
- 숙련도 조회(`GET /api/mastery`)에서 `TCP`는 `GUESSED`로 내려간다(가장 나중 세션 @s7의 판정).

# few-shot 예시 2 — 명세만 (같은 규칙으로 확장한다)

### DB 강함 / 알고리즘 약함

```
사용자: persona-algo@test.test / algoweak
카테고리: DB=strong, NETWORK=average, ALGORITHM=weak
태그: 시간 복잡도=weak, DFS=misconception
분량: 객관식 세션 4개, 서술형 세션 3개
기대: 최취약 ALGORITHM(약점도 0.8 내외) -> 목표 난이도 LOW,
     DFS는 빠른 오답이 쌓여 GUESSED로 남는다
```

### 콜드스타트 경계 확인용 초보자

```
사용자: persona-new@test.test / newbie1
카테고리: 전 영역 average
분량: 객관식 세션 1개(문항 2) — 풀이 이력 2건
기대: 이력이 3건 미만이라 추천이 personalized=false, generated=false로 내려가고
     난이도 LOW 문항이 카테고리별로 고르게 반환된다
```

이력을 1건만 더 늘리면(문항 3건) 개인화 분기로 넘어간다. 경계를 확인할 때 쓴다.

## [Self-Reflection — 생성 후 반드시 점검]

1. `solved_session.total_count`·`correct_count`가 실제 풀이 행 수·정답 수와 **정확히** 같은가?
2. 문항 `OFFSET`이 실측 개수 범위 안인가? 같은 문항을 두 번 고르지 않았는가(`question_stat` PK 충돌)?
3. `elapsed_seconds`가 `question_stat.avg_elapsed_seconds × 레벨 비율`로 계산됐는가? 경계값(0.7, 1.5)을 의도한 쪽에 두었는가?
4. **취약 태그의 표본이 2건 이상인가?** 미달이면 프로필에서 그 태그가 카테고리 값으로 폴백되어 "특히 이 태그가 약하다"가 사라진다.
5. `user_tag_mastery`의 시각 순서가 의도와 맞는가? 오개념으로 남기고 싶은 태그의 세션이 가장 나중인가?
6. 서술형 점수와 AI 판정이 모순되지 않는가(9점 + `NOT_LEARNED` 금지, 3점 + `MASTERED` 금지)?
7. 꼬리질문 행의 `question_id`가 `NULL`인가? 그 행에 대한 `mastery_record`를 만들지 않았는가?
8. `users.role`을 채웠는가? `nickname`이 4~8자인가? `email`·`nickname`이 기존 데이터와 겹치지 않는가?
9. `misconception`의 근거 문장이 "무엇을 무엇으로 착각했는지"를 지목하는가? "개념이 없다"로 쓰지 않았는가?

## [로드 후 검증]

```sql
-- 카테고리별 정답률·평균 소요시간이 페르소나 의도와 맞는지
SELECT q.category,
       COUNT(*) AS solved,
       ROUND(AVG(CASE WHEN s.is_correct THEN 1 ELSE 0 END), 2) AS accuracy,
       ROUND(AVG(s.elapsed_seconds)) AS avg_elapsed
FROM solved_multiple_choice s JOIN question q ON q.id = s.question_id
WHERE s.user_id = (SELECT id FROM users WHERE email = 'persona-net@test.test')
GROUP BY q.category;

-- 태그별 현재 숙련도
SELECT t.name, m.level, m.updated_at
FROM user_tag_mastery m JOIN tag t ON t.id = m.tag_id
WHERE m.user_id = (SELECT id FROM users WHERE email = 'persona-net@test.test')
ORDER BY m.updated_at DESC;
```

API로도 확인한다(`test` 비밀번호로 로그인해 토큰을 받는다).

1. `GET /api/mastery` — 취약 태그가 상위에 오고 근거 문장이 함께 내려오는지
2. `GET /api/recommendations/questions` — `personalized: true`이고 취약 카테고리를 조준하는지. `API_KEY`가 없으면 Mock 생성기가 동작해 `[MOCK]` 문항이 내려온다
3. `GET /api/wrong-notes` — 오답 세션이 목록에 보이는지

## [출력 제약]

- 출력은 **SQL 주석 + INSERT/SET 문**만이다. 머리말·맺음말·마크다운 코드펜스를 붙이지 않는다.
- 문항(`question`)·태그(`tag`)를 새로 만들지 않는다. 이미 있는 시드를 참조만 한다.
- `question_tag`를 건드리지 않는다. 태그 부여는 문항 시드의 책임이다.
