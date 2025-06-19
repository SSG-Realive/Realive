'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { getDeliveryDetail, updateDeliveryStatus, cancelOrderDelivery } from '@/service/seller/deliveryService';
import { OrderDeliveryDetail } from '@/types/seller/sellerdelivery/sellerDelivery';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerLayout from '@/components/layouts/SellerLayout';
import { DeliveryStatus } from '@/types/seller/sellerorder/sellerOrder';

export default function DeliveryDetailPage() {
    const checking = useSellerAuthGuard();
    const params = useParams();
    const orderId = params?.id as string;

    const [delivery, setDelivery] = useState<OrderDeliveryDetail | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    const [newStatus, setNewStatus] = useState<string>('');
    const [trackingNumber, setTrackingNumber] = useState<string>('');
    const [carrier, setCarrier] = useState<string>('');

    const getNextStatusOptionsFor = (currentStatus: string): string[] => {
        switch (currentStatus) {
            case 'INIT':
                return ['DELIVERY_PREPARING']; // ✅ 취소 포함
            case 'DELIVERY_PREPARING':
                return ['DELIVERY_IN_PROGRESS'];
            case 'DELIVERY_IN_PROGRESS':
                return ['DELIVERY_COMPLETED'];
            default:
                return [];
        }
    };

    useEffect(() => {
        if (checking || !orderId) return;

        const fetchData = async () => {
            try {
                const data = await getDeliveryDetail(Number(orderId));
                setDelivery(data);
                const nextOptions = getNextStatusOptionsFor(data.deliveryStatus);
                setNewStatus(nextOptions.length > 0 ? nextOptions[0] : data.deliveryStatus);
                setTrackingNumber(data.trackingNumber ?? '');
                setCarrier(data.carrier ?? '');
                setError(null);
            } catch (err) {
                console.error('배송 정보 불러오기 실패', err);
                setError('배송 정보를 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [orderId, checking]);

    const handleStatusChange = async () => {
        if (!delivery) return;

        const isStatusChanged = delivery.deliveryStatus !== newStatus;
        const isTrackingChanged = delivery.trackingNumber !== trackingNumber;
        const isCarrierChanged = delivery.carrier !== carrier;

        if (!isStatusChanged && !isTrackingChanged && !isCarrierChanged) {
            alert('변경사항이 없습니다.');
            return;
        }

        try {
            await updateDeliveryStatus(Number(orderId), {
                deliveryStatus: newStatus as DeliveryStatus,
                trackingNumber: isTrackingChanged ? trackingNumber : undefined,
                carrier: isCarrierChanged ? carrier : undefined,
            });

            alert('배송 상태가 변경되었습니다.');
            const updatedData = await getDeliveryDetail(Number(orderId));
            setDelivery(updatedData);
            setNewStatus(updatedData.deliveryStatus);
            setTrackingNumber(updatedData.trackingNumber ?? '');
            setCarrier(updatedData.carrier ?? '');
        } catch (err) {
            console.error('배송 상태 변경 실패', err);
            alert('배송 상태 변경 중 오류 발생');
        }
    };

    const handleCancel = async () => {
        const confirmCancel = confirm('배송을 정말 취소하시겠습니까?');
        if (!confirmCancel) return;

        try {
            await cancelOrderDelivery(Number(orderId));
            alert('배송이 취소되었습니다.');
            location.reload();
        } catch (err) {
            console.error('배송 취소 실패', err);
            alert('배송 취소 중 오류 발생');
        }
    };

    if (checking) return <div className="p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!delivery) return <div className="p-4">배송 정보를 불러올 수 없습니다.</div>;

    const nextStatusOptions = getNextStatusOptionsFor(delivery.deliveryStatus);
    const isFinalState =
        delivery.deliveryStatus === 'DELIVERY_COMPLETED' || delivery.deliveryStatus === 'CANCELLED';

    return (
        <SellerLayout>
            <div className="max-w-xl mx-auto p-4">
                <h1 className="text-xl font-bold mb-4">배송 상세 정보</h1>

                <div className="mb-4"><strong>주문 ID:</strong> {delivery.orderId}</div>
                <div className="mb-4"><strong>구매자 ID:</strong> {delivery.buyerId}</div>
                <div className="mb-4"><strong>상품명:</strong> {delivery.productName}</div>
                <div className="mb-4"><strong>현재 배송 상태:</strong> {delivery.deliveryStatus}</div>
                <div className="mb-4"><strong>배송 시작일:</strong> {delivery.startDate ?? '-'}</div>
                <div className="mb-4"><strong>배송 완료일:</strong> {delivery.completeDate ?? '-'}</div>

                {nextStatusOptions.length > 0 && (
                    <div className="mb-4">
                        <label>배송 상태 변경:</label>
                        <select
                            value={newStatus}
                            onChange={(e) => setNewStatus(e.target.value)}
                            className="w-full p-2 border mt-1"
                        >
                            {nextStatusOptions.map((status) => (
                                <option key={status} value={status}>
                                    {status}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                {(delivery.deliveryStatus === 'DELIVERY_IN_PROGRESS' || newStatus === 'DELIVERY_IN_PROGRESS') && (
                    <>
                        <div className="mb-4">
                            <label>송장 번호:</label>
                            <input
                                type="text"
                                value={trackingNumber}
                                onChange={(e) => setTrackingNumber(e.target.value)}
                                className="w-full p-2 border mt-1"
                            />
                        </div>
                        <div className="mb-4">
                            <label>택배사:</label>
                            <input
                                type="text"
                                value={carrier}
                                onChange={(e) => setCarrier(e.target.value)}
                                className="w-full p-2 border mt-1"
                            />
                        </div>
                    </>
                )}

                {/* 🚩 상태 변경 버튼 */}
                <button
                    onClick={handleStatusChange}
                    className={`w-full py-2 ${isFinalState ? 'bg-gray-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'} text-white`}
                    disabled={isFinalState}
                >
                    {delivery.deliveryStatus === 'DELIVERY_COMPLETED'
                        ? '배송 완료됨'
                        : delivery.deliveryStatus === 'CANCELLED'
                        ? '배송 취소됨'
                        : '배송 상태 변경'}
                </button>

                {/* 🚩 배송 취소 버튼 (INIT 전용) */}
                {delivery.deliveryStatus === 'INIT' && (
                    <button
                        onClick={handleCancel}
                        className="w-full mt-4 bg-red-600 text-white py-2 hover:bg-red-700"
                    >
                        배송 취소하기
                    </button>
                )}
            </div>
        </SellerLayout>
    );
}
