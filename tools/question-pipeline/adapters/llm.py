"""LLM 호출 어댑터.

노드는 이 인터페이스만 알고 LangChain을 모른다. 그래서 FakeClient로 그래프 전체를
API 호출 0회로 돌려볼 수 있다 — 45회를 쓰고 나서 포맷 버그가 터지는 걸 막는다.
"""

from __future__ import annotations

import json
import os
import re
import time
import urllib.error
import urllib.request
from typing import Any, Protocol

BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai"
MODEL = "gemini-3.5-flash-lite"
TEMPERATURE = 0.3

OLLAMA_HOST = "http://localhost:11434"
LOCAL_MODEL = "qwen3:8b"
LOCAL_TIMEOUT = 600
LOCAL_KEEP_ALIVE = "30m"
# 접속 규약은 PR #70(src/main/resources/config/application-ai-ollama.yml)과 같다 —
# /v1 엔드포인트, 더미 api-key, temperature 0.3.
# 모델만 다르다. 그쪽은 채점 비교용 gemma3:4b, 여기는 생성용이라 지시 준수가 더 필요하다.
# reasoning_effort=none 은 thinking 모델에게만 보낸다. 미지원 모델은 400으로 거절한다.

# 무료 티어 실측: GenerateRequestsPerMinutePerProjectPerModel-FreeTier, limit 15.
# 60/15 = 4초가 하한이고 여유를 둔다. 이게 없으면 세 번째 문항부터 전부 429다.
REQUESTS_PER_MINUTE = 15
MIN_INTERVAL = 60.0 / REQUESTS_PER_MINUTE * 1.15
RATE_LIMIT_RETRIES = 3
RETRY_DELAY = re.compile(r"retry in ([0-9.]+)s", re.IGNORECASE)

JSON_RETRIES = 3
_JSON_BLOCK = re.compile(r"\{.*}", re.DOTALL)
_STRICTER = (
    "\n\n직전 응답은 JSON으로 읽히지 않았다. 문자열 안의 큰따옴표는 \\\" 로 이스케이프하고, "
    "줄바꿈을 넣지 마라. JSON 객체 하나만 출력하라."
)


class LlmError(RuntimeError):
    pass


class LlmClient(Protocol):
    def complete(self, system: str, user: str) -> str: ...


class GeminiClient:
    """프로덕션과 같은 엔드포인트·같은 모델. 셀프체크가 의미를 가지려면 같아야 한다."""

    def __init__(self, api_key: str | None = None, model: str = MODEL) -> None:
        from langchain_openai import ChatOpenAI

        key = api_key or os.environ.get("API_KEY")
        if not key:
            raise LlmError("API_KEY가 없다. --dry-run 을 쓰거나 키를 넘겨라")
        self._chat = ChatOpenAI(
            base_url=BASE_URL, api_key=key, model=model, temperature=TEMPERATURE, max_retries=0
        )
        self._last_call = 0.0

    def complete(self, system: str, user: str) -> str:
        for attempt in range(RATE_LIMIT_RETRIES):
            self._throttle()
            try:
                response = self._chat.invoke([("system", system), ("human", user)])
                return str(response.content)
            except Exception as error:
                wait = _retry_after(error)
                if wait is None or attempt == RATE_LIMIT_RETRIES - 1:
                    raise
                print("  429 — %.0f초 대기" % wait, flush=True)
                time.sleep(wait)
        raise LlmError("호출에 실패했다")

    def _throttle(self) -> None:
        elapsed = time.monotonic() - self._last_call
        if elapsed < MIN_INTERVAL:
            time.sleep(MIN_INTERVAL - elapsed)
        self._last_call = time.monotonic()


def _retry_after(error: Exception) -> float | None:
    """429면 서버가 알려준 대기 시간을, 아니면 None을 돌려준다."""
    detail = str(error)
    if "429" not in detail and "RESOURCE_EXHAUSTED" not in detail:
        return None
    match = RETRY_DELAY.search(detail)
    return float(match.group(1)) + 1 if match else 60.0


class OllamaClient:
    """로컬 모델. 쿼터가 없으므로 스로틀도 재시도도 두지 않는다.

    Ollama 가 OpenAI 호환 엔드포인트를 내주므로 어댑터는 base_url 만 갈아끼운다.
    thinking 계열 모델은 <think>...</think> 를 앞에 달고 나오는데, 뒤의 JSON 만 쓰면 되도록
    parse_json 이 본문에서 객체를 찾아낸다.
    """

    def __init__(self, model: str | None = None, host: str = OLLAMA_HOST) -> None:
        from langchain_openai import ChatOpenAI

        model = model or LOCAL_MODEL
        body: dict[str, Any] = {"keep_alive": LOCAL_KEEP_ALIVE}
        if supports_thinking(model, host):
            body["reasoning_effort"] = "none"
        self._chat = ChatOpenAI(
            base_url="%s/v1" % host.rstrip("/"),
            api_key="ollama",
            model=model,
            temperature=TEMPERATURE,
            max_retries=0,
            timeout=LOCAL_TIMEOUT,
            extra_body=body,
        )
        self.model = model

    def complete(self, system: str, user: str) -> str:
        try:
            response = self._chat.invoke([("system", system), ("human", user)])
        except Exception as error:
            raise LlmError(
                "Ollama 호출에 실패했다 - %s. 데몬과 모델(%s)을 확인하라." % (error, self.model)
            ) from error
        return str(response.content)


def supports_thinking(model: str, host: str = OLLAMA_HOST) -> bool:
    """모델이 추론 토큰을 내는지 Ollama에 물어본다. 못 물어보면 안 내는 것으로 본다."""
    request = urllib.request.Request(
        "%s/api/show" % host.rstrip("/"),
        data=json.dumps({"model": model}).encode("utf-8"),
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return "thinking" in (json.loads(response.read()).get("capabilities") or [])
    except (urllib.error.URLError, TimeoutError, ValueError, KeyError):
        return False


class UnusedClient:
    """호출하면 안 되는 자리에 꽂는다. resume 은 LLM을 쓰지 않으므로 키가 필요 없다."""

    def complete(self, system: str, user: str) -> str:
        raise LlmError("이 단계는 LLM을 호출하지 않아야 한다")


class FakeClient:
    """대본대로 돌려준다. 키 하나가 여러 번 불리면 순서대로 소비한다."""

    def __init__(self, script: dict[str, list[str]]) -> None:
        self._script = {key: list(values) for key, values in script.items()}
        self.calls: list[tuple[str, str]] = []

    def complete(self, system: str, user: str) -> str:
        self.calls.append((system, user))
        for key, values in self._script.items():
            if key in system and values:
                return values.pop(0)
        raise LlmError(f"대본에 없는 호출이다 - system 앞부분={system[:60]!r}")


def complete_json(client: LlmClient, system: str, user: str) -> dict[str, Any]:
    """JSON이 깨지면 더 강한 지시를 붙여 다시 묻는다.

    긴 한국어 응답에서 큰따옴표 이스케이프가 빠지는 일이 실제로 있었다.
    한 번 깨졌다고 문항을 통째로 버리는 건 호출 낭비다.
    """
    last: LlmError | None = None
    for attempt in range(JSON_RETRIES):
        prompt = system if attempt == 0 else system + _STRICTER
        try:
            return parse_json(client.complete(prompt, user))
        except LlmError as error:
            last = error
    raise last if last else LlmError("JSON을 얻지 못했다")


def parse_json(text: str) -> dict[str, Any]:
    """모델이 코드펜스나 잡말을 붙여도 첫 JSON 객체만 건져낸다."""
    match = _JSON_BLOCK.search(text)
    if match is None:
        raise LlmError(f"응답에서 JSON을 찾지 못했다 - {text[:120]!r}")
    try:
        parsed = json.loads(match.group())
    except json.JSONDecodeError as error:
        raise LlmError(f"JSON을 읽지 못했다 - {error}") from error
    if not isinstance(parsed, dict):
        raise LlmError("JSON 최상위가 객체가 아니다")
    return parsed
