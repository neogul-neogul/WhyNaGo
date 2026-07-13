import type { Problem } from "@/types";

// 문제은행 (프로그래머스식 목록) — 더미
// qi: 객관식이면 multipleChoiceQuestions[], 서술형이면 essayQuestions[]의 인덱스
export const problemBank: Problem[] = [
  { type: "객관식", qi: 0, title: "TCP와 UDP의 핵심 차이", cat: "네트워크", keywords: ["네트워크", "TCP/UDP", "전송 계층"], diff: "중", solved: 1284, rate: 62, status: "완료" },
  { type: "서술형", qi: 3, title: "DFS와 BFS, 언제 무엇을 쓰는가", cat: "알고리즘", keywords: ["알고리즘", "그래프 탐색", "DFS/BFS"], diff: "상", solved: 687, rate: 41, status: "안 푼 문제" },
  { type: "객관식", qi: 2, title: "교착 상태(Deadlock)의 발생 조건", cat: "운영체제", keywords: ["운영체제", "교착 상태", "동시성"], diff: "상", solved: 734, rate: 38, status: "오답" },
  { type: "서술형", qi: 1, title: "정규화와 반정규화의 트레이드오프", cat: "DB", keywords: ["데이터베이스", "정규화", "스키마 설계"], diff: "상", solved: 516, rate: 33, status: "안 푼 문제" },
  { type: "객관식", qi: 1, title: "해시 테이블의 평균 탐색 복잡도", cat: "자료구조", keywords: ["자료구조", "해시 테이블", "시간 복잡도"], diff: "하", solved: 2031, rate: 81, status: "완료" },
  { type: "객관식", qi: 3, title: "트랜잭션 ACID — 격리성", cat: "DB", keywords: ["데이터베이스", "트랜잭션", "격리 수준"], diff: "중", solved: 1102, rate: 57, status: "안 푼 문제" },
  { type: "서술형", qi: 2, title: "세마포어와 뮤텍스의 차이", cat: "운영체제", keywords: ["운영체제", "동기화", "세마포어/뮤텍스"], diff: "중", solved: 923, rate: 51, status: "오답" },
  { type: "서술형", qi: 0, title: "TCP 흐름 제어 vs 혼잡 제어", cat: "네트워크", keywords: ["네트워크", "TCP", "흐름/혼잡 제어"], diff: "중", solved: 842, rate: 45, status: "안 푼 문제" },
  { type: "객관식", qi: 0, title: "3-way handshake의 동작 과정", cat: "네트워크", keywords: ["네트워크", "TCP", "연결 수립"], diff: "중", solved: 611, rate: 49, status: "안 푼 문제" },
  { type: "객관식", qi: 1, title: "배열과 연결 리스트의 접근 비용", cat: "자료구조", keywords: ["자료구조", "배열/연결 리스트", "시간 복잡도"], diff: "하", solved: 1760, rate: 78, status: "완료" },
  { type: "서술형", qi: 1, title: "인덱스를 무분별하게 늘리면 안 되는 이유", cat: "DB", keywords: ["데이터베이스", "인덱스", "쿼리 성능"], diff: "중", solved: 498, rate: 39, status: "안 푼 문제" },
  { type: "객관식", qi: 2, title: "프로세스 스케줄링 기법 비교", cat: "운영체제", keywords: ["운영체제", "스케줄링", "CPU"], diff: "상", solved: 402, rate: 35, status: "안 푼 문제" },
];