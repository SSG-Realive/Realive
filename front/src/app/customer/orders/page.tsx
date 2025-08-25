'use client';

import React, {useCallback, useEffect, useState} from 'react';
import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { getOrderList, deleteOrder } from '@/service/order/orderService';
import { useAuthStore } from '@/store/customer/authStore';
import Navbar from '@/components/customer/common/Navbar';
import { Page, Order, OrderItem } from '@/types/customer/order/order';

import './OrderListPage.css';

export default function OrderListPage() {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const orderId = searchParams.get('orderId');
    const sellerId = searchParams.get('sellerId');

    const { hydrated, isAuthenticated } = useAuthStore();

    const [ordersPage, setOrdersPage] = useState<Page<Order> | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [error, setError] = useState<Error | null>(null);

    const [showDeleteModal, setShowDeleteModal] = useState<boolean>(false);
    const [orderToDeleteId, setOrderToDeleteId] = useState<number | null>(null);
    const [isDeleting, setIsDeleting] = useState<boolean>(false);

    const [showErrorModal, setShowErrorModal] = useState<boolean>(false);
    const [errorMessage, setErrorMessage] = useState<string>('');

    const currentPage = Number(searchParams.get('page')) || 0;
    const pageSize = 10;

    const fetchOrders = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await getOrderList(currentPage, pageSize);
            setOrdersPage(data);
        } catch (err) {
            setError(err as Error);
            console.error("주문 목록 조회 실패:", err);
        } finally {
            setIsLoading(false);
        }
    }, [currentPage]);

    useEffect(() => {
        if (!hydrated) return;
        if (!isAuthenticated()) {
            setIsLoading(false);
            return;
        }
        fetchOrders();
    }, [fetchOrders, hydrated, isAuthenticated]);

    const handlePageChange = (pageNumber: number) => {
        const params = new URLSearchParams(searchParams);
        params.set('page', String(pageNumber));
        router.push(`${pathname}?${params.toString()}`);
    };

    const handleDeleteClick = (orderId: number) => {
        setOrderToDeleteId(orderId);
        setShowDeleteModal(true);
    };

    const handleConfirmDelete = async () => {
        if (orderToDeleteId === null) return;
        setIsDeleting(true);
        try {
            await deleteOrder(orderToDeleteId);
            setShowDeleteModal(false);
            setOrderToDeleteId(null);
            await fetchOrders();
            setErrorMessage('주문 내역이 성공적으로 삭제되었습니다.');
            setShowErrorModal(true);
        } catch (err) {
            const msg = (err as Error).message || '알 수 없는 오류가 발생했습니다.';
            console.error("주문 삭제 실패:", err);
            setShowDeleteModal(false);
            setOrderToDeleteId(null);
            setErrorMessage(`삭제 실패: ${msg}`);
            setShowErrorModal(true);
        } finally {
            setIsDeleting(false);
        }
    };

    const handleCancelDelete = () => {
        setShowDeleteModal(false);
        setOrderToDeleteId(null);
    };

    const handleErrorModalClose = () => {
        setShowErrorModal(false);
        setErrorMessage('');
    };

    if (isLoading) {
        return <OrderListSkeleton />;
    }

    if (hydrated && !isAuthenticated()) {
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
        <div>
            <Navbar/>
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
                                <div key={`${order.orderId}-${item.orderItemId}`} className="order-item">
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

                            <div className="order-actions">
                                <button
                                    className="button-outline button-delete"
                                    onClick={() => handleDeleteClick(order.orderId)}
                                    disabled={isDeleting && orderToDeleteId === order.orderId}
                                >
                                    {isDeleting && orderToDeleteId === order.orderId ? '삭제 중...' : '구매내역 삭제'}
                                </button>

                                <button
                                    className="button-outline"
                                    onClick={() => router.push(`/customer/orders/${order.orderId}`)}
                                >
                                    주문 상세
                                </button>
                            </div>
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

            {showDeleteModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2>구매내역 삭제</h2>
                        <p>정말로 이 주문 내역을 삭제하시겠습니까?</p>
                        <div className="modal-actions">
                            <button className="button-outline" onClick={handleCancelDelete} disabled={isDeleting}>
                                취소
                            </button>
                            <button
                                className="button-primary button-delete-confirm"
                                onClick={handleConfirmDelete}
                                disabled={isDeleting}
                            >
                                {isDeleting ? '삭제 중...' : '삭제'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {showErrorModal && (
                <div className="modal-overlay">
                    <div className="modal-content modal-error">
                        <h2>{errorMessage.includes('성공') ? '알림' : '오류 발생'}</h2>
                        <p>{errorMessage}</p>
                        <div className="modal-actions">
                            <button className="button-primary" onClick={handleErrorModalClose}>
                                확인
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
        </div>
    );
}

function OrderListSkeleton(): React.ReactElement {
    return (
        <div className="container order-list-page">
            <h1>주문 내역</h1>
            <div className="space-y-4">
                {[...Array(3)].map((_, i) => (
                    <div key={`skeleton-${i}`} className="skeleton-card">
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
