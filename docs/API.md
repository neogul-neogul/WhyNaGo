# **API Convention**

## **목표**

이 문서는 백엔드가 제공하는 HTTP API의 **요청/응답 규격**을 기록한다. 프론트엔드 연동, 신규 입사자 온보딩, API 변경 리뷰의 기준 문서로 사용한다.

API는 `docs/ARCHITECTURE.md`의 레이어 규칙을 따른다. 요청/응답 DTO는 presentation 레이어에서만 정의하고, service는 command/result 모델로 소통한다. 에러 응답 형식은 `docs/EXCEPTION.md`를 따른다.

## **공통 규칙**

- 기본 경로 접두사는 `/api`다.
- 요청/응답 본문은 모두 `application/json`이다.
- 성공 응답의 HTTP 상태 코드는 유스케이스 의미에 맞춘다. 리소스 생성은 `201 Created`, 조회는 `200 OK`를 사용한다.
- 요청 형식 검증 실패는 `400 Bad Request`와 `INVALID_INPUT` 코드로 내려간다.
- 비즈니스 규칙 위반은 도메인 `ErrorCode`에 정의된 상태 코드와 코드로 내려간다.
- 인증 사용자 식별자(`userId`)는 인증 계층에서 해석해 컨트롤러로 전달한다. 요청 본문에 담지 않는다.

## **에러 응답 형식**

모든 에러는 다음 형식을 사용한다. (상세 규칙은 `docs/EXCEPTION.md` 참고)

```json
{
  "code": "SOLVED_SESSION_BROKEN_CHAIN",
  "message": "꼬리질문 연결이 올바르지 않습니다."
}
```

---

# **Question API**

문제 조회와 서술형 풀이 진행을 담당한다. 관련 도메인은 `question`이다.

## **문제 목록 조회**

문제은행 화면의 목록을 조회한다. 사용자가 바로 시작할 수 있는 **진입 문제**만 반환한다. 다른 문제의 선택지에서 이어지는 객관식 꼬리질문은 목록에서 제외된다. 서술형 꼬리질문은 세션마다 AI가 생성해 재사용 `Question`이 없으므로(→ `docs/DOMAIN.md` 서술형 꼬리질문 생성 정책), 서술형 문제는 모두 진입 문제로 조회된다.

객관식과 서술형이 한 목록에 함께 내려간다. 유형을 구분해야 하면 `type` 필터를 쓰거나 응답의 `type` 필드로 분기한다.

### **Endpoint**

```
GET /api/questions
```

- 성공 시 `200 OK`와 문제 배열을 반환한다.
- 정렬은 문제 ID 내림차순(최신순) 고정이다. 페이징은 없다.

### **Query Parameters**

모두 선택이며, 생략하면 해당 조건을 적용하지 않는다. 여러 개를 함께 주면 모두 만족하는 문제만 조회된다.

| **파라미터** | **타입** | **설명** |
| --- | --- | --- |
| `type` | String | 문제 유형. `MULTIPLE_CHOICE` \| `ESSAY` |
| `difficulty` | String | 난이도. `LOW` \| `MEDIUM` \| `HIGH` |
| `category` | String | 카테고리. `DB` \| `NETWORK` \| `ALGORITHM` \| `DATA_STRUCTURE` \| `OS` \| `DESIGN_PATTERN` \| `LANGUAGE` |
| `q` | String | 제목·지문 키워드. 부분 일치이며 대소문자를 구분하지 않는다. |

### **Response Body**

```json
[
  {
    "id": 101,
    "title": "TCP 흐름 제어 vs 혼잡 제어",
    "content": "TCP의 흐름 제어(Flow Control)와 혼잡 제어(Congestion Control)의 차이를 설명하시오.",
    "type": "ESSAY",
    "difficulty": "MEDIUM",
    "category": "NETWORK",
    "explanation": "흐름 제어는 수신자의 처리 속도에 맞춰 송신량을 조절하는 것으로...",
    "choices": [],
    "tags": ["흐름 제어", "혼잡 제어"]
  },
  {
    "id": 1,
    "title": "TCP와 UDP의 핵심 차이",
    "content": "TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?",
    "type": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "category": "NETWORK",
    "explanation": "TCP는 3-way handshake로 연결을 수립하고 순서 보장·재전송·흐름 제어를 제공한다...",
    "choices": [
      {
        "id": 1,
        "content": "TCP는 연결 지향형으로 신뢰성을 보장하고, UDP는 비연결형으로 속도를 우선한다.",
        "sequence": 1,
        "explanation": "",
        "relatedQuestionId": 2
      }
    ],
    "tags": ["NETWORK"]
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제 ID. 서술형 풀이·세션 저장에 이 값을 사용한다. |
| `title` | String | 문제 제목. |
| `content` | String | 문제 발문. |
| `type` | String | 문제 유형(`MULTIPLE_CHOICE` \| `ESSAY`). |
| `difficulty` | String | 난이도. |
| `category` | String | 카테고리. |
| `explanation` | String | 정답 해설. |
| `choices` | Array | 선택지 목록. **서술형은 항상 빈 배열**이다. |
| `choices[].id` | Long | 선택지 ID. 채점 조회에 사용한다. |
| `choices[].content` | String | 선택지 내용. |
| `choices[].sequence` | int | 선택지 표시 순서. |
| `choices[].explanation` | String | 이 선택지를 골랐을 때의 오답 해설. 정답 선택지는 빈 값이다. |
| `choices[].relatedQuestionId` | Long | 이 선택지를 골랐을 때 이어지는 꼬리질문 ID. 없으면 `null`(그 지점에서 종료). |
| `tags` | Array | 문제 태그 이름 목록. 없으면 빈 배열. |

> 정답 여부(`isCorrect`)는 목록 응답에 포함하지 않는다. 객관식 채점은 보기 선택 결과 조회 API로만 확인한다.

### **에러**

조건에 맞는 문제가 없으면 에러가 아니라 빈 배열(`[]`)과 `200 OK`를 반환한다.

> **알려진 문제**: 필터에 enum에 없는 값을 보내면(예: `?type=FOO`) 요청 형식 오류이므로 `400 INVALID_INPUT`이어야 하는데, 현재는 `500 SERVER_ERROR`로 응답한다. 요청 바인딩 예외(`MethodArgumentTypeMismatchException`)가 `GlobalExceptionHandler`에 등록되지 않아서다. 별도 이슈로 처리한다.

---

## **서술형 세션 시작**

서술형 풀이를 시작할 때 **대화 식별자(`conversationId`)**를 발급한다. 이후 답변 채점 요청은 이 식별자로 묶이며, 서버는 대화 이력(ChatMemory)에 이전 문답을 보관해 꼬리질문 생성의 맥락으로 사용한다. 따라서 클라이언트는 매 요청에 전체 문답을 다시 보낼 필요가 없다.

### **Endpoint**

```
POST /api/questions/{questionId}/essay/sessions
```

- `questionId`는 서술형 본 질문 ID다.
- 성공 시 `201 Created`를 반환한다.

### **Response Body**

```json
{
  "conversationId": "3f1c8a2e-9b7d-4c2a-8f0e-1a2b3c4d5e6f"
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `conversationId` | String | 서버가 발급한 대화 식별자. 이후 답변 채점 요청에 담아 보낸다. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `QUESTION_NOT_FOUND` | `questionId` 문제가 존재하지 않음. |
| 400 | `QUESTION_NOT_ESSAY` | `questionId` 문제가 서술형(`ESSAY`)이 아님. |

---

## **서술형 답변 채점·꼬리질문 생성**

서술형 풀이 한 턴을 처리한다. 사용자가 제출한 답변을 LLM으로 채점해 결과(피드백·모범답안·통과 여부)를 반환하고, 이어질 꼬리질문을 AI로 생성해 함께 반환한다. 서술형 세션은 완료 시점에만 저장하므로(→ `docs/DOMAIN.md` 세션 집계 정책) 이 API는 아무것도 저장하지 않는다.

**이전 문답 맥락은 서버의 대화 이력(ChatMemory)이 보관**하므로, 클라이언트는 전체 문답이 아니라 **이번 턴의 질문·답변만** `conversationId`와 함께 보낸다. 본 질문 + 꼬리질문 2개(총 3문항)로 진행되며, 서버는 대화 이력의 턴 수로 진행 단계를 판단한다. 마지막 문항(3번째)을 채점하면 더 생성할 꼬리질문이 없어 `nextFollowup`이 `null`로 내려가고, 서버는 해당 대화 이력을 정리한다.

### **Endpoint**

```
POST /api/questions/{questionId}/essay/answers
```

- `questionId`는 서술형 본 질문 ID다.
- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "conversationId": "3f1c8a2e-9b7d-4c2a-8f0e-1a2b3c4d5e6f",
  "question": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
  "answer": "수신자가 광고한 윈도우 크기만큼만 데이터를 보내..."
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `conversationId` | String | O | 세션 시작 API로 발급받은 대화 식별자. |
| `question` | String | O | 이번에 채점할 문항 발문. 본 질문은 조회 API로 받은 텍스트, 꼬리질문은 직전 응답의 `nextFollowup.question` 텍스트를 담는다. |
| `answer` | String | O | 이번 문항에 사용자가 작성한 답변. |

**제약**:

- `conversationId`·`question`·`answer`는 공백일 수 없다. 위반 시 `INVALID_INPUT`이다.
- 꼬리질문 생성 여부와 진행 단계는 서버가 대화 이력의 턴 수로 판단한다(1·2턴째는 꼬리질문 생성, 3턴째는 생성하지 않고 대화 정리).

### **Response Body**

```json
{
  "grading": {
    "feedback": "흐름 제어와 혼잡 제어의 목적 차이(수신자 보호 vs 네트워크 보호)를 명확히 구분하면 더 좋습니다.",
    "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만 송신자가 미확인 데이터를 보내도록 하여 수신 버퍼가 넘치지 않게 조절합니다.",
    "isCorrect": true
  },
  "nextFollowup": {
    "question": "혼잡이 감지되면 TCP는 전송 속도를 어떻게 조절하나요?"
  }
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `grading` | Object | 이번 문항 답변에 대한 채점 결과. |
| `grading.feedback` | String | AI 피드백. |
| `grading.modelAnswer` | String | 해당 문항의 모범답안·해설. |
| `grading.isCorrect` | boolean | 통과 여부. LLM이 매긴 0~10 점수를 서버가 임계값(7 이상 통과)으로 환산한 값(→ `docs/DOMAIN.md` 서술형 정답 판정 기준). |
| `nextFollowup` | Object | 생성된 다음 꼬리질문. 마지막 문항(3턴째)이면 `null`(면접 종료). |
| `nextFollowup.question` | String | 생성된 꼬리질문 발문. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락 또는 공백. |
| 404 | `QUESTION_NOT_FOUND` | `questionId` 문제가 존재하지 않음. |
| 400 | `QUESTION_NOT_ESSAY` | `questionId` 문제가 서술형(`ESSAY`)이 아님. |
| 503 | `ESSAY_AI_UNAVAILABLE` | AI 채점·꼬리질문 생성 호출이 실패함(LLM 장애 등). |

---

# **SolvedSession API**

객관식 풀이 세션(본질문 → 꼬리질문 체인)을 저장하고 채점 결과를 반환한다. 관련 도메인은 `solvedsession`이다.

## **세션 저장**

사용자가 본질문부터 시작해, 고른 선택지에 연결된 꼬리질문을 이어 푼 뒤 "저장하기"를 누르면 그동안 푼 문제들을 하나의 세션으로 저장한다. 서버는 제출된 문항들이 실제로 이어지는 체인인지 검증한 뒤, 채점 결과와 함께 세션·문항 이력을 저장하고 오답이 있으면 오답노트를 생성한다.

### **Endpoint**

```
POST /api/solved-sessions
```

- 성공 시 `201 Created`를 반환한다.

### **Request Body**

```json
{
  "rootQuestion": {
    "questionId": 1,
    "choiceId": 3,
    "relationQuestionId": 5
  },
  "followupQuestions": [
    {
      "questionId": 5,
      "choiceId": 12,
      "relationQuestionId": 8
    },
    {
      "questionId": 8,
      "choiceId": 20,
      "relationQuestionId": null
    }
  ],
  "startedAt": "2026-06-25T09:58:00"
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `rootQuestion` | Object | O | 본질문 풀이 항목. |
| `followupQuestions` | Array | O | 꼬리질문 풀이 항목 목록. 본질문에서 이어 푼 순서대로. 비어 있을 수 있다(꼬리질문 없이 종료). |
| `*.questionId` | Long | O | 푼 문제 ID. |
| `*.choiceId` | Long | O | 사용자가 고른 선택지 ID. 해당 문제에 속한 선택지여야 한다. 정답 여부는 서버가 판정한다. |
| `*.relationQuestionId` | Long | X | 고른 선택지가 이어지는 다음 문제 ID. 마지막 항목은 `null`(체인 종료). |
| `startedAt` | LocalDateTime | O | 본질문을 처음 받은 시각(클라이언트 기준 세션 시작 시각). 학습 기록의 소요시간(`solvedAt - startedAt`) 계산에 사용한다(→ `docs/DOMAIN.md` 학습 기록 집계 정책). |

**체인 검증 규칙** (실패 시 `SOLVED_SESSION_BROKEN_CHAIN`):

- 각 항목의 `relationQuestionId`는 그 항목에서 고른 선택지(`choiceId`)의 실제 연결 문제(`AnswerChoice.relatedQuestionId`)와 일치해야 한다.
- 각 항목의 `relationQuestionId`는 바로 다음 항목의 `questionId`와 같아야 한다. 마지막 항목은 `relationQuestionId`가 `null`이어야 한다(더 이어지는 꼬리질문 없음).

### **Response Body**

```json
{
  "sessionId": 42
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `sessionId` | Long | 저장된 세션 ID. |

> 채점 결과·해설 표시는 클라이언트가 풀이 중 이미 처리하므로, 저장 응답은 생성된 세션 ID만 반환한다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락 등 요청 형식 검증 실패. |
| 400 | `SOLVED_SESSION_BROKEN_CHAIN` | 꼬리질문 연결이 맞지 않음(`relationQuestionId`가 고른 선택지의 실제 연결 또는 다음 항목과 불일치). |
| 400 | `CHOICE_NOT_IN_QUESTION` | `choiceId` 선택지가 해당 문제에 속하지 않음. |
| 404 | `CHOICE_NOT_FOUND` | `choiceId` 선택지가 없거나, 문제의 정답 선택지를 찾을 수 없음. |

## **서술형 세션 저장**

사용자가 서술형 본질문부터 꼬리질문2까지(총 3문항)를 이어 푼 뒤 "저장하기"를 누르면, 전체 문답과 채점 결과를 하나의 세션으로 저장한다. 서술형 꼬리질문·채점은 풀이 중 AI가 생성하므로 재사용 가능한 `Question`이 없어, 발문·답변·피드백·모범답안을 **스냅샷**으로 함께 저장한다(→ `docs/DOMAIN.md` 서술형 풀이 흐름 정책). 저장 시 재채점하지 않으며, 각 문항의 통과 여부(`isCorrect`)는 채점 API가 산출한 값을 클라이언트가 그대로 전달한다.

### **Endpoint**

```
POST /api/solved-sessions/essay
```

- 성공 시 `201 Created`를 반환한다.

### **Request Body**

```json
{
  "rootQuestion": {
    "questionId": 1,
    "questionText": "트랜잭션 격리 수준을 설명하시오.",
    "userAnswer": "격리 수준은 4단계로...",
    "feedback": "이상 현상 설명을 보완하면 좋습니다.",
    "modelAnswer": "READ UNCOMMITTED부터 SERIALIZABLE까지...",
    "isCorrect": true
  },
  "followupQuestions": [
    {
      "questionId": null,
      "questionText": "팬텀 리드는 어떤 격리 수준에서 막히나요?",
      "userAnswer": "SERIALIZABLE에서 막힙니다.",
      "feedback": "정확합니다.",
      "modelAnswer": "SERIALIZABLE는 팬텀 리드까지 방지합니다.",
      "isCorrect": true
    },
    {
      "questionId": null,
      "questionText": "MVCC는 격리 수준과 어떤 관계인가요?",
      "userAnswer": "스냅샷으로 읽기 일관성을 제공합니다.",
      "feedback": "핵심은 맞으나 언두 로그 언급이 없습니다.",
      "modelAnswer": "MVCC는 언두 로그 기반 스냅샷으로...",
      "isCorrect": false
    }
  ],
  "startedAt": "2026-06-24T09:20:00"
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `rootQuestion` | Object | O | 본질문 문답 스냅샷. |
| `followupQuestions` | Array | O | 꼬리질문 문답 스냅샷 목록. **정확히 2개**(본질문 1 + 꼬리질문 2 = 3문항 고정). |
| `*.questionId` | Long | △ | 본질문만 값. 꼬리질문은 세션마다 AI가 생성해 재사용 `Question`이 없으므로 `null`. |
| `*.questionText` | String | O | 문항 발문 스냅샷. |
| `*.userAnswer` | String | O | 사용자가 작성한 답변. |
| `*.feedback` | String | O | AI 피드백. 채점 API 응답의 `grading.feedback`을 그대로 담는다. |
| `*.modelAnswer` | String | O | 모범답안. 채점 API 응답의 `grading.modelAnswer`를 그대로 담는다. |
| `*.isCorrect` | boolean | O | 통과 여부. 채점 API가 산출한 값(서버 산출, 클라이언트 relay). 세션 `correctCount` 집계에 사용한다. |
| `startedAt` | LocalDateTime | O | 본질문을 처음 받은 시각(클라이언트 기준 세션 시작 시각). 학습 기록의 소요시간 계산에 사용한다(→ `docs/DOMAIN.md` 학습 기록 집계 정책). |

**제약**:

- `followupQuestions`는 정확히 2개여야 한다. 벗어나면 `INVALID_INPUT`이다.
- `questionText`·`userAnswer`·`feedback`·`modelAnswer`는 공백일 수 없다.

### **Response Body**

```json
{
  "sessionId": 42
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `sessionId` | Long | 저장된 세션 ID. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락, 꼬리질문 수가 2개가 아님, 필수 문자열이 공백 등 요청 형식 검증 실패. |
| 404 | `QUESTION_NOT_FOUND` | `rootQuestion.questionId` 문제가 존재하지 않음. |
| 400 | `QUESTION_NOT_ESSAY` | `rootQuestion.questionId` 문제가 서술형(`ESSAY`)이 아님. |

---

# **WrongNote API**

오답노트 목록·상세 조회, 북마크 수정, 삭제를 담당한다. 관련 도메인은 `wrongnote`다.

오답노트는 풀이 세션 저장 시 오답이 있으면 **자동 생성**되며(→ `docs/DOMAIN.md` 오답 자동 저장 정책), 별도의 생성 API는 없다. 상태·반복 횟수·출처 개념은 두지 않으므로(→ `docs/DOMAIN.md` 결정 사항) 목록 필터는 북마크 여부뿐이다. 모든 엔드포인트는 인증된 사용자 **본인 소유의 오답노트만** 조회·수정·삭제할 수 있다.

## **오답노트 목록 조회**

### **Endpoint**

```
GET /api/wrong-notes
```

| **Query Param** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `bookmarked` | boolean | X | `true`면 북마크한 오답노트만 반환. 생략하면 전체 반환. |

정렬은 `solvedAt` 내림차순(최신순)으로 고정한다.

### **Response Body**

```json
[
  {
    "id": 12,
    "type": "MULTIPLE_CHOICE",
    "category": "NETWORK",
    "difficulty": "MEDIUM",
    "title": "TCP 3-way handshake",
    "isBookmarked": true,
    "solvedAt": "2026-06-25T10:00:00"
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 오답노트 ID. 상세 조회·북마크 수정·삭제에 사용. |
| `type` | String | 오답이 발생한 세션 유형. `MULTIPLE_CHOICE` \| `ESSAY`. |
| `category` | String | 본 질문의 카테고리(`Category`). |
| `difficulty` | String | 본 질문의 난이도(`Difficulty`). |
| `title` | String | 본 질문의 제목(`Question.title`). |
| `isBookmarked` | boolean | 북마크 여부. |
| `solvedAt` | LocalDateTime | 해당 풀이 세션 완료 시각(`SolvedSession.solvedAt`). |

### **에러**

없음. 오답노트가 없으면 빈 배열을 반환한다.

---

## **오답노트 상세 조회**

본질문부터 꼬리질문까지 전체 문항과 내가 고른 답·정답·해설을 함께 반환한다. 원본 풀이 세션(`SolvedSession`)의 유형에 따라 `multipleChoiceItems` 또는 `essayItems` 중 **정확히 하나만** 채워진다.

### **Endpoint**

```
GET /api/wrong-notes/{wrongNoteId}
```

### **Response Body — 객관식(`type = MULTIPLE_CHOICE`)**

```json
{
  "id": 12,
  "type": "MULTIPLE_CHOICE",
  "category": "NETWORK",
  "difficulty": "MEDIUM",
  "isBookmarked": true,
  "solvedAt": "2026-06-25T10:00:00",
  "multipleChoiceItems": [
    {
      "sequence": 1,
      "questionId": 1,
      "title": "TCP 3-way handshake 순서",
      "content": "TCP 3-way handshake 과정에서 SYN, SYN-ACK, ACK 패킷의 순서로 옳은 것은?",
      "choices": [
        { "id": 10, "content": "SYN → ACK → SYN-ACK", "sequence": 1, "isCorrect": false },
        { "id": 11, "content": "SYN → SYN-ACK → ACK", "sequence": 2, "isCorrect": true }
      ],
      "userChoiceId": 10,
      "correctChoiceId": 11,
      "isCorrect": false,
      "explanation": "클라이언트가 SYN을 보내...",
      "choiceExplanation": "SYN 다음에 바로 ACK가 오는 것으로 골랐지만..."
    }
  ],
  "essayItems": null
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `multipleChoiceItems[].sequence` | int | 세션 내 순서. 본질문 1, 이후 꼬리질문 2·3. |
| `multipleChoiceItems[].questionId` | Long | 문제 ID. |
| `multipleChoiceItems[].title` / `content` | String | 문제 제목·지문. |
| `multipleChoiceItems[].choices` | Array | 이 문항의 보기 4개 전체(`id`, `content`, `sequence`, `isCorrect`). 이미 채점이 끝난 조회이므로 정답 여부를 그대로 노출한다. |
| `multipleChoiceItems[].userChoiceId` | Long | 사용자가 고른 보기 ID. |
| `multipleChoiceItems[].correctChoiceId` | Long | 정답 보기 ID. |
| `multipleChoiceItems[].isCorrect` | boolean | 이 문항 정답 여부. |
| `multipleChoiceItems[].explanation` | String | 문제 전체(정답) 해설(`Question.explanation`). |
| `multipleChoiceItems[].choiceExplanation` | String \| null | 고른 보기의 오답 해설. 정답이면 `null`. |

### **Response Body — 서술형(`type = ESSAY`)**

```json
{
  "id": 20,
  "type": "ESSAY",
  "category": "DB",
  "difficulty": "HIGH",
  "isBookmarked": false,
  "solvedAt": "2026-06-24T09:30:00",
  "multipleChoiceItems": null,
  "essayItems": [
    {
      "sequence": 1,
      "questionText": "트랜잭션 격리 수준을 설명하시오.",
      "userAnswer": "격리 수준은 4단계로...",
      "feedback": "이상 현상 설명을 보완하면 좋습니다.",
      "modelAnswer": "READ UNCOMMITTED부터 SERIALIZABLE까지...",
      "isCorrect": true
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `essayItems[].sequence` | int | 세션 내 순서. 본질문 1, 꼬리질문 2·3. |
| `essayItems[].questionText` | String | 문항 발문 스냅샷(`EssaySolved.questionText`). |
| `essayItems[].userAnswer` | String | 사용자가 작성한 답변. |
| `essayItems[].feedback` | String | AI 피드백. |
| `essayItems[].modelAnswer` | String | 모범답안. |
| `essayItems[].isCorrect` | boolean | 통과 여부. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `WRONG_NOTE_NOT_FOUND` | `wrongNoteId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

## **오답노트 북마크 수정**

### **Endpoint**

```
PATCH /api/wrong-notes/{wrongNoteId}/bookmark
```

- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "bookmarked": true
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `bookmarked` | boolean | O | 설정할 북마크 상태. |

### **Response Body**

```json
{
  "isBookmarked": true
}
```

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | `bookmarked` 누락. |
| 404 | `WRONG_NOTE_NOT_FOUND` | `wrongNoteId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

## **오답노트 삭제**

### **Endpoint**

```
DELETE /api/wrong-notes/{wrongNoteId}
```

- 성공 시 `204 No Content`를 반환한다. 원본 풀이 세션·문항 이력은 삭제하지 않고 오답노트만 삭제한다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `WRONG_NOTE_NOT_FOUND` | `wrongNoteId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

# **LearningRecord API**

학습 기록(잔디·최근 기록·연속/누적 학습일) 조회를 담당한다. 관련 도메인은 `learningrecord`다.

별도 저장 테이블 없이 `SolvedSession`을 조회 시점에 집계해 응답한다(→ `docs/DOMAIN.md` 학습 기록 집계 정책). 아래 항목은 이번 구현 범위에서 **제외**했다(→ `docs/DOMAIN.md` 보류):

- 최근 기록의 진입 경로(method: 문제 풀이·1일 1면접·오답 복습·모의 진단·카테고리별) 구분 — 대신 세션 유형(`type`)만 내려준다.
- 잔디 등급(0~4단계)·"학습량 점수" — 대신 일자별 세션 수·문항 수 원본 집계값만 내려준다.

모든 엔드포인트는 인증된 사용자 **본인의 기록만** 조회한다.

## **최근 기록 목록 조회**

### **Endpoint**

```
GET /api/learning-records/recent
```

| **Query Param** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `size` | int | X | 조회할 최근 세션 개수. 생략하면 `20`. |

정렬은 `solvedAt` 내림차순(최신순)으로 고정한다.

### **Response Body**

```json
[
  {
    "sessionId": 42,
    "type": "MULTIPLE_CHOICE",
    "category": "NETWORK",
    "totalCount": 3,
    "correctCount": 2,
    "wrongCount": 1,
    "startedAt": "2026-06-25T09:58:00",
    "solvedAt": "2026-06-25T10:16:00"
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `sessionId` | Long | 풀이 세션 ID(`SolvedSession.id`). |
| `type` | String | 세션 유형. `MULTIPLE_CHOICE` \| `ESSAY`. |
| `category` | String | 본질문의 카테고리(`Category`). |
| `totalCount` | int | 전체 문항 수. |
| `correctCount` | int | 정답 수. |
| `wrongCount` | int | 오답 수(`totalCount - correctCount`). |
| `startedAt` | LocalDateTime | 세션 시작 시각. |
| `solvedAt` | LocalDateTime | 세션 완료 시각. |

### **에러**

없음. 기록이 없으면 빈 배열을 반환한다.

---

## **연속·누적 학습일 조회**

### **Endpoint**

```
GET /api/learning-records/streak
```

`SolvedSession.solvedAt`의 distinct 날짜를 기준으로 계산한다. "하루"의 경계는 KST(Asia/Seoul) 자정이다. 오늘 아직 풀지 않았어도 어제까지 이어진 연속 학습일은 자정이 지나기 전까지는 끊긴 것으로 보지 않는다.

### **Response Body**

```json
{
  "streakDays": 7,
  "cumulativeDays": 42
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `streakDays` | int | 연속 학습일. |
| `cumulativeDays` | int | 누적 학습일(학습한 날의 총 수, distinct). |

### **에러**

없음.

---

## **일자별 학습량(잔디) 조회**

### **Endpoint**

```
GET /api/learning-records/daily-counts
```

| **Query Param** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `from` | LocalDate (`yyyy-MM-dd`) | X | 조회 시작일(포함). 생략하면 `to`로부터 364일 전. |
| `to` | LocalDate (`yyyy-MM-dd`) | X | 조회 종료일(포함). 생략하면 오늘(KST). |

학습이 없었던 날짜는 응답에 포함되지 않는다(0으로 간주). `from`이 `to`보다 늦으면 빈 배열을 반환한다.

### **Response Body**

```json
[
  {
    "date": "2026-06-25",
    "sessionCount": 2,
    "questionCount": 5
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `date` | LocalDate | 날짜. |
| `sessionCount` | int | 그 날짜에 완료한 풀이 세션 수. |
| `questionCount` | int | 그 날짜에 푼 전체 문항 수(세션별 `totalCount`의 합). |

### **에러**

없음. 범위 내 기록이 없으면 빈 배열을 반환한다.

---

# **User API**

로그인한 사용자 본인의 프로필 조회·수정을 담당한다. 관련 도메인은 `user`다.

## **내 프로필 조회**

### **Endpoint**

```
GET /api/users/me
```

### **Response Body**

```json
{
  "dailyGoal": 10
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `dailyGoal` | int | 최소 학습 목표(하루 최소 풀이 세션 수). 가입 시 기본값(10)으로 설정된다. |

### **에러**

없음.

---

## **최소 학습 목표 수정**

### **Endpoint**

```
PATCH /api/users/me
```

- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "dailyGoal": 15
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `dailyGoal` | int | O | 새 최소 학습 목표. 1 이상이어야 한다. |

### **Response Body**

```json
{
  "dailyGoal": 15
}
```

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | `dailyGoal`이 없거나 1 미만. |
