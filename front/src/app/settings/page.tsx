"use client";

import { useEffect, useState } from "react";
import type { NotificationSettingResponse } from "@/types";
import { ApiError } from "@/lib/api";
import { fetchNotificationSettings, updateNotificationSettings } from "@/lib/notification";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import NotificationToggles from "@/components/settings/NotificationToggles";
import NotifyTimeCard from "@/components/settings/NotifyTimeCard";
import SendConditions from "@/components/settings/SendConditions";

export default function SettingsPage() {
  const [settings, setSettings] = useState<NotificationSettingResponse | null>(null);
  const [loadError, setLoadError] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchNotificationSettings()
      .then((result) => {
        if (!cancelled) setSettings(result);
      })
      .catch(() => {
        if (!cancelled) setLoadError(true);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const applyChange = async (next: NotificationSettingResponse) => {
    const prev = settings;
    setSettings(next);
    setError(null);
    try {
      const result = await updateNotificationSettings(next);
      setSettings(result);
    } catch (e) {
      setSettings(prev);
      setError(e instanceof ApiError ? e.message : "저장에 실패했습니다. 다시 시도해주세요.");
    }
  };

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="알림 설정" subtitle="학습 지속을 돕는 이메일 알림을 설정합니다" />
      <PageBody>
        <div className="flex max-w-[720px] flex-col gap-[18px]">
          {error && <div className="text-[13px] font-semibold text-danger">{error}</div>}
          {loadError && (
            <div className="text-[13px] font-semibold text-danger">
              알림 설정을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
            </div>
          )}
          {!loadError && !settings && (
            <div className="text-[13px] text-soft">알림 설정을 불러오는 중입니다...</div>
          )}
          {settings && (
            <>
              <NotificationToggles
                settings={settings}
                onToggle={(field) => applyChange({ ...settings, [field]: !settings[field] })}
              />
              <NotifyTimeCard
                remindTime={settings.remindTime}
                onChange={(remindTime) => applyChange({ ...settings, remindTime })}
              />
            </>
          )}
          <SendConditions />
        </div>
      </PageBody>
    </main>
  );
}
