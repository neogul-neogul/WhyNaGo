import type { WrongNote } from "@/types";

// 오답노트 — 더미 (본 질문 + 꼬리질문, 내가 고른 답 포함)
export const wrongData: WrongNote[] = [
  {
    q: "TCP 3-way handshake 과정에서 SYN, SYN-ACK, ACK 패킷의 순서로 옳은 것은?",
    cat: "네트워크",
    diff: "중",
    status: "미복습",
    repeat: 3,
    source: "문제 풀이",
    solvedAt: "2026.06.25",
    options: [
      "SYN → ACK → SYN-ACK",
      "SYN → SYN-ACK → ACK",
      "ACK → SYN → SYN-ACK",
      "SYN-ACK → SYN → ACK",
    ],
    myAnswer: 0,
    correctAnswer: 1,
    explanation:
      "클라이언트가 SYN을 보내 연결을 요청하고, 서버가 SYN-ACK로 응답하며, 마지막으로 클라이언트가 ACK를 보내 연결이 수립된다. 3-way handshake의 정확한 순서는 SYN → SYN-ACK → ACK 이다.",
    wrongExp:
      "SYN 다음에 바로 ACK가 오는 것으로 골랐지만, 서버의 응답인 SYN-ACK 단계가 빠졌습니다. 연결 수립은 반드시 SYN → SYN-ACK → ACK 3단계를 거칩니다.",
    followups: [
      {
        text: "연결을 종료할 때 사용하는 방식으로 올바른 것은?",
        options: ["4-way handshake", "3-way handshake", "2-way handshake", "핸드쉐이크 없이 종료"],
        answer: 0,
        myAnswer: 2,
        explanation: "TCP 연결 종료는 FIN·ACK를 주고받는 4-way handshake로 이뤄진다.",
        wrongExp: "2-way로 골랐지만, 종료는 양쪽이 각각 FIN과 ACK를 주고받는 4단계를 거칩니다.",
      },
      {
        text: "마지막 ACK가 유실되면 어떻게 되는가?",
        options: ["서버가 SYN-ACK를 재전송하며 재시도한다", "연결이 즉시 성립된다", "클라이언트가 데이터를 먼저 보낸다", "연결이 영구히 끊긴다"],
        answer: 0,
        myAnswer: 0,
        explanation: "ACK가 유실되면 서버는 타임아웃 후 SYN-ACK를 재전송해 재시도한다.",
        wrongExp: "",
      },
    ],
  },
  {
    q: "DB 인덱스로 B-Tree 대신 B+Tree를 주로 사용하는 이유로 옳은 것은?",
    cat: "DB",
    diff: "상",
    status: "미복습",
    repeat: 2,
    source: "자가진단",
    solvedAt: "2026.06.24",
    options: [
      "모든 데이터를 내부 노드에 저장해 탐색이 빠르다",
      "리프 노드가 연결 리스트로 이어져 범위 검색에 유리하다",
      "트리 높이가 항상 1로 유지된다",
      "중복 키를 저장할 수 없다",
    ],
    myAnswer: 0,
    correctAnswer: 1,
    explanation:
      "B+Tree는 실제 데이터를 리프 노드에만 저장하고, 리프 노드끼리 연결 리스트로 이어져 있어 순차·범위 검색에 매우 유리하다. 그래서 DB 인덱스에 널리 쓰인다.",
    wrongExp:
      "내부 노드에도 데이터를 저장하는 것은 B-Tree의 특성입니다. B+Tree는 데이터를 리프에만 두어 범위 검색 성능을 확보한다는 점을 놓쳤습니다.",
    followups: [
      {
        text: "인덱스를 무분별하게 늘릴 때 생기는 문제로 올바른 것은?",
        options: ["쓰기 성능 저하와 저장 공간 증가", "조회가 불가능해진다", "트리가 사라진다", "정합성이 깨진다"],
        answer: 0,
        myAnswer: 1,
        explanation: "인덱스가 많으면 INSERT/UPDATE 시 인덱스도 갱신해야 해 쓰기 성능이 떨어지고 저장 공간도 늘어난다.",
        wrongExp: "조회 불가로 골랐지만, 인덱스가 많아도 조회는 가능합니다. 문제는 쓰기 성능과 저장 공간입니다.",
      },
      {
        text: "B+Tree에서 범위 검색이 빠른 핵심 이유는?",
        options: ["리프 노드가 연결 리스트로 이어져 있다", "트리 높이가 0이다", "키가 정렬되지 않는다", "내부 노드에 데이터가 있다"],
        answer: 0,
        myAnswer: 0,
        explanation: "리프 노드가 순서대로 연결되어 있어 범위 검색 시 순차 이동만으로 빠르게 찾는다.",
        wrongExp: "",
      },
    ],
  },
  {
    q: "프로세스와 스레드의 메모리 공유에 대한 설명으로 옳은 것은?",
    cat: "운영체제",
    diff: "중",
    status: "미복습",
    repeat: 1,
    source: "문제 풀이",
    solvedAt: "2026.06.24",
    options: [
      "스레드는 스택까지 모두 공유한다",
      "프로세스끼리 기본적으로 힙을 공유한다",
      "스레드는 코드·데이터·힙을 공유하고 스택은 개별로 가진다",
      "프로세스와 스레드는 메모리 구조가 동일하다",
    ],
    myAnswer: 1,
    correctAnswer: 2,
    explanation:
      "같은 프로세스 내 스레드들은 코드·데이터·힙 영역을 공유하지만, 각 스레드는 독립된 스택과 레지스터를 갖는다. 프로세스끼리는 기본적으로 메모리를 공유하지 않는다.",
    wrongExp:
      "프로세스끼리 힙을 공유한다고 골랐지만, 프로세스는 기본적으로 독립된 메모리 공간을 가집니다. 힙·데이터를 공유하는 것은 같은 프로세스 내 스레드입니다.",
    followups: [
      {
        text: "멀티스레드 환경의 대표적인 위험은?",
        options: ["경쟁 상태(Race Condition)", "메모리 절약", "무조건 속도 향상", "컨텍스트 스위칭 제거"],
        answer: 0,
        myAnswer: 0,
        explanation: "여러 스레드가 공유 자원에 동시 접근하면 경쟁 상태가 발생해 결과가 비결정적이 된다.",
        wrongExp: "",
      },
      {
        text: "스레드 동기화 도구가 아닌 것은?",
        options: ["페이지 테이블", "뮤텍스", "세마포어", "모니터"],
        answer: 0,
        myAnswer: 0,
        explanation: "페이지 테이블은 가상 메모리 주소 변환용이며 동기화 도구가 아니다.",
        wrongExp: "",
      },
    ],
  },
  {
    q: "정규화 단계 중 부분 함수 종속을 제거하는 단계는?",
    cat: "DB",
    diff: "중",
    status: "미복습",
    repeat: 1,
    source: "재풀이",
    solvedAt: "2026.06.23",
    options: ["제1정규형(1NF)", "제2정규형(2NF)", "제3정규형(3NF)", "BCNF"],
    myAnswer: 2,
    correctAnswer: 1,
    explanation:
      "2NF는 기본키의 일부에만 종속되는 부분 함수 종속을 제거한다. 1NF는 원자값, 3NF는 이행적 종속 제거를 다룬다.",
    wrongExp:
      "3NF로 골랐지만 3NF가 제거하는 것은 이행적 함수 종속입니다. 부분 함수 종속을 제거하는 단계는 2NF입니다.",
    followups: [
      {
        text: "이행적 함수 종속을 제거하는 단계는?",
        options: ["3NF", "1NF", "2NF", "0NF"],
        answer: 0,
        myAnswer: 2,
        explanation: "3NF는 기본키가 아닌 속성에 종속되는 이행적 함수 종속을 제거한다.",
        wrongExp: "2NF로 골랐지만 2NF는 부분 종속 제거 단계입니다. 이행적 종속은 3NF에서 제거합니다.",
      },
      {
        text: "과도한 정규화의 단점으로 올바른 것은?",
        options: ["조인 증가로 조회 성능이 저하될 수 있다", "데이터 중복이 늘어난다", "이상 현상이 늘어난다", "무결성이 약해진다"],
        answer: 0,
        myAnswer: 0,
        explanation: "정규화를 과도하게 하면 테이블이 나뉘어 조인이 많아지고 조회 성능이 떨어질 수 있다.",
        wrongExp: "",
      },
    ],
  },
  {
    q: "다익스트라 알고리즘이 음의 가중치 간선에서 올바르게 동작하지 않는 이유는?",
    cat: "알고리즘",
    diff: "상",
    status: "미복습",
    repeat: 1,
    source: "직접 저장",
    solvedAt: "2026.06.22",
    options: [
      "우선순위 큐를 사용하기 때문",
      "한 번 확정한 최단 거리를 다시 갱신하지 않기 때문",
      "시간복잡도가 너무 크기 때문",
      "방문 배열을 쓰지 않기 때문",
    ],
    myAnswer: 3,
    correctAnswer: 1,
    explanation:
      "다익스트라는 방문 확정된 노드의 최단 거리를 더 이상 갱신하지 않는 그리디 방식이다. 음의 간선이 있으면 나중에 더 짧은 경로가 나타날 수 있어 결과가 틀어진다. 이 경우 벨만-포드를 써야 한다.",
    wrongExp:
      "방문 배열 유무가 원인이 아닙니다. 핵심은 한 번 확정한 최단 거리를 다시 갱신하지 않는 그리디 특성 때문에 음의 간선을 제대로 처리하지 못한다는 점입니다.",
    followups: [
      {
        text: "음의 가중치에서 최단 경로를 구하는 알고리즘은?",
        options: ["벨만-포드", "다익스트라", "BFS", "이분 탐색"],
        answer: 0,
        myAnswer: 0,
        explanation: "벨만-포드는 간선을 반복 이완해 음의 가중치에서도 최단 경로를 구한다.",
        wrongExp: "",
      },
      {
        text: "우선순위 큐를 쓴 다익스트라의 시간복잡도는?",
        options: ["O(E log V)", "O(V^2 log V)", "O(V!)", "O(1)"],
        answer: 0,
        myAnswer: 1,
        explanation: "우선순위 큐(민힙) 기반 다익스트라의 시간복잡도는 O(E log V)이다.",
        wrongExp: "O(V^2 log V)로 골랐지만, 민힙을 쓰면 O(E log V)입니다. O(V^2)는 배열 구현 경우입니다.",
      },
    ],
  },
  {
    q: "옵저버 패턴과 발행-구독(Pub-Sub) 패턴의 차이로 옳은 것은?",
    cat: "디자인패턴",
    diff: "중",
    status: "미복습",
    repeat: 2,
    source: "문제 풀이",
    solvedAt: "2026.06.21",
    options: [
      "둘은 완전히 동일한 패턴이다",
      "옵저버는 주체와 관찰자가 직접 연결되고, Pub-Sub은 브로커를 통해 느슨히 결합된다",
      "Pub-Sub은 관찰자가 하나만 가능하다",
      "옵저버 패턴은 비동기에서만 쓰인다",
    ],
    myAnswer: 0,
    correctAnswer: 1,
    explanation:
      "옵저버 패턴은 주체(Subject)가 관찰자(Observer)를 직접 알고 통지한다. Pub-Sub은 발행자와 구독자 사이에 메시지 브로커/채널이 끼어 서로를 몰라도 되는 느슨한 결합을 만든다.",
    wrongExp:
      "두 패턴을 동일하다고 골랐지만, 결합도에서 분명히 다릅니다. 옵저버는 직접 결합, Pub-Sub은 브로커를 통한 느슨한 결합입니다.",
    followups: [
      {
        text: "Pub-Sub이 느슨한 결합을 만드는 핵심 요소는?",
        options: ["메시지 브로커/채널", "직접 참조", "상속", "전역 변수"],
        answer: 0,
        myAnswer: 0,
        explanation: "발행자와 구독자 사이에 브로커/채널이 끼어 서로를 몰라도 되는 느슨한 결합을 만든다.",
        wrongExp: "",
      },
      {
        text: "옵저버 패턴의 대표적인 사용처는?",
        options: ["UI 이벤트 리스너", "정렬 알고리즘", "메모리 할당", "파일 압축"],
        answer: 0,
        myAnswer: 0,
        explanation: "상태 변화를 여러 구성요소에 알려야 하는 UI 이벤트 리스너가 대표적인 예다.",
        wrongExp: "",
      },
    ],
  },
];
