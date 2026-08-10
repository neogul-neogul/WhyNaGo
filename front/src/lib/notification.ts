import { apiFetch } from "@/lib/api";
import type { NotificationSettingResponse, UpdateNotificationSettingRequest } from "@/types";

// 알림 설정 도메인 API

/** 알림 시간(HH:mm) → 화면 라벨 */
export const REMIND_TIME_LABELS: Record<string, string> = {
  "08:00": "오전 8시",
  "13:00": "오후 1시",
  "21:00": "오후 9시",
  "23:00": "오후 11시",
};

/** 백엔드 LocalTime은 초 단위까지 내려올 수 있어 앞 5자리(HH:mm)만 비교한다 */
export function remindTimeLabel(remindTime: string): string {
  return REMIND_TIME_LABELS[remindTime.slice(0, 5)] ?? remindTime;
}

/** 화면 라벨 → 알림 시간(HH:mm) */
export function labelToRemindTime(label: string): string | undefined {
  return Object.keys(REMIND_TIME_LABELS).find((time) => REMIND_TIME_LABELS[time] === label);
}

/** 내 알림 설정 조회 */
export function fetchNotificationSettings(): Promise<NotificationSettingResponse> {
  return apiFetch<NotificationSettingResponse>("/api/notification-settings/me");
}

/** 알림 설정 수정 (부분 수정이 아니라 매 요청마다 전체 필드를 보낸다) */
export function updateNotificationSettings(
  request: UpdateNotificationSettingRequest,
): Promise<NotificationSettingResponse> {
  return apiFetch<NotificationSettingResponse>("/api/notification-settings/me", {
    method: "PATCH",
    body: request,
  });
}
