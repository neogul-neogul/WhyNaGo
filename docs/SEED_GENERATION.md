# 객관식 문제 시드 생성 가이드

> 이 문서는 **새 컨텍스트에서 이 문서만 읽고 바로 객관식 문제 시드 SQL을 생성·검증**할 수 있도록, 지금까지의 결정·규칙·절차를 정리한 핸드오프 문서다. 실제 "생성 규칙 프롬프트"는 [`docs/SCRIPT.md`](./SCRIPT.md)에 있고, 도메인 정의는 [`docs/DOMAIN.md`](./DOMAIN.md)에 있다. 이 문서는 그 둘을 어떻게 굴려서 시드를 만들고 검증했는지에 대한 절차·맥락이다.

---

## 0. 한눈에 요약

- **만드는 것**: WhyNaGo 객관식 문제 시드 데이터(SQL INSERT문).
- **생성 규칙 원본**: `docs/SCRIPT.md` (LLM에게 주는 프롬프트). 생성 시 이 규칙을 그대로 따른다.
- **현재 결과물**: `src/main/resources/data2.sql` — 7개 카테고리 × 25문항 = **175문항, 700보기** + 테스트 유저 1명.
- **카테고리(enum 7종)**: `DB` · `NETWORK` · `ALGORITHM` · `DATA_STRUCTURE` · `OS` · `DESIGN_PATTERN` · `LANGUAGE`.

---

## 1. 도메인 핵심 (반드시 이해할 것)

### 테이블 3개 (컬럼 순서 그대로, `id`는 AUTO_INCREMENT라 INSERT에 명시하지 않음)
- `question(id, title, content, type, difficulty, category, explanation)`
- `answer_choice(id, question_id, content, sequence, is_correct, explanation, related_question_id)`
- `question_tag(id, question_id, name)`

### enum 값
- `type`: `MULTIPLE_CHOICE` | `ESSAY` (시드는 항상 `MULTIPLE_CHOICE`)
- `difficulty`: `LOW`(하) | `MEDIUM`(중) | `HIGH`(상)
- `category`: 위 7종

### "본질문 / 꼬리질문" 구분이 없다 (가장 중요한 개념)
- 객관식 `Question`에는 `isRoot` 같은 플래그가 **없다**. 생성되는 **모든 `question` 행은 동등한 독립 문항**이며, 전부 문제은행 목록에 노출·검색·직접 선택된다.
- "본질문"·"꼬리질문"은 **한 풀이 세션 안에서 문항이 등장하는 순서**를 가리키는 표현일 뿐이다. `answer_choice.related_question_id`는 "이 보기를 고르면 이어서 등장하는 다음 문항"으로의 링크이고, 그 대상 문항 역시 다른 문항과 동등한 독립 문제다.
- 따라서 목록 조회 시 참조 여부로 문항을 걸러내지 않는다. (백엔드 `QuestionRepository.findQuestions`가 조건에 맞는 모든 Question을 유형·본질문/꼬리질문 구분 없이 반환한다. 과거 `findRootMultipleChoices`가 "참조된 문항 제외" 조건을 갖고 있었으나 제거됨.)

### 풀이 세션 깊이 (프런트 정책 — 시드 구조와 직접 관련은 없지만 맥락)
- 한 풀이 세션은 **최대 3문항(본질문 + 꼬리질문 2개)**까지만 진행한다. 프런트(`MultipleChoiceQuiz.tsx`, `MAX_QUESTIONS = 3`)가 3문항째 채점 후 `nextQuestion`이 있어도 무시하고 세션을 종료 → `POST /api/solved-sessions` 저장 단계로 넘어간다.

---

## 2. SQL 생성 규칙 (SCRIPT.md 요약 — 반드시 지킬 것)

카테고리 1개당 **5개 그룹**을 만든다. 각 그룹 = 본질문(main) 1개 + 그 보기 4개에 각각 연결되는 꼬리질문 4개 = **5문항**. → 카테고리당 **25문항**.

1. **세션 변수로 id 캡처**: `id`는 AUTO_INCREMENT. `INSERT INTO question ...;`은 **한 행씩** 실행하고 **직후에 반드시** `SET @변수 = LAST_INSERT_ID();`. (⚠️ H2 MySQL 호환모드에서 `:=` 는 문법 오류 — 반드시 `=` 를 쓴다.)
   - 변수명: 그룹 N의 본질문 = `@qN`; 그 본질문의 보기 순서 K(1~4)에 연결되는 꼬리질문 = `@qN_K`.
2. **순환 연결로 NULL 제거**: 본질문의 보기 K는 `related_question_id = @qN_K`. 각 꼬리질문의 보기 4개는 **전부 `related_question_id = @qN`**(부모로 되돌아가는 순환). → 모든 `answer_choice.related_question_id`가 **NULL이 절대 없다**. (이렇게 해야 새 문항을 무한히 만들지 않으면서 NULL을 없앨 수 있음)
3. **그룹 내 실행 순서**: 본질문 INSERT+`SET @qN` → 꼬리질문 4개 INSERT+`SET @qN_1..@qN_4` → 본질문의 answer_choice 4행 → 꼬리질문 4개 각각의 answer_choice 4행 → 태그. (보기를 넣는 시점에 필요한 변수가 모두 준비돼 있어야 함)
4. **정답/해설**: 문항당 정답(`is_correct = TRUE`) **정확히 1개**, 정답 보기의 `explanation`은 `''`(빈 문자열). 오답 3개는 `FALSE` + 각각 "왜 틀렸나" 상세 서술.
5. **정답 위치 무작위**(중요): 정답 보기의 `sequence`를 문항마다 1·2·3·4로 **고르게 분산**. 1번에만 몰리면 안 됨. (choice→꼬리질문 연결은 정답 여부가 아니라 순서 번호로만 정해지므로 정답 위치를 옮겨도 구조는 안 깨짐.)
6. **품질**: 보기는 단답형이 아닌 **서술형**(SCRIPT.md 예시 톤/길이). 문항당 태그 1~2개. 한 카테고리의 본질문 5개는 세부 주제가 서로 겹치지 않게. 난이도는 신입 백엔드 수준.
7. **출력**: 순수 SQL만. 마크다운 코드펜스(```)·머리말·설명 금지. 그룹 구분 주석 정도만 허용. `users` INSERT는 시드 본문에 넣지 않음(테스트 유저는 파일 헤더에서 한 번만).

작은따옴표가 들어가는 내용(예: 자바 코드 `it's`)은 `''`로 이스케이프하거나 아포스트로피를 피한다.

---

## 3. 생성 워크플로 (권장 절차)

대량(카테고리 7개 × 25 = 175문항)은 **카테고리별 병렬 서브에이전트**로 만드는 게 효율적이었다.

1. `docs/SCRIPT.md`(+ `data2.sql` 톤 참고)를 읽는다.
2. 카테고리마다 `general-purpose` 서브에이전트 1개를 띄운다(한 메시지에서 동시에). 각 에이전트에 **§2 규칙 전체 + 그 카테고리의 세부주제 힌트**를 embed하고, 결과 SQL을 **세션 스크래치패드**의 `gen_<ENUM>.sql`로 Write하게 한다. (반환 메시지로 받지 말고 파일로 저장 → 트렁케이션·재구성 방지)
3. 각 파일을 grep으로 구조 검증: `INSERT INTO question ` 25개, `^SET @` 25개, `NULL)` 참조 0개, 정답 `sequence` 분포.
4. 결합: **헤더 주석 + 테스트 유저 INSERT 1회** + 7개 카테고리 순서대로 이어붙인다.
5. **실제 스키마에 넣어 검증**(§4).
6. 검증 통과 시 `src/main/resources/data2.sql`에 반영. 스크래치 파일·임시 테스트는 삭제.

> ⚠️ 세션 한도/네트워크 오류로 에이전트가 "failed"로 죽어도, Write가 이미 끝났으면 파일은 온전히 남아 있다. 누락된 카테고리만 `SendMessage`로 그 에이전트를 재개하거나 다시 생성하면 된다. (실제로 이번에 DESIGN_PATTERN 1개가 연결 끊김으로 누락돼 재개로 채웠다.)

세부주제 배분(이번에 쓴 예 — 재생성 시 중복 피하려면 참고):
- **DB**: 인덱스 · 트랜잭션 격리수준/이상현상 · 정규화·반정규화 · 락 · 조인
- **NETWORK**: HTTP 상태코드 · DNS · HTTPS/TLS · OSI 7계층 · 쿠키/세션/토큰
- **ALGORITHM**: Big-O · 정렬(퀵/병합/힙) · 이진탐색 · DP · DFS/BFS
- **DATA_STRUCTURE**: 힙(우선순위 큐) · 트리 순회 · 트라이 · 유니온-파인드 · B-트리/B+트리
- **OS**: 프로세스 vs 스레드 · CPU 스케줄링 · 동기화(뮤텍스/세마포어) · 데드락 · 가상메모리/페이징
- **DESIGN_PATTERN**: 싱글톤 · 전략 · 옵저버 · 팩토리 메서드 · SOLID
- **LANGUAGE(Java)**: JVM/클래스로더 · GC · equals/hashCode · 예외처리 · 오버로딩 vs 오버라이딩

---

## 4. 검증 방법 (실제 DB에 넣어 확인 — 반드시 수행)

Hibernate가 엔티티로 만든 실제 스키마(H2 MySQL 호환모드)에 SQL을 실제로 넣어 본다.

1. 결합한 SQL을 `src/test/resources/scratch-XXX.sql`로 복사한다.
2. `IntegrationTestSupport`(`@SpringBootTest` + `@Transactional`)를 상속한 **임시 테스트**를 만들어 `ScriptUtils.executeSqlScript`로 삽입하고 `JdbcTemplate`으로 아래를 assert:
   - `question` / `answer_choice` 개수 (예: 175 / 700)
   - `related_question_id IS NULL` → **0**
   - 문항당 정답 개수 MAX·MIN 모두 **1**
   - 끊어진 FK 0 (`NOT EXISTS (SELECT 1 FROM question q WHERE q.id = ac.related_question_id)`)
   - 정답 `sequence` 분포가 1~4에 고르게 (각 최소치 이상)
   - `questionRepository.findQuestions(MULTIPLE_CHOICE, null, 카테고리, null)`이 카테고리별 25개 전부 반환
   - 테스트 유저 비밀번호 매칭: `passwordEncoder.matches("test", 저장된해시)` → true
3. 통과하면 **임시 테스트 클래스와 scratch 리소스를 삭제**한다(커밋 대상 아님).

검증 테스트 핵심 스니펫:
```java
class SeedValidationTest extends IntegrationTestSupport {
    @Autowired DataSource dataSource;
    @Autowired JdbcTemplate jdbc;
    @Autowired QuestionRepository questionRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void 시드검증() throws Exception {
        try (var c = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(c, new ClassPathResource("scratch-XXX.sql"));
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM answer_choice WHERE related_question_id IS NULL", Integer.class)).isZero();
        // ... 나머지 assert
    }
}
```

이번 최종 검증 결과: 175문항·700보기, NULL 0, 문항당 정답 1개, 끊어진 FK 0, 카테고리별 25개 노출, 정답 위치 분포 **1번 42 / 2번 46 / 3번 45 / 4번 42**, 테스트 유저 매칭 OK.

---

## 5. 관련 백엔드 사실 & 로컬 실행

- **목록 조회**: `GET /api/questions?type=&difficulty=&category=&q=` → `QuestionRepository.findQuestions` (조건에 맞는 모든 Question, 유형·본질문/꼬리질문 구분 없이 전부 반환, 참조 여부로 제외하지 않음).
- **채점**: `GET /api/questions/{questionId}/choices/{choiceId}` → `ChoiceGradingResponse{correct, correctChoiceId, explanation, choiceExplanation, nextQuestion}`. `nextQuestion`은 고른 보기의 `related_question_id`가 가리키는 문항의 전체 `QuestionResponse`(보기 포함), 없으면 null.
- **세션 저장**: `POST /api/solved-sessions`.
- **로컬 서버 실행**:
  - 프로파일 `local`, `application-local.yml`이 `spring.sql.init.data-locations: classpath:data.sql,classpath:data2.sql`로 두 시드를 모두 로드.
  - `JWT_SECRET`, `API_KEY` 환경변수가 없으면 부팅 실패 → 로컬은 더미 값으로 export 가능(커밋 금지). 예:
    ```bash
    export JWT_SECRET="local-dev-secret..."; export API_KEY="dummy"
    ./gradlew bootRun --args='--spring.profiles.active=local'
    ```
  - 8080이 점유돼 있으면 `--server.port=8081` 등으로 회피(다른 프로세스는 건드리지 않음).
- **테스트 유저**: `test@test.test` / `test` (BCrypt strength 10 해시가 이미 `data2.sql` 헤더에 포함). 로그인 → 토큰 발급 확인됨.

---

## 6. 파일 위치 요약

| 무엇 | 경로 |
| --- | --- |
| 생성 규칙(프롬프트) | `docs/SCRIPT.md` |
| 도메인 정의 | `docs/DOMAIN.md` |
| 이 가이드 | `docs/SEED_GENERATION.md` |
| 시드 결과물 | `src/main/resources/data2.sql` |
| 기존 시드(참고 톤) | `src/main/resources/data.sql` |
| 목록 조회 쿼리 | `src/main/java/com/neogul/whynago/question/infra/QuestionRepository.java` |
| 카테고리별 생성 임시 산출물 | 세션 스크래치패드의 `gen_<ENUM>.sql` (임시, 커밋 안 함) |