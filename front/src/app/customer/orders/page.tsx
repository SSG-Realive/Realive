// app/orders/page.tsx (useState + useEffect 버전)

'use client';

import React, { useEffect, useState } from 'react'; // useEffect, useState로 변경
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { getOrderList } from '@/service/order/orderService'; // API 함수는 그대로 사용
import { useAuthStore } from '@/store/customer/authStore';
import { Page, Order } from '@/types/customer/order/order'; // 타입 임포트

// 이 페이지를 위한 CSS 파일을 임포트합니다.
import './OrderListPage.css';

export default function OrderListPage() {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    
    const { hydrated, isAuthenticated } = useAuthStore();
    
    // ✨ useQuery 대신 useState로 직접 상태 관리
    const [ordersPage, setOrdersPage] = useState<Page<Order> | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<Error | null>(null);

    const currentPage = Number(searchParams.get('page')) || 0;

    // ✨ useEffect를 사용해 데이터 로딩 로직 구현
    useEffect(() => {
        // 인증 상태가 준비되지 않았으면 API 호출을 하지 않음
        if (!hydrated || !isAuthenticated()) {
            setIsLoading(false); // 로딩 상태는 해제
            return;
        }

        const fetchOrders = async () => {
            setIsLoading(true); // 데이터 로딩 시작
            setError(null);
            try {
                const data = await getOrderList(currentPage);
                setOrdersPage(data);
            } catch (err) {
                setError(err as Error);
                console.error("주문 목록 조회 실패:", err);
            } finally {
                setIsLoading(false); // 데이터 로딩 완료
            }
        };

        fetchOrders();
    }, [currentPage, hydrated, isAuthenticated]); // currentPage나 인증 상태가 바뀔 때마다 실행

    const handlePageChange = (pageNumber: number) => {
        const params = new URLSearchParams(searchParams);
        params.set('page', String(pageNumber));
        router.push(`${pathname}?${params.toString()}`);
    };
    
    // UI 렌더링 부분은 이전과 거의 동일합니다.
    if (isLoading) {
        return <OrderListSkeleton />;
    }

    if (!isAuthenticated()) {
        return (
            <div className="container notice-section">
                 <h2>로그인이 필요합니다</h2>
                 <p>주문 내역을 확인하시려면 로그인해주세요.</p>
                 <button className="button-primary" onClick={() => router.push('/login')}>로그인 페이지로 이동</button>
            </div>
        );
    }
    
    if (error) {
        return (
            <div className="container error-alert">
                <strong>오류 발생</strong>
                <p>주문 목록을 불러오는 중 문제가 발생했습니다: {error.message}</p>
            </div>
        );
    }

    if (!ordersPage || ordersPage.empty) {
        return <div className="container notice-section">주문 내역이 없습니다.</div>;
    }

    return (
        <div className="container order-list-page">
            <h1>주문 내역</h1>
            
            <div className="order-list-container">
                {ordersPage.content.map((order) => (
                    <div key={order.orderId} className="order-card">
                        <div className="order-card-header">
                            <h2 className="order-date">
                                주문일: {new Date(order.orderCreatedAt).toLocaleDateString()}
                            </h2>
                            <span className="order-status">{order.orderStatus}</span>
                        </div>
                        <div className="order-card-content">
                            {order.orderItems.map((item) => (
                                <div key={item.orderItemId} className="order-item">
                                    <div className="item-info">
                                        <p className="item-name">{item.productName}</p>
                                        <p className="item-details">
                                            {item.price.toLocaleString()}원 x {item.quantity}개
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <div className="order-card-footer">
                            <p className="total-price">총 결제 금액: <strong>{order.totalPrice.toLocaleString()}원</strong></p>
                            <button className="button-outline">주문 상세</button>
                        </div>
                    </div>
                ))}
            </div>

            <div className="pagination-container">
                <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={ordersPage.first}
                    className="page-button"
                >
                    이전
                </button>
                {[...Array(ordersPage.totalPages).keys()].map(pageIdx => (
                    <button
                        key={pageIdx}
                        onClick={() => handlePageChange(pageIdx)}
                        className={`page-button ${currentPage === pageIdx ? 'active' : ''}`}
                    >
                        {pageIdx + 1}
                    </button>
                ))}
                <button
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={ordersPage.last}
                    className="page-button"
                >
                    다음
                </button>
            </div>
        </div>
    );
}

// 스켈레톤 UI 컴포넌트 (변경 없음)
function OrderListSkeleton(): React.ReactElement {
    return (
        <div className="container order-list-page">
            <h1>주문 내역</h1>
            <div className="space-y-4">
                {[...Array(3)].map((_, i) => (
                    <div key={i} className="skeleton-card">
                        <div className="skeleton-header">
                            <div className="skeleton-line" style={{ width: '50%', height: '24px' }}></div>
                        </div>
                        <div className="skeleton-content">
                            <div className="skeleton-line" style={{ height: '40px' }}></div>
                            <div className="skeleton-line" style={{ height: '40px' }}></div>
                        </div>
                        <div className="skeleton-footer">
                            <div className="skeleton-line" style={{ width: '30%', height: '20px' }}></div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}