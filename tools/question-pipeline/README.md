# question-pipeline

문항·루브릭 생성 파이프라인. 설계와 측정 결과는 `docs/RUBRIC_PIPELINE.md`에 있다.

흐름은 **태그 → 문제 생성 → 루브릭 생성 → 문제 컨펌 → 문제 출제**다.
**LLM은 로컬 Ollama(`qwen3:8b`)로 돈다.** 문항당 호출은 생성 1회 + 루브릭 1회, 총 2회다.
생성의 입력 축은 카테고리가 아니라 `tags.yml`의 태그이며, 문항에 붙는 태그는
그 목록을 벗어날 수 없다(`validate_tags` 게이트가 목록 밖 태그를 반려하고 다시 만들게 한다).

## 준비

`uv`로 관리한다. 전역 파이썬은 건드리지 않는다 — `.venv`는 이 폴더 안에 생기고 `.gitignore` 대상이다.

```bash
cd tools/question-pipeline
uv sync
```

## 실행

```bash
# 0. 어휘를 확인한다
uv run run.py tags --category DB

# 1. interrupt 지점까지 돌려 리뷰 파일을 만든다
uv run run.py generate --tag 인덱스 --difficulty MEDIUM --count 3 --rag
uv run run.py generate --category DB --count 5 --rag   # 사전 순서대로 태그 자동 선택

# 2. 사람이 읽는다 (컨펌 단계)
uv run run.py review

# 3. .work/review.json 의 verdict 를 approved/rejected 로 바꾸고 재개 → SQL
uv run run.py resume --out ../../src/main/resources/data5-generated.sql
```

`--tag`와 `--category`는 함께 쓸 수 없다. 하나는 반드시 준다.
`--category`를 주면 `tags.yml` 순서대로 태그를 고른다. 한 바퀴를 다 돌기 전에는 같은 태그가 다시 나오지 않는다.
난수를 쓰지 않으므로 같은 인자면 같은 태그 순서가 나온다.

**`--dry-run`을 붙이면 API 호출 0회로 돈다.** 가짜 LLM(`fakes.py`)이 모든 분기를 한 번씩
지나가는 대본을 돌려준다 — 중복 재생성, **목록 밖 태그 반려**, 루브릭 반려, 셀프체크, 승인.
배선을 먼저 확인하고 실제 호출을 쓰라는 뜻이다.

## LLM

기본은 로컬이다. Ollama 데몬과 모델 두 개가 필요하다.

```bash
ollama pull qwen3:8b     # 생성·루브릭
ollama pull bge-m3       # 임베딩 (RAG·중복 게이트)
```

| 플래그 | 무엇 |
| --- | --- |
| (없음) | Ollama `qwen3:8b`. 쿼터 없음 |
| `--model <이름>` | 다른 Ollama 모델 |
| `--gemini` | Gemini API. `--api-key` 또는 환경변수 `API_KEY` |
| `--dry-run` | LLM 호출 0회 |

접속 규약(`/v1`, 더미 api-key, temperature 0.3)은 PR #70의
`application-ai-ollama.yml`과 같다. 모델만 다르다 — 그쪽은 채점 비교용,
여기는 생성용이라 지시 준수가 더 필요하다.

**`reasoning_effort`는 보내지 않는다.** thinking 미지원 모델이 400으로 거절한다.

## 태그 어휘

`tags.yml` 이 폐쇄집합이다. 항목마다 세 가지를 갖는다.

| 필드 | 성격 |
| --- | --- |
| `name` | `question_tag.name` 에 그대로 저장된다. 검증·집계의 축 |

태그당 상한은 없다. 같은 태그로 몇 개를 요청하든 자르지 않고, 몇 개까지 갈리는지는
RAG 주입과 유사도 0.90 게이트가 판정한다.

문항 하나에 붙는 태그는 1~3개이고 **첫 번째는 반드시 주제 태그**이며,
나머지도 같은 카테고리 목록 안에서만 고른다.

## 중복 검사·실측 도구 (파이프라인과 독립)

```bash
uv run -m similarity.duplicates --top 40      # 어휘 중복 후보
uv run -m similarity.duplicates --calibrate   # 어휘 임계값 눈금
uv run -m similarity.embeddings --calibrate   # 임베딩 채택 판정 (Ollama 필요)
uv run replay.py                              # 기록된 문항을 현재 게이트에 재생
uv run stats.py                               # 시드 실측치
```

`similarity.embeddings` 는 로컬 Ollama(`bge-m3`)를 쓴다. 데몬이 떠 있어야 하고,
벡터는 `.work/embeddings.json` 에 캐시된다.

## 테스트

```bash
uv run -m unittest tests.test_pipeline -v
```

52개. **LLM을 쓰지 않는다** — 그래프 전체를 가짜 LLM으로 태우는 테스트까지 포함한다.

## 파일

| 경로 | 층 |
| --- | --- |
| `run.py` | 드라이버 (그래프 바깥). 유일한 진입점 |
| `core/` | 그래프 본체 — `graph.py`(LangGraph 배선) · `nodes.py`(노드) · `state.py` · `prompts.yml` |
| `vocabulary/` | 태그 어휘 — `tags.py`(로딩·폐쇄집합 검사·태그 배분) · `tags.yml` |
| `similarity/` | 중복 판정 — `duplicates.py`(어휘) · `embeddings.py`(의미, Ollama) · `probes.json` |
| `adapters/` | 바깥과 닿는 층 — `llm.py`(Ollama·Gemini) · `seed.py`(시드 파싱) · `sql.py`(산출) |
| `testing/` | `fakes.py` — dry-run 대본 LLM |
| `tests/` | `test_pipeline.py` |
| `stats.py` | 시드 실측 스크립트 |
| `replay.py` | evidence 문항을 현재 중복 게이트에 재생 |
| `evidence/` | 실제 실행 기록. 왜 지금 구조가 됐는지의 근거 |

**`core/`만 LangGraph를 안다.** `adapters/`는 저장소 밖(시드 파일·프로덕션 프롬프트)을 읽고,
`similarity/`와 `vocabulary/`는 순수 계산이라 단독으로 돌려볼 수 있다.

**노드는 LangGraph를 import 하지 않는다.** 그래야 단독으로 돌려볼 수 있고, LangGraph를
걷어내도 코드가 남는다. `review`만 예외이며 `graph.py`에 있다.

## 셀프체크를 왜 뺐나

프로덕션 프롬프트로 답변 세 개를 채점해 보던 단계가 있었다. 5문항에서 실측한 결과
`해설 그대로`는 10점만, `모르겠다`는 0점만 나왔다 — **한 번도 걸러낸 적이 없다.**
남은 `항목 제거`는 3건 중 2건이 입력 생성에 실패해 판정에서 빠졌다.
문항당 LLM 호출 6회 중 3회를 쓰면서 값을 못 했다.

게다가 이 단계의 근거는 "**운영과 같은 모델로** 채점해 본다"였는데, 로컬로 옮기면
그 근거 자체가 사라진다. 루브릭 품질은 결정적 검사(`validate_rubric`)와 사람 컨펌이 본다.
채점 품질을 정말 재려면 파이프라인 안이 아니라 골든셋 하네스로 따로 재는 쪽이 맞다.

## 왜 DB가 아니라 시드 파일을 읽나

운영 DB는 접근 대상이 아니고, 로컬은 `ddl-auto: create-drop`이라 컨테이너를 띄워도
`spring.sql.init`이 시드를 넣은 상태일 뿐이다. **시드가 곧 DB 내용**이라 파일을 읽는 쪽이
같은 데이터에 인프라 없이 도달한다. DB를 읽어야 하면 `seed.load()`만 갈아끼우면 된다.
