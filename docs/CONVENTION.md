# **Development Convention**

## **목표**

이 문서는 **특정 레이어에 종속되지 않고 백엔드 코드 전반에 공통으로 적용되는 개발 컨벤션**을 규정한다. 레이어 구조·의존성 방향은 `docs/ARCHITECTURE.md`, 예외 처리는 `docs/EXCEPTION.md`, 테스트는 `docs/TEST.md`를 참고한다.

## **모델은 별도 파일로 둔다 (inner class/record 지양)**

Request, Response, Command, Result, Payload처럼 레이어 간 값을 전달하는 모델을 Controller·service·implement·domain 클래스 내부에 inner class/record로 정의하지 않는다. 하나의 모델은 하나의 파일로 분리한다. 요청/응답 DTO는 `presentation.dto`, 서비스 커맨드/결과는 `service.dto`, implement의 페이로드/집계 모델은 `implement.dto`처럼 레이어별 `dto` 패키지에 둔다.

- 한 클래스가 여러 모델 정의를 떠안으면 클래스의 역할이 흐려진다.
- 파일명이 곧 모델 이름이 되어 탐색과 재사용이 쉬워진다.

권장:

```java
// SubmitSessionCommand.java
public record SubmitSessionCommand(...) { }

// SubmitSessionResult.java
public record SubmitSessionResult(...) { }
```

지양:

```java
public class SolvedSessionService {

    // 서비스 안에 모델을 inner record로 정의하지 않는다.
    public record SubmitSessionCommand(...) { }
    public record SubmitSessionResult(...) { }
}
```

## **불변 모델은 record로 정의한다**

값을 담아 전달하는 불변 모델(DTO, Command, Result, Payload 등)은 record로 정의한다. 별도의 상태 변경이 없는 데이터 전달 객체에 가변 클래스를 사용하지 않는다.

## **객체 변환은 정적 팩토리 메서드로 표현한다**

한 모델에서 다른 모델로의 변환은 정적 팩토리 메서드로 표현한다. 입력이 하나면 `from`, 단일 값으로부터 만들면 `of`처럼 의미가 드러나는 이름을 쓴다.

```java
public record SubmitItemResult(Long questionId, Long userChoiceId, Long correctChoiceId, boolean correct) {

    static SubmitItemResult from(ScoredAnswer scoredAnswer) {
        return new SubmitItemResult(
                scoredAnswer.questionId(),
                scoredAnswer.userChoiceId(),
                scoredAnswer.correctChoiceId(),
                scoredAnswer.correct()
        );
    }
}
```

## **Controller는 ResponseEntity로 응답한다**

Controller의 응답은 `ResponseEntity<T>`로 감싸 반환한다. `@ResponseStatus`나 바디 직접 반환 대신, 상태 코드를 응답 조립 지점에서 명시한다.

```java
@PostMapping
public ResponseEntity<CreateSolvedSessionResponse> create(
        Long userId,
        @Valid @RequestBody CreateSolvedSessionRequest request
) {
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CreateSolvedSessionResponse.from(solvedSessionService.create(userId, request.toCommand())));
}
```

- 조회는 `ResponseEntity.ok(...)`, 생성은 `ResponseEntity.status(HttpStatus.CREATED).body(...)`를 사용한다.
- 상태 코드가 메서드 시그니처와 반환문에서 함께 읽혀, 핸들러만 봐도 응답 형태를 알 수 있다.

## **의존성은 생성자 주입으로 받는다**

Spring 빈은 `@RequiredArgsConstructor`와 `private final` 필드를 사용해 생성자 주입한다. 필드 주입(`@Autowired` 필드)이나 세터 주입을 사용하지 않는다.

```java
@Service
@RequiredArgsConstructor
public class SolvedSessionService {

    private final SolvedSessionSubmitValidator solvedSessionSubmitValidator;
    private final MultipleChoiceAnswerScorer multipleChoiceAnswerScorer;
}
```

## **리뷰 체크리스트**

- Controller·service·implement·domain 클래스 안에 inner class/record로 모델을 정의하지 않았는가?
- 값 전달 모델을 record로 정의했는가?
- 모델 간 변환을 정적 팩토리 메서드(`from`/`of`)로 표현했는가?
- Controller가 `ResponseEntity`로 상태 코드를 명시해 응답하는가?
- 의존성을 `@RequiredArgsConstructor` 생성자 주입으로 받는가?
