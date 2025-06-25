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
    <aside className={className + " bg-[#f1f1f2] min-h-screen border-r"}>
      <div className="px-6 py-10">
        <h1 className="text-xl font-extrabold mb-10 text-white tracking-tight">판매자센터</h1>
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
                          ? "bg-white border-l-4 border-black text-gray-900 font-semibold"
                          : "text-white hover:bg-gray-200 active:bg-gray-300"
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
