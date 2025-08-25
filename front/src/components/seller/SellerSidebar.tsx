"use client";

import Link from "next/link";
import { usePathname  } from "next/navigation"; // ✅ 수정된 부분
import { FC } from "react";

interface MenuItem {
  label: string;
  href: string;
}

interface SellerSidebarProps {
  onClose?: () => void;
  isOpen?: boolean;
}

const menuItems: MenuItem[] = [
  { label: "대시보드", href: "/seller/dashboard" },
  { label: "마이페이지", href: "/seller/me" },
  { label: "상품관리", href: "/seller/products" },
  { label: "주문관리", href: "/seller/orders" },
  { label: "정산관리", href: "/seller/settlements" },
  { label: "고객문의확인", href: "/seller/qna" },
];

const SellerSidebar: FC<SellerSidebarProps & { className?: string }> = ({ onClose, className }) => {
  const pathname = usePathname();
  const currentPath = pathname;
  const handleMenuClick = () => {
    if (onClose) {
      onClose();
    }
  };
  return (
    <aside className={className + " bg-[#4b3a2f] min-h-screen border-r border-[#3e2f23] shadow-lg"}>
      <div className="px-6 pt-8 pb-10">
        <Link href="/seller/dashboard">
          <span className="text-2xl font-extrabold mb-10 text-[#e9dec7] tracking-tight block hover:text-[#bfa06a] transition-colors">Realive</span>
        </Link>
        <nav>
          <ul className="space-y-1">
            {menuItems.map((item) => {
              const isActive = currentPath.startsWith(item.href);
              return (
                <li key={item.href}>
                  <Link href={item.href} onClick={handleMenuClick}>
                    <div
                      className={`flex items-center px-5 py-3 rounded-lg font-medium transition-colors duration-200 text-base ${
                        isActive
                          ? "bg-[#bfa06a] border-l-4 border-[#3e2f23] text-[#4b3a2f] font-semibold shadow-sm"
                          : "text-[#e9dec7] hover:bg-[#bfa06a] active:bg-[#4b3a2f] hover:text-[#4b3a2f]"
                      }`}
                    >
                      <span>{item.label}</span>
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

export default SellerSidebar;
