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

## **서술형 답변 채점·꼬리질문 생성**

서술형 풀이 한 턴을 처리한다. 사용자가 제출한 답변을 LLM으로 채점해 결과(피드백·모범답안)를 반환하고, 이어질 꼬리질문을 AI로 생성해 함께 반환한다. 서술형 세션은 완료 시점에만 저장하므로(→ `docs/DOMAIN.md` 세션 집계 정책) 이 API는 아무것도 저장하지 않으며, 클라이언트가 지금까지의 문답을 매 요청에 실어 보낸다.

본 질문 + 꼬리질문 2개(총 3문항)로 진행되며, 채점과 생성은 하나의 요청에서 함께 처리한다. 마지막 문항(3번째)을 채점하면 더 생성할 꼬리질문이 없어 `nextFollowup`이 `null`로 내려간다. 꼬리질문1·2는 포함하는 문답 범위만 다를 뿐 같은 엔드포인트로 처리한다(요청의 `thread` 항목 수로 구분).

### **Endpoint**

```
POST /api/questions/{questionId}/essay/answers
```

- `questionId`는 서술형 본 질문 ID다.
- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "thread": [
    {
      "question": "TCP의 흐름 제어와 혼잡 제어의 차이를 설명하시오.",
      "answer": "흐름 제어는 수신자의 처리 속도에 맞춰 송신량을 조절하고..."
    },
    {
      "question": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
      "answer": "수신자가 광고한 윈도우 크기만큼만 데이터를 보내..."
    }
  ]
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `thread` | Array | O | 지금까지의 문답을 순서대로 담은 목록. 첫 항목은 본 질문, 이후 항목은 꼬리질문이다. **마지막 항목이 이번에 채점할 문항**이며, 앞 항목들은 꼬리질문 생성의 맥락으로 쓰인다. |
| `thread[].question` | String | O | 문항 발문. 본 질문은 조회 API로 받은 텍스트, 꼬리질문은 직전 응답의 `nextFollowup.question` 텍스트를 그대로 담는다. |
| `thread[].answer` | String | O | 해당 문항에 사용자가 작성한 답변. |

**제약**:

- `thread` 항목 수는 1 이상 3 이하다(본 질문 1 + 꼬리질문 최대 2). 범위를 벗어나면 `INVALID_INPUT`이다.
- 꼬리질문 생성 여부는 서버가 `thread` 항목 수로 판단한다. 1개면 꼬리질문1, 2개면 꼬리질문2를 생성하고, 3개면 생성하지 않는다.

### **Response Body**

```json
{
  "grading": {
    "feedback": "흐름 제어와 혼잡 제어의 목적 차이(수신자 보호 vs 네트워크 보호)를 명확히 구분하면 더 좋습니다.",
    "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만 송신자가 미확인 데이터를 보내도록 하여 수신 버퍼가 넘치지 않게 조절합니다."
  },
  "nextFollowup": {
    "question": "혼잡이 감지되면 TCP는 전송 속도를 어떻게 조절하나요?"
  }
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `grading` | Object | `thread`의 마지막 문항 답변에 대한 채점 결과. |
| `grading.feedback` | String | AI 피드백. |
| `grading.modelAnswer` | String | 해당 문항의 모범답안·해설. |
| `nextFollowup` | Object | 생성된 다음 꼬리질문. 마지막 문항(`thread` 항목 3개)이면 `null`(면접 종료). |
| `nextFollowup.question` | String | 생성된 꼬리질문 발문. |

> 정답/통과 판정 값(점수 등)은 `docs/DOMAIN.md`에서 보류 상태이므로 현재 응답에 포함하지 않는다. 확정 시 `grading`에 필드를 추가한다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락, `thread`가 비었거나 항목 수가 허용 범위를 벗어남. |
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
  ]
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `rootQuestion` | Object | O | 본질문 풀이 항목. |
| `followupQuestions` | Array | O | 꼬리질문 풀이 항목 목록. 본질문에서 이어 푼 순서대로. 비어 있을 수 있다(꼬리질문 없이 종료). |
| `*.questionId` | Long | O | 푼 문제 ID. |
| `*.choiceId` | Long | O | 사용자가 고른 선택지 ID. 해당 문제에 속한 선택지여야 한다. 정답 여부는 서버가 판정한다. |
| `*.relationQuestionId` | Long | X | 고른 선택지가 이어지는 다음 문제 ID. 마지막 항목은 `null`(체인 종료). |

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
