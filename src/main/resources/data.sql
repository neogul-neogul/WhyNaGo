-- 로컬(local) 프로파일 전용 시드. 프런트 목데이터(mcqQuestions[0]) 기반.
-- 본질문(1) → 정답 보기 선택 시 꼬리질문(2)으로 분기. 오답 보기는 분기 없이 종료.

-- 본질문
INSERT INTO question (id, title, content, type, difficulty, category, explanation, is_root) VALUES
(1, 'TCP와 UDP의 핵심 차이', 'TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'TCP는 3-way handshake로 연결을 수립하고 순서 보장·재전송·흐름 제어를 제공한다. UDP는 이런 보장 없이 헤더가 작고 지연이 적어 실시간 스트리밍·DNS 등에 쓰인다.', TRUE);

-- 꼬리질문(정답 보기에서 이어짐)
INSERT INTO question (id, title, content, type, difficulty, category, explanation, is_root) VALUES
(2, '실시간 음성 통화와 UDP', '실시간 음성 통화에 UDP가 적합한 가장 큰 이유는?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '실시간 통화는 약간의 패킷 손실보다 지연이 적은 것이 품질에 더 중요하므로, 재전송·흐름 제어가 없는 UDP가 유리하다.', FALSE);

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
