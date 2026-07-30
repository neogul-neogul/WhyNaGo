-- 로컬(local) 프로파일 참고용 추가 시드: 7개 카테고리 × 25문항 = 총 175개 객관식 문항.
-- docs/SCRIPT.md 규칙으로 생성. "본질문"·"꼬리질문"은 생성 순서를 가리키는 표현일 뿐,
-- 도메인/DB에는 엔티티 차원의 구분이 없다 — 모든 question 행이 동등한 독립 문항이며 문제은행에 노출된다.
-- 각 카테고리는 5개 그룹(각 1개 + 그 보기 4개에 연결된 문항 4개)으로 구성되며,
-- 모든 문항의 보기 4개가 전부 다른 문항으로 연결된다(NULL 없음).
-- 그룹당 꼬리 4개(@qN_1~@qN_4)는 부모 문항(@qN)으로 되돌아가는 순환 연결이다.
-- 정답 보기의 위치(sequence)는 문항마다 1~4로 무작위 분산되어 있다.
-- id는 AUTO_INCREMENT이며, 문항 간 연결은 세션 변수(@qN, @qN_1~@qN_4)로 캡처한 실제 id를 사용한다.
-- data.sql과 별도 파일이라 기본 설정으로는 자동 로드되지 않는다.
-- 실제 적용하려면 data.sql에 이어붙이거나 data-locations에 classpath:data2.sql을 추가한다.

-- 테스트 유저 (email: test@test.test / password: test)
-- 비밀번호는 BCryptPasswordEncoder(기본 strength 10)로 해시. 평문 test와 매칭됨.
INSERT INTO users (email, password, nickname, position) VALUES
('test@test.test', '$2a$10$QnOWMKP6UpzZHYzphdiuaOyg.Ei2ihHclJ1r5YmU0WYsvxQxSi/8q', 'test', 'BACKEND');


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

