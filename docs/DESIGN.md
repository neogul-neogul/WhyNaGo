# DESIGN.md — 프론트엔드 디자인 시스템

왜냐고(WhyNaGo) 프론트엔드의 디자인 시스템 문서. 새 화면/컴포넌트를 만들 때 이 문서를 기준으로 작성하고 리뷰한다.

색상 토큰의 구현은 두 곳에 있으며 항상 같은 값으로 동기화한다.

- **`front/src/app/globals.css`** — Tailwind v4 `@theme` 토큰. 정적 스타일은 여기서 생성되는 유틸리티(`text-ink`, `bg-subtle`, `border-line-card` 등)를 쓴다.
- **`front/src/lib/tokens.ts`** — 같은 팔레트의 TS 상수(`palette`). 클래스로 표현할 수 없는 곳(동적 inline style, SVG 속성, mock 데이터)에서만 쓴다.

## 1. 디자인 원칙

- **차분한 뉴트럴 + 잉크 블랙.** 웜 그레이(`page`) 배경 위에 흰색 카드 서피스를 얹고, 주요 액션과 강조는 잉크 블랙(`ink`)으로 처리한다. 화려한 브랜드 컬러 대신 콘텐츠와 상태 색이 돋보이게 한다.
- **색은 의미가 있을 때만.** 유채색(액센트/AI/성공/위험/경고)은 장식이 아니라 의미(문제 유형, AI, 정답/오답, 난이도)를 전달할 때만 사용한다.
- **숫자는 모노스페이스.** 통계·점수·날짜 등 숫자 데이터는 JetBrains Mono(`font-mono`, tabular numbers)로 표기해 정렬감을 준다.
- **작고 촘촘한 밀도.** 본문 13~15px, 좁은 자간(음수 tracking), 넉넉하지 않은 패딩으로 정보 밀도가 높은 대시보드 느낌을 유지한다.

## 2. 색상 토큰

모든 색은 아래 토큰 안에서 사용한다. 새로운 hex 값을 컴포넌트에 직접 쓰지 않는다. (필요하면 이 문서와 두 구현 파일을 먼저 갱신한다.)

### 2.1 잉크 & 텍스트 위계

| 토큰 | 값 | 용도 |
|---|---|---|
| `ink` | `#1C1C1A` | 제목·본문 강조, 주요 버튼/배너 배경 |
| `ink-hover` | `#333333` | 잉크 버튼 hover |
| `body` | `#3A3A34` | 해설 등 긴 본문 |
| `dim` | `#5A5A52` | 비활성 칩 텍스트, 인용 답변 |
| `secondary` | `#6B6B62` | 보조 텍스트, 보조 버튼 라벨, 표 수치 |
| `muted` | `#8A8A80` | 섹션 라벨, 카드 라벨, 부제 |
| `soft` | `#9A9A90` | 캡션, 비활성 탭, 서브 설명 |
| `placeholder` | `#A8A8A0` | placeholder, 단위, 표 헤더, 다크 배너 본문 |
| `axis` | `#B0B0A6` | 차트 축, 옅은 수치 |
| `icon` | `#C8C8C0` | 화살표/북마크 등 비활성 아이콘, 비활성 버튼 배경 |

### 2.2 뉴트럴 배경·보더

| 토큰 | 값 | 용도 |
|---|---|---|
| `page` | `#F6F6F4` | 페이지 배경 |
| `neutral` | `#F1F1ED` | 글로벌 헤더 배경, 중립 배지/태그 배경, 트랙 |
| `subtle` | `#FAFAF7` | 카드 내부 헤더/해설 박스/hover 배경, 입력 필드 배경 |
| `paper` | `#FCFCFA` | 접힘 영역 등 아주 옅은 배경 |
| `line-strong` | `#DCDCD4` | 보조 버튼 테두리, 토글 off, 스크롤바 |
| `line-input` | `#E0E0DA` | 입력·칩 비활성 테두리 |
| `line` | `#E6E6E0` | 페이지 헤더 하단 구분선, 선택지 기본 테두리 |
| `line-card` | `#ECECE8` | 카드·표 테두리 (가장 많이 쓰임) |
| `line-soft` | `#F0F0EC` | 카드 내부 행 구분선, 차트 그리드 |

### 2.3 의미 색

| 계열 | 토큰 | 용도 |
|---|---|---|
| **액센트 (인디고)** | `accent` `#4F46E5` · `accent-bg` `#EEF0FF` · `accent-faint` `#F5F6FF` · `accent-line` `#C7CCFF` | 객관식 배지, 선택 상태, 활성 문항 카드, 아바타, 링크성 액션 |
| **AI (바이올렛)** | `ai` `#6D28D9` · `ai-deep` `#5B21B6` · `ai-bg` `#F0EDFF` · `ai-line` `#E0D8FF` | 서술형 배지, AI 배지·버튼·배너, NEW 배지 |
| **성공 (그린)** | `success` `#16A34A` · `success-bg` `#F2FBF5` · `success-pale` `#E8F5EE` · `success-bright` `#5DDC8A` · `success-glow` `#34D36B` | 정답 상태, 완료 배지, 모범답안 박스. bright/glow는 다크 배너 전용 |
| **위험 (레드)** | `danger` `#DC2626` · `danger-bg` `#FEF2F2` | 오답 상태, 에러 메시지, 위험 메뉴 |
| **경고 (앰버)** | `warning` `#D97706` · `warning-bg` `#FEF7E8` | 난이도 '중' 배지, C 등급 |
| **오답 분석 (오렌지)** | `alert` `#C2410C` · `alert-bg` `#FEF4F2` · `alert-tint` `#FEF2E8` · `alert-line` `#F6DAD3` · `alert-deep` `#7A342A` | "왜 틀렸나" 박스, AI 피드백 제목, 경고 지표 |
| **스트릭** | `streak` `#EA580C` | 🔥 연속 학습일 카운터 |

다크 배너(잉크 배경) 안에서는: 본문 `placeholder`, 성공 강조 `success-bright`(배지 배경 `success-glow/[0.16]`), 구분선·트랙 `white/[0.12]`.

### 2.4 상태 페어 (전경/배경 세트)

배지·선택지 등 상태 표현은 반드시 전경+배경 페어로 쓴다. 기본 수단은 `Badge` 컴포넌트의 `tone`이다.

- 난이도: 하 → `success` · 중 → `warning` · 상 → `danger`. `front/src/lib/badges.ts`의 `diffTone`(Badge용) / `diffColor`·`diffBg`(inline style용) 사용
- 등급(A/B/C/D): `success` / `accent` / `warning` / `danger` → `gradeColor` 사용
- 풀이 상태: 완료 → `success` · 오답 → `danger` · 안 푼 문제 → `neutral`
- 문제 유형: 객관식 → `accent` · 서술형 → `ai`
- 활성 문항 카드: `border-accent-line` + `bg-accent-faint`

### 2.5 학습 잔디(히트맵)

GitHub 잔디 팔레트를 그대로 사용: `#EBEDF0 → #9BE9A8 → #40C463 → #30A14E → #216E39` (레벨 0~4). `front/src/mocks/records.ts`의 `grassColors` 참조 — 데이터 팔레트이므로 디자인 토큰에 넣지 않는다. 셀은 13×13px, `rounded-[3px]`, 간격 3px.

## 3. 타이포그래피

- **본문 폰트**: Pretendard (CDN dynamic subset, `layout.tsx`에서 로드). fallback: `-apple-system, BlinkMacSystemFont, sans-serif`.
- **숫자/코드 폰트**: JetBrains Mono (weight 500/600/700, `--font-jetbrains-mono`). `.font-mono`에 `font-feature-settings: "tnum"`이 걸려 있어 숫자가 등폭으로 정렬된다. 통계 수치, 날짜, 퍼센트, 카운트에 필수로 사용한다.
- **자간**: 큰 제목에는 음수 tracking을 준다. 21~23px → `-0.4px`, 15~20px → `-0.2px ~ -0.3px`.

크기 스케일 (Tailwind arbitrary value 기준):

| 역할 | 크기 / weight |
|---|---|
| 히어로 수치 | `30~34px` bold, font-mono |
| 배너 제목 | `20~23px` bold |
| 페이지 제목(h1) | `21px` bold |
| 카드 수치 | `24~28px` bold |
| 섹션/질문 제목 | `15~17px` bold·semibold |
| 본문 | `14~14.5px`, 해설은 `leading-[1.65]` 내외 |
| 보조 텍스트·부제 | `13~13.5px` |
| 라벨·캡션 | `12~12.5px` semibold |
| 배지 | `11~12px` bold·semibold |

weight 규칙: 제목·수치 `bold(700)`, 버튼·배지·라벨 `semibold(600)`, 본문 `regular~medium`. 활성/비활성 전환 시 weight를 한 단계 올린다(칩 500→600, 탭 600→700).

## 4. 모서리·그림자·간격

### 4.1 Radius 스케일

| 값 | 용도 |
|---|---|
| `3~6px` | 잔디 셀(3), 키워드 태그·xs 배지(5), sm 배지(6) |
| `8~11px` | sm 버튼(8), 칩·내비 항목(9), md/lg 버튼(10), xl 버튼·인풋(11), 아이콘 박스(9~11) |
| `12~14px` | 문항 카드·해설 박스·입력영역(12), 안내 배너·지표 카드(13~14) |
| `16~20px` | 콘텐츠 카드·표 컨테이너(16), 다크 배너(16~18), 인증 카드(20) |
| `20px(pill)` / `rounded-full` | 필 배지, 아바타, 선택지 번호 원 |

원칙: 컨테이너가 클수록 radius가 크다. 카드 안의 요소는 카드보다 작은 radius를 쓴다.

### 4.2 그림자

그림자는 최소한으로, 떠 있는 요소에만 쓴다.

- 활성 내비 탭: `0 1px 2px rgba(0,0,0,0.06)`
- 드롭다운: `0 12px 32px rgba(0,0,0,0.12)`
- 인증(로그인/회원가입) 카드: `0 20px 50px rgba(0,0,0,0.08)`
- 일반 콘텐츠 카드는 그림자 없이 `border-line-card`만 사용한다.

### 4.3 레이아웃·간격

- 콘텐츠 최대폭 `max-w-[1180px]`, 좌우 패딩 `px-9`. 페이지 본문은 `PageBody`(pt-8 / pb-[60px]) 사용.
- 읽기 중심 콘텐츠는 컨테이너 안에서 다시 폭을 제한한다: 폼 `max-w-[400~720px]`, 리포트 `max-w-[780~900px]`, 표 `max-w-[1000px]`.
- 섹션 간 `gap-[18~22px]`, 카드 그리드 `gap-3~3.5`(12~14px), 카드 내부 패딩 `px-5~7 py-[18~26px]`.
- 풀이 화면은 좌(문제)/우(답안) 분할: 좌 `flex-1`, 우 `flex-[1.15]`, 간격 `gap-[18px]`.

## 5. 레이아웃 컴포넌트

- **글로벌 헤더** (`components/layout/Header.tsx`): sticky, `bg-neutral` + `border-b border-line`. 로고(잉크 박스 + `</>` 모노 글리프) · 중앙 내비(활성 = 흰 배경 + 그림자) · 우측 스트릭/프로필 드롭다운. `/login`·`/signup`에서는 렌더하지 않는다.
- **페이지 헤더** (`components/layout/PageHeader.tsx`): 제목(21px bold) + 부제(13px muted) + 우측 오늘 날짜 박스. 모든 로그인 후 페이지 최상단에 사용.
- **PageBody**: 본문 공통 래퍼. 새 페이지는 `<PageHeader/> + <PageBody>` 조합을 기본 골격으로 한다.
- **페이지 구성 원칙**: 페이지(`page.tsx`)는 데이터(mock) 조합과 화면 흐름 상태만 갖고, 화면 섹션은 `components/<도메인>/` 컴포넌트로 분리한다(예: `today/TodayBanner`, `mock/MockResult`). 한 섹션 안에서만 쓰이는 상태는 그 컴포넌트 내부에 둔다.

## 6. 공용 UI 컴포넌트 (`components/ui/`)

### 6.1 Button — `components/ui/Button.tsx`

`variant` × `size`로 사용한다. 버튼을 새로 하드코딩하지 않는다.

| variant | 스타일 | 용도 |
|---|---|---|
| `primary` (기본) | 잉크 배경 + 흰 텍스트, hover `ink-hover`, disabled `icon` | 주요 액션 |
| `secondary` | 흰 배경 + `line-strong` 테두리 + 잉크 라벨 | 나란한 보조 액션 (다시 풀기 등) |
| `muted` | 흰 배경 + `line-strong` 테두리 + `secondary` 라벨 | 취소/종료 등 소극적 액션 |
| `ai` | 바이올렛 배경 | AI 관련 흐름 전용 |
| `success` | 그린 배경 | 결과 저장 등 완료성 액션 |

| size | 스펙 | 용도 |
|---|---|---|
| `sm` | `rounded-[8px] px-3.5 py-1.5 text-[12.5px]` | 리스트 행 안의 작은 액션 |
| `md` | `rounded-[10px] px-[22px] py-[11px] text-[13.5px]` | 폼 푸터(취소/저장) |
| `lg` (기본) | `rounded-[10px] px-[26px] py-[11px] text-[14px]` | 카드 푸터 액션 |
| `xl` | `rounded-[11px] px-7 py-[13px] text-[15px]` | 페이지 레벨 CTA |

너비 확장은 `className="flex-1"`/`"w-full"`로 조합한다. Tailwind는 충돌 유틸리티의 우선순위를 보장하지 않으므로 **패딩·색을 className으로 덮어쓰지 않는다** (필요하면 variant/size를 추가한다). "돌아가기" 같은 텍스트 버튼은 `text-[13px] font-semibold text-secondary` + 화살표 아이콘으로 직접 작성한다.

### 6.2 Badge — `components/ui/Badge.tsx`

전경/배경 페어가 내장된 상태 배지. `tone`: `accent`(객관식) · `ai`(서술형/AI) · `success` · `danger` · `warning` · `alert` · `neutral` · `ink`. `size`: `sm`(기본, `rounded-[6px] text-xs`) · `xs`(`rounded-[5px] text-[11px] bold` — 본 질문/꼬리질문 라벨). 난이도는 `diffTone()`으로 tone을 얻는다.

### 6.3 Card — `components/ui/Card.tsx`

`rounded-[16px] border-line-card bg-white` 컨테이너. 패딩은 사용처에서 지정한다. 내부 헤더 스트립은 `CardHeader`(`bg-subtle` + 하단 구분선). 13~14px radius의 지표 카드 등은 Card를 쓰지 않고 직접 작성한다.

### 6.4 Chip — `components/ui/Chip.tsx`

필터/선택용 칩. 비활성: 흰 배경 + `line-input` 테두리 + `dim` 텍스트(500). 활성: 잉크 배경 + 흰 텍스트(600). 필터 UI는 반드시 이 컴포넌트를 재사용한다.

### 6.5 StatCard — `components/ui/StatCard.tsx`

라벨 + 수치 지표 카드. 라벨은 `12.5px medium muted`로 고정, 수치 영역은 children으로 구성한다(수치는 `font-mono bold`, 단위는 `placeholder`). 오늘 지표·진척도 지표·마이페이지 통계가 공용으로 사용한다.

### 6.6 Input — `components/ui/Input.tsx`

공통 텍스트 인풋(§7 입력 스펙 내장). 여백(`mb-*`) 등 비충돌 유틸리티만 className으로 추가한다. 폭을 줄일 때는 래퍼 div에 폭을 주고 Input은 `w-full`을 유지한다.

### 6.7 Toggle — `components/ui/Toggle.tsx`

온/오프 스위치 (`success`/`line-strong`). 아이콘 전용 컨트롤이므로 `label`(aria-label)이 필수다.

## 7. 컴포넌트 패턴 (비공용)

- **다크 배너**: `bg-ink text-white rounded-[16~18px]` — 페이지당 최대 1개의 히어로 요약(오늘 상태, 주간 요약)에만. 수치 구분은 `w-px bg-white/[0.12]` 세로선.
- **안내 배너**: 옅은 유채색 배경 + 같은 계열 테두리 (예: AI 안내 `bg-ai-bg border-ai-line`) 또는 `bg-subtle` + 인포 아이콘.
- **지표(stat) 카드**: `rounded-[13~14px]` + 라벨(12.5px `muted`) 위, 수치(24~28px bold font-mono) 아래. 단위는 `13px placeholder`로 baseline 정렬.
- **메뉴 카드(링크)**: 흰 카드 + 좌측 42px 아이콘 박스(항목별 accent 배경) + 우측 chevron. hover 시 `border-ink`.
- **입력**: `rounded-[11px] border-line-input bg-subtle px-[15px] text-sm outline-none placeholder:text-placeholder`, focus 시 `border-ink`. 검색 인풋은 흰 배경 + `rounded-[12px]` + 좌측 돋보기 absolute. textarea는 `rounded-[12px] resize-y` + `font-mono` 글자수 카운터.
- **표 (문제 목록형)**: `Card`(overflow-hidden) + 헤더 행 `bg-subtle text-xs font-semibold text-placeholder` + 데이터 행 `border-b border-line-soft`, hover `bg-subtle`. 고정폭 컬럼은 `w-[..] flex-shrink-0`, 제목 컬럼은 `min-w-0 flex-1 truncate`. 수치 컬럼은 우측 정렬 + font-mono.
- **탭 (풀이 화면)**: `bg-subtle` 스트립 안에 배치. 활성: `border-b-2 border-ink font-bold text-ink`. 비활성: `border-transparent font-semibold text-soft`. 채점 결과는 탭 라벨 옆에 `✓`(success)/`✕`(danger).
- **선택지 (객관식)**: `rounded-[12px] border 1.5px + 번호 원 + 본문`. 상태 색은 `palette`로 inline style 처리 — 기본 `line`/`neutral`, 선택 `accent`/`accent-faint`, 정답 `success`/`success-bg`, 오답 `danger`/`danger-bg` + 우측 "정답"/"오답" 라벨. 채점 후 해설은 `border-t border-dashed border-line` 아래: 정답 해설(`bg-subtle`) → 오답 분석(`bg-alert-bg border-alert-line`, 본문 `alert-deep`) 순.
- **진행바**: 트랙 `h-[7px] rounded-md` + 채움 동일 radius. 다크 배너에서는 트랙 `bg-white/[0.12]`, 채움 `success-bright`.

## 8. 아이콘

- 라이브러리 없이 **인라인 SVG**로 작성한다. `viewBox="0 0 24 24"`, `fill="none"`, `stroke="currentColor"`, `strokeWidth 1.8~2.4`, `strokeLinecap/Linejoin="round"`.
- 렌더 크기 14~21px. 색은 부모의 텍스트 색 토큰(`text-icon`, `text-soft` 등)을 `currentColor`로 상속시킨다. 차트처럼 동적 색이 필요할 때만 `palette` 값을 속성에 직접 넣는다.
- 반복되는 아이콘 세트는 `MenuIcon`/`MenuCardIcon`처럼 name 기반 스위치 컴포넌트로 묶는다.

## 9. 작성 규칙

1. **토큰 준수**: §2에 없는 색을 새로 도입하지 않는다. `text-[#8A8A80]` 같은 hex arbitrary value 금지 — `text-muted`처럼 토큰 유틸리티를 쓴다. 필요하면 이 문서와 `globals.css`/`tokens.ts`를 먼저 갱신한다.
2. **공용 컴포넌트 우선**: 버튼은 `Button`, 배지는 `Badge`, 16px 카드는 `Card`, 필터는 `Chip`을 재사용한다. 난이도/등급 색·라벨은 `front/src/lib/badges.ts`를 쓴다. 같은 패턴을 세 번째로 복붙하게 되면 `components/ui/`로 승격을 검토한다.
3. **Tailwind 우선, 동적 값만 style**: 정적 스타일은 Tailwind 유틸리티로 쓰고, 상태에 따라 바뀌는 색/폭 등 동적 값만 `style` prop을 허용한다. 이때 색은 `palette`에서 가져온다.
4. **상태는 페어로**: 유채색 상태 표현은 항상 전경+배경 페어(§2.4)를 함께 쓴다. 전경색만 바꾸고 배경을 임의 조합하지 않는다.
5. **숫자엔 `font-mono`**: 카운트·퍼센트·날짜·점수 등 숫자 데이터 표기에 빠뜨리지 않는다.
6. **다크 배너는 절제**: 잉크 배경 배너는 페이지당 1개, 핵심 요약에만.
7. **접근성 기본기**: 클릭 요소는 `<button type="button">`/`<Link>`로, 아이콘 전용 버튼에는 `aria-label` 또는 `title`을 붙인다.

## 10. 변경 이력

**2026-07 토큰화** — 컴포넌트에 흩어져 있던 hex 리터럴을 `@theme` 토큰 + `palette` 상수로 이관하고 `Button`/`Badge`/`Card`를 승격했다. 이 과정에서 시각적으로 구분되지 않는 근접 중복 색을 통합했다:

- `#A8A89E` → `placeholder`(#A8A8A0), `#C4C4BC` → `icon`(#C8C8C0), `#B0B0A6`는 `axis`로 유지
- `#D8D8D0`·`#D9D9D2`·`#D4D4CC` → `line-strong`(#DCDCD4), `#F2F2EE` → `line-soft`(#F0F0EC)
- `#F5F5FF` → `accent-faint`(#F5F6FF), `#EBEBE6` → `neutral`(#F1F1ED)
- `#EAF7EF` → `success-bg`/`success-pale`, `#FEECEC` → `danger-bg`(#FEF2F2), `#7A4A38` → `alert-deep`(#7A342A)
- 배지·버튼 크기는 `Badge`/`Button`의 size 스케일로 정규화 (±0.5~1px 수준)
