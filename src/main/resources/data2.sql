-- 로컬(local) 프로파일 참고용 추가 시드: 7개 카테고리 × (객관식 25문항 + 서술형 25문항) = 총 350개 문항.
-- 객관식(MULTIPLE_CHOICE, 175개): docs/SCRIPT.md 규칙으로 생성. "본질문"·"꼬리질문"은 생성 순서를 가리키는 표현일 뿐,
-- 도메인/DB에는 엔티티 차원의 구분이 없다 — 모든 question 행이 동등한 독립 문항이며 문제은행에 노출된다.
-- 각 카테고리는 5개 그룹(각 1개 + 그 보기 4개에 연결된 문항 4개)으로 구성되며,
-- 모든 문항의 보기 4개가 전부 다른 문항으로 연결된다(NULL 없음).
-- 그룹당 꼬리 4개(@qN_1~@qN_4)는 부모 문항(@qN)으로 되돌아가는 순환 연결이다.
-- 정답 보기의 위치(sequence)는 문항마다 1~4로 무작위 분산되어 있다.
-- id는 AUTO_INCREMENT이며, 문항 간 연결은 세션 변수(@qN, @qN_1~@qN_4)로 캡처한 실제 id를 사용한다.
--
-- 서술형(ESSAY, 175개): docs/SCRIPT_ESSAY.md 규칙으로 생성. 서술형은 본 질문만 저장하며
-- (꼬리질문은 세션마다 AI가 동적 생성), answer_choice·related_question_id가 없다.
-- id는 AUTO_INCREMENT이며 세션 변수(@e1~@e175)로 캡처한 실제 id를 question_tag에 사용한다.
--
-- data.sql과 별도 파일이라 기본 설정으로는 자동 로드되지 않는다.
-- 실제 적용하려면 data.sql에 이어붙이거나 data-locations에 classpath:data2.sql을 추가한다.

-- 테스트 유저 (email: test@test.test / password: test)
-- 비밀번호는 BCryptPasswordEncoder(기본 strength 10)로 해시. 평문 test와 매칭됨.
# INSERT INTO users (email, password, nickname, position, daily_goal) VALUES
# ('test@test.test', '$2a$10$QnOWMKP6UpzZHYzphdiuaOyg.Ei2ihHclJ1r5YmU0WYsvxQxSi/8q', 'test', 'BACKEND', 10);


-- ============================================================
-- 카테고리: DB (25문항)
-- ============================================================
-- DB 그룹 1: 인덱스(B-Tree / 클러스터드)
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인덱스가 조회 성능을 높이는 원리', '관계형 데이터베이스에서 인덱스(Index)가 조회 성능을 높이는 핵심 원리로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '인덱스는 컬럼 값을 정렬한 별도의 자료구조에 실제 행의 위치(포인터)를 함께 저장하여, 전체 테이블을 처음부터 훑는 풀 스캔 없이 원하는 행으로 바로 찾아갈 수 있게 한다. 대신 저장 공간과 쓰기 비용이 늘어나는 트레이드오프가 있다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B-Tree 인덱스가 범위 조회에 강한 이유', '대부분의 관계형 데이터베이스가 인덱스 구조로 B-Tree(또는 B+Tree)를 채택하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'B-Tree 계열 인덱스는 키를 정렬된 상태로 유지하는 균형 트리이므로, 특정 값 탐색뿐 아니라 범위 조회와 정렬(ORDER BY)에도 효율적이며, 한 노드에 여러 키를 담아 트리 높이를 낮게 유지하므로 적은 디스크 접근으로 탐색할 수 있다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('클러스터드 인덱스와 논클러스터드 인덱스의 차이', '클러스터드 인덱스(Clustered Index)와 논클러스터드(보조) 인덱스의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '클러스터드 인덱스는 인덱스의 리프 노드가 실제 행 데이터 자체를 정렬해 보관하므로 테이블당 하나만 존재할 수 있다. 논클러스터드 인덱스는 리프에 실제 데이터 대신 행을 찾아가기 위한 키를 담아 별도로 존재하며 여러 개 둘 수 있다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인덱스가 쓰기 성능에 주는 영향', '인덱스를 많이 생성했을 때 쓰기(INSERT/UPDATE/DELETE) 성능에 나타나는 일반적인 영향으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '데이터가 변경될 때마다 관련된 모든 인덱스 구조도 함께 갱신·재정렬해야 하므로, 인덱스가 많을수록 쓰기 성능은 저하되고 저장 공간도 더 든다. 그래서 꼭 필요한 컬럼에만 인덱스를 두는 것이 좋다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵티마이저가 인덱스를 사용하지 않는 경우', '인덱스가 존재하는데도 옵티마이저가 인덱스를 타지 않고 풀 테이블 스캔을 선택할 수 있는 대표적인 상황으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '조회 결과가 테이블의 상당 비율을 차지하거나 컬럼의 카디널리티(값의 다양성)가 매우 낮으면, 인덱스를 통해 행을 하나씩 찾아가는 비용이 오히려 풀 스캔보다 커질 수 있어 옵티마이저가 풀 스캔을 택한다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 다른 문항으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '인덱스는 데이터를 해시 테이블로만 저장하므로 등호(=) 조회는 물론 범위 조회(BETWEEN, 부등호)도 항상 O(1)에 처리하기 때문이다.', 1, FALSE, '대부분의 관계형 DB 기본 인덱스는 해시가 아니라 B-Tree 계열이며, 해시 인덱스는 등호 조회에만 강하고 정렬을 보존하지 않아 범위 조회에는 부적합하다.', @q1_1),
(@q1, '컬럼 값을 정렬한 별도의 자료구조에 실제 행의 위치(포인터)를 함께 보관하여, 전체 테이블을 순차적으로 훑지 않고도 원하는 행으로 바로 찾아갈 수 있기 때문이다.', 2, TRUE, '', @q1_2),
(@q1, '인덱스를 생성하면 조회뿐 아니라 INSERT, UPDATE, DELETE 같은 쓰기 연산의 성능도 항상 함께 빨라지기 때문이다.', 3, FALSE, '인덱스는 쓰기 시마다 인덱스 구조도 함께 갱신해야 하므로 오히려 쓰기 성능은 저하되는 것이 일반적이다. 조회 성능 향상과 쓰기 비용 증가는 트레이드오프 관계다.', @q1_3),
(@q1, '인덱스는 모든 컬럼에 자동으로 생성되어 어떤 조건의 쿼리든 예외 없이 인덱스를 타게 만들기 때문이다.', 4, FALSE, '인덱스는 필요한 컬럼에 명시적으로 생성해야 하며, 카디널리티가 낮거나 옵티마이저 판단상 풀 스캔이 유리한 경우 등에는 인덱스를 타지 않을 수 있다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, 'B-Tree는 데이터를 삽입 순서 그대로 저장하므로 정렬 비용이 전혀 들지 않기 때문이다.', 1, FALSE, 'B-Tree는 삽입 순서가 아니라 키 값 순서로 정렬 상태를 유지하며, 이를 위해 삽입 시 노드 분할·재배치 비용이 든다.', @q1),
(@q1_1, 'B-Tree는 키를 해시하여 저장하므로 범위 조회를 O(1)에 처리하기 때문이다.', 2, FALSE, '해시 방식은 값의 순서를 보존하지 않아 범위 조회에 부적합하다. B-Tree는 해시가 아니라 정렬 순서를 유지하는 트리 구조다.', @q1),
(@q1_1, '키를 정렬된 상태로 유지하는 낮은 높이의 균형 트리라서, 특정 값 탐색은 물론 범위 조회와 정렬에도 적은 디스크 접근으로 대응할 수 있기 때문이다.', 3, TRUE, '', @q1),
(@q1_1, 'B-Tree는 트리 높이가 데이터 양에 정비례해 커지므로 대용량에서 가장 빠르기 때문이다.', 4, FALSE, 'B-Tree는 한 노드가 여러 키를 담아 높이를 낮게 유지한다. 높이가 데이터 양에 정비례한다는 설명은 틀렸다.', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '클러스터드 인덱스는 리프에 실제 행 데이터를 정렬해 보관해 테이블당 하나만 둘 수 있고, 논클러스터드 인덱스는 실제 데이터로 찾아가는 키만 담아 여러 개 둘 수 있다.', 1, TRUE, '', @q1),
(@q1_2, '두 인덱스 모두 리프에 실제 행 데이터를 정렬해 보관하므로 한 테이블에 여러 개의 클러스터드 인덱스를 둘 수 있다.', 2, FALSE, '실제 데이터를 정렬 보관하는 것은 클러스터드 인덱스뿐이며, 그래서 테이블당 하나만 가능하다.', @q1),
(@q1_2, '논클러스터드 인덱스가 실제 데이터를 정렬해 보관하고, 클러스터드 인덱스는 포인터만 갖는다.', 3, FALSE, '설명이 반대다. 실제 데이터를 정렬 보관하는 쪽이 클러스터드 인덱스이고, 논클러스터드가 찾아갈 키를 갖는다.', @q1),
(@q1_2, '클러스터드 인덱스는 조회 성능과 무관하며 오로지 저장 공간을 줄이기 위한 압축 기법이다.', 4, FALSE, '클러스터드 인덱스는 압축 기법이 아니라 데이터 정렬 저장 방식으로, 범위 조회 등에서 조회 성능에 직접 영향을 준다.', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '인덱스는 조회 전용 구조라 쓰기 연산에는 아무 영향을 주지 않는다.', 1, FALSE, '쓰기 시 인덱스도 함께 갱신되므로 영향이 없다는 설명은 틀렸다.', @q1),
(@q1_3, '인덱스가 많을수록 쓰기 연산도 항상 더 빨라진다.', 2, FALSE, '인덱스 갱신 비용 때문에 오히려 느려지는 것이 일반적이다.', @q1),
(@q1_3, '인덱스는 UPDATE에만 영향을 주고 INSERT와 DELETE에는 영향을 주지 않는다.', 3, FALSE, 'INSERT는 인덱스에 새 항목 추가, DELETE는 항목 제거가 필요하므로 세 연산 모두 영향을 받는다.', @q1),
(@q1_3, '데이터 변경 시 관련 인덱스도 함께 갱신해야 하므로 인덱스가 많을수록 쓰기 성능은 저하되고 저장 공간도 더 든다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '인덱스가 존재하면 옵티마이저는 어떤 경우에도 반드시 인덱스를 사용한다.', 1, FALSE, '옵티마이저는 비용을 추정해 더 효율적인 방식을 택하므로 인덱스를 건너뛸 수 있다.', @q1),
(@q1_4, '조회 대상이 테이블의 큰 비율을 차지하거나 컬럼의 카디널리티가 매우 낮아, 인덱스로 하나씩 찾아가는 비용이 풀 스캔보다 커질 때다.', 2, TRUE, '', @q1),
(@q1_4, '인덱스가 걸린 컬럼을 WHERE 조건에 그대로 사용할 때만 인덱스를 건너뛴다.', 3, FALSE, '인덱스 컬럼을 조건에 그대로 쓰는 것은 오히려 인덱스를 잘 타는 경우이며, 컬럼을 함수로 가공하면 인덱스를 못 타게 된다.', @q1),
(@q1_4, '인덱스가 존재하면 데이터 양과 무관하게 풀 스캔이 항상 더 빠르다.', 4, FALSE, '데이터가 많고 선택도가 높은 조회에서는 인덱스가 훨씬 빠르므로 항상 풀 스캔이 빠르다는 설명은 틀렸다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, '인덱스'), (@q1, '조회 성능'),
(@q1_1, 'B-Tree'), (@q1_1, '범위 조회'),
(@q1_2, '클러스터드 인덱스'),
(@q1_3, '인덱스 쓰기 비용'),
(@q1_4, '카디널리티'), (@q1_4, '옵티마이저');


-- DB 그룹 2: 트랜잭션 격리 수준·이상 현상
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트랜잭션 격리 수준과 동시성 트레이드오프', '트랜잭션 격리 수준(Isolation Level)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '격리 수준이 높아질수록 Dirty Read, Non-Repeatable Read, Phantom Read 같은 이상 현상은 줄어들지만, 그만큼 락 등으로 동시성이 낮아진다. READ UNCOMMITTED < READ COMMITTED < REPEATABLE READ < SERIALIZABLE 순으로 엄격해진다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Dirty Read(오손 읽기)의 정의', 'Dirty Read(오손 읽기) 현상에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'Dirty Read는 한 트랜잭션이 아직 커밋되지 않은 다른 트랜잭션의 변경 데이터를 읽는 현상이다. 그 변경이 롤백되면 존재하지 않는 값을 읽은 셈이 되며, READ COMMITTED 이상 격리 수준에서 방지된다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Non-Repeatable Read(반복 불가능 읽기)', '한 트랜잭션 안에서 같은 행을 두 번 조회했는데 그 사이 다른 트랜잭션의 커밋으로 값이 달라지는 현상은 무엇이며 어떻게 방지하는가?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '이 현상은 Non-Repeatable Read로, 같은 데이터를 반복 조회할 때 값이 일관되지 않게 보이는 문제다. REPEATABLE READ 이상 격리 수준에서 방지된다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Phantom Read(팬텀 리드)의 원인', 'Phantom Read(팬텀 리드)가 발생하는 상황과 방지 방법으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 'Phantom Read는 한 트랜잭션이 같은 검색 조건으로 여러 번 조회할 때, 다른 트랜잭션이 조건에 맞는 행을 삽입/삭제해 결과 집합의 행 개수가 달라지는 현상이다. SERIALIZABLE(또는 갭 락을 사용하는 REPEATABLE READ)에서 방지된다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('MySQL InnoDB의 기본 격리 수준', 'MySQL InnoDB의 기본 트랜잭션 격리 수준과 그 특징으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'MySQL InnoDB의 기본 격리 수준은 REPEATABLE READ이며, MVCC와 갭 락을 활용해 Non-Repeatable Read는 물론 상당 부분의 Phantom Read까지 억제한다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 다른 문항으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, 'READ UNCOMMITTED에서는 커밋되지 않은 데이터를 읽는 일이 절대 발생하지 않는다.', 1, FALSE, 'READ UNCOMMITTED는 가장 낮은 격리 수준으로, 커밋되지 않은 변경을 읽는 Dirty Read가 발생할 수 있다.', @q2_1),
(@q2, '한 트랜잭션이 같은 행을 두 번 읽을 때 값이 달라지는 현상은 어떤 격리 수준에서도 발생하지 않는다.', 2, FALSE, '이는 Non-Repeatable Read로, READ COMMITTED 이하에서 발생할 수 있고 REPEATABLE READ 이상에서 방지된다.', @q2_2),
(@q2, '새로운 행이 삽입되어 같은 조건의 재조회 결과 건수가 달라지는 현상은 격리 수준과 무관하게 항상 막을 수 없다.', 3, FALSE, '이는 Phantom Read로, SERIALIZABLE 또는 갭 락을 쓰는 REPEATABLE READ에서 방지할 수 있으므로 항상 막을 수 없다는 설명은 틀렸다.', @q2_3),
(@q2, '격리 수준이 높아질수록 이상 현상은 줄지만 동시성이 낮아지며, SERIALIZABLE이 가장 엄격한 수준이다.', 4, TRUE, '', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '아직 커밋되지 않은 다른 트랜잭션의 변경 데이터를 읽어, 그 변경이 롤백되면 실제로 존재하지 않는 값을 읽게 되는 현상이다.', 1, TRUE, '', @q2),
(@q2_1, '같은 행을 두 번 읽을 때 다른 트랜잭션의 커밋으로 값이 달라지는 현상이다.', 2, FALSE, '이는 Non-Repeatable Read에 대한 설명이다. Dirty Read는 커밋되지 않은 값을 읽는 것이다.', @q2),
(@q2_1, '같은 조건으로 재조회할 때 결과에 포함되는 행의 개수가 달라지는 현상이다.', 3, FALSE, '이는 Phantom Read에 대한 설명이며 Dirty Read와 다르다.', @q2),
(@q2_1, '트랜잭션이 커밋된 데이터만 읽어 무결성이 보장되는 정상 상태를 뜻한다.', 4, FALSE, 'Dirty Read는 정상 상태가 아니라 방지해야 할 이상 현상이다.', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, 'Phantom Read이며, READ UNCOMMITTED에서만 방지된다.', 1, FALSE, '같은 행의 값이 달라지는 것은 Non-Repeatable Read이며, READ UNCOMMITTED는 가장 약한 수준이라 오히려 이상 현상이 더 많이 발생한다.', @q2),
(@q2_2, 'Dirty Read이며 어떤 격리 수준으로도 막을 수 없다.', 2, FALSE, 'Dirty Read는 커밋 안 된 값을 읽는 별개 현상이고, 이상 현상들은 격리 수준을 높여 방지할 수 있다.', @q2),
(@q2_2, 'Non-Repeatable Read이며, REPEATABLE READ 이상 격리 수준에서 방지된다.', 3, TRUE, '', @q2),
(@q2_2, '정상적인 동작이므로 별도의 방지 수단이 필요 없다.', 4, FALSE, '반복 조회 결과가 달라지는 것은 방지 대상 이상 현상이며 정상 동작이 아니다.', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '단일 행의 값이 재조회 시 바뀌는 현상으로, READ COMMITTED로 완전히 방지된다.', 1, FALSE, '단일 행 값이 바뀌는 것은 Non-Repeatable Read이며, Phantom Read는 조건에 맞는 행 개수가 달라지는 현상이다.', @q2),
(@q2_3, '같은 조건으로 재조회할 때 다른 트랜잭션의 삽입/삭제로 결과 행 개수가 달라지는 현상으로, SERIALIZABLE 등에서 방지된다.', 2, TRUE, '', @q2),
(@q2_3, '커밋되지 않은 데이터를 읽는 현상으로, 격리 수준과 무관하게 발생한다.', 3, FALSE, '커밋되지 않은 데이터를 읽는 것은 Dirty Read이고, 격리 수준을 높이면 방지할 수 있다.', @q2),
(@q2_3, '트랜잭션이 스스로 삽입한 행을 다시 읽는 정상 동작을 가리킨다.', 4, FALSE, '자신이 삽입한 행을 읽는 것은 정상이며, Phantom Read는 다른 트랜잭션의 변경으로 결과가 달라지는 이상 현상이다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, '기본 격리 수준은 READ UNCOMMITTED이며 이상 현상을 전혀 막지 못한다.', 1, FALSE, 'InnoDB 기본값은 READ UNCOMMITTED가 아니라 REPEATABLE READ다.', @q2),
(@q2_4, '기본 격리 수준은 SERIALIZABLE이며 모든 조회에 항상 테이블 전체 락을 건다.', 2, FALSE, '기본값은 SERIALIZABLE이 아니며, SERIALIZABLE도 모든 조회에 테이블 전체 락을 항상 거는 방식은 아니다.', @q2),
(@q2_4, '기본 격리 수준은 READ COMMITTED이며 Non-Repeatable Read를 완전히 방지한다.', 3, FALSE, 'InnoDB 기본값은 REPEATABLE READ이고, READ COMMITTED는 Non-Repeatable Read를 방지하지 못한다.', @q2),
(@q2_4, '기본 격리 수준은 REPEATABLE READ이며, MVCC와 갭 락으로 Non-Repeatable Read와 상당 부분의 Phantom Read를 억제한다.', 4, TRUE, '', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, '트랜잭션 격리수준'), (@q2, '이상 현상'),
(@q2_1, 'Dirty Read'),
(@q2_2, 'Non-Repeatable Read'),
(@q2_3, '팬텀 리드'),
(@q2_4, 'REPEATABLE READ'), (@q2_4, 'MVCC');


-- DB 그룹 3: 정규화 / 반정규화
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('정규화의 목적', '관계형 데이터베이스에서 정규화(Normalization)를 수행하는 주된 목적으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '정규화는 데이터를 관련 있는 속성끼리 분리해 중복을 제거함으로써 삽입/갱신/삭제 이상(anomaly)을 방지하고 데이터 무결성을 높이는 설계 기법이다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('제1정규형(1NF)의 조건', '제1정규형(1NF)을 만족하기 위한 조건으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '제1정규형은 모든 속성의 값이 더 이상 나눌 수 없는 원자값(atomic)이어야 한다는 조건이다. 한 칸에 여러 값을 콤마로 나열하는 등의 형태는 1NF 위반이다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('제2정규형과 부분 함수 종속', '제2정규형(2NF)에서 제거하려는 부분 함수 종속(Partial Dependency)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '부분 함수 종속은 기본키가 복합키일 때, 비-기본키 속성이 복합키 전체가 아니라 그 일부에만 종속되는 상태를 말한다. 2NF는 이를 제거해 각 속성이 기본키 전체에 완전 종속되도록 만든다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('제3정규형과 이행적 종속', '제3정규형(3NF)에서 제거하려는 이행적 함수 종속(Transitive Dependency)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '이행적 종속은 A→B, B→C가 성립해 결과적으로 A→C가 되는 관계로, 비-기본키 속성이 다른 비-기본키 속성을 통해 기본키에 간접 종속되는 상태다. 3NF는 이를 분리해 제거한다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('반정규화를 적용하는 이유', '정규화된 스키마에 반정규화(Denormalization)를 의도적으로 적용하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '반정규화는 잦은 조인으로 인한 조회 성능 저하를 완화하기 위해 일부 데이터를 의도적으로 중복 저장하는 기법이다. 대신 데이터 중복으로 인한 갱신 이상 위험이 커지므로 트레이드오프를 고려해야 한다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 다른 문항으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, '데이터 중복을 제거하여 삽입/갱신/삭제 이상을 방지하고 데이터 무결성을 높이기 위함이다.', 1, TRUE, '', @q3_1),
(@q3, '조인을 최대한 많이 사용하도록 만들어 항상 조회 성능을 높이기 위함이다.', 2, FALSE, '정규화는 테이블이 나뉘어 오히려 조인이 늘 수 있으며, 목적은 조회 성능 향상이 아니라 중복 제거와 무결성 확보다.', @q3_2),
(@q3, '모든 컬럼을 하나의 테이블에 몰아넣어 저장 공간을 늘리기 위함이다.', 3, FALSE, '정규화는 관련 속성끼리 테이블을 분리하는 것이며, 한 테이블에 모든 컬럼을 몰아넣는 것은 정규화의 반대 방향이다.', @q3_3),
(@q3, '인덱스를 자동으로 생성해 조회 속도를 보장하기 위함이다.', 4, FALSE, '정규화는 스키마 설계 개념으로, 인덱스 생성과는 별개의 문제다.', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '기본키가 아닌 모든 속성이 기본키 전체에 완전 함수 종속되어야 한다.', 1, FALSE, '이는 제2정규형(2NF)의 조건이며 1NF의 조건이 아니다.', @q3),
(@q3_1, '기본키가 아닌 속성 간의 이행적 종속이 없어야 한다.', 2, FALSE, '이는 제3정규형(3NF)의 조건이며 1NF의 조건이 아니다.', @q3),
(@q3_1, '모든 속성의 값이 더 이상 분해되지 않는 원자값이어야 한다.', 3, TRUE, '', @q3),
(@q3_1, '모든 테이블이 반드시 하나의 컬럼만 가져야 한다.', 4, FALSE, '1NF는 값의 원자성을 요구할 뿐 테이블의 컬럼 개수를 제한하지 않는다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '비-기본키 속성이 다른 비-기본키 속성에 종속되는 것을 말한다.', 1, FALSE, '비-기본키 속성 간의 종속은 이행적 종속으로 제3정규형에서 다루는 문제다.', @q3),
(@q3_2, '복합 기본키의 일부에만 비-기본키 속성이 종속되는 상태로, 2NF에서 이를 제거한다.', 2, TRUE, '', @q3),
(@q3_2, '한 칸에 여러 값이 들어가 원자값이 아닌 상태를 말한다.', 3, FALSE, '이는 제1정규형 위반에 대한 설명이며 부분 함수 종속과 다르다.', @q3),
(@q3_2, '기본키가 단일 컬럼일 때만 발생하는 종속이다.', 4, FALSE, '부분 함수 종속은 복합키의 일부에 종속될 때 생기므로 단일 컬럼 기본키에서는 발생하지 않는다.', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '한 속성이 여러 개의 원자값을 동시에 갖는 상태다.', 1, FALSE, '이는 제1정규형 위반이며 이행적 종속과 무관하다.', @q3),
(@q3_3, '복합키의 일부에만 종속되는 상태다.', 2, FALSE, '이는 부분 함수 종속으로 제2정규형에서 다루는 문제다.', @q3),
(@q3_3, '기본키가 존재하지 않는 상태를 말한다.', 3, FALSE, '이행적 종속은 기본키 유무가 아니라 속성 간 간접 종속에 관한 것이다.', @q3),
(@q3_3, '비-기본키 속성이 다른 비-기본키 속성을 거쳐 기본키에 간접적으로 종속되는(A→B→C) 상태다.', 4, TRUE, '', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '잦은 조인으로 인한 조회 성능 저하를 줄이기 위해 데이터를 의도적으로 중복 저장하는 것으로, 갱신 이상 위험이라는 트레이드오프가 있다.', 1, TRUE, '', @q3),
(@q3_4, '데이터 중복을 완전히 제거해 무결성을 더욱 높이기 위한 것이다.', 2, FALSE, '중복 제거는 정규화의 목적이며, 반정규화는 오히려 중복을 의도적으로 허용한다.', @q3),
(@q3_4, '테이블을 더 잘게 분리해 조인 수를 늘리기 위한 것이다.', 3, FALSE, '반정규화는 테이블을 합치거나 중복을 두어 조인을 줄이는 방향이다.', @q3),
(@q3_4, '어떤 상황에서도 정규화보다 항상 우월한 표준 설계 방식이다.', 4, FALSE, '반정규화는 조회 성능과 갱신 이상 사이의 트레이드오프이므로 항상 우월하지 않다.', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, '정규화'), (@q3, '이상 현상'),
(@q3_1, '제1정규형'),
(@q3_2, '제2정규형'), (@q3_2, '부분 함수 종속'),
(@q3_3, '제3정규형'), (@q3_3, '이행적 종속'),
(@q3_4, '반정규화');


-- DB 그룹 4: 락(공유/배타/데드락)
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('공유 락과 배타 락의 차이', '데이터베이스의 공유 락(Shared Lock)과 배타 락(Exclusive Lock)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 '공유 락(S)은 읽기용으로 여러 트랜잭션이 동시에 보유할 수 있지만 그 자원에 배타 락은 걸 수 없다. 배타 락(X)은 쓰기용으로 한 트랜잭션만 보유할 수 있으며 다른 어떤 락과도 호환되지 않는다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('공유 락과 배타 락의 호환성', '한 자원에 이미 공유 락(S)이 걸려 있을 때, 다른 트랜잭션의 락 요청이 처리되는 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '공유 락끼리는 호환되므로 다른 트랜잭션의 공유 락 요청은 함께 허용된다. 그러나 배타 락은 공유 락과 호환되지 않으므로, 배타 락 요청은 기존 공유 락이 모두 해제될 때까지 대기해야 한다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락의 발생 조건', '데이터베이스에서 데드락(Deadlock)이 발생하기 위한 상황으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '데드락은 둘 이상의 트랜잭션이 서로가 보유한 락을 순환적으로 기다리며 아무도 진행하지 못하는 상태다. 상호 배제, 점유와 대기, 비선점, 순환 대기 조건이 함께 성립할 때 발생한다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락에 대한 데이터베이스의 대응', '데드락이 발생했을 때 대부분의 관계형 데이터베이스가 취하는 일반적인 대응으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'DBMS는 대기 그래프 등으로 순환 대기를 감지하면, 관련 트랜잭션 중 하나를 희생자로 선택해 강제로 롤백시켜 교착을 풀고 나머지 트랜잭션이 진행되게 한다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('비관적 락과 낙관적 락의 차이', '비관적 락(Pessimistic Lock)과 낙관적 락(Optimistic Lock)의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 '비관적 락은 충돌을 가정하고 데이터 접근 시점에 미리 락을 걸어 다른 트랜잭션을 막는다. 낙관적 락은 충돌이 드물다고 보고 락 없이 진행한 뒤, 커밋 시점에 버전(version) 등을 비교해 충돌이 있으면 실패 처리한다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 다른 문항으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '공유 락은 한 트랜잭션만 보유할 수 있고, 배타 락은 여러 트랜잭션이 동시에 보유할 수 있다.', 1, FALSE, '설명이 반대다. 여러 트랜잭션이 동시에 보유할 수 있는 것이 공유 락(S), 한 트랜잭션만 단독 보유하는 것이 배타 락(X)이다.', @q4_1),
(@q4, '두 트랜잭션이 서로가 가진 락을 기다리는 상황은 배타 락을 쓰지 않으면 절대 발생하지 않는다.', 2, FALSE, '서로의 락을 순환적으로 기다리는 교착(데드락)은 락 획득 순서 등에 의해 발생하며, 상황에 따라 배타 락만이 원인은 아니다.', @q4_2),
(@q4, '공유 락은 여러 트랜잭션이 동시에 읽기용으로 보유할 수 있지만, 배타 락은 한 트랜잭션만 보유하며 다른 락과 호환되지 않는다.', 3, TRUE, '', @q4_3),
(@q4, '락은 항상 테이블 전체 단위로만 걸 수 있고 행(row) 단위 락은 존재하지 않는다.', 4, FALSE, '많은 DB가 행 단위 락을 지원하며, 락 단위는 행/페이지/테이블 등 다양하게 설정될 수 있다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '다른 트랜잭션의 공유 락 요청도 즉시 거부된다.', 1, FALSE, '공유 락끼리는 호환되어 동시에 여러 개가 함께 허용된다.', @q4),
(@q4_1, '다른 공유 락 요청은 함께 허용되지만, 배타 락 요청은 기존 공유 락이 모두 풀릴 때까지 대기한다.', 2, TRUE, '', @q4),
(@q4_1, '배타 락 요청도 공유 락과 호환되어 즉시 함께 허용된다.', 3, FALSE, '배타 락은 공유 락과 호환되지 않으므로 즉시 함께 허용되지 않는다.', @q4),
(@q4_1, '기존 공유 락이 자동으로 배타 락으로 승격되어 다른 모든 요청을 막는다.', 4, FALSE, '공유 락이 다른 요청만으로 자동 승격되지는 않으며, 락 승격은 별도의 조건에서 일어난다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '한 트랜잭션이 아무 락도 갖지 않은 채 단독으로 실행될 때 발생한다.', 1, FALSE, '데드락은 여러 트랜잭션이 서로의 락을 기다릴 때 발생하며 단독 실행에서는 생기지 않는다.', @q4),
(@q4_2, '모든 트랜잭션이 동일한 순서로만 락을 획득할 때 발생한다.', 2, FALSE, '동일한 순서로 락을 획득하면 오히려 순환 대기가 생기지 않아 데드락 예방에 도움이 된다.', @q4),
(@q4_2, '읽기 전용 트랜잭션만 동시에 실행될 때 발생한다.', 3, FALSE, '읽기 전용(공유 락)끼리는 서로 호환되어 순환 대기가 잘 생기지 않는다.', @q4),
(@q4_2, '둘 이상의 트랜잭션이 서로가 보유한 락을 순환적으로 기다려 아무도 진행하지 못할 때 발생한다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, '순환 대기를 감지해 트랜잭션 중 하나를 희생자로 골라 강제 롤백하여 교착을 해소한다.', 1, TRUE, '', @q4),
(@q4_3, '두 트랜잭션을 모두 무한정 대기시켜 사용자가 직접 종료할 때까지 둔다.', 2, FALSE, '대부분의 DBMS는 데드락을 감지해 자동으로 하나를 롤백하며 무한정 방치하지 않는다.', @q4),
(@q4_3, '데드락에 관련된 테이블을 삭제하여 문제를 해결한다.', 3, FALSE, 'DBMS가 테이블을 삭제하는 일은 없으며, 이는 데이터 손실을 초래한다.', @q4),
(@q4_3, '두 트랜잭션을 모두 커밋 처리하여 강제로 진행시킨다.', 4, FALSE, '서로 상충하는 트랜잭션을 모두 커밋하면 무결성이 깨지며, 실제로는 하나를 롤백한다.', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '비관적 락은 커밋 시점에만 버전을 비교하고, 낙관적 락은 접근 시점에 즉시 락을 건다.', 1, FALSE, '설명이 반대다. 접근 시점에 미리 락을 거는 것이 비관적 락, 커밋 시점에 버전을 비교하는 것이 낙관적 락이다.', @q4),
(@q4_4, '두 방식 모두 데이터베이스 물리 락을 반드시 사용한다.', 2, FALSE, '낙관적 락은 보통 물리 락 대신 버전 컬럼 비교 같은 애플리케이션 수준의 검증을 사용한다.', @q4),
(@q4_4, '비관적 락은 접근 시점에 미리 락을 걸어 충돌을 막고, 낙관적 락은 락 없이 진행 후 커밋 시 버전을 비교해 충돌을 감지한다.', 3, TRUE, '', @q4),
(@q4_4, '낙관적 락은 충돌이 매우 잦은 환경에서 가장 효율적이다.', 4, FALSE, '충돌이 잦으면 낙관적 락은 재시도가 빈번해 오히려 비효율적이며, 그런 환경엔 비관적 락이 유리하다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, '공유 락'), (@q4, '배타 락'),
(@q4_1, '락 호환성'),
(@q4_2, '데드락'),
(@q4_3, '데드락 해소'),
(@q4_4, '낙관적 락'), (@q4_4, '비관적 락');


-- DB 그룹 5: 조인 종류·동작
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('INNER JOIN과 OUTER JOIN의 차이', 'SQL의 INNER JOIN과 OUTER JOIN(LEFT/RIGHT)의 핵심 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'INNER JOIN은 양쪽 테이블에서 조인 조건이 모두 일치하는 행만 결과에 포함한다. OUTER JOIN은 한쪽(또는 양쪽) 테이블의 행을 조건이 맞지 않아도 모두 포함하며, 짝이 없는 쪽 컬럼은 NULL로 채운다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('INNER JOIN의 결과 집합', '두 테이블을 INNER JOIN할 때 결과 집합에 포함되는 행으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'LOW', 'DB',
 'INNER JOIN은 조인 조건을 만족해 양쪽 테이블에서 서로 짝이 맞는 행들만 결과에 포함한다. 어느 한쪽에만 존재하고 짝이 없는 행은 결과에서 제외된다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('LEFT OUTER JOIN의 활용', 'LEFT OUTER JOIN을 사용해 왼쪽 테이블에는 있지만 오른쪽 테이블에는 짝이 없는 행만 골라내려면 어떻게 해야 하는가?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 'LEFT OUTER JOIN 후 WHERE 절에서 오른쪽 테이블의 컬럼이 NULL인 행을 걸러내면, 왼쪽에만 존재하고 오른쪽에 짝이 없는 행만 얻을 수 있다(anti-join).');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Nested Loop Join의 동작 방식', '옵티마이저가 선택하는 조인 알고리즘 중 Nested Loop Join의 동작 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DB',
 'Nested Loop Join은 한쪽(외부) 테이블의 행을 하나씩 읽으며, 각 행마다 다른(내부) 테이블에서 조인 조건에 맞는 행을 찾는 방식이다. 외부 결과 집합이 작고 내부 조인 컬럼에 인덱스가 있을 때 효율적이다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('CROSS JOIN의 결과 크기', '행이 각각 M개, N개인 두 테이블을 CROSS JOIN(카테시안 곱)하면 결과 행 수로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DB',
 'CROSS JOIN은 두 테이블의 모든 행을 서로 짝지어 조합하므로, 조인 조건이 없을 때 결과는 M 곱하기 N개의 행이 된다. 조건을 빠뜨린 조인이 의도치 않게 카테시안 곱을 만드는 경우도 이 때문이다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 다른 문항으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, 'INNER JOIN은 양쪽 중 한 테이블의 모든 행을 포함하고 짝이 없으면 NULL로 채운다.', 1, FALSE, '짝이 없어도 한쪽 행을 모두 포함하고 NULL로 채우는 것은 OUTER JOIN이다. INNER JOIN은 조건이 일치하는 행만 남긴다.', @q5_1),
(@q5, 'INNER JOIN은 조인 조건이 양쪽 모두 일치하는 행만 남기고, OUTER JOIN은 짝이 없는 행도 포함해 없는 쪽을 NULL로 채운다.', 2, TRUE, '', @q5_2),
(@q5, '두 조인 모두 항상 모든 행 조합(카테시안 곱)을 만든 뒤 조건을 적용한다.', 3, FALSE, '모든 조합을 무조건 만드는 것은 CROSS JOIN이며, INNER/OUTER JOIN은 조인 조건으로 매칭되는 행을 찾는다.', @q5_3),
(@q5, 'LEFT JOIN과 RIGHT JOIN은 결과가 항상 완전히 동일하다.', 4, FALSE, 'LEFT JOIN은 왼쪽 테이블 기준, RIGHT JOIN은 오른쪽 테이블 기준으로 모두 포함하므로 기준 테이블이 달라 일반적으로 결과가 다르다.', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '왼쪽 테이블의 모든 행이 포함되고 오른쪽은 짝이 없으면 NULL이 된다.', 1, FALSE, '이는 LEFT OUTER JOIN의 동작이며 INNER JOIN이 아니다.', @q5),
(@q5_1, '양쪽 테이블의 모든 행이 조건과 무관하게 전부 포함된다.', 2, FALSE, '조건과 무관하게 모두 포함하는 것은 FULL OUTER JOIN이나 CROSS JOIN에 가깝고, INNER JOIN은 짝이 맞는 행만 남긴다.', @q5),
(@q5_1, '오른쪽 테이블의 모든 행이 포함되고 왼쪽은 NULL로 채워진다.', 3, FALSE, '이는 RIGHT OUTER JOIN의 동작이며 INNER JOIN이 아니다.', @q5),
(@q5_1, '조인 조건을 만족해 양쪽에서 서로 짝이 맞는 행들만 포함된다.', 4, TRUE, '', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, 'LEFT JOIN 후 WHERE 절에서 오른쪽 테이블의 컬럼이 NULL인 행만 남기면 된다.', 1, TRUE, '', @q5),
(@q5_2, 'INNER JOIN을 사용하면 자동으로 짝 없는 행만 남는다.', 2, FALSE, 'INNER JOIN은 짝이 맞는 행만 남기므로, 짝 없는 행은 오히려 결과에서 사라진다.', @q5),
(@q5_2, 'LEFT JOIN 후 오른쪽 컬럼이 NOT NULL인 행만 남기면 된다.', 3, FALSE, 'NOT NULL인 행은 오른쪽에 짝이 있는 행이므로, 짝 없는 행을 찾으려는 목적과 정반대다.', @q5),
(@q5_2, 'CROSS JOIN을 사용하면 짝 없는 행만 걸러진다.', 4, FALSE, 'CROSS JOIN은 모든 조합을 만들 뿐 짝 유무를 구분하지 못한다.', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '두 테이블을 각각 조인 키로 정렬한 뒤 병합하며 조인한다.', 1, FALSE, '이는 Sort Merge Join의 방식이며 Nested Loop Join이 아니다.', @q5),
(@q5_3, '한 테이블로 해시 테이블을 만든 뒤 다른 테이블을 탐색해 조인한다.', 2, FALSE, '이는 Hash Join의 방식이며 Nested Loop Join이 아니다.', @q5),
(@q5_3, '외부 테이블의 각 행마다 내부 테이블에서 조건에 맞는 행을 반복 탐색하며 조인한다.', 3, TRUE, '', @q5),
(@q5_3, '두 테이블의 모든 행 조합을 먼저 만든 뒤 조건에 맞는 것만 남긴다.', 4, FALSE, '모든 조합을 먼저 만드는 것은 비효율적인 카테시안 곱 방식이며 Nested Loop Join의 핵심 동작이 아니다.', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, 'M 더하기 N개의 행이 생성된다.', 1, FALSE, 'CROSS JOIN은 덧셈이 아니라 모든 행의 조합이므로 곱셈 크기가 된다.', @q5),
(@q5_4, 'M과 N 중 큰 값만큼의 행이 생성된다.', 2, FALSE, '두 집합의 모든 조합을 만들므로 큰 값 하나로 결정되지 않는다.', @q5),
(@q5_4, '조인 조건이 맞는 행만 남아 M과 N 중 작은 값 이하가 된다.', 3, FALSE, 'CROSS JOIN에는 조인 조건이 없어 모든 조합이 생성된다.', @q5),
(@q5_4, 'M 곱하기 N개의 행이 생성된다.', 4, TRUE, '', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, '조인'), (@q5, 'OUTER JOIN'),
(@q5_1, 'INNER JOIN'),
(@q5_2, 'LEFT JOIN'), (@q5_2, 'anti-join'),
(@q5_3, 'Nested Loop Join'), (@q5_3, '실행 계획'),
(@q5_4, 'CROSS JOIN');


-- ============================================================
-- 카테고리: NETWORK (25문항)
-- ============================================================
-- NETWORK 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HTTP 상태 코드의 분류 체계', 'HTTP 상태 코드의 첫 자리 숫자에 따른 분류로 옳은 것은?', 'MULTIPLE_CHOICE', 'LOW', 'NETWORK',
 '상태 코드의 첫 자리는 응답의 성격을 나타낸다. 1xx는 정보성 임시 응답, 2xx는 성공, 3xx는 리다이렉션, 4xx는 클라이언트 오류, 5xx는 서버 오류를 의미한다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('1xx 정보성 상태 코드의 의미', 'HTTP 1xx 상태 코드에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '1xx는 요청을 정상적으로 받았고 처리를 계속 진행 중임을 알리는 정보성 임시 응답으로, 최종 결과가 아니라 처리 과정 중간에 전달되는 잠정 응답이다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('4xx와 5xx의 책임 소재 차이', '4xx와 5xx 상태 코드의 가장 근본적인 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '4xx는 요청 자체에 문제가 있어 클라이언트 측에 원인이 있는 오류이고, 5xx는 요청은 유효했으나 서버가 처리하는 과정에서 실패한 서버 측 오류다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('3xx 리다이렉션의 의미', 'HTTP 3xx 리다이렉션 상태 코드가 의미하는 바로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '3xx는 요청한 리소스가 다른 위치로 이동했거나 요청을 완료하기 위해 클라이언트가 추가 동작(다른 URL로의 재요청 등)을 해야 함을 알리는 응답이다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('404와 500의 구체적 차이', '404 Not Found와 500 Internal Server Error의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '404는 요청한 리소스를 서버에서 찾을 수 없다는 클라이언트 오류이고, 500은 서버 내부 로직에서 예기치 못한 오류가 발생했다는 서버 오류다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '1xx는 서버 오류를, 2xx는 클라이언트 오류를 의미해 두 코드가 오류 응답을 담당한다.', 1, FALSE, '1xx는 오류가 아니라 처리 중임을 알리는 정보성 응답이고, 2xx는 오류가 아니라 성공을 의미한다. 서술이 완전히 잘못되었다.', @q1_1),
(@q1, '2xx는 성공, 3xx는 리다이렉션, 4xx는 클라이언트 오류, 5xx는 서버 오류를 의미한다.', 2, TRUE, '', @q1_2),
(@q1, '3xx는 요청이 완전히 실패해 더 이상 어떤 처리도 불가능함을 의미한다.', 3, FALSE, '3xx는 실패가 아니라 리다이렉션으로, 다른 위치로의 재요청 등 추가 동작을 하면 요청을 완료할 수 있다.', @q1_3),
(@q1, '4xx와 5xx는 구분 없이 모두 서버 측에서 발생한 오류를 의미한다.', 4, FALSE, '4xx는 클라이언트 측 원인의 오류이고 5xx가 서버 측 오류다. 둘은 책임 소재로 명확히 구분된다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모(@q1)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '요청 처리가 완전히 끝나 최종 결과를 담아 반환하는 성공 응답이다.', 1, FALSE, '최종 성공 결과는 2xx가 담당하며, 1xx는 처리가 끝나기 전에 전달되는 임시 응답이다.', @q1),
(@q1_1, '클라이언트의 잘못된 요청을 거부했음을 알리는 오류 응답이다.', 2, FALSE, '잘못된 요청 거부는 4xx의 역할이며, 1xx는 오류 응답이 아니다.', @q1),
(@q1_1, '요청을 정상적으로 받았고 처리를 계속 진행 중임을 알리는 정보성 임시 응답이다.', 3, TRUE, '', @q1),
(@q1_1, '서버가 과부하로 응답할 수 없는 상태임을 알리는 코드다.', 4, FALSE, '서버가 응답할 수 없는 상태는 5xx(예: 503)로 표현되며, 1xx와 무관하다.', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모(@q1)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '4xx는 요청 자체에 문제가 있는 클라이언트 측 오류이고, 5xx는 서버가 요청을 처리하다 실패한 서버 측 오류다.', 1, TRUE, '', @q1),
(@q1_2, '4xx는 서버 내부 오류이고 5xx는 클라이언트의 잘못된 요청 오류다.', 2, FALSE, '책임 소재가 반대로 서술되었다. 4xx가 클라이언트, 5xx가 서버 측 오류다.', @q1),
(@q1_2, '4xx와 5xx는 모두 네트워크 물리 회선의 단절만을 나타낸다.', 3, FALSE, '두 코드는 애플리케이션 계층의 처리 결과를 나타내며, 물리 회선 단절만을 의미하지 않는다.', @q1),
(@q1_2, '4xx는 성공 응답의 일종이고 5xx만 오류를 의미한다.', 4, FALSE, '4xx도 명백한 오류 응답이며 성공 응답은 2xx다.', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모(@q1)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '서버 내부에서 처리할 수 없는 치명적 오류가 발생했음을 의미한다.', 1, FALSE, '치명적 서버 오류는 5xx의 역할이며, 3xx는 오류 코드가 아니다.', @q1),
(@q1_3, '요청 본문의 형식이 잘못되었으니 다시 작성하라는 클라이언트 오류다.', 2, FALSE, '잘못된 요청 형식은 400 등 4xx가 담당하며, 3xx는 리다이렉션이다.', @q1),
(@q1_3, '요청이 성공적으로 완료되어 더 이상 추가 동작이 필요 없음을 의미한다.', 3, FALSE, '추가 동작 없이 완료된 성공은 2xx이며, 3xx는 오히려 추가 동작(재요청)을 요구한다.', @q1),
(@q1_3, '요청한 리소스가 다른 위치로 이동했거나, 요청 완료를 위해 클라이언트가 추가 동작을 해야 함을 알린다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모(@q1)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '404는 서버 코드의 예외이고 500은 URL 오타 때문에 발생한다.', 1, FALSE, '설명이 뒤바뀌었다. 404가 리소스를 찾지 못한 경우, 500이 서버 내부 예외에 해당한다.', @q1),
(@q1_4, '404는 요청한 리소스를 찾을 수 없는 클라이언트 오류이고, 500은 서버 내부에서 예기치 못한 오류가 발생한 서버 오류다.', 2, TRUE, '', @q1),
(@q1_4, '404와 500 모두 인증 실패를 나타내는 동일한 의미의 코드다.', 3, FALSE, '인증 실패는 401이며, 404와 500은 인증과 직접 관련이 없고 서로 다른 오류 범주다.', @q1),
(@q1_4, '404는 성공을 뜻하고 500은 리다이렉션을 뜻한다.', 4, FALSE, '404는 4xx 클라이언트 오류, 500은 5xx 서버 오류로 둘 다 성공·리다이렉션과 무관하다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, 'HTTP 상태 코드'), (@q1, '상태 코드 분류'),
(@q1_1, '1xx'), (@q1_1, '정보 응답'),
(@q1_2, '4xx'), (@q1_2, '5xx'),
(@q1_3, '리다이렉션'),
(@q1_4, '404'), (@q1_4, '500');


-- NETWORK 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DNS의 기본 역할', 'DNS(Domain Name System)의 가장 기본적인 역할로 옳은 것은?', 'MULTIPLE_CHOICE', 'LOW', 'NETWORK',
 'DNS는 사람이 기억하기 쉬운 도메인 이름(예: example.com)을 실제 통신에 필요한 IP 주소로 변환해 주는 분산 이름 해석 시스템이다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DNS 캐싱과 TTL', 'DNS 리졸버가 조회 결과를 캐시에 얼마나 오래 보관할지 결정하는 값으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '각 DNS 레코드에 설정된 TTL(Time To Live) 값이 그 레코드를 캐시에 보관할 수 있는 유효 기간을 결정하며, TTL이 지나면 캐시를 폐기하고 다시 조회한다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DNS 질의가 사용하는 전송 프로토콜', 'DNS 질의가 일반적으로 사용하는 전송 계층 프로토콜로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'DNS는 응답이 빠르고 오버헤드가 적어야 하므로 일반적으로 UDP 53번 포트를 사용하며, 응답 크기가 크거나 영역 전송(zone transfer)이 필요한 경우에는 TCP를 사용한다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('재귀적 DNS 이름 해석 과정', '캐시에 없는 도메인 이름을 처음 조회할 때 DNS 리졸버가 IP 주소를 알아내는 과정으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 '리졸버는 루트 네임서버 → TLD(예: .com) 네임서버 → 권한(authoritative) 네임서버 순으로 계층을 따라 질의를 이어가며 최종적으로 도메인의 IP 주소를 얻는다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DNS 레코드 종류', 'DNS 레코드 타입에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'A 레코드는 도메인을 IPv4 주소에, AAAA 레코드는 IPv6 주소에 매핑하고, CNAME 레코드는 도메인을 다른 도메인 이름(별칭)에 매핑한다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, 'DNS는 웹 페이지의 HTML 콘텐츠를 전 세계에 캐싱해 전송 속도를 높이는 시스템이다.', 1, FALSE, '콘텐츠를 지역 서버에 캐싱해 전송 속도를 높이는 것은 CDN의 역할이며, DNS는 이름을 IP로 변환하는 시스템이다.', @q2_1),
(@q2, 'DNS는 전송 계층에서 패킷의 순서 보장과 재전송을 책임지는 프로토콜이다.', 2, FALSE, '순서 보장과 재전송은 전송 계층 TCP의 기능이며, DNS는 애플리케이션 계층의 이름 해석 서비스다.', @q2_2),
(@q2, '사람이 읽기 쉬운 도메인 이름을 실제 통신에 필요한 IP 주소로 변환한다.', 3, TRUE, '', @q2_3),
(@q2, 'DNS는 클라이언트와 서버 사이의 모든 데이터를 암호화하는 보안 프로토콜이다.', 4, FALSE, '데이터 암호화는 TLS의 역할이며, 기본 DNS는 이름 해석을 담당할 뿐 암호화 프로토콜이 아니다.', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모(@q2)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '클라이언트의 브라우저 창이 열려 있는 시간이 캐시 보관 기간을 결정한다.', 1, FALSE, '캐시 유효 기간은 브라우저 창 상태가 아니라 레코드의 TTL 값으로 정해진다.', @q2),
(@q2_1, '운영체제가 매 요청마다 임의로 정하는 난수 값이 보관 기간이 된다.', 2, FALSE, '보관 기간은 난수가 아니라 레코드에 지정된 TTL 값으로 결정된다.', @q2),
(@q2_1, '각 레코드에 설정된 TTL(Time To Live) 값이 캐시 보관 유효 기간을 결정하며, 만료되면 다시 조회한다.', 3, TRUE, '', @q2),
(@q2_1, 'DNS 캐시는 만료 개념이 없어 서버를 재부팅하기 전까지 영원히 유지된다.', 4, FALSE, 'DNS 캐시에는 TTL이라는 만료 개념이 있어, TTL이 지나면 폐기하고 다시 조회한다.', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모(@q2)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, '일반적으로 UDP 53번 포트를 사용하고, 응답이 크거나 영역 전송이 필요하면 TCP를 사용한다.', 1, TRUE, '', @q2),
(@q2_2, '항상 TCP만 사용하며 UDP는 절대 사용하지 않는다.', 2, FALSE, '일반 질의는 오버헤드가 적은 UDP를 주로 사용하고, TCP는 큰 응답·영역 전송 등에 보조적으로 쓰인다.', @q2),
(@q2_2, '전송 계층을 거치지 않고 물리 계층에서 직접 통신한다.', 3, FALSE, 'DNS도 UDP/TCP 같은 전송 계층 프로토콜 위에서 동작하며 물리 계층에서 직접 통신하지 않는다.', @q2),
(@q2_2, 'HTTP 위에서만 동작하므로 반드시 80번 포트를 사용한다.', 4, FALSE, '기본 DNS는 HTTP가 아니라 UDP/TCP 53번 포트를 사용한다.', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모(@q2)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '하나의 중앙 서버가 전 세계 모든 도메인의 IP를 통째로 저장하고 있어 한 번에 응답한다.', 1, FALSE, 'DNS는 단일 중앙 서버가 아니라 계층적으로 분산된 여러 네임서버로 구성된다.', @q2),
(@q2_3, '리졸버가 루트 → TLD → 권한 네임서버 순으로 계층을 따라 질의해 최종 IP를 얻는다.', 2, TRUE, '', @q2),
(@q2_3, '클라이언트가 목적지 IP를 이미 알고 있어야만 도메인 이름을 조회할 수 있다.', 3, FALSE, 'IP를 모르기 때문에 이름 해석을 하는 것이며, IP를 미리 알아야 한다는 전제는 앞뒤가 맞지 않는다.', @q2),
(@q2_3, '권한 네임서버에 먼저 물은 뒤 루트 네임서버로 거슬러 올라가며 조회한다.', 4, FALSE, '조회 방향이 반대다. 루트에서 시작해 TLD를 거쳐 권한 네임서버로 내려가며 해석한다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모(@q2)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, 'A 레코드는 도메인을 IPv6에, AAAA 레코드는 IPv4에 매핑한다.', 1, FALSE, 'A와 AAAA의 매핑 대상이 뒤바뀌었다. A는 IPv4, AAAA는 IPv6에 매핑한다.', @q2),
(@q2_4, '모든 도메인은 오직 A 레코드 하나만 가질 수 있어 다른 레코드 타입은 존재하지 않는다.', 2, FALSE, 'A 외에도 AAAA, CNAME, MX, TXT 등 다양한 레코드 타입이 존재한다.', @q2),
(@q2_4, 'A 레코드는 도메인을 IPv4 주소에, AAAA는 IPv6 주소에, CNAME은 다른 도메인 이름에 매핑한다.', 3, TRUE, '', @q2),
(@q2_4, 'CNAME 레코드는 도메인을 IP 주소가 아니라 이메일 서버 우선순위에 매핑한다.', 4, FALSE, '이메일 서버를 지정하는 것은 MX 레코드이며, CNAME은 도메인을 다른 도메인 이름(별칭)에 매핑한다.', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, 'DNS'), (@q2, '이름 해석'),
(@q2_1, 'DNS 캐싱'), (@q2_1, 'TTL'),
(@q2_2, 'DNS 전송 프로토콜'),
(@q2_3, '재귀 질의'), (@q2_3, '네임서버'),
(@q2_4, 'DNS 레코드');


-- NETWORK 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HTTP와 HTTPS의 핵심 차이', 'HTTPS가 HTTP와 근본적으로 다른 점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'HTTPS는 기존 HTTP 통신에 TLS(SSL) 보안 계층을 추가해, 주고받는 데이터를 암호화하고 서버의 신원을 인증함으로써 기밀성·무결성·인증을 제공한다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('TLS 핸드셰이크의 목적', 'HTTPS 통신 시작 시 수행되는 TLS 핸드셰이크의 주된 목적으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 'TLS 핸드셰이크는 서버(필요 시 클라이언트)의 신원을 인증하고, 이후 데이터 암호화에 사용할 대칭 세션 키를 양측이 안전하게 합의·공유하기 위한 절차다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('TLS에서 대칭키와 비대칭키를 함께 쓰는 이유', 'TLS가 비대칭키 암호화와 대칭키 암호화를 함께 사용하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 '비대칭키는 안전하지만 연산이 느리므로 세션 키 교환에만 쓰고, 실제 대량의 데이터는 상대적으로 빠른 대칭키로 암호화해 보안과 성능을 모두 확보한다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('SSL 인증서와 CA의 역할', 'HTTPS에서 SSL/TLS 인증서와 인증기관(CA)이 하는 역할로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'CA(Certificate Authority)가 서명한 인증서를 통해 클라이언트는 서버의 공개키와 신원이 신뢰할 수 있는지 검증하며, 이로써 중간자 공격을 방지한다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('TLS가 제공하는 보안 속성', 'TLS가 통신에 제공하는 보안 속성으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'TLS는 데이터를 암호화하는 기밀성, 전송 중 변조를 감지하는 무결성, 통신 상대의 신원을 확인하는 인증을 함께 제공한다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, 'HTTPS는 HTTP와 요청/응답 메시지 구조가 완전히 달라 서로 호환되지 않는다.', 1, FALSE, 'HTTPS는 동일한 HTTP 메시지를 TLS로 감싸 전송할 뿐, 메시지 구조 자체는 HTTP와 같다.', @q3_1),
(@q3, 'HTTPS는 데이터를 압축만 할 뿐 암호화하지는 않아 도청에 그대로 노출된다.', 2, FALSE, 'HTTPS의 핵심은 TLS를 통한 암호화이며, 압축만 하고 암호화하지 않는다는 서술은 틀렸다.', @q3_2),
(@q3, 'HTTPS는 서버의 신원은 확인하지 않고 클라이언트의 신원만 인증한다.', 3, FALSE, 'TLS는 기본적으로 서버 인증을 수행하며, 클라이언트 인증은 선택적이다. 서버 인증을 하지 않는다는 서술은 틀렸다.', @q3_3),
(@q3, 'HTTP에 TLS 보안 계층을 추가해 데이터를 암호화하고 서버의 신원을 인증한다.', 4, TRUE, '', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모(@q3)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '서버를 인증하고, 이후 데이터 암호화에 쓸 대칭 세션 키를 양측이 안전하게 합의·공유하기 위한 절차다.', 1, TRUE, '', @q3),
(@q3_1, '전송할 파일을 미리 압축해 대역폭을 줄이는 것이 유일한 목적이다.', 2, FALSE, '핸드셰이크의 목적은 압축이 아니라 인증과 세션 키 합의다.', @q3),
(@q3_1, '서버의 물리적 위치(국가·도시)를 알아내 가장 가까운 서버로 라우팅하기 위함이다.', 3, FALSE, '지리적 라우팅은 핸드셰이크의 목적이 아니며, 핸드셰이크는 보안 파라미터 협상과 키 교환을 담당한다.', @q3),
(@q3_1, 'HTTP 요청 메서드(GET, POST)를 결정하기 위한 사전 협상 절차다.', 4, FALSE, '메서드는 애플리케이션이 요청 시 정하는 것이며, TLS 핸드셰이크와 무관하다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모(@q3)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '비대칭키는 어떤 상황에서도 대칭키보다 빠르므로 데이터 암호화에도 비대칭키만 쓴다.', 1, FALSE, '비대칭키는 오히려 연산이 느리며, 그래서 대량 데이터는 빠른 대칭키로 암호화한다.', @q3),
(@q3_2, '비대칭키로 세션 키를 안전하게 교환한 뒤, 이후 대량의 데이터는 빠른 대칭키로 암호화한다.', 2, TRUE, '', @q3),
(@q3_2, '두 방식을 함께 쓰는 이유는 단지 표준 문서가 그렇게 요구하기 때문일 뿐 기술적 이점은 없다.', 3, FALSE, '보안(안전한 키 교환)과 성능(빠른 대량 암호화)이라는 분명한 기술적 이점이 있어 함께 사용한다.', @q3),
(@q3_2, '대칭키는 키를 공유할 필요가 없어 교환 과정 자체가 존재하지 않기 때문이다.', 4, FALSE, '대칭키는 양측이 같은 키를 공유해야 하며, 그 안전한 교환을 위해 비대칭키를 사용하는 것이다.', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모(@q3)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '인증서는 서버의 처리 속도를 높이기 위한 캐시 힌트일 뿐 신원과는 무관하다.', 1, FALSE, '인증서는 성능 캐시가 아니라 서버의 공개키와 신원을 증명하는 신뢰의 근거다.', @q3),
(@q3_3, 'CA는 클라이언트의 비밀번호를 대신 저장해 주는 서버다.', 2, FALSE, 'CA는 비밀번호 저장소가 아니라 인증서에 서명해 신원을 보증하는 신뢰 기관이다.', @q3),
(@q3_3, 'CA가 서명한 인증서로 서버의 공개키와 신원이 신뢰할 수 있는지 검증해 중간자 공격을 방지한다.', 3, TRUE, '', @q3),
(@q3_3, '인증서는 데이터를 압축하는 알고리즘을 지정하는 파일이다.', 4, FALSE, '인증서는 압축 알고리즘 지정 파일이 아니라 공개키와 신원 정보를 담아 CA가 서명한 문서다.', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모(@q3)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, 'TLS는 오직 데이터 전송 속도만 높여 줄 뿐 보안과는 관련이 없다.', 1, FALSE, 'TLS의 목적은 속도 향상이 아니라 보안(기밀성·무결성·인증) 제공이다.', @q3),
(@q3_4, 'TLS는 서버의 저장 용량을 늘려 더 많은 데이터를 보관하게 해 준다.', 2, FALSE, 'TLS는 저장 용량과 무관하며 통신 보안을 담당한다.', @q3),
(@q3_4, 'TLS는 데이터를 암호화하지 않고 무결성 검사만 제공한다.', 3, FALSE, 'TLS는 무결성뿐 아니라 암호화를 통한 기밀성, 그리고 인증까지 함께 제공한다.', @q3),
(@q3_4, '데이터를 암호화하는 기밀성, 변조를 감지하는 무결성, 상대 신원을 확인하는 인증을 함께 제공한다.', 4, TRUE, '', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, 'HTTPS'), (@q3, 'TLS'),
(@q3_1, 'TLS 핸드셰이크'),
(@q3_2, '대칭키'), (@q3_2, '비대칭키'),
(@q3_3, '인증서'), (@q3_3, 'CA'),
(@q3_4, '기밀성'), (@q3_4, '무결성');


-- NETWORK 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('OSI 7계층 모델의 원리', 'OSI 7계층 모델의 계층 구조에 대한 설명으로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'OSI 모델은 통신 기능을 7개 계층으로 나누며, 각 계층은 독립된 역할을 맡아 바로 아래 계층의 서비스를 이용하고 바로 위 계층에 서비스를 제공하는 계층화 구조를 갖는다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('네트워크 계층화의 이점', '네트워크를 여러 계층으로 나누어 설계하는 계층화(layering)의 주된 이점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '각 계층이 독립적이므로 한 계층의 구현을 나머지에 영향을 주지 않고 교체·개선할 수 있어, 표준화와 유지보수가 쉬워지고 복잡한 통신 기능을 모듈로 나누어 다룰 수 있다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전송 계층의 역할', 'OSI 모델에서 전송 계층(4계층)이 담당하는 역할로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '전송 계층은 포트 번호로 종단 프로세스를 구분하고, TCP/UDP를 통해 종단 간(end-to-end) 데이터 전달과 신뢰성·흐름 제어를 담당한다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('캡슐화(Encapsulation) 과정', '데이터가 상위 계층에서 하위 계층으로 내려갈 때 일어나는 캡슐화(encapsulation)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 '캡슐화는 각 계층이 상위 계층에서 받은 데이터에 자신의 제어 정보(헤더)를 덧붙여 하위 계층으로 전달하는 과정으로, 수신 측에서는 역순으로 역캡슐화가 일어난다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('네트워크 계층과 IP 주소', 'OSI 모델에서 네트워크 계층(3계층)이 IP 주소를 통해 담당하는 역할로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '네트워크 계층은 IP 주소라는 논리 주소를 기반으로 서로 다른 네트워크 간 목적지까지의 경로를 결정하는 라우팅을 담당한다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '각 계층은 독립된 역할을 맡아 아래 계층의 서비스를 이용하고 위 계층에 서비스를 제공하는 계층화 구조를 갖는다.', 1, TRUE, '', @q4_1),
(@q4, '전송 계층(4계층)은 IP 주소를 기반으로 네트워크 간 라우팅 경로를 결정한다.', 2, FALSE, 'IP 주소 기반 라우팅은 네트워크 계층(3계층)의 역할이며, 전송 계층은 포트 기반 종단 간 전달을 담당한다.', @q4_2),
(@q4, '상위 계층은 하위 계층의 내부 구현 세부사항을 모두 알고 있어야만 동작할 수 있다.', 3, FALSE, '계층화의 핵심은 추상화로, 상위 계층은 하위 계층이 제공하는 인터페이스만 알면 되고 내부 구현은 몰라도 된다.', @q4_3),
(@q4, '물리 계층(1계층)은 논리적 IP 주소를 할당하고 관리하는 역할을 한다.', 4, FALSE, 'IP 주소 할당·관리는 네트워크 계층(3계층)의 역할이며, 물리 계층은 비트를 전기·광 신호로 전송한다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모(@q4)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '계층을 나누면 모든 계층을 항상 동시에 함께 수정해야 해 유지보수 부담이 오히려 커진다.', 1, FALSE, '계층화의 목적은 그 반대로, 계층 간 독립성 덕분에 한 계층만 따로 수정할 수 있어 부담이 줄어든다.', @q4),
(@q4_1, '각 계층이 독립적이라 한 계층 구현을 나머지에 영향 없이 교체·개선할 수 있어 표준화와 유지보수가 쉬워진다.', 2, TRUE, '', @q4),
(@q4_1, '계층을 나누는 유일한 목적은 데이터 전송 속도를 물리적으로 높이기 위함이다.', 3, FALSE, '계층화의 목적은 속도 향상이 아니라 복잡성 분리와 모듈화, 표준화·유지보수 용이성이다.', @q4),
(@q4_1, '계층화하면 서로 다른 제조사의 장비는 절대 함께 통신할 수 없게 된다.', 4, FALSE, '오히려 표준 계층 인터페이스 덕분에 서로 다른 제조사 장비 간 상호운용이 가능해진다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모(@q4)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '비트를 전기 신호로 바꿔 물리 매체로 전송하는 것이 전송 계층의 핵심 역할이다.', 1, FALSE, '비트를 신호로 바꿔 전송하는 것은 물리 계층(1계층)의 역할이다.', @q4),
(@q4_2, 'MAC 주소를 이용해 같은 네트워크 내 인접 장비로 프레임을 전달하는 것이 전송 계층의 역할이다.', 2, FALSE, 'MAC 주소 기반 인접 장비 전달은 데이터 링크 계층(2계층)의 역할이다.', @q4),
(@q4_2, '포트 번호로 종단 프로세스를 구분하고, TCP/UDP로 종단 간 전달과 신뢰성·흐름 제어를 담당한다.', 3, TRUE, '', @q4),
(@q4_2, '사용자에게 보여줄 화면(UI)을 그리고 사용자 입력을 처리하는 것이 전송 계층의 역할이다.', 4, FALSE, '화면과 사용자 상호작용은 애플리케이션/표현 계층에 가까운 영역이며 전송 계층의 역할이 아니다.', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모(@q4)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, '캡슐화는 상위 계층 데이터를 압축해 크기를 줄이는 것이 유일한 목적이다.', 1, FALSE, '캡슐화의 핵심은 압축이 아니라 각 계층의 제어 정보(헤더)를 덧붙이는 것이다.', @q4),
(@q4_3, '캡슐화는 데이터를 암호화해 상위 계층이 내용을 볼 수 없게 만드는 과정이다.', 2, FALSE, '캡슐화는 암호화가 아니라 헤더 추가 과정이며, 암호화는 별개의 보안 기능이다.', @q4),
(@q4_3, '캡슐화는 하위 계층 데이터에서 헤더를 제거해 상위로 올려보내는 과정이다.', 3, FALSE, '헤더를 제거해 상위로 올리는 것은 수신 측의 역캡슐화이며, 캡슐화는 헤더를 덧붙여 하위로 내리는 과정이다.', @q4),
(@q4_3, '각 계층이 상위 계층에서 받은 데이터에 자신의 헤더를 덧붙여 하위 계층으로 전달하는 과정이다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모(@q4)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, 'IP 주소라는 논리 주소를 기반으로 서로 다른 네트워크 간 목적지까지의 경로를 결정하는 라우팅을 담당한다.', 1, TRUE, '', @q4),
(@q4_4, 'IP 주소를 이용해 두 프로세스 사이의 연결 신뢰성과 재전송을 보장한다.', 2, FALSE, '연결 신뢰성과 재전송은 전송 계층(TCP)의 역할이며, 네트워크 계층은 경로 결정을 담당한다.', @q4),
(@q4_4, 'IP 주소를 전기 신호로 변환해 케이블로 직접 흘려보낸다.', 3, FALSE, '신호 변환·전송은 물리 계층의 역할이며, 네트워크 계층은 논리 주소 기반 라우팅을 담당한다.', @q4),
(@q4_4, 'IP 주소로 사용자에게 보여줄 웹 페이지를 렌더링한다.', 4, FALSE, '페이지 렌더링은 네트워크 계층의 역할이 아니라 애플리케이션 영역의 일이다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, 'OSI 7계층'), (@q4, '계층화'),
(@q4_1, '계층화'), (@q4_1, '표준화'),
(@q4_2, '전송 계층'),
(@q4_3, '캡슐화'),
(@q4_4, '네트워크 계층'), (@q4_4, '라우팅');


-- NETWORK 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('쿠키와 세션의 저장 위치 차이', '쿠키(Cookie)와 세션(Session)의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '쿠키는 데이터를 클라이언트(브라우저)에 저장하고, 세션은 실제 상태를 서버에 저장한 뒤 클라이언트에는 그 상태를 식별하는 세션 ID만 전달한다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('쿠키가 서버로 전달되는 방식', '브라우저에 저장된 쿠키가 서버로 전달되는 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '브라우저는 해당 도메인으로 요청을 보낼 때마다 저장된 쿠키를 HTTP 요청의 Cookie 헤더에 담아 자동으로 함께 전송한다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('세션 기반 인증의 동작 원리', '서버가 세션을 이용해 로그인한 사용자를 식별하는 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '서버는 세션 저장소에 사용자별 상태를 보관하고, 클라이언트가 매 요청에 실어 보내는 세션 ID로 그 저장소를 조회해 어떤 사용자인지 식별한다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('토큰(JWT) 기반 인증의 특징', '세션 방식과 대비되는 토큰(JWT) 기반 인증의 특징으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 '토큰 기반 인증은 서버가 상태를 따로 저장하지 않고(stateless), 사용자 정보를 담아 서명한 토큰 자체를 클라이언트가 보관하며, 서버는 서명 검증만으로 인증을 처리한다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('쿠키의 보안 속성', '쿠키의 HttpOnly와 Secure 속성에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 'HttpOnly 속성은 자바스크립트에서 쿠키에 접근하지 못하게 막아 XSS로 인한 탈취를 완화하고, Secure 속성은 HTTPS 연결에서만 쿠키를 전송하도록 제한한다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, '쿠키와 세션 모두 데이터를 서버 메모리에만 저장하고 클라이언트에는 아무것도 남기지 않는다.', 1, FALSE, '쿠키는 클라이언트(브라우저)에 저장되며, 세션도 최소한 세션 ID를 클라이언트에 전달한다. 서버에만 저장한다는 서술은 틀렸다.', @q5_1),
(@q5, '쿠키는 클라이언트에 저장되고, 세션은 상태를 서버에 저장하며 클라이언트에는 세션 ID만 전달한다.', 2, TRUE, '', @q5_2),
(@q5, '세션은 클라이언트에, 쿠키는 서버에 데이터를 저장한다.', 3, FALSE, '저장 위치가 반대로 서술되었다. 쿠키가 클라이언트, 세션의 실제 상태가 서버에 저장된다.', @q5_3),
(@q5, '쿠키는 서버가 절대 접근할 수 없어 클라이언트 내부에서만 사용되는 저장소다.', 4, FALSE, '쿠키는 요청 시 서버로 전송되어 서버가 읽고 활용할 수 있으므로, 서버가 접근할 수 없다는 서술은 틀렸다.', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모(@q5)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '쿠키는 사용자가 직접 복사해 매번 수동으로 서버에 붙여넣어야 전달된다.', 1, FALSE, '쿠키 전송은 수동이 아니라 브라우저가 요청 시 자동으로 처리한다.', @q5),
(@q5_1, '쿠키는 최초 로그인 시 한 번만 전송되고 이후 요청에는 포함되지 않는다.', 2, FALSE, '쿠키는 만료 전까지 해당 도메인으로의 매 요청마다 자동으로 함께 전송된다.', @q5),
(@q5_1, '브라우저가 해당 도메인 요청 시마다 쿠키를 HTTP 요청의 Cookie 헤더에 담아 자동으로 전송한다.', 3, TRUE, '', @q5),
(@q5_1, '쿠키는 오직 응답의 본문(body)에만 담겨 서버에서 클라이언트로만 이동한다.', 4, FALSE, '쿠키는 요청 시 Cookie 헤더로 서버에 올라가고, 서버는 Set-Cookie 헤더로 내려보낸다. 본문으로만 이동하지 않는다.', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모(@q5)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, '서버는 세션 저장소에 사용자 상태를 두고, 클라이언트가 보내는 세션 ID로 그 저장소를 조회해 사용자를 식별한다.', 1, TRUE, '', @q5),
(@q5_2, '서버는 아무것도 저장하지 않고 매 요청마다 사용자에게 비밀번호를 다시 물어 식별한다.', 2, FALSE, '세션 방식은 로그인 상태를 세션 저장소에 유지하므로 매 요청마다 비밀번호를 다시 받지 않는다.', @q5),
(@q5_2, '세션 ID에 사용자의 모든 개인정보를 평문으로 담아 클라이언트가 보관한다.', 3, FALSE, '세션 ID는 식별자일 뿐 실제 정보는 서버 저장소에 있으며, 개인정보를 평문으로 담지 않는다.', @q5),
(@q5_2, '세션은 클라이언트의 IP 주소만으로 사용자를 식별하며 세션 ID는 사용하지 않는다.', 4, FALSE, 'IP는 공유·변동될 수 있어 신뢰할 수 없으며, 세션 방식은 세션 ID로 사용자를 식별한다.', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모(@q5)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '토큰 방식도 세션처럼 서버가 모든 사용자 상태를 메모리에 저장해야만 동작한다.', 1, FALSE, '토큰 방식의 핵심은 서버가 상태를 저장하지 않는(stateless) 점으로, 세션과 대비된다.', @q5),
(@q5_3, '토큰은 서명이 없어 누구나 자유롭게 내용을 위조할 수 있다.', 2, FALSE, 'JWT는 서명을 포함해 서버가 위변조 여부를 검증할 수 있으므로 자유로운 위조가 불가능하다.', @q5),
(@q5_3, '토큰은 반드시 서버 데이터베이스를 매 요청마다 조회해야만 유효성을 확인할 수 있다.', 3, FALSE, '토큰은 서명 검증만으로 유효성을 확인할 수 있어 매 요청 DB 조회가 필수는 아니다.', @q5),
(@q5_3, '서버가 상태를 저장하지 않고(stateless), 사용자 정보를 담아 서명한 토큰 자체를 클라이언트가 보관하며 서버는 서명 검증으로 인증한다.', 4, TRUE, '', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모(@q5)로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, 'HttpOnly는 쿠키를 HTTP가 아닌 방식으로만 전송하게 만들어 속도를 높이는 속성이다.', 1, FALSE, 'HttpOnly는 전송 속도와 무관하며, 자바스크립트의 쿠키 접근을 막아 XSS 탈취를 완화하는 속성이다.', @q5),
(@q5_4, 'HttpOnly는 자바스크립트의 쿠키 접근을 막아 XSS 탈취를 완화하고, Secure는 HTTPS 연결에서만 쿠키를 전송하게 한다.', 2, TRUE, '', @q5),
(@q5_4, 'Secure 속성은 쿠키를 암호화해 어떤 연결에서도 내용을 읽을 수 없게 만든다.', 3, FALSE, 'Secure는 쿠키 내용을 암호화하는 것이 아니라, HTTPS 연결에서만 전송되도록 제한하는 속성이다.', @q5),
(@q5_4, '두 속성 모두 쿠키의 만료 시간을 지정하는 데 사용된다.', 4, FALSE, '만료 시간은 Expires/Max-Age 속성으로 지정하며, HttpOnly와 Secure는 보안 목적의 속성이다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, '쿠키'), (@q5, '세션'),
(@q5_1, '쿠키 전송'), (@q5_1, 'Cookie 헤더'),
(@q5_2, '세션 인증'),
(@q5_3, 'JWT'), (@q5_3, '토큰 인증'),
(@q5_4, '쿠키 보안'), (@q5_4, 'HttpOnly');


-- ============================================================
-- 카테고리: ALGORITHM (25문항)
-- ============================================================
-- ALGORITHM 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('빅오 표기법이 나타내는 것', '알고리즘의 시간복잡도를 나타내는 빅오(Big-O) 표기법의 의미로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '빅오는 입력 크기 n이 커질 때 알고리즘의 연산 횟수가 증가하는 상한(최악의 점근적 증가율)을 나타내며, 상수 계수와 낮은 차수 항은 무시한다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('실측 실행 시간 대신 점근 표기를 쓰는 이유', '알고리즘 분석에서 실제 실행 시간(초) 대신 빅오 같은 점근적 표기를 사용하는 주된 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '실행 시간은 하드웨어·언어·컴파일러·입력에 따라 달라지므로, 입력 크기 증가에 따른 알고리즘 고유의 성능 경향을 하드웨어 독립적으로 비교하기 위해 점근 표기를 쓴다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('최선·최악·평균의 경우 분석', '알고리즘의 시간복잡도를 최선(best)·최악(worst)·평균(average)의 경우로 나누어 분석하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '같은 알고리즘도 입력의 구성에 따라 연산 횟수가 달라지므로, 입력에 따른 성능 편차를 파악하기 위해 경우를 나누며 보통 최악의 경우를 상한 보장으로 삼는다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('상수 계수와 낮은 차수 항을 무시하는 이유', '빅오 표기에서 3n^2 + 5n + 100을 O(n^2)으로 단순화하는 근거로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 'n이 충분히 커지면 최고차항이 전체 증가율을 지배하고 낮은 차수 항과 상수 계수의 영향은 상대적으로 미미해지므로, 점근적 증가율을 나타내기 위해 최고차항만 남긴다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('공간 복잡도의 의미', '알고리즘의 공간 복잡도(space complexity)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '공간 복잡도는 입력 크기에 따라 알고리즘이 추가로 사용하는 메모리 양의 증가율을 나타내며, 시간 복잡도와 독립적으로 트레이드오프 관계에 놓일 수 있다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 꼬리질문으로 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '빅오는 알고리즘이 실제로 실행되는 정확한 밀리초 단위 실행 시간을 측정한 값이다.', 1, FALSE, '빅오는 절대적인 실행 시간이 아니라 입력 크기 증가에 따른 연산 횟수의 증가 경향을 나타내는 점근적 표기다.', @q1_1),
(@q1, '빅오는 입력 크기와 무관하게 항상 최선의 경우 실행 시간만을 나타낸다.', 2, FALSE, '빅오는 일반적으로 최악의 경우 상한을 나타내며, 입력 크기 n에 따른 증가율을 표현한다.', @q1_2),
(@q1, '입력 크기 n이 커질 때 연산 횟수가 증가하는 상한(점근적 증가율)을 상수 계수와 낮은 차수 항을 무시하고 나타낸 것이다.', 3, TRUE, '', @q1_3),
(@q1, '빅오는 알고리즘이 사용하는 메모리 양만을 나타내며 시간과는 무관하다.', 4, FALSE, '메모리 양의 증가율은 공간 복잡도이며, 빅오는 시간과 공간 어느 쪽의 증가율도 표현할 수 있다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '점근 표기가 실측보다 항상 더 정확한 절대 실행 시간을 알려주기 때문이다.', 1, FALSE, '점근 표기는 절대 시간을 알려주지 않으며, 증가 경향을 상수 계수와 무관하게 표현할 뿐이다.', @q1),
(@q1_1, '실행 시간은 하드웨어·언어·입력에 따라 달라지므로, 입력 크기 증가에 따른 알고리즘 고유의 성능 경향을 하드웨어 독립적으로 비교하기 위함이다.', 2, TRUE, '', @q1),
(@q1_1, '점근 표기를 쓰면 알고리즘의 메모리 사용량까지 자동으로 계산되기 때문이다.', 3, FALSE, '시간에 대한 점근 표기가 메모리 사용량을 자동으로 알려주지는 않는다. 공간 복잡도는 별도로 분석한다.', @q1),
(@q1_1, '실측은 불가능하고 오직 점근 표기로만 성능을 알 수 있기 때문이다.', 4, FALSE, '실측(벤치마크)도 얼마든지 가능하며, 점근 표기는 하드웨어 독립적 비교를 위한 보완적 도구다.', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '세 경우는 항상 동일한 복잡도를 가지므로 형식적으로 구분할 뿐이다.', 1, FALSE, '많은 알고리즘에서 최선·최악·평균의 복잡도가 서로 다르므로 구분은 형식적인 것이 아니다.', @q1),
(@q1_2, '최선의 경우가 실제 성능을 대표하므로 최악은 분석할 필요가 없다.', 2, FALSE, '성능 보장을 위해서는 오히려 최악의 경우 분석이 중요하며, 최선만으로 대표할 수 없다.', @q1),
(@q1_2, '평균의 경우는 항상 최선과 최악의 산술 평균으로 계산된다.', 3, FALSE, '평균의 경우는 입력 분포에 대한 기댓값으로 계산되며, 최선과 최악의 단순 산술 평균이 아니다.', @q1),
(@q1_2, '같은 알고리즘도 입력 구성에 따라 연산 횟수가 달라지므로, 입력에 따른 성능 편차를 파악하고 보통 최악의 경우를 상한 보장으로 삼기 위함이다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, 'n이 충분히 커지면 최고차항이 전체 증가율을 지배하고 낮은 차수 항·상수 계수의 영향은 상대적으로 미미해지기 때문이다.', 1, TRUE, '', @q1),
(@q1_3, '상수와 낮은 차수 항은 실제로 존재하지 않는 값이라 계산에서 제외되기 때문이다.', 2, FALSE, '이 항들은 실제로 존재하지만, 점근적 증가율을 볼 때 지배적이지 않아 생략하는 것이다.', @q1),
(@q1_3, '빅오는 n이 매우 작은 경우만 다루므로 큰 항을 무시하는 것이다.', 3, FALSE, '빅오는 n이 충분히 커질 때의 경향(점근적)을 다루며, 작은 n을 전제로 하지 않는다.', @q1),
(@q1_3, '최고차항보다 상수 항이 항상 실행 시간에 더 큰 영향을 주기 때문이다.', 4, FALSE, 'n이 커질수록 최고차항의 영향이 상수 항을 압도하므로 서술이 반대다.', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '입력 크기에 따라 알고리즘이 추가로 사용하는 메모리 양의 증가율을 나타내며, 시간 복잡도와 트레이드오프 관계일 수 있다.', 1, TRUE, '', @q1),
(@q1_4, '공간 복잡도는 시간 복잡도와 항상 정확히 같은 값을 가진다.', 2, FALSE, '두 복잡도는 독립적이며, 메모리를 더 써서 시간을 줄이는 등 서로 다를 수 있다.', @q1),
(@q1_4, '공간 복잡도는 프로그램 소스 코드의 길이를 나타낸다.', 3, FALSE, '소스 코드 길이가 아니라 실행 중 사용하는 메모리 양의 증가율을 나타낸다.', @q1),
(@q1_4, '공간 복잡도가 낮으면 시간 복잡도도 반드시 함께 낮아진다.', 4, FALSE, '메모리를 아끼면 오히려 시간이 늘어나는 트레이드오프가 생길 수 있어 항상 함께 낮아지지 않는다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, '시간복잡도'), (@q1, '빅오'),
(@q1_1, '점근 표기'), (@q1_1, '성능 분석'),
(@q1_2, '최악의 경우'),
(@q1_3, '점근 표기'),
(@q1_4, '공간 복잡도');


-- ALGORITHM 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('퀵 정렬과 병합 정렬의 차이', '퀵 정렬(Quick Sort)과 병합 정렬(Merge Sort)의 비교로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '퀵 정렬은 평균 O(n log n)이지만 피벗이 극단적으로 치우치면 최악 O(n^2)이고 제자리(in-place) 정렬이 가능하다. 병합 정렬은 최악에도 O(n log n)을 보장하지만 병합용 추가 메모리 O(n)이 필요하다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('병합 정렬이 최악에도 O(n log n)인 이유', '병합 정렬이 입력 상태와 무관하게 최악의 경우에도 O(n log n)을 보장하는 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '병합 정렬은 입력을 항상 절반씩 분할해 재귀 깊이가 log n으로 고정되고, 각 레벨에서 전체 n개를 병합하므로 입력 분포와 무관하게 n log n의 연산이 보장된다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('병합 정렬이 추가 메모리를 쓰는 이유', '병합 정렬이 일반적으로 O(n)의 추가 메모리를 필요로 하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '두 정렬된 부분 배열을 하나로 병합할 때 원소들을 순서대로 옮겨 담을 별도의 임시 배열이 필요하기 때문이다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('안정 정렬(stable sort)의 의미', '정렬 알고리즘이 안정(stable)하다는 것의 의미로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '안정 정렬은 정렬 키가 같은 원소들의 상대적인 입력 순서가 정렬 후에도 그대로 보존되는 것을 의미한다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('퀵 정렬이 최악 O(n^2)이 되는 경우', '퀵 정렬이 최악의 경우 O(n^2) 시간복잡도를 갖게 되는 상황으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '피벗 선택이 매번 최솟값이나 최댓값에 가까워 분할이 한쪽으로 극단적으로 치우치면 분할 깊이가 n에 가까워져 O(n^2)이 된다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 꼬리질문으로 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, '병합 정렬은 최악의 경우 O(n^2)이고 퀵 정렬은 항상 O(n log n)을 보장한다.', 1, FALSE, '서술이 반대다. 병합 정렬이 최악에도 O(n log n)을 보장하고, 퀵 정렬이 최악의 경우 O(n^2)이 될 수 있다.', @q2_1),
(@q2, '두 정렬 모두 추가 메모리를 전혀 쓰지 않는 제자리 정렬이다.', 2, FALSE, '퀵 정렬은 제자리 정렬이 가능하지만, 병합 정렬은 병합 과정에서 O(n)의 임시 배열이 필요하다.', @q2_2),
(@q2, '퀵 정렬은 항상 안정 정렬(stable sort)이고 병합 정렬은 불안정 정렬이다.', 3, FALSE, '일반적인 퀵 정렬은 불안정 정렬이고, 병합 정렬은 대표적인 안정 정렬이므로 서술이 반대다.', @q2_3),
(@q2, '퀵 정렬은 평균 O(n log n)이나 최악 O(n^2)이며 제자리 정렬이 가능하고, 병합 정렬은 최악에도 O(n log n)을 보장하나 O(n) 추가 메모리가 필요하다.', 4, TRUE, '', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '입력을 항상 절반씩 분할해 재귀 깊이가 log n으로 고정되고 각 레벨에서 n개를 병합하므로, 입력 분포와 무관하게 n log n이 보장된다.', 1, TRUE, '', @q2),
(@q2_1, '병합 과정에서 피벗을 잘 골라 분할이 균등해지기 때문이다.', 2, FALSE, '병합 정렬은 피벗을 사용하지 않으며, 항상 절반으로 분할하므로 피벗 선택과 무관하다.', @q2),
(@q2_1, '이미 정렬된 입력에서만 O(n log n)이 성립하기 때문이다.', 3, FALSE, '병합 정렬은 입력 정렬 여부와 무관하게 항상 O(n log n)을 보장한다.', @q2),
(@q2_1, '병합 정렬이 내부적으로 이진 탐색을 사용하기 때문이다.', 4, FALSE, '병합 정렬은 이진 탐색이 아니라 분할과 병합으로 동작한다.', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, '재귀 호출 스택이 O(n) 깊이까지 쌓이기 때문이다.', 1, FALSE, '병합 정렬의 재귀 깊이는 log n이며, 추가 메모리는 병합용 임시 배열에서 발생한다.', @q2),
(@q2_2, '정렬할 원소를 모두 해시테이블에 복사해 두어야 하기 때문이다.', 2, FALSE, '병합 정렬은 해시테이블을 사용하지 않는다.', @q2),
(@q2_2, '두 정렬된 부분 배열을 하나로 병합할 때 원소를 순서대로 옮겨 담을 임시 배열이 필요하기 때문이다.', 3, TRUE, '', @q2),
(@q2_2, '피벗을 저장할 별도의 배열이 필요하기 때문이다.', 4, FALSE, '병합 정렬은 피벗을 사용하지 않으므로 피벗 저장 공간과 무관하다.', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '어떤 입력에도 항상 O(n log n) 시간을 보장한다는 의미다.', 1, FALSE, '이는 성능 보장에 관한 설명이며, 안정성(stability)과는 다른 개념이다.', @q2),
(@q2_3, '정렬 키가 같은 원소들의 상대적 입력 순서가 정렬 후에도 그대로 보존되는 것을 의미한다.', 2, TRUE, '', @q2),
(@q2_3, '정렬 도중 프로그램이 절대 오류로 중단되지 않는다는 의미다.', 3, FALSE, '안정 정렬은 실행 안정성이 아니라 동일 키 원소의 순서 보존을 뜻한다.', @q2),
(@q2_3, '추가 메모리를 전혀 사용하지 않는다는 의미다.', 4, FALSE, '추가 메모리 사용 여부는 제자리(in-place) 정렬 개념이며 안정성과 다르다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, '피벗을 항상 중앙값으로 정확히 고를 때다.', 1, FALSE, '피벗을 중앙값으로 고르면 분할이 균등해져 오히려 최선에 가까운 O(n log n)이 된다.', @q2),
(@q2_4, '입력 원소가 모두 서로 다른 값일 때다.', 2, FALSE, '원소가 서로 다른지 여부가 아니라 피벗 선택에 따른 분할 균형이 최악을 좌우한다.', @q2),
(@q2_4, '재귀 대신 반복문으로 구현했을 때다.', 3, FALSE, '구현이 재귀냐 반복이냐는 최악 복잡도를 바꾸지 않는다.', @q2),
(@q2_4, '피벗이 매번 최솟값·최댓값에 가까워 분할이 한쪽으로 극단적으로 치우칠 때(예: 정렬된 배열에서 항상 끝 원소를 피벗으로 선택)다.', 4, TRUE, '', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, '정렬 알고리즘'), (@q2, '퀵 정렬'),
(@q2_1, '병합 정렬'), (@q2_1, '분할 정복'),
(@q2_2, '공간 복잡도'),
(@q2_3, '안정 정렬'),
(@q2_4, '퀵 정렬'), (@q2_4, '최악의 경우');


-- ALGORITHM 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 탐색의 전제 조건', '이진 탐색(Binary Search)을 적용하기 위한 필수 전제 조건으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '이진 탐색은 매 단계에서 중앙값과 비교해 탐색 범위를 절반으로 줄이므로, 데이터가 대소 관계에 따라 정렬되어 있고 인덱스로 임의 접근이 가능해야 O(log n)에 동작한다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 탐색이 O(log n)인 이유', '이진 탐색의 시간복잡도가 O(log n)인 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '매 비교마다 탐색 범위를 절반으로 줄이므로, n을 1이 될 때까지 반으로 나누는 횟수인 log2(n)번의 비교만으로 탐색이 끝나기 때문이다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('해시 탐색과 이진 탐색의 차이', '해시 기반 탐색과 이진 탐색을 비교한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '해시 탐색은 평균 O(1)이지만 대소 순서 정보를 잃어 범위 탐색이 어렵고, 이진 탐색은 정렬을 전제로 O(log n)이지만 범위 탐색·정렬 순서 조회에 유리하다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('연결 리스트에서 이진 탐색이 비효율적인 이유', '정렬된 연결 리스트에서 이진 탐색이 배열만큼 효율적이지 못한 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '연결 리스트는 인덱스로 중앙 원소에 O(1)로 접근할 수 없고 매번 노드를 순차 이동해야 하므로, 중앙값을 찾는 데 O(n)이 들어 이진 탐색의 이점이 사라진다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 탐색의 중앙 인덱스 계산', '이진 탐색에서 중앙 인덱스를 mid = (low + high) / 2 대신 mid = low + (high - low) / 2로 계산하기도 하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 'low + high가 자료형의 최대값을 초과해 정수 오버플로가 발생하는 것을 방지하기 위해, 차이를 먼저 구해 더하는 방식으로 같은 중앙값을 안전하게 계산한다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 꼬리질문으로 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, '데이터가 정렬되어 있고 인덱스로 임의 접근이 가능해야 하며, 이때 매 단계 범위를 절반으로 줄여 O(log n)에 탐색한다.', 1, TRUE, '', @q3_1),
(@q3, '데이터가 정렬되어 있지 않아도 되며 해시 함수만 있으면 된다.', 2, FALSE, '해시 함수로 O(1) 탐색을 하는 것은 해시 탐색이며, 이진 탐색은 반드시 정렬을 전제로 한다.', @q3_2),
(@q3, '데이터가 연결 리스트로 저장되어 있을 때 가장 효율적이다.', 3, FALSE, '연결 리스트는 중앙 원소에 O(1)로 접근할 수 없어 이진 탐색에 불리하며, 배열이 적합하다.', @q3_3),
(@q3, '탐색 대상 값이 반드시 배열의 중앙에 위치해야 한다.', 4, FALSE, '이진 탐색은 값의 위치와 무관하게 범위를 좁혀가며 찾으므로, 중앙에 있어야 한다는 조건은 없다.', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '한 번의 비교로 정답 위치를 바로 계산하기 때문이다.', 1, FALSE, '한 번에 위치를 계산하는 것은 해시 탐색의 특징이며, 이진 탐색은 여러 번 비교해 범위를 좁힌다.', @q3),
(@q3_1, '배열 전체를 한 번씩 순회하기 때문이다.', 2, FALSE, '전체를 순회하면 O(n)이다. 이진 탐색은 순회하지 않고 절반씩 줄인다.', @q3),
(@q3_1, '매 비교마다 탐색 범위를 절반으로 줄이므로, n을 1까지 반으로 나누는 횟수인 log2(n)번의 비교로 끝나기 때문이다.', 3, TRUE, '', @q3),
(@q3_1, '데이터 개수와 무관하게 항상 상수 번만 비교하기 때문이다.', 4, FALSE, '비교 횟수는 상수가 아니라 데이터 개수에 로그 비례해 늘어난다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '해시 탐색과 이진 탐색 모두 정렬된 데이터를 반드시 요구한다.', 1, FALSE, '해시 탐색은 정렬을 요구하지 않으며, 정렬 전제는 이진 탐색에만 해당한다.', @q3),
(@q3_2, '이진 탐색은 평균 O(1)이고 해시 탐색은 O(log n)이다.', 2, FALSE, '서술이 반대다. 해시 탐색이 평균 O(1), 이진 탐색이 O(log n)이다.', @q3),
(@q3_2, '해시 탐색은 정렬된 순서대로 범위 조회를 하기에 이진 탐색보다 항상 우수하다.', 3, FALSE, '해시 탐색은 순서 정보를 잃어 범위 조회에 오히려 불리하다.', @q3),
(@q3_2, '해시 탐색은 평균 O(1)이나 순서 정보가 없어 범위 탐색이 어렵고, 이진 탐색은 O(log n)이나 정렬 순서 기반 범위 조회에 유리하다.', 4, TRUE, '', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '연결 리스트는 정렬 자체가 불가능하기 때문이다.', 1, FALSE, '연결 리스트도 정렬이 가능하며, 문제는 정렬이 아니라 임의 접근이 안 된다는 점이다.', @q3),
(@q3_3, '연결 리스트는 인덱스로 중앙 원소에 O(1)로 접근할 수 없고 매번 순차 이동해야 해, 중앙값을 찾는 데 O(n)이 들기 때문이다.', 2, TRUE, '', @q3),
(@q3_3, '연결 리스트는 값을 비교하는 연산을 지원하지 않기 때문이다.', 3, FALSE, '연결 리스트에 저장된 값도 얼마든지 비교할 수 있으므로 사실과 다르다.', @q3),
(@q3_3, '연결 리스트는 항상 역순으로만 순회할 수 있기 때문이다.', 4, FALSE, '단일 연결 리스트는 정방향 순회가 기본이며 역순 전용이라는 서술은 틀렸다.', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '두 식은 서로 다른 위치를 가리키며 후자가 항상 더 왼쪽을 찾기 때문이다.', 1, FALSE, '두 식은 수학적으로 같은 중앙값을 가리키며, 다른 위치를 찾지 않는다.', @q3),
(@q3_4, '후자가 계산 속도가 훨씬 빨라 시간복잡도가 낮아지기 때문이다.', 2, FALSE, '두 식의 계산 비용은 사실상 같으며 시간복잡도를 바꾸지 않는다.', @q3),
(@q3_4, 'low + high가 자료형의 최대값을 초과해 정수 오버플로가 나는 것을 방지하기 위함이며, 두 식은 같은 중앙값을 준다.', 3, TRUE, '', @q3),
(@q3_4, '후자를 쓰면 정렬되지 않은 배열에서도 이진 탐색이 가능해지기 때문이다.', 4, FALSE, '중앙 인덱스 계산 방식과 무관하게 이진 탐색은 여전히 정렬을 전제로 한다.', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, '이진 탐색'), (@q3, '정렬'),
(@q3_1, '시간복잡도'),
(@q3_2, '해시 탐색'),
(@q3_3, '임의 접근'),
(@q3_4, '정수 오버플로');


-- ALGORITHM 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('동적 계획법의 핵심 조건', '동적 계획법(Dynamic Programming)을 적용하기에 적합한 문제의 핵심 특징으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 'DP는 큰 문제가 작은 부분 문제로 나뉘고(최적 부분 구조), 그 부분 문제들이 여러 번 중복해서 등장할 때(중복 부분 문제) 각 부분 문제의 답을 한 번만 계산해 저장·재사용함으로써 효율을 높인다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('분할 정복과 동적 계획법의 차이', '분할 정복(Divide and Conquer)과 동적 계획법의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '둘 다 부분 문제로 나누지만, 분할 정복은 부분 문제가 서로 겹치지 않아 재계산이 없고, 동적 계획법은 부분 문제가 중복되므로 결과를 저장(메모이제이션)해 재계산을 피한다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('메모이제이션과 타뷸레이션', 'DP 구현 방식인 메모이제이션(top-down)과 타뷸레이션(bottom-up)의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '메모이제이션은 재귀로 큰 문제부터 내려가며 필요한 부분 문제 결과를 캐시에 저장하는 하향식이고, 타뷸레이션은 가장 작은 부분 문제부터 반복문으로 표를 채워 올라가는 상향식이다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그리디 알고리즘과 DP의 차이', '그리디(Greedy) 알고리즘과 동적 계획법의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '그리디는 매 단계에서 지역 최적 선택을 하고 그것을 되돌리지 않으며 특정 조건(탐욕 선택 속성)에서만 전체 최적을 보장한다. DP는 관련 부분 문제의 해를 모두 검토·결합해 전체 최적을 보장한다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DP에서 점화식(상태 전이)의 역할', '동적 계획법에서 점화식(상태 전이 식)을 정의하는 것이 중요한 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '점화식은 부분 문제의 해가 더 작은 부분 문제의 해로부터 어떻게 계산되는지를 정의하며, 이것이 있어야 부분 문제들을 올바른 순서로 채워 전체 해를 구성할 수 있다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 꼬리질문으로 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '부분 문제들이 서로 완전히 독립적이며 절대 겹치지 않아야 한다.', 1, FALSE, '부분 문제가 겹치지 않는 것은 분할 정복의 특징이며, DP는 오히려 중복 부분 문제가 있을 때 유리하다.', @q4_1),
(@q4, '큰 문제가 작은 부분 문제로 나뉘는 최적 부분 구조를 가지며, 그 부분 문제들이 여러 번 중복해서 등장(중복 부분 문제)해야 한다.', 2, TRUE, '', @q4_2),
(@q4, '매 단계에서 지역적으로 최적인 선택만 하면 항상 전체 최적해가 보장되어야 한다.', 3, FALSE, '이는 그리디 알고리즘의 적용 조건(탐욕 선택 속성)이며 DP의 핵심 조건이 아니다.', @q4_3),
(@q4, '입력이 반드시 정렬되어 있어야 한다.', 4, FALSE, 'DP는 입력 정렬을 전제로 하지 않으며, 정렬은 별개의 요구사항이다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '분할 정복은 부분 문제가 겹치지 않아 재계산이 없고, DP는 부분 문제가 중복되므로 결과를 저장해 재계산을 피한다.', 1, TRUE, '', @q4),
(@q4_1, '분할 정복은 부분 문제 결과를 항상 저장하고, DP는 저장하지 않는다.', 2, FALSE, '서술이 반대다. 결과를 저장해 재사용하는 쪽이 DP다.', @q4),
(@q4_1, '분할 정복은 반복문만 쓰고 DP는 재귀만 쓴다.', 3, FALSE, '두 기법 모두 재귀·반복 어느 쪽으로도 구현할 수 있어 구현 형태로 구분되지 않는다.', @q4),
(@q4_1, '두 기법은 이름만 다를 뿐 완전히 동일한 알고리즘이다.', 4, FALSE, '부분 문제의 중복 여부와 결과 저장 여부에서 분명히 다른 기법이다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '메모이제이션은 항상 타뷸레이션보다 시간복잡도가 낮다.', 1, FALSE, '두 방식은 같은 점화식을 계산하므로 시간복잡도는 일반적으로 동일하다.', @q4),
(@q4_2, '두 방식은 모두 부분 문제 결과를 저장하지 않는다는 공통점이 있다.', 2, FALSE, '두 방식 모두 부분 문제 결과를 저장(캐시/표)해 재사용하는 것이 핵심이다.', @q4),
(@q4_2, '메모이제이션은 상향식 반복문이고 타뷸레이션은 하향식 재귀다.', 3, FALSE, '서술이 반대다. 메모이제이션이 하향식 재귀, 타뷸레이션이 상향식 반복이다.', @q4),
(@q4_2, '메모이제이션은 재귀로 큰 문제부터 내려가며 결과를 캐시하는 하향식이고, 타뷸레이션은 작은 부분 문제부터 반복문으로 표를 채우는 상향식이다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, '그리디는 항상 DP보다 느리지만 더 정확한 답을 보장한다.', 1, FALSE, '그리디는 보통 더 빠르며, 정확성은 오히려 특정 조건에서만 보장된다.', @q4),
(@q4_3, '그리디와 DP 모두 모든 문제에서 항상 전체 최적해를 보장한다.', 2, FALSE, '그리디는 탐욕 선택 속성이 성립하는 문제에서만 최적을 보장한다.', @q4),
(@q4_3, '그리디는 매 단계 지역 최적 선택을 되돌리지 않아 특정 조건에서만 최적을 보장하고, DP는 관련 부분 문제 해를 모두 검토·결합해 최적을 보장한다.', 3, TRUE, '', @q4),
(@q4_3, 'DP는 지역 최적만 보고 그리디는 전역을 모두 탐색한다.', 4, FALSE, '서술이 반대다. 지역 최적만 보는 쪽이 그리디이고, 부분 문제를 두루 검토하는 쪽이 DP다.', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '부분 문제의 해가 더 작은 부분 문제의 해로부터 어떻게 계산되는지를 정의해, 부분 문제를 올바른 순서로 채워 전체 해를 구성할 수 있게 하기 때문이다.', 1, TRUE, '', @q4),
(@q4_4, '점화식은 입력 데이터를 정렬하는 규칙을 정의하는 것이다.', 2, FALSE, '점화식은 정렬 규칙이 아니라 부분 문제 간 관계(상태 전이)를 정의한다.', @q4),
(@q4_4, '점화식은 재귀 호출을 금지하기 위한 장치일 뿐이다.', 3, FALSE, '점화식은 재귀를 금지하는 장치가 아니라 부분 문제의 해를 계산하는 관계식이다.', @q4),
(@q4_4, '점화식이 없어도 DP는 항상 정상 동작하므로 형식적 절차에 불과하다.', 4, FALSE, '점화식(상태 전이) 정의가 없으면 부분 문제를 어떤 순서로 어떻게 결합할지 알 수 없어 DP를 구성할 수 없다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, '동적 계획법'), (@q4, '중복 부분 문제'),
(@q4_1, '분할 정복'),
(@q4_2, '메모이제이션'), (@q4_2, '타뷸레이션'),
(@q4_3, '그리디'),
(@q4_4, '점화식');


-- ALGORITHM 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DFS와 BFS의 탐색 순서 차이', '깊이 우선 탐색(DFS)과 너비 우선 탐색(BFS)의 핵심 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 'DFS는 한 경로를 끝까지 파고든 뒤 되돌아오며(스택/재귀 사용) 탐색하고, BFS는 시작점에서 가까운 노드부터 레벨 단위로(큐 사용) 넓게 탐색한다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DFS가 스택(재귀)을, BFS가 큐를 쓰는 이유', 'DFS는 스택(또는 재귀)을, BFS는 큐를 사용하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 'DFS는 가장 최근 방문한 노드에서 더 깊이 들어가야 하므로 후입선출인 스택이 맞고, BFS는 먼저 발견한 노드부터 순서대로 넓혀가야 하므로 선입선출인 큐가 맞다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('가중치 없는 그래프에서 BFS와 최단 경로', '가중치가 없는 그래프에서 BFS로 최단 경로(간선 수 기준)를 구할 수 있는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 'BFS는 시작점에서 가까운 노드부터 레벨 단위로 방문하므로, 어떤 노드에 처음 도달했을 때의 경로가 곧 그 노드까지의 최소 간선 수 경로가 되기 때문이다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DFS와 백트래킹(backtracking)', '백트래킹이 DFS 기반 탐색과 밀접한 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'ALGORITHM',
 '백트래킹은 한 경로로 깊이 내려가며 해를 시도하다가 조건을 만족할 수 없으면 직전 분기점으로 되돌아가(가지치기) 다른 선택을 탐색하는데, 이 깊이 우선으로 내려갔다 되돌아오는 흐름이 DFS 구조와 동일하기 때문이다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그래프 탐색에서 방문 체크가 필요한 이유', 'DFS/BFS로 그래프를 탐색할 때 방문 여부를 별도로 기록해야 하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'ALGORITHM',
 '그래프에는 사이클이 있을 수 있어 방문 여부를 기록하지 않으면 같은 노드를 반복 방문해 무한 루프에 빠지거나 중복 처리로 비효율이 발생하므로, 방문 배열 등으로 이미 방문한 노드를 걸러야 한다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 꼬리질문으로 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, 'DFS는 큐를, BFS는 스택을 사용해 구현하는 것이 일반적이다.', 1, FALSE, '서술이 반대다. DFS는 스택(재귀), BFS는 큐를 사용하는 것이 일반적이다.', @q5_1),
(@q5, 'DFS와 BFS 모두 시작점에서 가까운 노드부터 레벨 순으로 방문한다.', 2, FALSE, '레벨 순 방문은 BFS의 특징이며, DFS는 한 경로를 깊이 파고든다.', @q5_2),
(@q5, 'BFS는 한 경로를 끝까지 파고든 뒤 되돌아오는 방식으로 동작한다.', 3, FALSE, '한 경로를 끝까지 파고드는 것은 DFS이며, BFS는 레벨 단위로 넓게 탐색한다.', @q5_3),
(@q5, 'DFS는 한 경로를 끝까지 파고든 뒤 되돌아오며(스택/재귀) 탐색하고, BFS는 가까운 노드부터 레벨 단위로(큐) 넓게 탐색한다.', 4, TRUE, '', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '스택과 큐는 성능이 같아 아무거나 써도 결과가 동일하기 때문이다.', 1, FALSE, '자료구조에 따라 방문 순서(깊이 우선/너비 우선)가 달라지므로 결과가 같지 않다.', @q5),
(@q5_1, 'DFS는 가장 최근 방문한 노드에서 더 깊이 들어가야 해 후입선출 스택이 맞고, BFS는 먼저 발견한 노드부터 넓혀가야 해 선입선출 큐가 맞기 때문이다.', 2, TRUE, '', @q5),
(@q5_1, 'DFS는 반드시 큐로만, BFS는 반드시 스택으로만 구현할 수 있기 때문이다.', 3, FALSE, '연결이 반대이며, 실제로는 DFS가 스택(재귀), BFS가 큐를 사용한다.', @q5),
(@q5_1, '스택과 큐 모두 정렬 기능이 있어 탐색 순서를 자동 정렬하기 때문이다.', 4, FALSE, '스택과 큐는 정렬 기능이 없으며, 삽입·삭제 순서 규칙(LIFO/FIFO)만 제공한다.', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, 'BFS가 모든 경로의 가중치 합을 매번 정렬하기 때문이다.', 1, FALSE, '가중치 없는 그래프이며, BFS는 가중치 합을 정렬하지 않고 레벨 순으로 방문한다.', @q5),
(@q5_2, 'BFS는 방문할 노드를 무작위로 골라 우연히 최단 경로를 찾기 때문이다.', 2, FALSE, 'BFS는 무작위가 아니라 큐를 통해 가까운 노드부터 순서대로 방문한다.', @q5),
(@q5_2, 'BFS는 가까운 노드부터 레벨 단위로 방문하므로, 어떤 노드에 처음 도달한 경로가 곧 최소 간선 수 경로가 되기 때문이다.', 3, TRUE, '', @q5),
(@q5_2, 'BFS는 항상 목적지 노드를 가장 먼저 방문하기 때문이다.', 4, FALSE, 'BFS는 목적지를 먼저 방문하는 것이 아니라 시작점에서 가까운 순서로 방문한다.', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '백트래킹은 항상 그래프의 모든 노드를 레벨 순으로 방문하기 때문이다.', 1, FALSE, '레벨 순 방문은 BFS의 특징이며, 백트래킹은 깊이 우선으로 내려갔다 되돌아온다.', @q5),
(@q5_3, '백트래킹은 한 경로로 깊이 내려가 해를 시도하다 불가능하면 직전 분기점으로 되돌아가 다른 선택을 탐색하는데, 이 흐름이 DFS 구조와 같기 때문이다.', 2, TRUE, '', @q5),
(@q5_3, '백트래킹은 큐를 사용해 넓게 탐색하는 기법이기 때문이다.', 3, FALSE, '백트래킹은 큐 기반 너비 우선이 아니라 깊이 우선으로 내려갔다 되돌아오는 방식이다.', @q5),
(@q5_3, '백트래킹은 탐색 없이 수식으로 답을 바로 계산하기 때문이다.', 4, FALSE, '백트래킹은 여러 후보를 실제로 탐색하며 가지치기하는 기법이지 수식으로 즉시 계산하지 않는다.', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, '방문 기록은 탐색 결과를 정렬하기 위해 필요하다.', 1, FALSE, '방문 기록은 정렬이 아니라 재방문을 막기 위한 것이다.', @q5),
(@q5_4, '방문 기록이 없으면 그래프의 간선 개수를 셀 수 없기 때문이다.', 2, FALSE, '간선 개수 계산과 방문 기록은 직접 관련이 없다. 방문 기록은 재방문 방지를 위한 것이다.', @q5),
(@q5_4, '그래프에 사이클이 있을 수 있어, 방문 여부를 기록하지 않으면 같은 노드를 반복 방문해 무한 루프에 빠지거나 중복 처리가 생기기 때문이다.', 3, TRUE, '', @q5),
(@q5_4, '방문 기록이 있어야만 노드에 값을 저장할 수 있기 때문이다.', 4, FALSE, '노드 값 저장과 방문 기록은 무관하며, 방문 기록은 재방문을 걸러내기 위한 것이다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, 'DFS'), (@q5, 'BFS'),
(@q5_1, '스택'), (@q5_1, '큐'),
(@q5_2, '최단 경로'),
(@q5_3, '백트래킹'),
(@q5_4, '방문 체크');


-- ============================================================
-- 카테고리: DATA_STRUCTURE (25문항)
-- ============================================================
-- DATA_STRUCTURE 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙과 우선순위 큐의 관계', '이진 힙(Binary Heap)으로 우선순위 큐(Priority Queue)를 구현할 때의 특징으로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '이진 힙은 부모-자식 간 대소 관계(힙 속성)만 유지하는 완전 이진 트리로, 루트에서 최솟값 또는 최댓값을 O(1)에 확인하고 삽입·삭제를 O(log n)에 수행하여 우선순위 큐를 효율적으로 구현한다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙과 이진 탐색 트리의 차이', '힙(Heap)이 이진 탐색 트리(BST)와 구별되는 핵심 속성으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '힙은 부모와 자식 사이의 대소 관계(힙 속성)만 보장할 뿐 형제 노드 간이나 좌우 서브트리 간의 정렬 순서는 보장하지 않는다. 반면 BST는 왼쪽<노드<오른쪽의 전역 정렬 순서를 유지한다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙이 완전 정렬 상태가 아닌 이유', '이진 힙이 원소들을 완전히 정렬된 상태로 저장하지 않는데도 우선순위 큐로 널리 쓰이는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '우선순위 큐는 매번 최우선(최대 또는 최소) 원소 하나만 꺼내면 되므로, 전체를 완전 정렬하는 비용을 치르지 않고 루트만 최적으로 유지하는 힙이 삽입·삭제 O(log n)으로 더 효율적이다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('배열로 구현한 힙의 인덱스 관계', '이진 힙을 0-based 배열로 구현할 때, 인덱스 i인 노드의 부모와 자식 위치로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '완전 이진 트리를 배열에 레벨 순서로 채우면 인덱스 i의 왼쪽 자식은 2i+1, 오른쪽 자식은 2i+2, 부모는 (i-1)/2(정수 나눗셈)로 계산되어 포인터 없이도 부모·자식을 O(1)에 찾을 수 있다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙에서 루트 삭제 후 재정렬 비용', '최소 힙에서 루트(최솟값)를 삭제한 뒤 힙 속성을 복구하는 과정의 시간복잡도로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 '마지막 원소를 루트로 옮긴 뒤 자식과 비교하며 아래로 내려보내는(sift-down) 과정은 트리 높이만큼만 진행되므로 O(log n)이다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '힙은 모든 노드가 왼쪽 자식<부모<오른쪽 자식을 만족하는 이진 탐색 트리의 일종이며, 중위 순회하면 정렬된 순서가 나온다.', 1, FALSE, '힙은 형제 노드 간 순서를 보장하지 않고 중위 순회해도 정렬된 순서가 나오지 않는다. 이는 이진 탐색 트리의 특성과 혼동한 서술이다.', @q1_1),
(@q1, '힙은 항상 완전히 정렬된 상태를 유지하므로 임의의 k번째로 큰 원소를 O(1)에 바로 찾을 수 있다.', 2, FALSE, '힙은 완전 정렬 상태가 아니라 부모-자식 간 부분 순서만 유지하므로, 루트(1번째) 외의 임의 k번째 원소를 O(1)에 찾을 수 없다.', @q1_2),
(@q1, '힙은 부모-자식 간 대소 관계만 만족하는 완전 이진 트리로, 루트에서 최솟값(또는 최댓값)을 O(1)에 확인하고 삽입·삭제는 O(log n)에 수행한다.', 3, TRUE, '', @q1_3),
(@q1, '힙에서 최솟값 삭제 후 재정렬(heapify)은 트리 전체를 다시 정렬해야 하므로 O(n log n)이 걸린다.', 4, FALSE, '삭제 후 복구는 sift-down으로 트리 높이만큼만 내려가면 되므로 O(log n)이며, 전체 재정렬이 필요하지 않다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '힙도 BST처럼 중위 순회하면 오름차순 정렬 결과를 얻을 수 있다.', 1, FALSE, '힙은 형제 간 순서를 보장하지 않아 중위 순회해도 정렬된 결과가 나오지 않는다. 이는 BST에서만 성립한다.', @q1),
(@q1_1, '힙은 부모-자식 간 대소 관계만 보장하고 형제 간 순서는 보장하지 않는 반면, BST는 왼쪽<노드<오른쪽의 전역 정렬 순서를 유지한다.', 2, TRUE, '', @q1),
(@q1_1, '힙과 BST 모두 특정 값을 O(log n)에 탐색할 수 있다.', 3, FALSE, '힙은 임의의 값을 탐색하려면 전체를 훑어야 해 O(n)이 걸리며, O(log n) 탐색은 BST의 특성이다.', @q1),
(@q1_1, '힙은 반드시 포인터 기반으로만 구현할 수 있고 배열로는 구현할 수 없다.', 4, FALSE, '힙은 완전 이진 트리라 인덱스 계산으로 배열에 흔히 구현되며, 오히려 배열 구현이 일반적이다.', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '완전 정렬을 유지하면 삽입이 O(1)로 빨라지기 때문이다.', 1, FALSE, '완전 정렬을 유지하려면 삽입 시 올바른 위치를 찾아 넣어야 해 오히려 O(n)이 걸리므로 사실과 반대다.', @q1),
(@q1_2, '힙은 원소를 하나도 정렬하지 않아 어떤 순서 정보도 담지 않기 때문이다.', 2, FALSE, '힙은 부모-자식 간 대소 관계라는 부분 순서를 유지하므로 순서 정보를 전혀 담지 않는다는 서술은 틀렸다.', @q1),
(@q1_2, '완전 정렬 상태여야만 우선순위 큐를 구현할 수 있기 때문이다.', 3, FALSE, '완전 정렬 없이도 힙 속성만으로 우선순위 큐를 구현할 수 있으므로 완전 정렬은 필수 조건이 아니다.', @q1),
(@q1_2, '우선순위 큐는 매번 최우선 원소 하나만 필요하므로, 전체 정렬 비용 없이 루트만 최적으로 유지하는 힙이 O(log n) 삽입·삭제로 더 효율적이기 때문이다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '왼쪽 자식은 2i+1, 오른쪽 자식은 2i+2, 부모는 (i-1)/2로 계산된다.', 1, TRUE, '', @q1),
(@q1_3, '왼쪽 자식은 i-1, 오른쪽 자식은 i+1, 부모는 i/2로 계산된다.', 2, FALSE, 'i-1과 i+1은 같은 레벨의 이웃 인덱스일 뿐 자식이 아니다. 완전 이진 트리의 자식은 2i+1, 2i+2로 계산된다.', @q1),
(@q1_3, '자식과 부모 위치는 매번 트리를 순회해서 찾아야 하며 수식으로 구할 수 없다.', 3, FALSE, '완전 이진 트리를 배열에 레벨 순서로 담으면 부모·자식 위치를 수식으로 즉시 구할 수 있어 순회가 필요 없다.', @q1),
(@q1_3, '배열로는 부모만 계산할 수 있고 자식 위치는 별도 포인터가 필요하다.', 4, FALSE, '자식 위치도 2i+1, 2i+2로 수식 계산이 가능하므로 별도 포인터가 필요하지 않다.', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '전체 원소를 다시 비교해 정렬해야 하므로 O(n log n)이 걸린다.', 1, FALSE, '삭제 후 복구는 마지막 원소 하나를 아래로 내려보내는 것이라 전체 재정렬이 필요 없어 O(n log n)이 아니다.', @q1),
(@q1_4, '마지막 원소를 루트로 옮긴 뒤 트리 높이만큼 아래로 내려보내면 되므로 O(log n)이다.', 2, TRUE, '', @q1),
(@q1_4, '항상 루트만 교체하면 되므로 O(1)이다.', 3, FALSE, '루트 교체 후 힙 속성이 깨질 수 있어 sift-down으로 복구해야 하므로 O(1)이 아니다.', @q1),
(@q1_4, '모든 리프 노드를 방문해야 하므로 O(n)이 걸린다.', 4, FALSE, 'sift-down은 루트에서 한 경로만 따라 내려가므로 모든 리프를 방문하지 않아 O(n)이 아니다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, '힙'), (@q1, '우선순위 큐'),
(@q1_1, '힙'), (@q1_1, '이진 탐색 트리'),
(@q1_2, '우선순위 큐'),
(@q1_3, '배열 기반 힙'), (@q1_3, '인덱스'),
(@q1_4, 'heapify'), (@q1_4, '시간복잡도');


-- DATA_STRUCTURE 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 트리 순회 방식의 구분', '이진 트리의 깊이 우선 순회 중 중위 순회(In-order)의 방문 순서로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '중위 순회는 왼쪽 서브트리 → 루트 → 오른쪽 서브트리 순으로 방문하며, 이진 탐색 트리에 적용하면 오름차순 정렬된 순서로 노드를 방문하게 된다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전위 순회의 활용', '전위 순회(Pre-order, 루트→왼쪽→오른쪽)가 특히 유용하게 쓰이는 대표적인 상황으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '전위 순회는 루트를 가장 먼저 방문하므로, 트리 구조를 그대로 복제하거나 디렉터리 구조·수식 트리를 위에서부터 직렬화할 때 적합하다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('후위 순회가 적합한 작업', '후위 순회(Post-order, 왼쪽→오른쪽→루트)가 가장 자연스럽게 들어맞는 작업으로 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '후위 순회는 자식을 모두 처리한 뒤 마지막에 부모를 방문하므로, 자식 노드가 차지한 자원을 먼저 해제하고 부모를 해제하는 트리 메모리 해제나 디렉터리 용량 합산처럼 하위 결과를 모아 상위에서 종합하는 작업에 적합하다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('깊이 우선 순회의 반복문 구현', '전위·중위·후위 같은 깊이 우선 순회를 재귀 없이 반복문으로 구현할 때 일반적으로 사용하는 자료구조로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '깊이 우선 순회는 방문을 미뤄 둔 노드를 나중에 되돌아와 처리해야 하므로, 후입선출(LIFO) 특성의 스택을 이용하며 이는 재귀 호출의 콜 스택을 명시적으로 대체하는 것과 같다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('중위 순회와 정렬의 관계', '이진 탐색 트리(BST)를 중위 순회하면 오름차순 정렬 결과가 나오는 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 'BST는 모든 노드에서 왼쪽 서브트리 < 노드 < 오른쪽 서브트리가 성립하므로, 왼쪽→루트→오른쪽 순으로 방문하는 중위 순회는 항상 작은 값부터 큰 값 순으로 노드를 방문하게 된다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, '루트를 먼저 방문한 뒤 왼쪽 서브트리, 오른쪽 서브트리 순으로 방문한다.', 1, FALSE, '루트를 가장 먼저 방문하는 것은 전위 순회(Pre-order)이며, 중위 순회는 루트를 왼쪽 서브트리 방문 후에 방문한다.', @q2_1),
(@q2, '왼쪽 서브트리를 먼저 방문한 뒤 루트, 그다음 오른쪽 서브트리를 방문한다.', 2, TRUE, '', @q2_2),
(@q2, '왼쪽과 오른쪽 서브트리를 모두 방문한 뒤 마지막에 루트를 방문한다.', 3, FALSE, '자식을 모두 방문한 뒤 루트를 방문하는 것은 후위 순회(Post-order)이며 중위 순회가 아니다.', @q2_3),
(@q2, '트리를 레벨 단위로 위에서 아래로 방문한다.', 4, FALSE, '레벨 단위로 방문하는 것은 너비 우선(레벨 순서) 순회이며, 깊이 우선의 중위 순회와는 다르다.', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '이진 탐색 트리를 오름차순으로 정렬해 출력할 때 사용한다.', 1, FALSE, '오름차순 정렬 출력은 왼쪽→루트→오른쪽 순의 중위 순회로 얻으며 전위 순회로는 정렬 결과가 나오지 않는다.', @q2),
(@q2_1, '트리의 리프 노드부터 상향식으로 자원을 해제할 때 사용한다.', 2, FALSE, '자식부터 처리하고 부모를 나중에 처리하는 상향식 해제는 후위 순회에 적합하며 전위 순회가 아니다.', @q2),
(@q2_1, '트리를 레벨별로 나눠 최단 경로를 찾을 때 사용한다.', 3, FALSE, '레벨별 방문은 너비 우선(레벨 순서) 순회의 특성이며 전위 순회의 용도가 아니다.', @q2),
(@q2_1, '루트를 먼저 방문하므로 트리 구조를 그대로 복제하거나 위에서부터 직렬화할 때 사용한다.', 4, TRUE, '', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, '자식을 모두 처리한 뒤 부모를 방문하므로 트리의 메모리를 해제하거나 하위 결과를 상위에서 합산하는 작업에 적합하다.', 1, TRUE, '', @q2),
(@q2_2, '루트부터 방문하므로 트리를 그대로 복제할 때 가장 적합하다.', 2, FALSE, '루트부터 방문해 복제하는 것은 전위 순회의 용도이며 후위 순회의 특성이 아니다.', @q2),
(@q2_2, '노드를 오름차순으로 정렬해 출력하는 데 가장 적합하다.', 3, FALSE, '오름차순 출력은 중위 순회의 특성이며 후위 순회로는 정렬 순서가 나오지 않는다.', @q2),
(@q2_2, '가장 가까운 노드부터 방문해 최단 경로 탐색에 적합하다.', 4, FALSE, '가까운 노드부터 방문하는 것은 너비 우선 순회이며 후위 순회와는 무관하다.', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '먼저 발견한 노드를 먼저 처리하는 큐(FIFO)를 사용한다.', 1, FALSE, '큐를 쓰면 레벨 순서(너비 우선) 순회가 되며, 깊이 우선 순회에는 스택(LIFO)이 필요하다.', @q2),
(@q2_3, '우선순위 큐를 사용해 값이 작은 노드부터 방문한다.', 2, FALSE, '깊이 우선 순회의 방문 순서는 값의 크기가 아니라 트리 구조를 따르므로 우선순위 큐가 필요하지 않다.', @q2),
(@q2_3, '되돌아와 처리할 노드를 쌓아 두는 스택(LIFO)을 사용하며, 이는 재귀의 콜 스택을 명시적으로 대체한 것이다.', 3, TRUE, '', @q2),
(@q2_3, '해시테이블에 방문 순서를 저장해 순회한다.', 4, FALSE, '해시테이블은 순서를 보장하는 자료구조가 아니어서 순회 순서 관리에 적합하지 않다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, '중위 순회가 내부적으로 정렬 알고리즘을 호출하기 때문이다.', 1, FALSE, '중위 순회는 별도 정렬 알고리즘을 호출하지 않으며, BST의 구조적 속성 덕분에 방문 자체가 정렬 순서가 되는 것이다.', @q2),
(@q2_4, '모든 이진 트리는 중위 순회하면 정렬되기 때문이다.', 2, FALSE, '왼쪽<노드<오른쪽 속성을 만족하는 BST에서만 중위 순회 결과가 정렬되며, 일반 이진 트리는 그렇지 않다.', @q2),
(@q2_4, '중위 순회가 항상 값이 가장 큰 노드부터 방문하기 때문이다.', 3, FALSE, '중위 순회는 가장 작은 값부터 방문하므로 오름차순이 되며, 큰 값부터 방문한다는 서술은 틀렸다.', @q2),
(@q2_4, 'BST는 왼쪽<노드<오른쪽 속성을 만족하므로, 왼쪽→루트→오른쪽 순 방문이 곧 오름차순 방문이 되기 때문이다.', 4, TRUE, '', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, '트리 순회'), (@q2, '중위 순회'),
(@q2_1, '전위 순회'),
(@q2_2, '후위 순회'),
(@q2_3, '깊이 우선 순회'), (@q2_3, '스택'),
(@q2_4, '중위 순회'), (@q2_4, '이진 탐색 트리');


-- DATA_STRUCTURE 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이(Trie) 자료구조의 특징', '문자열 집합을 저장·검색하는 트라이(Trie, 접두사 트리)의 핵심 특징으로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '트라이는 문자열의 각 문자를 트리의 간선(또는 노드)에 대응시켜 공통 접두사를 경로로 공유하며, 길이 L인 문자열의 검색·삽입을 저장된 문자열 개수와 무관하게 O(L)에 수행한다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이의 공간 사용 특성', '트라이가 검색 속도 면에서 유리함에도 실제 사용 시 주의해야 하는 대표적인 단점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 '트라이는 각 노드가 가능한 다음 문자마다 자식 포인터를 두므로, 저장된 문자열이 접두사를 많이 공유하지 않으면 노드당 포인터 배열로 인해 메모리 사용량이 크게 늘어날 수 있다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이가 접두사 검색에 강한 이유', '검색어 자동완성처럼 특정 접두사로 시작하는 모든 단어를 찾는 작업에서 트라이가 특히 효율적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '트라이는 공통 접두사가 하나의 경로로 표현되므로, 접두사에 해당하는 노드까지 O(L)에 내려간 뒤 그 아래 서브트리만 탐색하면 해당 접두사로 시작하는 모든 단어를 얻을 수 있다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이와 해시테이블의 문자열 저장 비교', '문자열 집합 저장에서 트라이가 해시테이블과 구별되는 장점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '해시테이블은 키 전체를 해싱해 저장하므로 정확한 키 조회에는 빠르지만 접두사 기반 범위 검색이 어렵다. 트라이는 접두사를 경로로 공유하므로 접두사 검색·사전식 정렬 순회 같은 순서 기반 연산에 강하다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이 검색 시간복잡도', 'n개의 문자열이 저장된 트라이에서 길이 L인 문자열 하나를 검색할 때의 시간복잡도로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '트라이 검색은 문자열의 각 문자를 따라 노드를 한 번씩 내려가면 되므로 저장된 문자열 개수 n과 무관하게 O(L)이다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, '트라이는 각 노드에 하나의 완전한 문자열을 저장하며, 검색 시 저장된 문자열 수 n에 대해 O(log n)이 걸린다.', 1, FALSE, '트라이의 노드는 완전한 문자열이 아니라 문자 단위에 대응하며, 검색은 저장 개수 n이 아니라 문자열 길이 L에 비례한다.', @q3_1),
(@q3, '트라이는 해시 함수로 문자열을 버킷에 매핑하는 해시테이블의 일종이다.', 2, FALSE, '트라이는 해시 함수를 사용하지 않고 문자를 따라 경로를 내려가는 트리 구조이므로 해시테이블의 일종이 아니다.', @q3_2),
(@q3, '트라이는 문자열을 정렬된 배열에 저장해 이진 탐색으로 조회한다.', 3, FALSE, '트라이는 정렬 배열과 이진 탐색이 아니라 문자 경로를 따라가는 트리 탐색으로 조회한다.', @q3_3),
(@q3, '문자열의 각 문자를 경로로 표현해 공통 접두사를 공유하며, 길이 L 문자열의 검색·삽입을 저장 개수와 무관하게 O(L)에 수행한다.', 4, TRUE, '', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '각 노드가 가능한 다음 문자마다 자식 포인터를 두므로, 접두사 공유가 적으면 메모리 사용량이 크게 늘 수 있다.', 1, TRUE, '', @q3),
(@q3_1, '문자열 검색 시간이 저장된 문자열 개수 n에 비례해 느려진다.', 2, FALSE, '트라이 검색은 문자열 길이 L에만 비례하고 저장 개수 n과 무관하므로 n에 비례해 느려지지 않는다.', @q3),
(@q3_1, '트라이는 문자열의 접두사 검색을 전혀 지원하지 못한다.', 3, FALSE, '접두사 검색은 오히려 트라이의 대표적 강점이므로 지원하지 못한다는 서술은 틀렸다.', @q3),
(@q3_1, '삽입할 때마다 전체 트리를 재정렬해야 한다.', 4, FALSE, '트라이는 문자 경로를 따라 노드를 추가할 뿐 전체 재정렬이 필요하지 않다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '트라이가 모든 단어를 해시테이블에 저장해 O(1)에 조회하기 때문이다.', 1, FALSE, '트라이는 해시테이블 기반이 아니며, 접두사 검색의 강점은 해싱이 아니라 접두사를 경로로 공유하는 구조에서 나온다.', @q3),
(@q3_2, '트라이가 단어들을 미리 알파벳순 배열로 정렬해 두기 때문이다.', 2, FALSE, '트라이는 정렬된 배열이 아니라 문자 경로 트리 구조이며, 접두사 검색은 서브트리 탐색으로 이뤄진다.', @q3),
(@q3_2, '공통 접두사가 하나의 경로로 표현되어, 접두사 노드까지 내려간 뒤 그 아래 서브트리만 탐색하면 되기 때문이다.', 3, TRUE, '', @q3),
(@q3_2, '트라이가 접두사를 만나면 자동으로 전체 단어를 반환하는 내장 함수를 갖기 때문이다.', 4, FALSE, '그런 내장 함수는 존재하지 않으며, 접두사 아래 서브트리를 직접 탐색해 단어들을 수집하는 것이다.', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '트라이는 해시 충돌이 발생하지 않도록 해시 함수를 이중으로 적용한다.', 1, FALSE, '트라이는 해시 함수를 아예 사용하지 않으므로 해시 충돌이나 이중 해싱과 무관하다.', @q3),
(@q3_3, '접두사를 경로로 공유하므로 접두사 검색이나 사전식 정렬 순회 같은 순서 기반 연산에 강하다.', 2, TRUE, '', @q3),
(@q3_3, '트라이는 항상 해시테이블보다 적은 메모리를 사용한다.', 3, FALSE, '트라이는 노드마다 자식 포인터를 두어 접두사 공유가 적으면 오히려 해시테이블보다 메모리를 더 쓸 수 있다.', @q3),
(@q3_3, '트라이는 문자열이 아닌 정수 키만 저장할 수 있다.', 4, FALSE, '트라이는 본래 문자열(문자 시퀀스) 저장에 쓰이는 자료구조이므로 정수 키만 저장한다는 서술은 틀렸다.', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '문자열의 각 문자를 따라 노드를 내려가면 되므로 저장 개수 n과 무관하게 O(L)이다.', 1, TRUE, '', @q3),
(@q3_4, '저장된 문자열 개수에 비례하는 O(n)이다.', 2, FALSE, '트라이 검색은 문자열 길이 L을 따라 내려가는 것이라 저장 개수 n에 비례하지 않는다.', @q3),
(@q3_4, '정렬된 문자열에 대한 이진 탐색과 같아 O(L log n)이다.', 3, FALSE, '트라이는 이진 탐색을 사용하지 않고 문자 경로를 직접 따라가므로 O(L log n)이 아니다.', @q3),
(@q3_4, '모든 문자열과 일일이 비교하므로 O(n·L)이다.', 4, FALSE, '트라이는 후보 문자열을 전수 비교하지 않고 해당 경로만 따라가므로 O(n·L)이 아니다.', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, '트라이'), (@q3, '접두사 트리'),
(@q3_1, '트라이'), (@q3_1, '공간복잡도'),
(@q3_2, '접두사 검색'), (@q3_2, '자동완성'),
(@q3_3, '트라이'), (@q3_3, '해시테이블'),
(@q3_4, '시간복잡도');


-- DATA_STRUCTURE 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온-파인드(Disjoint Set)의 용도', '유니온-파인드(Union-Find, 서로소 집합) 자료구조가 해결하는 핵심 문제로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '유니온-파인드는 원소들을 서로소 집합으로 관리하며, 두 집합을 합치는 union 연산과 특정 원소가 속한 집합의 대표를 찾는 find 연산으로 원소들이 같은 집합에 속하는지(연결되어 있는지)를 효율적으로 판별한다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온-파인드의 경로 압축', '유니온-파인드에서 경로 압축(Path Compression) 최적화가 하는 일로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 'find 연산을 수행하며 거쳐 간 노드들이 직접 루트(대표)를 가리키도록 부모 포인터를 갱신해 트리 높이를 낮춤으로써, 이후의 find 연산이 거의 상수 시간에 가깝게 동작하도록 만든다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온 시 트리가 깊어지는 것을 막는 방법', '유니온-파인드에서 union 연산을 반복해도 트리가 한쪽으로 길게 늘어지지 않도록 하는 대표적인 기법으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 'union by rank(또는 union by size)는 두 트리를 합칠 때 높이(또는 크기)가 작은 트리를 큰 트리 아래에 붙여, 결과 트리의 높이가 불필요하게 커지는 것을 방지한다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온-파인드의 대표적 활용', '유니온-파인드가 특히 유용하게 쓰이는 대표적인 알고리즘으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 '크루스칼(Kruskal)의 최소 신장 트리 알고리즘에서 간선을 추가할 때 두 정점이 이미 같은 집합(연결 요소)에 속하는지 find로 확인해 사이클 형성을 방지하는 데 유니온-파인드가 핵심적으로 쓰인다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('최적화된 유니온-파인드의 연산 비용', '경로 압축과 union by rank를 모두 적용한 유니온-파인드에서 find·union 연산의 평균 시간복잡도로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 '두 최적화를 함께 적용하면 연산당 상각(amortized) 시간복잡도가 거의 상수에 가까운 O(α(n))(아커만 함수의 역함수)으로, 실질적으로 상수 시간에 가깝다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '원소들을 서로소 집합으로 관리하며, union과 find로 두 원소가 같은 집합(연결 요소)에 속하는지 효율적으로 판별한다.', 1, TRUE, '', @q4_1),
(@q4, '원소들을 우선순위에 따라 정렬해 매번 최솟값을 꺼내는 문제를 해결한다.', 2, FALSE, '우선순위에 따라 최솟값을 꺼내는 것은 힙 기반 우선순위 큐의 용도이며 유니온-파인드의 역할이 아니다.', @q4_2),
(@q4, '문자열의 접두사를 공유해 저장·검색하는 문제를 해결한다.', 3, FALSE, '접두사 공유 저장·검색은 트라이의 용도이며 유니온-파인드와는 무관하다.', @q4_3),
(@q4, '키를 해시값으로 매핑해 O(1) 조회하는 문제를 해결한다.', 4, FALSE, '해시값 매핑 조회는 해시테이블의 용도이며 유니온-파인드의 핵심 문제가 아니다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '집합에 속한 모든 원소를 하나의 배열로 복사해 정렬한다.', 1, FALSE, '경로 압축은 원소를 복사하거나 정렬하는 것이 아니라 find 경로의 노드가 루트를 직접 가리키게 갱신하는 것이다.', @q4),
(@q4_1, '두 집합을 합칠 때 항상 더 큰 집합을 루트로 삼는다.', 2, FALSE, '큰 집합을 루트로 삼는 것은 union by rank(size)의 설명이며 경로 압축과는 다른 최적화다.', @q4),
(@q4_1, 'find 중 거쳐 간 노드들이 직접 루트를 가리키게 갱신해 트리 높이를 낮추고 이후 find를 거의 상수 시간으로 만든다.', 3, TRUE, '', @q4),
(@q4_1, '집합 내 원소를 해시테이블에 저장해 조회를 빠르게 한다.', 4, FALSE, '유니온-파인드는 부모 포인터 배열로 집합을 관리하며 경로 압축은 해시테이블과 무관하다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '합칠 때마다 두 트리를 모두 이진 탐색 트리로 재구성한다.', 1, FALSE, 'union by rank는 이진 탐색 트리 재구성과 무관하며, 작은 트리를 큰 트리 아래에 붙이는 방식이다.', @q4),
(@q4_2, '두 트리를 합칠 때 높이(또는 크기)가 작은 트리를 큰 트리 아래에 붙이는 union by rank(size)를 사용한다.', 2, TRUE, '', @q4),
(@q4_2, '항상 번호가 작은 원소를 루트로 삼아 합친다.', 3, FALSE, '번호 기준으로 루트를 정하면 트리 높이가 커질 수 있어 균형을 보장하지 못하므로 rank/size 기준을 쓴다.', @q4),
(@q4_2, '두 집합의 모든 원소를 매번 다시 연결한다.', 4, FALSE, '모든 원소를 다시 연결하면 O(n)이 들어 비효율적이며, 실제로는 두 루트만 연결한다.', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, '이진 탐색으로 정렬된 배열에서 값을 찾는 데 쓰인다.', 1, FALSE, '이진 탐색은 정렬 배열에서의 값 탐색 기법이며 집합 연결 관계를 다루는 유니온-파인드와 무관하다.', @q4),
(@q4_3, '다익스트라 최단 경로에서 최소 거리 정점을 꺼내는 데 쓰인다.', 2, FALSE, '최소 거리 정점을 꺼내는 데는 우선순위 큐(힙)가 쓰이며 유니온-파인드의 활용이 아니다.', @q4),
(@q4_3, '문자열 자동완성 기능을 구현하는 데 쓰인다.', 3, FALSE, '자동완성은 접두사 트리인 트라이의 활용이며 유니온-파인드와는 관계가 없다.', @q4),
(@q4_3, '크루스칼 최소 신장 트리에서 간선 추가 시 사이클 형성 여부를 find로 판별하는 데 쓰인다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '항상 정확히 O(n)이 걸린다.', 1, FALSE, '경로 압축과 union by rank를 적용하면 연산당 비용이 거의 상수에 가까워 O(n)이 아니다.', @q4),
(@q4_4, '항상 O(log n)으로 고정된다.', 2, FALSE, '두 최적화를 함께 쓰면 O(log n)보다 빠른 거의 상수에 가까운 O(α(n))이 된다.', @q4),
(@q4_4, '거의 상수에 가까운 O(α(n))(아커만 함수의 역함수)으로 동작한다.', 3, TRUE, '', @q4),
(@q4_4, '트리 전체를 정렬해야 하므로 O(n log n)이다.', 4, FALSE, '유니온-파인드는 정렬을 수행하지 않으므로 O(n log n)이 걸리지 않는다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, '유니온 파인드'), (@q4, '서로소 집합'),
(@q4_1, '경로 압축'),
(@q4_2, 'union by rank'),
(@q4_3, '크루스칼'), (@q4_3, '최소 신장 트리'),
(@q4_4, '시간복잡도');


-- DATA_STRUCTURE 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B-트리의 구조적 특징', '데이터베이스 인덱스 등에 널리 쓰이는 B-트리(B-Tree)의 구조적 특징으로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 'B-트리는 한 노드가 여러 개의 키와 자식을 가질 수 있는 균형 잡힌 다진(multi-way) 탐색 트리로, 모든 리프 노드가 같은 깊이에 있도록 유지되어 디스크 접근 횟수를 낮은 트리 높이로 억제한다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B-트리가 디스크 기반 저장에 적합한 이유', 'B-트리가 이진 탐색 트리 대신 데이터베이스나 파일 시스템의 디스크 인덱스로 선호되는 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 '한 노드에 많은 키를 담아 트리 높이(depth)를 크게 낮추므로, 노드 하나를 디스크 블록 하나에 대응시키면 탐색에 필요한 느린 디스크 접근 횟수를 크게 줄일 수 있기 때문이다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B+트리와 B-트리의 차이', 'B+트리(B+ Tree)가 B-트리와 구별되는 핵심 특징으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 'B+트리는 실제 데이터(레코드)를 모두 리프 노드에만 저장하고 내부 노드는 인덱스(키) 역할만 하며, 리프 노드들이 연결 리스트로 이어져 있어 범위 검색과 순차 스캔에 특히 유리하다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B-트리의 탐색 시간복잡도', 'n개의 키를 저장한 B-트리에서 특정 키를 탐색할 때의 시간복잡도로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DATA_STRUCTURE',
 'B-트리는 균형이 유지되어 높이가 O(log n)이며, 각 노드 내에서 키를 찾는 비용을 포함해도 탐색은 O(log n)에 수행된다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B-트리의 노드 분할', 'B-트리에 키를 삽입하다가 한 노드가 허용된 최대 키 개수를 초과할 때 일어나는 처리로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DATA_STRUCTURE',
 '가득 찬 노드를 두 개로 쪼개고(split) 가운데 키를 부모 노드로 끌어올리며, 이 분할이 위로 전파되면서도 모든 리프가 같은 깊이를 유지하도록 트리의 균형을 보존한다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, 'B-트리는 각 노드가 최대 2개의 자식만 갖는 이진 트리로, 이진 탐색 트리와 구조가 동일하다.', 1, FALSE, 'B-트리는 한 노드가 여러 개의 키와 자식을 갖는 다진 트리이므로 자식이 최대 2개인 이진 트리라는 서술은 틀렸다.', @q5_1),
(@q5, '한 노드가 여러 키와 자식을 갖는 균형 잡힌 다진 탐색 트리로, 모든 리프가 같은 깊이에 있어 트리 높이가 낮게 유지된다.', 2, TRUE, '', @q5_2),
(@q5, 'B-트리는 균형을 맞추지 않으므로 삽입 순서에 따라 한쪽으로 편향될 수 있다.', 3, FALSE, 'B-트리는 분할 등을 통해 항상 모든 리프가 같은 깊이가 되도록 균형을 유지하므로 편향되지 않는다.', @q5_3),
(@q5, 'B-트리는 모든 데이터를 루트 노드 하나에만 저장한다.', 4, FALSE, 'B-트리의 데이터는 여러 노드에 분산 저장되며 루트 하나에만 모두 담기지 않는다.', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '한 노드에 많은 키를 담아 트리 높이를 낮추므로, 노드를 디스크 블록에 대응시켜 느린 디스크 접근 횟수를 줄일 수 있기 때문이다.', 1, TRUE, '', @q5),
(@q5_1, 'B-트리는 모든 데이터를 메모리에만 올려 두어 디스크 접근이 아예 없기 때문이다.', 2, FALSE, 'B-트리는 대용량 데이터를 디스크에 두고 인덱싱하는 구조이므로 디스크 접근이 아예 없다는 서술은 틀렸다.', @q5),
(@q5_1, 'B-트리는 이진 탐색 트리보다 항상 노드 수가 적어 메모리를 덜 쓰기 때문이다.', 3, FALSE, '선호 이유의 핵심은 노드 수나 메모리가 아니라 낮은 트리 높이로 디스크 접근 횟수를 줄이는 데 있다.', @q5),
(@q5_1, 'B-트리는 정렬을 유지하지 않아 삽입이 더 빠르기 때문이다.', 4, FALSE, 'B-트리는 키를 정렬된 상태로 유지하는 탐색 트리이므로 정렬을 유지하지 않는다는 서술은 틀렸다.', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, 'B+트리는 내부 노드에만 데이터를 저장하고 리프 노드는 비워 둔다.', 1, FALSE, 'B+트리는 실제 데이터를 리프 노드에 저장하고 내부 노드는 인덱스 역할만 하므로 서술이 반대로 되어 있다.', @q5),
(@q5_2, 'B+트리는 이진 트리이고 B-트리는 다진 트리라는 점이 다르다.', 2, FALSE, 'B+트리와 B-트리 모두 한 노드가 여러 자식을 갖는 다진 트리이므로 이진 트리라는 서술은 틀렸다.', @q5),
(@q5_2, 'B+트리는 균형을 맞추지 않지만 B-트리는 균형을 맞춘다.', 3, FALSE, 'B+트리도 B-트리처럼 모든 리프가 같은 깊이가 되도록 균형을 유지하므로 균형을 맞추지 않는다는 서술은 틀렸다.', @q5),
(@q5_2, '데이터를 모두 리프 노드에만 저장하고 리프들이 연결 리스트로 이어져 있어 범위 검색·순차 스캔에 유리하다.', 4, TRUE, '', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '한쪽으로 편향될 수 있어 최악의 경우 O(n)이 걸린다.', 1, FALSE, 'B-트리는 항상 균형을 유지해 높이가 O(log n)이므로 O(n)까지 편향되지 않는다.', @q5),
(@q5_3, '노드마다 자식이 많아 탐색이 O(n log n)으로 느려진다.', 2, FALSE, '노드 내 키 탐색 비용을 포함해도 전체 탐색은 O(log n)이며 O(n log n)이 아니다.', @q5),
(@q5_3, '균형이 유지되어 높이가 O(log n)이므로 탐색도 O(log n)에 수행된다.', 3, TRUE, '', @q5),
(@q5_3, '모든 리프를 순회해야 하므로 O(리프 수)가 걸린다.', 4, FALSE, '탐색은 루트에서 리프로 한 경로만 따라 내려가므로 모든 리프를 순회하지 않는다.', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, '초과된 키를 그냥 버리고 삽입을 취소한다.', 1, FALSE, 'B-트리는 키를 버리지 않고 노드를 분할해 삽입을 완료하므로 삽입을 취소한다는 서술은 틀렸다.', @q5),
(@q5_4, '가득 찬 노드를 둘로 쪼개고 가운데 키를 부모로 올리며, 이를 통해 모든 리프가 같은 깊이를 유지하도록 균형을 보존한다.', 2, TRUE, '', @q5),
(@q5_4, '노드를 그대로 두고 새 키를 별도의 오버플로 영역에 무한히 쌓는다.', 3, FALSE, 'B-트리는 오버플로 영역에 쌓지 않고 노드 분할로 처리하여 각 노드의 키 개수 제한을 지킨다.', @q5),
(@q5_4, '트리 전체를 처음부터 다시 만든다.', 4, FALSE, '노드 분할은 국소적으로 이뤄지고 필요 시 위로 전파될 뿐 트리 전체를 재구성하지 않는다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, 'B-트리'), (@q5, '다진 탐색 트리'),
(@q5_1, 'B-트리'), (@q5_1, '디스크 인덱스'),
(@q5_2, 'B+트리'),
(@q5_3, '시간복잡도'),
(@q5_4, '노드 분할');


-- ============================================================
-- 카테고리: OS (25문항)
-- ============================================================
-- OS 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로세스와 스레드의 근본적인 차이', '프로세스(Process)와 스레드(Thread)의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '프로세스는 실행 중인 프로그램으로 운영체제로부터 독립된 메모리 공간(코드/데이터/힙/스택)을 할당받는 자원 할당 단위이고, 스레드는 그 프로세스 내부에서 코드·데이터·힙 영역을 공유하면서 스택과 레지스터만 별도로 갖는 실행 흐름 단위다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로세스와 스레드를 구분하는 기준', '프로세스와 스레드를 서로 다른 개념으로 구분하는 가장 핵심적인 기준으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '프로세스는 메모리·파일 등 자원을 할당받는 자원 할당의 단위이고, 스레드는 그 자원을 사용하며 CPU를 점유해 코드를 실행하는 실행(디스패치)의 단위라는 점에서 역할이 구분된다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스레드 간 공유되는 메모리 영역', '같은 프로세스에 속한 스레드들이 서로 공유하는 메모리 영역으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '같은 프로세스의 스레드들은 코드(Text)·데이터·힙 영역을 공유하므로 전역 변수나 동적 할당 객체를 함께 접근할 수 있고, 스택과 레지스터·프로그램 카운터만 스레드마다 별도로 가진다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스레드가 스택을 별도로 갖는 이유', '스레드가 힙·데이터 영역은 공유하면서도 스택만은 각자 별도로 갖는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '스택에는 함수 호출 프레임과 지역 변수, 복귀 주소가 쌓이는데 스레드마다 실행 흐름과 호출 경로가 다르므로, 각자 독립된 스택을 가져야 서로의 함수 호출·지역 변수를 침범하지 않고 독립적으로 실행될 수 있다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('멀티스레드가 멀티프로세스보다 갖는 이점', '동일한 작업을 멀티프로세스 대신 멀티스레드로 처리할 때 얻는 주요 이점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '스레드는 프로세스 내부 자원을 공유하므로 생성 비용과 문맥 교환 비용이 프로세스보다 작고, 별도의 IPC 없이 공유 메모리로 손쉽게 데이터를 주고받을 수 있어 응답성과 자원 효율이 높다.');
SET @q1_4 = LAST_INSERT_ID();

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '프로세스와 스레드는 완전히 동일한 개념이며 부르는 용어만 다를 뿐 구조적 차이는 없다.', 1, FALSE, '프로세스는 자원 할당 단위이고 스레드는 그 자원을 사용하는 실행 단위로, 메모리 공유 범위가 명확히 다르므로 동일한 개념이 아니다.', @q1_1),
(@q1, '프로세스는 독립된 메모리 공간을 할당받고, 스레드는 프로세스 안에서 코드·데이터·힙 영역을 공유하며 스택과 레지스터만 별도로 갖는 실행 흐름이다.', 2, TRUE, '', @q1_2),
(@q1, '스레드는 각자 완전히 독립된 메모리 공간을 가지므로 같은 프로세스의 다른 스레드와는 어떤 데이터도 공유할 수 없다.', 3, FALSE, '스레드는 코드·데이터·힙 영역을 공유하며, 완전히 독립된 메모리 공간을 갖는 것은 프로세스다.', @q1_3),
(@q1, '하나의 프로세스는 반드시 하나의 스레드만 가질 수 있어 프로세스 내부에서의 병렬 처리는 불가능하다.', 4, FALSE, '하나의 프로세스는 여러 스레드를 가질 수 있으며(멀티스레드), 이를 통해 프로세스 내부 병렬 처리가 가능하다.', @q1_4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '프로세스와 스레드는 실행 속도만 다를 뿐 담당하는 역할은 완전히 같다.', 1, FALSE, '둘은 속도 차이가 아니라 자원 할당 단위와 실행 단위라는 역할 자체가 다르다.', @q1),
(@q1_1, '프로세스는 실행 단위이고 스레드는 자원 할당 단위다.', 2, FALSE, '설명이 반대다. 프로세스가 자원 할당 단위, 스레드가 실행 단위다.', @q1),
(@q1_1, '프로세스는 메모리·파일 등 자원을 할당받는 자원 할당의 단위이고, 스레드는 그 자원을 사용해 CPU를 점유하며 코드를 실행하는 실행의 단위다.', 3, TRUE, '', @q1),
(@q1_1, '프로세스와 스레드의 구분 기준은 오직 우선순위 숫자의 크기다.', 4, FALSE, '우선순위는 스케줄링을 위한 값일 뿐 프로세스와 스레드를 구분하는 기준이 아니다.', @q1);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '스레드들은 코드·데이터·힙 영역을 공유하고, 스택과 레지스터·프로그램 카운터만 각자 별도로 가진다.', 1, TRUE, '', @q1),
(@q1_2, '스레드들은 스택을 공유하고 힙만 각자 별도로 가진다.', 2, FALSE, '공유·비공유가 반대다. 힙은 공유되고 스택은 스레드마다 별도다.', @q1),
(@q1_2, '스레드들은 어떤 영역도 공유하지 않고 모든 메모리를 독립적으로 갖는다.', 3, FALSE, '모든 메모리를 독립적으로 갖는 것은 프로세스이며, 스레드는 코드·데이터·힙을 공유한다.', @q1),
(@q1_2, '스레드들은 프로그램 카운터까지 하나를 공유해 항상 같은 명령을 동시에 실행한다.', 4, FALSE, '프로그램 카운터는 스레드마다 별도로 가지므로 각 스레드는 서로 다른 명령을 실행할 수 있다.', @q1);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '스택은 운영체제 커널이 관리하므로 사용자 스레드가 접근할 수 없기 때문이다.', 1, FALSE, '스택은 각 스레드의 사용자 실행 흐름을 위한 공간이며, 커널만 접근하는 영역이라 별도로 두는 것이 아니다.', @q1),
(@q1_3, '스택은 크기가 힙보다 훨씬 크기 때문에 공유하면 메모리가 부족하기 때문이다.', 2, FALSE, '스택을 별도로 두는 이유는 크기 문제가 아니라 각 스레드의 독립적인 함수 호출 흐름 때문이다.', @q1),
(@q1_3, '모든 스레드가 항상 동일한 함수만 순서대로 실행하도록 강제하기 위해서다.', 3, FALSE, '스레드는 서로 다른 함수를 독립적으로 실행할 수 있으며, 스택을 별도로 두는 것은 오히려 그 독립 실행을 가능하게 하기 위함이다.', @q1),
(@q1_3, '스택에는 함수 호출 프레임·지역 변수·복귀 주소가 쌓이고 스레드마다 호출 경로가 다르므로, 각자 독립된 스택이 있어야 서로의 실행 흐름을 침범하지 않고 독립적으로 동작할 수 있기 때문이다.', 4, TRUE, '', @q1);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '멀티스레드는 각 스레드가 완전히 격리되어 있어 한 스레드의 오류가 절대 다른 스레드에 영향을 주지 않는다.', 1, FALSE, '스레드는 메모리를 공유하므로 한 스레드의 잘못된 메모리 접근이 프로세스 전체에 영향을 줄 수 있어, 오히려 안정성 측면은 멀티프로세스가 유리하다.', @q1),
(@q1_4, '스레드는 프로세스 내부 자원을 공유하므로 생성·문맥 교환 비용이 작고, 별도 IPC 없이 공유 메모리로 데이터를 주고받을 수 있어 자원 효율과 응답성이 높다.', 2, TRUE, '', @q1),
(@q1_4, '멀티스레드는 항상 CPU 코어 수보다 많은 스레드를 만들수록 성능이 선형으로 증가한다.', 3, FALSE, '스레드 수가 코어 수를 넘어서면 문맥 교환 비용이 늘어 오히려 성능이 저하될 수 있으므로 선형 증가하지 않는다.', @q1),
(@q1_4, '멀티스레드는 데이터를 공유하지 않으므로 동기화 문제가 전혀 발생하지 않는다.', 4, FALSE, '스레드는 힙·데이터를 공유하므로 경쟁 상태가 생길 수 있어 오히려 동기화가 반드시 필요하다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, '프로세스'), (@q1, '스레드'),
(@q1_1, '실행 단위'), (@q1_1, '자원 할당 단위'),
(@q1_2, '메모리 공유'),
(@q1_3, '스레드 스택'),
(@q1_4, '멀티스레드');


-- OS 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('선점형과 비선점형 스케줄링의 차이', 'CPU 스케줄링에서 선점형(Preemptive)과 비선점형(Non-preemptive) 방식의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '선점형은 우선순위가 높은 프로세스가 등장하거나 타임 슬라이스가 만료되면 실행 중인 프로세스로부터 CPU를 강제로 회수할 수 있고, 비선점형은 프로세스가 CPU를 자발적으로 반납(종료·대기)할 때까지 실행을 보장한다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('라운드 로빈이 선점형 스케줄링인 이유', '라운드 로빈(Round Robin) 스케줄링이 선점형으로 분류되는 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '각 프로세스에 동일한 타임 퀀텀(time quantum)을 부여하고, 그 시간이 만료되면 작업이 끝나지 않았더라도 CPU를 회수해 준비 큐 맨 뒤로 보내기 때문에, 실행 중인 프로세스의 의사와 무관하게 CPU를 빼앗는 선점형이다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('FCFS 스케줄링과 콘보이 효과', '비선점형인 FCFS(First-Come-First-Served) 스케줄링에서 발생하기 쉬운 대표적인 문제로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '먼저 도착한 실행 시간이 긴 프로세스가 CPU를 오래 점유하면 뒤에 도착한 짧은 프로세스들이 오래 기다리게 되는 콘보이 효과(Convoy Effect)가 발생해 평균 대기 시간이 길어진다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('선점형 스케줄링의 문맥 교환 오버헤드', '선점형 스케줄링이 응답성을 높이는 대신 감수해야 하는 비용으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '실행 중인 프로세스로부터 CPU를 자주 회수하면 그때마다 레지스터·PCB 등 상태를 저장하고 복원하는 문맥 교환(Context Switching)이 빈번하게 일어나므로, 그만큼 순수 계산에 쓰이지 못하는 오버헤드가 늘어난다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('SJF 스케줄링과 기아 문제', '최단 작업 우선(SJF, Shortest Job First) 스케줄링의 특징과 한계로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 'SJF는 실행 시간이 가장 짧은 프로세스를 먼저 처리해 평균 대기 시간을 최소화하지만, 짧은 작업이 계속 도착하면 실행 시간이 긴 프로세스가 무한정 밀리는 기아(Starvation) 문제가 생길 수 있고, 실행 시간을 미리 정확히 알기 어렵다는 한계가 있다.');
SET @q2_4 = LAST_INSERT_ID();

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, '비선점형은 실행 중인 프로세스를 언제든 강제로 중단시키고 다른 프로세스에 CPU를 넘길 수 있는 방식이다.', 1, FALSE, '실행 중인 프로세스를 강제로 중단시키는 것은 선점형의 특징이며, 비선점형은 자발적 반납을 기다린다.', @q2_1),
(@q2, '선점형과 비선점형 모두 한 번 CPU를 얻은 프로세스가 스스로 반납할 때까지 절대 중단되지 않는다.', 2, FALSE, '스스로 반납할 때까지 실행을 보장하는 것은 비선점형뿐이며, 선점형은 CPU를 회수할 수 있다.', @q2_2),
(@q2, '선점형 스케줄링은 문맥 교환이 전혀 발생하지 않아 오버헤드가 없다.', 3, FALSE, '선점형은 CPU를 자주 회수하므로 오히려 문맥 교환이 빈번하게 발생해 오버헤드가 있다.', @q2_3),
(@q2, '선점형은 우선순위가 높은 프로세스 등장이나 타임 슬라이스 만료 시 CPU를 회수할 수 있고, 비선점형은 프로세스가 자발적으로 반납할 때까지 실행을 보장한다.', 4, TRUE, '', @q2_4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '각 프로세스에 동일한 타임 퀀텀을 주고 시간이 만료되면 작업이 끝나지 않았어도 CPU를 회수해 준비 큐 뒤로 보내기 때문이다.', 1, TRUE, '', @q2),
(@q2_1, '프로세스가 작업을 완전히 끝낼 때까지 CPU를 계속 점유하도록 보장하기 때문이다.', 2, FALSE, '작업 완료까지 CPU를 보장하는 것은 비선점형의 특징이며, 라운드 로빈은 시간이 되면 CPU를 회수한다.', @q2),
(@q2_1, '우선순위가 가장 높은 하나의 프로세스만 계속 실행하기 때문이다.', 3, FALSE, '라운드 로빈은 우선순위가 아니라 동일한 시간 할당을 순환하는 방식이라 설명이 맞지 않는다.', @q2),
(@q2_1, '실행 시간이 가장 짧은 프로세스를 먼저 처리하기 때문이다.', 4, FALSE, '가장 짧은 작업을 먼저 처리하는 것은 SJF의 특징이며 라운드 로빈과 무관하다.', @q2);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, 'FCFS는 타임 퀀텀마다 CPU를 회수하므로 문맥 교환이 지나치게 잦아지는 문제가 있다.', 1, FALSE, '타임 퀀텀으로 CPU를 회수하는 것은 라운드 로빈이며, FCFS는 비선점형이라 그런 회수가 없다.', @q2),
(@q2_2, 'FCFS는 우선순위가 낮은 프로세스가 영원히 실행되지 못하는 기아 현상이 필연적으로 발생한다.', 2, FALSE, 'FCFS는 도착 순서대로 처리하므로 결국 모든 프로세스가 실행되어 기아가 필연적이지 않다. 기아는 우선순위·SJF 계열에서 두드러진다.', @q2),
(@q2_2, '먼저 도착한 실행 시간이 긴 프로세스가 CPU를 오래 점유하면 뒤의 짧은 프로세스들이 오래 기다리게 되는 콘보이 효과가 발생한다.', 3, TRUE, '', @q2),
(@q2_2, 'FCFS는 프로세스의 실행 시간을 미리 알아야만 스케줄링이 가능하다.', 4, FALSE, 'FCFS는 도착 순서만으로 스케줄링하므로 실행 시간을 미리 알 필요가 없다. 실행 시간 예측이 필요한 것은 SJF다.', @q2);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '선점이 잦으면 프로세스의 실행 시간 자체가 길어져 프로그램 로직이 느려진다.', 1, FALSE, '문맥 교환은 프로그램 로직의 실행 시간을 늘리는 것이 아니라, 상태 저장·복원에 별도의 CPU 시간을 소모하는 오버헤드를 유발한다.', @q2),
(@q2_3, 'CPU를 자주 회수할수록 레지스터·PCB 등 상태를 저장·복원하는 문맥 교환이 빈번해져, 순수 계산에 쓰이지 못하는 오버헤드가 늘어난다.', 2, TRUE, '', @q2),
(@q2_3, '선점형은 문맥 교환이 하드웨어에서 자동 처리되어 어떤 비용도 발생시키지 않는다.', 3, FALSE, '문맥 교환은 상태 저장·복원과 캐시 무효화 등 실질적인 비용을 수반하므로 무비용이 아니다.', @q2),
(@q2_3, '선점형은 문맥 교환이 없으므로 대신 메모리 사용량만 늘어난다.', 4, FALSE, '선점형은 문맥 교환이 오히려 잦으며, 문제의 핵심 비용도 바로 그 문맥 교환 오버헤드다.', @q2);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, 'SJF는 도착 순서대로만 처리하므로 평균 대기 시간이 항상 가장 길다.', 1, FALSE, 'SJF는 도착 순서가 아니라 실행 시간이 짧은 순으로 처리하며, 평균 대기 시간을 최소화하는 특성이 있다.', @q2),
(@q2_4, 'SJF는 모든 프로세스에 동일한 시간을 배분하므로 기아가 절대 발생하지 않는다.', 2, FALSE, '동일 시간 배분은 라운드 로빈의 특성이며, SJF는 긴 작업이 계속 밀려 오히려 기아가 발생할 수 있다.', @q2),
(@q2_4, 'SJF는 실행 시간이 긴 프로세스를 우선 처리해 처리량을 극대화한다.', 3, FALSE, 'SJF는 짧은 작업을 우선 처리하며, 긴 작업을 우선하는 방식이 아니다.', @q2),
(@q2_4, 'SJF는 짧은 작업을 먼저 처리해 평균 대기 시간을 최소화하지만, 긴 작업이 무한정 밀리는 기아가 생길 수 있고 실행 시간을 미리 정확히 알기 어렵다는 한계가 있다.', 4, TRUE, '', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, 'CPU 스케줄링'), (@q2, '선점형 스케줄링'),
(@q2_1, '라운드 로빈'), (@q2_1, '타임 퀀텀'),
(@q2_2, 'FCFS'), (@q2_2, '콘보이 효과'),
(@q2_3, '문맥 교환'),
(@q2_4, 'SJF'), (@q2_4, '기아');


-- OS 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('뮤텍스와 세마포어의 차이', '동기화 도구인 뮤텍스(Mutex)와 세마포어(Semaphore)의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '뮤텍스는 잠금을 획득한 스레드만 해제할 수 있는 소유권(ownership) 개념이 있는 상호배제 도구이고, 세마포어는 카운터로 동시에 접근 가능한 자원 개수를 제어하며 소유 개념 없이 다른 스레드도 신호(signal)를 줄 수 있다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('뮤텍스의 소유권 개념', '뮤텍스가 가진 소유권(ownership) 개념이 의미하는 바로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '뮤텍스는 잠금을 획득(lock)한 스레드만이 그 잠금을 해제(unlock)할 수 있으며, 잠금을 갖지 않은 다른 스레드가 임의로 해제할 수 없다는 점에서 소유권 개념을 가진다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('카운팅 세마포어의 용도', '카운팅 세마포어(Counting Semaphore)가 이진 세마포어와 달리 유용하게 쓰이는 상황으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '카운팅 세마포어는 초기값을 N으로 두어 동시에 접근 가능한 동일 자원이 여러 개(예: 커넥션 풀의 커넥션 N개)일 때, 최대 N개의 스레드가 동시에 자원을 사용하도록 제어하는 데 적합하다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('임계 영역의 정의', '동기화에서 임계 영역(Critical Section)이 의미하는 바로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '임계 영역은 둘 이상의 스레드/프로세스가 공유 자원에 접근하는 코드 구간으로, 동시에 실행되면 경쟁 상태가 발생할 수 있어 한 번에 하나만 진입하도록 상호배제로 보호해야 하는 부분이다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('모니터가 동기화를 제공하는 방식', '모니터(Monitor)가 뮤텍스·세마포어와 달리 동기화를 제공하는 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '모니터는 공유 자원과 그 자원을 다루는 프로시저를 하나로 캡슐화하고, 프로그래밍 언어·런타임 차원에서 한 번에 하나의 스레드만 모니터 내부에 진입하도록 상호배제를 자동 보장하며, 조건 변수(condition variable)로 대기·통지를 처리한다.');
SET @q3_4 = LAST_INSERT_ID();

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, '뮤텍스는 잠금을 획득한 스레드만 해제할 수 있는 상호배제 도구이고, 세마포어는 카운터로 자원 개수를 제어하며 소유 개념 없이 다른 스레드도 신호를 줄 수 있다.', 1, TRUE, '', @q3_1),
(@q3, '세마포어는 항상 0과 1 두 값만 가질 수 있어 뮤텍스와 완전히 동일하며 아무런 차이가 없다.', 2, FALSE, '0과 1만 갖는 것은 이진 세마포어의 경우이며, 세마포어는 카운터를 N까지 둘 수 있고 소유권 개념 유무에서 뮤텍스와 다르다.', @q3_2),
(@q3, '뮤텍스는 여러 스레드가 동시에 임계 영역에 진입하는 것을 허용하는 도구다.', 3, FALSE, '뮤텍스는 한 번에 하나의 스레드만 임계 영역에 진입하도록 상호배제하는 도구다.', @q3_3),
(@q3, '뮤텍스와 세마포어는 모두 임계 영역 보호와 무관한 동적 메모리 할당 전용 도구다.', 4, FALSE, '둘 다 동기화(상호배제·자원 접근 제어) 도구이며 메모리 할당과는 관련이 없다.', @q3_4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '어떤 스레드든 자유롭게 잠금을 획득하고 해제할 수 있어 순서에 제약이 없다는 뜻이다.', 1, FALSE, '아무나 해제할 수 있는 것은 세마포어에 가깝고, 뮤텍스는 잠금을 획득한 스레드만 해제할 수 있다.', @q3),
(@q3_1, '뮤텍스가 자원의 물리적 소유권을 운영체제로부터 완전히 넘겨받는다는 뜻이다.', 2, FALSE, '소유권은 물리 자원 이전이 아니라 잠금을 해제할 수 있는 주체가 정해져 있다는 논리적 개념이다.', @q3),
(@q3_1, '잠금을 획득한 스레드만이 그 잠금을 해제할 수 있고, 잠금을 갖지 않은 다른 스레드는 임의로 해제할 수 없다는 뜻이다.', 3, TRUE, '', @q3),
(@q3_1, '뮤텍스로 보호되는 자원은 한 번 잠기면 프로그램이 종료될 때까지 해제되지 않는다는 뜻이다.', 4, FALSE, '뮤텍스는 임계 영역을 벗어날 때 잠금을 해제하며, 종료까지 계속 잠겨 있는 것이 아니다.', @q3);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '오직 하나의 스레드만 자원에 접근하도록 완전한 상호배제를 걸어야 할 때 쓰인다.', 1, FALSE, '단 하나만 접근하도록 하는 것은 이진 세마포어(또는 뮤텍스)의 역할이며, 카운팅 세마포어의 차별점이 아니다.', @q3),
(@q3_2, '동시에 접근 가능한 동일 자원이 여러 개(예: 커넥션 풀의 커넥션 N개)일 때, 최대 N개의 스레드가 동시에 사용하도록 제어할 때 쓰인다.', 2, TRUE, '', @q3),
(@q3_2, '카운터를 음수로 두어 자원을 사용할수록 값을 증가시키는 데 쓰인다.', 3, FALSE, '세마포어는 자원을 사용할 때 값을 감소시키고 반납할 때 증가시키며, 음수 초기값으로 동작시키는 용도가 아니다.', @q3),
(@q3_2, '메모리 누수를 자동으로 회수하는 가비지 컬렉션 용도로 쓰인다.', 4, FALSE, '세마포어는 동기화 도구이며 가비지 컬렉션과는 무관하다.', @q3);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '프로그램에서 절대 실행되면 안 되는 예외 처리 전용 코드 구간을 뜻한다.', 1, FALSE, '임계 영역은 실행되면 안 되는 구간이 아니라, 공유 자원에 접근하므로 상호배제로 보호해야 하는 구간이다.', @q3),
(@q3_3, 'CPU가 가장 빠르게 실행할 수 있도록 최적화된 코드 구간을 뜻한다.', 2, FALSE, '임계 영역은 성능 최적화 구간이 아니라 공유 자원 접근으로 인한 경쟁 상태를 막아야 하는 구간이다.', @q3),
(@q3_3, '오직 커널 모드에서만 실행되는 시스템 콜 코드 구간을 뜻한다.', 3, FALSE, '임계 영역은 커널 모드 여부와 무관하며, 사용자 코드에서도 공유 자원을 다루는 구간이 임계 영역이 된다.', @q3),
(@q3_3, '둘 이상의 스레드가 공유 자원에 접근하는 코드 구간으로, 동시에 실행되면 경쟁 상태가 발생할 수 있어 한 번에 하나만 진입하도록 보호해야 하는 부분이다.', 4, TRUE, '', @q3);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '모니터는 공유 자원과 프로시저를 캡슐화하고 언어·런타임 차원에서 한 번에 하나의 스레드만 진입하도록 상호배제를 자동 보장하며, 조건 변수로 대기·통지를 처리한다.', 1, TRUE, '', @q3),
(@q3_4, '모니터는 개발자가 매번 직접 lock/unlock을 호출해야만 상호배제가 동작한다.', 2, FALSE, '모니터의 상호배제는 언어·런타임이 자동 보장하며, 개발자가 매번 수동으로 잠금을 걸어야 하는 방식은 아니다.', @q3),
(@q3_4, '모니터는 하드웨어 인터럽트를 비활성화하는 방식으로만 동기화를 구현한다.', 3, FALSE, '인터럽트 비활성화는 단일 프로세서의 저수준 기법이며, 모니터는 고수준 언어 구조로 동기화를 제공한다.', @q3),
(@q3_4, '모니터는 임계 영역 보호 기능이 없어 세마포어와 반드시 함께 써야만 동작한다.', 4, FALSE, '모니터 자체가 상호배제를 제공하므로 세마포어와 반드시 병행해야만 동작하는 것은 아니다.', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, '뮤텍스'), (@q3, '세마포어'),
(@q3_1, '소유권'), (@q3_1, '상호배제'),
(@q3_2, '카운팅 세마포어'),
(@q3_3, '임계 영역'),
(@q3_4, '모니터'), (@q3_4, '조건 변수');


-- OS 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락 발생의 4가지 필요조건', '교착 상태(Deadlock)가 발생하기 위한 필요조건에 대한 설명으로 옳은 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '데드락은 상호배제, 점유와 대기(Hold and Wait), 비선점(No Preemption), 원형 대기(Circular Wait)라는 네 조건이 동시에 모두 성립할 때만 발생하며, 이 중 하나라도 성립하지 않게 만들면 데드락을 예방할 수 있다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락의 비선점 조건', '데드락 필요조건 중 비선점(No Preemption)이 의미하는 바로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '비선점은 어떤 프로세스가 이미 점유한 자원을 다른 프로세스가 강제로 빼앗을 수 없고, 오직 그 프로세스가 스스로 다 쓰고 반납할 때까지 기다려야 한다는 조건이다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락의 상호배제 조건', '데드락 필요조건 중 상호배제(Mutual Exclusion)가 의미하는 바로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '상호배제는 한 번에 하나의 프로세스만 자원을 사용할 수 있고 다른 프로세스는 그 자원을 동시에 쓸 수 없는 상태를 말하며, 자원 자체가 공유 불가능해야 데드락의 전제가 성립한다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('원형 대기를 깨는 예방 기법', '데드락의 원형 대기(Circular Wait) 조건을 무너뜨려 데드락을 예방하는 기법으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '모든 자원에 고유한 순서(번호)를 부여하고 프로세스가 항상 오름차순으로만 자원을 요청하도록 강제하면, 자원을 서로 물고 도는 순환 형태가 만들어질 수 없어 원형 대기 조건이 깨진다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('은행원 알고리즘과 데드락 회피', '데드락 회피(Avoidance) 기법인 은행원 알고리즘(Banker''s Algorithm)의 동작 원리로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '은행원 알고리즘은 자원을 할당하기 전에 그 할당이 시스템을 안전 상태(safe state)로 유지하는지를 미리 검사하여, 안전한 경우에만 할당하고 그렇지 않으면 대기시켜 데드락을 회피한다.');
SET @q4_4 = LAST_INSERT_ID();

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '상호배제, 점유와 대기, 선점 가능, 원형 대기 중 하나라도 성립하면 데드락이 발생한다.', 1, FALSE, '조건은 선점 가능이 아니라 비선점이며, 네 조건 중 하나만 성립해서가 아니라 모두 동시에 성립해야 데드락이 발생한다.', @q4_1),
(@q4, '상호배제 조건 하나만 만족하면 나머지와 무관하게 데드락이 항상 발생한다.', 2, FALSE, '상호배제만으로는 데드락이 성립하지 않으며, 네 조건이 동시에 모두 만족되어야 한다.', @q4_2),
(@q4, '상호배제, 점유와 대기, 비선점, 원형 대기라는 네 조건이 동시에 모두 성립할 때 발생하며, 이 중 하나라도 깨면 예방할 수 있다.', 3, TRUE, '', @q4_3),
(@q4, '데드락은 CPU 스케줄링 알고리즘을 바꾸기만 하면 필요조건과 무관하게 원천적으로 사라진다.', 4, FALSE, '데드락은 자원 할당 구조에서 비롯되므로 스케줄링 알고리즘 변경만으로 원천 제거되지 않는다.', @q4_4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '어떤 프로세스가 점유한 자원을 운영체제가 필요할 때 언제든 강제로 회수할 수 있다는 조건이다.', 1, FALSE, '이는 비선점의 반대인 선점 가능 상황이며, 데드락 조건인 비선점의 의미와 반대다.', @q4),
(@q4_1, '어떤 프로세스가 이미 점유한 자원을 다른 프로세스가 강제로 빼앗을 수 없고, 스스로 반납할 때까지 기다려야 한다는 조건이다.', 2, TRUE, '', @q4),
(@q4_1, '모든 프로세스가 동시에 같은 자원을 공유해서 사용할 수 있다는 조건이다.', 3, FALSE, '동시 공유가 가능하다는 것은 상호배제가 없는 상황이며, 비선점의 의미가 아니다.', @q4),
(@q4_1, '프로세스가 자원을 하나도 점유하지 않은 상태에서만 새 자원을 요청할 수 있다는 조건이다.', 4, FALSE, '이는 점유와 대기 조건을 깨는 예방 기법에 가깝고, 비선점 조건의 정의가 아니다.', @q4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '여러 프로세스가 순환 형태로 서로의 자원을 기다린다는 조건이다.', 1, FALSE, '이는 원형 대기 조건에 대한 설명이며, 상호배제의 정의가 아니다.', @q4),
(@q4_2, '프로세스가 자원을 점유한 채로 다른 자원을 추가로 기다린다는 조건이다.', 2, FALSE, '이는 점유와 대기(Hold and Wait) 조건에 대한 설명이며, 상호배제와 다르다.', @q4),
(@q4_2, '이미 점유된 자원을 강제로 빼앗을 수 없다는 조건이다.', 3, FALSE, '이는 비선점 조건에 대한 설명이며, 상호배제의 정의가 아니다.', @q4),
(@q4_2, '한 번에 하나의 프로세스만 자원을 사용할 수 있고 다른 프로세스는 동시에 그 자원을 쓸 수 없는 상태를 말한다.', 4, TRUE, '', @q4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, '모든 자원에 고유한 순서(번호)를 부여하고 항상 오름차순으로만 자원을 요청하도록 강제해 자원을 물고 도는 순환이 생기지 않게 한다.', 1, TRUE, '', @q4),
(@q4_3, '모든 프로세스가 실행 전에 필요한 자원을 하나도 확보하지 못하게 막는다.', 2, FALSE, '자원 확보를 원천 봉쇄하면 프로세스가 실행될 수 없으며, 이는 원형 대기를 깨는 기법이 아니다.', @q4),
(@q4_3, '자원 사용 중인 프로세스를 무조건 강제 종료시켜 자원을 회수한다.', 3, FALSE, '강제 종료는 비선점 조건을 깨는 회복(recovery)에 가까우며 원형 대기 예방 기법이 아니다.', @q4),
(@q4_3, '자원을 요청 순서와 무관하게 무작위로 할당해 예측 불가능하게 만든다.', 4, FALSE, '무작위 할당은 오히려 순환 대기를 유발할 수 있어 원형 대기를 깨는 방법이 아니다.', @q4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '데드락이 이미 발생한 뒤에 프로세스를 강제 종료해 순환을 끊는 사후 회복 방식이다.', 1, FALSE, '은행원 알고리즘은 데드락 발생 후 회복이 아니라, 할당 전에 검사해 회피하는 예방적 회피 기법이다.', @q4),
(@q4_4, '자원 요청을 무조건 즉시 허용하고 문제가 생기면 그때 처리하는 방식이다.', 2, FALSE, '은행원 알고리즘은 무조건 허용이 아니라 안전 상태를 유지할 때만 허용한다.', @q4),
(@q4_4, '자원을 할당하기 전에 그 할당이 시스템을 안전 상태로 유지하는지 미리 검사하여, 안전한 경우에만 할당하고 아니면 대기시킨다.', 3, TRUE, '', @q4),
(@q4_4, '모든 자원에 번호를 매겨 오름차순으로만 요청하게 강제하는 방식이다.', 4, FALSE, '이는 원형 대기를 깨는 예방 기법이며, 안전 상태를 검사하는 은행원 알고리즘의 원리와 다르다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, '데드락'), (@q4, '필요조건'),
(@q4_1, '비선점'),
(@q4_2, '상호배제'),
(@q4_3, '원형 대기'), (@q4_3, '데드락 예방'),
(@q4_4, '은행원 알고리즘'), (@q4_4, '데드락 회피');


-- OS 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('페이징이 외부 단편화를 해결하는 원리', '페이징(Paging) 기법이 외부 단편화(External Fragmentation)를 발생시키지 않는 근본적인 이유로 옳은 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '페이징은 논리 메모리와 물리 메모리를 동일한 고정 크기의 페이지/프레임으로 나누고, 페이지 테이블을 통해 논리적으로 연속된 페이지를 물리적으로 임의의 프레임에 흩어 배치할 수 있어, 남은 공간이 조각나 쓰이지 못하는 외부 단편화가 발생하지 않는다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('세그먼테이션의 특징', '세그먼테이션(Segmentation)이 페이징과 구별되는 특징으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '세그먼테이션은 프로그램을 코드·데이터·스택 등 논리적 의미 단위인 가변 크기의 세그먼트로 나누어 관리하므로 논리 구조를 잘 반영하지만, 크기가 제각각이라 빈 공간이 조각나는 외부 단편화가 발생할 수 있다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('페이지 테이블의 역할', '페이징 기법에서 페이지 테이블(Page Table)이 담당하는 핵심 역할로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'OS',
 '페이지 테이블은 프로세스의 논리 주소상 페이지 번호를 실제 물리 메모리의 프레임 번호로 변환(매핑)하는 정보를 담고 있어, 흩어져 배치된 프레임에도 논리적으로 연속된 것처럼 접근할 수 있게 한다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('페이징에서 내부 단편화가 생기는 이유', '페이징이 외부 단편화는 없애지만 내부 단편화(Internal Fragmentation)는 여전히 발생할 수 있는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '페이지는 고정 크기로 나뉘므로 프로세스의 마지막 페이지가 페이지 크기를 꽉 채우지 못하면 그 페이지 내부에 사용되지 않는 자투리 공간이 남게 되어 내부 단편화가 발생한다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('가상 메모리와 요구 페이징', '가상 메모리(Virtual Memory)에서 요구 페이징(Demand Paging)이 동작하는 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'OS',
 '요구 페이징은 프로세스의 모든 페이지를 미리 올리지 않고, 실제로 참조되는 페이지만 그때그때 물리 메모리에 적재하며, 없는 페이지를 참조하면 페이지 폴트(Page Fault)가 발생해 디스크에서 해당 페이지를 가져온다.');
SET @q5_4 = LAST_INSERT_ID();

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, '페이징은 물리 메모리를 논리적 의미 단위의 가변 크기 블록으로 나누어 세그먼트 단위로 할당하기 때문이다.', 1, FALSE, '가변 크기 논리 단위로 나누는 것은 세그먼테이션이며, 페이징은 고정 크기 페이지/프레임을 사용한다.', @q5_1),
(@q5, '페이징은 프로세스를 반드시 물리 메모리의 연속된 공간에 통째로 할당하기 때문이다.', 2, FALSE, '페이징은 프레임 단위로 흩어 배치할 수 있어 연속 할당이 필요 없으며, 바로 그 점 덕분에 외부 단편화가 없다.', @q5_2),
(@q5, '페이징은 외부 단편화뿐 아니라 내부 단편화까지 모두 완전히 제거하기 때문이다.', 3, FALSE, '페이징은 외부 단편화는 없애지만 마지막 페이지의 자투리로 인한 내부 단편화는 여전히 발생할 수 있다.', @q5_3),
(@q5, '논리·물리 메모리를 동일한 고정 크기 페이지/프레임으로 나누고, 페이지 테이블로 임의의 프레임에 흩어 배치할 수 있어 남은 공간이 조각나지 않기 때문이다.', 4, TRUE, '', @q5_4);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '세그먼테이션은 모든 세그먼트를 동일한 고정 크기로 잘라 관리하므로 외부 단편화가 없다.', 1, FALSE, '고정 크기로 자르는 것은 페이징이며, 세그먼테이션은 가변 크기라 외부 단편화가 생길 수 있다.', @q5),
(@q5_1, '세그먼테이션은 프로그램을 코드·데이터·스택 등 논리적 의미 단위인 가변 크기 세그먼트로 나누므로 논리 구조를 잘 반영하지만, 크기가 제각각이라 외부 단편화가 발생할 수 있다.', 2, TRUE, '', @q5),
(@q5_1, '세그먼테이션은 페이지 테이블이 필요 없고 물리 주소를 논리 주소와 항상 동일하게 사용한다.', 3, FALSE, '세그먼테이션도 세그먼트 테이블로 주소 변환을 하며, 논리 주소와 물리 주소가 동일하지 않다.', @q5),
(@q5_1, '세그먼테이션은 가상 메모리를 지원하지 못해 물리 메모리보다 큰 프로그램을 실행할 수 없다.', 4, FALSE, '세그먼테이션도 가상 메모리 기법과 결합될 수 있으며, 물리 메모리 한계와 직접 연결되는 특징이 아니다.', @q5);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, '논리 주소의 페이지 번호를 실제 물리 메모리의 프레임 번호로 변환하는 매핑 정보를 담아, 흩어진 프레임에도 연속된 것처럼 접근하게 한다.', 1, TRUE, '', @q5),
(@q5_2, '프로세스의 모든 데이터를 통째로 복사해 두었다가 필요할 때 되돌리는 백업 역할을 한다.', 2, FALSE, '페이지 테이블은 데이터 백업이 아니라 주소 변환(페이지 번호→프레임 번호) 정보를 담는 자료구조다.', @q5),
(@q5_2, '페이지 교체가 필요할 때 어떤 페이지를 내보낼지 결정하는 교체 알고리즘 자체를 뜻한다.', 3, FALSE, '교체 대상 선정은 페이지 교체 알고리즘의 역할이며, 페이지 테이블은 주소 매핑을 담당한다.', @q5),
(@q5_2, 'CPU의 캐시 히트율을 높이기 위한 하드웨어 캐시 라인을 저장하는 공간이다.', 4, FALSE, '페이지 테이블은 캐시 라인 저장소가 아니라 논리-물리 주소 매핑 테이블이다.', @q5);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '프레임 사이에 빈 공간이 조각나 흩어지기 때문에 내부 단편화가 생긴다.', 1, FALSE, '프레임 사이에 조각난 빈 공간은 외부 단편화 개념이며, 페이징은 그런 외부 단편화가 없다.', @q5),
(@q5_3, '페이지 크기가 매번 달라져 서로 맞지 않기 때문에 내부 단편화가 생긴다.', 2, FALSE, '페이지 크기는 고정되어 있으며 매번 달라지지 않는다. 내부 단편화의 원인은 마지막 페이지의 자투리다.', @q5),
(@q5_3, '페이지는 고정 크기로 나뉘므로 프로세스의 마지막 페이지가 크기를 꽉 채우지 못하면 그 페이지 내부에 사용되지 않는 자투리 공간이 남기 때문이다.', 3, TRUE, '', @q5),
(@q5_3, '페이지 테이블이 물리 메모리를 너무 많이 차지해 프로세스가 쓸 공간이 줄어들기 때문이다.', 4, FALSE, '페이지 테이블의 메모리 사용은 별개의 오버헤드이며, 내부 단편화의 원인인 마지막 페이지 자투리와는 다르다.', @q5);

INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, '프로세스를 실행하려면 항상 모든 페이지를 먼저 물리 메모리에 올려야만 실행을 시작할 수 있다.', 1, FALSE, '이는 요구 페이징의 반대 개념이다. 요구 페이징은 필요한 페이지만 그때그때 적재한다.', @q5),
(@q5_4, '실제로 참조되는 페이지만 그때그때 물리 메모리에 적재하고, 없는 페이지를 참조하면 페이지 폴트가 발생해 디스크에서 가져온다.', 2, TRUE, '', @q5),
(@q5_4, '페이지 폴트가 발생하면 프로세스를 즉시 강제 종료해 메모리를 회수한다.', 3, FALSE, '페이지 폴트는 정상적인 처리 과정이며 프로세스를 종료시키는 것이 아니라 필요한 페이지를 적재한 뒤 실행을 이어간다.', @q5),
(@q5_4, '요구 페이징은 디스크를 전혀 사용하지 않고 오직 물리 메모리 안에서만 페이지를 관리한다.', 4, FALSE, '요구 페이징은 보조기억장치(디스크)의 스왑 공간과 물리 메모리를 오가며 페이지를 관리한다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, '페이징'), (@q5, '외부 단편화'),
(@q5_1, '세그먼테이션'),
(@q5_2, '페이지 테이블'), (@q5_2, '주소 변환'),
(@q5_3, '내부 단편화'),
(@q5_4, '가상 메모리'), (@q5_4, '요구 페이징');


-- ============================================================
-- 카테고리: DESIGN_PATTERN (25문항)
-- ============================================================
-- DESIGN_PATTERN 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('싱글톤 패턴의 목적', '싱글톤(Singleton) 패턴의 핵심 목적으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '싱글톤은 특정 클래스의 인스턴스가 애플리케이션 전체에서 오직 하나만 생성되도록 보장하고, 그 유일한 인스턴스에 대한 전역 접근점을 제공하는 패턴이다. 주로 설정 관리자, 커넥션 풀처럼 하나만 존재해야 하는 자원에 사용된다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('싱글톤과 멀티스레드 안전성', '지연 초기화(lazy initialization)로 구현한 싱글톤을 멀티스레드 환경에서 아무런 동기화 없이 사용할 때 발생할 수 있는 문제로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DESIGN_PATTERN',
 '여러 스레드가 동시에 getInstance()의 null 검사 구간에 진입하면 인스턴스가 두 개 이상 생성될 수 있다. 이를 막기 위해 synchronized, double-checked locking, 정적 내부 클래스(홀더), enum 등의 기법을 사용한다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('싱글톤 패턴의 대표적 단점', '싱글톤 패턴이 흔히 안티패턴으로 비판받는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '싱글톤은 전역 상태를 만들어 여러 모듈이 은연중에 하나의 인스턴스에 강하게 결합되게 하고, 테스트 시 mock으로 교체하기 어렵게 만들며, 숨겨진 의존성을 유발한다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인스턴스 수 제한과 구현 교체의 차이', '"인스턴스를 하나로 제한하는 것"과 "런타임에 구현체를 교체하는 것"은 서로 다른 설계 관심사다. 이에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '싱글톤은 인스턴스의 개수(하나)를 통제하는 데 관심이 있고, 전략·다형성 기반 패턴은 어떤 구현을 쓸지를 교체하는 데 관심이 있다. 두 관심사는 직교적이어서 하나의 싱글톤이 다형적으로 동작하도록 함께 쓸 수도 있다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('싱글톤 패턴과 DI 컨테이너의 싱글톤 스코프', 'Spring 같은 DI 컨테이너가 관리하는 싱글톤 빈과, 고전적인 GoF 싱글톤 패턴의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 'GoF 싱글톤은 클래스 스스로 private 생성자와 정적 접근자로 인스턴스를 하나로 강제하지만, DI 컨테이너의 싱글톤은 컨테이너가 인스턴스 하나를 관리·주입하는 것이라 클래스 자체는 평범하게 작성할 수 있고 테스트·교체가 더 쉽다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 꼬리질문으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '요청이 들어올 때마다 매번 새로운 인스턴스를 생성해, 객체 간 상태 공유를 원천적으로 차단하기 위한 패턴이다.', 1, FALSE, '매번 새 인스턴스를 만드는 것은 싱글톤의 목적과 정반대다. 싱글톤은 인스턴스를 단 하나만 유지한다.', @q1_1),
(@q1, '특정 클래스의 인스턴스를 애플리케이션 전체에서 오직 하나만 생성하도록 보장하고, 그 유일한 인스턴스에 대한 전역 접근점을 제공한다.', 2, TRUE, '', @q1_2),
(@q1, '하나의 인터페이스를 여러 하위 클래스가 서로 다르게 구현하도록 강제하여, 런타임에 구현체를 교체할 수 있게 하는 패턴이다.', 3, FALSE, '이는 전략 패턴 등 다형성을 활용한 패턴에 대한 설명이며, 인스턴스 개수를 하나로 제한하는 싱글톤과는 다르다.', @q1_3),
(@q1, '객체 생성 코드를 별도의 팩토리 클래스로 분리해, 클라이언트가 구체 클래스를 몰라도 객체를 생성할 수 있게 하는 패턴이다.', 4, FALSE, '이는 팩토리 패턴에 대한 설명이다. 싱글톤의 핵심은 생성 위임이 아니라 인스턴스 수를 하나로 제한하는 것이다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '컴파일 시점에 오류가 발생해 아예 빌드가 되지 않는다.', 1, FALSE, '동기화 누락은 컴파일 오류가 아니라 런타임에 드러나는 논리적 문제다.', @q1),
(@q1_1, '싱글톤 인스턴스가 자동으로 스레드마다 하나씩 생성되어 스레드 안전이 보장된다.', 2, FALSE, '스레드마다 별도 인스턴스를 두는 것은 ThreadLocal의 동작이며, 동기화 없는 싱글톤이 자동으로 그렇게 되지는 않는다.', @q1),
(@q1_1, '인스턴스가 절대 생성되지 않아 항상 null이 반환된다.', 3, FALSE, '인스턴스는 생성되며, 문제는 오히려 중복 생성이다.', @q1),
(@q1_1, '여러 스레드가 동시에 null 검사를 통과해 인스턴스가 두 개 이상 생성될 수 있다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, '전역 상태를 공유하게 되어 모듈 간 결합도가 높아지고, 테스트에서 mock 객체로 교체하기 어려워 단위 테스트가 까다로워진다.', 1, TRUE, '', @q1),
(@q1_2, '인스턴스를 여러 개 만들 수 있어 메모리를 과도하게 사용한다.', 2, FALSE, '싱글톤은 오히려 인스턴스를 하나로 제한하므로, 인스턴스 남발로 인한 메모리 문제와는 반대다.', @q1),
(@q1_2, '객체를 전혀 재사용할 수 없어 매번 새로 생성해야 하는 비효율이 있다.', 3, FALSE, '싱글톤은 인스턴스를 재사용하는 구조이므로 매번 새로 생성한다는 설명은 사실과 다르다.', @q1),
(@q1_2, '상속이 불가능해 어떤 방식으로도 확장할 수 없다.', 4, FALSE, '싱글톤 구현이 확장을 어렵게 할 수는 있으나 상속이 원천적으로 불가능하다는 것은 과장이며, 대표적 비판점은 전역 상태와 테스트 곤란이다.', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '두 관심사는 완전히 동일하며, 싱글톤을 쓰면 자동으로 구현체 교체가 이루어진다.', 1, FALSE, '인스턴스 수 제한과 구현 교체는 별개의 관심사이며, 싱글톤이 자동으로 구현을 교체해 주지는 않는다.', @q1),
(@q1_3, '구현체를 교체하려면 반드시 인스턴스를 매번 새로 생성해야 하므로 싱글톤과 병행할 수 없다.', 2, FALSE, '싱글톤 인스턴스가 내부적으로 다른 전략 객체를 참조하도록 하면 인스턴스를 새로 만들지 않고도 구현 교체가 가능하다.', @q1),
(@q1_3, '인스턴스 수 제한과 구현 교체는 직교적인 관심사여서, 하나의 싱글톤이 다형적으로 동작하도록 함께 설계할 수도 있다.', 3, TRUE, '', @q1),
(@q1_3, '싱글톤은 다형성을 전혀 활용할 수 없어 인터페이스를 구현해서는 안 된다.', 4, FALSE, '싱글톤 클래스도 인터페이스를 구현할 수 있으며 다형성과 배타적이지 않다.', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '둘은 완전히 동일한 개념으로, DI 컨테이너의 싱글톤도 클래스가 private 생성자로 스스로 인스턴스를 제한한다.', 1, FALSE, 'DI 컨테이너의 싱글톤은 컨테이너가 관리하는 것이지, 클래스가 private 생성자로 스스로 제한하는 방식이 아니다.', @q1),
(@q1_4, 'GoF 싱글톤은 클래스가 스스로 인스턴스를 하나로 강제하지만, DI 컨테이너의 싱글톤은 컨테이너가 인스턴스를 하나만 만들어 관리·주입하므로 클래스는 평범하게 작성되어 테스트·교체가 쉽다.', 2, TRUE, '', @q1),
(@q1_4, 'DI 컨테이너의 싱글톤 빈은 요청마다 새로운 인스턴스를 생성하는 프로토타입과 동일하게 동작한다.', 3, FALSE, '싱글톤 스코프와 프로토타입 스코프는 다르다. 싱글톤은 컨테이너당 하나, 프로토타입은 요청마다 새로 생성한다.', @q1),
(@q1_4, 'GoF 싱글톤이 DI 컨테이너 싱글톤보다 항상 테스트하기 쉽다.', 4, FALSE, '오히려 전역 정적 접근에 의존하는 GoF 싱글톤이 mock 교체가 어려워 테스트가 더 까다로운 편이다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, '싱글톤'), (@q1, '생성 패턴'),
(@q1_1, '싱글톤'), (@q1_1, '멀티스레드'),
(@q1_2, '싱글톤'), (@q1_2, '안티패턴'),
(@q1_3, '다형성'),
(@q1_4, '의존성 주입');


-- DESIGN_PATTERN 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전략 패턴의 핵심 개념', '전략(Strategy) 패턴에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '전략 패턴은 동일한 목적의 알고리즘군을 각각 별도의 클래스로 캡슐화하고 공통 인터페이스로 추상화하여, 클라이언트가 런타임에 알고리즘(전략)을 자유롭게 교체할 수 있게 하는 행위 패턴이다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전략 패턴과 개방-폐쇄 원칙', '전략 패턴이 개방-폐쇄 원칙(OCP)을 잘 지킨다고 평가받는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '새로운 알고리즘이 필요하면 기존 코드를 수정하지 않고 전략 인터페이스를 구현한 새 클래스를 추가하기만 하면 되므로, 확장에는 열려 있고 변경에는 닫혀 있다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('조건 분기 대신 전략 패턴을 쓰는 이점', '결제 수단에 따라 if-else/switch로 분기하던 코드를 전략 패턴으로 바꿨을 때 얻는 이점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '각 분기 로직을 독립된 전략 클래스로 분리하면 결제 수단이 늘어도 거대한 조건문을 수정할 필요 없이 새 클래스만 추가하면 되고, 각 로직을 독립적으로 테스트·관리할 수 있다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전략 패턴의 컨텍스트와 전략의 관계', '전략 패턴에서 컨텍스트(Context)와 전략(Strategy) 객체의 관계로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '컨텍스트는 전략 인터페이스 타입의 참조를 필드로 가지고(컴포지션), 실제 작업을 그 전략 객체에 위임한다. 어떤 구체 전략을 주입하느냐에 따라 동작이 달라진다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전략 패턴과 템플릿 메서드 패턴의 차이', '전략 패턴과 템플릿 메서드 패턴은 모두 알고리즘의 일부를 갈아끼운다는 점에서 비슷하다. 두 패턴의 핵심 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DESIGN_PATTERN',
 '전략 패턴은 컴포지션(객체 위임)으로 런타임에 알고리즘 전체를 교체하고, 템플릿 메서드는 상속으로 상위 클래스가 알고리즘의 골격을 정하고 하위 클래스가 일부 단계만 오버라이딩해 컴파일 타임에 결정된다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 꼬리질문으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, '하나의 인스턴스만 생성되도록 보장하여 전역에서 공유하게 만드는 패턴이다.', 1, FALSE, '이는 싱글톤 패턴에 대한 설명이다. 전략 패턴의 핵심은 인스턴스 수 제한이 아니라 알고리즘 교체다.', @q2_1),
(@q2, '객체에 동적으로 새로운 책임(기능)을 덧붙여 기능을 확장하는 패턴이다.', 2, FALSE, '이는 데코레이터 패턴에 대한 설명이다. 전략 패턴은 기능을 덧붙이는 것이 아니라 알고리즘 자체를 교체한다.', @q2_2),
(@q2, '동일한 목적의 알고리즘군을 각각 캡슐화하고 공통 인터페이스로 추상화하여, 런타임에 알고리즘을 교체할 수 있게 한다.', 3, TRUE, '', @q2_3),
(@q2, '요청을 처리할 수 있는 객체들을 사슬처럼 연결해 순차적으로 넘기는 패턴이다.', 4, FALSE, '이는 책임 연쇄(Chain of Responsibility) 패턴에 대한 설명이다.', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '새 알고리즘이 필요할 때 기존 코드를 수정하지 않고 전략 인터페이스를 구현한 새 클래스만 추가하면 되기 때문이다.', 1, TRUE, '', @q2),
(@q2_1, '모든 알고리즘을 하나의 거대한 조건문에 모아 한곳에서 관리하기 때문이다.', 2, FALSE, '조건문에 모으는 방식은 새 알고리즘 추가 시 기존 코드를 계속 수정해야 해 오히려 OCP를 위반한다.', @q2),
(@q2_1, '전략을 추가할 때마다 컨텍스트 클래스의 내부 로직을 함께 고쳐야 하기 때문이다.', 3, FALSE, '컨텍스트를 매번 고쳐야 한다면 변경에 닫혀 있지 않은 것이므로 OCP 준수의 근거가 될 수 없다.', @q2),
(@q2_1, '전략을 컴파일 타임에 고정해 런타임 교체를 막기 때문이다.', 4, FALSE, '전략 패턴은 오히려 런타임 교체를 가능하게 하며, 교체를 막는 것이 장점은 아니다.', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, '전체 코드 줄 수가 항상 줄어들어 무조건 더 짧은 코드가 된다.', 1, FALSE, '클래스와 인터페이스가 늘어 오히려 전체 코드량은 증가할 수 있다. 이점은 코드 길이가 아니라 확장성·유지보수성이다.', @q2),
(@q2_2, '조건문이 사라져 프로그램 실행 속도가 반드시 빨라진다.', 2, FALSE, '전략 패턴의 목적은 성능 향상이 아니라 구조 개선이며, 실행 속도가 반드시 빨라지지는 않는다.', @q2),
(@q2_2, '결제 수단을 하나로 통합해 더 이상 여러 수단을 지원하지 않게 된다.', 3, FALSE, '전략 패턴은 여러 수단을 각각 캡슐화해 유연하게 지원하기 위한 것이지 수단을 하나로 줄이는 것이 아니다.', @q2),
(@q2_2, '결제 수단이 늘어도 거대한 조건문을 수정하지 않고 새 전략 클래스만 추가하면 되며, 각 로직을 독립적으로 테스트할 수 있다.', 4, TRUE, '', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '컨텍스트는 각 구체 전략 클래스를 직접 상속받아 기능을 물려받는다.', 1, FALSE, '전략 패턴은 상속이 아니라 컴포지션(전략 객체를 필드로 참조)을 사용해 결합도를 낮춘다.', @q2),
(@q2_3, '컨텍스트는 전략 인터페이스 타입의 참조를 필드로 가지고 실제 작업을 그 전략 객체에 위임하며, 주입되는 구체 전략에 따라 동작이 달라진다.', 2, TRUE, '', @q2),
(@q2_3, '전략 객체가 컨텍스트를 생성하고 소유하며, 컨텍스트의 생명주기를 직접 관리한다.', 3, FALSE, '일반적으로 컨텍스트가 전략을 참조·사용하는 방향이며, 전략이 컨텍스트를 소유하지는 않는다.', @q2),
(@q2_3, '컨텍스트와 전략은 반드시 하나의 클래스로 합쳐져 있어야 한다.', 4, FALSE, '둘을 분리해 위임하는 것이 전략 패턴의 핵심이며, 하나로 합치면 패턴의 이점이 사라진다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, '두 패턴 모두 상속만 사용하며 실질적인 차이가 없다.', 1, FALSE, '전략 패턴은 컴포지션을, 템플릿 메서드는 상속을 사용한다는 근본적 차이가 있다.', @q2),
(@q2_4, '전략 패턴은 상속을, 템플릿 메서드는 컴포지션을 사용한다.', 2, FALSE, '방향이 반대다. 전략 패턴이 컴포지션, 템플릿 메서드가 상속을 사용한다.', @q2),
(@q2_4, '전략 패턴은 컴포지션으로 런타임에 알고리즘 전체를 교체하고, 템플릿 메서드는 상속으로 골격을 고정한 채 하위 클래스가 일부 단계만 오버라이딩한다.', 3, TRUE, '', @q2),
(@q2_4, '전략 패턴은 생성 패턴이고 템플릿 메서드는 구조 패턴이다.', 4, FALSE, '둘 다 행위(behavioral) 패턴에 속한다.', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, '전략 패턴'), (@q2, '행위 패턴'),
(@q2_1, 'OCP'),
(@q2_2, '조건 분기'), (@q2_2, '리팩터링'),
(@q2_3, '컴포지션'),
(@q2_4, '템플릿 메서드');


-- DESIGN_PATTERN 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴의 핵심 개념', '옵저버(Observer) 패턴에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '옵저버 패턴은 한 객체(주체, Subject)의 상태가 변하면 그 객체에 등록된 여러 관찰자(Observer)에게 자동으로 통지되어 갱신되도록 일대다 의존 관계를 정의하는 행위 패턴이다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴이 만드는 느슨한 결합', '옵저버 패턴이 주체(Subject)와 관찰자(Observer)를 느슨하게 결합한다고 말하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '주체는 관찰자가 공통 인터페이스를 구현했다는 사실만 알 뿐 구체 타입이나 개수를 몰라도 통지할 수 있으므로, 관찰자를 추가·제거해도 주체 코드를 수정할 필요가 없다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴의 통지(notify) 흐름', '옵저버 패턴에서 주체의 상태가 바뀐 뒤 관찰자들이 갱신되는 일반적인 흐름으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '주체는 등록된 관찰자 목록을 순회하며 각 관찰자의 갱신 메서드(update 등)를 호출해 통지하고, 관찰자는 통지를 받아 필요한 데이터를 반영한다. 관찰자는 주체에 등록(subscribe)함으로써 통지 대상이 된다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴이 적용된 실제 사례', '옵저버 패턴이 실제로 적용된 대표적인 사례로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'LOW', 'DESIGN_PATTERN',
 'GUI의 이벤트 리스너(버튼 클릭 시 등록된 리스너들이 호출됨), 메시지 발행-구독 시스템, MVC에서 모델 변경 시 뷰 갱신 등이 옵저버 패턴의 전형적인 적용 사례다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴 사용 시 주의점', '옵저버 패턴을 사용할 때 주의해야 할 단점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '관찰자를 등록하고 해제하지 않으면 주체가 계속 참조를 붙들어 메모리 누수(lapsed listener)가 발생할 수 있고, 통지 순서에 의존하거나 통지가 연쇄되면 디버깅이 어려워질 수 있다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 꼬리질문으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, '객체 생성을 서브클래스가 결정하도록 위임하는 생성 패턴이다.', 1, FALSE, '이는 팩토리 메서드 패턴에 대한 설명이며, 상태 변화 통지를 다루는 옵저버와는 다르다.', @q3_1),
(@q3, '호환되지 않는 인터페이스를 클라이언트가 기대하는 인터페이스로 변환해 주는 패턴이다.', 2, FALSE, '이는 어댑터 패턴에 대한 설명이다.', @q3_2),
(@q3, '복잡한 서브시스템에 대한 단순한 통합 창구(인터페이스)를 제공하는 패턴이다.', 3, FALSE, '이는 퍼사드(Facade) 패턴에 대한 설명이다.', @q3_3),
(@q3, '한 주체의 상태가 변하면 등록된 여러 관찰자에게 자동으로 통지되어 갱신되도록 일대다 의존 관계를 정의한다.', 4, TRUE, '', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '주체가 각 관찰자의 구체 클래스 타입을 모두 알고 직접 호출하기 때문이다.', 1, FALSE, '구체 타입을 모두 알고 호출한다면 강한 결합이며, 이는 느슨한 결합의 근거가 될 수 없다.', @q3),
(@q3_1, '주체는 관찰자가 공통 인터페이스를 구현했다는 사실만 알면 되고 구체 타입·개수를 몰라도 통지할 수 있어, 관찰자를 추가·제거해도 주체를 수정할 필요가 없기 때문이다.', 2, TRUE, '', @q3),
(@q3_1, '관찰자와 주체가 반드시 같은 클래스 안에 함께 정의되어 있기 때문이다.', 3, FALSE, '같은 클래스에 묶여 있다면 오히려 강하게 결합된 것이다.', @q3),
(@q3_1, '주체와 관찰자가 서로를 컴파일 타임에 고정적으로 참조하기 때문이다.', 4, FALSE, '고정 참조는 결합을 강하게 만들며, 옵저버 패턴은 런타임 등록으로 결합을 느슨하게 한다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '주체가 등록된 관찰자 목록을 순회하며 각 관찰자의 갱신 메서드를 호출해 통지하고, 관찰자는 이를 받아 상태를 반영한다.', 1, TRUE, '', @q3),
(@q3_2, '각 관찰자가 일정 주기로 주체를 직접 폴링(polling)하는 것이 옵저버 패턴의 유일한 정의다.', 2, FALSE, '주기적 폴링은 별개의 방식이며, 옵저버 패턴의 본질은 주체가 등록된 관찰자에게 통지를 push하는 구조다.', @q3),
(@q3_2, '주체가 데이터베이스에 상태를 저장하면 관찰자가 트리거로만 갱신된다.', 3, FALSE, 'DB 트리거는 옵저버 패턴의 일반적 구현 방식이 아니며, 패턴 자체는 객체 간 통지로 정의된다.', @q3),
(@q3_2, '관찰자가 주체를 상속받아야만 통지를 받을 수 있다.', 4, FALSE, '관찰자는 상속이 아니라 주체에 등록(subscribe)함으로써 통지를 받는다.', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '두 수를 더하는 단순 산술 함수를 호출하는 것', 1, FALSE, '단순 함수 호출에는 상태 변화 통지라는 일대다 관계가 없어 옵저버 패턴과 무관하다.', @q3),
(@q3_3, '배열을 정렬하는 퀵소트 알고리즘의 내부 동작', 2, FALSE, '정렬 알고리즘은 자료 처리 로직일 뿐 관찰자 통지 구조와 관련이 없다.', @q3),
(@q3_3, 'GUI 버튼에 등록된 여러 이벤트 리스너가 클릭 발생 시 자동으로 호출되는 것', 3, TRUE, '', @q3),
(@q3_3, '문자열을 정수로 변환하는 파싱 로직', 4, FALSE, '단순 타입 변환에는 주체-관찰자 통지 관계가 존재하지 않는다.', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '관찰자를 단 하나만 등록할 수 있어 확장이 불가능하다.', 1, FALSE, '옵저버 패턴은 여러 관찰자를 등록할 수 있는 일대다 구조다.', @q3),
(@q3_4, '주체와 관찰자가 강하게 결합되어 관찰자 추가 시 항상 주체를 수정해야 한다.', 2, FALSE, '옵저버 패턴은 오히려 느슨한 결합을 지향하며, 관찰자 추가 시 주체 수정이 필요 없다.', @q3),
(@q3_4, '통지가 동기적으로만 가능해 어떤 경우에도 비동기 처리를 할 수 없다.', 3, FALSE, '통지를 비동기로 구현하는 것도 가능하므로 동기만 가능하다는 서술은 틀렸다.', @q3),
(@q3_4, '관찰자를 해제하지 않으면 주체가 참조를 계속 붙들어 메모리 누수가 생길 수 있고, 통지 순서·연쇄 통지로 디버깅이 어려워질 수 있다.', 4, TRUE, '', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, '옵저버 패턴'), (@q3, '행위 패턴'),
(@q3_1, '느슨한 결합'),
(@q3_2, '통지'), (@q3_2, '발행-구독'),
(@q3_3, '이벤트 리스너'), (@q3_3, 'MVC'),
(@q3_4, '메모리 누수');


-- DESIGN_PATTERN 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('팩토리 메서드 패턴의 핵심 개념', '팩토리 메서드(Factory Method) 패턴에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '팩토리 메서드는 객체 생성을 위한 인터페이스(메서드)를 상위 클래스에 정의하되, 어떤 구체 클래스의 인스턴스를 생성할지는 하위 클래스가 결정하도록 위임하는 생성 패턴이다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('팩토리 메서드가 객체 생성 책임을 분리하는 효과', '클라이언트 코드에서 new로 구체 클래스를 직접 생성하는 대신 팩토리 메서드를 통해 객체를 얻도록 했을 때의 이점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '객체 생성 지점을 팩토리로 캡슐화하면 클라이언트는 구체 클래스가 아니라 추상 타입에만 의존하게 되어, 생성할 구체 타입이 바뀌어도 클라이언트 코드를 수정할 필요가 없다(결합도 감소, DIP 지향).');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('팩토리 메서드와 추상 팩토리의 차이', '팩토리 메서드 패턴과 추상 팩토리(Abstract Factory) 패턴의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DESIGN_PATTERN',
 '팩토리 메서드는 하나의 제품을 생성하는 메서드를 하위 클래스가 오버라이딩하는 방식이고, 추상 팩토리는 서로 연관된 여러 제품군(family)을 생성하는 여러 메서드를 묶은 인터페이스를 제공한다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('구체 클래스를 직접 new로 생성할 때의 문제', '클라이언트 코드 곳곳에서 구체 클래스를 직접 new로 생성할 때 생기는 문제로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '구체 클래스를 여기저기서 직접 생성하면 클라이언트가 그 구체 타입에 강하게 결합되어, 구현체를 교체하거나 생성 방식을 바꿀 때 모든 생성 지점을 찾아 수정해야 하는 부담이 생긴다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('팩토리 메서드와 다형성의 활용', '팩토리 메서드 패턴이 하위 클래스마다 다른 객체를 생성할 수 있는 것은 어떤 객체지향 특성 덕분인가?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '상위 클래스가 선언한 팩토리 메서드를 하위 클래스가 오버라이딩(재정의)하여 각자 다른 구체 객체를 반환하도록 하는 다형성(오버라이딩) 덕분에, 상위 타입 코드는 그대로 두고도 생성되는 객체를 달리할 수 있다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 꼬리질문으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, '객체 생성 메서드를 상위 클래스에 정의하되, 실제로 어떤 구체 클래스를 생성할지는 하위 클래스가 결정하도록 위임한다.', 1, TRUE, '', @q4_1),
(@q4, '이미 생성된 객체에 실행 중 새로운 기능을 겹겹이 덧씌워 확장하는 패턴이다.', 2, FALSE, '이는 데코레이터 패턴에 대한 설명이다.', @q4_2),
(@q4, '실제 객체에 대한 접근을 대리하는 대리인 객체를 두어 접근 제어·지연 로딩 등을 수행하는 패턴이다.', 3, FALSE, '이는 프록시 패턴에 대한 설명이다.', @q4_3),
(@q4, '복잡한 객체의 생성 과정을 단계별로 분리해 동일한 절차로 서로 다른 표현을 만드는 패턴이다.', 4, FALSE, '이는 빌더 패턴에 대한 설명이다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '객체 생성 속도가 항상 빨라져 성능이 크게 향상된다.', 1, FALSE, '팩토리 메서드의 목적은 성능이 아니라 생성 책임의 분리와 결합도 감소다.', @q4),
(@q4_1, '클라이언트가 모든 구체 클래스를 직접 참조하게 되어 코드가 더 명확해진다.', 2, FALSE, '구체 클래스를 직접 참조하면 결합도가 높아지며, 팩토리 메서드는 오히려 그 참조를 없애려는 것이다.', @q4),
(@q4_1, '클라이언트가 구체 클래스가 아니라 추상 타입에만 의존하게 되어, 생성할 구체 타입이 바뀌어도 클라이언트를 수정할 필요가 없다.', 3, TRUE, '', @q4),
(@q4_1, '객체를 아예 생성하지 않고도 기능을 사용할 수 있게 된다.', 4, FALSE, '팩토리 메서드도 결국 객체를 생성하며, 생성 자체를 없애는 것이 아니다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, '팩토리 메서드는 여러 제품군을 한꺼번에 생성하고, 추상 팩토리는 단일 제품만 생성한다.', 1, FALSE, '설명이 반대다. 여러 제품군을 다루는 쪽이 추상 팩토리다.', @q4),
(@q4_2, '팩토리 메서드는 하나의 제품 생성 메서드를 하위 클래스가 결정하고, 추상 팩토리는 서로 연관된 여러 제품군을 생성하는 메서드들을 묶은 인터페이스를 제공한다.', 2, TRUE, '', @q4),
(@q4_2, '두 패턴은 이름만 다를 뿐 완전히 동일한 구조와 목적을 가진다.', 3, FALSE, '다루는 제품의 범위(단일 vs 제품군)와 구조가 다르므로 동일하지 않다.', @q4),
(@q4_2, '추상 팩토리는 생성 패턴이 아니라 행위 패턴에 속한다.', 4, FALSE, '추상 팩토리는 팩토리 메서드와 마찬가지로 생성(creational) 패턴이다.', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, 'new를 사용하면 컴파일이 되지 않아 프로그램을 실행할 수 없다.', 1, FALSE, 'new로 구체 클래스를 생성하는 것 자체는 정상적인 문법이며 컴파일 오류가 아니다. 문제는 설계상의 결합도다.', @q4),
(@q4_3, 'new로 생성한 객체는 절대 재사용할 수 없어 매번 성능이 저하된다.', 2, FALSE, '객체 재사용 여부는 별개의 문제이며, 직접 생성의 핵심 문제는 성능이 아니라 결합도다.', @q4),
(@q4_3, '구체 클래스를 직접 생성하면 다형성을 전혀 사용할 수 없게 된다.', 3, FALSE, '직접 생성하더라도 상위 타입 변수에 담아 다형성을 쓸 수는 있다. 핵심 문제는 생성 지점의 결합이다.', @q4),
(@q4_3, '클라이언트가 구체 타입에 강하게 결합되어, 구현체 교체나 생성 방식 변경 시 흩어진 모든 생성 지점을 찾아 수정해야 한다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '상위 클래스의 팩토리 메서드를 하위 클래스가 오버라이딩해 각자 다른 객체를 반환하도록 하는 다형성 덕분이다.', 1, TRUE, '', @q4),
(@q4_4, '모든 객체를 정적(static) 메서드로만 생성하기 때문이다.', 2, FALSE, '정적 메서드는 오버라이딩되지 않아 하위 클래스별로 생성을 다형적으로 위임하는 팩토리 메서드의 핵심과 맞지 않는다.', @q4),
(@q4_4, '하위 클래스가 상위 클래스의 필드를 직접 수정하기 때문이다.', 3, FALSE, '필드 직접 수정이 아니라 메서드 오버라이딩(다형성)이 서로 다른 생성을 가능하게 한다.', @q4),
(@q4_4, '컴파일러가 자동으로 적절한 구현체를 선택해 주기 때문이다.', 4, FALSE, '구현체 선택은 컴파일러의 자동 처리가 아니라 하위 클래스의 오버라이딩과 런타임 다형성으로 이루어진다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, '팩토리 메서드'), (@q4, '생성 패턴'),
(@q4_1, 'DIP'), (@q4_1, '결합도'),
(@q4_2, '추상 팩토리'),
(@q4_3, '결합도'),
(@q4_4, '다형성'), (@q4_4, '오버라이딩');


-- DESIGN_PATTERN 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('단일 책임 원칙(SRP)의 의미', 'SOLID 원칙 중 단일 책임 원칙(SRP, Single Responsibility Principle)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '단일 책임 원칙은 하나의 클래스(모듈)는 변경되어야 하는 이유가 오직 하나여야 한다는 원칙이다. 즉 하나의 클래스가 하나의 책임(액터)만 담당하도록 하여 변경의 파급을 줄인다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('개방-폐쇄 원칙(OCP)의 실천', '개방-폐쇄 원칙(OCP)을 실제로 잘 지키는 설계 방법으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '변할 수 있는 부분을 인터페이스(추상)로 뽑아내고, 기능 확장이 필요하면 기존 코드를 수정하는 대신 그 인터페이스를 구현하는 새 클래스를 추가하도록 설계한다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('단일 책임 원칙을 위반한 설계의 신호', '한 클래스가 단일 책임 원칙(SRP)을 위반하고 있다는 대표적인 신호로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '서로 다른 이유로(예: UI 변경, 세금 정책 변경, DB 스키마 변경) 같은 클래스를 자꾸 수정하게 된다면, 그 클래스가 여러 책임(여러 액터)을 떠안고 있다는 신호이므로 책임별로 분리해야 한다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('리스코프 치환 원칙(LSP) 위반 사례', '리스코프 치환 원칙(LSP)을 위반하는 대표적인 사례로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'DESIGN_PATTERN',
 '하위 타입이 상위 타입의 계약을 어겨, 상위 타입을 기대하는 코드에서 하위 타입으로 바꿔 넣었을 때 예외가 나거나 잘못 동작하는 경우가 LSP 위반이다. 예: Rectangle을 상속한 Square가 setWidth/setHeight의 기대 동작을 깨뜨리는 경우.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('의존 역전 원칙(DIP)의 적용', '의존 역전 원칙(DIP)을 적용해 상위 모듈이 하위 모듈에 직접 의존하지 않도록 하는 방법으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'DESIGN_PATTERN',
 '상위 모듈과 하위 모듈이 모두 추상(인터페이스)에 의존하도록 하고, 구체 구현체는 외부에서 주입(DI)받게 하면, 상위 모듈이 하위 구현체의 변경에 영향을 받지 않는다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 꼬리질문으로 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, '소프트웨어 요소는 확장에는 열려 있고 변경에는 닫혀 있어야 한다는 원칙이다.', 1, FALSE, '이는 개방-폐쇄 원칙(OCP)에 대한 설명이다.', @q5_1),
(@q5, '하나의 클래스는 변경되어야 하는 이유가 오직 하나여야 하며, 하나의 책임만 담당해야 한다.', 2, TRUE, '', @q5_2),
(@q5, '자식 타입은 언제나 부모 타입을 대체할 수 있어야 한다는 원칙이다.', 3, FALSE, '이는 리스코프 치환 원칙(LSP)에 대한 설명이다.', @q5_3),
(@q5, '상위 모듈은 하위 모듈의 구체 구현이 아니라 추상화에 의존해야 한다는 원칙이다.', 4, FALSE, '이는 의존 역전 원칙(DIP)에 대한 설명이다.', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '기능이 추가될 때마다 기존 클래스의 조건문을 계속 수정해 나간다.', 1, FALSE, '기존 코드를 계속 수정하는 방식은 변경에 닫혀 있지 않으므로 OCP 위반이다.', @q5),
(@q5_1, '모든 클래스를 final로 선언해 어떤 확장도 불가능하게 만든다.', 2, FALSE, '확장을 원천 차단하는 것은 확장에 열려 있어야 한다는 OCP의 취지에 어긋난다.', @q5),
(@q5_1, '변하는 부분을 인터페이스로 추상화하고, 확장이 필요하면 기존 코드를 고치지 않고 그 인터페이스를 구현한 새 클래스를 추가한다.', 3, TRUE, '', @q5),
(@q5_1, '하나의 클래스에 모든 기능을 몰아넣어 한곳에서만 수정하도록 한다.', 4, FALSE, '기능을 한 클래스에 몰아넣는 것은 오히려 SRP·OCP를 모두 해친다.', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, '클래스에 필드가 하나도 없다.', 1, FALSE, '필드 개수는 책임 분리와 직접적 관련이 없다.', @q5),
(@q5_2, '클래스가 인터페이스를 구현하고 있다.', 2, FALSE, '인터페이스 구현 자체는 SRP 위반의 신호가 아니며 오히려 좋은 설계일 수 있다.', @q5),
(@q5_2, '클래스 이름이 지나치게 짧다.', 3, FALSE, '이름 길이는 책임의 개수와 무관하다.', @q5),
(@q5_2, 'UI 변경, 정책 변경, DB 변경 등 서로 다른 이유로 같은 클래스를 자꾸 수정하게 된다.', 4, TRUE, '', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '상위 타입을 기대하는 코드에 하위 타입을 넣었더니, 하위 타입이 상위의 계약을 어겨 예외가 나거나 잘못 동작한다.', 1, TRUE, '', @q5),
(@q5_3, '하위 타입이 상위 타입의 모든 메서드를 문제없이 그대로 수행한다.', 2, FALSE, '치환해도 문제없이 동작하는 것은 오히려 LSP를 잘 지키는 경우다.', @q5),
(@q5_3, '클래스가 인터페이스를 여러 개 구현한다.', 3, FALSE, '다중 인터페이스 구현 자체는 LSP 위반과 무관하다.', @q5),
(@q5_3, '상위 클래스와 하위 클래스가 서로 다른 패키지에 위치한다.', 4, FALSE, '패키지 위치는 치환 가능성과 관련이 없다.', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, '상위 모듈이 하위 구체 클래스를 직접 new로 생성해 사용하도록 한다.', 1, FALSE, '구체 클래스를 직접 생성하면 상위 모듈이 하위 구현에 강하게 의존하게 되어 DIP를 위반한다.', @q5),
(@q5_4, '상위·하위 모듈이 모두 추상(인터페이스)에 의존하게 하고, 구체 구현체는 외부에서 주입받도록 한다.', 2, TRUE, '', @q5),
(@q5_4, '모든 클래스를 정적 메서드로 만들어 인스턴스 없이 호출하게 한다.', 3, FALSE, '정적 메서드화는 추상화에 의존하게 만드는 것과 무관하며 오히려 결합을 고착시킬 수 있다.', @q5),
(@q5_4, '하위 모듈이 상위 모듈의 구체 클래스를 상속받도록 한다.', 4, FALSE, 'DIP의 핵심은 추상화에 의존하는 것이지 구체 클래스 상속이 아니다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, 'SOLID'), (@q5, '단일 책임 원칙'),
(@q5_1, '개방-폐쇄 원칙'),
(@q5_2, '단일 책임 원칙'),
(@q5_3, '리스코프 치환 원칙'),
(@q5_4, '의존 역전 원칙');


-- ============================================================
-- 카테고리: LANGUAGE (25문항)
-- ============================================================
-- LANGUAGE 그룹 1
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('JVM 메모리 영역의 역할 구분', 'JVM 런타임 데이터 영역에 대한 설명으로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'JVM 런타임 데이터 영역은 힙(객체 인스턴스 저장, 모든 스레드가 공유), 스택(스레드마다 별도로 생성되어 메서드 호출 정보와 지역 변수 저장), 메서드 영역(클래스 메타데이터와 정적 변수 저장) 등으로 나뉜다. 힙과 메서드 영역은 스레드가 공유하고 스택은 스레드별로 존재한다.');
SET @q1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스택 영역이 스레드마다 별도로 존재하는 이유', 'JVM에서 스택 영역이 스레드마다 독립적으로 생성되는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '각 스레드는 자신만의 실행 흐름과 메서드 호출 순서, 지역 변수를 독립적으로 관리해야 하므로, 스택을 스레드별로 두어 서로 간섭 없이 안전하게 실행되도록 한다.');
SET @q1_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('클래스로더의 로딩 시점과 방식', 'Java 클래스로더가 클래스를 메서드 영역에 적재하는 시점과 방식으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '클래스로더는 해당 클래스가 처음 사용되는 시점(예: 인스턴스 생성, 정적 멤버 접근)에 클래스 파일을 읽어 로딩, 링크, 초기화 과정을 거쳐 메서드 영역에 적재하는 지연 로딩(lazy loading) 방식으로 동작한다.');
SET @q1_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('메서드 호출 시 생성되는 스택 프레임', '메서드가 호출될 때 JVM 스택에 생성되는 스택 프레임(stack frame)에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '메서드가 호출될 때마다 스택 프레임이 하나 생성되어 지역 변수 배열, 피연산자 스택, 현재 메서드가 속한 클래스의 상수 풀 참조 등을 담고, 메서드가 끝나면 해당 프레임은 스택에서 제거된다.');
SET @q1_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('PC 레지스터의 역할', 'JVM의 PC(Program Counter) 레지스터가 담당하는 역할로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'PC 레지스터는 각 스레드마다 존재하며, 현재 스레드가 실행 중인 JVM 명령(바이트코드)의 주소를 가리켜 다음에 실행할 명령을 추적하는 역할을 한다.');
SET @q1_4 = LAST_INSERT_ID();

-- 본질문(q1) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1, '힙 영역은 각 스레드마다 별도로 생성되어 서로 다른 스레드가 접근할 수 없다.', 1, FALSE, '힙은 모든 스레드가 공유하는 영역이며, 스레드마다 별도로 생성되는 것은 스택 영역이다.', @q1_1),
(@q1, '힙 영역은 모든 스레드가 공유하며 객체 인스턴스가 저장되고, 스택 영역은 스레드마다 별도로 생성되어 메서드 호출 정보와 지역 변수를 저장한다.', 2, TRUE, '', @q1_2),
(@q1, '메서드 영역은 지역 변수를 저장하며 메서드 호출이 끝나면 즉시 해제된다.', 3, FALSE, '지역 변수는 스택 프레임에 저장되며, 메서드 영역은 클래스 메타데이터와 정적 변수를 저장하는 공유 영역이다.', @q1_3),
(@q1, 'PC 레지스터는 힙에 저장된 객체의 참조 개수를 세는 용도로 사용된다.', 4, FALSE, 'PC 레지스터는 현재 스레드가 실행 중인 명령의 주소를 가리키는 영역이며, 참조 개수 계산과는 무관하다.', @q1_4);

-- 꼬리질문(q1_1) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_1, '각 스레드가 자신만의 메서드 호출 순서와 지역 변수를 독립적으로 관리해야 서로 간섭 없이 실행되기 때문이다.', 1, TRUE, '', @q1),
(@q1_1, '객체 인스턴스를 저장하는 공간이라 스레드마다 복제해야 하기 때문이다.', 2, FALSE, '객체 인스턴스는 스택이 아니라 힙에 저장되며, 스택에는 참조와 지역 변수가 저장된다.', @q1),
(@q1_1, '스택을 공유하면 가비지 컬렉션이 동작하지 않기 때문이다.', 3, FALSE, '가비지 컬렉션은 주로 힙을 대상으로 하며, 스택의 공유 여부와 직접적인 관련이 없다.', @q1),
(@q1_1, '스택이 힙보다 접근 속도가 느려 스레드마다 나눠야 하기 때문이다.', 4, FALSE, '스택을 스레드별로 두는 이유는 접근 속도가 아니라 실행 흐름의 독립성 때문이다.', @q1);

-- 꼬리질문(q1_2) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_2, 'JVM이 시작될 때 클래스패스의 모든 클래스를 한꺼번에 메모리에 적재한다.', 1, FALSE, 'JVM은 모든 클래스를 미리 적재하지 않고, 실제로 필요한 시점에 지연 로딩한다.', @q1),
(@q1_2, '클래스는 소스 코드가 컴파일될 때 곧바로 메서드 영역에 올라간다.', 2, FALSE, '컴파일은 바이트코드를 생성할 뿐이며, 메서드 영역 적재는 런타임에 클래스로더가 수행한다.', @q1),
(@q1_2, '클래스가 처음 사용되는 시점에 로딩, 링크, 초기화를 거쳐 메서드 영역에 적재하는 지연 로딩 방식으로 동작한다.', 3, TRUE, '', @q1),
(@q1_2, '클래스로더는 클래스를 힙 영역에 적재하며 스레드마다 사본을 만든다.', 4, FALSE, '클래스 메타데이터는 힙이 아니라 메서드 영역에 적재되며, 스레드마다 사본을 만들지 않는다.', @q1);

-- 꼬리질문(q1_3) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_3, '스택 프레임은 프로그램 실행 내내 하나만 존재하며 모든 메서드가 공유한다.', 1, FALSE, '스택 프레임은 메서드 호출마다 새로 생성되며, 하나만 존재하는 것이 아니다.', @q1),
(@q1_3, '스택 프레임에는 객체의 인스턴스 필드 값이 직접 저장된다.', 2, FALSE, '인스턴스 필드는 힙의 객체 안에 저장되고, 스택 프레임에는 지역 변수와 참조가 저장된다.', @q1),
(@q1_3, '스택 프레임은 메서드가 끝나도 제거되지 않고 가비지 컬렉터가 수거할 때까지 남는다.', 3, FALSE, '스택 프레임은 메서드가 종료되는 즉시 스택에서 제거되며 가비지 컬렉션 대상이 아니다.', @q1),
(@q1_3, '메서드 호출마다 생성되어 지역 변수 배열과 피연산자 스택 등을 담고, 메서드가 끝나면 스택에서 제거된다.', 4, TRUE, '', @q1);

-- 꼬리질문(q1_4) 선택지: 전부 부모 본질문(@q1)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q1_4, '힙에 있는 객체가 몇 개의 참조로 가리켜지는지 개수를 센다.', 1, FALSE, '참조 개수 계산은 일부 참조 카운팅 방식의 개념이며, PC 레지스터의 역할이 아니다.', @q1),
(@q1_4, '현재 스레드가 실행 중인 JVM 명령의 주소를 가리켜 다음에 실행할 명령을 추적한다.', 2, TRUE, '', @q1),
(@q1_4, '정적 변수와 상수 풀 정보를 저장하는 공유 영역이다.', 3, FALSE, '정적 변수와 상수 풀은 메서드 영역에 저장되며, PC 레지스터의 역할이 아니다.', @q1),
(@q1_4, '모든 스레드가 하나의 PC 레지스터를 공유하여 실행 위치를 관리한다.', 4, FALSE, 'PC 레지스터는 스레드마다 별도로 존재하며 공유되지 않는다.', @q1);

INSERT INTO question_tag (question_id, name) VALUES
(@q1, 'JVM 메모리 구조'), (@q1, '런타임 데이터 영역'),
(@q1_1, '스택 영역'), (@q1_1, '스레드'),
(@q1_2, '클래스로더'), (@q1_2, '지연 로딩'),
(@q1_3, '스택 프레임'),
(@q1_4, 'PC 레지스터');


-- LANGUAGE 그룹 2
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('가비지 컬렉션의 객체 수거 대상 판별', 'Java 가비지 컬렉터가 어떤 객체를 회수 대상으로 판단하는 기준으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'JVM의 가비지 컬렉터는 GC 루트(스택의 지역 변수, 정적 변수 등)로부터 참조 사슬을 따라가 도달할 수 없는(unreachable) 객체를 더 이상 사용되지 않는 것으로 보고 회수한다. 참조가 남아 있는 한 객체는 회수되지 않는다.');
SET @q2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Java가 수동 메모리 해제 대신 GC를 사용하는 이점', 'Java가 개발자의 수동 메모리 해제 대신 가비지 컬렉션을 채택함으로써 얻는 대표적인 이점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '수동 해제 방식에서 흔한 이중 해제, 해제 후 접근(dangling pointer), 해제 누락으로 인한 메모리 누수 같은 오류를 런타임이 자동으로 관리해 줌으로써 상당수 방지할 수 있다.');
SET @q2_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('세대별 가비지 컬렉션의 전제', 'HotSpot JVM이 힙을 Young/Old 영역으로 나누는 세대별(generational) 가비지 컬렉션의 바탕이 되는 가설로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '대부분의 객체는 생성된 뒤 금방 사용되지 않게 된다는 약한 세대 가설(weak generational hypothesis)에 근거해, 새로 생성된 객체를 Young 영역에 모아 자주 빠르게 수거함으로써 GC 효율을 높인다.');
SET @q2_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('GC 루트가 될 수 있는 참조', '도달 가능성 분석에서 GC 루트(root)로 취급되는 참조로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '실행 중인 스레드의 스택에 있는 지역 변수, 클래스의 정적 변수, JNI 참조 등 프로그램이 직접 접근 가능한 시작점들이 GC 루트가 되며, 여기서 출발해 도달 가능한 객체는 살아 있는 것으로 간주된다.');
SET @q2_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Stop-The-World가 발생하는 이유', '가비지 컬렉션 과정에서 Stop-The-World가 필요한 근본적인 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '객체의 도달 가능성을 정확히 분석하려면 그 순간 참조 관계가 바뀌지 않아야 하므로, 애플리케이션 스레드를 잠시 멈춰 일관된 상태에서 살아 있는 객체를 식별하기 위함이다.');
SET @q2_4 = LAST_INSERT_ID();

-- 본질문(q2) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2, '개발자가 명시적으로 free()를 호출한 객체만 회수한다.', 1, FALSE, 'Java에는 명시적 free()가 없으며, GC가 도달 가능성을 기준으로 자동으로 회수한다.', @q2_1),
(@q2, '생성된 지 일정 시간이 지난 모든 객체를 시간 기준으로 회수한다.', 2, FALSE, '회수 기준은 경과 시간이 아니라 GC 루트로부터의 도달 가능 여부다.', @q2_2),
(@q2, 'GC 루트로부터 참조를 따라가 도달할 수 없는 객체를 더 이상 사용되지 않는 것으로 보고 회수한다.', 3, TRUE, '', @q2_3),
(@q2, '힙이 가득 찰 때 가장 크기가 큰 객체부터 순서대로 회수한다.', 4, FALSE, '회수 대상은 객체의 크기가 아니라 도달 가능성으로 결정된다.', @q2_4);

-- 꼬리질문(q2_1) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_1, '가비지 컬렉션을 쓰면 메모리 누수가 원천적으로 절대 발생하지 않는다.', 1, FALSE, 'GC를 써도 참조가 계속 남아 있으면 회수되지 않아 논리적 메모리 누수가 발생할 수 있다.', @q2),
(@q2_1, '객체를 힙이 아닌 스택에만 할당하게 되어 접근 속도가 항상 빨라진다.', 2, FALSE, 'GC 사용 여부와 무관하게 객체는 힙에 할당되며, GC가 할당 위치를 스택으로 바꾸지 않는다.', @q2),
(@q2_1, 'GC가 동작하는 동안에도 애플리케이션 스레드는 전혀 멈추지 않는다.', 3, FALSE, '많은 GC 방식은 수거 과정에서 애플리케이션을 잠시 멈추는 Stop-The-World 구간을 가진다.', @q2),
(@q2_1, '이중 해제나 해제 후 접근, 해제 누락으로 인한 오류를 런타임이 자동 관리해 상당수 방지해 준다.', 4, TRUE, '', @q2);

-- 꼬리질문(q2_2) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_2, '대부분의 객체는 생성된 직후 금방 사용되지 않게 된다는 약한 세대 가설에 근거한다.', 1, TRUE, '', @q2),
(@q2_2, '오래된 객체일수록 더 자주 참조되므로 먼저 수거해야 한다는 가설에 근거한다.', 2, FALSE, '세대별 GC는 오래 살아남은 객체를 Old 영역으로 승격시켜 덜 자주 수거하며, 서술이 반대다.', @q2),
(@q2_2, '객체의 크기가 클수록 수명이 짧다는 가설에 근거한다.', 3, FALSE, '세대 구분의 근거는 객체 크기가 아니라 생성 시점과 수명의 경향이다.', @q2),
(@q2_2, '모든 객체의 수명이 정확히 동일하다는 전제에 근거한다.', 4, FALSE, '세대별 GC는 객체마다 수명이 다르다는 점을 전제로 하며, 동일하다는 것은 반대다.', @q2);

-- 꼬리질문(q2_3) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_3, '힙에 있는 임의의 객체는 모두 그 자체로 GC 루트가 된다.', 1, FALSE, '힙의 일반 객체는 루트가 아니며, 루트로부터 도달 가능한지가 생존을 결정한다.', @q2),
(@q2_3, '실행 중인 스레드 스택의 지역 변수와 클래스의 정적 변수 등이 GC 루트가 된다.', 2, TRUE, '', @q2),
(@q2_3, '이미 unreachable 상태가 된 객체가 GC 루트로 지정된다.', 3, FALSE, 'unreachable 객체는 회수 대상이지 루트가 아니다.', @q2),
(@q2_3, '가장 최근에 생성된 객체가 자동으로 GC 루트가 된다.', 4, FALSE, '생성 시점만으로 루트가 되지는 않으며, 루트는 스택 지역 변수와 정적 변수 등 접근 시작점이다.', @q2);

-- 꼬리질문(q2_4) 선택지: 전부 부모 본질문(@q2)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q2_4, '가비지 컬렉터가 CPU를 100% 독점하기 위해서다.', 1, FALSE, 'Stop-The-World의 목적은 CPU 독점이 아니라 참조 관계의 일관성 확보다.', @q2),
(@q2_4, '힙 메모리를 물리적으로 디스크에 백업하기 위해서다.', 2, FALSE, 'GC는 힙을 디스크에 백업하지 않으며, Stop-The-World는 그런 목적과 무관하다.', @q2),
(@q2_4, '도달 가능성 분석 중 참조 관계가 바뀌지 않도록 일관된 상태에서 살아 있는 객체를 식별하기 위해서다.', 3, TRUE, '', @q2),
(@q2_4, '개발자가 호출한 System.gc()를 강제로 실행하기 위해서다.', 4, FALSE, 'System.gc()는 수거를 요청할 뿐이며, Stop-The-World의 근본 이유는 참조 일관성 확보다.', @q2);

INSERT INTO question_tag (question_id, name) VALUES
(@q2, '가비지 컬렉션'), (@q2, '도달 가능성'),
(@q2_1, '메모리 관리'),
(@q2_2, '세대별 GC'), (@q2_2, 'Young Generation'),
(@q2_3, 'GC 루트'),
(@q2_4, 'Stop-The-World');


-- LANGUAGE 그룹 3
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('equals와 hashCode를 함께 재정의해야 하는 이유', 'equals()를 재정의할 때 hashCode()도 함께 재정의해야 하는 이유로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'Java 규약상 equals로 같다고 판단되는 두 객체는 반드시 같은 hashCode를 반환해야 한다. 이를 어기면 HashMap, HashSet 같은 해시 기반 컬렉션에서 논리적으로 같은 객체를 서로 다른 버킷에 저장해 조회에 실패할 수 있다.');
SET @q3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HashMap이 키를 찾는 과정', 'HashMap에서 특정 키에 해당하는 값을 조회할 때 내부적으로 일어나는 과정으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'HashMap은 먼저 키의 hashCode로 버킷을 찾은 뒤, 그 버킷 안의 후보들과 equals로 실제 동등성을 비교해 값을 찾는다. 따라서 hashCode가 어긋나면 애초에 올바른 버킷을 찾지 못한다.');
SET @q3_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('== 연산자와 equals의 차이', '참조 타입에서 == 연산자와 equals() 메서드의 차이로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'LOW', 'LANGUAGE',
 '== 연산자는 두 참조가 같은 객체(메모리 주소)를 가리키는지 비교하고, equals()는 재정의된 경우 객체가 논리적으로 같은 값을 갖는지 비교한다. 기본 Object.equals는 ==와 동일하게 동작한다.');
SET @q3_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('hashCode 규약의 정확한 내용', 'Java의 hashCode() 규약에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'equals로 같은 두 객체는 반드시 같은 hashCode를 반환해야 하지만, 그 역은 성립하지 않는다. 즉 서로 다른(equals가 false인) 객체가 우연히 같은 hashCode를 가질 수 있으며(해시 충돌), 이는 규약 위반이 아니다.');
SET @q3_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HashSet에 저장한 객체의 필드를 변경할 때의 문제', 'equals와 hashCode에 사용되는 필드를 가진 객체를 HashSet에 저장한 뒤, 그 필드 값을 변경하면 생길 수 있는 문제로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '필드를 바꾸면 hashCode가 달라져, 원래 저장된 버킷과 다른 버킷을 찾게 되므로 contains나 remove로 그 객체를 다시 찾지 못할 수 있다. 그래서 해시 컬렉션의 키로는 불변 객체를 쓰는 것이 안전하다.');
SET @q3_4 = LAST_INSERT_ID();

-- 본질문(q3) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3, 'hashCode를 재정의하지 않으면 컴파일 자체가 되지 않기 때문이다.', 1, FALSE, 'equals만 재정의해도 컴파일은 정상적으로 되며, 문제는 런타임의 해시 기반 컬렉션 동작에서 발생한다.', @q3_1),
(@q3, 'equals를 재정의하면 == 연산자까지 자동으로 함께 재정의되기 때문이다.', 2, FALSE, '== 연산자는 재정의 대상이 아니며 항상 참조 동일성을 비교한다.', @q3_2),
(@q3, 'hashCode는 객체의 메모리 주소를 반드시 반환해야 한다는 규약 때문이다.', 3, FALSE, 'hashCode가 반드시 메모리 주소를 반환해야 하는 것은 아니며, equals와 일관되기만 하면 된다.', @q3_3),
(@q3, 'equals로 같은 두 객체는 반드시 같은 hashCode를 반환해야 한다는 규약을 지켜 해시 기반 컬렉션이 올바르게 동작하도록 하기 위해서다.', 4, TRUE, '', @q3_4);

-- 꼬리질문(q3_1) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_1, '키의 hashCode로 버킷을 먼저 찾고, 그 버킷 안에서 equals로 실제 동등성을 비교해 값을 찾는다.', 1, TRUE, '', @q3),
(@q3_1, '모든 키를 저장 순서대로 하나씩 equals로 비교하며 선형 탐색한다.', 2, FALSE, 'HashMap은 전체를 선형 탐색하지 않고 hashCode로 버킷을 좁힌 뒤 비교한다.', @q3),
(@q3_1, '키의 == 참조 비교만으로 값을 찾는다.', 3, FALSE, 'HashMap은 == 참조 비교가 아니라 hashCode와 equals를 사용한다.', @q3),
(@q3_1, '값(value)의 hashCode를 기준으로 버킷을 찾는다.', 4, FALSE, '버킷은 값이 아니라 키의 hashCode를 기준으로 결정된다.', @q3);

-- 꼬리질문(q3_2) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_2, '== 는 값의 논리적 동등성을, equals는 참조 동일성을 비교한다.', 1, FALSE, '설명이 반대다. == 가 참조 동일성을, equals가 논리적 동등성을 비교한다.', @q3),
(@q3_2, '== 는 두 참조가 같은 객체를 가리키는지 비교하고, equals는 재정의된 경우 논리적 동등성을 비교한다.', 2, TRUE, '', @q3),
(@q3_2, '== 와 equals는 참조 타입에서 항상 완전히 동일하게 동작한다.', 3, FALSE, 'equals가 재정의되면 두 결과가 달라질 수 있으므로 항상 동일하지는 않다.', @q3),
(@q3_2, 'equals는 원시 타입(primitive)만 비교할 수 있고 참조 타입에는 쓸 수 없다.', 4, FALSE, 'equals는 참조 타입 객체를 비교하는 메서드이며, 원시 타입 전용이 아니다.', @q3);

-- 꼬리질문(q3_3) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_3, '서로 다른 객체는 반드시 서로 다른 hashCode를 가져야 한다.', 1, FALSE, '서로 다른 객체가 같은 hashCode를 갖는 해시 충돌은 허용되며 규약 위반이 아니다.', @q3),
(@q3_3, 'hashCode가 같으면 두 객체는 반드시 equals도 true여야 한다.', 2, FALSE, 'hashCode가 같아도 equals가 false일 수 있으며, 이것이 해시 충돌이다.', @q3),
(@q3_3, 'equals로 같은 두 객체는 같은 hashCode를 반환해야 하지만, 서로 다른 객체가 같은 hashCode를 갖는 것은 허용된다.', 3, TRUE, '', @q3),
(@q3_3, 'hashCode는 실행할 때마다 매번 다른 값을 반환해야 한다.', 4, FALSE, '같은 객체는 실행 중 일관되게 같은 hashCode를 반환해야 하며, 매번 달라지면 안 된다.', @q3);

-- 꼬리질문(q3_4) 선택지: 전부 부모 본질문(@q3)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q3_4, '아무 문제가 없으며 항상 정상적으로 다시 조회된다.', 1, FALSE, '필드 변경으로 hashCode가 바뀌면 저장된 버킷을 못 찾아 조회에 실패할 수 있다.', @q3),
(@q3_4, 'HashSet이 자동으로 객체를 올바른 버킷으로 재배치해 준다.', 2, FALSE, 'HashSet은 저장 후 필드 변경을 감지해 자동 재배치해 주지 않는다.', @q3),
(@q3_4, '컴파일 시점에 오류가 발생해 필드 변경 자체가 막힌다.', 3, FALSE, '필드 변경은 컴파일 오류를 일으키지 않으며, 문제는 런타임 조회에서 나타난다.', @q3),
(@q3_4, 'hashCode가 달라져 원래 버킷과 다른 곳을 찾게 되어 contains나 remove로 객체를 다시 찾지 못할 수 있다.', 4, TRUE, '', @q3);

INSERT INTO question_tag (question_id, name) VALUES
(@q3, 'equals'), (@q3, 'hashCode'),
(@q3_1, 'HashMap'), (@q3_1, '버킷'),
(@q3_2, '동일성과 동등성'),
(@q3_3, 'hashCode 규약'), (@q3_3, '해시 충돌'),
(@q3_4, '불변 객체');


-- LANGUAGE 그룹 4
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Checked 예외와 Unchecked 예외의 차이', 'Java의 Checked 예외와 Unchecked 예외의 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'Checked 예외는 컴파일러가 처리(try-catch 또는 throws 선언)를 강제하며 Exception을 상속한다. Unchecked 예외는 RuntimeException을 상속하며 컴파일러가 처리를 강제하지 않는다. 주로 복구 가능성이 있는 상황은 Checked, 프로그래밍 오류는 Unchecked로 표현한다.');
SET @q4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('복구 가능성에 따른 예외 선택', '예외를 Checked로 둘지 Unchecked로 둘지 설계할 때 일반적으로 권장되는 기준으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '호출자가 합리적으로 복구를 시도할 수 있는 상황은 Checked 예외로, 프로그래밍 오류처럼 복구가 어렵고 코드 수정이 필요한 상황은 Unchecked 예외로 표현하는 것이 일반적인 권장 기준이다.');
SET @q4_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Java 예외 클래스 계층 구조', 'Java 예외 관련 클래스의 상속 계층에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '최상위는 Throwable이며 그 아래에 Error와 Exception이 있다. RuntimeException은 Exception의 하위 클래스이고, RuntimeException과 그 하위가 Unchecked, 그 밖의 Exception 하위가 Checked 예외다. Error는 시스템 수준의 심각한 문제를 나타낸다.');
SET @q4_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('finally 블록과 자원 해제', 'try-catch-finally에서 finally 블록의 동작에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 'finally 블록은 try에서 예외가 발생하든 안 하든, 심지어 try나 catch에서 return을 만나도 (JVM 종료 등 특수한 경우를 제외하면) 항상 실행되므로, 파일이나 커넥션 같은 자원 해제 코드를 두기에 적합하다.');
SET @q4_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('예외를 잡아 무시하는 것의 문제', 'catch 블록에서 예외를 잡고도 아무 처리 없이 비워 두는(예외 무시) 코드의 문제로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '예외를 잡고 아무것도 하지 않으면 오류가 발생했다는 사실 자체가 감춰져, 문제의 원인을 추적하기 어렵고 프로그램이 잘못된 상태로 계속 진행될 수 있다. 최소한 로깅하거나 다시 던지는 등의 처리가 필요하다.');
SET @q4_4 = LAST_INSERT_ID();

-- 본질문(q4) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4, 'Checked 예외는 컴파일러가 try-catch 또는 throws로 처리하도록 강제하지만, Unchecked 예외(RuntimeException 계열)는 처리를 강제하지 않는다.', 1, TRUE, '', @q4_1),
(@q4, 'Checked 예외는 RuntimeException을 상속하고, Unchecked 예외는 Exception을 직접 상속한다.', 2, FALSE, '상속 관계가 반대다. Unchecked 예외가 RuntimeException을 상속하고, Checked 예외는 Exception을 상속하되 RuntimeException 계열은 아니다.', @q4_2),
(@q4, '두 예외 모두 컴파일러가 반드시 처리하도록 강제한다.', 3, FALSE, 'Unchecked 예외는 컴파일러가 처리를 강제하지 않는다.', @q4_3),
(@q4, 'Unchecked 예외는 발생하면 JVM이 즉시 종료되어 절대 catch할 수 없다.', 4, FALSE, 'Unchecked 예외도 try-catch로 잡을 수 있으며, 잡지 않으면 스레드가 종료될 뿐 항상 JVM 전체가 즉시 종료되는 것은 아니다.', @q4_4);

-- 꼬리질문(q4_1) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_1, '예외 클래스 이름이 길면 Checked, 짧으면 Unchecked로 정한다.', 1, FALSE, '예외 구분 기준은 이름 길이가 아니라 복구 가능성과 오류의 성격이다.', @q4),
(@q4_1, '무조건 모든 예외를 Checked로 선언하는 것이 항상 가장 좋은 설계다.', 2, FALSE, '복구 불가능한 프로그래밍 오류까지 Checked로 강제하면 불필요한 처리 코드가 늘어나 좋지 않다.', @q4),
(@q4_1, '호출자가 복구를 시도할 수 있는 상황은 Checked로, 복구가 어려운 프로그래밍 오류는 Unchecked로 표현한다.', 3, TRUE, '', @q4),
(@q4_1, '성능이 중요한 코드에서는 Checked, 아니면 Unchecked를 쓴다.', 4, FALSE, '예외 구분은 성능이 아니라 복구 가능성과 오류의 성격을 기준으로 한다.', @q4);

-- 꼬리질문(q4_2) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_2, 'Exception과 Error는 서로 상속 관계로, Error가 Exception을 상속한다.', 1, FALSE, 'Error와 Exception은 상속 관계가 아니라 둘 다 Throwable을 상속하는 형제 관계다.', @q4),
(@q4_2, 'RuntimeException은 Throwable을 직접 상속하며 Exception과 무관하다.', 2, FALSE, 'RuntimeException은 Exception의 하위 클래스다.', @q4),
(@q4_2, '모든 예외의 최상위 클래스는 Exception이다.', 3, FALSE, '최상위는 Exception이 아니라 Throwable이며, Exception과 Error가 그 아래에 있다.', @q4),
(@q4_2, '최상위 Throwable 아래에 Error와 Exception이 있고, RuntimeException은 Exception의 하위 클래스로 Unchecked 예외에 해당한다.', 4, TRUE, '', @q4);

-- 꼬리질문(q4_3) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_3, 'finally 블록은 예외가 발생했을 때만 실행된다.', 1, FALSE, 'finally는 예외 발생 여부와 관계없이 항상 실행된다.', @q4),
(@q4_3, 'finally 블록은 예외 발생 여부나 return과 관계없이 대부분의 경우 항상 실행되어 자원 해제에 적합하다.', 2, TRUE, '', @q4),
(@q4_3, 'try 안에서 return을 만나면 finally 블록은 건너뛴다.', 3, FALSE, 'try에서 return을 만나도 finally는 그 반환이 실제로 이루어지기 전에 실행된다.', @q4),
(@q4_3, 'finally 블록은 catch가 있을 때만 작성할 수 있다.', 4, FALSE, 'finally는 catch 없이 try-finally 형태로도 사용할 수 있다.', @q4);

-- 꼬리질문(q4_4) 선택지: 전부 부모 본질문(@q4)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q4_4, '오류 발생 사실이 감춰져 원인 추적이 어렵고, 프로그램이 잘못된 상태로 계속 진행될 수 있다.', 1, TRUE, '', @q4),
(@q4_4, '컴파일러가 빈 catch 블록을 허용하지 않아 컴파일 오류가 난다.', 2, FALSE, '빈 catch 블록도 문법상 허용되어 컴파일은 정상적으로 된다.', @q4),
(@q4_4, '예외를 무시하면 JVM이 자동으로 예외를 다시 던져 준다.', 3, FALSE, 'JVM은 잡힌 예외를 자동으로 다시 던지지 않으며, 무시하면 그대로 사라진다.', @q4),
(@q4_4, '빈 catch 블록은 항상 성능을 크게 향상시킨다.', 4, FALSE, '예외 무시는 성능 향상 기법이 아니라 오류를 감추는 안티패턴이다.', @q4);

INSERT INTO question_tag (question_id, name) VALUES
(@q4, 'Checked 예외'), (@q4, 'Unchecked 예외'),
(@q4_1, '예외 설계'), (@q4_1, '복구 가능성'),
(@q4_2, '예외 계층'), (@q4_2, 'Throwable'),
(@q4_3, 'finally'), (@q4_3, '자원 해제'),
(@q4_4, '예외 처리 안티패턴');


-- LANGUAGE 그룹 5
INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오버로딩과 오버라이딩의 차이', '메서드 오버로딩(overloading)과 오버라이딩(overriding)의 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'LANGUAGE',
 '오버로딩은 같은 클래스 안에서 메서드 이름은 같되 매개변수의 개수, 타입, 순서를 다르게 하여 여러 버전을 정의하는 것이고, 오버라이딩은 상위 클래스에서 상속받은 메서드를 하위 클래스에서 같은 시그니처로 재정의하는 것이다.');
SET @q5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오버로딩된 메서드의 선택 시점', '여러 오버로딩된 메서드 중 어떤 것을 호출할지 결정되는 시점으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '오버로딩은 컴파일 시점에 인자의 정적 타입을 보고 호출할 메서드가 결정되는 정적 바인딩(static binding)이다. 반면 오버라이딩은 런타임에 실제 객체 타입으로 결정되는 동적 바인딩이다.');
SET @q5_1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오버라이딩과 동적 바인딩', '상위 타입 참조 변수로 하위 클래스 객체를 가리킬 때 재정의된 메서드가 호출되는 원리로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '오버라이딩된 메서드는 참조 변수의 타입이 아니라 실제 객체의 타입에 따라 런타임에 호출 대상이 결정되는 동적 바인딩(런타임 다형성) 덕분에 하위 클래스의 재정의 버전이 호출된다.');
SET @q5_2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오버라이딩 시 지켜야 하는 규칙', '메서드를 오버라이딩할 때 지켜야 하는 규칙으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '오버라이딩 메서드는 상위 메서드보다 접근 범위를 좁힐 수 없고(예: public을 protected로 축소 불가), Checked 예외를 상위 메서드보다 더 넓게 던질 수 없으며, 메서드 시그니처(이름과 매개변수)는 동일해야 한다.');
SET @q5_3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('반환 타입과 메서드 구분', '메서드 오버로딩과 반환 타입의 관계에 대한 설명으로 가장 적절한 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'LANGUAGE',
 '오버로딩은 매개변수의 개수, 타입, 순서로 구분하며, 반환 타입만 다르고 시그니처가 같은 두 메서드는 오버로딩으로 인정되지 않아 컴파일 오류가 난다. 반면 오버라이딩에서는 하위 타입으로의 공변 반환 타입이 허용된다.');
SET @q5_4 = LAST_INSERT_ID();

-- 본질문(q5) 선택지: 4개 보기 전부 꼬리질문에 연결됨(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5, '오버로딩은 상위 클래스의 메서드를 하위 클래스에서 재정의하는 것이고, 오버라이딩은 같은 이름의 메서드를 매개변수만 다르게 여러 개 정의하는 것이다.', 1, FALSE, '두 개념의 설명이 서로 뒤바뀌었다. 재정의가 오버라이딩, 매개변수를 달리한 다중 정의가 오버로딩이다.', @q5_1),
(@q5, '오버로딩과 오버라이딩 모두 반드시 상속 관계가 있어야만 가능하다.', 2, FALSE, '오버로딩은 상속 없이 같은 클래스 안에서도 가능하며, 상속이 필수인 것은 오버라이딩이다.', @q5_2),
(@q5, '오버로딩은 같은 클래스에서 이름은 같되 매개변수를 다르게 한 여러 메서드를 두는 것이고, 오버라이딩은 상속받은 메서드를 하위 클래스에서 같은 시그니처로 재정의하는 것이다.', 3, TRUE, '', @q5_3),
(@q5, '오버로딩은 반환 타입만 다르면 성립하고, 오버라이딩은 반환 타입을 반드시 바꿔야 성립한다.', 4, FALSE, '반환 타입만 다른 것으로는 오버로딩이 성립하지 않으며, 오버라이딩은 원칙적으로 같은(또는 공변) 반환 타입을 유지한다.', @q5_4);

-- 꼬리질문(q5_1) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_1, '실행 시점에 인자의 실제 객체 타입을 보고 동적으로 결정된다.', 1, FALSE, '동적으로 결정되는 것은 오버라이딩이며, 오버로딩은 컴파일 시점에 정적 타입으로 결정된다.', @q5),
(@q5_1, '컴파일 시점에 인자의 정적 타입을 기준으로 결정되는 정적 바인딩이다.', 2, TRUE, '', @q5),
(@q5_1, '오버로딩 선택은 항상 가장 마지막에 정의된 메서드로 고정된다.', 3, FALSE, '정의 순서가 아니라 인자 타입에 가장 잘 맞는 메서드가 선택된다.', @q5),
(@q5_1, 'JVM이 런타임에 무작위로 하나를 선택한다.', 4, FALSE, '호출 대상은 무작위가 아니라 컴파일 시점에 결정된다.', @q5);

-- 꼬리질문(q5_2) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_2, '참조 변수 타입이 아니라 실제 객체 타입에 따라 런타임에 호출 대상이 결정되는 동적 바인딩 덕분이다.', 1, TRUE, '', @q5),
(@q5_2, '참조 변수의 선언 타입에 따라 컴파일 시점에 상위 클래스 메서드가 고정 호출된다.', 2, FALSE, '오버라이딩은 실제 객체 타입에 따라 하위 클래스 메서드가 호출되며, 선언 타입으로 고정되지 않는다.', @q5),
(@q5_2, '재정의된 메서드는 호출되지 않고 항상 상위 클래스 버전만 실행된다.', 3, FALSE, '실제 객체가 하위 클래스이면 재정의된 하위 클래스 버전이 실행된다.', @q5),
(@q5_2, '두 메서드가 매번 번갈아 가며 호출된다.', 4, FALSE, '호출 대상은 번갈아 정해지지 않고 실제 객체 타입으로 결정된다.', @q5);

-- 꼬리질문(q5_3) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_3, '오버라이딩 메서드는 상위 메서드보다 접근 범위를 더 좁게 바꿔야 한다.', 1, FALSE, '접근 범위는 좁힐 수 없으며, 같게 유지하거나 더 넓힐 수만 있다.', @q5),
(@q5_3, '오버라이딩 메서드는 매개변수의 개수를 자유롭게 바꿔도 된다.', 2, FALSE, '매개변수를 바꾸면 오버라이딩이 아니라 오버로딩이 되며, 시그니처는 동일해야 한다.', @q5),
(@q5_3, '오버라이딩 메서드는 상위 메서드보다 더 넓은 범위의 Checked 예외를 던질 수 있다.', 3, FALSE, '오버라이딩 메서드는 상위 메서드보다 더 넓은 Checked 예외를 던질 수 없다.', @q5),
(@q5_3, '접근 범위를 좁힐 수 없고, 더 넓은 Checked 예외를 던질 수 없으며, 메서드 시그니처는 동일해야 한다.', 4, TRUE, '', @q5);

-- 꼬리질문(q5_4) 선택지: 전부 부모 본질문(@q5)으로 되돌아가는 순환 연결(NULL 없음)
INSERT INTO answer_choice (question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(@q5_4, '반환 타입만 다르면 매개변수가 같아도 오버로딩으로 인정된다.', 1, FALSE, '반환 타입만 다른 것으로는 오버로딩이 성립하지 않으며 컴파일 오류가 난다.', @q5),
(@q5_4, '오버로딩은 반환 타입이 반드시 모두 동일해야만 성립한다.', 2, FALSE, '오버로딩은 매개변수로 구분되며, 반환 타입은 같아도 달라도 무방하다. 다만 반환 타입만으로는 구분되지 않는다.', @q5),
(@q5_4, '오버로딩은 매개변수로 구분되며 반환 타입만 다른 것으로는 성립하지 않고, 오버라이딩에서는 공변 반환 타입이 허용된다.', 3, TRUE, '', @q5),
(@q5_4, '오버라이딩에서는 반환 타입을 상위 타입으로 넓혀서 재정의해야 한다.', 4, FALSE, '공변 반환 타입은 상위 타입이 아니라 하위 타입으로 좁히는 것이 허용된다.', @q5);

INSERT INTO question_tag (question_id, name) VALUES
(@q5, '오버로딩'), (@q5, '오버라이딩'),
(@q5_1, '정적 바인딩'),
(@q5_2, '동적 바인딩'), (@q5_2, '다형성'),
(@q5_3, '오버라이딩 규칙'),
(@q5_4, '공변 반환 타입');



-- ============================================================
-- 서술형(ESSAY) 문항 (아래부터)
-- ============================================================

-- 카테고리: DB (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인덱스가 조회를 빠르게 하는 원리', '인덱스가 조회 성능을 높이는 원리와, 인덱스를 남용했을 때 발생할 수 있는 단점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '인덱스는 B+Tree 등으로 데이터를 정렬된 형태로 별도 저장해 두어, 전체 테이블을 처음부터 끝까지 스캔하지 않고 조건에 맞는 행을 로그 시간에 가깝게 찾을 수 있게 합니다. 다만 인덱스가 늘어날수록 INSERT·UPDATE·DELETE 시마다 관련된 인덱스를 함께 갱신해야 해서 쓰기 성능이 저하되고, 인덱스 자체가 저장 공간을 추가로 차지합니다. 따라서 조회 빈도가 높고 카디널리티가 높은 컬럼에 한해 선별적으로 인덱스를 생성하는 것이 바람직합니다.');
SET @e1 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트랜잭션 격리수준과 이상현상', '트랜잭션의 격리수준(Isolation Level) 4단계와 각 단계에서 발생할 수 있는 이상현상을 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 'READ UNCOMMITTED는 커밋되지 않은 변경도 읽어 더티 리드가 발생할 수 있고, READ COMMITTED는 커밋된 데이터만 읽지만 같은 트랜잭션 내에서 같은 행을 다시 읽으면 값이 달라지는 반복 불가능한 읽기(non-repeatable read)가 발생할 수 있습니다. REPEATABLE READ는 트랜잭션 내에서 읽은 행의 값을 고정해 반복 불가능한 읽기를 막지만 새로운 행이 추가로 조회되는 팬텀 리드는 DB 구현에 따라 남을 수 있고, SERIALIZABLE은 트랜잭션을 순차 실행한 것처럼 완전히 격리해 모든 이상현상을 막지만 동시성 처리량이 가장 낮습니다. 즉 격리수준이 높아질수록 데이터 일관성은 강해지지만 동시 처리 성능은 떨어지는 트레이드오프가 있습니다.');
SET @e2 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('비관적 락과 낙관적 락', '비관적 락과 낙관적 락의 차이와 각각이 적합한 상황을 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '비관적 락은 데이터를 조회하는 시점부터 실제로 잠금을 걸어(예: SELECT ... FOR UPDATE) 다른 트랜잭션의 접근을 막아 충돌 자체를 미리 방지하는 방식입니다. 낙관적 락은 잠금을 걸지 않고 버전(version) 컬럼 등을 두어, 커밋 시점에 값이 그 사이 변경되었는지 확인해 충돌이 감지되면 갱신을 실패시키고 재시도하는 방식입니다. 충돌이 빈번하게 일어나는 환경에서는 비관적 락이, 충돌이 드물고 동시 처리량이 중요한 환경에서는 낙관적 락이 더 적합합니다.');
SET @e3 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('정규화와 반정규화의 트레이드오프', '정규화와 반정규화를 각각 언제 적용하는지 트레이드오프와 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '정규화는 중복 데이터를 제거하고 하나의 사실을 한 곳에만 저장하도록 테이블을 분리해 삽입·갱신·삭제 이상현상을 방지하고 데이터 무결성을 높이지만, 조회 시 여러 테이블을 조인해야 해 성능이 떨어질 수 있습니다. 반정규화는 의도적으로 중복을 허용하거나 조인이 필요한 컬럼을 미리 합쳐두어 조회 성능을 높이지만, 같은 데이터가 여러 곳에 존재해 갱신 시 정합성을 맞추기 위한 추가 로직이나 부담이 늘어납니다. 일반적으로 쓰기와 정합성이 중요한 시스템은 정규화를 기본으로 하고, 조회 성능이 병목인 특정 테이블에 한해 반정규화를 선택적으로 적용합니다.');
SET @e22 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('INNER JOIN과 OUTER JOIN의 차이', 'INNER JOIN, LEFT/RIGHT OUTER JOIN, CROSS JOIN의 동작 차이를 결과 집합 관점에서 설명하시오.', 'ESSAY', 'LOW', 'DB',
 'INNER JOIN은 두 테이블에서 조인 조건을 만족하는 행만 결과에 포함하고, 조건에 맞지 않는 행은 어느 쪽에서도 제외됩니다. LEFT OUTER JOIN은 왼쪽 테이블의 모든 행을 결과에 포함하고 오른쪽에 매칭되는 행이 없으면 NULL로 채우며, RIGHT OUTER JOIN은 반대로 오른쪽 테이블 전체를 기준으로 동작합니다. CROSS JOIN은 조인 조건 없이 두 테이블의 모든 행을 곱집합(카티지언 곱)으로 결합해 두 테이블 행 수의 곱만큼 결과가 생성됩니다.');
SET @e23 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('클러스터드 인덱스와 논클러스터드 인덱스', '클러스터드 인덱스와 논클러스터드 인덱스의 저장 구조 차이와, 테이블당 개수 제약이 다른 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '클러스터드 인덱스는 인덱스의 순서대로 실제 데이터 행 자체를 정렬해 저장하므로 데이터 페이지 자체가 인덱스이며, 하나의 테이블은 물리적으로 한 가지 순서로만 정렬될 수 있어 테이블당 하나만 가질 수 있습니다. 논클러스터드 인덱스는 실제 데이터와 별도의 구조에 키 값과 데이터 위치(포인터 또는 클러스터드 키)를 저장해 두므로, 하나의 테이블에 여러 개를 만들 수 있지만 실제 데이터를 가져오려면 다시 한 번 데이터 페이지를 찾아가는 추가 조회가 필요할 수 있습니다. 따라서 클러스터드 인덱스는 주로 기본키처럼 범위 조회가 잦은 컬럼에, 논클러스터드 인덱스는 그 외 검색 조건 컬럼들에 보조적으로 사용됩니다.');
SET @e24 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('커버링 인덱스가 성능을 높이는 원리', '커버링 인덱스가 무엇이며 조회 성능을 높이는 원리를 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '커버링 인덱스는 쿼리가 필요로 하는 모든 컬럼(조회·조건·정렬에 사용되는 컬럼)이 하나의 인덱스에 포함되어 있어, 인덱스만으로 쿼리 결과를 완성할 수 있는 인덱스를 말합니다. 일반적인 인덱스 조회는 인덱스에서 조건에 맞는 키를 찾은 뒤 실제 값을 가져오기 위해 다시 데이터 페이지에 접근하는 랜덤 I/O가 발생하는데, 커버링 인덱스는 이 추가 접근 없이 인덱스 자체에서 모든 데이터를 읽어 응답할 수 있어 조회 성능이 크게 향상됩니다. 단, 필요한 컬럼을 모두 포함하도록 인덱스를 구성해야 하므로 인덱스 크기와 유지 비용이 늘어날 수 있습니다.');
SET @e25 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('ORM에서 N+1 문제가 발생하는 이유', 'ORM 사용 시 N+1 문제가 발생하는 원인과 해결 방법을 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 'N+1 문제는 연관된 엔티티를 지연 로딩(Lazy Loading)으로 설정했을 때, 부모 엔티티 목록을 조회하는 쿼리 1번 이후 각 부모마다 연관 엔티티를 조회하는 쿼리가 N번 추가로 실행되어 총 N+1번의 쿼리가 발생하는 현상입니다. 이는 조회하는 데이터 건수가 늘어날수록 쿼리 수가 선형으로 증가해 성능을 크게 저하시킵니다. 이를 해결하기 위해 연관 데이터를 처음부터 조인해서 함께 가져오는 fetch join이나 별도의 배치 쿼리로 한 번에 가져오는 방법(batch size 설정) 등을 사용합니다.');
SET @e26 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('ORM 사용과 순수 SQL 작성의 트레이드오프', 'ORM을 사용하는 것과 순수 SQL(또는 MyBatis 같은 SQL Mapper)을 직접 작성하는 것의 장단점을 비교하시오.', 'ESSAY', 'LOW', 'DB',
 'ORM은 객체와 테이블을 매핑해 SQL을 직접 작성하지 않고도 객체지향적으로 데이터를 다룰 수 있어 생산성이 높고 데이터베이스 종류가 바뀌어도 코드 변경이 적지만, 복잡한 통계성 쿼리나 대량 데이터 처리에서는 생성되는 SQL을 세밀하게 제어하기 어렵고 의도치 않은 쿼리(N+1 등)가 발생할 수 있습니다. 순수 SQL은 실행되는 쿼리를 정확히 예측하고 성능을 세밀하게 튜닝할 수 있지만, 테이블 구조가 바뀔 때마다 여러 SQL을 일일이 수정해야 해 유지보수 비용이 늘어납니다. 실무에서는 일반적인 CRUD는 ORM으로 처리하고, 성능이 중요한 복잡한 조회는 순수 SQL을 병행하는 방식을 많이 사용합니다.');
SET @e27 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('샤딩과 파티셔닝의 차이', '샤딩(Sharding)과 파티셔닝(Partitioning)의 차이를 데이터 분산 단위 관점에서 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 '파티셔닝은 하나의 데이터베이스 인스턴스 안에서 한 테이블의 데이터를 특정 기준(범위, 해시 등)으로 나누어 여러 물리적 파티션에 저장하는 것으로, 논리적으로는 여전히 하나의 테이블처럼 다뤄지며 관리가 비교적 단순합니다. 샤딩은 데이터를 여러 개의 독립된 데이터베이스 서버(샤드)로 분산시키는 것으로, 각 샤드가 별도의 인스턴스이기 때문에 저장 용량뿐 아니라 쓰기·조회 처리량 자체를 수평적으로 확장할 수 있지만, 샤드 간 조인이나 트랜잭션이 어려워지고 라우팅·리샤딩 같은 애플리케이션 레벨의 복잡도가 크게 늘어납니다. 즉 파티셔닝은 단일 서버 내 관리 단위 분할이고, 샤딩은 여러 서버로의 확장이라는 점이 핵심 차이입니다.');
SET @e28 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DB 레플리케이션과 읽기·쓰기 분리', '마스터-슬레이브(Master-Slave) 레플리케이션의 동작 방식과, 이를 이용한 읽기·쓰기 분리의 장점 및 발생 가능한 문제를 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '마스터-슬레이브 레플리케이션은 모든 쓰기(INSERT/UPDATE/DELETE)를 마스터 서버에서만 처리하고, 마스터의 변경 내용을 바이너리 로그 등을 통해 하나 이상의 슬레이브 서버에 비동기 또는 반동기로 복제하는 구조입니다. 이를 이용해 쓰기는 마스터로, 조회는 여러 슬레이브로 분산시키면 조회 트래픽을 수평으로 확장할 수 있고 마스터의 부하를 줄일 수 있습니다. 다만 복제는 즉시 반영되지 않을 수 있어, 마스터에 쓴 데이터를 곧바로 슬레이브에서 조회하면 아직 반영되지 않은 이전 값을 읽는 복제 지연(Replication Lag) 문제가 발생할 수 있습니다.');
SET @e29 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('분산 시스템의 CAP 이론', 'CAP 이론이 말하는 세 속성과, 분산 데이터베이스가 그중 두 가지만 동시에 만족할 수 있는 이유를 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 'CAP 이론은 분산 시스템이 일관성(Consistency, 모든 노드가 항상 같은 데이터를 보는 것), 가용성(Availability, 모든 요청이 항상 응답을 받는 것), 분할 내성(Partition Tolerance, 네트워크 분단이 발생해도 시스템이 동작하는 것) 세 속성을 동시에 완벽히 만족할 수 없다는 이론입니다. 네트워크 분단은 분산 환경에서 언제든 발생할 수 있는 전제이므로 분할 내성은 포기할 수 없고, 결국 분단 상황에서 일관성을 지키려면 응답을 지연·거부해 가용성을 희생해야 하고, 가용성을 지키려면 최신 데이터를 보장하지 못해 일관성을 희생해야 합니다. 이 때문에 실제 분산 데이터베이스들은 설계 목적에 따라 CP(예: 일부 NoSQL의 강한 일관성 모드) 또는 AP(예: 최종적 일관성을 택하는 시스템) 중 하나에 가깝게 트레이드오프를 선택합니다.');
SET @e30 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트랜잭션의 ACID 속성', '트랜잭션이 보장해야 하는 ACID 네 가지 속성을 각각 설명하시오.', 'ESSAY', 'LOW', 'DB',
 '원자성(Atomicity)은 트랜잭션에 속한 작업들이 전부 성공하거나 전부 실패해야 함을 의미하고, 일관성(Consistency)은 트랜잭션 전후로 데이터베이스가 정의된 제약과 규칙을 항상 만족하는 상태를 유지해야 함을 의미합니다. 격리성(Isolation)은 동시에 실행되는 트랜잭션들이 서로의 중간 상태에 영향을 주지 않고 마치 순차적으로 실행된 것처럼 보장되어야 함을 의미하며, 지속성(Durability)은 커밋이 완료된 트랜잭션의 결과는 시스템 장애가 발생해도 손실되지 않고 영구적으로 반영되어야 함을 의미합니다. 이 네 속성을 통해 데이터베이스는 동시성과 장애 상황에서도 데이터의 신뢰성을 보장합니다.');
SET @e31 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('MVCC가 잠금 없이 동시성을 높이는 방식', 'MVCC(Multi-Version Concurrency Control)가 읽기와 쓰기 간의 잠금 경쟁을 줄이는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 'MVCC는 데이터를 변경할 때 기존 값을 바로 덮어쓰지 않고, 트랜잭션 시점 기준으로 여러 버전의 데이터를 유지해 각 트랜잭션이 자신의 시작 시점에 맞는 버전을 읽도록 하는 방식입니다. 이 덕분에 읽기 작업은 쓰기 작업이 만들고 있는 새 버전과 무관하게 이전 버전(또는 언두 로그에 남은 스냅샷)을 읽을 수 있어, 읽기가 쓰기를 기다리거나 쓰기가 읽기를 막는 잠금 경쟁 없이 동시에 처리될 수 있습니다. 다만 여러 버전을 유지하고 더 이상 필요 없는 오래된 버전을 정리(가비지 컬렉션)하는 추가 비용이 발생하며, 트랜잭션이 오래 유지될수록 정리되지 못한 버전이 쌓여 부담이 커질 수 있습니다.');
SET @e32 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('슬로우 쿼리를 진단하고 개선하는 절차', '느린 쿼리를 발견했을 때 원인을 진단하고 개선하는 일반적인 절차를 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '먼저 슬로우 쿼리 로그나 모니터링 도구로 실제 오래 걸리는 쿼리를 식별하고, EXPLAIN으로 실행 계획을 확인해 풀 테이블 스캔이 발생하는지, 예상한 인덱스를 타는지, 조인 순서나 임시 테이블 사용 여부를 점검합니다. 원인이 인덱스 부재나 부적절한 조건(예: 컬럼에 함수를 적용해 인덱스를 못 타는 경우)이라면 적절한 인덱스를 추가하거나 쿼리를 재작성하고, 불필요하게 많은 컬럼을 조회하고 있다면 필요한 컬럼만 선택하도록 줄입니다. 인덱스나 쿼리 개선으로 한계가 있다면 데이터 양 자체를 줄이는 페이징·아카이빙이나 캐시 도입, 읽기 분산 등 구조적인 해결을 함께 고려합니다.');
SET @e33 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('뷰(View)를 사용하는 목적', '데이터베이스에서 뷰(View)를 사용하는 목적과, 뷰가 실제 테이블 데이터를 저장하지 않는다는 점이 갖는 의미를 설명하시오.', 'ESSAY', 'LOW', 'DB',
 '뷰는 하나 이상의 테이블에 대한 쿼리 결과를 미리 정의해 마치 하나의 가상 테이블처럼 사용할 수 있게 하는 객체로, 자주 쓰는 복잡한 조인이나 조건을 뷰로 감싸 SQL을 간결하게 재사용하거나, 특정 컬럼만 노출해 접근 권한을 제한하는 용도로 사용됩니다. 뷰는 실제 데이터를 별도로 저장하지 않고 정의된 쿼리를 그대로 담고 있어, 뷰를 조회할 때마다 내부적으로 원본 쿼리가 다시 실행되므로 원본 테이블의 데이터가 바뀌면 뷰의 조회 결과도 항상 최신 상태를 반영합니다. 다만 이 때문에 복잡한 뷰를 반복 조회하면 매번 원본 쿼리 비용이 그대로 들어 성능에 유의해야 합니다.');
SET @e34 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트리거의 동작 방식과 사용 시 주의점', '데이터베이스 트리거가 무엇이며, 트리거를 남용할 때 발생할 수 있는 문제를 설명하시오.', 'ESSAY', 'LOW', 'DB',
 '트리거는 특정 테이블에 INSERT·UPDATE·DELETE 같은 이벤트가 발생했을 때 애플리케이션 코드의 명시적 호출 없이 DB가 자동으로 실행하는 저장된 로직으로, 변경 이력 기록이나 관련 데이터의 자동 갱신 같은 용도로 사용됩니다. 트리거는 애플리케이션 코드에는 드러나지 않고 DB 내부에서 암묵적으로 실행되기 때문에, 트리거를 많이 사용할수록 어떤 로직이 언제 실행되는지 추적하기 어려워져 디버깅과 유지보수가 힘들어집니다. 또한 트리거가 연쇄적으로 다른 트리거를 유발하거나 무거운 로직을 담고 있으면 원래 의도한 간단한 쓰기 작업의 응답 시간이 예상치 못하게 늘어날 수 있습니다.');
SET @e35 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('저장 프로시저 사용의 장단점', '저장 프로시저(Stored Procedure)를 사용하는 것의 장점과 단점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '저장 프로시저는 여러 SQL 문과 로직을 DB 서버에 미리 컴파일된 형태로 저장해 두고 호출만으로 실행할 수 있어, 애플리케이션과 DB 사이의 네트워크 왕복을 줄이고 자주 쓰는 복잡한 로직을 재사용할 수 있게 합니다. 하지만 비즈니스 로직이 애플리케이션 코드가 아닌 DB 안에 나뉘어 존재하게 되어 버전 관리나 코드 리뷰, 테스트가 상대적으로 어렵고, 특정 DBMS의 문법에 종속되어 DB를 교체하거나 애플리케이션을 다른 언어로 재작성하기 어려워집니다. 이런 이유로 최근에는 비즈니스 로직을 애플리케이션 레이어에 두고 저장 프로시저는 성능이 critical한 일부 배치 작업 등에 제한적으로 사용하는 경우가 많습니다.');
SET @e36 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('RDBMS와 NoSQL을 선택하는 기준', 'RDBMS와 NoSQL의 데이터 모델 차이와, 각각을 선택하는 기준을 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 'RDBMS는 스키마를 미리 정의한 테이블과 행 구조로 데이터를 저장하고 관계와 제약조건, 트랜잭션을 통해 강한 일관성을 보장하는 반면, NoSQL은 문서·키-값·컬럼패밀리·그래프 등 유연한 스키마 구조를 사용해 데이터 형태 변경에 유연하고 수평 확장이 상대적으로 쉽습니다. 데이터 간 관계가 복잡하고 정합성이 중요한 결제·정산 같은 도메인에는 RDBMS가 적합하고, 데이터 구조가 자주 바뀌거나 매우 큰 트래픽을 수평으로 분산 처리해야 하는 로그·세션 저장 같은 경우에는 NoSQL이 적합합니다. 실무에서는 하나의 서비스 안에서도 도메인 특성에 맞게 RDBMS와 NoSQL을 함께 사용하는 폴리글랏 퍼시스턴스 전략을 취하기도 합니다.');
SET @e37 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('커넥션 풀을 사용하는 이유', 'DB 커넥션 풀이 필요한 이유와, 풀 크기를 너무 작게 또는 너무 크게 설정했을 때의 문제를 설명하시오.', 'ESSAY', 'LOW', 'DB',
 'DB 커넥션을 매 요청마다 새로 생성하고 종료하면 TCP 연결과 인증 등에 비용이 들어 응답 시간이 늘어나므로, 커넥션 풀은 미리 일정 수의 연결을 만들어 두고 요청마다 빌려주고 반납받는 방식으로 이 비용을 없애줍니다. 풀 크기를 너무 작게 설정하면 동시 요청이 많을 때 커넥션을 얻지 못해 대기하거나 타임아웃이 발생해 처리량이 제한되고, 너무 크게 설정하면 DB 서버가 동시에 처리해야 하는 연결이 늘어나 오히려 DB 자체의 컨텍스트 스위칭·메모리 부담이 커져 성능이 저하될 수 있습니다. 따라서 애플리케이션의 동시 처리량과 DB 서버의 처리 한계를 함께 고려해 적절한 풀 크기를 설정해야 합니다.');
SET @e38 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('외래키 제약을 사용할 때의 장단점', '외래키(Foreign Key) 제약을 애플리케이션 레벨이 아닌 DB 레벨에 두었을 때의 장점과, 그로 인해 발생할 수 있는 제약을 설명하시오.', 'ESSAY', 'LOW', 'DB',
 '외래키 제약을 DB에 걸어두면 참조하는 데이터가 존재하지 않는 값이 들어오거나, 참조되고 있는 데이터가 함부로 삭제되는 것을 DB가 직접 막아주어 애플리케이션 코드의 실수와 무관하게 참조 무결성을 보장할 수 있습니다. 반면 대량의 데이터를 삽입·삭제할 때마다 매번 참조 관계를 검사해야 해 쓰기 성능에 부담이 되고, 샤딩처럼 관련 테이블이 서로 다른 DB 인스턴스로 분리되는 구조에서는 물리적으로 외래키 제약을 걸 수 없다는 한계가 있습니다. 이런 이유로 트래픽이 매우 큰 서비스에서는 외래키 제약을 걸지 않고 애플리케이션 로직으로 참조 무결성을 관리하는 선택을 하기도 합니다.');
SET @e39 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('대량 데이터를 배치로 삽입할 때의 최적화', '대량의 데이터를 한 건씩 INSERT하는 대신 배치(Batch)로 처리하는 것이 성능에 유리한 이유를 설명하시오.', 'ESSAY', 'LOW', 'DB',
 '한 건씩 INSERT하면 각 쿼리마다 네트워크 왕복과 트랜잭션 커밋, 인덱스 갱신 같은 오버헤드가 매번 반복해서 발생하지만, 여러 행을 하나의 INSERT 문(또는 배치 API)으로 묶어 보내면 이런 오버헤드를 한 번의 요청으로 분산시켜 전체 처리 시간을 크게 줄일 수 있습니다. 다만 배치 크기를 너무 크게 잡으면 하나의 트랜잭션이 오래 유지되어 락 경합이나 언두 로그 사용량이 늘어날 수 있으므로, 적절한 크기(예: 수백~수천 건 단위)로 나누어 커밋하는 것이 일반적입니다. 또한 대량 삽입 시에는 관련 인덱스를 일시적으로 비활성화한 뒤 삽입 완료 후 재생성하는 방식으로 추가 성능을 얻는 경우도 있습니다.');
SET @e40 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('DB 조회 결과 캐싱과 무효화 전략', 'DB 조회 결과를 캐시할 때 데이터 최신성을 유지하기 위한 캐시 무효화 전략을 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 '캐시는 DB 조회 결과를 메모리에 저장해 같은 요청을 다시 DB까지 가지 않고 빠르게 응답하기 위해 사용하지만, 원본 데이터가 변경된 후에도 캐시에는 예전 값이 남아 있어 최신 데이터와 불일치하는 문제가 발생할 수 있습니다. 이를 막기 위해 일정 시간이 지나면 자동으로 캐시를 만료시키는 TTL(Time To Live) 방식, 데이터가 변경되는 시점에 해당 캐시를 명시적으로 삭제하거나 갱신하는 write-through/write-invalidate 방식 등을 사용합니다. TTL만 사용하면 만료 전까지 최신성이 떨어질 수 있고, 변경 시점 무효화만 사용하면 무효화 로직을 빠뜨렸을 때 캐시가 영구히 오래된 값을 반환할 위험이 있어, 실무에서는 두 방식을 함께 적용해 위험을 줄입니다.');
SET @e41 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('EXPLAIN으로 쿼리 실행 계획을 분석하는 방법', 'EXPLAIN 명령으로 확인할 수 있는 정보와, 이를 통해 비효율적인 쿼리를 판단하는 기준을 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 'EXPLAIN은 실제로 쿼리를 실행하지 않고 옵티마이저가 세운 실행 계획을 보여주는데, 어떤 테이블에 접근하는 순서인지, 인덱스를 사용하는지와 어떤 인덱스를 사용하는지(type, key), 몇 건의 행을 검사할 것으로 예상하는지(rows), 임시 테이블이나 파일 정렬을 사용하는지(Extra) 등을 확인할 수 있습니다. type이 인덱스를 전혀 타지 못하는 전체 테이블 스캔(ALL)에 가깝거나, rows 값이 실제 필요한 결과보다 지나치게 크거나, Extra에 Using filesort·Using temporary가 나타나면 비효율적인 쿼리일 가능성이 높습니다. 이런 지표를 바탕으로 인덱스를 추가하거나 조건·조인 방식을 조정해 실행 계획을 개선합니다.');
SET @e42 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오프셋 기반 페이징과 커서 기반 페이징', '오프셋(OFFSET) 기반 페이징과 커서(Cursor) 기반 페이징의 동작 차이와, 데이터가 많을 때 오프셋 방식이 느려지는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'DB',
 '오프셋 기반 페이징은 LIMIT과 OFFSET을 사용해 앞의 N개 행을 건너뛰고 다음 페이지를 가져오는 방식인데, OFFSET 값이 커질수록 DB는 건너뛸 행까지도 일단 읽고 세어야 해서 뒤쪽 페이지로 갈수록 조회 비용이 커집니다. 커서 기반 페이징은 이전 페이지의 마지막 행이 가진 정렬 기준 값(예: id, 생성 시각)을 기준으로 그 값보다 큰(또는 작은) 행 중 N개를 조회하는 방식으로, 인덱스를 이용해 항상 해당 위치부터 바로 읽을 수 있어 페이지 번호와 무관하게 일정한 성능을 냅니다. 다만 커서 방식은 임의의 페이지 번호로 바로 이동하기 어렵다는 제약이 있어, 무한 스크롤처럼 순차 탐색이 자연스러운 UI에 더 적합합니다.');
SET @e43 = LAST_INSERT_ID();

-- 서술형 태그

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e1, '인덱스'), (@e1, 'B+Tree'), (@e1, '쓰기 성능'),
(@e2, '트랜잭션 격리수준'), (@e2, '더티 리드'), (@e2, '팬텀 리드'),
(@e3, '비관적 락'), (@e3, '낙관적 락'),
(@e22, '정규화'), (@e22, '반정규화'), (@e22, '데이터 무결성'),
(@e23, 'JOIN'), (@e23, 'OUTER JOIN'), (@e23, 'CROSS JOIN'),
(@e24, '클러스터드 인덱스'), (@e24, '논클러스터드 인덱스'), (@e24, '인덱스 구조'),
(@e25, '커버링 인덱스'), (@e25, '인덱스'), (@e25, 'I/O 최적화'),
(@e26, 'N+1 문제'), (@e26, '지연 로딩'), (@e26, 'fetch join'),
(@e27, 'ORM'), (@e27, 'SQL Mapper'), (@e27, '생산성'),
(@e28, '샤딩'), (@e28, '파티셔닝'), (@e28, '수평 확장'),
(@e29, '레플리케이션'), (@e29, '마스터-슬레이브'), (@e29, '복제 지연'),
(@e30, 'CAP 이론'), (@e30, '분산 시스템'), (@e30, '일관성'), (@e30, '가용성'),
(@e31, 'ACID'), (@e31, '원자성'), (@e31, '지속성'),
(@e32, 'MVCC'), (@e32, '동시성 제어'), (@e32, '스냅샷'),
(@e33, '슬로우 쿼리'), (@e33, 'EXPLAIN'), (@e33, '쿼리 튜닝'),
(@e34, '뷰'), (@e34, 'View'), (@e34, '가상 테이블'),
(@e35, '트리거'), (@e35, '자동화 로직'), (@e35, '유지보수성'),
(@e36, '저장 프로시저'), (@e36, '비즈니스 로직'), (@e36, 'DBMS 종속성'),
(@e37, 'NoSQL'), (@e37, 'RDBMS'), (@e37, '데이터 모델'),
(@e38, '커넥션 풀'), (@e38, '리소스 튜닝'),
(@e39, '외래키'), (@e39, '참조 무결성'), (@e39, '제약조건'),
(@e40, '배치 처리'), (@e40, '대량 삽입'), (@e40, '성능 최적화'),
(@e41, '캐시'), (@e41, '캐시 무효화'), (@e41, 'TTL'),
(@e42, 'EXPLAIN'), (@e42, '실행 계획'), (@e42, '쿼리 최적화'),
(@e43, '페이징'), (@e43, '오프셋 페이징'), (@e43, '커서 페이징');

-- 카테고리: NETWORK (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HTTP와 HTTPS의 차이', 'HTTP와 HTTPS의 차이를 TLS 핸드셰이크 과정과 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 'HTTP는 데이터를 평문으로 주고받아 중간에서 가로채면 내용이 그대로 노출되지만, HTTPS는 TCP 연결 위에 TLS 계층을 추가해 데이터를 암호화합니다. TLS 핸드셰이크에서는 클라이언트와 서버가 지원 가능한 암호화 방식을 협상하고, 서버가 인증서를 제시해 신원을 증명하며, 이후 실제 데이터를 암호화할 세션 키를 안전하게 합의합니다. 이 과정 덕분에 HTTPS는 기밀성뿐 아니라 서버 신원 인증과 데이터 무결성도 함께 보장합니다.');
SET @e4 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('TCP 연결의 수립과 종료', 'TCP의 3-way handshake와 4-way handshake 과정을 각각 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '3-way handshake는 TCP 연결을 수립하는 절차로, 클라이언트가 SYN을 보내고 서버가 SYN-ACK으로 응답한 뒤 클라이언트가 다시 ACK을 보내 양쪽이 서로의 송수신 준비 상태를 확인하고 연결을 맺습니다. 4-way handshake는 연결을 종료하는 절차로, 한쪽이 FIN을 보내면 상대가 ACK으로 응답하고, 이후 상대도 자신의 FIN을 보내면 처음 쪽이 ACK으로 응답해 양방향 연결을 각각 종료합니다. 연결 수립은 3단계로 끝나지만 종료는 양쪽 모두가 각자 자신의 전송을 마쳤음을 알려야 하므로 4단계가 필요합니다.');
SET @e5 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('REST API의 설계 원칙', 'REST API가 지켜야 하는 설계 원칙과, 그 원칙을 지킬 때의 이점을 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 'REST는 자원을 URI로 식별하고 HTTP 메서드(GET/POST/PUT/DELETE)로 행위를 표현하는 Uniform Interface, 서버가 클라이언트의 이전 요청 상태를 저장하지 않는 Stateless, 응답에 캐시 가능 여부를 명시하는 Cacheable 등의 제약조건을 지킵니다. 이런 원칙을 지키면 클라이언트와 서버의 역할이 명확히 분리되어 서로 독립적으로 발전시킬 수 있고, 무상태성 덕분에 서버를 수평 확장하기도 쉬워집니다. 다만 실무에서는 모든 제약을 완벽히 따르기보다 URI·메서드 설계 등 핵심 원칙 위주로 적용하는 경우가 많습니다.');
SET @e6 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('도메인 이름이 IP로 바뀌는 과정', '웹 브라우저에 도메인 이름을 입력했을 때 실제 IP 주소로 변환되기까지의 DNS 조회 과정을 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '브라우저는 먼저 자신의 캐시와 OS의 hosts 파일 및 캐시를 확인하고, 없으면 로컬 네트워크에 설정된 DNS 리졸버(주로 ISP 또는 공용 DNS 서버)에 질의를 보냅니다. 리졸버는 캐시에 없으면 루트 네임서버, TLD(.com 등) 네임서버, 해당 도메인의 권한 있는(authoritative) 네임서버를 순차적으로 조회해 최종 IP 주소를 얻는 재귀적 질의를 수행합니다. 조회된 결과는 TTL(Time To Live) 동안 각 단계의 캐시에 저장되어, 이후 같은 도메인 조회 시에는 상위 서버까지 가지 않고 캐시된 값을 재사용해 응답 속도를 높입니다.');
SET @e44 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('여러 서버로 요청을 분산시키는 방법', '대표적인 로드밸런싱 알고리즘 세 가지 이상을 제시하고 각각의 동작 방식과 적합한 상황을 비교하시오.', 'ESSAY', 'HIGH', 'NETWORK',
 '라운드 로빈(Round Robin)은 서버 목록을 순서대로 돌며 요청을 균등하게 분배해 서버 성능이 비슷할 때 적합하고, 가중 라운드 로빈은 서버별로 가중치를 두어 성능이 다른 서버들의 배분 비율을 다르게 처리합니다. 최소 연결(Least Connections) 방식은 현재 처리 중인 연결 수가 가장 적은 서버로 요청을 보내 요청 처리 시간이 서버마다 크게 다를 때 유리하며, IP 해시 방식은 클라이언트 IP를 해싱해 항상 같은 서버로 보내 세션 고정(sticky session)이 필요한 경우에 사용합니다. 트래픽 패턴과 서버 간 성능 차이, 세션 유지 필요성에 따라 적절한 알고리즘을 선택해야 합니다.');
SET @e45 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('네트워크에서 데이터를 전달하는 두 가지 방식', '패킷 스위칭과 서킷 스위칭의 차이를 설명하고, 인터넷이 어떤 방식을 채택하고 있는지 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 '서킷 스위칭은 통신을 시작하기 전에 송신자와 수신자 사이에 전용 회선(경로)을 미리 확보해두고 통신이 끝날 때까지 그 경로를 독점적으로 사용하는 방식으로, 전화망처럼 지연 없이 안정적인 전송이 보장되지만 회선이 유휴 상태여도 다른 통신이 그 경로를 사용할 수 없어 자원 효율이 낮습니다. 패킷 스위칭은 데이터를 패킷 단위로 나누어 각 패킷이 네트워크 상황에 따라 서로 다른 경로로 전달될 수 있게 하는 방식으로, 여러 통신이 같은 경로(회선)를 공유해 자원을 효율적으로 사용할 수 있지만 패킷마다 도착 순서가 달라지거나 혼잡에 따라 지연이 변할 수 있습니다. 인터넷은 자원을 효율적으로 공유하기 위해 패킷 스위칭 방식을 기반으로 설계되었으며, 각 패킷은 IP 프로토콜에 따라 독립적으로 라우팅됩니다.');
SET @e46 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프록시가 클라이언트 편인지 서버 편인지', '포워드 프록시와 리버스 프록시의 차이를 각각이 누구를 대신하는지 관점에서 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '포워드 프록시는 클라이언트 쪽에 위치해 클라이언트를 대신해 외부 서버에 요청을 전달하는 프록시로, 클라이언트의 IP를 숨기거나 사내망에서 특정 사이트 접근을 통제하는 용도로 사용됩니다. 리버스 프록시는 서버 쪽에 위치해 여러 클라이언트의 요청을 대신 받아 내부의 실제 서버로 전달하는 프록시로, 서버의 구조를 숨기고 로드밸런싱이나 SSL 종료, 캐싱 같은 부가 기능을 제공합니다. 즉 두 프록시 모두 요청과 응답 사이에 위치하지만, 누구의 존재를 감추고 누구를 대신하는지가 반대입니다.');
SET @e47 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('실시간 통신을 구현하는 두 가지 방식', '웹소켓과 HTTP 롱폴링의 동작 방식 차이를 설명하고, 각각의 장단점을 비교하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 'HTTP 롱폴링은 클라이언트가 요청을 보내면 서버가 새로운 데이터가 생길 때까지 응답을 늦춰서 보내고, 응답을 받으면 클라이언트가 곧바로 다시 요청을 보내는 방식으로 매 요청마다 HTTP 헤더 오버헤드와 커넥션 재설정 비용이 발생합니다. 웹소켓은 최초 HTTP 핸드셰이크로 연결을 맺은 뒤 하나의 TCP 커넥션을 계속 유지하며 서버와 클라이언트가 양방향으로 자유롭게 데이터를 주고받을 수 있어, 오버헤드가 적고 실시간성이 더 뛰어납니다. 다만 웹소켓은 연결을 계속 유지해야 해 서버 자원(커넥션 수)을 더 많이 소모하므로, 상황에 따라 두 방식 중 적합한 것을 선택합니다.');
SET @e48 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('여러 번 요청해도 결과가 같은 메서드', 'HTTP 메서드의 멱등성(Idempotency)이 무엇인지 설명하고, GET·PUT·POST 메서드를 멱등성 관점에서 비교하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '멱등성이란 동일한 요청을 여러 번 반복해도 서버의 최종 상태가 한 번 요청했을 때와 같게 유지되는 성질을 말합니다. GET은 데이터를 조회만 하므로 몇 번을 호출해도 서버 상태가 변하지 않아 멱등하고, PUT은 특정 리소스를 지정한 값으로 완전히 대체하므로 여러 번 호출해도 결과가 동일해 멱등합니다. 반면 POST는 매번 새로운 리소스를 생성하는 경우가 많아 같은 요청을 반복하면 매번 다른 결과(중복 생성)를 낳을 수 있어 멱등하지 않습니다.');
SET @e49 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('다른 출처로의 요청이 막히는 이유', 'CORS(Cross-Origin Resource Sharing) 정책이 존재하는 이유와, 브라우저가 이를 검사하는 방식을 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '브라우저는 동일 출처 정책(Same-Origin Policy)에 따라 스크립트가 자신과 다른 출처(도메인·프로토콜·포트 중 하나라도 다른 곳)의 리소스에 자유롭게 접근하지 못하도록 기본적으로 제한해, 악의적인 사이트가 사용자 몰래 다른 사이트에 요청을 보내 정보를 탈취하는 것을 막습니다. CORS는 서버가 응답 헤더(Access-Control-Allow-Origin 등)로 어떤 출처의 요청을 허용할지 명시하면 브라우저가 그 헤더를 확인해 스크립트에 응답을 넘겨줄지 판단하는 메커니즘입니다. 상태를 변경할 수 있는 요청 등에는 브라우저가 실제 요청 전에 OPTIONS 메서드로 사전 요청(Preflight)을 보내 서버가 허용하는지 먼저 확인합니다.');
SET @e50 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('네트워크 통신을 7단계로 나눈 모델', 'OSI 7계층 모델의 각 계층이 담당하는 역할을 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 '물리 계층은 전기적 신호로 비트를 전송하고, 데이터 링크 계층은 MAC 주소를 이용해 인접한 노드 간 프레임 전송과 오류 검출을 담당하며, 네트워크 계층은 IP 주소를 이용해 서로 다른 네트워크 간 경로를 찾아 패킷을 전달(라우팅)합니다. 전송 계층은 TCP/UDP처럼 종단 간 신뢰성 있는 데이터 전달이나 흐름 제어를 담당하고, 세션·표현 계층은 연결 세션 관리와 데이터 형식 변환·암호화를 맡으며, 응용 계층은 HTTP·FTP 등 사용자가 실제로 사용하는 프로토콜을 제공합니다. 각 계층은 하위 계층이 제공하는 기능 위에서 동작하며 계층별로 책임을 분리해 프로토콜을 독립적으로 교체·발전시킬 수 있게 합니다.');
SET @e51 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('IP 주소로 MAC 주소를 알아내는 방법', 'ARP(Address Resolution Protocol)가 필요한 이유와 동작 과정을 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 '같은 네트워크(LAN) 안에서 실제로 데이터를 전달하려면 데이터 링크 계층의 MAC 주소가 필요한데, 통신하려는 상대의 IP 주소는 알아도 MAC 주소는 모르는 경우가 많아 이를 알아내기 위해 ARP를 사용합니다. 송신 호스트는 목적지 IP 주소를 담아 로컬 네트워크 전체에 브로드캐스트로 ARP 요청을 보내고, 해당 IP를 가진 호스트만 자신의 MAC 주소를 담아 유니캐스트로 응답합니다. 이렇게 얻은 IP-MAC 매핑은 일정 시간 동안 ARP 캐시 테이블에 저장되어 매번 새로 조회하지 않고 재사용됩니다.');
SET @e52 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('사설 IP로도 인터넷에 접속할 수 있는 이유', 'NAT(Network Address Translation)가 필요한 이유와 동작 원리를 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 'IPv4 주소는 한정되어 있어 모든 기기에 공인 IP를 부여하기 어렵기 때문에, 사내망이나 가정 내에서는 사설 IP를 사용하고 외부 인터넷과 통신할 때만 NAT 장비(주로 라우터)가 사설 IP를 하나의 공인 IP로 변환해 내보냅니다. 여러 내부 호스트가 동시에 외부와 통신할 수 있도록, NAT는 포트 번호까지 함께 변환해(PAT, Port Address Translation) 각 세션을 구분하고 응답이 오면 저장해둔 매핑 테이블을 참조해 올바른 내부 호스트로 되돌려 보냅니다. 이 덕분에 공인 IP 절약뿐 아니라 내부 네트워크 구조가 외부에 직접 노출되지 않는 보안상 이점도 얻을 수 있습니다.');
SET @e53 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('하나의 네트워크를 여러 개로 나누는 방법', '서브넷팅(Subnetting)이 필요한 이유와 서브넷 마스크의 역할을 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '서브넷팅은 하나의 큰 네트워크 대역을 여러 개의 작은 네트워크(서브넷)로 나누는 것으로, IP 주소를 필요한 만큼만 나누어 낭비를 줄이고 트래픽을 분리해 브로드캐스트 범위를 줄이며 보안 및 관리 단위를 세분화할 수 있게 합니다. 서브넷 마스크는 IP 주소 중 어느 비트까지가 네트워크를 식별하는 부분이고 어느 비트가 호스트를 식별하는 부분인지 구분해주는 역할을 하며, IP 주소와 서브넷 마스크를 AND 연산하면 해당 IP가 속한 네트워크 주소를 알 수 있습니다. 예를 들어 서브넷 마스크의 비트 수를 늘리면(예: /24 → /26) 네트워크 부분이 늘어나 더 작은 서브넷으로 세분화되지만 각 서브넷에 속할 수 있는 호스트 수는 줄어듭니다.');
SET @e54 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('브라우저가 다시 요청 없이 자원을 재사용하는 방법', 'HTTP 캐시와 관련된 Cache-Control, ETag 헤더의 역할과 동작 방식을 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 'Cache-Control 헤더는 응답을 얼마나 오래(max-age), 어떤 조건으로 캐시해도 되는지를 지정해, 그 기간 동안은 클라이언트가 서버에 재요청을 보내지 않고 로컬 캐시를 그대로 사용하게 합니다. ETag는 리소스의 특정 버전을 식별하는 값(주로 해시)으로, 캐시 기간이 지난 뒤 클라이언트가 If-None-Match 헤더에 저장해둔 ETag를 담아 재요청하면 서버는 리소스가 변경되지 않았을 경우 본문 없이 304 Not Modified만 응답해 대역폭을 절약할 수 있습니다. 즉 Cache-Control은 캐시를 쓸지 말지와 유효 기간을 결정하고, ETag는 캐시가 만료된 뒤에도 실제로 리소스가 바뀌었는지 효율적으로 검증하는 역할을 합니다.');
SET @e55 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('REST를 대체할 수 있는 또 다른 API 방식', 'gRPC와 REST API의 차이를 통신 방식, 데이터 형식, 성능 관점에서 비교하시오.', 'ESSAY', 'HIGH', 'NETWORK',
 'REST는 보통 JSON을 텍스트 형태로 주고받고 HTTP/1.1 위에서 요청-응답 방식으로 동작하며, 사람이 읽기 쉽고 널리 쓰이는 도구·문서 생태계를 갖추고 있습니다. gRPC는 Protocol Buffers라는 바이너리 직렬화 포맷을 사용해 데이터 크기가 작고 파싱이 빠르며, HTTP/2 기반으로 하나의 커넥션에서 멀티플렉싱과 양방향 스트리밍을 지원해 REST보다 더 다양한 통신 패턴(단일 요청-응답뿐 아니라 서버 스트리밍, 클라이언트 스트리밍 등)을 효율적으로 처리할 수 있습니다. 다만 gRPC는 바이너리 포맷이라 브라우저에서 직접 다루기 어렵고 사람이 읽기 어려워 주로 내부 서비스 간(MSA) 통신에 적합하며, 외부에 공개하는 API는 여전히 REST가 널리 쓰입니다.');
SET @e56 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('HTTP 버전이 올라가며 개선된 지점', 'HTTP/1.1, HTTP/2, HTTP/3가 각각 이전 버전의 어떤 한계를 어떻게 개선했는지 설명하시오.', 'ESSAY', 'HIGH', 'NETWORK',
 'HTTP/1.1은 기본적으로 한 커넥션에서 요청을 순차적으로 처리해 헤드 오브 라인 블로킹이 발생하기 쉽고, 이를 우회하려 브라우저가 도메인당 여러 TCP 커넥션을 맺는 방식이 흔했습니다. HTTP/2는 하나의 TCP 커넥션 위에서 여러 스트림을 멀티플렉싱해 동시에 여러 요청·응답을 처리하고 헤더를 압축(HPACK)해 오버헤드를 줄였지만, 근본적으로 TCP 위에서 동작하기 때문에 패킷 하나만 손실돼도 그 아래의 모든 스트림이 대기해야 하는 TCP 수준의 헤드 오브 라인 블로킹은 여전히 남아 있었습니다. HTTP/3는 TCP 대신 UDP 기반의 QUIC 프로토콜을 사용해 스트림별로 독립적으로 손실을 복구하게 함으로써 TCP 수준의 블로킹 문제를 해소하고, 연결 수립과 암호화 협상을 통합해 초기 연결 지연도 줄였습니다.');
SET @e57 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('로그인 상태를 유지하는 여러 방식', '쿠키, 세션, 토큰(JWT) 기반 인증 방식의 차이와 각각의 장단점을 비교하시오.', 'ESSAY', 'HIGH', 'NETWORK',
 '쿠키는 클라이언트에 값을 저장해 매 요청마다 서버로 함께 전송되는 저장 수단 자체를 말하며, 세션 방식은 서버가 사용자별 상태를 세션 저장소에 저장하고 클라이언트에는 그 세션을 식별할 수 있는 세션 ID만 쿠키로 내려주어 서버가 요청마다 세션 저장소를 조회해 사용자를 식별합니다. 이 방식은 서버가 언제든 세션을 무효화할 수 있어 제어가 쉽지만, 서버가 상태를 저장해야 하므로 서버 확장 시 세션 저장소를 공유하거나 동기화해야 하는 부담이 있습니다. 토큰(JWT) 방식은 사용자 정보와 서명을 담은 토큰 자체를 클라이언트가 들고 있다가 요청마다 함께 보내고 서버는 서명만 검증하면 되므로 서버가 상태를 저장하지 않아 수평 확장이 쉽지만, 발급된 토큰을 서버가 즉시 무효화하기 어렵다는 단점이 있습니다.');
SET @e58 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('신뢰성과 속도, 두 전송 프로토콜의 선택', 'TCP와 UDP의 차이를 설명하고, 각각이 적합한 서비스의 예를 드시오.', 'ESSAY', 'LOW', 'NETWORK',
 'TCP는 연결형 프로토콜로 3-way handshake를 통해 연결을 맺고, 순서 보장·재전송·흐름 제어·혼잡 제어 등을 통해 데이터가 유실 없이 순서대로 전달되도록 보장하지만 그만큼 오버헤드가 있습니다. UDP는 비연결형 프로토콜로 연결 수립 과정이나 순서·재전송 보장이 없어 오버헤드가 적고 전송 속도가 빠르지만 데이터가 유실되거나 순서가 바뀔 수 있습니다. 그래서 파일 전송이나 웹 요청처럼 정확한 전달이 중요한 서비스는 TCP를, 실시간 스트리밍이나 온라인 게임처럼 약간의 손실보다 지연 없는 전달이 더 중요한 서비스는 UDP를 사용하는 경우가 많습니다.');
SET @e59 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('TCP가 송신 속도를 조절하는 두 가지 이유', 'TCP의 흐름 제어(Flow Control)와 혼잡 제어(Congestion Control)의 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '흐름 제어는 수신자의 처리 속도에 맞춰 송신량을 조절하는 것으로, 수신 측이 자신의 수신 버퍼 여유 공간(윈도우 크기)을 송신 측에 알려주어 그 크기를 넘지 않게 슬라이딩 윈도우로 전송량을 조정합니다. 혼잡 제어는 네트워크 경로 자체의 혼잡으로 인한 전체적인 성능 저하와 패킷 손실을 막기 위한 것으로, 느린 시작(Slow Start)으로 전송량을 서서히 늘리다가 패킷 손실이 감지되면 혼잡 윈도우를 줄이는 방식으로 동작합니다. 즉 흐름 제어는 송신자와 수신자 사이의 국지적 문제를, 혼잡 제어는 네트워크 전체 경로의 문제를 다룬다는 점에서 대상이 다릅니다.');
SET @e60 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로세스가 네트워크로 통신하는 창구', '소켓(Socket)이 무엇이며, 서버 소켓 프로그래밍에서 연결이 수립되기까지의 과정을 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 '소켓은 애플리케이션이 네트워크를 통해 데이터를 주고받을 수 있도록 운영체제가 제공하는 통신 종단점으로, IP 주소와 포트 번호의 조합으로 식별됩니다. 서버는 소켓을 생성(socket)한 뒤 특정 포트에 바인딩(bind)하고 연결 요청을 받을 준비 상태로 만들어(listen) 대기하다가, 클라이언트가 연결을 시도하면 accept를 통해 실제 통신에 사용할 새로운 소켓을 만들어 통신을 시작합니다. 클라이언트는 서버의 IP와 포트로 connect를 호출해 연결을 요청하며, 이 과정에서 내부적으로 TCP라면 3-way handshake가 함께 이루어집니다.');
SET @e61 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('서버가 이전 요청을 기억하지 않는다는 것의 의미', 'HTTP의 무상태성(Stateless)이 무엇인지 설명하고, 이로 인해 얻는 이점과 실무에서 상태를 유지해야 할 때의 해결 방법을 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 'HTTP는 각 요청을 이전 요청과 독립적으로 처리하며 서버가 클라이언트의 이전 상태를 기억하지 않는 무상태 프로토콜로, 요청마다 필요한 정보를 전부 담아 보내야 합니다. 이 덕분에 서버는 특정 클라이언트의 상태를 별도로 관리하지 않아도 되어 서버를 여러 대로 수평 확장하기 쉽고, 어느 서버가 요청을 처리해도 결과가 동일합니다. 다만 로그인 상태처럼 실제로는 상태를 유지해야 하는 경우가 많아, 쿠키·세션이나 토큰을 요청마다 함께 실어 보내는 방식으로 애플리케이션 계층에서 상태를 흉내 내어 처리합니다.');
SET @e62 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('브라우저가 사이트의 인증서를 믿는 이유', 'HTTPS에서 인증서 기반 신뢰 체계가 동작하는 방식과 CA(인증 기관)의 역할을 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '서버는 자신의 공개키와 신원 정보를 담은 인증서를 CA(Certificate Authority)라는 신뢰할 수 있는 제3의 기관에게 서명받아 제시하며, 브라우저는 이 인증서가 자신이 미리 신뢰하고 있는 루트 CA(또는 그 하위 중간 CA)의 서명으로 발급되었는지를 검증해 서버의 신원을 확인합니다. 이 신뢰 체계 덕분에 클라이언트는 처음 접속하는 서버라도 사설 기관이 아닌 공인된 CA가 신원을 보증했다는 사실만으로 그 서버가 실제로 주장하는 도메인의 소유자인지 신뢰할 수 있습니다. 만약 인증서가 만료되었거나 신뢰 체인에 있는 CA가 신뢰 목록에 없다면 브라우저는 경고를 띄워 사용자가 접속을 계속할지 판단하게 합니다.');
SET @e63 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이벤트가 생겼을 때 알아내는 두 가지 방법', '웹훅(Webhook)과 폴링(Polling)의 동작 방식 차이를 설명하고, 각각이 적합한 상황을 비교하시오.', 'ESSAY', 'LOW', 'NETWORK',
 '폴링은 클라이언트가 주기적으로 서버에 새로운 이벤트나 데이터가 있는지 반복해서 요청을 보내 확인하는 방식으로, 구현이 간단하지만 이벤트가 없을 때도 계속 요청을 보내야 해 불필요한 네트워크·서버 자원을 소모하고 실시간성도 폴링 주기에 좌우됩니다. 웹훅은 반대로 이벤트가 실제로 발생했을 때 이벤트를 발생시킨 쪽(서버)이 미리 등록된 URL로 직접 HTTP 요청을 보내 알려주는 방식으로, 불필요한 요청 없이 이벤트가 생긴 즉시 전달되어 자원 효율과 실시간성이 더 좋습니다. 다만 웹훅은 알림을 받는 쪽이 외부에서 접근 가능한 엔드포인트를 미리 노출해두어야 하고, 전달 실패 시 재시도 처리를 별도로 구현해야 하는 부담이 있습니다.');
SET @e64 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('먼 곳의 사용자도 콘텐츠를 빠르게 받는 방법', 'CDN(Content Delivery Network)이 무엇이며, 이를 사용했을 때 응답 속도가 개선되는 원리를 설명하시오.', 'ESSAY', 'LOW', 'NETWORK',
 'CDN은 원본 서버의 콘텐츠(이미지, 정적 파일 등)를 세계 여러 지역에 분산된 캐시 서버(엣지 서버)에 미리 복제해두고, 사용자의 요청이 오면 지리적으로 가장 가까운 엣지 서버가 대신 응답하는 시스템입니다. 사용자가 물리적으로 먼 원본 서버까지 왕복하지 않고 가까운 엣지 서버에서 콘텐츠를 받기 때문에 네트워크 지연(latency)이 줄어들고, 원본 서버로 가는 요청 자체도 줄어들어 원본 서버의 부하도 함께 낮아집니다. 엣지 서버에 콘텐츠가 없거나 캐시가 만료된 경우에는 원본 서버까지 요청을 전달(origin fetch)해 콘텐츠를 가져온 뒤 캐시에 저장하고 사용자에게 응답합니다.');
SET @e65 = LAST_INSERT_ID();

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e4, 'HTTPS'), (@e4, 'TLS 핸드셰이크'),
(@e5, '3-way handshake'), (@e5, '4-way handshake'),
(@e6, 'REST'), (@e6, 'API 설계'), (@e6, 'Stateless'),
(@e44, 'DNS'), (@e44, '네임서버'), (@e44, '캐시'), (@e44, 'TTL'),
(@e45, '로드밸런싱'), (@e45, '라운드 로빈'), (@e45, '최소 연결'), (@e45, '세션 고정'),
(@e46, '패킷 스위칭'), (@e46, '서킷 스위칭'), (@e46, '라우팅'),
(@e47, '포워드 프록시'), (@e47, '리버스 프록시'),
(@e48, '웹소켓'), (@e48, '롱폴링'), (@e48, '실시간 통신'),
(@e49, '멱등성'), (@e49, 'HTTP 메서드'), (@e49, 'PUT'), (@e49, 'POST'),
(@e50, 'CORS'), (@e50, '동일 출처 정책'), (@e50, 'Preflight'),
(@e51, 'OSI 7계층'), (@e51, '네트워크 계층'), (@e51, '전송 계층'),
(@e52, 'ARP'), (@e52, 'MAC 주소'), (@e52, '브로드캐스트'),
(@e53, 'NAT'), (@e53, '사설 IP'), (@e53, '포트 변환'),
(@e54, '서브넷팅'), (@e54, '서브넷 마스크'), (@e54, 'IP 주소'),
(@e55, '캐시 헤더'), (@e55, 'Cache-Control'), (@e55, 'ETag'),
(@e56, 'gRPC'), (@e56, 'REST'), (@e56, 'Protocol Buffers'), (@e56, 'HTTP/2'),
(@e57, 'HTTP/1.1'), (@e57, 'HTTP/2'), (@e57, 'HTTP/3'), (@e57, 'QUIC'),
(@e58, '쿠키'), (@e58, '세션'), (@e58, 'JWT'), (@e58, '인증'),
(@e59, 'TCP'), (@e59, 'UDP'), (@e59, '신뢰성'), (@e59, '연결형'),
(@e60, '흐름 제어'), (@e60, '혼잡 제어'), (@e60, '슬라이딩 윈도우'),
(@e61, '소켓'), (@e61, '소켓 프로그래밍'), (@e61, '포트'),
(@e62, 'Stateless'), (@e62, '무상태성'), (@e62, '수평 확장'),
(@e63, 'HTTPS'), (@e63, '인증서'), (@e63, 'CA'), (@e63, '신뢰 체인'),
(@e64, '웹훅'), (@e64, '폴링'), (@e64, '이벤트 기반'),
(@e65, 'CDN'), (@e65, '엣지 서버'), (@e65, '지연시간');

-- 카테고리: ALGORITHM (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Big-O 표기법의 의미', 'Big-O 표기법이 나타내는 것과, 이를 이용해 알고리즘의 효율을 비교하는 방법을 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 'Big-O 표기법은 입력 크기 n이 커질 때 알고리즘의 실행 시간(또는 메모리 사용량)이 얼마나 증가하는지를 최고차항 위주로 나타낸 것으로, 상수나 하위 차수 항은 무시하고 증가율의 상한을 나타냅니다. 예를 들어 O(n)인 알고리즘은 입력이 2배 늘면 시간도 대략 2배 늘고, O(n^2)인 알고리즘은 입력이 2배 늘면 시간이 4배로 늘어나므로, 입력이 커질수록 두 알고리즘의 실제 성능 차이는 크게 벌어집니다. 이를 통해 특정 입력값에서의 실행 시간이 아니라, 입력 규모가 커질 때의 확장성을 기준으로 알고리즘들을 객관적으로 비교할 수 있습니다.');
SET @e7 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('동적 프로그래밍과 분할정복', '동적 프로그래밍과 분할정복의 차이를 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 '분할정복은 문제를 서로 독립적인 작은 부분 문제로 나누어 각각을 재귀적으로 해결한 뒤 결과를 합치는 방식으로, 부분 문제들이 겹치지 않는 것을 전제로 합니다(예: 병합 정렬). 동적 프로그래밍은 부분 문제들이 중복되어 반복 계산되는 경우, 한 번 계산한 부분 문제의 결과를 메모이제이션이나 테이블에 저장해 재사용함으로써 중복 계산을 없애는 방식입니다. 즉 두 기법 모두 문제를 부분 문제로 나눈다는 점은 같지만, 부분 문제가 겹치는지 여부와 그 결과를 재사용하는지가 핵심적인 차이입니다.');
SET @e8 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('퀵 정렬과 병합 정렬', '퀵 정렬과 병합 정렬의 동작 원리와 시간복잡도 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '퀵 정렬은 피벗을 기준으로 작은 값과 큰 값을 좌우로 분할한 뒤 각 부분을 재귀적으로 정렬하는 방식으로, 평균 시간복잡도는 O(n log n)이지만 피벗 선택이 나쁘면 최악의 경우 O(n^2)까지 나빠질 수 있고 추가 메모리를 거의 쓰지 않는 in-place 정렬입니다. 병합 정렬은 배열을 절반씩 나눠 각각 정렬한 뒤 두 정렬된 배열을 합치는 방식으로, 항상 O(n log n)을 보장하지만 병합 과정에서 추가 배열이 필요해 O(n)의 추가 메모리를 사용합니다. 따라서 메모리가 제한적이고 평균 성능이 중요하면 퀵 정렬을, 안정적인 성능과 안정 정렬(stable sort)이 필요하면 병합 정렬을 선택하는 경우가 많습니다.');
SET @e9 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그리디 알고리즘의 특징', '그리디 알고리즘이 각 단계에서 어떤 방식으로 선택을 하는지와, 이 방식이 항상 최적해를 보장하지 못하는 이유를 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '그리디 알고리즘은 전체 최적해를 고려하지 않고 매 단계에서 현재 시점에 가장 좋아 보이는 선택(local optimum)을 반복적으로 선택해 나가는 방식입니다. 이 방식은 이전 선택을 번복하지 않고 계산이 빠르지만, 부분적으로 최적인 선택들의 합이 전체 최적해와 일치한다는 보장(탐욕적 선택 속성, 최적 부분 구조)이 없는 문제에서는 잘못된 답을 낼 수 있습니다. 예를 들어 동전 교환 문제는 동전 단위가 특정 조건을 만족할 때만 그리디로 최적해를 구할 수 있고, 그렇지 않으면 동적 프로그래밍 등으로 모든 경우를 고려해야 합니다.');
SET @e66 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('백트래킹의 동작 방식', '백트래킹이 완전 탐색과 어떻게 다른지, 가지치기(pruning)가 어떤 역할을 하는지 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '완전 탐색은 가능한 모든 경우의 수를 끝까지 탐색해 답을 찾는 방식이지만, 백트래킹은 해를 구성해 나가는 과정에서 현재까지의 선택이 이미 조건을 위반한 경우 더 이상 진행하지 않고 그 지점에서 즉시 되돌아가(backtrack) 다음 후보를 시도합니다. 이때 조건을 위반해 답이 될 수 없음이 확실한 하위 트리 전체를 탐색하지 않고 건너뛰는 것이 가지치기이며, 이를 통해 실질적인 탐색 범위를 크게 줄일 수 있습니다. 즉 백트래킹은 완전 탐색의 한 형태이지만, 가지치기를 통해 불필요한 탐색을 생략함으로써 평균적인 성능을 개선한 기법입니다.');
SET @e67 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('투 포인터 기법의 활용', '투 포인터 기법이 무엇이며 어떤 상황에서 시간복잡도를 개선할 수 있는지 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '투 포인터 기법은 정렬된 배열이나 리스트에서 두 개의 인덱스(포인터)를 각각 다른 위치에서 시작시켜 조건에 따라 이동시키며 문제를 해결하는 방법으로, 두 포인터가 서로 반대 방향에서 좁혀 오거나 같은 방향으로 함께 이동하는 형태로 사용됩니다. 예를 들어 정렬된 배열에서 두 수의 합이 특정 값이 되는 쌍을 찾을 때, 이중 for문으로 모든 쌍을 확인하면 O(n^2)이 걸리지만 투 포인터를 이용하면 각 포인터가 최대 n번씩만 이동해 O(n)으로 해결할 수 있습니다. 이처럼 불필요하게 중복 탐색되는 구간을 포인터의 이동 방향과 조건으로 걸러내어 시간복잡도를 낮추는 것이 핵심입니다.');
SET @e68 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('슬라이딩 윈도우 기법의 원리', '슬라이딩 윈도우 기법이 부분 배열/부분 문자열 문제를 푸는 원리와, 이를 사용하지 않았을 때와의 시간복잡도 차이를 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '슬라이딩 윈도우는 일정한 크기 또는 가변 크기의 구간(윈도우)을 배열 위에서 이동시키며 문제를 해결하는 기법으로, 윈도우가 한 칸 이동할 때 새로 포함되는 원소만 더하고 빠지는 원소만 빼는 방식으로 구간의 합이나 조건을 갱신합니다. 이런 최적화 없이 매번 윈도우 내의 모든 원소를 처음부터 다시 계산하면 윈도우 개수만큼 O(n)의 계산을 반복해 전체 O(n*k) 또는 O(n^2)이 걸리지만, 슬라이딩 윈도우는 이전 결과를 재사용해 전체를 O(n)으로 줄일 수 있습니다. 이 기법은 투 포인터와 함께 자주 쓰이며, 연속된 구간이라는 조건이 있는 문제에서 특히 효과적입니다.');
SET @e69 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('다익스트라 알고리즘의 동작 원리', '다익스트라 알고리즘이 최단 경로를 구하는 절차를 설명하고, 음수 가중치가 있는 그래프에 적용할 수 없는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '다익스트라 알고리즘은 시작 노드로부터의 거리가 확정되지 않은 노드 중 현재까지 거리가 가장 짧은 노드를 우선순위 큐 등으로 선택해 확정하고, 그 노드를 거쳐 갈 수 있는 인접 노드들의 거리를 갱신하는 과정을 모든 노드가 확정될 때까지 반복하는 그리디 방식입니다. 이 알고리즘은 한 번 최단 거리로 확정된 노드의 거리 값이 이후에 더 줄어들지 않는다는 전제 위에서 동작하는데, 음수 가중치가 존재하면 이미 확정한 노드보다 나중에 더 짧은 경로가 나타날 수 있어 이 전제가 깨집니다. 따라서 음수 가중치가 있는 그래프에서는 다익스트라 대신 벨만-포드 알고리즘처럼 모든 경로를 반복적으로 완화(relaxation)하는 방법을 사용해야 합니다.');
SET @e70 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('다익스트라와 벨만-포드의 차이', '다익스트라 알고리즘과 벨만-포드 알고리즘의 차이를 시간복잡도와 처리 가능한 그래프의 조건 측면에서 비교하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 '다익스트라는 매 단계에서 확정되지 않은 노드 중 최단 거리가 가장 짧은 노드를 그리디하게 선택해 진행하므로 우선순위 큐를 사용할 경우 O((V+E) log V) 정도로 빠르지만, 음수 가중치가 있으면 정확한 답을 보장하지 못합니다. 벨만-포드는 모든 간선을 최대 V-1번 반복해서 완화(relaxation)하는 방식으로 동작해 시간복잡도가 O(V*E)로 다익스트라보다 느리지만, 음수 가중치를 허용하며 V번째 반복에서도 거리가 갱신되는 간선이 있으면 음수 순환(negative cycle)이 존재함을 탐지할 수 있습니다. 따라서 음수 가중치가 없는 그래프에서는 다익스트라가, 음수 가중치가 있거나 음수 순환 탐지가 필요한 경우에는 벨만-포드가 적합합니다.');
SET @e71 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('크루스칼과 프림 알고리즘의 차이', '최소 스패닝 트리(MST)를 구하는 크루스칼 알고리즘과 프림 알고리즘의 동작 방식 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '크루스칼 알고리즘은 그래프의 모든 간선을 가중치 기준으로 정렬한 뒤, 가중치가 작은 간선부터 순서대로 확인하면서 사이클을 만들지 않는 간선만 선택해 트리를 완성해 나가며, 사이클 판별에는 주로 유니온-파인드를 사용합니다. 프림 알고리즘은 임의의 한 노드에서 시작해 현재까지 트리에 포함된 노드들과 연결된 간선 중 가중치가 가장 작은 간선을 하나씩 선택해 트리를 확장해 나가는 방식으로, 노드 집합을 하나의 트리로 계속 키워 나간다는 점에서 다익스트라와 유사한 구조를 가집니다. 즉 크루스칼은 간선 중심으로 전역적인 관점에서 선택하고, 프림은 노드 중심으로 현재 트리에서 가까운 간선을 확장해 나간다는 차이가 있습니다.');
SET @e72 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('위상 정렬이 필요한 상황', '위상 정렬(Topological Sort)이 어떤 문제에 사용되며, 이를 수행할 수 없는 그래프의 조건을 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '위상 정렬은 방향 그래프에서 노드 간의 선행 관계(의존성)를 지키면서 모든 노드를 순서대로 나열하는 것으로, 작업 스케줄링이나 빌드 순서 결정, 강의 선수 과목 순서 결정처럼 순서 제약이 있는 문제에 사용됩니다. 대표적으로 진입 차수가 0인 노드부터 큐에 넣어 하나씩 꺼내며 그 노드에서 나가는 간선들의 목적지 노드의 진입 차수를 줄이고, 진입 차수가 0이 되면 다시 큐에 넣는 방식(Kahn 알고리즘)으로 구현합니다. 그래프 안에 사이클이 존재하면 그 사이클에 포함된 노드들은 서로가 서로의 선행 조건이 되어 진입 차수가 영원히 0이 되지 않는 노드가 남으므로, 위상 정렬은 사이클이 없는 방향 그래프(DAG)에서만 수행할 수 있습니다.');
SET @e73 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('KMP 문자열 탐색 알고리즘', 'KMP 알고리즘이 단순 문자열 탐색보다 효율적인 이유를 실패 함수(failure function)의 역할과 함께 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 '단순 문자열 탐색은 패턴이 텍스트의 특정 위치에서 불일치하면 텍스트의 시작 위치를 한 칸만 옮겨 처음부터 다시 비교하기 때문에 최악의 경우 O(N*M)이 걸립니다. KMP는 패턴 내부에서 접두사와 접미사가 일치하는 최대 길이를 미리 계산한 실패 함수(부분 일치 테이블)를 이용해, 불일치가 발생했을 때 이미 일치했던 부분 중 다시 비교할 필요가 없는 구간을 건너뛰고 텍스트를 되돌아가지 않은 채 다음 비교를 이어갈 수 있습니다. 이 덕분에 텍스트 포인터가 뒤로 돌아가는 일이 없어 전체 탐색을 O(N+M)에 수행할 수 있습니다.');
SET @e74 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 탐색의 응용 범위', '이진 탐색이 정렬된 배열에서 값을 찾는 것 외에 어떤 문제에 응용될 수 있는지, 적용 조건과 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '이진 탐색은 탐색 대상이 반드시 정렬된 배열의 값이 아니어도, "어떤 값 x 이하(또는 이상)에서는 조건을 만족하고 그 반대편에서는 만족하지 않는" 단조성(monotonicity)을 가지는 경우 답이 될 수 있는 값의 범위 자체를 절반씩 좁혀가며 최적의 값을 찾는 매개변수 탐색(parametric search)에 응용할 수 있습니다. 예를 들어 "나무를 최대 몇 미터로 잘라야 필요한 나무의 총량을 확보할 수 있는가"와 같은 문제는 자르는 높이가 커질수록 확보량이 단조감소하므로, 높이를 이진 탐색하며 각 높이에서 조건을 만족하는지 확인해 답을 찾습니다. 이처럼 이진 탐색을 응용하려면 탐색 범위 전체에 대해 조건 만족 여부를 판별하는 함수가 단조적이어야 한다는 전제가 필요합니다.');
SET @e75 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온-파인드 자료구조', '유니온-파인드(Union-Find, 분리집합) 자료구조가 어떤 문제를 해결하며, 경로 압축과 랭크에 의한 합치기가 성능에 어떤 영향을 주는지 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '유니온-파인드는 서로소인 여러 집합을 관리하며 두 원소가 같은 집합에 속하는지 확인(find)하고 두 집합을 하나로 합치는(union) 연산을 지원하는 자료구조로, 그래프의 사이클 판별이나 크루스칼 알고리즘의 최소 스패닝 트리 구성 등에 사용됩니다. 최적화 없이 구현하면 트리가 한쪽으로 길게 늘어져 find 연산이 O(n)까지 느려질 수 있는데, 경로 압축(path compression)은 find 과정에서 방문한 노드들을 모두 루트에 직접 연결해 다음 탐색을 빠르게 만들고, 랭크(또는 크기)에 의한 합치기는 항상 더 작은 트리를 큰 트리 아래에 붙여 트리의 높이가 커지는 것을 방지합니다. 두 최적화를 함께 사용하면 각 연산의 시간복잡도를 거의 O(1)에 가깝게(상각 O(α(n))) 만들 수 있습니다.');
SET @e76 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('메모이제이션과 타뷸레이션의 차이', '동적 프로그래밍을 구현하는 두 가지 방식인 메모이제이션(Top-Down)과 타뷸레이션(Bottom-Up)의 차이를 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '메모이제이션은 재귀 호출로 문제를 위에서 아래로 쪼개어 내려가다가, 이미 계산한 부분 문제의 결과를 캐시(배열이나 맵)에 저장해두고 같은 부분 문제를 다시 만나면 캐시된 값을 즉시 반환하는 하향식(Top-Down) 방식입니다. 타뷸레이션은 가장 작은 부분 문제부터 테이블에 순서대로 값을 채워 올라가는 상향식(Bottom-Up) 방식으로, 재귀 호출이 없어 함수 호출 오버헤드와 스택 오버플로우 위험이 없지만 모든 부분 문제를 항상 계산해야 합니다. 따라서 일부 부분 문제만 실제로 필요한 경우에는 메모이제이션이, 대부분의 부분 문제를 결국 다 계산해야 하는 경우에는 타뷸레이션이 더 유리한 경우가 많습니다.');
SET @e77 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('재귀 호출과 스택 오버플로우', '재귀 함수가 스택 오버플로우를 일으킬 수 있는 이유와, 이를 방지하거나 완화하는 방법을 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '함수가 호출될 때마다 지역 변수와 복귀 주소 등을 담은 스택 프레임이 콜 스택에 쌓이는데, 재귀 함수가 종료 조건에 도달하지 못하거나 재귀 깊이가 매우 깊어지면 콜 스택에 쌓이는 프레임이 스레드에 할당된 스택 메모리 한도를 초과해 스택 오버플로우가 발생합니다. 이를 방지하려면 재귀를 반복문과 명시적인 스택 자료구조를 사용한 반복적(iterative) 형태로 바꾸어 콜 스택 사용을 줄이거나, 종료 조건이 반드시 유한한 횟수 내에 도달하도록 로직을 점검해야 합니다. 일부 언어나 컴파일러는 함수의 마지막 동작이 자기 자신의 재귀 호출인 꼬리 재귀(tail recursion)를 감지해 스택 프레임을 재사용하는 최적화를 제공하지만, Java와 같은 환경은 이 최적화를 보장하지 않아 재귀 깊이 관리에 더 주의해야 합니다.');
SET @e78 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('시간-공간 트레이드오프', '알고리즘 설계에서 시간-공간 트레이드오프가 무엇인지, 구체적인 예를 들어 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '시간-공간 트레이드오프는 실행 시간을 줄이기 위해 추가적인 메모리 공간을 사용하거나, 반대로 메모리 사용을 줄이기 위해 더 많은 연산 시간을 감수하는 설계상의 균형을 말합니다. 예를 들어 동적 프로그래밍에서 이미 계산한 부분 문제의 결과를 배열이나 해시맵에 저장해 두면(메모이제이션) 같은 계산을 반복하지 않아 시간은 크게 줄지만 그 결과를 저장할 추가 메모리가 필요하며, 반대로 메모리가 부족한 환경에서는 저장을 포기하고 필요할 때마다 다시 계산해 시간을 더 쓰는 선택을 할 수 있습니다. 이처럼 어느 쪽을 우선할지는 주어진 하드웨어 자원과 요구되는 응답 시간 등 문제의 제약 조건에 따라 달라집니다.');
SET @e79 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('NP-완전 문제의 의미', 'P, NP, NP-완전 문제의 관계를 설명하고, 어떤 문제가 NP-완전임을 증명하는 것이 실무적으로 어떤 의미를 갖는지 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 'P는 다항 시간 안에 풀 수 있는 문제들의 집합이고, NP는 어떤 해가 주어졌을 때 그것이 답인지를 다항 시간 안에 검증할 수 있는 문제들의 집합이며, NP-완전 문제는 NP에 속하면서 NP에 속한 모든 문제를 다항 시간 안에 이 문제로 환원(reduction)할 수 있는, 즉 NP 중에서 가장 어려운 문제들의 부분집합입니다. 어떤 문제가 NP-완전임이 증명되면, 그 문제를 다항 시간 안에 정확히 푸는 알고리즘을 아직 아무도 찾지 못했고(P=NP가 증명되지 않는 한) 앞으로도 찾기 어려울 것이라는 강한 신호로 받아들여집니다. 따라서 실무에서는 이런 문제를 정확히 푸는 대신, 제한된 시간 안에 근사해를 구하는 근사 알고리즘이나 휴리스틱, 또는 문제의 입력 조건을 제한해 다항 시간에 풀 수 있는 특수한 경우로 좁히는 접근을 택하는 경우가 많습니다.');
SET @e80 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('비트마스킹을 이용한 상태 표현', '비트마스킹이 무엇이며, 이를 이용해 집합이나 상태를 표현할 때 어떤 이점이 있는지 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '비트마스킹은 정수형 변수의 각 비트를 원소의 포함 여부나 상태의 on/off로 사용해, 여러 개의 불(boolean) 값이나 원소들로 이루어진 부분집합을 하나의 정수로 표현하는 기법입니다. 예를 들어 원소가 최대 20개인 집합의 모든 부분집합은 비트 연산(AND, OR, XOR, 시프트)만으로 O(1)에 포함 여부 확인·추가·제거를 할 수 있어, 배열이나 집합 자료구조로 같은 상태를 표현할 때보다 메모리 사용량과 연산 속도 모두에서 유리합니다. 이런 특성 때문에 외판원 순회(TSP)나 방문 상태를 추적해야 하는 동적 프로그래밍 문제에서 "어떤 원소들을 이미 방문했는가"와 같은 상태를 비트마스크로 표현해 상태 공간을 압축하는 데 자주 사용됩니다.');
SET @e81 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('최장 공통 부분열(LCS) 문제', '최장 공통 부분열(LCS) 문제를 동적 프로그래밍으로 해결하는 방법과, 최장 공통 부분 문자열(Substring)과의 차이를 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 'LCS는 두 문자열에서 순서는 유지하되 연속되지 않아도 되는 공통 부분열 중 가장 긴 것을 찾는 문제로, 두 문자열의 각 접두사 쌍에 대해 마지막 두 글자가 같으면 그 이전까지의 LCS 길이에 1을 더하고, 다르면 한쪽 글자를 제외한 두 경우 중 더 긴 값을 가져오는 점화식을 2차원 DP 테이블로 채워 O(n*m)에 해결합니다. 이 점화식이 성립하는 이유는 부분 문제들이 중복해서 나타나고, 전체 문제의 최적해가 부분 문제의 최적해로부터 구성되는 최적 부분 구조를 가지기 때문입니다. 반면 최장 공통 부분 문자열은 두 문자열에서 실제로 끊기지 않고 이어지는 공통 구간만을 인정한다는 점에서, 순서만 지키면 되는 LCS보다 더 엄격한 제약을 갖는 별개의 문제입니다.');
SET @e82 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('0/1 배낭 문제', '0/1 배낭 문제가 무엇이며, 이를 동적 프로그래밍으로 풀 때 무게를 기준으로 테이블을 구성하는 이유를 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 '0/1 배낭 문제는 무게 제한이 있는 배낭에 각각 무게와 가치를 가진 물건들을 넣되, 각 물건을 통째로 넣거나 아예 넣지 않는(쪼갤 수 없는) 선택만 가능할 때 배낭에 담을 수 있는 물건들의 가치 합을 최대화하는 문제입니다. 이 문제는 탐욕적으로 가치나 무게 대비 가치가 높은 물건부터 담아도 최적해를 보장하지 못하기 때문에, 물건 개수와 배낭의 남은 무게라는 두 축으로 부분 문제를 정의한 DP 테이블(dp[i][w])을 채워 각 물건을 넣을지 말지에 따른 두 경우 중 더 나은 값을 저장해 나가야 합니다. 무게를 축으로 삼는 이유는 담을 수 있는 남은 용량이 바로 앞으로 어떤 물건을 더 담을 수 있는지를 결정하는 상태이기 때문이며, 이 상태를 빠짐없이 표현해야 물건들의 모든 조합을 중복 없이 고려할 수 있습니다.');
SET @e83 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('정렬 알고리즘의 안정성', '정렬 알고리즘에서 안정 정렬(Stable Sort)이 무엇을 의미하는지, 그리고 이 특성이 왜 중요한지 예를 들어 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '안정 정렬은 정렬 기준이 되는 값이 같은 두 원소가 있을 때, 정렬 후에도 정렬 전의 상대적인 순서가 그대로 유지되는 정렬 방식을 말합니다. 예를 들어 이름으로 이미 정렬된 학생 목록을 다시 점수 기준으로 정렬할 때, 점수가 같은 학생들 사이에서는 원래의 이름 순서가 유지되어야 여러 기준으로 순차적으로 정렬(다중 키 정렬)한 결과가 사용자의 기대와 일치합니다. 안정 정렬이 아닌 알고리즘을 사용하면 같은 값을 가진 원소들의 순서가 뒤바뀔 수 있어, 이전에 적용한 정렬 기준의 정보가 사라지는 문제가 발생할 수 있습니다.');
SET @e84 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('해시를 이용한 알고리즘 최적화', '해시 자료구조를 활용해 브루트포스로 풀면 오래 걸리는 문제의 시간복잡도를 낮출 수 있는 원리를, 예시와 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '해시맵이나 해시셋은 평균적으로 O(1)에 삽입·조회가 가능하기 때문에, 이미 처리한 값이나 앞으로 필요한 값의 존재 여부를 매번 배열 전체를 탐색하지 않고 즉시 확인할 수 있게 해줍니다. 대표적으로 배열에서 합이 특정 값이 되는 두 수를 찾는 투 섬(Two Sum) 문제는 이중 for문으로 모든 쌍을 확인하면 O(n^2)이 걸리지만, 배열을 한 번 순회하면서 지금까지 나온 값과 그 인덱스를 해시맵에 저장해 두고 목표 값에서 현재 값을 뺀 나머지 값이 해시맵에 이미 있는지 확인하면 O(n)으로 해결할 수 있습니다. 이처럼 "이 값을 이전에 본 적이 있는가"를 반복해서 물어야 하는 문제에서는 해시를 이용해 탐색 비용을 O(1)로 낮추는 것이 시간복잡도를 크게 개선하는 핵심 아이디어입니다.');
SET @e85 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('버블·삽입·선택 정렬의 특징', '버블 정렬, 삽입 정렬, 선택 정렬의 동작 방식과 각각의 특징을 비교하여 설명하시오.', 'ESSAY', 'LOW', 'ALGORITHM',
 '버블 정렬은 인접한 두 원소를 비교해 순서가 잘못되어 있으면 계속 교환해 나가며 큰 값을 뒤로 밀어내는 방식이고, 선택 정렬은 매 회전마다 정렬되지 않은 구간에서 최솟값(또는 최댓값)을 찾아 맨 앞으로 교환하는 방식이며, 삽입 정렬은 이미 정렬된 앞부분에 새로운 원소를 적절한 위치에 끼워 넣는 방식으로 동작합니다. 세 알고리즘 모두 평균·최악 시간복잡도가 O(n^2)로 비효율적이지만, 삽입 정렬은 데이터가 이미 거의 정렬되어 있는 경우 비교와 이동 횟수가 크게 줄어 최선의 경우 O(n)에 가까운 성능을 보인다는 점에서 나머지 둘과 차별화됩니다. 선택 정렬은 교환 횟수가 최대 n-1번으로 적어 교환 비용이 큰 상황에 유리하고, 버블 정렬은 구현이 가장 단순하지만 교환이 잦아 실무에서는 잘 쓰이지 않습니다.');
SET @e86 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그래프의 인접 리스트와 인접 행렬', '그래프를 인접 리스트와 인접 행렬로 표현하는 방식의 차이와, 그래프의 특성(밀집/희소)에 따라 어떤 표현이 유리한지 설명하시오.', 'ESSAY', 'MEDIUM', 'ALGORITHM',
 '인접 행렬은 노드 개수만큼의 정사각 배열을 만들어 각 칸에 두 노드 사이의 연결 여부나 가중치를 저장하는 방식으로, 두 노드가 연결되어 있는지를 O(1)에 확인할 수 있지만 노드 개수의 제곱에 비례하는 O(V^2) 메모리를 항상 사용합니다. 인접 리스트는 각 노드마다 자신과 연결된 노드들의 목록만 저장하는 방식으로, 메모리 사용량이 실제 간선 수에 비례하는 O(V+E)로 줄지만 특정 두 노드의 연결 여부를 확인하려면 해당 노드의 목록을 순회해야 합니다. 따라서 노드 수에 비해 간선이 적은 희소 그래프에는 인접 리스트가, 간선이 많아 대부분의 노드가 서로 연결된 밀집 그래프이거나 연결 여부를 자주 O(1)로 확인해야 하는 경우에는 인접 행렬이 더 유리합니다.');
SET @e87 = LAST_INSERT_ID();

-- 서술형 태그 (ALGORITHM 카테고리 추가분)

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e7, 'Big-O'), (@e7, '시간복잡도'),
(@e8, '동적 프로그래밍'), (@e8, '분할정복'), (@e8, '메모이제이션'),
(@e9, '퀵 정렬'), (@e9, '병합 정렬'), (@e9, '정렬 알고리즘'),
(@e66, '그리디'), (@e66, '최적 부분 구조'), (@e66, '탐욕적 선택 속성'),
(@e67, '백트래킹'), (@e67, '가지치기'), (@e67, '완전 탐색'),
(@e68, '투 포인터'), (@e68, '시간복잡도 개선'), (@e68, '정렬된 배열'),
(@e69, '슬라이딩 윈도우'), (@e69, '부분 배열'), (@e69, '시간복잡도'),
(@e70, '다익스트라'), (@e70, '최단 경로'), (@e70, '그리디'),
(@e71, '다익스트라'), (@e71, '벨만-포드'), (@e71, '음수 순환'),
(@e72, '크루스칼'), (@e72, '프림'), (@e72, '최소 스패닝 트리'), (@e72, '유니온-파인드'),
(@e73, '위상 정렬'), (@e73, 'DAG'), (@e73, '진입 차수'),
(@e74, 'KMP'), (@e74, '문자열 탐색'), (@e74, '실패 함수'),
(@e75, '이진 탐색'), (@e75, '매개변수 탐색'), (@e75, '단조성'),
(@e76, '유니온-파인드'), (@e76, '경로 압축'), (@e76, '사이클 판별'),
(@e77, '메모이제이션'), (@e77, '타뷸레이션'), (@e77, '동적 프로그래밍'),
(@e78, '재귀'), (@e78, '스택 오버플로우'), (@e78, '콜 스택'),
(@e79, '시간-공간 트레이드오프'), (@e79, '메모이제이션'),
(@e80, 'NP-완전'), (@e80, 'P와 NP'), (@e80, '근사 알고리즘'),
(@e81, '비트마스킹'), (@e81, '상태 압축'), (@e81, '비트 연산'),
(@e82, 'LCS'), (@e82, '동적 프로그래밍'), (@e82, '최적 부분 구조'),
(@e83, '배낭 문제'), (@e83, '동적 프로그래밍'), (@e83, '최적화 문제'),
(@e84, '안정 정렬'), (@e84, '정렬 알고리즘'), (@e84, '다중 키 정렬'),
(@e85, '해시'), (@e85, '시간복잡도 최적화'), (@e85, '투 섬'),
(@e86, '버블 정렬'), (@e86, '선택 정렬'), (@e86, '삽입 정렬'),
(@e87, '인접 리스트'), (@e87, '인접 행렬'), (@e87, '그래프 표현');

-- 카테고리: DATA_STRUCTURE (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('배열과 연결리스트', '배열과 연결리스트의 구조적 차이와 각각의 장단점을 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '배열은 메모리상에 데이터가 연속적으로 저장되어 인덱스를 통해 O(1)로 임의 접근이 가능하지만, 크기가 고정되어 있거나 중간에 삽입·삭제할 때 뒤의 원소들을 옮겨야 해 O(n)이 걸립니다. 연결리스트는 각 노드가 다음 노드의 주소를 가리키는 형태로 흩어져 저장되어 특정 위치로의 삽입·삭제는 O(1)로 빠르지만, 임의 위치에 접근하려면 처음부터 노드를 따라가야 해 접근이 O(n)입니다. 따라서 조회가 잦으면 배열이, 삽입·삭제가 잦으면 연결리스트가 유리합니다.');
SET @e10 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('해시 충돌과 해결 방법', '해시테이블에서 해시 충돌이 발생하는 이유와 이를 해결하는 방법을 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '해시테이블은 키를 해시 함수로 변환해 배열의 인덱스로 사용하는데, 서로 다른 키가 같은 인덱스로 매핑되면 해시 충돌이 발생합니다. 이를 해결하는 대표적인 방법으로는 같은 인덱스에 매핑된 값들을 연결리스트 등으로 묶어 저장하는 체이닝(Chaining)과, 충돌이 나면 다른 빈 슬롯을 찾아 저장하는 오픈 어드레싱(Open Addressing)이 있습니다. 해시 함수가 키를 고르게 분산시키지 못하거나 저장된 데이터가 많아 적재율이 높아질수록 충돌이 잦아져 조회 성능이 O(1)에서 O(n)에 가깝게 나빠질 수 있습니다.');
SET @e11 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('이진 탐색 트리의 순회 방식', '이진 탐색 트리의 전위·중위·후위 순회 방식과 각각의 활용 사례를 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '전위 순회(Pre-order)는 루트를 먼저 방문한 뒤 왼쪽, 오른쪽 서브트리를 방문하는 방식으로 트리를 복제하거나 구조를 그대로 출력할 때 사용하고, 중위 순회(In-order)는 왼쪽, 루트, 오른쪽 순으로 방문해 이진 탐색 트리에서는 오름차순으로 정렬된 값을 얻을 수 있어 정렬된 목록이 필요할 때 사용합니다. 후위 순회(Post-order)는 왼쪽, 오른쪽, 루트 순으로 방문해 자식을 모두 처리한 뒤 부모를 처리해야 하는 경우(예: 트리 삭제, 디렉터리 용량 합산)에 사용합니다. 세 방식 모두 재귀 또는 스택을 이용한 반복문으로 구현할 수 있습니다.');
SET @e12 = LAST_INSERT_ID();

-- 카테고리: DATA_STRUCTURE 추가 22문항 (@e88 ~ @e109)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스택과 큐의 동작 방식', '스택(Stack)과 큐(Queue)의 데이터 처리 순서 차이와 각각이 적합한 사용 사례를 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '스택은 마지막에 들어온 데이터가 가장 먼저 나가는 후입선출(LIFO) 구조로, 함수 호출 스택이나 실행 취소(undo) 기능처럼 가장 최근 작업을 먼저 처리해야 하는 경우에 적합합니다. 큐는 먼저 들어온 데이터가 먼저 나가는 선입선출(FIFO) 구조로, 작업 대기열이나 프린터 출력처럼 요청이 들어온 순서대로 처리해야 하는 경우에 적합합니다. 두 구조 모두 삽입과 삭제가 각각 한쪽 끝(스택은 top, 큐는 front/rear)에서만 일어나 O(1)에 처리할 수 있습니다.');
SET @e88 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙과 우선순위 큐의 관계', '힙(Heap)이 우선순위 큐(Priority Queue)를 구현하는 데 사용되는 이유를 시간복잡도와 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '우선순위 큐는 매번 우선순위가 가장 높은(혹은 낮은) 원소를 꺼내야 하는데, 배열이나 연결리스트로 구현하면 최댓값을 찾는 데 O(n)이 걸리거나 정렬 상태를 유지하는 데 삽입마다 O(n)이 걸립니다. 힙은 부모가 항상 자식보다 크거나(최대 힙) 작다는(최소 힙) 부분 순서만 유지하는 완전 이진 트리로, 삽입과 삭제(루트 제거) 모두 트리의 높이에 비례한 O(log n)으로 처리할 수 있습니다. 이 때문에 표준 라이브러리의 우선순위 큐는 대부분 힙을 내부 구현으로 사용합니다.');
SET @e89 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트라이(Trie) 자료구조', '트라이(Trie)의 구조와, 이를 문자열 검색에 사용할 때 해시테이블 대비 갖는 장점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '트라이는 문자열의 각 문자를 트리의 각 레벨에 대응시켜, 공통 접두사를 가진 문자열들이 루트에서부터 같은 경로(노드)를 공유하도록 구성한 트리 구조입니다. 검색·삽입 시간이 저장된 문자열의 개수가 아니라 찾는 문자열의 길이에만 비례해 O(L)로 동작하며, 해시테이블처럼 충돌을 고려할 필요가 없습니다. 또한 접두사가 같은 단어를 모아 찾는 자동완성이나 사전 검색처럼, 해시테이블로는 어려운 접두사 기반 검색을 자연스럽게 지원한다는 장점이 있습니다.');
SET @e90 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그래프의 인접리스트와 인접행렬', '그래프를 인접리스트와 인접행렬로 표현하는 방식의 차이와, 각각이 유리한 상황을 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '인접행렬은 정점 개수만큼의 크기를 가진 2차원 배열에 두 정점 간 연결 여부를 저장해, 두 정점의 연결 여부를 O(1)로 바로 확인할 수 있지만 정점 수가 V일 때 O(V^2)의 공간을 항상 사용합니다. 인접리스트는 각 정점마다 자신과 연결된 정점들의 목록만 저장해, 간선 수가 E일 때 O(V+E)의 공간만 사용하지만 두 정점의 연결 여부를 확인하려면 해당 리스트를 순회해야 합니다. 따라서 간선이 적은 희소 그래프에는 인접리스트가, 간선이 많은 밀집 그래프이거나 연결 여부를 자주 확인해야 하면 인접행렬이 유리합니다.');
SET @e91 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('B+Tree가 DB 인덱스에 쓰이는 이유', 'B+Tree가 이진 탐색 트리 대신 DB 인덱스 구조로 널리 사용되는 이유를 디스크 I/O 관점에서 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 '이진 탐색 트리는 노드마다 자식이 최대 2개뿐이라 데이터가 많아지면 트리의 높이가 깊어져, 노드 하나가 디스크의 한 블록에 대응되는 DB 환경에서는 탐색 시 디스크 접근 횟수가 많아집니다. B+Tree는 하나의 노드(블록)에 여러 개의 키를 저장해 자식을 다수 가질 수 있는 다진 트리로, 같은 개수의 데이터를 저장하더라도 트리의 높이가 훨씬 낮아 탐색에 필요한 디스크 접근 횟수를 줄일 수 있습니다. 또한 실제 데이터는 리프 노드에만 저장하고 리프 노드들이 서로 연결 리스트로 이어져 있어, 범위 검색이나 순차 접근도 효율적으로 처리할 수 있습니다.');
SET @e92 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('레드블랙트리의 균형 유지 방식', '레드블랙트리가 노드에 색을 부여해 균형을 유지하는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 '레드블랙트리는 모든 노드를 빨강 또는 검정으로 표시하고, 루트와 리프는 검정이며 빨간 노드의 자식은 항상 검정이어야 하고 어떤 노드에서 리프까지 가는 모든 경로의 검정 노드 수가 같아야 한다는 규칙을 강제합니다. 삽입이나 삭제로 이 규칙이 깨지면 색을 다시 칠하거나(recoloring) 회전(rotation)을 수행해 규칙을 복구하는데, 이 과정이 트리의 높이를 항상 O(log n) 수준으로 제한합니다. 완벽하게 균형 잡힌 트리는 아니지만 삽입·삭제 시 재조정 비용이 AVL 트리보다 적어, Java의 TreeMap 등 실무 라이브러리에서 널리 사용됩니다.');
SET @e93 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('AVL 트리의 회전 연산', 'AVL 트리가 삽입·삭제 후 균형을 유지하기 위해 회전(Rotation)을 수행하는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 'AVL 트리는 각 노드마다 왼쪽 서브트리와 오른쪽 서브트리의 높이 차이(균형 인수)를 유지하며, 이 값이 -1, 0, 1을 벗어나면 불균형 상태로 간주합니다. 삽입이나 삭제로 균형 인수가 깨지면 불균형이 발생한 지점을 기준으로 좌회전이나 우회전, 또는 이 둘을 조합한 회전을 수행해 트리 구조를 재배치함으로써 균형을 되찾습니다. 이 덕분에 AVL 트리는 항상 엄격하게 균형이 잡혀 있어 탐색 성능은 레드블랙트리보다 근소하게 낫지만, 삽입·삭제마다 회전이 더 자주 발생해 갱신 비용은 더 클 수 있습니다.');
SET @e94 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('유니온-파인드와 경로 압축', '유니온-파인드(Disjoint Set) 자료구조의 목적과, 경로 압축(Path Compression)이 성능에 주는 효과를 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 '유니온-파인드는 서로 겹치지 않는 집합들을 관리하며 두 원소가 같은 집합에 속하는지(find) 확인하고 두 집합을 하나로 합치는(union) 연산을 지원하는 자료구조로, 사이클 판별이나 최소 신장 트리(크루스칼 알고리즘) 등에 사용됩니다. 트리 형태로 구현할 때 union을 반복하면 트리가 한쪽으로 길게 늘어져 find 연산이 O(n)까지 느려질 수 있는데, 경로 압축은 find를 수행하며 방문한 노드들이 곧바로 루트를 직접 가리키게 만들어 이후 탐색 경로를 단축시킵니다. 랭크에 따른 union까지 함께 적용하면 find와 union 모두 거의 O(1)에 가까운 상수 시간으로 처리할 수 있습니다.');
SET @e95 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('세그먼트 트리의 구간 질의', '세그먼트 트리가 배열의 구간 합(혹은 구간 최솟값 등) 질의를 빠르게 처리할 수 있는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 '배열에서 매 구간 질의마다 해당 구간을 순회해 합을 계산하면 질의당 O(n)이 걸리는데, 세그먼트 트리는 각 노드가 배열의 특정 구간에 대한 연산 결과(합, 최솟값 등)를 미리 저장해 두는 이진 트리를 구성합니다. 임의의 구간에 대한 질의가 들어오면 그 구간을 트리에서 겹치지 않는 O(log n)개의 노드 구간으로 분해해 결과를 조합할 수 있고, 배열의 특정 값이 갱신되면 그 값을 포함하는 경로의 노드들만 O(log n)에 갱신하면 됩니다. 따라서 값의 갱신과 구간 질의가 반복적으로 섞여 있는 경우 전체 순회보다 훨씬 효율적입니다.');
SET @e96 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('원형 큐가 필요한 이유', '배열로 큐를 구현할 때 발생하는 문제와, 원형 큐(Circular Queue)가 이를 해결하는 방식을 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '배열로 일반적인 큐를 구현하면 앞쪽에서 원소를 꺼낼 때마다 front 인덱스만 뒤로 이동시키므로, 배열의 앞부분 공간이 비어 있어도 재사용하지 못하고 rear가 배열 끝에 도달하면 더 이상 삽입할 수 없는 공간 낭비가 발생합니다. 원형 큐는 배열의 끝과 처음을 논리적으로 이어 붙여, rear나 front 인덱스가 배열 끝에 도달하면 나머지 연산(modulo)을 이용해 다시 처음 인덱스로 돌아가게 만듭니다. 이렇게 하면 배열의 모든 공간을 순환적으로 재사용할 수 있어, 고정된 크기의 배열로도 삽입·삭제를 계속 O(1)에 처리할 수 있습니다.');
SET @e97 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('덱(Deque)의 특징과 활용', '덱(Deque)이 스택 및 큐와 구조적으로 다른 점과, 이를 활용할 수 있는 사례를 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '스택은 한쪽 끝에서만, 큐는 한쪽 끝에서 삽입하고 다른 쪽 끝에서 삭제하는 데 반해, 덱(Double-Ended Queue)은 양쪽 끝 모두에서 삽입과 삭제가 O(1)로 가능한 자료구조입니다. 이 덕분에 덱 하나로 스택처럼도, 큐처럼도 사용할 수 있어 범용성이 높고, 슬라이딩 윈도우 알고리즘에서 윈도우의 양 끝 원소를 계속 넣고 빼야 하는 경우나 최근 사용 항목을 앞뒤로 유지해야 하는 경우에 활용됩니다. 배열 기반으로 구현할 때는 원형 큐처럼 양 끝 인덱스를 순환시키는 방식을, 연결리스트 기반으로 구현할 때는 이중 연결리스트를 사용하는 경우가 많습니다.');
SET @e98 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('LRU 캐시의 자료구조적 구현', 'LRU(Least Recently Used) 캐시를 해시맵과 이중 연결리스트를 결합해 구현하는 방식과, 그렇게 구현해야 하는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 'LRU 캐시는 특정 키로 값을 O(1)에 조회하면서도, 가장 오래 사용되지 않은 항목을 O(1)에 찾아 제거해야 하는데 해시맵만으로는 사용 순서를 추적할 수 없고 연결리스트만으로는 특정 키를 찾는 데 O(n)이 걸립니다. 해시맵은 키를 이중 연결리스트의 특정 노드에 매핑해 O(1) 조회를 담당하고, 이중 연결리스트는 노드를 사용 순서대로 유지해 항목이 접근될 때마다 해당 노드를 리스트의 맨 앞(또는 뒤)으로 옮기는 작업을 O(1)에 수행합니다. 캐시가 가득 차면 리스트의 반대쪽 끝(가장 오래 사용되지 않은 항목)을 O(1)에 제거함으로써, 조회·삽입·삭제 모두를 O(1)로 유지할 수 있습니다.');
SET @e99 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('그래프와 트리의 구조적 차이', '트리를 그래프의 특수한 형태로 볼 수 있는 이유와, 트리가 일반 그래프와 구조적으로 다른 점을 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '그래프는 정점과 간선의 집합으로 정점 간 연결에 특별한 제약이 없어 사이클이 존재할 수도 있고 그래프 전체가 여러 개의 연결되지 않은 부분으로 나뉠 수도 있습니다. 트리는 정점이 N개일 때 간선이 정확히 N-1개이며 사이클이 없고 모든 정점이 하나로 연결되어 있는 그래프로, 임의의 두 정점 사이에 경로가 정확히 하나만 존재한다는 성질을 갖습니다. 즉 트리는 사이클이 없는 연결 그래프라는 조건을 만족하는 그래프의 부분집합이라고 볼 수 있습니다.');
SET @e100 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('힙을 배열로 표현하는 방식', '힙을 포인터 기반 트리가 아닌 배열로 표현할 수 있는 이유와, 이때 부모·자식 인덱스를 계산하는 방식을 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '힙은 항상 완전 이진 트리 형태를 유지하기 때문에 노드들 사이에 빈틈이 생기지 않아, 트리를 위에서부터 왼쪽에서 오른쪽 순서로 나열하면 배열의 인덱스만으로 각 노드의 위치를 온전히 표현할 수 있습니다. 배열의 인덱스를 1부터 시작한다고 하면, 인덱스 i인 노드의 부모는 i/2, 왼쪽 자식은 2i, 오른쪽 자식은 2i+1로 계산할 수 있어 별도의 포인터 없이도 부모·자식 관계를 즉시 구할 수 있습니다. 이 방식은 포인터를 저장할 공간이 필요 없어 메모리 효율이 높고, 배열 기반이라 캐시 지역성도 좋아 실무에서 힙을 구현할 때 널리 사용됩니다.');
SET @e101 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('단일 연결리스트와 이중 연결리스트의 차이', '단일 연결리스트와 이중 연결리스트의 구조적 차이와, 이중 연결리스트가 추가 비용을 감수하고도 사용되는 이유를 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '단일 연결리스트는 각 노드가 다음 노드만을 가리키는 포인터 하나만 가져, 뒤에서 앞으로 거슬러 가려면 처음부터 다시 순회해야 하고 특정 노드를 삭제하려면 그 이전 노드를 알아야 합니다. 이중 연결리스트는 각 노드가 다음 노드와 이전 노드를 가리키는 포인터를 모두 가져, 양방향 순회가 가능하고 삭제하려는 노드 자체만 알고 있어도 이전 노드에 바로 접근해 O(1)에 삭제할 수 있습니다. 다만 노드마다 포인터를 하나 더 저장해야 해 메모리 사용량이 늘어나므로, 양방향 접근이나 빈번한 삭제가 필요한 경우(LRU 캐시 등)에 이 비용을 감수하고 이중 연결리스트를 사용합니다.');
SET @e102 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('해시맵의 로드 팩터와 리사이징', '해시맵에서 로드 팩터(Load Factor)가 의미하는 것과, 리사이징(Rehashing)이 필요한 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '로드 팩터는 해시맵에 저장된 데이터 개수를 버킷(배열 슬롯) 개수로 나눈 값으로, 이 값이 커질수록 하나의 버킷에 여러 데이터가 몰려 충돌이 잦아지고 조회·삽입 성능이 O(1)에서 점점 멀어집니다. 로드 팩터가 일정 임계값(예: 0.75)을 넘으면 해시맵은 내부 배열의 크기를 늘리고, 기존에 저장된 모든 데이터를 새 배열 크기에 맞춰 해시값을 다시 계산해 재배치하는 리사이징(Rehashing)을 수행합니다. 리사이징은 버킷당 데이터 수를 낮게 유지해 평균 성능을 O(1)에 가깝게 되돌리지만, 리사이징이 일어나는 시점의 삽입 연산은 전체 데이터를 옮기느라 일시적으로 비용이 커집니다.');
SET @e103 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('트리의 균형이 필요한 이유', '이진 탐색 트리가 균형을 잃었을 때 발생하는 문제를, 균형 잡힌 트리와 비교하여 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '이진 탐색 트리는 데이터를 정렬된 순서대로 삽입하는 등 특정 상황에서 한쪽으로만 자식이 이어지는 연결리스트와 같은 형태로 치우칠 수 있는데, 이렇게 치우친(degenerate) 트리는 트리의 높이가 노드 개수 n에 비례해 O(n)이 되어 탐색·삽입·삭제가 모두 O(n)까지 느려집니다. 균형 잡힌 트리는 왼쪽과 오른쪽 서브트리의 높이 차이를 일정 수준 이하로 유지해, 노드가 많아져도 트리의 높이를 O(log n) 수준으로 억제합니다. 이 때문에 AVL 트리나 레드블랙트리처럼 삽입·삭제 시마다 스스로 균형을 재조정하는 자가 균형 트리를 사용해 최악의 경우에도 로그 시간 성능을 보장합니다.');
SET @e104 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스킵 리스트의 탐색 원리', '스킵 리스트(Skip List)가 정렬된 연결리스트보다 빠르게 탐색할 수 있는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 '정렬된 단일 연결리스트는 특정 값을 찾으려면 처음부터 하나씩 순회해야 해 탐색에 O(n)이 걸립니다. 스킵 리스트는 원래의 연결리스트 위에 일부 노드만을 골라 연결한 상위 레벨 리스트를 여러 층으로 만들어, 상위 레벨에서 목표 값보다 작은 지점까지 성큰 이동한 뒤 아래 레벨로 내려가며 탐색 범위를 점점 좁혀 나갑니다. 각 레벨의 노드 수를 대략 절반씩 줄여나가면 평균적으로 O(log n)의 탐색 성능을 얻을 수 있어, 균형 트리의 대안으로 정렬된 데이터를 다루면서도 구현이 비교적 단순한 자료구조로 활용됩니다.');
SET @e105 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('큐를 이용한 BFS 구현', '너비 우선 탐색(BFS)에서 큐(Queue)가 반드시 필요한 이유를 스택을 사용하는 DFS와 비교하여 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 'BFS는 시작 노드로부터 가까운 노드부터 순서대로, 즉 레벨 단위로 탐색해야 하는데 큐는 먼저 들어온 노드를 먼저 꺼내는 FIFO 구조이므로 방문한 노드의 인접 노드들을 큐에 넣어두면 항상 더 가까운(먼저 발견된) 노드부터 처리되어 레벨 순서를 자연스럽게 보장합니다. 반대로 DFS는 한 경로를 끝까지 파고든 뒤 되돌아오는 방식이라, 가장 나중에 발견한 노드를 먼저 처리하는 LIFO 구조인 스택(혹은 재귀 호출 스택)을 사용해야 이러한 깊이 우선 순서가 만들어집니다. 만약 BFS에서 큐 대신 스택을 사용하면 레벨 순서가 깨져 실제로는 DFS와 유사한 탐색 순서가 되어버립니다.');
SET @e106 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('LFU 캐시의 자료구조적 구현', 'LFU(Least Frequently Used) 캐시를 O(1) 시간에 구현하기 위해 필요한 자료구조 구성을 설명하시오.', 'ESSAY', 'HIGH', 'DATA_STRUCTURE',
 'LFU 캐시는 접근 빈도가 가장 낮은 항목을 제거해야 하는데, 단순히 각 키의 빈도를 해시맵에 저장하는 것만으로는 매번 최소 빈도를 가진 키를 찾는 데 전체를 순회해야 해 O(n)이 걸립니다. 이를 O(1)에 처리하려면 키를 값과 빈도에 매핑하는 해시맵과, 같은 빈도를 가진 키들을 이중 연결리스트로 모아두고 그 빈도별 리스트들을 다시 빈도값을 키로 하는 또 다른 해시맵으로 관리하는 이중 구조가 필요합니다. 항목이 접근되면 기존 빈도 리스트에서 제거해 빈도+1인 리스트로 옮기고, 제거 시에는 현재 가장 낮은 빈도를 가리키는 포인터가 지시하는 리스트에서 항목을 꺼내면 조회·갱신·제거를 모두 O(1)에 수행할 수 있습니다.');
SET @e107 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('자료구조 선택 시 시간복잡도와 공간복잡도의 트레이드오프', '동일한 문제를 해결할 수 있는 여러 자료구조 중 하나를 선택할 때, 시간복잡도와 공간복잡도를 함께 고려해야 하는 이유를 예를 들어 설명하시오.', 'ESSAY', 'LOW', 'DATA_STRUCTURE',
 '같은 문제라도 배열은 접근이 빠르지만 크기 변경에 비용이 들고, 해시맵은 조회가 빠르지만 해시 함수와 버킷 배열에 추가 메모리가 필요하며, 트라이는 문자열 검색이 빠르지만 노드마다 자식 포인터를 여러 개 저장해 메모리 사용량이 큰 것처럼, 시간 효율과 공간 효율은 흔히 서로 상충합니다. 예를 들어 정수 범위가 작고 고정되어 있다면 해시맵 대신 배열로 카운팅해 해시 연산 없이 O(1) 접근과 더 적은 메모리를 동시에 얻을 수 있지만, 값의 범위가 크고 희소하다면 배열은 메모리를 낭비하므로 해시맵이 더 적합합니다. 따라서 자료구조를 선택할 때는 이론적인 시간복잡도만 볼 것이 아니라 실제 데이터의 규모, 접근 패턴, 사용 가능한 메모리 제약을 함께 고려해야 합니다.');
SET @e108 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스택과 재귀 호출의 관계', '함수의 재귀 호출이 내부적으로 스택 자료구조와 어떻게 연관되는지, 그리고 재귀가 스택 오버플로우를 유발할 수 있는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'DATA_STRUCTURE',
 '함수가 호출될 때마다 그 함수의 지역 변수와 복귀 주소 등을 담은 스택 프레임이 호출 스택(call stack)에 쌓이고, 함수가 종료되면 해당 프레임이 스택에서 제거되며 이전 호출 지점으로 되돌아가는데, 이는 후입선출 구조인 스택과 정확히 같은 방식으로 동작합니다. 재귀 함수는 종료 조건에 도달하기 전까지 자기 자신을 계속 호출하므로 호출 스택에 프레임이 계속 쌓이는데, 재귀 깊이가 지나치게 깊거나 종료 조건이 없으면 스택에 할당된 메모리 공간을 모두 소진해 스택 오버플로우가 발생합니다. 이런 문제를 피하기 위해 재귀를 반복문과 명시적 스택으로 변환하거나, 꼬리 재귀 최적화를 지원하는 환경에서는 이를 활용해 스택 프레임 누적을 줄이는 방법을 사용합니다.');
SET @e109 = LAST_INSERT_ID();

-- 서술형 태그

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e10, '배열'), (@e10, '연결리스트'),
(@e11, '해시테이블'), (@e11, '해시 충돌'), (@e11, '체이닝'),
(@e12, '이진 탐색 트리'), (@e12, '트리 순회'),
(@e88, '스택'), (@e88, '큐'), (@e88, 'LIFO'), (@e88, 'FIFO'),
(@e89, '힙'), (@e89, '우선순위 큐'), (@e89, '완전 이진 트리'),
(@e90, '트라이'), (@e90, '문자열 검색'), (@e90, '접두사'),
(@e91, '그래프'), (@e91, '인접리스트'), (@e91, '인접행렬'), (@e91, '희소 그래프'),
(@e92, 'B+Tree'), (@e92, '인덱스'), (@e92, '디스크 I/O'), (@e92, '다진 트리'),
(@e93, '레드블랙트리'), (@e93, '균형 트리'), (@e93, '회전'),
(@e94, 'AVL 트리'), (@e94, '회전'), (@e94, '균형 인수'),
(@e95, '유니온-파인드'), (@e95, '경로 압축'), (@e95, '사이클 판별'),
(@e96, '세그먼트 트리'), (@e96, '구간 질의'), (@e96, '구간 합'),
(@e97, '원형 큐'), (@e97, '배열 기반 큐'), (@e97, '공간 재사용'),
(@e98, '덱'), (@e98, '슬라이딩 윈도우'), (@e98, '양방향 삽입삭제'),
(@e99, 'LRU 캐시'), (@e99, '해시맵'), (@e99, '이중 연결리스트'),
(@e100, '그래프'), (@e100, '트리'), (@e100, '사이클'),
(@e101, '힙'), (@e101, '배열 표현'), (@e101, '완전 이진 트리'), (@e101, '인덱스 계산'),
(@e102, '단일 연결리스트'), (@e102, '이중 연결리스트'), (@e102, '양방향 순회'),
(@e103, '해시맵'), (@e103, '로드 팩터'), (@e103, '리사이징'), (@e103, '리해싱'),
(@e104, '이진 탐색 트리'), (@e104, '트리 균형'), (@e104, '최악의 경우 시간복잡도'),
(@e105, '스킵 리스트'), (@e105, '연결리스트'), (@e105, '다단계 인덱스'),
(@e106, 'BFS'), (@e106, '큐'), (@e106, 'DFS'), (@e106, '탐색 순서'),
(@e107, 'LFU 캐시'), (@e107, '해시맵'), (@e107, '이중 연결리스트'), (@e107, '빈도 관리'),
(@e108, '자료구조 선택'), (@e108, '시간복잡도'), (@e108, '공간복잡도'), (@e108, '트레이드오프'),
(@e109, '스택'), (@e109, '재귀'), (@e109, '호출 스택'), (@e109, '스택 오버플로우');

-- 카테고리: OS (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로세스와 스레드의 차이', '프로세스와 스레드의 차이를 메모리 구조 관점에서 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '프로세스는 코드·데이터·힙·스택을 포함한 독립된 메모리 공간을 각각 할당받아 서로 격리되어 있어, 한 프로세스가 다른 프로세스의 메모리에 직접 접근할 수 없습니다. 스레드는 같은 프로세스 내에서 코드·데이터·힙 영역을 다른 스레드들과 공유하고 스택만 각자 독립적으로 가져, 프로세스보다 생성·전환 비용이 적고 메모리 공유를 통한 통신이 쉽습니다. 다만 스레드는 메모리를 공유하기 때문에 동기화를 제대로 처리하지 않으면 race condition 같은 문제가 발생할 수 있습니다.');
SET @e13 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데드락 발생 조건과 예방', '데드락이 발생하는 4가지 조건과 이를 예방하는 방법을 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '데드락은 상호 배제(자원을 한 번에 한 프로세스만 점유), 점유와 대기(자원을 점유한 채 다른 자원을 기다림), 비선점(다른 프로세스가 점유한 자원을 강제로 빼앗을 수 없음), 순환 대기(프로세스들이 서로가 필요한 자원을 순환 형태로 기다림) 네 조건이 동시에 성립할 때 발생합니다. 이 중 하나라도 성립하지 않게 만들면 예방할 수 있는데, 예를 들어 자원에 순서를 정해 항상 같은 순서로 점유하게 하면 순환 대기를 없앨 수 있고, 필요한 자원을 한 번에 모두 할당받게 하면 점유와 대기를 없앨 수 있습니다. 실무에서는 완전한 예방보다 타임아웃이나 데드락 탐지 후 회복(자원 선점, 프로세스 종료) 전략을 함께 쓰는 경우도 많습니다.');
SET @e14 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('가상 메모리와 페이징', '가상 메모리와 페이징이 필요한 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '가상 메모리는 프로세스마다 실제 물리 메모리 크기와 무관하게 독립된 주소 공간을 제공해, 프로세스들이 서로의 메모리를 침범하지 못하게 격리하고 물리 메모리보다 큰 프로그램도 실행할 수 있게 합니다. 페이징은 이 가상 주소 공간과 물리 메모리를 동일한 크기의 페이지 단위로 나누어 매핑함으로써, 필요한 페이지만 물리 메모리에 올리고 당장 쓰지 않는 페이지는 디스크에 두는 방식으로 메모리를 효율적으로 사용합니다. 이 덕분에 여러 프로세스를 동시에 실행하면서도 실제 물리 메모리 용량의 제약을 상당 부분 극복할 수 있습니다.');
SET @e15 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('FCFS와 SJF 스케줄링', 'CPU 스케줄링 알고리즘인 FCFS(First-Come First-Served)와 SJF(Shortest Job First)의 동작 방식과 각각의 장단점을 비교하여 설명하시오.', 'ESSAY', 'LOW', 'OS',
 'FCFS는 도착한 순서대로 프로세스에 CPU를 할당하는 방식으로 구현이 단순하지만, 실행 시간이 긴 프로세스가 먼저 도착하면 뒤의 짧은 프로세스들이 오래 대기하는 컨보이 효과(convoy effect)가 발생할 수 있습니다. SJF는 실행 시간이 가장 짧은 프로세스를 먼저 실행해 평균 대기 시간을 최소화할 수 있지만, 실행 시간을 사전에 정확히 알기 어렵고 짧은 작업이 계속 들어오면 긴 작업이 무한정 밀리는 기아(starvation) 문제가 생길 수 있습니다. 즉 FCFS는 공정성과 단순성을, SJF는 평균 대기시간 최적화를 우선한 트레이드오프 관계에 있습니다.');
SET @e110 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Round Robin의 타임 슬라이스 크기', 'Round Robin 스케줄링의 동작 방식과, 타임 슬라이스(Time Quantum) 크기를 너무 작게 또는 크게 설정했을 때 각각 발생하는 문제를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 'Round Robin은 각 프로세스에 동일한 시간(타임 슬라이스)만큼 CPU를 순환 할당하고, 시간이 끝나면 컨텍스트 스위칭을 통해 다음 프로세스로 넘어가는 선점형 스케줄링 방식입니다. 타임 슬라이스를 너무 작게 설정하면 컨텍스트 스위칭이 지나치게 빈번히 발생해 그 오버헤드가 커져 전체 처리량이 떨어지고, 반대로 너무 크게 설정하면 여러 프로세스가 번갈아 실행되는 응답성이 떨어져 FCFS와 비슷하게 동작하게 됩니다. 따라서 타임 슬라이스는 컨텍스트 스위칭 오버헤드와 응답 시간 사이의 균형을 고려해 적절한 값으로 설정해야 합니다.');
SET @e111 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('선점형 스케줄링과 비선점형 스케줄링', '선점형(Preemptive) 스케줄링과 비선점형(Non-preemptive) 스케줄링의 차이를 실행 중인 프로세스의 처리 관점에서 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '비선점형 스케줄링은 한 번 CPU를 할당받은 프로세스가 스스로 종료하거나 I/O를 요청할 때까지 CPU를 계속 점유하며, 다른 프로세스나 스케줄러가 중간에 CPU를 빼앗을 수 없습니다. 선점형 스케줄링은 타이머 인터럽트나 더 높은 우선순위 프로세스의 도착 등의 조건이 되면 실행 중인 프로세스라도 스케줄러가 강제로 CPU를 회수해 다른 프로세스에 할당할 수 있습니다. 선점형 방식은 특정 프로세스가 CPU를 오래 독점하는 것을 막아 응답성이 좋지만 컨텍스트 스위칭이 더 자주 발생하고 공유 자원 접근 시 동기화를 신경 써야 하는 부담이 있습니다.');
SET @e112 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('컨텍스트 스위칭의 발생 시점과 비용', '컨텍스트 스위칭이 발생하는 대표적인 상황과, 컨텍스트 스위칭에 비용이 발생하는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '컨텍스트 스위칭은 타임 슬라이스가 만료되거나, 실행 중인 프로세스가 I/O 요청 등으로 대기 상태에 들어가거나, 더 높은 우선순위의 프로세스가 준비 상태가 되는 등 CPU를 다른 프로세스나 스레드로 넘겨야 할 때 발생합니다. 이때 CPU는 현재 실행 중인 프로세스의 레지스터 값, 프로그램 카운터 등 실행 상태를 PCB(Process Control Block)에 저장하고, 다음에 실행할 프로세스의 저장된 상태를 다시 불러와야 하므로 그 자체로 CPU 시간이 소요됩니다. 또한 프로세스 간 전환은 캐시나 TLB에 저장된 이전 프로세스의 데이터가 무효화되어 새 프로세스가 다시 채우는 과정에서 성능 저하(캐시 미스 증가)가 추가로 발생합니다.');
SET @e113 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인터럽트와 시스템 콜', '인터럽트(Interrupt)와 시스템 콜(System Call)의 차이를, 각각이 발생하는 주체와 목적을 중심으로 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '인터럽트는 하드웨어 장치(타이머, I/O 장치 등)나 예외 상황이 CPU에게 현재 작업을 멈추고 즉시 처리해야 할 이벤트가 생겼음을 알리는 신호로, CPU 외부에서 비동기적으로 발생합니다. 시스템 콜은 사용자 프로그램이 파일 입출력이나 프로세스 생성처럼 커널만 수행할 수 있는 작업을 요청하기 위해 소프트웨어적으로 커널 모드로 전환을 요청하는 것으로, 프로그램 내부에서 의도적으로 호출합니다. 다만 시스템 콜도 내부적으로는 소프트웨어 인터럽트(트랩)를 발생시켜 커널 모드로 전환된다는 점에서, 시스템 콜은 인터럽트 메커니즘을 활용하는 상위 개념이라고 볼 수 있습니다.');
SET @e114 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('커널 모드와 유저 모드', '커널 모드와 유저 모드를 구분하는 이유와, 두 모드 사이를 전환해야 하는 대표적인 경우를 설명하시오.', 'ESSAY', 'LOW', 'OS',
 'CPU는 명령어 실행 권한을 커널 모드와 유저 모드로 나누어, 유저 모드에서 실행되는 일반 응용 프로그램이 메모리나 I/O 장치 같은 시스템 자원에 직접 접근하지 못하게 제한함으로써 하나의 프로세스 오류나 악의적 코드가 시스템 전체나 다른 프로세스에 영향을 주지 못하도록 보호합니다. 반면 커널 모드에서는 CPU와 메모리, 디바이스 등 모든 하드웨어 자원에 제한 없이 접근할 수 있습니다. 응용 프로그램이 파일 입출력이나 네트워크 통신처럼 커널의 도움이 필요한 시스템 콜을 호출하거나 인터럽트가 발생하면 유저 모드에서 커널 모드로 전환되고, 처리가 끝나면 다시 유저 모드로 복귀합니다.');
SET @e115 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('LRU와 FIFO 페이지 교체 알고리즘', '페이지 교체 알고리즘인 LRU(Least Recently Used)와 FIFO(First In First Out)의 동작 방식과 각각의 한계를 비교하여 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 'FIFO는 가장 먼저 메모리에 올라온 페이지를 교체 대상으로 선택하는 방식으로 구현이 단순하지만, 자주 사용되는 페이지라도 먼저 올라왔다는 이유만으로 교체될 수 있어 히트율이 낮아지고, 프레임 수를 늘렸는데도 페이지 폴트가 오히려 증가하는 벨레이디의 변칙(Belady''s Anomaly)이 발생할 수 있습니다. LRU는 가장 최근에 사용되지 않은 페이지를 교체 대상으로 선택해 지역성(locality)을 반영하므로 FIFO보다 일반적으로 히트율이 높지만, 모든 페이지의 최근 사용 시점을 추적해야 해서 구현 비용과 오버헤드가 더 큽니다. 따라서 실제 시스템에서는 LRU를 근사적으로 구현한 알고리즘(Clock 알고리즘 등)을 절충안으로 사용하는 경우가 많습니다.');
SET @e116 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스래싱의 발생 원인과 해결', '스래싱(Thrashing)이 발생하는 원인과 이를 완화하기 위한 방법을 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '스래싱은 시스템에 동시에 실행되는 프로세스가 너무 많아 각 프로세스에 할당된 물리 메모리 프레임이 부족해지면서, 필요한 페이지가 계속 메모리에 없어 페이지 폴트와 디스크 입출력이 과도하게 반복되고 정작 CPU가 실제 연산을 처리하는 시간은 급격히 줄어드는 현상입니다. CPU 이용률이 낮아지는 것을 보고 운영체제가 새로운 프로세스를 더 실행시키면 오히려 상황이 악화되는 악순환에 빠지기 쉽습니다. 이를 완화하려면 프로세스별로 필요한 최소 페이지 집합(Working Set)을 파악해 그만큼의 프레임을 보장하거나, 동시에 실행하는 프로세스 수를 제한하는 부하 제어(load control) 기법을 적용합니다.');
SET @e117 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('멀티프로세싱과 멀티스레딩', '멀티프로세싱과 멀티스레딩의 차이를 자원 공유와 안정성 관점에서 비교하여 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '멀티프로세싱은 여러 개의 독립된 프로세스를 병렬로 실행하는 방식으로, 프로세스마다 메모리 공간이 분리되어 있어 한 프로세스가 비정상 종료되어도 다른 프로세스에 영향을 주지 않아 안정성이 높지만, 프로세스 간 통신(IPC)에 별도의 메커니즘이 필요하고 생성·전환 비용이 큽니다. 멀티스레딩은 하나의 프로세스 안에서 여러 스레드가 메모리를 공유하며 동시에 실행되는 방식으로, 데이터 공유가 쉽고 생성·전환 비용이 적지만 한 스레드의 오류가 메모리를 공유하는 프로세스 전체를 함께 종료시킬 수 있고 동기화 문제에 더 취약합니다. 따라서 안정성과 격리가 중요하면 멀티프로세싱을, 자원 공유와 경량성이 중요하면 멀티스레딩을 선택하는 경향이 있습니다.');
SET @e118 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스핀락의 동작 원리', '스핀락(Spinlock)의 동작 원리와, 스핀락이 적합한 상황 및 부적합한 상황을 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '스핀락은 임계 구역에 진입하려는 스레드가 락을 얻지 못했을 때 스레드를 잠들게 하지 않고, 락이 풀릴 때까지 반복문을 돌며(busy-wait) 계속 락 상태를 확인하는 방식으로 동작합니다. 임계 구역을 점유하는 시간이 매우 짧고 락을 곧 풀 것으로 예상되는 멀티코어 환경에서는, 스레드를 잠들고 깨우는 컨텍스트 스위칭 비용보다 잠깐 대기하는 비용이 더 작아 스핀락이 효율적일 수 있습니다. 반면 임계 구역 점유 시간이 길거나 단일 코어 환경이라면 대기하는 동안 CPU를 계속 낭비하게 되므로, 이런 경우에는 대기 중인 스레드를 잠들게 하는 방식이 더 적합합니다.');
SET @e119 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('멀티코어 환경의 캐시 일관성 문제', '멀티코어 CPU 환경에서 캐시 일관성(Cache Coherence) 문제가 발생하는 이유와 이를 해결하는 접근 방식을 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '멀티코어 환경에서는 각 코어가 자신만의 로컬 캐시를 가지고 동일한 메모리 주소의 데이터를 각자 캐시에 복사해 두는데, 한 코어가 자신의 캐시에 있는 값을 변경해도 다른 코어의 캐시에는 이전 값이 그대로 남아 있어 코어마다 같은 메모리 주소에 대해 서로 다른 값을 보게 되는 캐시 일관성 문제가 발생합니다. 이를 해결하기 위해 하드웨어 수준에서 캐시 라인의 상태(Modified/Shared/Invalid 등)를 관리하며 한 코어가 값을 변경하면 다른 코어의 해당 캐시 라인을 무효화하거나 갱신하는 MESI와 같은 캐시 일관성 프로토콜을 사용합니다. 이 프로토콜은 코어 간 캐시 동기화를 자동으로 처리해주지만, 여러 코어가 같은 캐시 라인을 자주 갱신하면 무효화가 반복되어 성능이 저하되는 false sharing 현상이 발생할 수 있습니다.');
SET @e120 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('파일 시스템의 구조', '파일 시스템에서 inode와 디렉터리 엔트리가 각각 어떤 정보를 담당하며 파일을 어떻게 찾아내는지 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '디렉터리 엔트리는 파일 이름과 그 파일에 대응하는 inode 번호를 매핑해 저장하는 역할을 하며, 실제 파일의 메타데이터나 데이터 위치는 담고 있지 않습니다. inode는 파일의 소유자, 권한, 크기, 수정 시각과 함께 실제 데이터 블록들의 위치를 가리키는 포인터 정보를 담고 있어, 파일의 실질적인 속성과 저장 위치를 관리하는 단위입니다. 따라서 파일을 열 때는 경로를 따라 디렉터리 엔트리에서 파일명에 대응하는 inode 번호를 찾고, 그 inode에 기록된 데이터 블록 위치를 통해 실제 파일 내용에 접근하는 두 단계를 거칩니다.');
SET @e121 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('디스크 I/O 스케줄링', '디스크 입출력 스케줄링이 필요한 이유와, FCFS 방식과 SSTF(Shortest Seek Time First) 방식의 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '디스크는 헤드가 물리적으로 이동해 데이터를 읽고 써야 하므로 요청을 처리하는 순서에 따라 헤드의 이동 거리(seek time)가 크게 달라지고, 이 탐색 시간이 전체 디스크 성능에 큰 영향을 미치기 때문에 I/O 요청의 처리 순서를 정하는 스케줄링이 필요합니다. FCFS는 요청이 들어온 순서대로 처리해 구현이 단순하지만 헤드가 디스크 여기저기를 불필요하게 왔다 갔다 하며 탐색 시간이 길어질 수 있습니다. SSTF는 현재 헤드 위치에서 가장 가까운 요청을 먼저 처리해 평균 탐색 시간을 줄이지만, 헤드에서 먼 위치의 요청이 계속 밀려 처리되지 않는 기아 문제가 발생할 수 있습니다.');
SET @e122 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('버퍼링과 캐싱의 차이', '버퍼링(Buffering)과 캐싱(Caching)의 목적 차이를 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '버퍼링은 데이터를 생성하는 쪽과 사용하는 쪽의 속도 차이나 처리 단위 차이를 완충하기 위해 데이터를 임시로 모아두는 것으로, 예를 들어 느린 디스크 쓰기를 위해 데이터를 일정량 모았다가 한 번에 기록하는 데 사용됩니다. 캐싱은 이미 읽었거나 계산한 데이터를 더 빠른 저장 장소에 복사해 두어, 같은 데이터를 다시 요청할 때 원본 소스에 다시 접근하지 않고 빠르게 재사용하기 위한 것입니다. 즉 버퍼링은 속도 차이를 완충해 데이터 흐름을 원활히 하는 것이 목적이고, 캐싱은 데이터 재사용을 통해 접근 속도 자체를 높이는 것이 목적입니다.');
SET @e123 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('좀비 프로세스와 고아 프로세스', '좀비 프로세스와 고아 프로세스가 각각 어떤 상황에서 생기는지 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '좀비 프로세스는 자식 프로세스가 실행을 종료했지만 부모 프로세스가 그 종료 상태 값을 wait()으로 회수하지 않아, 프로세스 테이블에 종료된 프로세스의 정보(PCB)가 계속 남아 있는 상태를 말합니다. 고아 프로세스는 부모 프로세스가 자식보다 먼저 종료되어, 자식 프로세스가 부모 없이 남게 된 상태를 말하며 이 경우 대부분의 운영체제는 고아 프로세스를 init(또는 그 역할을 하는 프로세스)의 자식으로 재입양시켜 종료 시 정리가 되도록 처리합니다. 즉 좀비 프로세스는 종료된 자식의 정리가 안 된 문제이고, 고아 프로세스는 부모를 잃은 실행 중인 자식의 문제라는 점에서 발생 시점과 상태가 다릅니다.');
SET @e124 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스레드 풀을 사용하는 이유', '스레드 풀(Thread Pool)을 사용하는 이유와, 스레드 풀의 크기를 너무 작거나 크게 설정했을 때의 문제를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '스레드 풀은 작업이 들어올 때마다 스레드를 새로 생성하고 종료하는 대신, 미리 일정 수의 스레드를 생성해 두고 작업을 큐에 넣어 재사용함으로써 스레드 생성·소멸에 드는 비용을 줄이고 동시에 실행되는 스레드 수를 제어할 수 있게 합니다. 스레드 풀 크기를 너무 작게 설정하면 동시에 처리할 수 있는 작업 수가 부족해 작업이 큐에 쌓여 대기 시간이 길어지고, 너무 크게 설정하면 컨텍스트 스위칭이 과도하게 발생하고 메모리 사용량이 늘어 오히려 처리량이 떨어질 수 있습니다. 따라서 작업의 특성(CPU 바운드인지 I/O 바운드인지)과 하드웨어의 코어 수를 고려해 적절한 풀 크기를 설정해야 합니다.');
SET @e125 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('동기 I/O와 비동기 I/O', '동기(Synchronous) I/O와 비동기(Asynchronous) I/O의 차이를, 호출한 스레드가 결과를 어떻게 받는지를 중심으로 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '동기 I/O는 호출한 스레드가 I/O 작업이 완료되어 결과를 받을 때까지 다음 코드를 실행하지 않고 그 자리에서 기다리며, 작업의 흐름이 요청과 결과 수신이 순서대로 이어집니다. 비동기 I/O는 호출한 스레드가 I/O 작업을 요청한 뒤 결과를 기다리지 않고 곧바로 다음 작업을 수행하며, I/O가 실제로 완료되면 콜백이나 이벤트, 별도의 알림을 통해 결과를 통지받습니다. 따라서 동기 방식은 코드 흐름이 단순하지만 I/O 대기 시간 동안 스레드를 활용하지 못하고, 비동기 방식은 그 대기 시간에 다른 작업을 처리할 수 있어 처리량을 높일 수 있지만 코드의 흐름 제어가 더 복잡해집니다.');
SET @e126 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('블로킹 I/O와 논블로킹 I/O', '블로킹(Blocking) I/O와 논블로킹(Non-blocking) I/O의 차이를 설명하고, 이 구분이 동기·비동기 구분과 어떻게 다른지 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '블로킹 I/O는 I/O를 요청한 스레드가 커널이 작업을 완전히 처리해 결과를 돌려줄 때까지 제어권을 돌려받지 못하고 멈춰 있는 방식이고, 논블로킹 I/O는 커널이 즉시 처리할 수 없는 상태라도 곧바로 제어권을 돌려주어 호출한 스레드가 다른 작업을 계속할 수 있는 방식입니다. 블로킹·논블로킹은 호출이 즉시 반환되는지를 기준으로 구분되는 반면, 동기·비동기는 작업 완료 여부를 누가, 언제 확인하는지를 기준으로 구분되므로 서로 다른 축의 개념입니다. 예를 들어 논블로킹 호출이라도 완료 여부를 스레드가 반복적으로 직접 확인(polling)한다면 동기적 논블로킹이 되고, 완료 시 커널이 알려주는 방식이라면 비동기적 논블로킹이 되는 등 두 구분은 독립적으로 조합될 수 있습니다.');
SET @e127 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('내부 단편화와 외부 단편화', '메모리 할당 과정에서 발생하는 내부 단편화와 외부 단편화의 차이와 각각의 발생 원인을 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '내부 단편화는 메모리를 고정된 크기의 블록(페이지 등) 단위로 할당할 때, 실제로 필요한 크기보다 블록 크기가 더 커서 할당된 블록 내부에 사용되지 않는 공간이 남는 현상입니다. 외부 단편화는 가변 크기로 메모리를 할당·해제하는 과정에서 사용 중인 메모리 블록들 사이에 작은 빈 공간들이 여기저기 흩어져 남아, 전체 여유 공간의 합은 충분해도 하나의 요청을 만족할 만큼 연속된 공간이 없어 할당에 실패하는 현상입니다. 페이징처럼 고정 크기 블록을 쓰면 외부 단편화는 사라지지만 내부 단편화가 생기고, 반대로 가변 크기 분할 방식은 내부 단편화는 없지만 외부 단편화가 생기는 트레이드오프가 있습니다.');
SET @e128 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('스와핑과 페이징의 차이', '스와핑(Swapping)의 동작 방식과, 스와핑이 페이지 단위로 메모리를 관리하는 페이징과 어떻게 다른지 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '스와핑은 물리 메모리가 부족할 때 특정 프로세스 전체(코드·데이터·스택 등 모든 메모리 이미지)를 디스크의 스왑 영역으로 통째로 내리고, 필요할 때 다시 통째로 물리 메모리로 올리는 방식으로 프로세스 단위로 메모리를 관리합니다. 페이징은 프로세스 전체가 아니라 고정된 크기의 페이지 단위로 필요한 부분만 물리 메모리에 올리고 나머지는 디스크에 두는 방식으로, 프로세스를 통째로 옮기지 않고 더 세밀한 단위로 메모리를 관리합니다. 따라서 스와핑은 전체 프로세스를 옮기는 만큼 오버헤드가 크고 요즘은 페이징 기반 가상 메모리와 결합된 형태(필요한 페이지만 스왑)로 주로 사용되는 반면, 페이징은 항상 프로세스 단위가 아닌 세밀한 단위로 메모리를 다룬다는 점에서 차이가 있습니다.');
SET @e129 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로세스의 상태 전이', '프로세스가 생성부터 종료까지 거치는 상태(New, Ready, Running, Waiting, Terminated)와 각 상태 사이의 전이가 일어나는 조건을 설명하시오.', 'ESSAY', 'LOW', 'OS',
 '프로세스는 생성 요청이 접수되어 아직 실행 준비가 끝나지 않은 New 상태에서 시작해, 필요한 자원 준비가 끝나면 CPU 할당을 기다리는 Ready 상태로 전이됩니다. 스케줄러에게 CPU를 할당받으면 Running 상태가 되어 실제 명령어를 실행하고, 실행 중 I/O 요청 등으로 자원을 기다려야 하면 Waiting 상태로 전이되며 그 자원이 준비되면 다시 Ready 상태로 돌아갑니다. Running 상태에서 타임 슬라이스가 만료되면 Ready 상태로 돌아가고, 실행을 모두 마치거나 강제로 종료되면 Terminated 상태로 전이되어 프로세스가 종료됩니다.');
SET @e130 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('락프리 동시성 기법', '락프리(Lock-Free) 동시성 기법의 개념과, CAS(Compare-And-Swap) 연산이 락을 사용하지 않고 동시성 문제를 해결하는 방식을 설명하시오.', 'ESSAY', 'HIGH', 'OS',
 '락프리 기법은 임계 구역에 락을 걸어 다른 스레드의 접근을 막는 대신, 여러 스레드가 공유 데이터에 동시에 접근하더라도 하드웨어가 지원하는 원자적 연산을 이용해 데이터의 일관성을 보장하는 방식입니다. CAS는 메모리의 현재 값이 예상한 값과 같을 때만 새 값으로 교체하는 원자적 연산으로, 스레드는 값을 읽어 새 값을 계산한 뒤 CAS로 교체를 시도하고, 그 사이 다른 스레드가 값을 먼저 바꿔 CAS가 실패하면 최신 값을 다시 읽어 재시도합니다. 락프리 기법은 락으로 인한 블로킹과 데드락 위험을 없앨 수 있지만, 경쟁이 심한 환경에서는 CAS 재시도가 반복되어 오히려 성능이 떨어질 수 있습니다.');
SET @e131 = LAST_INSERT_ID();

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e13, '프로세스'), (@e13, '스레드'), (@e13, '메모리 구조'),
(@e14, '데드락'), (@e14, '동시성'),
(@e15, '가상 메모리'), (@e15, '페이징'),
(@e110, 'FCFS'), (@e110, 'SJF'), (@e110, '컨보이 효과'), (@e110, '기아'),
(@e111, 'Round Robin'), (@e111, '타임 슬라이스'), (@e111, '컨텍스트 스위칭 오버헤드'),
(@e112, '선점형 스케줄링'), (@e112, '비선점형 스케줄링'), (@e112, '타이머 인터럽트'),
(@e113, '컨텍스트 스위칭'), (@e113, 'PCB'), (@e113, 'TLB'),
(@e114, '인터럽트'), (@e114, '시스템 콜'), (@e114, '트랩'), (@e114, '커널 모드 전환'),
(@e115, '커널 모드'), (@e115, '유저 모드'), (@e115, '시스템 보호'),
(@e116, 'LRU'), (@e116, 'FIFO'), (@e116, '페이지 교체 알고리즘'), (@e116, '벨레이디의 변칙'),
(@e117, '스래싱'), (@e117, 'Working Set'), (@e117, '페이지 폴트'),
(@e118, '멀티프로세싱'), (@e118, '멀티스레딩'), (@e118, 'IPC'), (@e118, '안정성'),
(@e119, '스핀락'), (@e119, 'busy-wait'), (@e119, '임계 구역'), (@e119, '멀티코어'),
(@e120, '캐시 일관성'), (@e120, 'MESI'), (@e120, 'false sharing'),
(@e121, '파일 시스템'), (@e121, 'inode'), (@e121, '디렉터리 엔트리'),
(@e122, '디스크 스케줄링'), (@e122, 'FCFS'), (@e122, 'SSTF'), (@e122, '탐색 시간'),
(@e123, '버퍼링'), (@e123, '캐싱'), (@e123, '데이터 재사용'),
(@e124, '좀비 프로세스'), (@e124, '고아 프로세스'), (@e124, 'wait()'),
(@e125, '스레드 풀'), (@e125, '스레드 생성 비용'), (@e125, '작업 큐'),
(@e126, '동기 I/O'), (@e126, '비동기 I/O'), (@e126, '콜백'),
(@e127, '블로킹 I/O'), (@e127, '논블로킹 I/O'), (@e127, 'polling'),
(@e128, '내부 단편화'), (@e128, '외부 단편화'), (@e128, '메모리 할당'),
(@e129, '스와핑'), (@e129, '페이징'), (@e129, '스왑 영역'),
(@e130, '프로세스 상태'), (@e130, 'Ready'), (@e130, 'Running'), (@e130, 'Waiting'),
(@e131, '락프리'), (@e131, 'CAS'), (@e131, '원자적 연산'), (@e131, '데드락');

-- 카테고리: DESIGN_PATTERN (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('싱글톤 패턴과 스레드 안전성', '싱글톤 패턴의 목적과, 멀티스레드 환경에서 발생할 수 있는 문제 및 해결 방법을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '싱글톤 패턴은 클래스의 인스턴스가 애플리케이션 전체에서 오직 하나만 존재하도록 보장하고, 그 인스턴스에 접근할 수 있는 전역적인 지점을 제공하는 패턴입니다. 멀티스레드 환경에서 인스턴스 생성 로직에 동기화 처리를 하지 않으면 여러 스레드가 동시에 조건문을 통과해 인스턴스가 두 번 이상 생성되는 문제가 발생할 수 있습니다. 이를 해결하기 위해 synchronized 키워드로 생성 메서드를 동기화하거나, 클래스 로딩 시점에 미리 인스턴스를 생성하는 방식(정적 초기화), 또는 이중 검사 락킹(double-checked locking)을 사용합니다.');
SET @e16 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('전략 패턴과 개방-폐쇄 원칙', '전략 패턴이 개방-폐쇄 원칙(OCP)을 어떻게 지키는지 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '전략 패턴은 알고리즘(행위)을 인터페이스로 추상화해 각 구체적인 알고리즘을 별도의 클래스로 캡슐화하고, 컨텍스트 객체는 그 인터페이스에만 의존하도록 구성합니다. 이렇게 하면 새로운 알고리즘이 필요할 때 기존 컨텍스트나 다른 전략 클래스의 코드를 수정하지 않고 인터페이스를 구현하는 새 클래스만 추가하면 되므로, 확장에는 열려 있고 기존 코드의 수정에는 닫혀 있는 개방-폐쇄 원칙(OCP)을 지킬 수 있습니다. 대표적으로 결제 방식이나 정렬 기준처럼 런타임에 알고리즘을 교체해야 하는 상황에 적합합니다.');
SET @e17 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('옵저버 패턴과 발행-구독 패턴', '옵저버 패턴의 구조와, 발행-구독(Pub-Sub) 패턴과의 차이를 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '옵저버 패턴은 하나의 발행자(Subject)가 자신을 구독하는 여러 관찰자(Observer)를 직접 참조 목록으로 들고 있다가, 상태가 변하면 그 목록의 관찰자들에게 직접 알림(notify)을 호출하는 구조입니다. 발행-구독 패턴은 발행자와 구독자 사이에 브로커(메시지 큐 등)가 중간에 위치해, 발행자는 구독자를 전혀 알지 못한 채 브로커에 이벤트만 전달하고 브로커가 해당 이벤트를 구독한 대상들에게 전달합니다. 즉 옵저버 패턴은 발행자와 구독자가 서로를 직접 참조하는 강한 결합인 반면, 발행-구독 패턴은 중개자를 통해 완전히 분리된 약한 결합 구조입니다.');
SET @e18 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('팩토리 메서드 패턴의 목적', '팩토리 메서드 패턴이 객체 생성을 캡슐화하는 방식과, 이를 통해 얻는 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '팩토리 메서드 패턴은 객체 생성을 위한 인터페이스만 상위 클래스(또는 인터페이스)에 정의하고, 실제로 어떤 구체 클래스의 인스턴스를 생성할지는 하위 클래스가 오버라이드해서 결정하게 하는 패턴입니다. 클라이언트 코드는 구체적인 클래스 이름을 직접 알지 않고 팩토리 메서드를 통해 객체를 받으므로, 생성 로직과 사용 로직이 분리되어 새로운 제품 클래스가 추가되어도 클라이언트 코드를 수정할 필요가 없습니다. 이는 객체 생성 시점에 결정해야 하는 분기(if-else, switch)를 팩토리 메서드 내부로 숨겨 결합도를 낮추는 효과도 있습니다.');
SET @e132 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('추상 팩토리 패턴과 팩토리 메서드 패턴의 차이', '추상 팩토리 패턴이 팩토리 메서드 패턴과 어떻게 다른지, 서로 관련된 객체 군을 생성할 때 추상 팩토리가 갖는 이점과 함께 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '팩토리 메서드 패턴은 하나의 제품을 생성하는 메서드 하나를 하위 클래스가 오버라이드하는 데 초점을 맞추지만, 추상 팩토리 패턴은 서로 관련되거나 함께 사용되어야 하는 여러 제품군(예: 버튼, 체크박스 등 UI 컴포넌트 세트)을 만드는 여러 팩토리 메서드를 하나의 인터페이스로 묶어 제공합니다. 클라이언트는 구체 팩토리를 하나만 선택해 주입받으면 그 팩토리가 생성하는 제품들끼리는 항상 호환되는 세트임을 보장받을 수 있어, 서로 다른 제품군이 섞여 조합되는 실수를 방지합니다. 따라서 제품이 하나인지 여러 제품이 일관된 군을 이루는지가 두 패턴을 구분하는 핵심 기준입니다.');
SET @e133 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('빌더 패턴이 필요한 이유', '빌더 패턴이 생성자를 직접 사용하는 방식에 비해 어떤 문제를 해결하는지 설명하시오.', 'ESSAY', 'LOW', 'DESIGN_PATTERN',
 '생성자로 객체를 만들 때 필수·선택 필드가 많아지면 파라미터가 다른 생성자를 여러 개 오버로딩해야 하는 텔레스코핑 생성자 문제가 발생하고, 인자의 순서나 의미를 파악하기 어려워 가독성과 실수 위험이 커집니다. 빌더 패턴은 필드 값을 하나씩 메서드 체이닝으로 설정하고 마지막에 build()를 호출해 완성된 불변 객체를 생성하도록 해, 어떤 값이 어떤 필드에 대응하는지 명확히 드러내고 선택적 필드를 자유롭게 생략할 수 있게 합니다. 또한 build() 시점에 필수 값 검증을 한 번에 수행할 수 있어 일관성 없는 중간 상태의 객체가 만들어지는 것도 방지합니다.');
SET @e134 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프로토타입 패턴의 동작 원리', '프로토타입 패턴이 객체를 생성하는 방식과, 이 패턴이 유리한 상황을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '프로토타입 패턴은 새 객체를 new로 처음부터 생성하는 대신, 이미 만들어져 있는 원본 객체를 복제(clone)해서 필요한 부분만 수정해 사용하는 방식입니다. 객체 생성 비용이 크거나(예: DB 조회, 복잡한 초기화 로직이 필요한 경우) 초기 상태가 비슷한 객체를 반복적으로 만들어야 할 때, 매번 생성 로직을 다시 실행하는 대신 복제로 대체하면 성능을 아낄 수 있습니다. 다만 복제 시 객체 내부에 참조 타입 필드가 있으면 얕은 복사와 깊은 복사 중 무엇을 쓸지에 따라 원본과 복제본이 내부 객체를 공유할 위험이 있어 주의가 필요합니다.');
SET @e135 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('어댑터 패턴의 역할', '어댑터 패턴이 호환되지 않는 인터페이스 사이의 문제를 어떻게 해결하는지 설명하시오.', 'ESSAY', 'LOW', 'DESIGN_PATTERN',
 '어댑터 패턴은 클라이언트가 기대하는 인터페이스와 실제로 사용해야 하는 기존 클래스(또는 외부 라이브러리)의 인터페이스가 서로 다를 때, 그 사이에 어댑터 클래스를 두어 호출을 변환해 주는 패턴입니다. 어댑터는 클라이언트가 기대하는 인터페이스를 구현하면서, 내부적으로는 기존 클래스의 메서드를 호출해 결과를 원하는 형태로 변환해 반환합니다. 이를 통해 기존 클래스의 소스 코드를 수정하지 않고도 새로운 인터페이스 규격에 맞춰 재사용할 수 있어, 레거시 코드나 외부 라이브러리를 새 시스템에 통합할 때 특히 유용합니다.');
SET @e136 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('데코레이터 패턴과 상속의 차이', '데코레이터 패턴이 상속으로 기능을 확장하는 방식과 비교해 어떤 이점을 갖는지 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '상속으로 기능을 확장하면 조합 가능한 기능의 수가 늘어날 때마다 그 조합에 해당하는 하위 클래스를 각각 만들어야 해 클래스 수가 기하급수적으로 늘어나고, 확장 시점이 컴파일 타임으로 고정됩니다. 데코레이터 패턴은 원본 객체와 같은 인터페이스를 구현하는 데코레이터 클래스가 원본 객체를 필드로 감싸고 있다가, 기존 메서드 호출 앞뒤에 추가 동작을 끼워 넣는 방식으로 기능을 확장합니다. 이렇게 하면 런타임에 필요한 데코레이터를 겹겹이 조합해 씌울 수 있어, 상속 없이도 다양한 기능 조합을 유연하게 만들 수 있습니다.');
SET @e137 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('퍼사드 패턴의 역할', '퍼사드 패턴이 복잡한 서브시스템을 사용하는 클라이언트에게 어떤 이점을 주는지 설명하시오.', 'ESSAY', 'LOW', 'DESIGN_PATTERN',
 '퍼사드 패턴은 여러 클래스로 구성된 복잡한 서브시스템 앞에 단순화된 하나의 인터페이스(퍼사드)를 두어, 클라이언트가 서브시스템 내부의 세부 클래스들을 직접 알고 호출할 필요 없이 퍼사드의 몇 개 메서드만으로 원하는 기능을 사용할 수 있게 합니다. 이를 통해 클라이언트와 서브시스템 사이의 결합도가 낮아지고, 서브시스템 내부 구현이 바뀌어도 퍼사드의 인터페이스만 유지되면 클라이언트 코드는 영향을 받지 않습니다. 다만 퍼사드는 서브시스템의 기능을 완전히 대체하는 것이 아니라 자주 쓰이는 흐름을 편리하게 감싸는 것이므로, 세밀한 제어가 필요할 때는 서브시스템 클래스를 직접 사용할 수도 있게 열어두는 경우가 많습니다.');
SET @e138 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('컴포지트 패턴의 구조', '컴포지트 패턴이 개별 객체와 그 객체들의 집합을 동일하게 다룰 수 있게 하는 방법을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '컴포지트 패턴은 단일 객체(Leaf)와 여러 객체를 담는 복합 객체(Composite)가 동일한 인터페이스(Component)를 구현하도록 해, 클라이언트가 둘을 구분하지 않고 같은 방식으로 다룰 수 있게 합니다. Composite는 내부에 Component 타입의 자식들을 리스트로 가지고 있다가, 자신에게 호출된 연산을 자식들에게도 재귀적으로 위임해 전체 트리 구조에 대한 연산을 자연스럽게 전파합니다. 이 구조는 디렉터리와 파일처럼 부분-전체 계층을 표현해야 하는 경우에 적합하며, 트리의 깊이나 자식 수가 늘어나도 클라이언트 코드를 변경하지 않고 대응할 수 있습니다.');
SET @e139 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('프록시 패턴의 활용 목적', '프록시 패턴이 실제 객체에 대한 접근을 대리하는 방식과, 이를 활용하는 대표적인 목적들을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '프록시 패턴은 실제 객체(Real Subject)와 동일한 인터페이스를 구현하는 대리 객체(Proxy)를 두어, 클라이언트의 요청을 대리 객체가 먼저 받은 뒤 필요한 부가 처리를 하고 실제 객체에 위임하는 구조입니다. 대표적으로 실제 객체 생성 비용이 커서 실제로 필요한 시점까지 생성을 미루는 가상 프록시(lazy initialization), 접근 권한을 검사하는 보호 프록시, 원격 객체에 대한 호출을 대신 처리하는 원격 프록시 등으로 활용됩니다. 클라이언트는 프록시와 실제 객체를 동일한 인터페이스로 다루므로, 프록시가 추가되어도 클라이언트 코드를 수정할 필요가 없습니다.');
SET @e140 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('플라이웨이트 패턴과 메모리 절약', '플라이웨이트 패턴이 다수의 유사한 객체를 다룰 때 메모리 사용량을 줄이는 방법을 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '플라이웨이트 패턴은 객체가 가진 상태를 여러 인스턴스가 공유할 수 있는 내부 상태(intrinsic state)와 인스턴스마다 달라지는 외부 상태(extrinsic state)로 나누어, 내부 상태만 담은 객체를 팩토리를 통해 캐싱하고 여러 곳에서 공유해서 재사용합니다. 외부 상태는 객체 안에 저장하지 않고 메서드 호출 시점에 클라이언트가 파라미터로 전달해, 같은 내부 상태를 갖는 객체를 매번 새로 만들지 않고도 다양한 외부 상태에 대응할 수 있습니다. 이런 방식은 텍스트 에디터의 글자 객체나 게임의 파티클처럼 동일한 속성을 가진 객체가 대량으로 필요한 상황에서 메모리 사용량을 크게 줄일 수 있습니다.');
SET @e141 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('브릿지 패턴이 상속 폭발을 막는 방식', '브릿지 패턴이 상속의 계층 폭발 문제를 어떻게 해결하는지 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '기능의 추상화 축과 구현의 축이 모두 상속으로 표현되면, 두 축의 조합마다 클래스를 만들어야 해 두 축의 경우의 수가 곱해진 만큼 클래스 수가 폭발적으로 늘어납니다. 브릿지 패턴은 추상화(Abstraction) 계층과 구현(Implementor) 계층을 상속이 아니라 위임(composition) 관계로 연결해, 추상화 쪽 클래스가 구현 인터페이스를 필드로 참조하도록 분리합니다. 이렇게 하면 추상화와 구현을 각각 독립적으로 확장할 수 있어, 두 축이 조합될 때마다 새 클래스를 추가하지 않고 런타임에 구현 객체만 바꿔 끼워도 원하는 조합을 얻을 수 있습니다.');
SET @e142 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('커맨드 패턴의 요청 캡슐화', '커맨드 패턴이 요청 자체를 객체로 캡슐화함으로써 얻을 수 있는 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '커맨드 패턴은 실행할 동작(수신자와 그 메서드, 필요한 파라미터)을 execute() 메서드를 가진 하나의 커맨드 객체로 캡슐화해, 요청을 호출하는 쪽(Invoker)이 요청을 실행하는 구체적인 대상과 방법을 알지 못해도 되게 분리합니다. 요청이 객체이므로 큐에 쌓아 순서대로 실행하거나, 실행 취소(undo)를 위해 이전 커맨드들을 스택에 저장해두거나, 로그로 남겨 재실행할 수도 있습니다. 이런 특성 덕분에 리모컨의 버튼 동작이나 에디터의 실행 취소 기능처럼 요청을 나중에 실행하거나 취소해야 하는 상황에 적합합니다.');
SET @e143 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('템플릿 메서드 패턴의 구조', '템플릿 메서드 패턴이 알고리즘의 전체 흐름과 세부 단계를 어떻게 나누어 관리하는지 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '템플릿 메서드 패턴은 상위 클래스에 알고리즘의 전체 실행 순서를 정의하는 템플릿 메서드를 두고, 그 안에서 호출되는 세부 단계들은 하위 클래스가 오버라이드해서 구체적으로 구현하도록 추상 메서드나 훅 메서드로 남겨둡니다. 이렇게 하면 알고리즘의 골격(순서, 공통 로직)은 상위 클래스에서 한 곳에서 관리되어 중복이 줄고, 하위 클래스는 자신에게 다른 부분만 구현하면 되어 변경이 필요한 지점이 명확해집니다. 전략 패턴과 달리 알고리즘 전체를 통째로 교체하는 것이 아니라, 알고리즘의 뼈대는 고정한 채 일부 단계만 하위 클래스마다 다르게 만드는 것이 핵심 차이입니다.');
SET @e144 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('책임 연쇄 패턴의 동작 방식', '책임 연쇄 패턴이 여러 처리 객체 중 하나가 요청을 처리하게 만드는 방식과 그 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '책임 연쇄 패턴은 요청을 처리할 수 있는 여러 핸들러 객체를 체인 형태로 연결해두고, 요청이 들어오면 체인의 첫 핸들러부터 순서대로 자신이 처리할 수 있는지 확인한 뒤, 처리할 수 없으면 다음 핸들러로 요청을 넘기는 방식으로 동작합니다. 요청을 보내는 쪽은 체인 안의 어떤 핸들러가 실제로 처리하는지 알 필요가 없어 발신자와 수신자 사이의 결합도가 낮아지고, 체인에 핸들러를 추가·제거·순서 변경하는 것만으로 처리 흐름을 유연하게 바꿀 수 있습니다. 서블릿 필터나 예외 처리 미들웨어 체인처럼 요청이 여러 단계를 거쳐 검증·가공되는 상황에 적합합니다.');
SET @e145 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('상태 패턴과 조건 분기', '상태 패턴이 객체의 상태별 행위를 조건문 분기 대신 어떻게 표현하는지 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '상태 패턴을 쓰지 않으면 객체의 행위가 현재 상태에 따라 달라질 때 메서드 내부에 상태를 검사하는 if-else나 switch 분기가 많아지고, 상태가 추가될수록 그 분기를 계속 수정해야 합니다. 상태 패턴은 각 상태를 별도의 클래스로 만들어 상태별 행위를 그 클래스 안에 구현하고, 컨텍스트 객체는 현재 상태 객체에게 행위 실행을 위임하며 상태 전이가 필요하면 참조하는 상태 객체 자체를 교체합니다. 이렇게 하면 새로운 상태가 추가되어도 기존 상태 클래스나 컨텍스트의 분기 로직을 수정하지 않고 새 상태 클래스만 추가하면 되어 확장에 유리합니다.');
SET @e146 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('중재자 패턴의 역할', '중재자 패턴이 여러 객체 간의 복잡한 상호작용을 어떻게 단순화하는지 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '여러 객체가 서로를 직접 참조하며 상호작용하면 객체 수가 늘어날수록 참조 관계가 그물처럼 복잡해져 하나를 변경하면 다른 여러 객체에 영향이 퍼지기 쉽습니다. 중재자 패턴은 객체들이 서로를 직접 참조하지 않고, 중재자라는 별도의 객체를 통해서만 소통하도록 해 각 객체는 중재자 하나만 알면 되게 만듭니다. 각 객체(참여자)는 자신의 상태 변화를 중재자에게 알리고, 중재자가 그 변화에 따라 다른 참여자들에게 필요한 처리를 지시하므로, 참여자 사이의 결합은 줄고 상호작용 로직은 중재자 한 곳에 모여 관리하기 쉬워집니다.');
SET @e147 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('방문자 패턴의 구조', '방문자 패턴이 객체 구조를 변경하지 않고 새로운 연산을 추가할 수 있게 하는 원리를 설명하시오.', 'ESSAY', 'HIGH', 'DESIGN_PATTERN',
 '방문자 패턴은 각 원소(Element) 클래스에 accept(visitor) 메서드만 두고, 실제 처리 로직은 별도의 방문자(Visitor) 클래스에 원소 타입별 visit 메서드로 구현해, 원소 클래스 자체에는 다양한 연산 코드를 계속 추가하지 않아도 되게 분리합니다. 원소는 자신을 방문한 방문자의 해당 visit 메서드를 호출해주는 더블 디스패치 구조로 동작하며, 새로운 연산이 필요하면 새로운 Visitor 클래스만 추가하면 되어 기존 원소 클래스들의 코드는 그대로 유지됩니다. 다만 반대로 원소의 종류(클래스) 자체가 자주 늘어나는 구조라면, 모든 Visitor에 새 visit 메서드를 추가해야 해 오히려 변경 범위가 넓어지는 단점이 있습니다.');
SET @e148 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('MVC 패턴의 계층 분리', 'MVC 패턴이 Model, View, Controller를 나누는 이유와 각 계층의 책임을 설명하시오.', 'ESSAY', 'LOW', 'DESIGN_PATTERN',
 'MVC 패턴은 데이터와 비즈니스 로직을 다루는 Model, 화면 출력을 담당하는 View, 사용자 입력을 받아 Model을 조작하고 그 결과를 View에 반영하도록 흐름을 제어하는 Controller로 책임을 나눕니다. 이렇게 관심사를 분리하면 화면 디자인이 바뀌어도 Model이나 비즈니스 로직을 건드릴 필요가 없고, 반대로 로직이 바뀌어도 View 코드에 영향이 적어 각 계층을 독립적으로 수정·테스트할 수 있습니다. Model은 View나 Controller를 알지 못하도록 설계해 재사용성과 테스트 용이성을 높이는 것이 일반적인 원칙입니다.');
SET @e149 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('의존성 주입과 제어의 역전', '의존성 주입(DI)이 제어의 역전(IoC)을 실현하는 방식과, 이를 통해 얻는 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '제어의 역전(IoC)은 객체가 자신이 사용할 의존 객체를 직접 생성하고 제어하던 것을 외부(프레임워크나 컨테이너)로 넘기는 원칙이고, 의존성 주입(DI)은 이 IoC를 구현하는 대표적인 방법으로 객체가 필요한 의존 객체를 직접 new로 생성하지 않고 생성자나 세터, 필드를 통해 외부에서 주입받는 방식입니다. 이렇게 하면 클래스는 구체적인 구현체가 아니라 인터페이스에만 의존하게 되어, 실제 구현체를 교체하거나 테스트용 mock 객체로 바꿔 끼우기가 쉬워집니다. 스프링 컨테이너가 빈(bean)의 생성과 의존관계 설정을 대신 처리해주는 것이 이런 DI/IoC를 실제로 적용한 대표적인 예입니다.');
SET @e150 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('단일 책임 원칙(SRP)', '단일 책임 원칙(SRP)이 클래스 설계에서 요구하는 것과, 이를 지키지 않았을 때 생기는 문제를 설명하시오.', 'ESSAY', 'LOW', 'DESIGN_PATTERN',
 '단일 책임 원칙은 하나의 클래스가 변경되어야 하는 이유(책임)를 오직 하나만 가져야 한다는 원칙으로, 서로 다른 이유로 변경되는 기능들을 한 클래스에 몰아넣지 말라는 뜻입니다. 이를 지키지 않고 여러 책임을 한 클래스에 섞으면, 한 책임과 관련된 요구사항이 바뀌었을 뿐인데 그 클래스를 수정해야 하고 이는 관련 없는 다른 책임의 코드에도 영향을 줄 위험을 만들어 회귀 버그가 발생하기 쉬워집니다. 책임을 명확히 나누어 각 클래스가 하나의 역할만 담당하게 하면, 변경 범위가 예측 가능해지고 테스트와 재사용도 쉬워집니다.');
SET @e151 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인터페이스 분리 원칙(ISP)', '인터페이스 분리 원칙(ISP)이 인터페이스 설계에서 요구하는 것과, 이를 어겼을 때 발생하는 문제를 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '인터페이스 분리 원칙은 클라이언트가 자신이 사용하지 않는 메서드에는 의존하지 않아야 한다는 원칙으로, 하나의 거대한 인터페이스보다 클라이언트별로 필요한 기능만 담은 여러 개의 작은 인터페이스로 나누는 것을 권장합니다. 이를 어기고 다양한 기능을 한 인터페이스에 모두 넣으면, 그 인터페이스를 구현하는 클래스는 자신이 실제로 쓰지 않는 메서드까지 구현해야 하고, 인터페이스의 일부만 바뀌어도 그 인터페이스에 의존하는 모든 클라이언트가 영향을 받을 수 있습니다. 인터페이스를 역할 단위로 잘게 분리하면 클라이언트는 필요한 인터페이스만 구현·의존하게 되어 불필요한 결합이 줄어듭니다.');
SET @e152 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('의존관계 역전 원칙(DIP)', '의존관계 역전 원칙(DIP)이 상위 모듈과 하위 모듈의 의존 방향에 대해 요구하는 것을 설명하시오.', 'ESSAY', 'MEDIUM', 'DESIGN_PATTERN',
 '의존관계 역전 원칙은 상위 수준의 모듈이 하위 수준의 구체적인 구현 클래스에 직접 의존하지 않고, 둘 다 추상화(인터페이스)에 의존해야 한다는 원칙입니다. 상위 모듈이 하위 모듈의 구체 클래스를 직접 참조하면 하위 모듈의 구현이 바뀔 때마다 상위 모듈도 영향을 받게 되지만, 인터페이스를 사이에 두면 하위 모듈의 구현이 바뀌어도 인터페이스만 유지되면 상위 모듈은 수정할 필요가 없습니다. 이 원칙은 전략 패턴이나 의존성 주입(DI)과 함께 적용되는 경우가 많으며, 결과적으로 고수준 정책이 저수준의 세부 구현 변경에 흔들리지 않도록 만듭니다.');
SET @e153 = LAST_INSERT_ID();

-- 서술형 태그

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e16, '싱글톤 패턴'), (@e16, '스레드 안전성'),
(@e17, '전략 패턴'), (@e17, '개방-폐쇄 원칙'),
(@e18, '옵저버 패턴'), (@e18, '발행-구독'),
(@e132, '팩토리 메서드 패턴'), (@e132, '객체 생성'), (@e132, '캡슐화'),
(@e133, '추상 팩토리 패턴'), (@e133, '팩토리 메서드 패턴'), (@e133, '제품군'),
(@e134, '빌더 패턴'), (@e134, '생성자'), (@e134, '불변 객체'),
(@e135, '프로토타입 패턴'), (@e135, '객체 복제'), (@e135, '얕은 복사'), (@e135, '깊은 복사'),
(@e136, '어댑터 패턴'), (@e136, '인터페이스 호환'), (@e136, '레거시 통합'),
(@e137, '데코레이터 패턴'), (@e137, '상속'), (@e137, '기능 확장'),
(@e138, '퍼사드 패턴'), (@e138, '서브시스템'), (@e138, '결합도'),
(@e139, '컴포지트 패턴'), (@e139, '트리 구조'), (@e139, '부분-전체 계층'),
(@e140, '프록시 패턴'), (@e140, '지연 초기화'), (@e140, '접근 제어'),
(@e141, '플라이웨이트 패턴'), (@e141, '내부 상태'), (@e141, '외부 상태'), (@e141, '메모리 최적화'),
(@e142, '브릿지 패턴'), (@e142, '추상화와 구현 분리'), (@e142, '상속'),
(@e143, '커맨드 패턴'), (@e143, '요청 캡슐화'), (@e143, '실행 취소'),
(@e144, '템플릿 메서드 패턴'), (@e144, '알고리즘 골격'), (@e144, '상속'),
(@e145, '책임 연쇄 패턴'), (@e145, '핸들러 체인'), (@e145, '결합도'),
(@e146, '상태 패턴'), (@e146, '상태 전이'), (@e146, '조건 분기'),
(@e147, '중재자 패턴'), (@e147, '결합도'), (@e147, '상호작용 캡슐화'),
(@e148, '방문자 패턴'), (@e148, '더블 디스패치'), (@e148, '구조와 연산 분리'),
(@e149, 'MVC 패턴'), (@e149, '관심사 분리'), (@e149, '계층 구조'),
(@e150, '의존성 주입'), (@e150, '제어의 역전'), (@e150, '스프링 컨테이너'),
(@e151, '단일 책임 원칙'), (@e151, 'SOLID'), (@e151, '응집도'),
(@e152, '인터페이스 분리 원칙'), (@e152, 'SOLID'), (@e152, '인터페이스 설계'),
(@e153, '의존관계 역전 원칙'), (@e153, 'SOLID'), (@e153, '추상화');

-- 카테고리: LANGUAGE (25문항)

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('JVM 메모리 구조와 가비지 컬렉션', 'JVM의 메모리 구조와 가비지 컬렉션의 동작 원리를 설명하시오.', 'ESSAY', 'HIGH', 'LANGUAGE',
 'JVM 메모리는 크게 모든 스레드가 공유하는 힙(객체 인스턴스가 저장되는 영역)과 메서드 영역(클래스 메타데이터, 상수), 그리고 스레드마다 독립적으로 갖는 스택(메서드 호출 시 지역 변수·연산 정보를 담는 프레임)으로 구성됩니다. 가비지 컬렉션은 힙에 있는 객체 중 더 이상 어떤 참조도 도달하지 못하는(Unreachable) 객체를 식별해 자동으로 메모리를 회수하는 과정으로, 대부분 객체가 금방 사라진다는 가정 아래 Young 영역과 오래 살아남은 객체를 모으는 Old 영역을 나누어 관리하는 세대별 수집 방식을 사용합니다. 이 덕분에 개발자가 직접 메모리를 해제하지 않아도 되지만, GC가 실행되는 동안 애플리케이션이 일시 정지(Stop-The-World)되는 비용이 발생할 수 있습니다.');
SET @e19 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('equals와 hashCode의 관계', 'equals()와 hashCode()를 함께 재정의해야 하는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 'equals()는 두 객체가 논리적으로 같은지를 판단하는 기준이고, hashCode()는 HashMap·HashSet 같은 해시 기반 컬렉션에서 객체를 저장할 버킷을 결정하는 값입니다. equals()로 같다고 판단되는 두 객체는 반드시 같은 hashCode()를 반환해야 한다는 규약이 있는데, equals()만 재정의하고 hashCode()를 재정의하지 않으면 논리적으로 같은 두 객체가 서로 다른 버킷에 저장되어 HashSet에 중복으로 들어가거나 HashMap에서 값을 찾지 못하는 문제가 생깁니다. 따라서 equals()를 재정의할 때는 항상 hashCode()도 그 기준에 맞게 함께 재정의해야 합니다.');
SET @e20 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('체크 예외와 언체크 예외', '체크 예외와 언체크 예외의 차이와 각각 언제 사용하는지 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 '체크 예외(Checked Exception)는 컴파일러가 처리를 강제하는 예외로, 메서드 시그니처에 throws로 선언하거나 try-catch로 반드시 처리해야 하며 파일 입출력처럼 호출자가 미리 대응할 수 있고 대응해야 하는 상황에 사용합니다. 언체크 예외(Unchecked Exception, RuntimeException 계열)는 컴파일러가 처리를 강제하지 않는 예외로, 잘못된 인자나 널 참조처럼 프로그램의 버그로 인해 발생하는 예외에 주로 사용합니다. 즉 호출자가 복구할 수 있는 예상 가능한 예외는 체크 예외로, 코드 결함으로 인한 예외는 언체크 예외로 설계하는 것이 일반적인 기준입니다.');
SET @e21 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('제네릭과 타입 소거', '제네릭(Generic)이 무엇이고, 자바의 타입 소거(Type Erasure)가 런타임에 어떤 제약을 만드는지 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '제네릭은 컴파일 시점에 타입을 매개변수화하여 컬렉션 등에 잘못된 타입의 객체가 담기는 것을 막고, instanceof나 캐스팅 없이도 타입 안전성을 확보하게 해주는 기능입니다. 자바의 제네릭은 하위 호환성을 위해 컴파일 시점에만 타입 정보를 검사하고 바이트코드에는 타입 정보를 지우는 타입 소거 방식을 사용하므로, 런타임에는 List<String>과 List<Integer>가 같은 List로 취급되어 제네릭 타입으로 배열을 생성하거나 instanceof로 제네릭 타입을 구분할 수 없습니다. 대신 컴파일러가 필요한 곳에 형변환 코드를 자동으로 삽입해 타입 안전성을 컴파일 단계에서만 보장합니다.');
SET @e154 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('어노테이션과 리플렉션의 관계', '자바의 어노테이션(Annotation)이 리플렉션(Reflection)과 함께 동작하는 방식을 예시와 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '어노테이션은 클래스, 메서드, 필드 등에 부착하는 메타데이터로, 그 자체로는 아무 동작도 수행하지 않고 단지 코드에 대한 부가 정보를 표시하는 역할만 합니다. 이 정보가 실제 동작으로 이어지려면 런타임에 리플렉션 API를 이용해 클래스의 어노테이션을 조회하고, 그 정보를 바탕으로 특정 로직(예: 필드 주입, 메서드 실행)을 수행하는 별도의 처리기가 필요합니다. 예를 들어 스프링의 @Autowired는 어노테이션 자체가 아니라 스프링 컨테이너가 리플렉션으로 해당 어노테이션이 붙은 필드를 찾아 의존성을 주입해주기 때문에 동작합니다.');
SET @e155 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인터페이스와 추상 클래스의 차이', '인터페이스와 추상 클래스의 차이를 설계 의도 관점에서 비교하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 '추상 클래스는 공통된 필드와 구현을 일부 가진 채 하위 클래스들이 상속해 코드를 재사용하도록 하는 것이 목적이라 단일 상속만 가능하고, 하나의 클래스만 상속할 수 있습니다. 인터페이스는 구현 클래스가 반드시 따라야 하는 행위(메서드 규약)를 정의하는 것이 목적이라 필드를 가질 수 없고(상수 제외) 여러 인터페이스를 동시에 구현할 수 있어 다중 상속과 유사한 효과를 낼 수 있습니다. 따라서 is-a 관계이면서 공통 상태·구현을 공유해야 하면 추상 클래스를, 서로 관련 없는 클래스들에 동일한 행위 규약만 강제하고 싶으면 인터페이스를 선택합니다.');
SET @e156 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('함수형 인터페이스와 람다식', '함수형 인터페이스가 무엇이며 람다식이 이를 어떻게 구현하는지 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 '함수형 인터페이스는 추상 메서드를 정확히 하나만 가진 인터페이스로, @FunctionalInterface 어노테이션으로 명시적으로 표시할 수 있으며 Runnable, Comparator처럼 하나의 동작만 정의하는 인터페이스가 여기에 해당합니다. 람다식은 이 함수형 인터페이스의 유일한 추상 메서드에 대한 구현을 익명 클래스보다 간결한 문법으로 작성한 것으로, 컴파일러가 람다식이 대입되는 대상의 타입을 함수형 인터페이스로 추론해 그 메서드의 본문으로 치환합니다. 이 덕분에 익명 클래스를 매번 정의하지 않고도 동작 자체를 값처럼 변수에 담거나 메서드 인자로 전달할 수 있습니다.');
SET @e157 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Stream API의 지연 연산', 'Stream API의 중간 연산과 최종 연산의 차이, 그리고 지연 연산(Lazy Evaluation)이 갖는 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '중간 연산(filter, map 등)은 새로운 스트림을 반환할 뿐 즉시 실행되지 않고, 최종 연산(collect, forEach, count 등)이 호출되는 시점에야 지금까지 쌓인 중간 연산들이 한꺼번에 실행되는 지연 연산 방식을 따릅니다. 이 덕분에 스트림은 각 원소마다 모든 중간 연산을 순서대로 적용한 뒤 다음 원소로 넘어가므로, 불필요한 중간 컬렉션을 매 단계마다 생성하지 않고 필요한 경우 조건을 만족하는 순간 나머지 연산을 건너뛸 수도 있습니다(예: findFirst). 반면 최종 연산이 한 번도 호출되지 않으면 중간 연산은 전혀 실행되지 않으므로, 스트림은 재사용할 수 없는 일회성 파이프라인으로 설계되어 있습니다.');
SET @e158 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('String, StringBuilder, StringBuffer의 차이', 'String, StringBuilder, StringBuffer의 차이를 불변성과 동기화 관점에서 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'String은 불변(immutable) 객체라 문자열을 연결하거나 수정할 때마다 새로운 String 객체가 생성되므로, 반복적인 문자열 조작에는 많은 임시 객체가 생겨 성능이 저하됩니다. StringBuilder는 내부 버퍼를 가변으로 관리해 문자열을 이어붙이거나 수정해도 새 객체를 만들지 않아 반복 조작에 효율적이지만 동기화를 지원하지 않아 스레드 안전하지 않습니다. StringBuffer는 StringBuilder와 API가 거의 동일하지만 내부 메서드에 synchronized가 걸려 있어 스레드 안전하나 그만큼 동기화 오버헤드가 있어, 단일 스레드 환경에서는 StringBuilder가 더 널리 쓰입니다.');
SET @e159 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('불변 객체(Immutable Object)의 장점', '불변 객체가 무엇이며 이를 사용할 때의 장점을 멀티스레드 환경과 함께 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '불변 객체는 생성된 이후 내부 상태가 절대 변하지 않는 객체로, 자바에서는 필드를 final로 선언하고 setter를 제공하지 않으며 가변 객체를 필드로 가질 경우 방어적 복사를 통해 만듭니다. 상태가 변하지 않으므로 여러 스레드가 동시에 같은 불변 객체를 참조해도 값이 변경될 위험이 없어 별도의 동기화 없이 안전하게 공유할 수 있고, 이는 락으로 인한 성능 저하나 동시성 버그를 근본적으로 없애줍니다. 또한 객체의 상태가 항상 유효한 상태로 고정되어 있어 디버깅과 추론이 쉬워지고, 해시값이 변하지 않아 HashMap의 키로도 안전하게 사용할 수 있습니다.');
SET @e160 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('List, Set, Map의 특징 비교', '컬렉션 프레임워크에서 List, Set, Map의 구조적 특징과 각각을 선택하는 기준을 비교하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'List는 순서가 있는 데이터의 나열로 인덱스를 통한 접근과 중복 원소를 허용하며 순서가 중요한 데이터(예: 히스토리)에 사용합니다. Set은 중복을 허용하지 않고 순서를 보장하지 않는 경우가 일반적(HashSet)이며 어떤 원소가 이미 존재하는지 빠르게 확인해야 하는 상황에 적합하고, 필요에 따라 LinkedHashSet이나 TreeSet으로 순서·정렬을 추가할 수 있습니다. Map은 키-값 쌍을 저장해 키의 중복은 허용하지 않지만 값은 중복될 수 있으며, 특정 키로 값을 빠르게 조회해야 하는 상황(예: ID로 객체 찾기)에 사용합니다.');
SET @e161 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('오토박싱과 언박싱', '오토박싱과 언박싱이 무엇이며, 이로 인해 발생할 수 있는 성능·오류 이슈를 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '오토박싱은 int, double 같은 기본형 값을 Integer, Double 같은 래퍼 클래스 객체로 컴파일러가 자동으로 변환해주는 것이고, 언박싱은 반대로 래퍼 객체를 다시 기본형 값으로 자동 변환해주는 것입니다. 이 변환이 반복문 등에서 빈번하게 일어나면 매번 객체를 생성하고 꺼내는 오버헤드가 쌓여 성능이 저하될 수 있고, 값이 null인 래퍼 객체를 언박싱하려 하면 NullPointerException이 발생할 수 있습니다. 또한 Integer는 -128~127 범위를 캐싱해 재사용하기 때문에 이 범위 안의 값은 ==으로 비교해도 같아 보이지만 범위를 벗어나면 서로 다른 객체가 되어 ==비교가 실패하는 함정도 있어, 래퍼 객체 비교에는 equals()를 사용해야 합니다.');
SET @e162 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('static 키워드의 의미', 'static 키워드가 필드, 메서드에 붙었을 때 각각 어떤 의미를 갖는지 인스턴스 멤버와 비교하여 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'static이 붙은 필드는 인스턴스마다 별도로 존재하지 않고 클래스 전체가 공유하는 단 하나의 값으로, 클래스가 로딩되는 시점에 메모리에 할당되어 모든 인스턴스가 같은 값을 참조합니다. static 메서드는 특정 인스턴스의 상태에 의존하지 않는 동작을 담기 위한 것으로, 인스턴스를 생성하지 않고도 클래스 이름으로 바로 호출할 수 있지만 그 안에서는 인스턴스 필드나 인스턴스 메서드에 직접 접근할 수 없습니다. 반면 인스턴스 필드·메서드는 객체가 생성될 때마다 독립적으로 존재하며 각 인스턴스의 고유한 상태를 다룰 때 사용합니다.');
SET @e163 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('final 키워드의 활용', 'final 키워드가 클래스, 메서드, 변수에 각각 적용될 때의 의미와 목적을 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'final 클래스는 더 이상 상속할 수 없는 클래스로, 클래스의 동작이 하위 클래스에서 변경되는 것을 막아 불변성이나 보안이 중요한 클래스(예: String)를 안전하게 설계할 때 사용합니다. final 메서드는 하위 클래스에서 오버라이드할 수 없는 메서드로, 상위 클래스의 핵심 동작이 하위 클래스에서 임의로 바뀌지 않도록 보장하고 싶을 때 사용합니다. final 변수는 한 번 값이 대입되면 다시 재대입할 수 없는 변수로, 상수를 정의하거나 값이 바뀌지 않아야 하는 필드를 명시적으로 표현할 때 사용하며 컴파일러가 이를 강제해 실수로 값이 바뀌는 것을 방지합니다.');
SET @e164 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('접근 제어자의 범위', 'public, protected, default(package-private), private 접근 제어자가 허용하는 접근 범위를 비교하여 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'private은 선언된 클래스 내부에서만 접근 가능해 외부에 절대 노출하지 않아야 하는 구현 세부사항에 사용하고, default(접근 제어자를 명시하지 않은 경우)는 같은 패키지 내에서만 접근을 허용합니다. protected는 같은 패키지에서의 접근에 더해 다른 패키지에 있더라도 해당 클래스를 상속한 하위 클래스에서는 접근을 허용해, 상속 관계에서 재사용하되 외부에는 감추고 싶은 멤버에 사용합니다. public은 어떤 패키지에서든 제한 없이 접근할 수 있어 외부에 공개할 API에 사용하며, 접근 범위가 넓어질수록 캡슐화 수준은 낮아지므로 실제 필요한 범위로 최소화하는 것이 좋은 설계로 여겨집니다.');
SET @e165 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('try-finally와 return의 상호작용', 'try 블록과 finally 블록 모두에서 return 문을 사용했을 때 어떤 값이 최종적으로 반환되는지, 그리고 finally 블록이 항상 실행되는 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 'finally 블록은 try 블록에서 예외가 발생하든 정상적으로 종료되든, 심지어 try나 catch 안에서 return이 실행되더라도 메서드가 실제로 값을 반환하기 전에 반드시 실행되도록 JVM이 보장하는 영역으로, 자원 해제 같은 마무리 작업을 위해 존재합니다. 만약 try 블록에서 return으로 반환할 값이 이미 결정된 상태에서 finally 블록이 다시 return 문을 실행하면, finally의 반환값이 try의 반환값을 덮어써 최종적으로 finally의 값이 반환됩니다. 이런 동작은 코드를 읽기 어렵게 만들기 때문에 실무에서는 finally 블록 안에서 return을 사용하는 것을 지양하는 경우가 많습니다.');
SET @e166 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Thread 상속과 Runnable 구현', '자바에서 스레드를 생성하는 두 가지 방법인 Thread 클래스 상속과 Runnable 인터페이스 구현의 차이와 각각의 장단점을 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 'Thread 클래스를 상속하는 방식은 run() 메서드를 오버라이드해 스레드의 동작을 정의하지만, 자바는 단일 상속만 지원하므로 이미 다른 클래스를 상속하고 있다면 이 방식을 사용할 수 없습니다. Runnable 인터페이스를 구현하는 방식은 run() 메서드만 구현한 객체를 만들어 Thread의 생성자에 넘겨 실행하는 방식으로, 다른 클래스를 상속하면서도 스레드 동작을 정의할 수 있고 실행할 작업(Runnable)과 실행 주체(Thread)를 분리해 관심사를 나눌 수 있습니다. 따라서 대부분의 경우 상속의 제약이 없고 재사용성이 높은 Runnable 구현 방식이 더 권장됩니다.');
SET @e167 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('synchronized와 Lock(ReentrantLock)의 차이', 'synchronized 키워드와 java.util.concurrent의 Lock(ReentrantLock)의 차이를 사용상의 유연성 관점에서 설명하시오.', 'ESSAY', 'HIGH', 'LANGUAGE',
 'synchronized는 메서드나 블록에 선언하면 JVM이 자동으로 락을 획득·해제해주는 방식으로 사용이 간단하지만, 락을 얻으려는 스레드는 획득될 때까지 무조건 기다려야 하고 이미 진입한 블록의 범위 안에서만 락이 유지되어 유연성이 떨어집니다. ReentrantLock은 명시적으로 lock()과 unlock()을 호출해야 하는 대신, 락 획득을 특정 시간만 시도하는 tryLock, 대기 중 인터럽트 가능한 lockInterruptibly, 대기 중인 스레드 중 가장 오래 기다린 스레드에게 우선권을 주는 공정성(fairness) 옵션 등을 제공해 더 세밀한 제어가 가능합니다. 다만 ReentrantLock은 unlock()을 명시적으로 호출해야 하므로 보통 try-finally와 함께 사용해 락이 반드시 해제되도록 해야 합니다.');
SET @e168 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('volatile 키워드의 역할', 'volatile 키워드가 보장하는 것과, 이것만으로는 스레드 안전성을 완전히 보장할 수 없는 이유를 설명하시오.', 'ESSAY', 'HIGH', 'LANGUAGE',
 'volatile로 선언된 변수는 각 스레드의 CPU 캐시가 아니라 항상 메인 메모리에서 직접 읽고 쓰도록 보장되어, 한 스레드가 값을 변경하면 다른 스레드가 그 변경을 즉시 볼 수 있는 가시성(visibility)을 보장합니다. 하지만 volatile은 read-modify-write 같은 여러 연산을 하나의 원자적 단위로 묶어주지는 않기 때문에, 예를 들어 volatile 변수에 대해 값을 읽고 1을 더해 다시 쓰는 연산(count++)을 여러 스레드가 동시에 수행하면 중간에 다른 스레드가 값을 바꿔 갱신이 누락되는 문제가 여전히 발생할 수 있습니다. 따라서 여러 연산의 원자성까지 보장하려면 synchronized나 AtomicInteger 같은 별도의 동기화 수단이 필요합니다.');
SET @e169 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('클래스 로더의 위임 모델', '자바 클래스 로더가 클래스를 로딩하는 위임 모델(Delegation Model)의 동작 방식과 그 이점을 설명하시오.', 'ESSAY', 'HIGH', 'LANGUAGE',
 '자바의 클래스 로더는 부트스트랩, 플랫폼(확장), 애플리케이션 클래스 로더가 계층 구조를 이루고 있으며, 클래스를 로딩할 때 자기 자신이 먼저 로드를 시도하지 않고 먼저 부모 클래스 로더에게 로딩을 위임하는 위임 모델을 따릅니다. 부모가 해당 클래스를 찾지 못했을 때에만 자신이 직접 클래스패스를 뒤져 클래스를 로드하며, 이 과정을 최상위 부트스트랩 로더까지 재귀적으로 반복합니다. 이 방식 덕분에 java.lang.String 같은 핵심 클래스를 애플리케이션 코드가 임의로 재정의해 로드하는 것을 막을 수 있어 코어 API의 무결성과 보안을 지킬 수 있습니다.');
SET @e170 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('직렬화와 SerialVersionUID', '자바 직렬화(Serializable)의 동작 방식과 SerialVersionUID가 필요한 이유를 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '직렬화는 객체의 상태(필드 값)를 바이트 스트림으로 변환해 파일에 저장하거나 네트워크로 전송할 수 있게 하고, 역직렬화는 그 바이트 스트림을 다시 객체로 복원하는 과정으로, 자바에서는 Serializable 인터페이스를 구현하기만 하면 별도의 메서드 구현 없이 이 기능을 사용할 수 있습니다. SerialVersionUID는 직렬화된 객체가 어떤 클래스 버전과 호환되는지 식별하는 값으로, 명시적으로 선언하지 않으면 클래스의 필드 등을 기반으로 JVM이 자동 생성하기 때문에 클래스가 조금만 변경되어도 값이 달라질 수 있습니다. 만약 직렬화 시점의 클래스와 역직렬화 시점의 클래스의 SerialVersionUID가 다르면 InvalidClassException이 발생하므로, 버전 관리를 위해 SerialVersionUID를 명시적으로 고정해 선언하는 것이 권장됩니다.');
SET @e171 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('Optional 클래스의 목적', 'Optional 클래스가 도입된 목적과, null을 직접 반환하는 것과 비교했을 때의 이점을 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 'Optional은 메서드의 반환값이 존재할 수도 있고 없을 수도 있다는 사실을 타입 시스템 차원에서 명시적으로 드러내기 위해 도입된 컨테이너 객체로, 호출하는 쪽이 반환값이 없을 가능성을 인지하고 처리하도록 유도합니다. null을 직접 반환하면 호출자가 그 사실을 문서나 관례로만 알 수 있어 null 체크를 빠뜨리면 NullPointerException으로 이어지기 쉽지만, Optional을 사용하면 isPresent(), orElse(), map() 같은 메서드로 값이 없는 경우를 명시적이고 안전하게 처리할 수 있습니다. 다만 Optional은 메서드의 반환 타입으로 사용하는 것이 권장되며, 필드나 메서드 파라미터로 사용하는 것은 오히려 코드를 복잡하게 만든다는 지적도 있습니다.');
SET @e172 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('인터페이스의 default 메서드', '자바 8에서 인터페이스에 default 메서드가 도입된 배경과, 여러 인터페이스의 default 메서드가 충돌할 때 해결 방법을 설명하시오.', 'ESSAY', 'HIGH', 'LANGUAGE',
 '자바 8 이전에는 인터페이스에 메서드를 추가하면 그 인터페이스를 구현한 모든 클래스가 컴파일 오류를 일으켰기 때문에, 이미 널리 쓰이는 인터페이스(예: Collection)에 새 기능을 추가하기가 어려웠습니다. default 메서드는 인터페이스에 구현체를 가진 메서드를 정의할 수 있게 해, 기존 구현 클래스들을 수정하지 않고도 인터페이스에 새 메서드를 추가할 수 있게 해주었습니다. 한 클래스가 서로 다른 인터페이스로부터 같은 시그니처의 default 메서드를 상속받아 충돌이 발생하면 컴파일 오류가 발생하며, 이 경우 구현 클래스가 해당 메서드를 직접 오버라이드해 명시적으로 어느 인터페이스의 메서드를 호출할지(InterfaceName.super.method())를 정해주어야 합니다.');
SET @e173 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('문자열 리터럴과 String Pool', '문자열 리터럴로 생성한 String과 new 연산자로 생성한 String의 차이를 String Pool과 함께 설명하시오.', 'ESSAY', 'LOW', 'LANGUAGE',
 '문자열 리터럴로 String을 생성하면 JVM은 힙 내부의 String Pool(상수 풀)이라는 별도 영역을 확인해, 이미 동일한 내용의 문자열이 있으면 그 객체를 재사용하고 없으면 새로 만들어 풀에 등록하기 때문에 같은 내용의 리터럴은 항상 같은 객체를 참조합니다. new String("...")으로 생성하면 String Pool을 거치지 않고 힙에 매번 새로운 객체를 만들기 때문에, 내용이 같아도 리터럴로 만든 문자열과는 ==비교 시 서로 다른 객체로 판정됩니다. 이 때문에 문자열의 내용을 비교할 때는 참조를 비교하는 == 대신 값을 비교하는 equals()를 사용해야 하며, String Pool 덕분에 같은 리터럴을 반복해서 사용해도 메모리를 절약할 수 있습니다.');
SET @e174 = LAST_INSERT_ID();

INSERT INTO question (title, content, type, difficulty, category, explanation) VALUES
('enum의 활용', '자바의 enum이 단순한 상수 집합을 넘어 클래스로서 가질 수 있는 특징과, enum을 이용한 싱글톤 구현 방식을 설명하시오.', 'ESSAY', 'MEDIUM', 'LANGUAGE',
 '자바의 enum은 단순한 정수 상수의 집합이 아니라 실제로는 각 열거 상수가 해당 enum 타입의 인스턴스인 클래스로, 필드와 메서드를 가질 수 있고 상수마다 다른 동작을 구현하기 위해 각 상수별로 메서드를 오버라이드할 수도 있습니다. enum 상수는 JVM에 의해 클래스가 로딩되는 시점에 딱 한 번만 생성되는 것이 보장되고 직렬화·리플렉션을 통한 임의의 추가 인스턴스 생성이 언어 차원에서 막혀 있어, 별도의 동기화 코드 없이도 안전한 싱글톤을 구현하는 방법으로 자주 사용됩니다. 이런 이유로 이펙티브 자바에서도 enum 방식의 싱글톤 구현을 다른 방식보다 권장합니다.');
SET @e175 = LAST_INSERT_ID();

-- 서술형 태그 (LANGUAGE 추가 22문항)

-- 서술형 태그
INSERT INTO question_tag (question_id, name) VALUES
(@e19, 'JVM'), (@e19, '가비지 컬렉션'), (@e19, '메모리 구조'),
(@e20, 'equals'), (@e20, 'hashCode'),
(@e21, '체크 예외'), (@e21, '언체크 예외'),
(@e154, '제네릭'), (@e154, '타입 소거'), (@e154, '타입 안전성'),
(@e155, '어노테이션'), (@e155, '리플렉션'), (@e155, '메타데이터'),
(@e156, '인터페이스'), (@e156, '추상 클래스'), (@e156, '다중 상속'),
(@e157, '함수형 인터페이스'), (@e157, '람다식'), (@e157, '익명 클래스'),
(@e158, 'Stream API'), (@e158, '지연 연산'), (@e158, '중간 연산'),
(@e159, 'String'), (@e159, 'StringBuilder'), (@e159, 'StringBuffer'), (@e159, '불변성'),
(@e160, '불변 객체'), (@e160, '스레드 안전성'), (@e160, '방어적 복사'),
(@e161, 'List'), (@e161, 'Set'), (@e161, 'Map'), (@e161, '컬렉션 프레임워크'),
(@e162, '오토박싱'), (@e162, '언박싱'), (@e162, '래퍼 클래스'),
(@e163, 'static'), (@e163, '클래스 변수'), (@e163, '인스턴스 멤버'),
(@e164, 'final'), (@e164, '상속 제한'), (@e164, '불변 변수'),
(@e165, '접근 제어자'), (@e165, '캡슐화'), (@e165, '패키지'),
(@e166, 'try-finally'), (@e166, 'return'), (@e166, '예외 처리 흐름'),
(@e167, 'Thread'), (@e167, 'Runnable'), (@e167, '스레드 생성'),
(@e168, 'synchronized'), (@e168, 'ReentrantLock'), (@e168, '동시성 제어'),
(@e169, 'volatile'), (@e169, '가시성'), (@e169, '원자성'),
(@e170, '클래스 로더'), (@e170, '위임 모델'), (@e170, '부트스트랩 클래스 로더'),
(@e171, '직렬화'), (@e171, 'SerialVersionUID'), (@e171, '역직렬화'),
(@e172, 'Optional'), (@e172, 'NullPointerException'), (@e172, 'null 안전성'),
(@e173, 'default 메서드'), (@e173, '인터페이스'), (@e173, '메서드 충돌'),
(@e174, 'String Pool'), (@e174, '문자열 리터럴'), (@e174, 'equals와 =='),
(@e175, 'enum'), (@e175, '싱글톤'), (@e175, '열거형');

