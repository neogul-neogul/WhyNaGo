# Handoff: 맞춤 문제 추천 (취약 태그 기반 문제 생성)

## Overview
데브루틴에 **맞춤 문제 추천** 화면을 추가했습니다. 사용자의 취약 태그를 보여주고, 그 중 한 태그에서 문제 1개를 생성해 바로 풀게 하는 흐름입니다.

- 오늘의 학습(홈) 화면에 추천 페이지 진입 카드 추가
- 추천 페이지: 취약 태그 목록 + `취약점 기반 문제 생성` 버튼
- 버튼 클릭 → 취약 태그 카드가 사라지고 **로딩 카드**(단계 문구 3회 갱신) → **생성된 문제 카드**

**이번 핸드오프는 위 범위만 다룹니다.** 다른 화면(문제 풀이, 오답노트, 1일 1면접, 모의 진단, 문제집, 기록/진척도/설정)은 변경 사항 없습니다.

## About the Design Files
번들의 `개발자 학습 플랫폼.dc.html`은 **HTML 디자인 레퍼런스**입니다. 의도한 화면과 동작을 보여주는 프로토타입이며 프로덕션 코드가 아닙니다. 기존 코드베이스의 패턴·컴포넌트로 재구현하세요.

## Fidelity
**High-fidelity.** 색상·타이포·간격은 최종값입니다. 기존 디자인 토큰에 매핑해 재현하세요.

## Routing / Navigation
- 새 뷰 키: `recommend`
- 페이지 헤더 제목: `맞춤 문제 추천`, 서브: `취약한 영역에서 한 문제를 무작위로 생성해 드립니다`
- 헤더 네비게이션 탭은 **추가하지 않음**. 진입은 홈 카드에서만.

### 홈(오늘의 학습) 진입 카드 (변경)
`오늘 완료 가능한 학습` 섹션 **바로 위**에 카드 1개 추가. 카드 전체가 클릭 영역 → `recommend` 이동.

- 컨테이너: `background:#fff; border:1px solid #ECECE8; border-radius:16px; padding:20px 24px; display:flex; align-items:center; gap:18px`, hover `border-color:#1C1C1A` (`transition:border-color .15s`)
- 좌측 아이콘 박스: 46×46, `border-radius:12px; background:#EEF0FF`, 안에 별(sparkle) 아이콘 22×22 stroke `#4F46E5` stroke-width 1.8
  - path: `M12 3l2.2 5.3 5.8.5-4.4 3.8 1.3 5.6L12 15.4 7.1 18.2l1.3-5.6L4 8.8l5.8-.5z`
- 제목 행: `맞춤 문제 추천` 15.5px/600 + 배지 `취약 #TCP/IP`(= 최취약 태그) 10.5px/700, `color:#C2410C; background:#FEF2E8; padding:2px 7px; border-radius:5px`
- 서브: `내 취약점을 확인하고, 그 영역에서 한 문제를 생성해 바로 풀어보세요` 12.5px `#9A9A90`
- 우측: 텍스트 `추천 보기` 13.5px/600 + chevron 18×18 stroke `#C8C8C0`

## Screen: 맞춤 문제 추천
페이지 본문은 `display:flex; flex-direction:column; gap:20px`. 상태는 하나만 렌더링됩니다.

### 상태 머신
| 상태 | 표시 | 전환 |
|---|---|---|
| `idle` | 취약 태그 카드(+생성 버튼) | 버튼 클릭 → `loading` |
| `loading` | 로딩 카드만 | 생성 완료 → `done` |
| `done` | 생성된 문제 카드만 | `다른 문제 생성` → `loading` |

- `loading` / `done` 상태에서 취약 태그 카드는 **표시하지 않습니다.**
- 재생성 시 이전 문제 카드는 즉시 사라지고 로딩 카드로 대체됩니다.
- 로딩 중 버튼 재클릭은 무시(중복 요청 방지).

### 1. 취약 태그 카드 (`idle`)
컨테이너: `background:#fff; border:1px solid #ECECE8; border-radius:16px; padding:24px 26px; display:flex; flex-direction:column; gap:18px`

헤더 행 (`display:flex; align-items:baseline; justify-content:space-between; gap:16px`)
- 좌: `취약 태그` 15.5px/600
- 우: `최근 30일 · 128문제 기준` 12.5px `#9A9A90` (집계 기간·표본 수는 API 값으로 대체)

태그 행 목록 (`display:flex; flex-direction:column; gap:13px`), 각 행은 `display:flex; align-items:center; gap:16px`
1. 태그 pill — mono 13px/600, `color:#4F46E5; background:#EEF0FF; padding:4px 10px; border-radius:20px; width:112px; text-align:center; flex-shrink:0`
2. 정답률 바 — 트랙 `flex:1; height:8px; border-radius:20px; background:#F1F1EC; overflow:hidden`, 채움 `width:{정답률}%`, `border-radius:20px`
   - 채움 색: 정답률 60% 미만 `#DC7A5A`, 60% 이상 `#D9A34A`
3. 정답률 텍스트 — mono 12.5px `#6B6B62`, `width:42px; text-align:right`
4. 오답 수 — `오답 N문제` 12px `#9A9A90`, `width:96px`

정렬: 정답률 오름차순(가장 취약한 태그가 위). 목록은 4개까지 노출.

안내 문구: `위 취약 태그 중 하나에서 한 문제를 무작위로 생성합니다.` 13px `#8A8A80`, `line-height:1.6`

생성 버튼 (구분선 `border-top:1px solid #F1F1EC; padding-top:18px` 위)
- 풀폭, `background:#1C1C1A; color:#fff; border:none; border-radius:11px; padding:16px; font-size:14.5px; font-weight:600; display:flex; align-items:center; justify-content:center; gap:9px`
- 라벨 `취약점 기반 문제 생성` + 별 아이콘 17×17 stroke-width 1.8 (홈 카드와 동일 path)

### 2. 로딩 카드 (`loading`)
- 컨테이너: `background:#fff; border:1px solid #ECECE8; border-radius:16px; padding:76px 24px; display:flex; flex-direction:column; align-items:center; gap:20px`
- 스피너: 42×42, `border:3px solid #ECECE8; border-top-color:#4F46E5; border-radius:50%`, `animation: spin .8s linear infinite` (0→360deg 회전)
- 제목: `취약점에 맞는 문제를 만들고 있어요` 15px/600
- 단계 문구 13px `#9A9A90` — 순차 갱신
  1. `취약 카테고리 분석 중` (즉시)
  2. `오답 이력에서 개념 추출 중` (+0.9s)
  3. `문제 문장을 작성하는 중` (+1.8s)
  - 프로토타입은 2.7s 후 완료로 고정. **실제로는 생성 API 응답까지 로딩 유지**, 단계 문구는 타이머로 순환시키고 마지막 문구는 응답 전까지 유지.
  - 실패 시 처리(에러 문구 + 재시도)는 이번 범위에 없음 — 기존 에러 패턴을 따르세요.

### 3. 생성된 문제 카드 (`done`)
컨테이너: `background:#fff; border:1px solid #ECECE8; border-radius:16px; padding:28px 30px; display:flex; flex-direction:column; gap:20px`

메타 행 (`display:flex; align-items:center; gap:9px`)
- 난이도 배지: `난이도 상|중|하` 11px/700, `color:#4F46E5; background:#EEF0FF; padding:3px 8px; border-radius:5px`
- 태그 pill: mono 12px/600, `color:#4F46E5; background:#EEF0FF; padding:3px 8px; border-radius:20px` — 생성 근거가 된 취약 태그
- 카테고리 12.5px `#8A8A80` · 구분점 `·` (`color:#DCDCD6`) · 유형(`서술형`/`객관식`) 12.5px `#8A8A80`

본문
- 문제 제목: 21px/600, `line-height:1.45`
- 문제 설명: 14.5px `#4A4A44`, `line-height:1.7`

액션 행 (`display:flex; align-items:center; gap:10px`)
- `이 문제 풀기` — `background:#1C1C1A; color:#fff; border:none; border-radius:10px; padding:13px 26px; font-size:14px; font-weight:600` → 문제 풀이 화면으로 이동(생성된 문제를 현재 문제로 로드)
- `다른 문제 생성` — `background:#fff; color:#1C1C1A; border:1px solid #DCDCD6; border-radius:10px; padding:13px 20px; font-size:14px; font-weight:600`, hover `border-color:#1C1C1A`, 좌측 refresh 아이콘 15×15 stroke-width 2
  - path: `M21 12a9 9 0 11-3.5-7.1` + `M21 3v6h-6`
  - 클릭 → `loading` 재진입, 같은 태그 풀에서 다른 문제 생성(직전 문제 반복 지양)

## Data
```
GET  /me/weak-tags          -> { periodDays, sampleCount, tags: [{ tag, accuracy, wrongCount }] }
POST /problems/generate     -> { id, tag, category, type, level, title, body }
```
- `level` 1/2/3 → 표기 `하`/`중`/`상`
- 생성 요청 바디에 대상 태그를 넘기지 않으면 서버가 취약 태그 중 무작위 선택
- 생성된 문제는 저장되어 `이 문제 풀기` 및 이후 기록/오답노트에서 동일 id로 참조

## Out of scope
- 추천 문제 목록(여러 문제 한 번에 추천), 태그별 필터 UI
- 생성 실패/제한(rate limit) UI
- 헤더 네비게이션 탭 추가
