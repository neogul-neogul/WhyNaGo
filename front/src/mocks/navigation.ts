import type { NavItem, ProfileMenuItem } from "@/types";

// 상단 공통 헤더 내비게이션 (더미)
export const navItems: NavItem[] = [
  { key: "home", label: "오늘의 학습", href: "/" },
  { key: "solve", label: "문제 풀이", href: "/solve" },
  { key: "wrong", label: "오답노트", href: "/wrong" },
  { key: "interview", label: "1일 1면접", href: "/interview", badge: "AI" },
  { key: "mock", label: "모의 진단", href: "/mock" },
];

// 프로필 드롭다운 메뉴 (더미)
export const profileMenu: ProfileMenuItem[] = [
  { label: "학습 기록", href: "/records", icon: "records" },
  { label: "진척도", href: "/progress", icon: "progress" },
  { label: "주간 리포트", href: "/weekly", icon: "weekly" },
  { label: "마이페이지", href: "/mypage", icon: "user" },
  { label: "알림 설정", href: "/settings", icon: "settings" },
  { label: "로그아웃", href: "/login", icon: "logout", danger: true, action: "logout" },
];
