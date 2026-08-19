"use client";

import { useEffect, useState } from "react";
import type { AdminMemberDetailResponse, AdminMemberResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchAdminMemberDetail, formatJoinedDate } from "@/lib/admin";
import { mockMemberMeta } from "@/mocks/admin";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import { TierBadge } from "@/components/admin/AdminBadges";

// 회원 목록 위에 뜨는 회원 상세 모달.
// 닉네임·이메일은 목록 행 값을 그대로 쓰고, 학습 지표는 열릴 때 상세 API로 조회한다.
export default function MemberDetailModal({
  member,
  onClose,
}: {
  member: AdminMemberResponse;
  onClose: () => void;
}) {
  const [detail, setDetail] = useState<AdminMemberDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchAdminMemberDetail(member.id)
      .then((response) => {
        if (!cancelled) setDetail(response);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(e instanceof ApiError ? e.message : "회원 정보를 불러오지 못했습니다.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [member.id]);

  const meta = mockMemberMeta(member.id);
  // 조회 전에는 항목 자리를 유지한 채 값만 비워 둔다
  const metric = (value: number | undefined) => (value === undefined ? "—" : value.toLocaleString());

  return (
    <Modal
      onClose={onClose}
      labelledBy="member-detail-title"
      className="flex w-full max-w-[520px] flex-col overflow-hidden"
    >
      <div className="flex items-start justify-between gap-4 border-b border-line-soft px-[26px] pb-4 pt-5">
        <div className="flex min-w-0 flex-col gap-[7px]">
          <div className="flex items-center gap-2.5">
            <span
              id="member-detail-title"
              className="text-[19px] font-bold tracking-[-0.3px] text-ink"
            >
              {member.nickname}
            </span>
            {/* 티어·점수는 API가 내려주지 않는 목업 값이다 */}
            <TierBadge tier={meta.tier} />
          </div>
          <span className="font-mono text-[13px] font-medium text-secondary">{member.email}</span>
        </div>
        <button
          type="button"
          aria-label="닫기"
          onClick={onClose}
          className="cursor-pointer text-[19px] leading-none text-placeholder transition-colors hover:text-ink"
        >
          ×
        </button>
      </div>

      {error && (
        <div className="border-b border-line-soft px-[26px] py-3 text-[12.5px] font-medium text-danger">
          {error}
        </div>
      )}

      <div className="grid grid-cols-2 gap-x-[22px] gap-y-4 px-[26px] py-[22px]">
        <Metric label="점수" value={meta.score.toLocaleString()} />
        <Metric label="스트릭" value={metric(detail?.streakDays)} unit="일" />
        <Metric label="풀이 횟수" value={metric(detail?.solvedQuestionCount)} unit="문항" />
        <Metric label="면접 횟수" value={metric(detail?.completedInterviewCount)} unit="회" />
        <div className="col-span-2 flex flex-col gap-[5px] border-t border-dashed border-line pt-4">
          <span className="text-xs font-semibold text-soft">가입일</span>
          <span className="font-mono text-sm font-semibold text-ink">
            {formatJoinedDate(member.createdAt)}
          </span>
        </div>
      </div>

      <div className="flex justify-end border-t border-line-soft bg-subtle px-[26px] py-4">
        <Button size="md" onClick={onClose}>
          닫기
        </Button>
      </div>
    </Modal>
  );
}

function Metric({ label, value, unit }: { label: string; value: string; unit?: string }) {
  return (
    <div className="flex flex-col gap-[5px]">
      <span className="text-xs font-semibold text-soft">{label}</span>
      <span className="font-mono text-[19px] font-bold text-ink">
        {value}
        {unit && (
          <span className="ml-0.5 font-sans text-[13px] font-semibold text-placeholder">{unit}</span>
        )}
      </span>
    </div>
  );
}
