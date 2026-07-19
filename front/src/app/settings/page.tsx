import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import NotificationToggles from "@/components/settings/NotificationToggles";
import NotifyTimeCard from "@/components/settings/NotifyTimeCard";
import SendConditions from "@/components/settings/SendConditions";

export default function SettingsPage() {
  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="알림 설정" subtitle="학습 지속을 돕는 이메일 알림을 설정합니다" />
      <PageBody>
        <div className="flex max-w-[720px] flex-col gap-[18px]">
          <NotificationToggles />
          <NotifyTimeCard />
          <SendConditions />
        </div>
      </PageBody>
    </main>
  );
}
