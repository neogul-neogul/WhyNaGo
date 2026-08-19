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

# **Auth API**

회원가입·로그인과 토큰 수명 관리를 담당한다. 관련 도메인은 `auth`다.

## **인증 방식**

로그인하면 **access token**과 **refresh token**을 발급한다. 인증이 필요한 API는 access token을 `Authorization` 헤더에 담아 호출한다.

```
Authorization: Bearer {accessToken}
```

| **토큰** | **수명** | **용도** |
| --- | --- | --- |
| access token | 30분 | API 호출 인증 |
| refresh token | 7일 | access token 재발급 |

- **refresh token은 서버에 저장된다.** 저장되어 있는지가 곧 유효한지이며, 재발급·로그아웃 시 폐기된다.
- **재발급하면 refresh token도 함께 교체된다(rotation).** 재발급에 쓴 토큰은 무효가 되므로, 클라이언트는 응답으로 받은 새 refresh token으로 반드시 갈아끼워야 한다.
- **재발급에는 10초의 유예 시간이 있다.** 여러 탭이 같은 refresh token으로 동시에 재발급을 요청해도 모두 성공하며, 각 요청은 서로 다른 토큰 쌍을 받는다. 유예 시간이 지난 뒤 같은 토큰을 다시 쓰면 `AUTH_TOKEN_INVALID`로 거절된다.
- **한 계정의 활성 세션은 하나다.** 다시 로그인하면 이전 기기의 refresh token이 폐기되고, 그 기기는 다음 재발급 시점에 로그아웃된다.
- `/api/auth/**`는 모두 인증 없이 호출한다. 재발급과 로그아웃은 access token이 이미 만료된 상태에서 호출되므로 인증을 요구하지 않는다.

> refresh token을 해싱하는 과정(회원가입·로그인·재발급·로그아웃 모두 해당)에서 서버 환경이 해시 알고리즘(SHA-256)을 지원하지 않으면 `500 AUTH_TOKEN_HASH_FAILED`를 반환한다. 정상 배포 환경에서는 발생하지 않는 방어적 오류다.

### **인증 실패 응답**

인증이 필요한 API에서 토큰이 유효하지 않으면 모두 `401 Unauthorized`로 내려가며, `code`로 원인을 구분한다.

| **상황** | **code** | **클라이언트 처리** |
| --- | --- | --- |
| `Authorization` 헤더가 없거나 비어 있음 | `AUTH_TOKEN_MISSING` | 로그인 화면으로 유도 |
| `Bearer ` 형식이 아니거나 서명이 맞지 않음 | `AUTH_TOKEN_INVALID` | 저장된 토큰을 버리고 재로그인 |
| 토큰에 권한(`role`)이 담기지 않음 | `AUTH_TOKEN_INVALID` | 저장된 토큰을 버리고 재로그인 |
| 토큰이 만료됨 | `AUTH_TOKEN_EXPIRED` | 재발급을 시도하고, 실패하면 재로그인 |

> 권한 도입 전에 발급된 토큰은 `role`이 없어 무효로 처리한다. 권한 배포 직후 한 번 전원 재로그인이 필요하다는 뜻이며, 그 이후에는 발생하지 않는다.

### **권한**

토큰에는 사용자 권한(`role`)이 함께 담긴다. `USER`(일반 사용자) 또는 `ADMIN`(관리자)이며, 로그인 응답으로도 내려준다.

- **관리자 전용 API는 `/api/admin/**` 경로를 쓴다.** 인증을 통과했더라도 `role`이 `ADMIN`이 아니면 `403 Forbidden` `AUTH_FORBIDDEN`을 반환한다.
- **권한 승격은 API로 제공하지 않는다.** 관리자 지정은 운영 DB에서 직접 수행한다. → `docs/DOMAIN.md` 권한 정책
- 재발급 시 권한은 토큰이 아니라 저장된 값을 다시 읽어 담는다. 따라서 권한 변경은 늦어도 access token 수명(30분) 안에 반영된다.

---

## **회원가입**

계정을 생성한다. 토큰은 발급하지 않으므로 가입 후 로그인을 별도로 호출해야 한다. 직무(`position`)는 현재 `BACKEND`로 고정된다.

### **Endpoint**

```
POST /api/auth/signup
```

- 성공 시 `201 Created`와 생성된 사용자 ID를 반환한다.
- 인증이 필요 없다.

### **Request Body**

```json
{
  "email": "member@example.com",
  "password": "password123",
  "nickname": "테스터"
}
```

| **필드** | **타입** | **제약** | **설명** |
| --- | --- | --- | --- |
| `email` | String | 필수, 이메일 형식 | 로그인 ID로 사용한다. |
| `password` | String | 필수, 8~12자 | 저장 시 해싱된다. |
| `nickname` | String | 필수, 4~8자 | 중복될 수 없다. |

### **Response Body**

```json
{
  "userId": 1
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `userId` | Long | 생성된 사용자 ID. |

### **에러**

| **상황** | **status** | **code** |
| --- | --- | --- |
| 형식 검증 실패 | 400 | `INVALID_INPUT` |
| 이미 사용 중인 이메일 | 409 | `USER_DUPLICATE_EMAIL` |
| 구글 계정으로 가입된 이메일 | 409 | `USER_DUPLICATE_EMAIL_SOCIAL` |
| 이미 사용 중인 닉네임 | 409 | `USER_DUPLICATE_NICKNAME` |
| 닉네임 길이 규칙 위반 | 400 | `USER_INVALID_NICKNAME` |
| 이메일 형식 규칙 위반 | 400 | `USER_INVALID_EMAIL` |

> `INVALID_INPUT`은 요청 DTO 검증(`@Email`·`@Size`)에서, `USER_INVALID_*`는 도메인 모델 검증에서 발생한다. 같은 입력이라도 앞단에서 걸리면 `INVALID_INPUT`이 먼저 내려간다.

---

## **로그인**

이메일·비밀번호를 검증하고 토큰 쌍과 사용자 정보를 발급한다. **이 시점에 해당 사용자의 기존 refresh token이 모두 폐기된다.**

### **Endpoint**

```
POST /api/auth/login
```

- 성공 시 `200 OK`를 반환한다.
- 인증이 필요 없다.

### **Request Body**

```json
{
  "email": "member@example.com",
  "password": "password123"
}
```

| **필드** | **타입** | **제약** | **설명** |
| --- | --- | --- | --- |
| `email` | String | 필수 | 가입한 이메일. |
| `password` | String | 필수 | 비밀번호. |

### **Response Body**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "email": "member@example.com",
  "nickname": "테스터",
  "position": "BACKEND",
  "role": "USER"
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `accessToken` | String | API 호출에 사용한다. 30분 후 만료된다. |
| `refreshToken` | String | 재발급에 사용한다. 7일 후 만료된다. |
| `id` | Long | 사용자 ID. |
| `email` | String | 이메일. |
| `nickname` | String | 닉네임. |
| `position` | String | 직무(`BACKEND` \| `FRONTEND` \| `FULLSTACK`). |
| `role` | String | 권한(`USER` \| `ADMIN`). `ADMIN`이면 클라이언트가 관리자 화면으로 보낸다. |

### **에러**

| **상황** | **status** | **code** |
| --- | --- | --- |
| 형식 검증 실패 | 400 | `INVALID_INPUT` |
| 등록되지 않은 이메일이거나 비밀번호가 틀림 | 401 | `AUTH_LOGIN_FAILED` |
| 구글 계정으로 가입된 이메일 | 401 | `AUTH_SOCIAL_ACCOUNT` |

> 이메일이 없는 경우와 비밀번호가 틀린 경우를 같은 코드로 응답한다. 어느 쪽인지 알려주면 가입된 이메일을 확인해줄 수 있기 때문이다.
>
> 다만 **구글로 가입한 이메일은 예외로 구분해준다.** 이미 그 계정의 존재를 아는 사용자에게 "비밀번호가 틀렸다"고만 답하면 영원히 로그인할 수 없기 때문이다.

---

## **구글 로그인**

구글 계정으로 로그인한다. **처음 로그인하는 계정이면 가입까지 함께 처리하고**, 이후에는 같은 계정으로 로그인한다. 일반 로그인과 마찬가지로 **이 시점에 해당 사용자의 기존 refresh token이 모두 폐기된다.**

프론트는 Google Identity Services(GIS) SDK로 받은 `credential`(구글이 서명한 id_token)을 그대로 전달하고, 서버가 서명·`aud`·`iss`·`exp`·`email_verified`를 검증한다. 리다이렉트나 `client_secret`은 쓰지 않는다.

가입 시 값은 다음과 같이 정해진다.

- `email` — 구글 계정의 이메일
- `nickname` — 서버가 자동 생성한다(`u` + 6자리 숫자). 마이페이지에서 변경할 수 있다.
- `position` — 회원가입과 동일하게 `BACKEND`로 고정
- `role` — 회원가입과 동일하게 `USER`로 고정
- 비밀번호는 저장하지 않는다.

**한 계정은 로그인 수단을 하나만 가진다.** 이메일이 겹쳐도 기존 계정에 구글을 연동하지 않고, 어느 쪽으로 로그인해야 하는지 안내한다.

### **Endpoint**

```
POST /api/auth/login/google
```

- 성공 시 `200 OK`를 반환한다. 신규 가입이어도 `201`이 아니라 `200`이다 — 응답의 본질이 로그인 결과(토큰)이기 때문이다.
- 인증이 필요 없다.

### **Request Body**

```json
{
  "credential": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

| **필드** | **타입** | **제약** | **설명** |
| --- | --- | --- | --- |
| `credential` | String | 필수 | GIS 콜백이 넘겨준 id_token. 클라이언트는 저장하지 않고 이 요청에만 쓴다. |

### **Response Body**

로그인과 동일하다.

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "email": "member@example.com",
  "nickname": "u483920",
  "position": "BACKEND",
  "role": "USER"
}
```

### **에러**

| **상황** | **status** | **code** |
| --- | --- | --- |
| `credential`이 비어 있음 | 400 | `INVALID_INPUT` |
| id_token 검증 실패(서명·`aud`·`iss`·만료) 또는 `email_verified`가 아님 | 401 | `AUTH_OAUTH_TOKEN_INVALID` |
| 일반 계정으로 가입된 이메일 | 409 | `AUTH_LOCAL_ACCOUNT` |

> `aud` 검증은 서버가 설정된 client id로 수행한다. 프론트와 서버의 client id가 다르면 모든 요청이 `AUTH_OAUTH_TOKEN_INVALID`로 떨어진다.

---

## **토큰 재발급**

refresh token으로 새 토큰 쌍을 발급한다. access token이 만료돼 `401 AUTH_TOKEN_EXPIRED`를 받았을 때 호출한다.

요청에 담긴 refresh token은 **폐기되고 새 것으로 교체된다(rotation).**

같은 refresh token으로 두 번 재발급하는 것은 **10초 안에서만 허용된다.** 여러 탭의 access token이 동시에 만료되면 같은 refresh token으로 재발급 요청이 여러 번 나가는데, 이때 뒤늦은 요청까지 거절하면 멀쩡한 세션이 끊기기 때문이다. 유예 시간이 지난 뒤의 재사용은 토큰 탈취로 보고 거절한다.

### **Endpoint**

```
POST /api/auth/reissue
```

- 성공 시 `200 OK`를 반환한다.
- 인증이 필요 없다. 만료된 access token은 보내지 않아도 된다.

### **Request Body**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

| **필드** | **타입** | **제약** | **설명** |
| --- | --- | --- | --- |
| `refreshToken` | String | 필수 | 로그인 또는 직전 재발급으로 받은 refresh token. |

### **Response Body**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `accessToken` | String | 새 access token. |
| `refreshToken` | String | 새 refresh token. **기존 값을 이 값으로 교체해야 한다.** |

> 응답에 사용자 정보(`nickname` 등)는 포함하지 않는다. 재발급은 토큰 갱신만 담당한다.

### **에러**

| **상황** | **status** | **code** |
| --- | --- | --- |
| `refreshToken` 누락·공백 | 400 | `INVALID_INPUT` |
| refresh token 만료 | 401 | `AUTH_TOKEN_EXPIRED` |
| 서명 불일치, 이미 폐기됨(유예 시간이 지난 재사용·로그아웃·타 기기 로그인), access token을 보냄 | 401 | `AUTH_TOKEN_INVALID` |

> 모든 실패는 재로그인이 필요하다는 뜻이다. `AUTH_TOKEN_EXPIRED`와 `AUTH_TOKEN_INVALID`를 구분해 안내 문구를 다르게 할 수는 있으나, 처리는 동일하게 로그인 화면으로 보내면 된다.

---

## **로그아웃**

서버에 저장된 refresh token을 폐기한다. 클라이언트 저장소를 비우는 것만으로는 서버의 토큰이 살아 있어 재발급이 계속 가능하므로, 반드시 호출해야 한다.

### **Endpoint**

```
POST /api/auth/logout
```

- 성공 시 `204 No Content`를 반환한다. 응답 본문이 없다.
- 인증이 필요 없다. 폐기 대상은 본문의 refresh token이 지정한다.

### **Request Body**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

| **필드** | **타입** | **제약** | **설명** |
| --- | --- | --- | --- |
| `refreshToken` | String | 필수 | 폐기할 refresh token. |

### **에러**

| **상황** | **status** | **code** |
| --- | --- | --- |
| `refreshToken` 누락·공백 | 400 | `INVALID_INPUT` |

> **멱등하다.** 이미 폐기됐거나 만료된 refresh token을 보내도 `204`를 반환한다. 토큰의 서명을 검증하지 않으므로, access token이 만료된 상태에서도 로그아웃할 수 있다.
>
> access token은 무효화되지 않는다. 서버가 보관하지 않기 때문이며, 남은 수명(최대 30분) 동안은 계속 유효하다. 클라이언트는 로그아웃 시 저장소에서 함께 지워야 한다.

---

# **Question API**

문제 조회와 서술형 풀이 진행을 담당한다. 관련 도메인은 `question`이다.

> **인증 범위**: `GET /api/questions`(목록 조회)와 `GET /api/questions/{questionId}`(단건 조회)만 **선택적 인증**이다. `Authorization` 헤더 없이 호출할 수 있고, 이때는 모든 문항의 `solved`가 `false`로 내려간다. 헤더를 보내면 해석해 푼 문제에 `solved = true`를 채운다(토큰이 만료·위조면 다른 경로와 동일하게 401). 그 외 이 도메인의 모든 하위 경로(`/api/questions/{id}/choices/{id}`, `/api/questions/{id}/essay`, `/api/questions/{id}/essay/sessions`, `/api/questions/{id}/essay/answers`)는 `Authorization` 헤더가 필요하다. 특히 서술형 채점(`/essay/answers`)은 채점 시점에 숙련도를 사용자별로 기록하므로 인증 없이는 호출할 수 없다(`WebConfig`가 `/api/questions`와 한 단계 아래 경로(`/api/questions/*`)만 인증 인터셉터에서 제외하고, 같은 경로에 선택적 인증 인터셉터를 등록한다. `/essay`·`/choices` 하위는 경로 깊이가 달라 제외 대상이 아니다).

## **문제 목록 조회**

문제은행 화면의 목록을 조회한다. 사용자가 바로 시작할 수 있는 **진입 문제**만 반환한다. 다른 문제의 선택지에서 이어지는 객관식 꼬리질문은 목록에서 제외된다. 서술형 꼬리질문은 세션마다 AI가 생성해 재사용 `Question`이 없으므로(→ `docs/DOMAIN.md` 서술형 꼬리질문 생성 정책), 서술형 문제는 모두 진입 문제로 조회된다.

객관식과 서술형이 한 목록에 함께 내려간다. 유형을 구분해야 하면 `type` 필터를 쓰거나 응답의 `type` 필드로 분기한다.

문항마다 이미 푼 문제인지를 `solved`로 함께 내려준다. 객관식 풀이 이력(`SolvedMultipleChoice`)과 서술형 풀이 이력(`EssaySolved`)을 함께 보며, 정답/오답은 구분하지 않는다. 완료된 풀이 세션만 저장되므로(→ 세션 저장) `solved = true`는 "끝까지 풀어 저장한 문제"를 뜻한다. 서술형 꼬리질문은 세션마다 AI가 생성해 참조할 `Question`이 없으므로(→ `docs/DOMAIN.md` 서술형 꼬리질문 생성 정책) 이력에서 제외된다.

### **Endpoint**

```
GET /api/questions
```

- 성공 시 `200 OK`와 페이지 응답을 반환한다.
- 정렬은 문제 ID 내림차순(최신순) 고정이다. ID가 유일해 페이지 간 순서가 흔들리지 않는다.

### **Query Parameters**

모두 선택이며, 생략하면 해당 조건을 적용하지 않는다. 여러 개를 함께 주면 모두 만족하는 문제만 조회된다.

| **파라미터** | **타입** | **설명** |
| --- | --- | --- |
| `type` | String | 문제 유형. `MULTIPLE_CHOICE` \| `ESSAY` |
| `difficulty` | String | 난이도. `LOW` \| `MEDIUM` \| `HIGH` |
| `category` | String | 카테고리. `DB` \| `NETWORK` \| `ALGORITHM` \| `DATA_STRUCTURE` \| `OS` \| `DESIGN_PATTERN` \| `LANGUAGE` \| `GENERAL_CS` |
| `q` | String | 제목·지문 키워드. 부분 일치이며 대소문자를 구분하지 않는다. |
| `page` | int | 0부터 시작하는 페이지 번호. 생략하거나 음수면 `0`으로 보정한다. |
| `size` | int | 한 페이지 문항 수. 생략하거나 1 미만이면 `20`, 100을 넘으면 `100`으로 보정한다. |

### **Response Body**

문항 배열은 `content`에 담기고, 나머지 필드는 페이지 정보다.

```json
{
  "content": [
    {
      "id": 101,
      "title": "TCP 흐름 제어 vs 혼잡 제어",
      "content": "TCP의 흐름 제어(Flow Control)와 혼잡 제어(Congestion Control)의 차이를 설명하시오.",
      "type": "ESSAY",
      "difficulty": "MEDIUM",
      "category": "NETWORK",
      "explanation": "흐름 제어는 수신자의 처리 속도에 맞춰 송신량을 조절하는 것으로...",
      "choices": [],
      "tags": ["흐름 제어", "혼잡 제어"],
      "solved": true
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
      "tags": ["NETWORK"],
      "solved": false
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 137,
  "totalPages": 7,
  "last": false
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `content` | Array | 이 페이지의 문항 목록. |
| `page` | int | 0부터 시작하는 현재 페이지 번호. 요청값을 보정한 결과다. |
| `size` | int | 한 페이지 문항 수. 요청값을 보정한 결과다. |
| `totalElements` | long | 조건에 맞는 전체 문항 수. 화면의 "N개 문제" 표시에 사용한다. |
| `totalPages` | int | 전체 페이지 수. `totalElements`가 0이면 0이다. |
| `last` | boolean | 마지막 페이지인지 여부. |

`content` 원소의 필드는 다음과 같다.

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
| `solved` | boolean | 요청한 사용자가 이미 푼 문제인지 여부. 비로그인 요청이면 항상 `false`. |

> 정답 여부(`isCorrect`)는 목록 응답에 포함하지 않는다. 객관식 채점은 보기 선택 결과 조회 API로만 확인한다. `solved`도 정답/오답과 무관하며, 풀어서 저장했는지만 나타낸다.

### **에러**

조건에 맞는 문제가 없으면 에러가 아니라 빈 `content`와 `200 OK`를 반환한다(`totalElements` 0, `totalPages` 0, `last` true). 범위를 넘는 `page`를 요청해도 마찬가지로 빈 `content`를 반환하며, 이때 `totalElements`는 조건에 맞는 전체 문항 수 그대로다.

> **알려진 문제**: 필터에 enum에 없는 값을 보내면(예: `?type=FOO`) 요청 형식 오류이므로 `400 INVALID_INPUT`이어야 하는데, 현재는 `500 SERVER_ERROR`로 응답한다. 요청 바인딩 예외(`MethodArgumentTypeMismatchException`)가 `GlobalExceptionHandler`에 등록되지 않아서다. 별도 이슈로 처리한다.

---

## **문제 단건 조회**

문제 하나를 조회한다. 목록이 페이지 단위라 문제 상세(풀이) 화면이 목록에서 문항을 찾아낼 수 없으므로, 화면 진입 시 이 API로 문항을 가져온다. 응답은 목록 응답의 `content` 원소와 완전히 같은 형식이며, 객관식·서술형을 가리지 않는다(서술형은 `choices`가 빈 배열).

서술형 발문만 다시 보여주는 용도라면 `explanation` 없이 내려주는 **서술형 문제 조회**(`/api/questions/{questionId}/essay`)를 쓴다.

### **Endpoint**

```
GET /api/questions/{questionId}
```

- 성공 시 `200 OK`를 반환한다.
- 목록 조회와 동일하게 **선택적 인증**이다. 비로그인 요청이면 `solved`가 항상 `false`다.

### **Response Body**

```json
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
  "tags": ["NETWORK"],
  "solved": false
}
```

필드 설명은 목록 조회의 `content` 원소와 같다.

### **에러**

| **상태** | **code** | **설명** |
| --- | --- | --- |
| 404 | `QUESTION_NOT_FOUND` | 해당 ID의 문제가 없다. |

---

## **선택지 채점 결과 조회**

객관식 문항에서 사용자가 고른 선택지를 채점한다. 정답 여부·해설과, 고른 선택지에 연결된 다음 꼬리질문(있으면 문항 전체 정보)을 함께 반환한다. 이 API는 조회일 뿐 아무것도 저장하지 않는다 — 풀이 결과 저장은 SolvedSession API를 별도로 호출해야 한다.

### **Endpoint**

```
GET /api/questions/{questionId}/choices/{choiceId}
```

- `questionId`는 채점할 문항 ID, `choiceId`는 사용자가 고른 선택지 ID다.
- 성공 시 `200 OK`를 반환한다.

### **Response Body**

```json
{
  "correct": false,
  "correctChoiceId": 2,
  "explanation": "TCP는 3-way handshake로 연결을 수립하고...",
  "choiceExplanation": "TCP를 비연결형으로 설명해 틀렸습니다...",
  "nextQuestion": {
    "id": 5,
    "title": "TCP 흐름 제어",
    "content": "...",
    "type": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "category": "NETWORK",
    "explanation": "...",
    "choices": [],
    "tags": [],
    "solved": false
  }
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `correct` | boolean | 고른 선택지(`choiceId`)의 정답 여부. |
| `correctChoiceId` | Long | 이 문항의 정답 선택지 ID. |
| `explanation` | String | 문제 전체(정답) 해설(`Question.explanation`). |
| `choiceExplanation` | String \| null | 고른 선택지의 오답 해설. 정답을 골랐으면 `null`. |
| `nextQuestion` | Object \| null | 고른 선택지의 `relatedQuestionId`가 가리키는 다음 문항(문제 목록 조회 응답과 동일한 형태). 없으면 `null`(그 지점에서 세션 종료). |

> `nextQuestion.solved`는 목록 조회와 형태를 맞추느라 딸려 나올 뿐 **항상 `false`**다. 완료 표시는 문제 목록 조회에서만 의미가 있다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `QUESTION_NOT_FOUND` | `questionId` 문제가 존재하지 않음. |
| 404 | `CHOICE_NOT_FOUND` | `choiceId` 선택지가 존재하지 않음. |
| 400 | `CHOICE_NOT_IN_QUESTION` | `choiceId` 선택지가 `questionId` 문제에 속하지 않음. |

---

## **서술형 문제 단건 조회**

서술형 문제 하나를 상세 조회한다. 서술형 세션을 시작하기 전, 발문을 다시 보여줄 때 사용한다. 목록 조회 응답과 달리 `explanation`·`choices`는 내려주지 않는다 — 서술형은 선택지가 없고, 정답 해설은 매 턴 AI 채점 결과(`grading.modelAnswer`)로 대체되기 때문이다.

### **Endpoint**

```
GET /api/questions/{questionId}/essay
```

- 성공 시 `200 OK`를 반환한다.

### **Response Body**

```json
{
  "id": 101,
  "title": "TCP 흐름 제어 vs 혼잡 제어",
  "content": "TCP의 흐름 제어(Flow Control)와 혼잡 제어(Congestion Control)의 차이를 설명하시오.",
  "type": "ESSAY",
  "difficulty": "MEDIUM",
  "category": "NETWORK",
  "tags": ["흐름 제어", "혼잡 제어"]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제 ID. |
| `title` | String | 문제 제목. |
| `content` | String | 문제 발문. |
| `type` | String | 항상 `ESSAY`. |
| `difficulty` | String | 난이도. |
| `category` | String | 카테고리. |
| `tags` | Array | 문제 태그 이름 목록. 없으면 빈 배열. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `QUESTION_NOT_FOUND` | `questionId` 문제가 존재하지 않음. |
| 400 | `QUESTION_NOT_ESSAY` | `questionId` 문제가 서술형(`ESSAY`)이 아님. |

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
| `elapsedSeconds` | int | X | 이번 문항을 푸는 데 걸린 시간(초). 채점에 반영된다(→ `docs/DOMAIN.md` 서술형 소요시간 반영 정책). 측정하지 못했으면 보내지 않아도 되고, 그때는 시간을 채점에 쓰지 않는다. |

**제약**:

- `conversationId`·`question`·`answer`는 공백일 수 없다. 위반 시 `INVALID_INPUT`이다.
- `elapsedSeconds`는 음수일 수 없다. 클라이언트가 보고하는 값이라 서버가 상한 600초로 자르고, 0 이하는 미측정으로 다룬다.
- 꼬리질문 생성 여부와 진행 단계는 서버가 대화 이력의 턴 수로 판단한다(1·2턴째는 꼬리질문 생성, 3턴째는 생성하지 않고 대화 정리).

### **Response Body**

```json
{
  "grading": {
    "feedback": "흐름 제어와 혼잡 제어의 목적 차이(수신자 보호 vs 네트워크 보호)를 명확히 구분하면 더 좋습니다.",
    "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만 송신자가 미확인 데이터를 보내도록 하여 수신 버퍼가 넘치지 않게 조절합니다.",
    "score": 6,
    "isCorrect": false,
    "mastery": "UNSTABLE",
    "masteryReason": "윈도우 크기로 송신량을 조절한다는 결론은 맞지만, 그것이 수신자 보호를 위한 것이라는 근거를 제시하지 못했습니다.",
    "rubricCriteria": [
      {
        "point": "흐름 제어는 수신자가 광고한 윈도우 크기만큼만 보내도록 송신량을 조절한다.",
        "weight": 6,
        "met": true,
        "reason": "윈도우 크기만큼만 보낸다고 정확히 서술했습니다."
      },
      {
        "point": "그 목적은 수신 버퍼가 넘치지 않게 수신자를 보호하는 것이다.",
        "weight": 4,
        "met": false,
        "reason": "조절한다는 사실만 말하고 수신자 보호라는 목적을 짚지 않았습니다."
      }
    ],
    "solvingTime": {
      "elapsedSeconds": 300,
      "averageSeconds": 150,
      "pace": "SLOW",
      "scoreAdjustment": 0
    }
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
| `grading.score` | int | 0~10 점수. 문항에 루브릭이 있으면 **충족한 루브릭 항목의 배점 합**이고, 없으면 LLM이 매긴 점수다. 거기에 **소요시간 가감**(`grading.solvingTime.scoreAdjustment`)을 얹은 최종값이며 0~10으로 잘린다(→ `docs/DOMAIN.md` 서술형 정답 판정 정책). |
| `grading.isCorrect` | boolean | 통과 여부. `grading.score`를 서버가 임계값(7 이상 통과)으로 환산한 값(→ `docs/DOMAIN.md` 서술형 정답 판정 정책). |
| `grading.mastery` | String \| null | 이 답변이 드러낸 이해 수준. `MASTERED` \| `SOLID` \| `UNSTABLE` \| `GUESSED` \| `WEAK` \| `NOT_LEARNED`. AI가 판정하지 못하면 `null`이며, 이때 숙련도는 기록되지 않는다. |
| `grading.masteryReason` | String \| null | 그 판정의 근거. 답변에서 근거가 된 부분을 짚은 문장이다. `mastery`가 `null`이면 함께 `null`이다. |
| `grading.rubricCriteria` | Array | 루브릭 항목별 채점 결과. 어떤 채점 기준을 맞추고 어떤 기준을 놓쳤는지 보여준다. **`null`로 내려가지 않는다** — 루브릭이 없는 문항이거나 꼬리질문 턴이면 빈 배열이다. |
| `grading.rubricCriteria[].point` | String | 채점 기준 항목. 답변이 담아야 할 내용이다. |
| `grading.rubricCriteria[].weight` | int | 그 항목의 배점. 한 문항의 배점 합은 항상 10이다. |
| `grading.rubricCriteria[].met` | boolean | 답변이 그 항목을 담았는지. `true`인 항목의 `weight` 합이 `grading.score`다. |
| `grading.rubricCriteria[].reason` | String | 그 판정의 근거. `met`이 `false`면 무엇이 빠졌는지를 짚는다. |
| `grading.solvingTime` | Object \| null | 소요시간이 점수에 어떻게 반영됐는지. 요청에 `elapsedSeconds`를 보내지 않았거나 0 이하였으면 `null`이며, 그때는 시간이 점수에 반영되지 않았다. |
| `grading.solvingTime.elapsedSeconds` | int | 채점에 실제로 쓰인 소요시간(초). 요청값을 상한 600초로 자른 값이다. |
| `grading.solvingTime.averageSeconds` | int | 비교 기준이 된 그 문항의 평균 소요시간(초). 통계가 없거나 표본이 5건 미만이면 기본값 180이다. |
| `grading.solvingTime.pace` | String | 평균 대비 속도. `FAST` \| `NORMAL` \| `SLOW`. |
| `grading.solvingTime.scoreAdjustment` | int | 점수에 더해진 값. `FAST`면 `+1`, `SLOW`면 `-1`, `NORMAL`이면 `0`이다. **`rubricCriteria`의 배점 합과 `score`가 다르면 이 값 때문이다.** |

> 채점과 동시에 서버가 그 문항의 태그·카테고리에 숙련도를 기록한다(→ `docs/RECOMMENDATION.md` 숙련도 기록·조회). 그래서 이 API는 **인증이 필요**하다. 기록된 숙련도는 `GET /api/mastery`로 조회한다.
| `nextFollowup` | Object | 생성된 다음 꼬리질문. 마지막 문항(3턴째)이면 `null`(면접 종료). |
| `nextFollowup.question` | String | 생성된 꼬리질문 발문. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락 또는 공백. |
| 404 | `QUESTION_NOT_FOUND` | `questionId` 문제가 존재하지 않음. |
| 400 | `QUESTION_NOT_ESSAY` | `questionId` 문제가 서술형(`ESSAY`)이 아님. |
| 503 | `ESSAY_AI_UNAVAILABLE` | AI 채점·꼬리질문 생성 호출이 실패함(LLM 장애 등). 즉시 재시도 가능. |
| 429 | `ESSAY_AI_QUOTA_EXCEEDED` | AI 분당 요청 한도를 초과함. 잠시(약 1분) 기다린 뒤 재시도해야 한다. |
| 429 | `ESSAY_AI_DAILY_QUOTA_EXCEEDED` | AI 일일 요청 한도를 초과함. 한도가 초기화되기 전에는 재시도해도 실패한다. |

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

> 저장과 동시에 서버가 문항마다 숙련도를 판정해 그 문항의 태그·카테고리에 기록한다(출처 `RULE_CHOICE`, 꼬리질문 포함). 판정은 클라이언트가 보낸 정답 여부가 아니라 서버가 다시 채점한 결과와 `elapsedSeconds`로 계산한다(→ `docs/RECOMMENDATION.md` 숙련도 판정 정책). 기록된 숙련도는 `GET /api/mastery`로 조회한다.

### **Request Body**

```json
{
  "rootQuestion": {
    "questionId": 1,
    "choiceId": 3,
    "relationQuestionId": 5,
    "elapsedSeconds": 78
  },
  "followupQuestions": [
    {
      "questionId": 5,
      "choiceId": 12,
      "relationQuestionId": 8,
      "elapsedSeconds": 45
    },
    {
      "questionId": 8,
      "choiceId": 20,
      "relationQuestionId": null,
      "elapsedSeconds": 63
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
| `*.elapsedSeconds` | int | X | 그 문항을 푸는 데 걸린 시간(초). **문항이 화면에 표시된 시점부터 "정답 확인"을 누른 시점까지**이며, 채점 후 해설을 읽은 시간은 포함하지 않는다. 관리자 문제 통계의 평균 소요 시간 집계에 쓴다(→ `docs/DOMAIN.md` 문제 통계 집계 정책). |
| `startedAt` | LocalDateTime | O | 본질문을 처음 받은 시각(클라이언트 기준 세션 시작 시각). 학습 기록의 소요시간(`solvedAt - startedAt`) 계산에 사용한다(→ `docs/DOMAIN.md` 학습 기록 집계 정책). |

`elapsedSeconds`는 선택 필드다. 보내지 않으면 소요 시간 없이 저장되고 통계 집계에서 빠질 뿐, 세션 저장은 정상 처리된다.

- **음수면 `400 INVALID_INPUT`이다.** 측정 로직의 버그이지 사용자 입력이 아니므로 거절한다.
- **600초(10분)를 넘으면 세션은 저장되고 그 문항의 소요 시간은 600초로 잘라 저장한다.** 문제를 띄워둔 채 자리를 비운 시간을 "푸는 데 걸린 시간"으로 볼 수 없기 때문이며, 이것 때문에 실제로 푼 기록까지 잃게 하지는 않는다.
- **0초는 저장하지 않는다(`null`).** 문제를 읽고 답을 고르는 데 0초가 걸릴 수는 없으므로 측정 실패로 본다.

정규화 규칙은 `common.domain.ElapsedSecondsPolicy` 한 곳에 있고 서술형 풀이(`essay_solved`)와 서술형 채점의 소요시간에도 같은 규칙이 적용된다.

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

> 상세 조회는 오답노트가 참조하는 `SolvedSession`을 함께 조회한다. 오답노트는 항상 유효한 세션을 참조하므로 실무에서는 발생하지 않지만, 참조된 세션이 없으면 `404 SOLVED_SESSION_NOT_FOUND`를 반환하는 방어적 코드가 존재한다.

## **오답노트 목록 조회**

### **Endpoint**

```
GET /api/wrong-notes
```

| **Query Param** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `bookmarked` | boolean | X | `true`면 북마크한 오답노트만 반환. 생략하면 전체 반환. |

정렬은 오답노트 ID(`WrongNote.id`) 내림차순(생성순의 역순)으로 고정한다. 오답노트는 세션 완료 시 자동 생성되므로 실질적으로 최신순과 같다.

### **Response Body**

```json
[
  {
    "id": 12,
    "questionId": 7,
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
| `questionId` | Long | 본 질문의 ID(`Question.id`). 재풀이 진입(`/solve/{questionId}`)에 사용. |
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

# **Interview API**

하루에 한 번 진행하는 **1일 1면접**을 담당한다. 관련 도메인은 `interview`다.

풀이 흐름 자체는 서술형과 같다 — 본 질문 + AI 꼬리질문 2개(총 3문항), 답변마다 LLM 채점. 차이는 다음과 같다(→ `docs/DOMAIN.md` 1일 1면접 정책).

- **하루 1회**다. 오늘 이미 시작했으면 다시 시작할 수 없다. 하루 경계는 KST(Asia/Seoul) 자정이다.
- **날짜를 요청으로 받지 않는다.** 서버가 KST 기준으로 판단한다.
- **오늘의 질문은 전역 고정**이다. 그날 모든 사용자가 같은 질문을 받으며, 시작 API로만 공개된다(상태 조회에는 노출하지 않는다).
- **`conversationId`를 서버가 소유한다.** 서술형과 달리 클라이언트가 대화 식별자를 보관하지 않고 `interviewId`만 보낸다.
- **제한 시간(180초)은 서버가 강제하지 않는다.** 클라이언트가 표시·강제한다.

모든 엔드포인트는 인증이 필요하며 **본인의 면접만** 다룬다. 남의 `interviewId`를 조회·조작하면 존재 여부를 노출하지 않기 위해 `INTERVIEW_NOT_FOUND`(404)로 응답한다.

## **오늘의 면접 상태 조회**

면접 안내 화면 진입 시 호출한다. 오늘 면접을 시작할 수 있는지, 진행 중인지, 이미 마쳤는지를 반환한다. **오늘의 질문은 노출하지 않는다.**

### **Endpoint**

```
GET /api/interviews/today
```

- 요청 본문·쿼리 파라미터가 없다. 사용자와 날짜 모두 서버가 해석한다.

### **Response Body**

```json
{
  "status": "AVAILABLE",
  "interviewId": null
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `status` | String | `AVAILABLE`(오늘 아직 안 봄) \| `IN_PROGRESS`(시작했으나 미완료) \| `COMPLETED`(완료). |
| `interviewId` | Long | `AVAILABLE`이면 `null`, 그 외엔 오늘 면접 ID. |

`AVAILABLE`은 `InterviewStatus`에 없는 값이다. "오늘 면접 행이 존재하지 않음"을 API 레벨에서 표현한 것이다.

### **에러**

없음.

---

## **면접 시작**

오늘의 면접을 시작하고 그날의 질문을 반환한다. 이 시점에 `DailyInterview` 행이 `IN_PROGRESS`로 생성되며 **오늘 자리가 소진된다**. 그날 첫 요청이면 서버가 서술형 문제 중 하나를 뽑아 오늘의 질문으로 고정한다.

### **Endpoint**

```
POST /api/interviews
```

- 요청 본문이 없다. 질문은 서버가 정하므로 클라이언트가 고를 수 없다.
- 성공 시 `201 Created`를 반환한다.

### **Response Body**

```json
{
  "interviewId": 7,
  "question": {
    "id": 17,
    "title": "TCP 흐름 제어",
    "content": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
    "category": "NETWORK",
    "difficulty": "MEDIUM"
  },
  "totalQuestionCount": 3,
  "timeLimitSeconds": 180,
  "startedAt": "2026-08-07T09:14:02"
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `interviewId` | Long | 생성된 면접 ID. 이후 모든 요청에 사용한다. |
| `question` | Object | 오늘의 본 질문. **그날 모든 사용자에게 동일하다.** |
| `question.id` | Long | `Question.id`. |
| `question.title` | String | 문제 제목. |
| `question.content` | String | 발문. 첫 답변 요청의 `question`에 그대로 담는다. |
| `question.category` | String | `Category`. |
| `question.difficulty` | String | `Difficulty`. |
| `totalQuestionCount` | int | 총 문항 수. 현재 `3` 고정(본 질문 + 꼬리질문 2개). |
| `timeLimitSeconds` | int | 제한 시간(초). 현재 `180` 고정. **서버가 강제하지 않는다.** |
| `startedAt` | LocalDateTime | 서버가 기록한 시작 시각. 소요시간의 기준이다. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 409 | `INTERVIEW_ALREADY_STARTED_TODAY` | 오늘 이미 면접을 시작함(진행 중·완료 무관). |
| 404 | `INTERVIEW_QUESTION_NOT_AVAILABLE` | 서술형(`ESSAY`) 문제가 하나도 없어 오늘의 질문을 정할 수 없음. |

---

## **면접 답변 채점·꼬리질문 생성**

면접 한 턴을 처리한다. 동작은 서술형 채점 API와 같고 응답 형태도 동일하다(`{grading, nextFollowup}`) — 프런트가 같은 파싱을 쓸 수 있다. **차이는 `conversationId`를 보내지 않는다는 것**이다. 서버가 `interviewId`로 대화 식별자를 찾는다.

이 API는 **아무것도 저장하지 않는다.** 클라이언트가 3턴 결과를 모아 완료 API로 올린다.

### **Endpoint**

```
POST /api/interviews/{interviewId}/answers
```

- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "question": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
  "answer": "수신자가 광고한 윈도우 크기만큼만 데이터를 보내..."
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `question` | String | O | 이번에 채점할 문항 발문. 1턴은 시작 API의 `question.content`, 이후는 직전 응답의 `nextFollowup.question`. 공백 불가. |
| `answer` | String | O | 사용자가 작성한 답변. **공백을 허용한다**(제한 시간 안에 못 쓴 경우를 인정) — `null`만 거부한다. |
| `elapsedSeconds` | int | X | 이번 문항에 답하는 데 걸린 시간(초). 채점에 반영된다(→ `docs/DOMAIN.md` 서술형 소요시간 반영 정책). 보내지 않으면 시간을 채점에 쓰지 않는다. 음수는 `INVALID_INPUT`이다. |

### **Response Body**

```json
{
  "grading": {
    "feedback": "흐름 제어와 혼잡 제어의 목적 차이를 명확히 구분하면 더 좋습니다.",
    "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만 송신자가 미확인 데이터를 보내도록 조절합니다.",
    "score": 10,
    "isCorrect": true,
    "mastery": "SOLID",
    "masteryReason": "윈도우 크기로 송신량을 조절한다는 점과 그 목적을 함께 설명했습니다.",
    "rubricCriteria": [
      {
        "point": "흐름 제어는 수신자가 광고한 윈도우 크기만큼만 보내도록 송신량을 조절한다.",
        "weight": 6,
        "met": true,
        "reason": "윈도우 크기만큼만 보낸다고 정확히 서술했습니다."
      },
      {
        "point": "그 목적은 수신 버퍼가 넘치지 않게 수신자를 보호하는 것이다.",
        "weight": 4,
        "met": true,
        "reason": "수신 버퍼 보호라는 목적까지 짚었습니다."
      }
    ],
    "solvingTime": {
      "elapsedSeconds": 80,
      "averageSeconds": 150,
      "pace": "FAST",
      "scoreAdjustment": 1
    }
  },
  "nextFollowup": {
    "question": "혼잡이 감지되면 TCP는 전송 속도를 어떻게 조절하나요?"
  }
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `grading.feedback` | String | AI 피드백. |
| `grading.modelAnswer` | String | 모범답안·해설. |
| `grading.score` | int | 0~10 점수. 문항에 루브릭이 있으면 충족한 루브릭 항목의 배점 합이고, 없으면 LLM이 매긴 점수다. 거기에 소요시간 가감을 얹은 최종값이다. |
| `grading.isCorrect` | boolean | 통과 여부(→ `docs/DOMAIN.md` 서술형 정답 판정 정책). |
| `grading.mastery` | String \| null | 이 답변이 드러낸 이해 수준. AI가 판정하지 못하면 `null`. 서술형 채점과 같은 6분류다. |
| `grading.masteryReason` | String \| null | 그 판정의 근거. |
| `grading.rubricCriteria` | Array | 루브릭 항목별 채점 결과(`point`·`weight`·`met`·`reason`). 서술형 채점 응답과 형식이 같다. 루브릭이 없는 문항이거나 꼬리질문 턴이면 **빈 배열**이며 `null`로 내려가지 않는다. |
| `grading.solvingTime` | Object \| null | 소요시간이 점수에 어떻게 반영됐는지(`elapsedSeconds`·`averageSeconds`·`pace`·`scoreAdjustment`). 서술형 채점 응답과 형식이 같다. `elapsedSeconds`를 보내지 않았으면 `null`이다. |
| `nextFollowup` | Object | 다음 꼬리질문. 마지막 문항(3턴째)이면 `null`. |
| `nextFollowup.question` | String | 생성된 꼬리질문 발문. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | `question`이 공백이거나 `answer`가 누락됨. |
| 404 | `INTERVIEW_NOT_FOUND` | 면접이 없거나 본인 것이 아님. |
| 400 | `INTERVIEW_NOT_IN_PROGRESS` | 이미 완료된 면접임. |
| 503 | `ESSAY_AI_UNAVAILABLE` | AI 호출 실패(LLM 장애 등). 즉시 재시도 가능. |
| 429 | `ESSAY_AI_QUOTA_EXCEEDED` | AI 분당 요청 한도 초과. 잠시 후 재시도. |
| 429 | `ESSAY_AI_DAILY_QUOTA_EXCEEDED` | AI 일일 요청 한도 초과. 한도 초기화 전에는 재시도해도 실패한다. |

한 문항도 채점받지 못한 상태에서 AI 오류가 계속되면 **면접 취소 API**로 오늘 자리를 돌려받을 수 있다.

---

## **면접 완료**

3문항 문답을 학습 기록으로 저장하고 면접을 종료한다. `SolvedSession`(`type = ESSAY`) 1건 + `EssaySolved` 3행이 생성되며, 오답이 있으면 오답노트도 자동 생성된다. 동시에 `DailyInterview`가 `COMPLETED`로 전이된다.

서술형 세션 저장 API(`POST /api/solved-sessions/essay`)와 본문 모양이 거의 같지만 **`startedAt`과 본 질문 ID를 받지 않는다** — 둘 다 서버가 소유한 값이다.

### **Endpoint**

```
POST /api/interviews/{interviewId}/complete
```

- 성공 시 `201 Created`를 반환한다.

### **Request Body**

```json
{
  "rootQuestion": {
    "questionText": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
    "userAnswer": "수신자가 광고한 윈도우 크기만큼만...",
    "feedback": "목적 차이를 구분하면 더 좋습니다.",
    "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만...",
    "isCorrect": true
  },
  "followupQuestions": [
    {
      "questionText": "혼잡이 감지되면 TCP는 전송 속도를 어떻게 조절하나요?",
      "userAnswer": "",
      "feedback": "답변이 없어 평가할 수 없습니다.",
      "modelAnswer": "혼잡 윈도우를 줄이고 느린 시작으로 되돌아갑니다.",
      "isCorrect": false
    },
    {
      "questionText": "빠른 재전송은 언제 발생하나요?",
      "userAnswer": "중복 ACK 3번이면...",
      "feedback": "정확합니다.",
      "modelAnswer": "중복 ACK 3회 수신 시 타임아웃을 기다리지 않고 재전송합니다.",
      "isCorrect": true
    }
  ],
  "focusLossCount": 1
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `rootQuestion` | Object | O | 본 질문 문답 스냅샷. `sequence = 1`, `type = MAIN`으로 저장된다. 본 질문 ID는 서버가 채운다. |
| `followupQuestions` | Array | O | 꼬리질문 문답 스냅샷. **정확히 2개**여야 한다. `type = FOLLOWUP`으로 저장되며 `questionId`는 `null`이다. |
| `focusLossCount` | int | O | 면접 중 화면 이탈 횟수. `0` 이상. 서버는 검증 없이 저장한다. |

각 문답 스냅샷의 필드는 다음과 같다.

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `questionText` | String | O | 발문. 공백 불가. |
| `userAnswer` | String | O | 사용자 답변. **공백 허용**, `null` 불가. |
| `feedback` | String | O | 채점 API가 반환한 피드백. 공백 불가. |
| `modelAnswer` | String | O | 채점 API가 반환한 모범답안. 공백 불가. |
| `isCorrect` | boolean | O | 채점 API가 반환한 통과 여부. 세션 `correctCount` 집계에 쓰인다. |

### **Response Body**

```json
{
  "interviewId": 7,
  "solvedSessionId": 128
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `interviewId` | Long | 완료된 면접 ID. |
| `solvedSessionId` | Long | 생성된 풀이 세션 ID. 학습 기록·오답노트에서 이 세션으로 조회된다. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락·공백, 또는 `followupQuestions`가 2개가 아님. |
| 404 | `INTERVIEW_NOT_FOUND` | 면접이 없거나 본인 것이 아님. |
| 400 | `INTERVIEW_NOT_IN_PROGRESS` | 이미 완료된 면접임. |

---

## **면접 취소**

**서버 사유로 한 문항도 채점받지 못했을 때** 면접을 취소해 오늘 자리를 돌려준다. AI 장애·쿼터 초과는 사용자 잘못이 아닌데 하루를 통째로 날리는 것이 과하기 때문이다.

취소는 `DailyInterview` **행 삭제**로 처리한다(취소 상태를 두지 않는다). 삭제 후 같은 날 다시 시작할 수 있으며, 오늘의 질문은 이미 고정돼 있으므로 같은 질문을 받는다.

### **Endpoint**

```
DELETE /api/interviews/{interviewId}
```

- 성공 시 `204 No Content`를 반환한다.

### **취소 가능 조건**

`status = IN_PROGRESS` **이고** 채점 성공 턴 수가 `0`일 때만 가능하다. 턴 수는 클라이언트 주장이 아니라 **서버의 대화 이력**에서 읽으므로, 채점을 받고 나서 마음에 안 들어 취소·재시작하는 우회가 불가능하다.

> ⚠️ 대화 이력은 인메모리라 서버가 재시작되면 턴 수가 `0`으로 리셋된다. 그 경우 이미 채점받은 면접도 취소 가능해진다(수용된 트레이드오프).

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `INTERVIEW_NOT_FOUND` | 면접이 없거나 본인 것이 아님. |
| 400 | `INTERVIEW_NOT_CANCELABLE` | 이미 완료됐거나, 한 문항 이상 채점받음. |

---

## **면접 결과 조회**

완료된 면접의 결과를 조회한다. 문답 내역은 `SolvedSession`·`EssaySolved`에서, 이탈 횟수·소요시간 같은 면접 고유 값은 `DailyInterview`에서 조립한다.

### **Endpoint**

```
GET /api/interviews/{interviewId}
```

- **완료된 면접만** 조회할 수 있다. 진행 중인 면접은 `INTERVIEW_NOT_COMPLETED`다.

### **Response Body**

```json
{
  "interviewId": 7,
  "interviewDate": "2026-08-07",
  "status": "COMPLETED",
  "category": "NETWORK",
  "totalCount": 3,
  "correctCount": 2,
  "focusLossCount": 1,
  "startedAt": "2026-08-07T09:14:02",
  "completedAt": "2026-08-07T09:16:41",
  "durationSeconds": 159,
  "items": [
    {
      "sequence": 1,
      "type": "MAIN",
      "questionText": "슬라이딩 윈도우가 흐름 제어에서 어떻게 동작하나요?",
      "userAnswer": "수신자가 광고한 윈도우 크기만큼만...",
      "feedback": "목적 차이를 구분하면 더 좋습니다.",
      "modelAnswer": "수신자가 광고한 윈도우 크기(rwnd)만큼만...",
      "isCorrect": true
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `interviewId` | Long | 면접 ID. |
| `interviewDate` | LocalDate | 면접 날짜(KST). |
| `status` | String | 항상 `COMPLETED`. |
| `category` | String | 오늘의 질문 카테고리(`Category`). |
| `totalCount` | int | 전체 문항 수. |
| `correctCount` | int | 통과 문항 수. |
| `focusLossCount` | int | 화면 이탈 횟수. |
| `startedAt` | LocalDateTime | 시작 시각. |
| `completedAt` | LocalDateTime | 완료 시각. |
| `durationSeconds` | long | 소요 시간(초). `completedAt - startedAt`. 제한 시간을 초과한 값일 수 있다(서버 미강제). |
| `items` | Array | 문항별 결과. `sequence` 오름차순. |
| `items[].sequence` | int | 세션 내 순서. 본 질문이 1. |
| `items[].type` | String | `MAIN`(본 질문) \| `FOLLOWUP`(꼬리질문). |
| `items[].questionText` | String | 발문 스냅샷. |
| `items[].userAnswer` | String | 사용자 답변. 미작성이면 빈 문자열. |
| `items[].feedback` | String | AI 피드백. |
| `items[].modelAnswer` | String | 모범답안. |
| `items[].isCorrect` | boolean | 통과 여부. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `INTERVIEW_NOT_FOUND` | 면접이 없거나 본인 것이 아님. |
| 400 | `INTERVIEW_NOT_COMPLETED` | 아직 완료되지 않은 면접임. |

---

## **면접 기록 목록 조회**

지금까지 완료한 면접 이력을 전부 조회한다. 오답노트와 달리 정답/오답으로 필터링하지 않는다 — 3문항을 모두 맞혀 통과한 면접도 포함해 완료된 면접을 전부 반환한다. `status = IN_PROGRESS`인(아직 완료하지 못한) 면접은 제외한다.

### **Endpoint**

```
GET /api/interviews
```

정렬은 `interviewDate` 내림차순(최신순)으로 고정한다. `(userId, interviewDate)`가 유니크이므로 이는 `completedAt` 내림차순과 동일하다.

### **Response Body**

```json
[
  {
    "interviewId": 7,
    "interviewDate": "2026-08-07",
    "category": "NETWORK",
    "title": "TCP 흐름 제어",
    "totalCount": 3,
    "correctCount": 2,
    "completedAt": "2026-08-07T09:16:41"
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `interviewId` | Long | 면접 ID. 결과 상세 조회(`GET /api/interviews/{interviewId}`)에 사용한다. |
| `interviewDate` | LocalDate | 면접 날짜(KST). |
| `category` | String | 그날 질문의 카테고리(`Category`). |
| `title` | String | 그날 질문의 제목(`Question.title`). |
| `totalCount` | int | 전체 문항 수. |
| `correctCount` | int | 통과 문항 수. |
| `completedAt` | LocalDateTime | 완료 시각. |

### **에러**

없음. 완료된 면접이 없으면 빈 배열을 반환한다.

---

# **User API**

로그인한 사용자 본인의 프로필 조회·수정을 담당한다. 관련 도메인은 `user`다.

> 이 도메인의 모든 엔드포인트는 인증 토큰의 `userId`로 사용자를 조회한다. 토큰 발급 이후 해당 사용자가 삭제된 경우에만 `404 USER_NOT_FOUND`가 발생할 수 있다(현재 회원 삭제 기능이 없어 실질적으로는 발생하지 않는 방어적 오류다).

## **내 프로필 조회**

### **Endpoint**

```
GET /api/users/me
```

### **Response Body**

```json
{
  "nickname": "지민",
  "email": "jimin.dev@gmail.com",
  "position": "BACKEND",
  "dailyGoal": 10
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `nickname` | String | 닉네임. |
| `email` | String | 이메일. |
| `position` | String | 직무. `BACKEND` \| `FRONTEND` \| `FULLSTACK`. 가입 시 `BACKEND`로 고정되며, 프로필 수정으로 변경할 수 있다. |
| `dailyGoal` | int | 최소 학습 목표(하루 최소 풀이 세션 수). 가입 시 기본값(10)으로 설정된다. |

### **에러**

없음.

---

## **프로필 수정**

닉네임·직무·최소 학습 목표를 한 번에 수정한다(부분 수정 아님 — 매 요청마다 전체 필드를 보낸다). 이메일은 가입 시 값으로 고정되며 이 엔드포인트로 변경할 수 없다.

### **Endpoint**

```
PATCH /api/users/me
```

- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "nickname": "지민",
  "position": "BACKEND",
  "dailyGoal": 15
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `nickname` | String | O | 4~8자. 다른 사용자와 중복될 수 없다(본인의 기존 닉네임은 예외). |
| `position` | String | O | `BACKEND` \| `FRONTEND` \| `FULLSTACK`. |
| `dailyGoal` | int | O | 새 최소 학습 목표. 1 이상이어야 한다. |

### **Response Body**

```json
{
  "nickname": "지민",
  "email": "jimin.dev@gmail.com",
  "position": "BACKEND",
  "dailyGoal": 15
}
```

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | 필수값 누락, 닉네임 길이 위반, 이메일 형식 오류, `dailyGoal` 1 미만 등 요청 형식 검증 실패. |
| 409 | `USER_DUPLICATE_EMAIL` | 다른 사용자가 이미 사용 중인 이메일. |
| 409 | `USER_DUPLICATE_NICKNAME` | 다른 사용자가 이미 사용 중인 닉네임. |

---

# **NotificationSetting API**

로그인한 사용자 본인의 알림 설정 조회·수정을 담당한다. 관련 도메인은 `notification`이다.

설정은 가입 시 미리 만들지 않고 **최초 조회·수정 시점에 기본값으로 생성**된다(→ `docs/DOMAIN.md` NotificationSetting). `everyDayRemind = true`인 사용자에게는 매일 오후 9시(KST)에 학습 리마인드 메일이 발송된다. 이 발송은 HTTP API로 노출하지 않는다 — 수동 확인은 테스트 코드(`StudyReminderManualSendTest`)로 한다.

## **내 알림 설정 조회**

### **Endpoint**

```
GET /api/notification-settings/me
```

### **Response Body**

```json
{
  "everyDayRemind": true
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `everyDayRemind` | boolean | 매일 학습 리마인드 수신 여부. |

### **에러**

없음. 설정이 없던 사용자는 기본값으로 생성한 뒤 반환한다.

---

## **알림 설정 수정**

부분 수정이 아니라 매 요청마다 전체 필드를 보낸다(User 프로필 수정과 동일).

### **Endpoint**

```
PATCH /api/notification-settings/me
```

- 성공 시 `200 OK`를 반환한다.

### **Request Body**

```json
{
  "everyDayRemind": true
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `everyDayRemind` | boolean | O | 매일 학습 리마인드 수신 여부. |

### **Response Body**

```json
{
  "everyDayRemind": true
}
```

### **에러**

없음.

---

# **Progress API**

점수·티어와 카테고리별 풀이 현황을 담당한다. 관련 도메인은 `progress`다.

별도 저장 테이블 없이 `SolvedSession`(본질문은 `learningrecord` 도메인의 본질문 조회 로직을 재사용)을 조회 시점에 집계해 응답한다(→ `docs/DOMAIN.md` 학습 기록 집계 정책과 동일한 원칙). 모든 엔드포인트는 인증된 사용자 **본인의 진척도만** 조회한다.

## **점수 산정 규칙**

- 한 풀이 세션이 **본질문+꼬리질문을 전부 맞혔을 때만**(`correctCount == totalCount`) 점수를 받는다. 세션이 이 조건을 만족하지 못하면 그 세션에 포함된 문항은 전부 0점이다.
- 객관식과 서술형은 점수를 매기는 단위가 다르다(→ `docs/DOMAIN.md` 꼬리질문 분기·서술형 꼬리질문 생성 정책).
  - **객관식**: 본질문·꼬리질문 모두 문제은행의 실제 독립된 `Question`이므로, 세션에 등장한 **문항 하나하나(본질문 1개)가 각자 자기 `difficulty`로** 점수를 받는다(하 1점, 중 2점, 상 3점). 예를 들어 본질문(중, 2점)에 꼬리질문(상, 3점)이 이어져 둘 다 맞혔다면 그 세션은 총 5점이다.
  - **서술형**: 꼬리질문은 AI가 그때그때 생성하는 스냅샷이라 실제 `Question`(난이도)이 없으므로, **본질문 하나(본질문 1개 + 꼬리질문 2개, 총 3문항)의 `difficulty`로만** 점수를 매긴다. 점수는 같은 난이도의 객관식 점수의 4배(하 4점, 중 8점, 상 12점) — 3문항을 전부 맞혀야 이 점수를 받는다.
- **같은 `Question`은 유저가 살면서 최초로 만점 세션에 포함되어 맞힌 시점에만 점수를 지급한다.** 그 이후 같은 문항을 다시 풀면(본질문으로 나오든 다른 세션의 꼬리질문으로 나오든) 점수가 다시 오르지 않는다. 최초 성공 전의 실패한 시도는 점수 없이 지나갈 뿐, 이후 성공 기회를 막지 않는다.

## **티어**

| 티어 | 최소 누적 점수 |
| --- | --- |
| `BRONZE` | 0 |
| `SILVER` | 58 |
| `GOLD` | 198 |
| `PLATINUM` | 420 |
| `DIAMOND` | 677 |

## **진척도 조회**

### **Endpoint**

```
GET /api/progress
```

### **Response Body**

```json
{
  "score": 90,
  "tier": "SILVER",
  "nextTier": "GOLD",
  "scoreToNextTier": 108,
  "totalQuestionCount": 15,
  "categories": [
    { "category": "NETWORK", "totalCount": 40, "solvedCount": 12, "correctCount": 5, "score": 30 },
    { "category": "LANGUAGE", "totalCount": 100, "solvedCount": 5, "correctCount": 3, "score": 8 }
  ],
  "tiers": [
    { "tier": "BRONZE", "minScore": 0 },
    { "tier": "SILVER", "minScore": 58 },
    { "tier": "GOLD", "minScore": 198 },
    { "tier": "PLATINUM", "minScore": 420 },
    { "tier": "DIAMOND", "minScore": 677 }
  ],
  "maxScore": 700
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `score` | int | 누적 점수. |
| `tier` | String | 현재 티어(`BRONZE` \| `SILVER` \| `GOLD` \| `PLATINUM` \| `DIAMOND`). |
| `nextTier` | String \| null | 다음 티어. 이미 `DIAMOND`이면 `null`. |
| `scoreToNextTier` | int | 다음 티어까지 필요한 점수. 이미 `DIAMOND`이면 `0`. |
| `totalQuestionCount` | int | 지금까지 푼 전체 문항 수(완료한 세션의 `totalCount` 합, 본질문+꼬리질문 포함). 같은 문항을 여러 번 풀면 각각 더해진다. |
| `categories` | Array | 카테고리별 진척도. **`Category` enum 전체가 항상 내려간다** — 기록이 없는 카테고리도 0으로 채운다. 순서는 enum 선언 순서 고정. |
| `categories[].category` | String | 카테고리. |
| `categories[].totalCount` | int | 문제은행에 있는 그 카테고리 **전체 문항 수**. 객관식 꼬리질문도 독립된 `Question`이므로 포함된다(→ `docs/DOMAIN.md` 문제은행 표시 정책). 서술형 꼬리질문은 `Question`이 아니라 제외된다. |
| `categories[].solvedCount` | int | **풀어본 문항 수**(distinct `Question`, 정답/오답 무관). |
| `categories[].correctCount` | int | **맞힌 문항 수**(distinct `Question`). 위 산정 규칙대로 점수를 받은 문항 수와 같다 — 세션 전체가 정답이어야 하므로, 그 문항 자체는 맞혔어도 같은 세션의 다른 문항을 틀렸다면 포함되지 않는다. |
| `categories[].score` | int | 카테고리별 **획득 점수**. |
| `tiers` | Array | 티어 구간표. 클라이언트가 티어 진행 바의 구간 비율을 그리는 데 사용한다. 순서는 `minScore` 오름차순 고정. |
| `tiers[].tier` | String | 티어 이름. |
| `tiers[].minScore` | int | 그 티어의 진입 점수(구간 하한). |
| `maxScore` | int | 티어 진행 바의 표시 상한(현재 `700`). 프로덕트가 정한 값이며 획득 가능한 실제 최대 점수와는 별개다. |

> 임계값을 응답에 담는 이유: 진행 바가 구간 비율을 그리려면 경계값이 필요한데, 클라이언트가 같은 수치를 따로 들고 있으면 정책이 바뀔 때 조용히 어긋난다. 티어 구간은 도메인 정책이므로 서버를 단일 소스로 둔다.

### **에러**

없음. 풀이 기록이 없으면 `score: 0`, `tier: "BRONZE"`, `totalQuestionCount: 0`을 반환하고, `categories`의 `solvedCount`·`correctCount`·`score`가 모두 `0`이 된다(`totalCount`·`tiers`·`maxScore`는 사용자 기록과 무관하게 항상 채워진다).

## **진척도 상단 통계 조회**

누적/연속 학습일, 총 풀이 문제·정답·오답, 1일1면접 참여 횟수를 조회한다. 누적/연속 학습일은 `docs/API.md` LearningRecord API의 연속·누적 학습일 조회와 동일한 값(같은 `SolvedSession.solvedAt` distinct 날짜 집계)이다.

### **Endpoint**

```
GET /api/progress/summary
```

### **Response Body**

```json
{
  "cumulativeDays": 42,
  "streakDays": 7,
  "totalQuestionCount": 128,
  "totalCorrectCount": 96,
  "totalWrongCount": 32,
  "completedInterviewCount": 16
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `cumulativeDays` | int | 누적 학습일(학습한 날의 총 수, distinct). |
| `streakDays` | int | 연속 학습일. |
| `totalQuestionCount` | int | 지금까지 푼 전체 문항 수(완료한 세션의 `totalCount` 합, 본질문+꼬리질문 포함). **같은 문항을 여러 번 풀면 각각 더해진다** — distinct 문항 수가 아니라 풀이 횟수다(카테고리별 `solvedCount`는 distinct라 값이 다르다). |
| `totalCorrectCount` | int | 지금까지 맞힌 전체 문항 수. 위와 같이 중복을 포함한다. |
| `totalWrongCount` | int | 지금까지 틀린 전체 문항 수(`totalQuestionCount - totalCorrectCount`). |
| `completedInterviewCount` | int | 완료한 1일1면접 총 횟수. |

### **에러**

없음. 기록이 없으면 전부 `0`을 반환한다.

---

# **ProblemSet API**

문제집 생성·목록/상세 조회·문제 담기/빼기·삭제를 담당한다. 관련 도메인은 `problemset`이다.

문제집은 유튜브 재생목록과 같은 개념으로, 사용자가 원하는 문제를 모아 만드는 이름 붙은 목록이다(→ `docs/DOMAIN.md` ProblemSet). **항상 본인만 볼 수 있다** — 공개 범위 같은 필드 자체가 없다. 모든 엔드포인트는 인증된 사용자 **본인 소유의 문제집만** 조회·수정·삭제할 수 있다. 담긴 문제는 `questionId`만 참조하고 제목·카테고리·유형·난이도는 조회 시점에 문제은행에서 조인해 채운다.

## **문제집 생성**

### **Endpoint**

```
POST /api/problem-sets
```

- 성공 시 `201 Created`를 반환한다.
- **항상 빈 문제집으로 생성된다** — 생성과 동시에 문제를 담는 기능은 없다. 문제 풀이 화면의 "문제집에 저장" 모달에서 새 문제집을 만들어도 마찬가지로 빈 문제집이 만들어지며, 그 문제를 담으려면 모달에서 새로 생긴 항목을 다시 체크해야 한다(→ [문제집에 문제 담기](#문제집에-문제-담기)).

### **Request Body**

```json
{
  "name": "면접 D-7 벼락치기"
}
```

| **필드** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `name` | String | O | 문제집 이름. 빈 문자열 불가. |

### **Response Body**

```json
{
  "id": 1,
  "name": "면접 D-7 벼락치기",
  "updatedAt": "2026-06-25T10:00:00"
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제집 ID. |
| `name` | String | 문제집 이름. |
| `updatedAt` | LocalDateTime | 수정 시각. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | `name` 누락 또는 빈 문자열. |

---

## **문제집 목록 조회**

### **Endpoint**

```
GET /api/problem-sets
```

정렬은 문제집 ID(`ProblemSet.id`) 내림차순(생성순의 역순)으로 고정한다.

### **Response Body**

```json
[
  {
    "id": 1,
    "name": "면접 D-7 벼락치기",
    "itemCount": 3,
    "previewTitles": ["TCP와 UDP의 핵심 차이는?", "해시 테이블의 평균 탐색 시간은?"],
    "updatedAt": "2026-06-25T10:00:00"
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제집 ID. 상세 조회·삭제에 사용. |
| `name` | String | 문제집 이름. |
| `itemCount` | int | 담긴 문제 수. |
| `previewTitles` | String[] | 담긴 문제 중 먼저 추가된 순서로 최대 3개의 제목. |
| `updatedAt` | LocalDateTime | 수정 시각. |

### **에러**

없음. 문제집이 없으면 빈 배열을 반환한다.

---

## **문제집 저장 멤버십 조회**

문제 풀이 화면의 "문제집에 저장" 모달용 엔드포인트다. 특정 문제 하나를 기준으로 내 모든 문제집을 조회하면서, 각 문제집에 그 문제가 이미 담겨 있는지(`saved`)까지 함께 내려준다 — 모달의 체크박스 초기 상태를 만드는 데 쓴다.

### **Endpoint**

```
GET /api/problem-sets/membership
```

| **Query Param** | **타입** | **필수** | **설명** |
| --- | --- | --- | --- |
| `questionId` | Long | O | 담겨 있는지 확인할 문제 ID. |

### **Response Body**

```json
[
  {
    "id": 1,
    "name": "면접 D-7 벼락치기",
    "itemCount": 3,
    "saved": true
  }
]
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제집 ID. |
| `name` | String | 문제집 이름. |
| `itemCount` | int | 담긴 문제 수. |
| `saved` | boolean | 쿼리로 넘긴 `questionId`가 이 문제집에 이미 담겨 있는지. |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `INVALID_INPUT` | `questionId` 누락. |

---

## **문제집 상세 조회**

### **Endpoint**

```
GET /api/problem-sets/{problemSetId}
```

### **Response Body**

```json
{
  "id": 1,
  "name": "면접 D-7 벼락치기",
  "updatedAt": "2026-06-25T10:00:00",
  "items": [
    {
      "questionId": 7,
      "title": "TCP와 UDP의 핵심 차이는?",
      "category": "NETWORK",
      "type": "MULTIPLE_CHOICE",
      "difficulty": "MEDIUM"
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제집 ID. |
| `name` | String | 문제집 이름. |
| `updatedAt` | LocalDateTime | 수정 시각. |
| `items` | Object[] | 담긴 문제 목록. 추가한 순서대로 정렬된다. |
| `items[].questionId` | Long | 문제 ID. 풀이 화면(`/solve/{questionId}`) 진입에 사용. |
| `items[].title` | String | 문제 제목. |
| `items[].category` | String | 카테고리. |
| `items[].type` | String | 유형(`MULTIPLE_CHOICE` \| `ESSAY`). |
| `items[].difficulty` | String | 난이도(`LOW` \| `MEDIUM` \| `HIGH`). |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `PROBLEM_SET_NOT_FOUND` | `problemSetId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

## **문제집에 문제 담기**

체크박스를 켜는 동작에 대응한다. 이미 담겨 있으면 아무 일도 하지 않는다(멱등).

### **Endpoint**

```
PUT /api/problem-sets/{problemSetId}/items/{questionId}
```

- 성공 시 `204 No Content`를 반환한다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `PROBLEM_SET_NOT_FOUND` | `problemSetId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |
| 404 | `QUESTION_NOT_FOUND` | `questionId`가 실재하지 않는 문제. |

---

## **문제집에서 문제 빼기**

체크박스를 끄는 동작, 또는 상세 화면의 "제거"에 대응한다. 이미 없는 문제를 빼려 해도 아무 일도 하지 않는다(멱등). 문제(`Question`) 자체나 다른 문제집에는 영향이 없다.

### **Endpoint**

```
DELETE /api/problem-sets/{problemSetId}/items/{questionId}
```

- 성공 시 `204 No Content`를 반환한다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `PROBLEM_SET_NOT_FOUND` | `problemSetId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

## **문제집 삭제**

### **Endpoint**

```
DELETE /api/problem-sets/{problemSetId}
```

- 성공 시 `204 No Content`를 반환한다. 담겨 있던 `ProblemSetItem`도 함께 삭제된다. 문제(`Question`) 자체는 삭제하지 않는다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 404 | `PROBLEM_SET_NOT_FOUND` | `problemSetId`가 존재하지 않거나, 요청한 사용자 소유가 아님. |

---

# **Mastery API**

사용자가 지금까지 받은 숙련도 판정을 조회한다. 관련 도메인은 `mastery`다.

숙련도는 **문제를 풀 때마다** 판정해 그 문항의 태그·카테고리에 연결해 기록한다. 서술형은 채점 AI가 답변 내용을 근거로 판정하고(채점 응답 시점), 객관식은 AI를 호출하지 않고 정답 여부와 소요시간 비율로 서버가 판정한다(세션 저장 시점). 두 트랙 모두 이 API로 조회된다(→ `docs/RECOMMENDATION.md` 숙련도 판정 정책).

## **숙련도 조회**

### **Endpoint**

```
GET /api/mastery
```

- 인증이 필요하다. `userId`는 인증 계층에서 해석한다.
- 성공 시 `200 OK`를 반환한다.
- 판정 이력이 하나도 없는 카테고리는 배열에서 제외된다. 판정이 전혀 없으면 `categories`가 빈 배열이다.

### **Response Body**

```json
{
  "categories": [
    {
      "category": "DB",
      "levelCounts": {
        "NOT_LEARNED": 2,
        "UNSTABLE": 1
      },
      "tags": [
        {
          "tagId": 1,
          "name": "인덱스",
          "level": "NOT_LEARNED",
          "reason": "카디널리티를 언급했지만 인덱스를 타지 않는 이유를 설명하지 못했습니다.",
          "updatedAt": "2026-08-19T10:00:00"
        }
      ]
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `categories[].category` | String | 카테고리. |
| `categories[].levelCounts` | Object | 그 카테고리에서 각 숙련도를 몇 번 받았는지. 받은 적 없는 숙련도는 키에서 빠진다. 태그가 없는 문항의 판정도 포함된다. |
| `categories[].tags[]` | Array | 태그별 **현재** 숙련도. 최근에 판정된 태그가 먼저 온다. |
| `categories[].tags[].level` | String | 그 태그의 현재 숙련도. 새 판정이 오면 누적이 아니라 덮어써진다. |
| `categories[].tags[].reason` | String | 그 판정의 근거. 서술형은 AI가 답변에서 짚은 문장, 객관식은 정답 여부·평균 대비 소요시간을 옮긴 문장이다. |
| `categories[].tags[].updatedAt` | String | 현재 숙련도가 갱신된 시각. |

`levelCounts`는 이력 기반 누적이고 `tags[].level`은 현재값이다. 그래서 같은 태그를 여러 번 풀면 `levelCounts`의 합은 늘어나지만 `tags[].level`은 하나만 남는다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | 토큰이 없거나 유효하지 않음. |

---

## **맞춤 문제 추천**

사용자의 약점을 진단해 그에 맞는 문제를 반환한다. 취약 주제에 해당하는 **서술형 문항을 AI로 생성**해 내려주고, 생성이 불가능하거나 부족하면 기존 문제은행 문항으로 채운다. 추천 자체가 실패하지 않는 것이 원칙이다(→ `docs/RECOMMENDATION.md`).

생성된 문항은 검수 전(`PENDING`) 상태로 저장되므로 문제 목록 조회(`GET /api/questions`)에는 나타나지 않는다. 이 응답과 문제 단건 조회로만 도달한다.

같은 사용자가 하루에 여러 번 호출해도 생성은 한 번이다. 문제를 더 풀어 약점 프로필이 바뀌면 캐시가 무효화되어 다음 호출에서 새로 생성한다.

### **Endpoint**

```
GET /api/recommendations/questions
```

- 인증이 필요하다. `userId`는 인증 계층에서 해석한다.
- 성공 시 `200 OK`와 문항 배열을 반환한다.
- 한 번의 응답에 최대 3문항을 담는다. 문항 하나가 채점까지 합쳐 여러 번의 AI 호출을 유발하므로 보수적으로 제한한다.

### **Response Body**

```json
{
  "personalized": true,
  "generated": true,
  "questions": [
    {
      "id": 342,
      "title": "낮은 카디널리티 컬럼의 인덱스",
      "content": "카디널리티가 낮은 컬럼에 인덱스를 걸면 조회 성능이 어떻게 달라지는지 이유와 함께 설명하라.",
      "type": "ESSAY",
      "difficulty": "LOW",
      "category": "DB",
      "tags": ["인덱스"],
      "generated": true
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `personalized` | boolean | 약점 프로필을 근거로 골랐는지 여부. 풀이 이력이 3건 미만인 콜드스타트면 `false`. |
| `generated` | boolean | 이번 응답에 AI가 새로 만든 문항이 하나라도 포함됐는지 여부. |
| `questions[].type` | String | `MULTIPLE_CHOICE` \| `ESSAY`. 생성 문항은 항상 `ESSAY`이고, 폴백·콜드스타트에서는 객관식도 내려간다. |
| `questions[].generated` | boolean | 그 문항이 이번 추천을 위해 생성된 검수 전 문항인지 여부. **현재 클라이언트는 이 값을 화면에 노출하지 않는다** — 생성 문항인지 기존 문항인지는 사용자에게 구분해 보여주지 않기로 했다. 운영·디버깅용 정보다. |

`questions[]`의 나머지 필드는 문제 목록 조회와 같은 의미다. 다만 `explanation`·`choices`는 내려가지 않으므로, 문제를 풀려면 문제 단건 조회로 상세를 받는다.

`personalized`·`generated`도 `questions[].generated`와 같이 화면에 노출하지 않는다. 사용자에게는 콜드스타트·폴백·정상 생성이 모두 "내 취약점에 맞춘 문제" 하나로 보이고, 세 경우의 화면 문구가 같다. 클라이언트는 `questions[]`만 쓰면 된다.

### **동작 분기**

| 상황 | `personalized` | `generated` | 내용 |
| --- | --- | --- | --- |
| 풀이 이력 3건 미만 | `false` | `false` | 난이도 `LOW` 문항을 카테고리별로 고르게 반환. AI를 호출하지 않는다. |
| 정상 | `true` | `true` | 취약 주제로 생성한 서술형 문항. 부족분은 기존 문항으로 채운다. |
| 생성 실패·쿼터 소진 | `true` | `false` | 취약 주제에 해당하는 기존 문제은행 문항(객관식·서술형 모두). |
| 같은 날 재조회 | 이전과 동일 | 이전과 동일 | 캐시된 문항을 같은 순서로 반환한다. |

재조회해도 같은 문항이 돌아오므로, "다른 문제"는 이 API를 다시 부르지 말고 한 번의 응답으로 받은 `questions[]`를 클라이언트가 순환해 보여준다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 401 | `UNAUTHORIZED` | 토큰이 없거나 유효하지 않음. |

AI 생성 실패·쿼터 소진은 에러로 내리지 않고 폴백으로 응답한다. 아래 에러코드는 추천 전용 쿼터 버킷으로 정의돼 있으며, 서술형 채점 쿼터(`ESSAY_AI_*`)를 잠식하지 않는다. 폴백까지 실패하는 경우에만 노출된다.

| **code** | **의미** |
| --- | --- |
| `RECOMMENDATION_AI_UNAVAILABLE` | 생성 중 AI 장애. |
| `RECOMMENDATION_AI_QUOTA_EXCEEDED` | 순간 요청량 초과. |
| `RECOMMENDATION_AI_DAILY_QUOTA_EXCEEDED` | 일일 생성 한도 초과. |

### **취약 태그 조회**

전체 풀이 이력을 약점 프로필로 계산해, 취약도 상위 태그를 최대 4개 반환한다. 최근 기간으로 제한하지 않는다.

```
GET /api/recommendations/weak-tags
```

```json
{
  "sampleCount": 128,
  "tags": [
    {
      "tag": "TCP/IP",
      "weaknessScore": 0.85,
      "sampleCount": 7
    }
  ]
}
```

| 필드 | 설명 |
| --- | --- |
| `sampleCount` | 약점 계산에 반영된 전체 풀이 문항 수. |
| `tags[].tag` | 취약 태그명. |
| `tags[].weaknessScore` | `0.0`~`1.0` 범위의 약점도. 높을수록 취약하다. |
| `tags[].sampleCount` | 해당 태그가 붙은 문제를 푼 횟수. |

인증이 필요하다. 풀이 이력이나 태그가 없으면 `tags`는 빈 배열이다.

---

# **Admin API**

관리자 백오피스 전용 API. 관련 도메인은 `admin`이다.

모든 엔드포인트는 `/api/admin` 접두사를 쓰며, 인증을 통과했더라도 `role`이 `ADMIN`이 아니면 `403 AUTH_FORBIDDEN`을 반환한다(→ 권한, `docs/DOMAIN.md` 관리자 권한 정책). 통계 값은 별도 집계 테이블 없이 **조회 시점에 계산**한다 — 오답노트·학습 기록이 상태를 컬럼으로 저장하지 않는 원칙과 같다.

---

## **문제 목록 조회**

관리자 백오피스의 문제 관리 목록을 조회한다. 공개 문제은행 목록(`GET /api/questions`)과 동일한 조회 조건·페이징 규칙을 쓰되, `solved` 대신 문제별 **풀이수·정답률**을 함께 내려준다.

**공개 목록과 달리 검수 전 문항(`PENDING`)과 거절된 문항(`REJECTED`)도 함께 반환한다.** 검수 대기열을 봐야 하는 유일한 화면이기 때문이며, 화면이 구분할 수 있도록 `reviewStatus`와 `source`를 함께 내려준다(→ `docs/RECOMMENDATION.md` 저장 정책).

풀이수·정답률은 문제 유형에 따라 다른 테이블을 집계한다 — 객관식은 `SolvedMultipleChoice`, 서술형은 `EssaySolved`(본질문 풀이만, `questionId`가 있는 행) 기준이다. 단건 조회(객관식 문제 통계 조회)와 달리 페이지에 담긴 여러 문제를 한 번에 묶어 집계한다.

### **Endpoint**

```
GET /api/admin/questions
```

- 성공 시 `200 OK`와 페이지 응답을 반환한다.
- 정렬은 문제 ID 내림차순(최신순) 고정이다.

### **Query Parameters**

문제 목록 조회(`GET /api/questions`)와 동일하다. 모두 선택이며, 생략하면 해당 조건을 적용하지 않는다.

| **파라미터** | **타입** | **설명** |
| --- | --- | --- |
| `type` | String | 문제 유형. `MULTIPLE_CHOICE` \| `ESSAY` |
| `difficulty` | String | 난이도. `LOW` \| `MEDIUM` \| `HIGH` |
| `category` | String | 카테고리. `DB` \| `NETWORK` \| `ALGORITHM` \| `DATA_STRUCTURE` \| `OS` \| `DESIGN_PATTERN` \| `LANGUAGE` |
| `q` | String | 제목·지문 키워드. 부분 일치이며 대소문자를 구분하지 않는다. |
| `page` | int | 0부터 시작하는 페이지 번호. 생략하거나 음수면 `0`으로 보정한다. |
| `size` | int | 한 페이지 문항 수. 생략하거나 1 미만이면 `20`, 100을 넘으면 `100`으로 보정한다. |

### **Response Body**

```json
{
  "content": [
    {
      "id": 12,
      "title": "REPEATABLE READ의 이상 현상",
      "category": "DB",
      "difficulty": "MEDIUM",
      "type": "MULTIPLE_CHOICE",
      "reviewStatus": "APPROVED",
      "source": "SEEDED",
      "solveCount": 1842,
      "correctRate": 63.8
    },
    {
      "id": 7,
      "title": "AI가 생성한 인덱스 문항",
      "category": "DB",
      "difficulty": "HIGH",
      "type": "ESSAY",
      "reviewStatus": "PENDING",
      "source": "GENERATED",
      "solveCount": 0,
      "correctRate": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 137,
  "totalPages": 7,
  "last": false
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `content[].id` | Long | 문제 ID. |
| `content[].title` | String | 문제 제목. |
| `content[].category` | String | 카테고리. |
| `content[].difficulty` | String | 난이도. |
| `content[].type` | String | 문제 유형(`MULTIPLE_CHOICE` \| `ESSAY`). |
| `content[].reviewStatus` | String | 검수 상태(`APPROVED` \| `PENDING` \| `REJECTED`). 관리자가 전이시키는 가변 상태다. |
| `content[].source` | String | 문항 출신(`SEEDED` \| `GENERATED`). 생성 시점에 확정되며 승인 뒤에도 바뀌지 않는다. |
| `content[].solveCount` | long | 이 문제의 전체 풀이 응답 수. 본질문·꼬리질문 구분 없이 더한다. |
| `content[].correctRate` | double \| null | 정답률(%), 소수점 첫째 자리로 반올림. **`solveCount`가 0이면 `0.0`이 아니라 `null`이다** — "아직 안 풀림"과 "0% 정답"은 다르다. |

### **에러**

없음. 조건에 맞는 문제가 없으면 빈 `content`와 `200 OK`를 반환한다.

---

## **문제 상세 조회**

관리자 백오피스의 문제 상세 화면에서 쓴다. 공개 문제 단건 조회(`GET /api/questions/{questionId}`)와 달리 **선택지에 정답 여부(`correct`)를 그대로 노출**한다 — 관리자는 정답을 볼 수 있어야 하므로 사용자용 응답과 DTO를 분리했다.

서술형 문제는 `choices`가 빈 배열이며, 대신 `solveCount`·`correctRate`로 풀이수·정답률을 함께 내려준다. **객관식 문제는 이 두 필드가 항상 `null`이다** — 객관식은 지표 집합이 더 큰 별도 통계 조회 API(`GET /api/admin/questions/{questionId}/statistics`)를 쓴다.

### **Endpoint**

```
GET /api/admin/questions/{questionId}
```

- 성공 시 `200 OK`를 반환한다.

### **Response Body — 객관식**

```json
{
  "id": 12,
  "title": "REPEATABLE READ의 이상 현상",
  "content": "트랜잭션 격리 수준을 REPEATABLE READ로 설정했을 때...",
  "type": "MULTIPLE_CHOICE",
  "difficulty": "MEDIUM",
  "category": "DB",
  "explanation": "REPEATABLE READ는 동일 트랜잭션 내...",
  "choices": [
    {
      "id": 33,
      "sequence": 1,
      "content": "Dirty Read — 커밋되지 않은 데이터를 읽는 현상",
      "correct": false,
      "explanation": "Dirty Read는 READ UNCOMMITTED에서만 발생하며...",
      "relatedQuestionId": null
    },
    {
      "id": 34,
      "sequence": 2,
      "content": "Phantom Read — 범위 조회 시 없던 행이 나타나는 현상",
      "correct": true,
      "explanation": "",
      "relatedQuestionId": null
    }
  ],
  "tags": ["트랜잭션", "격리수준"],
  "solveCount": null,
  "correctRate": null
}
```

### **Response Body — 서술형**

```json
{
  "id": 7,
  "title": "SYN flooding이 성립하는 원인",
  "content": "TCP 3-way handshake 과정에서 SYN flooding 공격이 성립하는 원인을 서술하세요.",
  "type": "ESSAY",
  "difficulty": "HIGH",
  "category": "NETWORK",
  "explanation": "서버가 SYN을 받은 뒤 SYN+ACK를 보내고...",
  "choices": [],
  "tags": ["TCP", "핸드셰이크", "보안"],
  "solveCount": 312,
  "correctRate": 41.2
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `id` | Long | 문제 ID. |
| `title` | String | 문제 제목. |
| `content` | String | 문제 발문. |
| `type` | String | 문제 유형(`MULTIPLE_CHOICE` \| `ESSAY`). |
| `difficulty` | String | 난이도. |
| `category` | String | 카테고리. |
| `explanation` | String | 정답(전체) 해설. |
| `choices` | Array | 선택지 목록. 서술형은 항상 빈 배열. |
| `choices[].id` | Long | 선택지 ID. |
| `choices[].sequence` | int | 보기 표시 순서. |
| `choices[].content` | String | 선택지 내용. |
| `choices[].correct` | boolean | 정답 선택지 여부. 단일 정답이므로 `true`는 정확히 1개다. |
| `choices[].explanation` | String | 이 선택지를 골랐을 때의 오답 해설. 정답 선택지는 빈 값. |
| `choices[].relatedQuestionId` | Long \| null | 이 선택지를 골랐을 때 이어지는 꼬리질문 ID. 없으면 `null`. |
| `tags` | Array | 문제 태그 이름 목록. 없으면 빈 배열. |
| `solveCount` | long \| null | 서술형 문제의 전체 풀이 응답 수(본질문 풀이만, `EssaySolved.questionId`가 있는 행 기준). **객관식은 항상 `null`.** |
| `correctRate` | double \| null | 서술형 문제의 정답률(%), 소수점 첫째 자리로 반올림. 풀이가 없으면 `null`. **객관식은 항상 `null`.** |

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 403 | `AUTH_FORBIDDEN` | `role`이 `ADMIN`이 아님. |
| 404 | `QUESTION_NOT_FOUND` | `questionId`가 존재하지 않음. |

---

## **객관식 문제 통계 조회**

객관식 문제 한 건의 전체 풀이 횟수·정답률·평균 소요 시간·가장 많이 고른 선택지·보기별 선택 분포를 조회한다. 관리자 문제 상세 화면의 "통계" 탭에서 사용한다.

집계 대상은 `SolvedMultipleChoice`에 남은 해당 문제의 모든 응답이다. **어떤 세션에서 본질문으로 풀렸는지 꼬리질문으로 풀렸는지 구분하지 않는다** — 모든 `Question`이 동등한 독립 문항이고 등장 위치는 세션마다 달라지기 때문이다(→ `docs/DOMAIN.md` 결정 사항: 본질문·꼬리질문 구분 없음).

**서술형 문제에는 사용할 수 없다.** 선택지가 없어 분포·최다 선택지가 성립하지 않으므로 `400 QUESTION_NOT_MULTIPLE_CHOICE`를 반환한다. 서술형 통계는 지표 집합이 달라 별도 엔드포인트로 설계한다(미구현).

### **Endpoint**

```
GET /api/admin/questions/{questionId}/statistics
```

- 성공 시 `200 OK`를 반환한다.
- 아직 아무도 풀지 않은 문제도 에러가 아니다. 모든 지표가 `0`이고 `mostChosenChoice`·`averageElapsedSeconds`가 `null`이며, 보기 분포는 전부 `selectedCount: 0`으로 채워진다.

### **Response Body**

```json
{
  "questionId": 12,
  "totalSolveCount": 1842,
  "correctCount": 1175,
  "correctRate": 63.8,
  "averageElapsedSeconds": 78,
  "elapsedSampleCount": 412,
  "mostChosenChoice": {
    "choiceId": 34,
    "sequence": 2,
    "content": "Phantom Read — 범위 조회 시 없던 행이 나타나는 현상",
    "correct": true,
    "selectedCount": 1175,
    "selectedRate": 63.8
  },
  "choiceDistribution": [
    {
      "choiceId": 33,
      "sequence": 1,
      "content": "Dirty Read — 커밋되지 않은 데이터를 읽는 현상",
      "correct": false,
      "selectedCount": 318,
      "selectedRate": 17.3
    },
    {
      "choiceId": 34,
      "sequence": 2,
      "content": "Phantom Read — 범위 조회 시 없던 행이 나타나는 현상",
      "correct": true,
      "selectedCount": 1175,
      "selectedRate": 63.8
    },
    {
      "choiceId": 35,
      "sequence": 3,
      "content": "Lost Update — 동시 갱신으로 한쪽 변경이 사라지는 현상",
      "correct": false,
      "selectedCount": 214,
      "selectedRate": 11.6
    },
    {
      "choiceId": 36,
      "sequence": 4,
      "content": "Non-Repeatable Read — 재조회 시 값이 달라지는 현상",
      "correct": false,
      "selectedCount": 135,
      "selectedRate": 7.3
    }
  ]
}
```

| **필드** | **타입** | **설명** |
| --- | --- | --- |
| `questionId` | Long | 조회한 문제 ID. |
| `totalSolveCount` | long | 이 문제에 대한 전체 응답 수. 같은 사용자가 여러 번 풀면 각각 더해지고, 꼬리질문으로 푼 응답도 포함한다. |
| `correctCount` | long | 맞힌 응답 수. 응답 시점에 저장된 `SolvedMultipleChoice.isCorrect` 기준이다. |
| `correctRate` | double | 정답률(%). `correctCount / totalSolveCount`를 소수점 첫째 자리로 반올림한 값. 응답이 없으면 `0.0`. |
| `averageElapsedSeconds` | int \| null | 평균 소요 시간(초, 반올림). **소요 시간이 수집된 응답이 하나도 없으면 `0`이 아니라 `null`이다** — "아직 데이터 없음"과 "0초에 풀었다"는 다르다. |
| `elapsedSampleCount` | long | 위 평균이 몇 건의 응답으로 계산됐는지. `totalSolveCount`보다 작을 수 있다 — 소요 시간 수집 이전에 쌓인 응답과 이상치는 집계에서 빠지기 때문이다. 평균을 신뢰할지 판단하는 근거로 함께 본다. |
| `mostChosenChoice` | Object \| null | 가장 많이 선택된 보기. 원소 형식은 `choiceDistribution`과 같다. 동점이면 `sequence`가 빠른 보기다. **응답이 한 건도 없으면 `null`.** |
| `choiceDistribution` | Object[] | 보기별 선택 분포. **현재 보기 전체**가 `sequence` 오름차순으로 담기며, 아무도 고르지 않은 보기도 `selectedCount: 0`으로 포함된다. |
| `choiceDistribution[].choiceId` | Long | 보기 ID. |
| `choiceDistribution[].sequence` | int | 화면의 보기 번호(1부터). |
| `choiceDistribution[].content` | String | 보기 텍스트. |
| `choiceDistribution[].correct` | boolean | 정답 보기 여부. 단일 정답이므로 `true`는 정확히 1개다. |
| `choiceDistribution[].selectedCount` | long | 이 보기를 고른 응답 수. |
| `choiceDistribution[].selectedRate` | double | 전체 응답 대비 비율(%). 분모는 `totalSolveCount`이며 소수점 첫째 자리로 반올림한다. |

비율은 숫자로 내려간다. `%` 기호를 붙이는 등의 표시 형식은 클라이언트가 정한다.

> **분포 비율의 합이 100%가 아닐 수 있다.** 문제 수정으로 보기가 교체·삭제되면 과거 응답이 가리키는 보기가 현재 목록에 없다. 이런 응답은 `totalSolveCount`에는 남지만 어느 보기 행에도 속하지 않으므로 분포에서 빠진다. 과거 풀이 횟수를 조용히 줄이지 않기 위한 선택이다.

### **에러**

| **HTTP** | **code** | **발생 조건** |
| --- | --- | --- |
| 400 | `QUESTION_NOT_MULTIPLE_CHOICE` | `questionId`가 서술형(`ESSAY`) 문제임. |
| 403 | `AUTH_FORBIDDEN` | `role`이 `ADMIN`이 아님. |
| 404 | `QUESTION_NOT_FOUND` | `questionId`가 존재하지 않음. |
