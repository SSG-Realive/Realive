"use client";

import Link from "next/link";
import { usePathname  } from "next/navigation";
import { FC, useState } from "react";
import { ChevronDown, ChevronRight } from "lucide-react";

interface MenuItem {
  label: string;
  href: string;
}

interface MenuGroup {
  label: string;
  items: MenuItem[];
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
];

const feedbackGroup: MenuGroup = {
  label: "피드백",
  items: [
    { label: "고객리뷰", href: "/seller/reviews" },
  { label: "고객문의확인", href: "/seller/qna" },
    { label: "관리자문의", href: "/seller/admin-qna" },
  ]
};

const SellerSidebar: FC<SellerSidebarProps & { className?: string }> = ({ onClose, className }) => {
  const pathname = usePathname();
  const currentPath = pathname;
  const [isFeedbackOpen, setIsFeedbackOpen] = useState(false);

  // 피드백 관련 페이지에 있으면 아코디언을 자동으로 열기
  const isFeedbackActive = feedbackGroup.items.some(item => currentPath.startsWith(item.href));
  
  const handleMenuClick = () => {
    if (onClose) {
      onClose();
    }
  };

  const toggleFeedback = () => {
    setIsFeedbackOpen(!isFeedbackOpen);
  };

  return (
    <aside className={className + " bg-[#23272a] min-h-screen border-r border-[#23272a] shadow-lg"}>
      <div className="px-6 pt-8 pb-10">
        <Link href="/seller/dashboard">
          <span className="text-2xl font-extrabold mb-10 text-[#fff] tracking-tight block hover:text-[#a89f91] transition-colors">Realive</span>
        </Link>
        <nav>
          <ul className="space-y-1">
            {/* 기본 메뉴 아이템들 */}
            {menuItems.map((item) => {
              const isActive = currentPath.startsWith(item.href);
              return (
                <li key={item.href}>
                  <Link href={item.href} onClick={handleMenuClick}>
                    <div
                      className={`flex items-center px-5 py-3 rounded-lg font-medium transition-colors duration-200 text-base ${
                        isActive
                          ? "bg-[#393e46] border-l-4 border-[#a89f91] text-[#fff] font-semibold shadow-sm sidebar-active"
                          : "text-[#fff] hover:bg-[#393e46] hover:text-[#a89f91] sidebar-hover"
                      }`}
                      style={
                        isActive
                          ? { boxShadow: '4px 0 12px -4px #ede9e3' }
                          : undefined
                      }
                    >
                      <span>{item.label}</span>
                    </div>
                  </Link>
                </li>
              );
            })}
            
            {/* 피드백 아코디언 메뉴 */}
            <li>
              <button
                onClick={toggleFeedback}
                className={`w-full flex items-center justify-between px-5 py-3 rounded-lg font-medium transition-colors duration-200 text-base ${
                  isFeedbackActive
                    ? "bg-[#393e46] border-l-4 border-[#a89f91] text-[#fff] font-semibold shadow-sm sidebar-active"
                    : "text-[#fff] hover:bg-[#393e46] hover:text-[#a89f91] sidebar-hover"
                }`}
                style={
                  isFeedbackActive
                    ? { boxShadow: '4px 0 12px -4px #ede9e3' }
                    : undefined
                }
              >
                <span>{feedbackGroup.label}</span>
                <ChevronRight 
                  className={`w-4 h-4 transition-transform duration-300 ease-in-out ${
                    (isFeedbackOpen || isFeedbackActive) ? 'rotate-90' : 'rotate-0'
                  }`} 
                />
              </button>
              
              {/* 하위 메뉴들 */}
              <div 
                className={`overflow-hidden transition-all duration-300 ease-in-out ${
                  (isFeedbackOpen || isFeedbackActive) 
                    ? 'max-h-40 opacity-100' 
                    : 'max-h-0 opacity-0'
                }`}
              >
                <ul className="mt-1 ml-4 space-y-1">
                  {feedbackGroup.items.map((item) => {
                    const isActive = currentPath.startsWith(item.href);
                    return (
                      <li key={item.href}>
                        <Link href={item.href} onClick={handleMenuClick}>
                          <div
                            className={`flex items-center px-4 py-2 rounded-lg font-medium transition-colors duration-200 text-sm ${
                              isActive
                                ? "bg-[#4a5568] text-[#a89f91] font-semibold"
                                : "text-[#cbd5e0] hover:bg-[#4a5568] hover:text-[#a89f91]"
                            }`}
                          >
                            <span>{item.label}</span>
                          </div>
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              </div>
            </li>
          </ul>
        </nav>
      </div>
    </aside>
  );
};

export default SellerSidebar;
