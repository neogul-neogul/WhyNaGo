-- 로컬(local) 프로파일 전용 시드. 프런트 목데이터(mcqQuestions[0]) 기반.
-- 본질문(1) → 정답 보기 선택 시 꼬리질문(2)으로 분기. 오답 보기는 분기 없이 종료.

-- 본질문
INSERT INTO question (id, title, content, type, difficulty, category, explanation) VALUES
(1, 'TCP와 UDP의 핵심 차이', 'TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'TCP는 3-way handshake로 연결을 수립하고 순서 보장·재전송·흐름 제어를 제공한다. UDP는 이런 보장 없이 헤더가 작고 지연이 적어 실시간 스트리밍·DNS 등에 쓰인다.');

-- 꼬리질문(정답 보기에서 이어짐)
INSERT INTO question (id, title, content, type, difficulty, category, explanation) VALUES
(2, '실시간 음성 통화와 UDP', '실시간 음성 통화에 UDP가 적합한 가장 큰 이유는?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '실시간 통화는 약간의 패킷 손실보다 지연이 적은 것이 품질에 더 중요하므로, 재전송·흐름 제어가 없는 UDP가 유리하다.');

-- 본질문 선택지 (1번 보기가 정답, 정답 선택 시 꼬리질문 2로 분기)
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(1, 1, 'TCP는 연결 지향형으로 신뢰성을 보장하고, UDP는 비연결형으로 속도를 우선한다.', 1, TRUE, '', 2),
(2, 1, 'TCP는 비연결형이고 UDP는 연결을 수립한 뒤 통신한다.', 2, FALSE, 'TCP와 UDP의 연결 방식이 반대로 서술되었다. TCP가 연결 지향형, UDP가 비연결형이다.', NULL),
(3, 1, '두 프로토콜 모두 신뢰성을 보장하며 TCP가 항상 더 느리다.', 3, FALSE, 'UDP는 신뢰성을 보장하지 않으며, TCP가 항상 느리다는 서술은 틀렸다.', NULL),
(4, 1, 'UDP는 흐름 제어와 혼잡 제어를 모두 수행한다.', 4, FALSE, '흐름 제어와 혼잡 제어는 TCP의 기능이다.', NULL);

-- 꼬리질문 선택지 (1번 보기가 정답, 이어지는 꼬리질문 없음 → 세션 종료)
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(5, 2, '약간의 패킷 손실보다 낮은 지연이 더 중요하기 때문', 1, TRUE, '', NULL),
(6, 2, '재전송 덕분에 음질이 더 좋아지기 때문', 2, FALSE, '재전송은 오히려 지연을 늘려 실시간성을 해친다.', NULL),
(7, 2, 'UDP가 TCP보다 보안이 강하기 때문', 3, FALSE, 'UDP는 보안과 직접 관련이 없다.', NULL),
(8, 2, 'UDP가 순서 보장을 해주기 때문', 4, FALSE, '순서 보장은 TCP의 특징이다.', NULL);

-- 태그
INSERT INTO question_tag (id, question_id, name) VALUES
(1, 1, 'NETWORK'), (2, 1, 'TCP/UDP'), (3, 1, '전송 계층'),
(4, 2, 'NETWORK'), (5, 2, 'UDP');



-- ============================================================
-- 서술형(ESSAY). 프런트 목데이터(essayQuestions[]) 기반.
-- 꼬리질문은 세션마다 AI가 동적 생성하므로 DB에는 본질문만 저장한다.
-- explanation 컬럼은 본질문 모범답안(mock의 model)을 담는다.
-- ============================================================

-- 서술형 본질문
INSERT INTO question (id, title, content, type, difficulty, category, explanation) VALUES
(3, 'TCP 흐름 제어 vs 혼잡 제어', 'TCP의 흐름 제어(Flow Control)와 혼잡 제어(Congestion Control)의 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'NETWORK',
 '흐름 제어는 수신자의 처리 속도에 맞춰 송신량을 조절하는 것으로, 수신 버퍼가 넘치지 않도록 슬라이딩 윈도우로 송신 윈도우 크기를 조정합니다. 혼잡 제어는 네트워크 전체의 혼잡을 막기 위한 것으로, 느린 시작(Slow Start)·혼잡 회피 등으로 전송 속도를 조절합니다. 즉 흐름 제어는 종단 간(수신자) 문제, 혼잡 제어는 네트워크 경로 전체의 문제를 다룹니다.'),
(4, '정규화와 반정규화의 트레이드오프', '정규화와 반정규화를 각각 언제 사용하는지, 트레이드오프와 함께 설명하시오.', 'ESSAY', 'HIGH', 'DB',
 '정규화는 데이터 중복과 이상 현상을 제거해 무결성을 높이는 과정으로, 삽입·갱신·삭제 이상을 방지하지만 조인이 많아져 조회 성능이 떨어질 수 있습니다. 반정규화는 의도적으로 중복을 허용해 조인을 줄이고 조회 성능을 높이는 기법으로, 읽기가 많고 성능이 중요한 경우 사용하지만 데이터 정합성 관리 부담이 늘어납니다. 일반적으로 정규화를 기본으로 하되, 성능 병목이 명확한 부분에 한해 반정규화를 적용합니다.'),
(5, '세마포어와 뮤텍스의 차이', '세마포어(Semaphore)와 뮤텍스(Mutex)의 차이를 설명하시오.', 'ESSAY', 'MEDIUM', 'OS',
 '뮤텍스는 하나의 자원에 대한 상호 배제를 보장하는 잠금으로, 락을 획득한 스레드만 해제할 수 있습니다(소유권 있음). 세마포어는 정해진 개수만큼의 자원 접근을 허용하는 카운터로, 소유권 개념이 없어 다른 스레드가 signal 할 수 있습니다. 즉 뮤텍스는 카운트가 1인 이진 잠금에 가깝고, 세마포어는 여러 자원 접근을 제어하는 일반화된 형태입니다.'),
(6, 'DFS와 BFS, 언제 무엇을 쓰는가', 'DFS와 BFS의 차이와, 각각 어떤 상황에 적합한지 설명하시오.', 'ESSAY', 'HIGH', 'ALGORITHM',
 'DFS는 한 경로를 끝까지 탐색한 뒤 되돌아오는 방식으로 스택(또는 재귀)을 사용하며, 경로 존재 여부·백트래킹·사이클 탐지에 적합합니다. BFS는 가까운 노드부터 레벨 단위로 탐색하며 큐를 사용하고, 가중치가 같은 그래프에서 최단 경로를 찾을 때 적합합니다. 메모리는 보통 DFS가 적게 쓰고, 최단 거리 보장은 BFS가 가능합니다.');

-- 서술형 태그 (mock essayQuestions[].keywords)
INSERT INTO question_tag (id, question_id, name) VALUES
(6, 3, '흐름 제어'), (7, 3, '혼잡 제어'), (8, 3, '슬라이딩 윈도우'), (9, 3, '느린 시작'), (10, 3, '수신 버퍼'),
(11, 4, '정규화'), (12, 4, '반정규화'), (13, 4, '이상 현상'), (14, 4, '조인'), (15, 4, '무결성'), (16, 4, '조회 성능'),
(17, 5, '뮤텍스'), (18, 5, '세마포어'), (19, 5, '상호 배제'), (20, 5, '소유권'), (21, 5, '카운터'),
(22, 6, 'DFS'), (23, 6, 'BFS'), (24, 6, '스택'), (25, 6, '큐'), (26, 6, '최단 경로'), (27, 6, '백트래킹');
