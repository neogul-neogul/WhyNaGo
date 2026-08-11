import { apiFetch } from "@/lib/api";
import type { NotificationSettingResponse, UpdateNotificationSettingRequest } from "@/types";

// 알림 설정 도메인 API

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
