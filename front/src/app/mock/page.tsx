"use client";

import { useState } from "react";
import { catStatsData } from "@/mocks/diagnosis";
import PageHeader, { PageBody } from "@/components/layout/PageHeader";
import MockSetup from "@/components/mock/MockSetup";
import MockResult from "@/components/mock/MockResult";

export default function MockPage() {
  const [done, setDone] = useState(false);
  const [cat, setCat] = useState("전체");

  return (
    <main className="flex min-w-0 flex-1 flex-col">
      <PageHeader title="모의 진단" subtitle="정답률과 난이도 기반으로 실력 등급을 확인합니다" />
      <PageBody>
        <div className="flex flex-col gap-[18px]">
          {!done ? (
            <MockSetup cat={cat} onSelectCat={setCat} onStart={() => setDone(true)} />
          ) : (
            <MockResult stats={catStatsData} onRetry={() => setDone(false)} />
          )}
        </div>
      </PageBody>
    </main>
  );
}
