"""표준 라이브러리 unittest. 추가 패키지 없이 돈다.

    uv run -m unittest test_pipeline -v
"""

from __future__ import annotations

import re
import tempfile
import pathlib
import unittest

from similarity import duplicates
from testing import fakes
from core import graph as graph_module
from core import nodes
import run
from adapters import seed
from adapters import sql
from vocabulary import tags as tags_module
from adapters.llm import LlmError, parse_json

EXPLANATION = (
    "인덱스는 테이블과 별도로 B+Tree 같은 정렬된 자료구조에 키와 행의 위치를 저장합니다. "
    "그래서 조회할 때 테이블을 처음부터 끝까지 읽지 않고 풀 스캔 없이 탐색 범위를 좁혀 가며 "
    "원하는 행을 로그 시간에 가깝게 찾습니다. 대신 쓰기마다 인덱스를 갱신해야 해서 "
    "INSERT와 UPDATE가 느려지고, 인덱스 자체가 저장 공간을 추가로 차지하며 "
    "카디널리티가 낮은 컬럼에는 효과가 거의 없습니다. "
    "그래서 조회 빈도가 높은 컬럼을 골라 선택적으로 걸어야 합니다."
)


def question(**overrides):
    base = {
        "title": "인덱스가 조회를 빠르게 하는 원리",
        "content": "인덱스가 조회를 빠르게 하는 원리와 남용했을 때의 문제를 설명하시오.",
        "explanation": EXPLANATION,
        "tags": ["인덱스", "B+Tree"],
        "category": "DB",
        "difficulty": "MEDIUM",
    }
    return {**base, **overrides}


def rubric(weights=(4, 3, 2, 1), points=None):
    points = points or [
        "B+Tree 같은 정렬된 자료구조에 키와 행의 위치를 저장한다",
        "풀 스캔 없이 탐색 범위를 좁혀 간다",
        "쓰기마다 인덱스를 갱신해야 해서 INSERT와 UPDATE가 느려진다",
        "카디널리티가 낮은 컬럼에는 효과가 거의 없다",
    ]
    return {
        "criteria": [
            {"point": point, "weight": weight} for point, weight in zip(points, weights)
        ],
        "followupScope": {"allowed": ["B+Tree"], "forbidden": ["버퍼 풀 내부 동작"]},
    }


def deps(existing=None):
    return nodes.Deps.create(
        fakes.ScriptedClient(), existing=existing or [], semantic=fakes.semantic
    )


class SeedTest(unittest.TestCase):
    """시드 파일마다 형식이 다르다. 하나만 읽으면 중복 기준선에 구멍이 난다."""

    def setUp(self):
        self.questions = seed.load()

    def test_시드_파일을_전부_읽는다(self):
        names = [path.name for path in seed.seed_paths()]

        self.assertIn("data.sql", names)
        self.assertIn("data2.sql", names)
        self.assertIn("data4-generated.sql", names, "파이프라인 산출물도 기준선에 들어가야 한다")

    def test_id_컬럼과_다중행_형식을_읽는다(self):
        """id 컬럼이 있고 한 문장에 여러 행이 들어가는 형식. 지금 시드에는 없지만 파서는 남겨 둔다."""
        with tempfile.TemporaryDirectory() as directory:
            path = pathlib.Path(directory) / "data0.sql"
            path.write_text(
                "INSERT INTO question (id, title, content, type, difficulty, category, explanation) VALUES\n"
                "(1, '첫 문항', '발문1', 'ESSAY', 'MEDIUM', 'OS', '해설1'),\n"
                "(2, '둘째 문항', '발문2', 'ESSAY', 'HIGH', 'OS', '해설2');\n",
                encoding="utf-8",
            )

            titles = [question.title for question in seed.load([path])]

        self.assertEqual(titles, ["첫 문항", "둘째 문항"])

    def test_세션변수_형식을_읽는다(self):
        """data2.sql — id 없이 SET @eN = LAST_INSERT_ID() 로 참조한다."""
        tagged = [
            question for question in self.questions
            if question.title == "인덱스가 조회를 빠르게 하는 원리"
        ]

        self.assertEqual(len(tagged), 1)
        self.assertTrue(tagged[0].tags, "세션 변수로 붙은 태그를 읽어야 한다")

    def test_모든_문항이_필수_필드를_갖는다(self):
        for question in self.questions:
            self.assertTrue(question.title)
            self.assertTrue(question.content)
            self.assertIn(question.type, ("ESSAY", "MULTIPLE_CHOICE"))
            self.assertIn(question.difficulty, ("LOW", "MEDIUM", "HIGH"))


class SimilarityTest(unittest.TestCase):
    def test_jaccard가_길이차가_크면_포함관계를_못_잡는다(self):
        """해설에 그대로 있는 문장인데 Jaccard로 재면 임계값 아래로 떨어진다."""
        part = duplicates.ngrams("풀 스캔 없이 탐색 범위를 좁혀 간다")
        whole = duplicates.ngrams(EXPLANATION)

        self.assertLess(duplicates.jaccard(part, whole), nodes.GROUNDING_CUT)
        self.assertGreater(duplicates.containment(part, whole), nodes.GROUNDING_CUT * 3)

    def test_무관한_문장은_포함도가_낮다(self):
        part = duplicates.ngrams("HTTP 캐시 헤더의 우선순위를 짚는다")
        whole = duplicates.ngrams(EXPLANATION)

        self.assertLess(duplicates.containment(part, whole), nodes.GROUNDING_CUT)


class ValidateRubricTest(unittest.TestCase):
    def state(self, **overrides):
        return {"question": question(), "rubric": rubric(), "rubric_failures": [], **overrides}

    def test_정상_루브릭은_통과한다(self):
        result = nodes.validate_rubric(self.state(), deps())

        self.assertEqual(result["last_problems"], [])

    def test_배점_합이_10이_아니면_반려한다(self):
        result = nodes.validate_rubric(self.state(rubric=rubric(weights=(4, 3, 2, 2))), deps())

        self.assertTrue(any("배점 합이 11" in problem for problem in result["last_problems"]))

    def test_항목_수가_범위를_벗어나면_반려한다(self):
        result = nodes.validate_rubric(self.state(rubric=rubric(weights=(6, 4))), deps())

        self.assertTrue(any("채점 항목이 2개" in problem for problem in result["last_problems"]))

    def test_해설에_없는_항목을_반려한다(self):
        invented = rubric(points=[
            "B+Tree 같은 정렬된 자료구조에 키와 행의 위치를 저장한다",
            "풀 스캔 없이 탐색 범위를 좁혀 간다",
            "쓰기마다 인덱스를 갱신해야 해서 INSERT와 UPDATE가 느려진다",
            "샤딩 전략을 상황에 맞게 선택한다",
        ])
        result = nodes.validate_rubric(self.state(rubric=invented), deps())

        self.assertTrue(any("샤딩 전략" in problem for problem in result["last_problems"]))

    def test_꼬리질문_범위가_비면_반려한다(self):
        empty = rubric()
        empty["followupScope"] = {"allowed": [], "forbidden": []}

        result = nodes.validate_rubric(self.state(rubric=empty), deps())

        self.assertEqual(len(
            [p for p in result["last_problems"] if "followupScope" in p]), 2)

    def test_반려_사유가_누적된다(self):
        state = self.state(rubric=rubric(weights=(4, 3, 2, 2)), rubric_failures=["이전 사유"])

        result = nodes.validate_rubric(state, deps())

        self.assertEqual(result["rubric_failures"][0], "이전 사유")
        self.assertGreater(len(result["rubric_failures"]), 1)


class GenerateTest(unittest.TestCase):
    """생성기가 기존 주제를 알아야 한다. 모르면 같은 주제를 반복해 만든다."""

    def existing(self):
        return [
            seed.Question(
                variable="@e1",
                title="인덱스가 조회를 빠르게 하는 원리",
                content="발문",
                type="ESSAY",
                difficulty="MEDIUM",
                category="DB",
                explanation=EXPLANATION,
            ),
            seed.Question(
                variable="@e2",
                title="TCP 흐름 제어",
                content="발문",
                type="ESSAY",
                difficulty="MEDIUM",
                category="NETWORK",
                explanation=EXPLANATION,
            ),
        ]

    def test_같은_카테고리_기존_제목을_프롬프트에_담는다(self):
        client = fakes.ScriptedClient()
        dependencies = nodes.Deps.create(
            client, existing=self.existing(), semantic=fakes.semantic)

        nodes.generate({"category": "DB", "difficulty": "MEDIUM", "tag": "인덱스"}, dependencies)

        system = client.calls[0][0]
        self.assertIn("인덱스가 조회를 빠르게 하는 원리", system)
        self.assertNotIn("TCP 흐름 제어", system, "다른 카테고리는 넣지 않는다")

    def test_같은_실행에서_만든_문항도_담는다(self):
        client = fakes.ScriptedClient()
        dependencies = nodes.Deps.create(
            client, existing=self.existing(), semantic=fakes.semantic)
        dependencies.produced.append(question(title="이번 실행에서 만든 문항"))

        nodes.generate({"category": "DB", "difficulty": "MEDIUM", "tag": "인덱스"}, dependencies)

        self.assertIn("이번 실행에서 만든 문항", client.calls[0][0])


class GenerateRetryTest(unittest.TestCase):
    """목록 밖 태그로 세 번 연속 떨어지는 일이 있었다. 재시도는 주제 태그 하나로 좁힌다."""

    def _system(self, failures):
        dependencies = deps()
        nodes.generate(
            {
                "category": "DB",
                "difficulty": "MEDIUM",
                "tag": "인덱스",
                "generate_failures": failures,
            },
            dependencies,
        )
        return dependencies.llm.calls[0][0]

    def test_목록밖_태그로_실패하면_주제_태그만_쓰라고_한다(self):
        system = self._system([tags_module.OUT_OF_LIST + " - 인덱스 설계"])

        self.assertIn("주제 태그 하나만", system)
        self.assertIn('["인덱스"]', system)

    def test_다른_이유로_실패하면_붙이지_않는다(self):
        system = self._system(["정답 해설이 120자다. 180~500자여야 한다."])

        self.assertNotIn("주제 태그 하나만", system)

    def test_첫_시도에는_붙이지_않는다(self):
        system = self._system([])

        self.assertNotIn("주제 태그 하나만", system)


class DedupTest(unittest.TestCase):
    def existing(self):
        return [
            seed.Question(
                variable="@e1",
                title="인덱스가 조회를 빠르게 하는 원리",
                content="인덱스가 조회를 빠르게 하는 원리와 남용했을 때의 문제를 설명하시오.",
                type="ESSAY",
                difficulty="MEDIUM",
                category="DB",
                explanation=EXPLANATION,
            )
        ]

    def test_기존_문항과_같으면_중복으로_잡는다(self):
        result = nodes.dedup({"question": question()}, deps(self.existing()))

        self.assertTrue(any("중복이다" in problem for problem in result["last_problems"]))

    def test_다른_주제는_통과한다(self):
        other = question(title="트랜잭션 격리수준", content="격리수준 4단계를 설명하시오.")

        result = nodes.dedup({"question": other}, deps(self.existing()))

        self.assertEqual(result["last_problems"], [])

    def test_같은_실행에서_만든_문항도_비교_대상이다(self):
        dependencies = deps()
        dependencies.produced.append(question())

        result = nodes.dedup({"question": question()}, dependencies)

        self.assertTrue(result["last_problems"])

    def test_카테고리가_다르면_비교하지_않는다(self):
        dependencies = deps(self.existing())

        result = nodes.dedup({"question": question(category="NETWORK")}, dependencies)

        self.assertEqual(result["last_problems"], [])

    def test_폐기된_문항도_비교_대상이다(self):
        """NETWORK 배치 회귀 — 폐기를 기준선에서 빼니 다음 문항이 같은 주제를 다시 만들었다."""
        discarded = question(
            category="NETWORK",
            title="쿠키와 세션의 개념과 차이점",
            content="웹 애플리케이션에서 사용자의 상태를 유지하기 위해 사용되는 쿠키와 세션의"
                    " 개념을 각각 설명하고, 두 방식의 주요 차이점을 서술하시오.",
        )
        next_attempt = question(
            category="NETWORK",
            title="쿠키와 세션의 개념과 차이점",
            content="웹 애플리케이션에서 사용자 상태를 유지하기 위해 사용되는 쿠키와 세션의"
                    " 개념을 각각 설명하고, 두 방식의 주요 차이점을 서술하시오.",
        )
        dependencies = deps()
        run._remember(dependencies, {"status": "discarded", "question": discarded})

        result = nodes.dedup({"question": next_attempt}, dependencies)

        self.assertTrue(result["last_problems"], "폐기된 문항과 같은 주제인데 통과했다")


class SqlTest(unittest.TestCase):
    def test_작은따옴표를_이스케이프한다(self):
        rendered = sql.render([{**question(title="Optional's 목적"), "rubric": rubric()}])

        self.assertIn("'Optional''s 목적'", rendered)

    def test_세션_변수_접두사를_분리한다(self):
        rendered = sql.render([{**question(), "rubric": rubric()}])
        statements = "\n".join(
            line for line in rendered.splitlines() if not line.startswith("--")
        )

        self.assertIn("SET @p1 = LAST_INSERT_ID();", rendered)
        self.assertNotIn("@e", statements)

    def test_루브릭을_개행을_살려_넣는다(self):
        rendered = sql.render([{**question(), "rubric": rubric()}])

        self.assertIn('"criteria": [', rendered)
        self.assertIn("(@p1, '인덱스')", rendered)


class ParseJsonTest(unittest.TestCase):
    def test_코드펜스가_붙어도_읽는다(self):
        self.assertEqual(parse_json('```json\n{"score": 8}\n```'), {"score": 8})

    def test_JSON이_없으면_실패한다(self):
        with self.assertRaises(LlmError):
            parse_json("죄송합니다. 만들 수 없습니다.")


class GraphTest(unittest.TestCase):
    """API 0회로 그래프 전체를 태운다."""

    def test_모든_분기를_지나_승인_대기까지_간다(self):
        essays = seed.essays(seed.load())
        client = fakes.ScriptedClient(
            duplicate_of=next(q for q in essays if q.category == "DB"))
        dependencies = nodes.Deps.create(client, existing=essays, semantic=fakes.semantic)
        compiled = graph_module.build(dependencies)

        state = compiled.invoke(
            fakes.sample_state(), {"configurable": {"thread_id": "test-1"}})

        self.assertEqual(client.counts["generate"], 2, "중복이라 한 번 다시 만들어야 한다")
        self.assertEqual(client.counts["rubric"], 2, "배점 합 9라 한 번 반려돼야 한다")
        self.assertEqual(sum(client.counts.values()), 4, "문항당 LLM 호출은 생성·루브릭뿐이다")
        self.assertFalse(state.get("verdict"), "interrupt 지점에서 멈춰야 한다")

    def test_재시도_상한을_넘으면_폐기한다(self):
        essays = seed.essays(seed.load())
        duplicate = next(q for q in essays if q.category == "DB")

        class AlwaysDuplicate(fakes.ScriptedClient):
            def _generate(self, system):
                self._next("generate")
                import json as _json
                return _json.dumps(
                    {
                        "title": duplicate.title,
                        "content": duplicate.content,
                        "explanation": duplicate.explanation,
                        "tags": ["인덱스"],
                    },
                    ensure_ascii=False,
                )

        dependencies = nodes.Deps.create(
            AlwaysDuplicate(), existing=essays, semantic=fakes.semantic)
        compiled = graph_module.build(dependencies)

        state = compiled.invoke(
            fakes.sample_state(), {"configurable": {"thread_id": "test-2"}})

        self.assertEqual(state["verdict"], "discarded")
        self.assertEqual(state["discarded"], graph_module.DISCARDED_DUPLICATE)


class BackfillTest(unittest.TestCase):
    """기존 시드 문항에 루브릭만 붙이는 경로."""

    def _target(self):
        catalog = seed.load()
        return catalog, seed.needs_rubric(catalog)[0]

    def test_루브릭이_있는_문항은_대상에서_빠진다(self):
        catalog = seed.load()

        titles = {question.title for question in seed.needs_rubric(catalog)}

        self.assertNotIn("인덱스가 조회를 빠르게 하는 원리", titles, "data3-rubric.sql이 title로 붙였다")
        self.assertNotIn("데드락의 발생 원인과 해결 방법", titles, "data4-generated.sql이 @p 변수로 붙였다")

    def test_제목이_겹치는_문항은_대상에서_빠진다(self):
        catalog = seed.load()
        ambiguous = seed.ambiguous_titles(catalog)

        for title in ambiguous:
            self.assertNotIn(
                title,
                {question.title for question in seed.needs_rubric(catalog)},
                "title로 UPDATE하면 두 문항을 같이 덮어쓴다",
            )

    def test_생성과_중복검사를_건너뛴다(self):
        catalog, target = self._target()
        client = fakes.ScriptedClient()
        compiled = graph_module.build(nodes.Deps.create(client, existing=seed.essays(catalog)))

        state = compiled.invoke(
            fakes.existing_state(target), {"configurable": {"thread_id": "backfill-1"}}
        )

        self.assertNotIn("generate", client.counts, "문항을 새로 만들면 안 된다")
        self.assertEqual(state["question"]["title"], target.title, "문항이 바뀌면 안 된다")
        self.assertFalse(state.get("verdict"), "interrupt 지점에서 멈춰야 한다")

    def test_해설이_짧아도_반려하지_않는다(self):
        short = question(explanation="짧은 해설입니다.")

        problems = nodes.validate_rubric(
            {"question": short, "rubric": rubric(), "existing": True}, deps()
        )["last_problems"]

        self.assertFalse(
            [problem for problem in problems if "정답 해설이" in problem],
            "길이 규칙은 생성기에 거는 제약이지 기존 문항의 결함이 아니다",
        )

    def test_새_문항은_해설_길이를_계속_본다(self):
        """단, 보는 자리가 validate_question 이다.

        validate_rubric 에 두면 실패가 rubric 노드로 돌아가는데 해설은 안 바뀌므로
        같은 길이로 세 번 떨어지고 폐기된다. generate 로 돌아가야 고칠 기회가 생긴다.
        """
        short = question(explanation="짧은 해설입니다.")
        short["tags"] = ["인덱스"]

        problems = nodes.validate_question({"tag": "인덱스", "question": short}, deps())[
            "last_problems"
        ]

        self.assertTrue([problem for problem in problems if "정답 해설이" in problem])

    def test_해설_길이는_루브릭_결함으로_보지_않는다(self):
        short = question(explanation="짧은 해설입니다.")

        problems = nodes.validate_rubric({"question": short, "rubric": rubric()}, deps())[
            "last_problems"
        ]

        self.assertFalse(
            [problem for problem in problems if "정답 해설이" in problem],
            "여기서 반려하면 rubric 재시도로 돌아가 영영 못 고친다",
        )

    def test_묵은_체크포인트가_새_실행을_오염시키지_않는다(self):
        catalog, target = self._target()
        compiled = graph_module.build(
            nodes.Deps.create(fakes.ScriptedClient(), existing=seed.essays(catalog))
        )
        config = {"configurable": {"thread_id": "backfill-dirty"}}
        compiled.update_state(config, {"last_problems": ["지난 실행의 실패 사유"], "rubric_attempts": 3})

        state = compiled.invoke(fakes.existing_state(target), config)

        self.assertFalse(state.get("verdict"), "지난 실패 사유로 폐기되면 안 된다")

    def test_문항을_넣지_않고_루브릭만_UPDATE_한다(self):
        rendered = sql.render_rubrics([{**question(), "rubric": rubric()}])

        self.assertNotIn("INSERT INTO", rendered)
        self.assertIn("UPDATE question SET rubric = '{", rendered)
        self.assertIn(
            "WHERE type = 'ESSAY' AND title = '인덱스가 조회를 빠르게 하는 원리';", rendered
        )


class TagCatalogTest(unittest.TestCase):
    """tags.yml 이 폐쇄집합으로 서 있는지."""

    def test_카테고리가_Category_enum과_같다(self):
        self.assertEqual(
            set(tags_module.categories()),
            {"DB", "NETWORK", "ALGORITHM", "DATA_STRUCTURE", "OS",
             "DESIGN_PATTERN", "LANGUAGE", "GENERAL_CS"},
        )

    def test_사전과_한_글자도_다르지_않다(self):
        """tags.yml 은 data-tag.sql(원본 docs/TAG.md)에서 생성한다. 손으로 고치면 여기서 걸린다."""
        source = seed.RESOURCES / "data-tag.sql"
        rows = re.findall(
            r"\(\s*'((?:[^']|'')*)'\s*,\s*'(\w+)'\s*\)", source.read_text(encoding="utf-8"))
        expected = {(name.replace("''", "'"), category) for name, category in rows}

        self.assertEqual({(tag.name, tag.category) for tag in tags_module.load()}, expected)

    def test_목록에_없는_태그를_찾으면_막는다(self):
        with self.assertRaises(tags_module.UnknownTag):
            tags_module.find("인덱스 설계")

    def test_한_바퀴_안에서는_같은_태그를_두_번_주지_않는다(self):
        """TAG.md 3-6 회차 내 주 태그 중복 금지."""
        pool = tags_module.by_category("DESIGN_PATTERN")
        picked = tags_module.plan(len(pool), "DESIGN_PATTERN")

        self.assertEqual(len(set(picked)), len(pool))

    def test_plan은_난수를_쓰지_않는다(self):
        self.assertEqual(tags_module.plan(7, "DB"), tags_module.plan(7, "DB"))

    def test_한_바퀴는_모든_태그를_한_번씩_거친다(self):
        pool = tags_module.by_category("OS")

        self.assertEqual({tag.name for tag in tags_module.plan(len(pool), "OS")},
                         {tag.name for tag in pool})


class PromptContractTest(unittest.TestCase):
    """fakes 가 프롬프트를 문자열로 파싱한다. 머리말이 바뀌면 dry-run 이 조용히 망가진다."""

    def test_대본이_찾는_머리말이_프롬프트에_있다(self):
        client = fakes.ScriptedClient()
        nodes.generate({"difficulty": "MEDIUM", "tag": "인덱스"}, deps())
        nodes.generate({"difficulty": "MEDIUM", "tag": "인덱스"}, nodes.Deps.create(client))

        system = client.calls[0][0]
        self.assertIn(fakes.TAG_MARK, system)
        self.assertIn(fakes.DIFFICULTY_MARK, system)


class LocalClientTest(unittest.TestCase):
    """--model 을 안 주면 argparse 가 None 을 넘긴다. 그대로 두면 ChatOpenAI 가 죽는다."""

    def test_모델을_안_주면_기본_모델을_쓴다(self):
        from adapters.llm import LOCAL_MODEL, OllamaClient

        self.assertEqual(OllamaClient(None).model, LOCAL_MODEL)

    def test_모델을_주면_그것을_쓴다(self):
        from adapters.llm import OllamaClient

        self.assertEqual(OllamaClient("gemma3:4b").model, "gemma3:4b")


class ClusterPlanTest(unittest.TestCase):
    """태그를 뭉쳐 뽑는 배분. 같은 태그가 여러 번 나와야 게이트가 시험받는다."""

    def test_요청한_수만큼_나온다(self):
        self.assertEqual(len(tags_module.cluster_plan(10, "DB", per_tag=3)), 10)

    def test_한_태그가_per_tag_를_넘지_않는다(self):
        picked = tags_module.cluster_plan(10, "DB", per_tag=3)
        counts = {tag.name: picked.count(tag) for tag in set(picked)}

        self.assertTrue(all(count <= 3 for count in counts.values()), counts)

    def test_plan_과_달리_같은_태그를_여러_번_준다(self):
        clustered = tags_module.cluster_plan(10, "DB", per_tag=3)
        spread = tags_module.plan(10, "DB")

        self.assertLess(len(set(clustered)), len(set(spread)))
        self.assertEqual(clustered.count(clustered[0]), 3, "per_tag 만큼 몰아 준다")


class TagGateTest(unittest.TestCase):
    """폐쇄집합 게이트. 목록 밖 태그가 붙으면 문항을 다시 만든다."""

    def _state(self, assigned):
        return {
            "tag": "인덱스",
            "question": {
                "tags": assigned,
                "explanation": EXPLANATION,
            },
        }

    def test_목록_안이면_통과한다(self):
        result = nodes.validate_question(self._state(["인덱스", "실행 계획"]), deps())

        self.assertEqual(result["last_problems"], [])

    def test_목록_밖이면_막는다(self):
        result = nodes.validate_question(self._state(["인덱스", "인덱스 설계"]), deps())

        self.assertTrue(result["last_problems"])
        self.assertIn("인덱스 설계", result["last_problems"][0])

    def test_주제_태그가_빠지면_막는다(self):
        result = nodes.validate_question(self._state(["실행 계획"]), deps())

        self.assertTrue(any("주제 태그" in problem for problem in result["last_problems"]))

    def test_다른_카테고리_태그는_막는다(self):
        result = nodes.validate_question(self._state(["인덱스", "데드락"]), deps())

        self.assertTrue(result["last_problems"], "OS의 데드락은 DB 문항에 붙을 수 없다")

    def test_네_개_이상이면_막는다(self):
        result = nodes.validate_question(
            self._state(["인덱스", "실행 계획", "페이징", "JOIN"]), deps()
        )

        self.assertTrue(any("태그가 4개다" in problem for problem in result["last_problems"]))

    def test_제목이_태그_이름_그대로면_막는다(self):
        """585개 시드 중 제목이 사전 태그와 같은 것은 1개뿐이다."""
        state = self._state(["인덱스"])
        state["question"]["title"] = "인덱스"

        result = nodes.validate_question(state, deps())

        self.assertTrue(any("태그 이름 그대로" in p for p in result["last_problems"]))

    def test_실패_사유가_재시도_프롬프트로_넘어간다(self):
        result = nodes.validate_question(self._state(["인덱스 설계"]), deps())

        self.assertTrue(result["generate_failures"], "generate 재시도에 사유가 실려야 한다")


class TagAxisTest(unittest.TestCase):
    """생성 프롬프트가 태그 축으로 도는지."""

    def test_주제_태그와_범위를_프롬프트에_담는다(self):
        client = fakes.ScriptedClient()
        nodes.generate({"difficulty": "MEDIUM", "tag": "인덱스"}, nodes.Deps.create(client, existing=[]))

        system = client.calls[0][0]
        self.assertIn("[주제 태그]", system)

    def test_고를_수_있는_태그만_보여준다(self):
        client = fakes.ScriptedClient()
        nodes.generate({"difficulty": "MEDIUM", "tag": "인덱스"}, nodes.Deps.create(client, existing=[]))

        system = client.calls[0][0]
        self.assertIn("실행 계획", system, "같은 카테고리 태그는 보여준다")
        self.assertNotIn("컨텍스트 스위칭", system, "다른 카테고리 태그는 보여주지 않는다")

    def test_카테고리를_태그에서_끌어온다(self):
        result = nodes.generate(
            {"difficulty": "MEDIUM", "tag": "데드락"},
            nodes.Deps.create(fakes.ScriptedClient(), existing=[]),
        )

        self.assertEqual(result["category"], "OS")
        self.assertEqual(result["question"]["category"], "OS")


class TagPipelineTest(unittest.TestCase):
    """태그 → 생성 → 루브릭 → 컨펌 전체 경로."""

    def test_승인_대기까지_가고_태그가_목록_안이다(self):
        compiled = graph_module.build(nodes.Deps.create(fakes.ScriptedClient(), existing=[]))

        state = compiled.invoke(
            fakes.sample_state("커넥션 풀"), {"configurable": {"thread_id": "tag-1"}}
        )

        self.assertFalse(state.get("verdict"), "interrupt 지점에서 멈춰야 한다")
        self.assertIn("커넥션 풀", state["question"]["tags"])
        self.assertTrue(set(state["question"]["tags"]) <= tags_module.names("DB"))

    def test_목록을_못_벗어나면_폐기하고_사유를_남긴다(self):
        class AlwaysBadTag(fakes.ScriptedClient):
            def _generate(self, system):
                self._next("generate")
                import json as _json
                return _json.dumps(
                    {"title": "제목", "content": "발문", "explanation": EXPLANATION,
                     "tags": ["인덱스", "인덱스 설계"]},
                    ensure_ascii=False,
                )

        compiled = graph_module.build(nodes.Deps.create(AlwaysBadTag(), existing=[]))

        state = compiled.invoke(
            fakes.sample_state("인덱스"), {"configurable": {"thread_id": "tag-2"}}
        )

        self.assertEqual(state["verdict"], "discarded")
        self.assertIn(graph_module.DISCARDED_TAGS, state["discarded"])

    def test_출제_SQL에_태그가_실린다(self):
        rendered = sql.render([{**question(), "tags": ["인덱스", "실행 계획"], "rubric": rubric()}])

        self.assertIn("INSERT INTO question_tag (question_id, name) VALUES", rendered)
        self.assertIn("(@p1, '인덱스')", rendered)
        self.assertIn("(@p1, '실행 계획')", rendered)


if __name__ == "__main__":
    unittest.main()
