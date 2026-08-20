import type { MasteryResponse } from "@/types";
import Card from "@/components/ui/Card";
import CategoryMasteryList from "@/components/progress/CategoryMasteryList";

/**
 * 숙련도 대시보드 — 진척도(얼마나 풀었나)와 달리 **얼마나 이해했나**를 보여준다.
 *
 * 두 지표를 한 카드에 합치지 않는 이유는 분모가 다르기 때문이다. 진척도는 문제은행 전체가 분모이고,
 * 숙련도는 내가 답한 것만 분모다. 같은 카드에 두면 같은 축으로 읽힌다.
 */
export default function MasteryDashboard({ mastery }: { mastery: MasteryResponse }) {
  return (
    <Card className="overflow-hidden">
      <div className="border-b border-line-card px-[30px] py-[22px]">
        <div className="text-[11px] font-medium tracking-[0.14em] text-secondary">MY MASTERY</div>
        <div className="mt-1.5 text-[23px] font-bold text-ink">이해도 진단</div>
        <p className="mt-1.5 text-[12.5px] text-secondary">
          답변 내용을 근거로 판정한 주제별 이해 수준입니다
        </p>
      </div>
      <div className="px-[26px] py-[20px]">
        <CategoryMasteryList categories={mastery.categories} />
      </div>
    </Card>
  );
}
