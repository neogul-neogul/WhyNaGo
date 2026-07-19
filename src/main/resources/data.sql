-- 로컬(local) 프로파일 전용 시드. 본질문 1개 + 보기별로 분기하는 깊이 2의 꼬리질문 체인.
-- 분기: 본질문(1) 1번 보기 → 꼬리질문(2) → 꼬리질문(4), 2번 보기 → 꼬리질문(3) → 꼬리질문(5).
-- 3·4번 보기는 연결 꼬리질문이 없어 그 지점에서 세션이 종료된다.

INSERT INTO question (id, title, content, type, difficulty, category, explanation) VALUES
(1, 'TCP와 UDP의 핵심 차이', 'TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'TCP는 3-way handshake로 연결을 수립하고 순서 보장·재전송·흐름 제어를 제공한다. UDP는 이런 보장 없이 헤더가 작고 지연이 적어 실시간 스트리밍·DNS 등에 쓰인다.'),
(2, '실시간 음성 통화와 UDP', '실시간 음성 통화에 UDP가 적합한 가장 큰 이유는?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 '실시간 통화는 약간의 패킷 손실보다 지연이 적은 것이 품질에 더 중요하므로, 재전송·흐름 제어가 없는 UDP가 유리하다.'),
(3, 'TCP의 연결 수립', 'TCP가 데이터 전송 전에 연결을 수립하는 절차는?', 'MULTIPLE_CHOICE', 'MEDIUM', 'NETWORK',
 'TCP는 SYN, SYN-ACK, ACK 세 단계의 3-way handshake로 연결을 수립한 뒤 데이터를 주고받는다.'),
(4, 'UDP 위에서의 신뢰성 보완', 'UDP를 쓰면서도 신뢰성이 필요할 때 일반적으로 사용하는 방법은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 'UDP 자체는 신뢰성을 제공하지 않으므로, QUIC처럼 애플리케이션 계층에서 재전송·순서 보장을 직접 구현해 보완한다.'),
(5, '3-way handshake의 각 단계', '3-way handshake에서 클라이언트와 서버가 주고받는 메시지 순서로 옳은 것은?', 'MULTIPLE_CHOICE', 'HIGH', 'NETWORK',
 '클라이언트가 SYN을 보내고, 서버가 SYN-ACK으로 응답하며, 클라이언트가 ACK을 보내면 연결이 수립된다.');

-- 본질문(1) 선택지: 1번 보기(정답) → 꼬리질문 2, 2번 보기(오답) → 꼬리질문 3
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(1, 1, 'TCP는 연결 지향형으로 신뢰성을 보장하고, UDP는 비연결형으로 속도를 우선한다.', 1, TRUE, '', 2),
(2, 1, 'TCP는 비연결형이고 UDP는 연결을 수립한 뒤 통신한다.', 2, FALSE, 'TCP와 UDP의 연결 방식이 반대로 서술되었다. TCP가 연결 지향형, UDP가 비연결형이다.', 3),
(3, 1, '두 프로토콜 모두 신뢰성을 보장하며 TCP가 항상 더 느리다.', 3, FALSE, 'UDP는 신뢰성을 보장하지 않으며, TCP가 항상 느리다는 서술은 틀렸다.', NULL),
(4, 1, 'UDP는 흐름 제어와 혼잡 제어를 모두 수행한다.', 4, FALSE, '흐름 제어와 혼잡 제어는 TCP의 기능이다.', NULL);

-- 꼬리질문(2) 선택지: 1번 보기(정답) → 꼬리질문 4, 2번 보기(오답) → 꼬리질문 4
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(5, 2, '약간의 패킷 손실보다 낮은 지연이 더 중요하기 때문', 1, TRUE, '', 4),
(6, 2, '재전송 덕분에 음질이 더 좋아지기 때문', 2, FALSE, '재전송은 오히려 지연을 늘려 실시간성을 해친다.', 4),
(7, 2, 'UDP가 TCP보다 보안이 강하기 때문', 3, FALSE, 'UDP는 보안과 직접 관련이 없다.', NULL),
(8, 2, 'UDP가 순서 보장을 해주기 때문', 4, FALSE, '순서 보장은 TCP의 특징이다.', NULL);

-- 꼬리질문(3) 선택지: 1번 보기(정답) → 꼬리질문 5, 2번 보기(오답) → 꼬리질문 5
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(9, 3, 'SYN, SYN-ACK, ACK을 주고받는 3-way handshake를 수행한다.', 1, TRUE, '', 5),
(10, 3, '데이터를 먼저 보내고 응답이 오면 연결된 것으로 간주한다.', 2, FALSE, 'TCP는 데이터 전송 전에 반드시 handshake로 연결을 수립한다.', 5),
(11, 3, '서버가 먼저 클라이언트에 연결 요청을 보낸다.', 3, FALSE, '연결 요청(SYN)은 클라이언트가 먼저 보낸다.', NULL),
(12, 3, '4-way handshake로 연결을 수립한다.', 4, FALSE, '4-way handshake는 연결 종료 절차다.', NULL);

-- 꼬리질문(4) 선택지: 연결 꼬리질문 없음 → 세션 종료 지점
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(13, 4, 'QUIC처럼 애플리케이션 계층에서 재전송과 순서 보장을 구현한다.', 1, TRUE, '', NULL),
(14, 4, 'UDP 헤더의 체크섬만으로 신뢰성이 충분히 보장된다.', 2, FALSE, '체크섬은 손상 감지만 할 뿐 재전송·순서 보장을 제공하지 않는다.', NULL),
(15, 4, '신뢰성이 필요하면 UDP로는 불가능하므로 반드시 TCP만 써야 한다.', 3, FALSE, 'QUIC 등 UDP 기반 프로토콜이 신뢰성을 구현한 사례가 있다.', NULL),
(16, 4, '라우터가 자동으로 유실 패킷을 재전송해 준다.', 4, FALSE, '라우터는 패킷 재전송을 책임지지 않는다.', NULL);

-- 꼬리질문(5) 선택지: 연결 꼬리질문 없음 → 세션 종료 지점
INSERT INTO answer_choice (id, question_id, content, sequence, is_correct, explanation, related_question_id) VALUES
(17, 5, '클라이언트 SYN → 서버 SYN-ACK → 클라이언트 ACK', 1, TRUE, '', NULL),
(18, 5, '클라이언트 ACK → 서버 SYN → 클라이언트 SYN-ACK', 2, FALSE, '연결은 클라이언트의 SYN으로 시작한다.', NULL),
(19, 5, '서버 SYN → 클라이언트 SYN-ACK → 서버 ACK', 3, FALSE, '연결 요청은 서버가 아니라 클라이언트가 시작한다.', NULL),
(20, 5, '클라이언트 SYN → 서버 ACK → 클라이언트 FIN', 4, FALSE, 'FIN은 연결 종료에 사용하는 플래그다.', NULL);

-- 태그
INSERT INTO question_tag (id, question_id, name) VALUES
(1, 1, 'NETWORK'), (2, 1, 'TCP/UDP'), (3, 1, '전송 계층'),
(4, 2, 'NETWORK'), (5, 2, 'UDP'),
(6, 3, 'NETWORK'), (7, 3, 'TCP'),
(8, 4, 'NETWORK'), (9, 4, 'QUIC'),
(10, 5, 'NETWORK'), (11, 5, 'handshake');
