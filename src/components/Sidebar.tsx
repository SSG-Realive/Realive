"use client";

import Link from "next/link";
import { useRouter } from "next/navigation"; // ✅ 수정된 부분
import { FC } from "react";

interface MenuItem {
  label: string;
  href: string;
}

const menuItems: MenuItem[] = [
  { label: "마이페이지", href: "/seller/me" },
  { label: "상품관리", href: "/seller/products" },
  { label: "주문관리", href: "/seller/orders" },
  { label: "정산관리", href: "/seller/settlements" },
  { label: "고객문의확인", href: "/seller/contacts" },
];

const Sidebar: FC = () => {
  const router = useRouter();
  const currentPath = typeof window !== "undefined" ? window.location.pathname : "";

  return (
    <aside className="w-60 min-h-screen bg-gray-800 text-white flex-shrink-0">
      <div className="px-6 py-8">
        <h1 className="text-2xl font-bold mb-8">판매자</h1>
        <nav>
          <ul className="space-y-2">
            {menuItems.map((item) => {
              const isActive = currentPath.startsWith(item.href);
              return (
                <li key={item.href}>
                  <Link href={item.href}>
                    <div
                      className={`block px-4 py-2 rounded-md transition-colors ${
                        isActive
                          ? "bg-gray-700 text-green-300"
                          : "hover:bg-gray-700 hover:text-green-200"
                      }`}
                    >
                      <span className="text-sm font-medium">{item.label}</span>
                    </div>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </div>
    </aside>
  );
};

export default Sidebar;
