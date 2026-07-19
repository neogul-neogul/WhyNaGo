# 도메인 문서

> 이 문서는 프런트엔드 **객관식 문제 풀이 화면**(`front` 의 `MultipleChoiceQuiz` / `ProblemBank` / `mocks/questions.ts`)에서 관찰되는 동작을 도메인 모델로 정리한 것이다. 서술형·알림은 아직 작성 중이다.

---

## 엔티티

### Question (문제)

본 질문과 꼬리 질문 모두 이 엔티티로 표현한다. 꼬리 질문은 별도 테이블이 아니라, 선택지(`AnswerChoice.relatedQuestionId`)가 가리키는 또 다른 `Question` 이다. → [꼬리질문 진행 정책](#꼬리질문-분기꼬리-진행-정책) 참고.

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | 문제 ID | PK |
| title | 제목 | 문제은행 목록에 노출되는 짧은 제목 (예: "TCP와 UDP의 핵심 차이") |
| content | 지문 | 실제 문제 본문/발문 |
| type | 유형 | `MULTIPLE_CHOICE`(객관식) \| `ESSAY`(서술형) |
| difficulty | 난이도 | `LOW`(하) \| `MEDIUM`(중) \| `HIGH`(상) |
| category | 카테고리 | `DB` \| `NETWORK` \| `ALGORITHM` \| `DATA_STRUCTURE` \| `OS` \| `DESIGN_PATTERN` \| `LANGUAGE` |
| explanation | 정답 해설 | 문제 전체(정답) 해설. 채점 후 항상 노출. 선택지별 오답 사유(`AnswerChoice.explanation`)와 별개 |

> **집계(파생) 값** — 문제은행 화면의 `완료한 사람 수`, `정답률`, 사용자별 `상태(완료/오답/안 푼 문제)` 는 `SolvedMultipleChoice` 를 집계해 얻는 조회용 값이며 `Question` 컬럼으로 저장하지 않는다. → [문제은행 표시 정책](#문제은행-표시-정책)

### QuestionTag (문제 태그)

프런트의 `keywords`(예: "TCP/UDP", "전송 계층")에 대응한다. 한 문제에 여러 태그.

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | 태그 ID | PK |
| questionId | 문제 ID | FK → Question |
| name | 태그명 | 검색·필터에 사용 |

### AnswerChoice (객관식 선택지)

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | 선택지 ID | PK |
| questionId | 문제 ID | FK → Question (이 선택지가 속한 문제) |
| content | 내용 | 보기 텍스트 |
| sequence | 보기 순서 | 1부터. 화면의 보기 번호(1·2·3·4) |
| isCorrect | 정답 여부 | 이 보기가 정답인지. 한 문제에 `true` 는 정확히 1개(단일 정답) |
| explanation | 오답 해설 | 이 보기를 골랐을 때의 "왜 틀렸나" 해설. **정답 보기는 비움** |
| relatedQuestionId | 연결 꼬리질문 ID | 이 보기 선택 후 채점 시 이어지는 꼬리 `Question`. 없으면(null) 그 보기 선택 시 세션 종료 |

### SolvedSession (풀이 세션)

본 질문부터 꼬리 질문까지, 한 번에 이어 푼 하나의 풀이 단위.

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | 세션 ID | PK |
| userId | 사용자 ID | FK → User |
| type | 유형 | `MULTIPLE_CHOICE` \| `ESSAY` |
| status | 상태 | `COMPLETED`(끝까지 진행). 저장 API는 완료된 세션만 받는다 — [세션 종료 정책](#세션-집계-정책) |
| totalCount | 전체 문항 수 | 세션에서 실제 응답한 문항 수(본질문+거친 꼬리질문) |
| correctCount | 정답 수 | 맞힌 문항 수 |
| solvedAt | 완료 시각 | |

### SolvedMultipleChoice (푼 객관식 문항)

한 풀이 세션 안에서 사용자가 응답한 개별 객관식 문항(본질문/꼬리질문 각각 1행).

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | PK | |
| solvedSessionId | 풀이 세션 ID | FK → SolvedSession |
| userId | 사용자 ID | (조회 편의를 위한 비정규화, 세션에서도 도출 가능) |
| type | 문항 유형 | `MAIN`(본질문) \| `FOLLOWUP`(꼬리질문) |
| sequence | 세션 내 순서 | 본질문이면 1, 이후 꼬리질문 2·3… |
| userChoiceId | 유저 답안 | FK → AnswerChoice (사용자가 고른 보기) |
| answerChoiceId | 실제 답안 | FK → AnswerChoice (정답 보기). 정답은 불변이므로 `AnswerChoice.isCorrect` 로 도출 가능 — 조회 편의를 위한 선택 필드 |
| isCorrect | 정답 여부 | `userChoiceId == answerChoiceId` |
| solvedAt | 응답 시각 | 개별 문항 채점 시각 (선택) |
| isRoot | 본질문 여부 | 문제은행에 노출되는 진입 문제인지. 꼬리질문(다른 보기의 `relatedQuestionId` 대상)은 `false` |

### WrongNote (오답노트)

오답이 발생하면 자동 생성/갱신된다. — 단순히 "틀린 문제 목록"이며, 반복 횟수·북마크·출처만 관리한다. → [오답 자동 저장 정책](#오답-자동-저장-정책)

| 이름              | 한글       | 설명                               |
|-----------------|----------|----------------------------------|
| id              | PK       |                                  |
| userId          | 사용자 ID   | FK → User                        |
| solvedSessionId | 풀이 세션 ID | FK → SolvedSession (틀린 문제 풀이 세션) |
| isBookmarked    | 북마크 여부   |                                  |

### EssaySolved (푼 서술형 문제, 작성 중)

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | | |
| problemId | | |
| userId | | |

### NotificationSetting (작성 중)

| 이름 | 한글 | 설명 |
| --- | --- | --- |
| id | | |
| userId | | |
| everyDayRemind | 매일 리마인드 | |
| streakStopPrevention | 연속 학습 중단 방지 | |
| wrongNote | 오답 복습 알림 | |
| weeklyReport | 주간 리포트 수신 | |

---

## 정책 · 제약

### 객관식 응답 규칙
- 선택지는 **단일 선택**이다. 한 문제의 `AnswerChoice.isCorrect == true` 는 **정확히 1개**(단일 정답).
- 채점("정답 확인")은 선택지가 선택된 상태에서만 가능하다.
- **한 문항당 1회만 응답**한다. 채점 후에는 선택을 바꿀 수 없다.
- 채점 즉시 정답 해설(`Question.explanation`)을 노출한다. 오답이면 사용자가 고른 보기의 오답 해설(`AnswerChoice.explanation`)을 함께 노출한다.
- 선택지는 **4개 고정**(4지선다).

### 꼬리질문 분기·꼬리 진행 정책
- 꼬리질문은 **사용자가 고른 보기(`AnswerChoice`)의 `relatedQuestionId`** 로 이어진다(보기별 분기).
- 공개 시점: **직전 문항을 채점하면** 이어질 꼬리질문이 공개된다. **정답/오답과 무관**하다.
- 고른 보기의 `relatedQuestionId` 가 없으면 그 지점에서 세션이 종료된다.
- ⚠️ **프런트 정합 필요**: 현재 `MultipleChoiceQuiz` 구현은 이 분기를 반영하지 않고, 고른 보기와 무관하게 문제에 딸린 꼬리질문을 **순차 공개**한다. 도메인 정책(보기별 분기)에 맞춰 추후 수정이 필요하다.

### 오답 자동 저장 정책
- 채점 결과가 오답이면 해당 문항이 **자동으로 오답노트에 저장**된다("오답노트에 자동 저장됨").
- 사용자가 문제를 직접 삭제하거나 북마크할 수 있다.

### 세션 집계 정책
- 본질문~꼬리질문 전체가 하나의 `SolvedSession` 이다.
- 결과 화면의 값 = 세션 집계: 정답률 `correctCount / totalCount`, 정답 수, 오답 수(`totalCount - correctCount`).
- 마지막 문항까지 답하고 "저장하기"로 끝낸 세션만 저장하며 상태는 `COMPLETED`다. 중간에 "종료하기"로 이탈한 풀이는 저장하지 않는다(중단 세션 저장은 추후 필요 시 별도 논의).

### 문제은행 표시 정책
- `완료한 사람 수` = 그 문제를 푼(세션에 포함된) 사용자 수(집계).
- `정답률` = 전체 사용자 응답 중 정답 비율(집계).
- 사용자별 `상태`: 해당 사용자가 그 문제를 마지막에 **맞혔으면 `완료`**, **틀렸으면 `오답`**, **푼 적 없으면 `안 푼 문제`**.
- 필터: 유형(전체/객관식/서술형), 난이도(전체/하/중/상), 카테고리, 제목·개념 검색.

---

## Enum

| Enum | 값 |
| --- | --- |
| Category | `DB` · `NETWORK` · `ALGORITHM` · `DATA_STRUCTURE` · `OS` · `DESIGN_PATTERN` · `LANGUAGE` |
| Difficulty | `LOW`(하) · `MEDIUM`(중) · `HIGH`(상) |
| QuestionType | `MULTIPLE_CHOICE` · `ESSAY` |
| WrongNoteSource | `PROBLEM_SOLVING` · `SELF_DIAGNOSIS` · `RETRY` · `MANUAL` |

---

## 결정 사항 (확정)

- **본질문 구분**: `Question.isRoot` 플래그 사용. ("어떤 보기의 `relatedQuestionId` 로도 참조되지 않는 문제 = 본질문"으로도 도출 가능하나 플래그 병행 허용)
- **선택지 개수**: 4개 고정(4지선다).
- **오답노트 상태**: 두지 않는다. 틀린 문제 목록 + 북마크만 유지, 재풀이 시 새로운 문제 세션에서 진행.
- **정답 불변**: 문제 정답은 바뀌지 않으므로 정답 스냅샷을 별도로 관리하지 않는다. `SolvedMultipleChoice.answerChoiceId` 는 조회 편의용 선택 필드.

## 보류 (추후 MVP)

- **1일 1면접(서술형) 세션 통합**: 학습 기록의 `method` 에 "1일 1면접"이 있으나, 면접은 추후 MVP이므로 `SolvedSession` 통합/분리 여부는 나중에 결정한다.
