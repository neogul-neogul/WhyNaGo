// AI 사용 범위 안내 배너
export default function AiNotice() {
  return (
    <div className="flex items-center gap-[11px] rounded-[13px] border border-ai-line bg-ai-bg px-[18px] py-3.5">
      <span className="flex-shrink-0 rounded-[6px] bg-ai px-[9px] py-1 text-[11px] font-bold text-white">
        AI
      </span>
      <span className="text-[13px] leading-[1.5] text-ai-deep">
        AI는 면접 질문과 피드백에만 사용됩니다. 문제 추천·오답 분석·학습 계획에는 사용하지
        않아요. <b>하루 1회</b> 진행할 수 있습니다.
      </span>
    </div>
  );
}
