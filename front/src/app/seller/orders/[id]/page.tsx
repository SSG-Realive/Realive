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

    if (checking) return <div className="p-4 sm:p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!delivery) return <div className="p-4">배송 정보를 불러올 수 없습니다.</div>;

    const nextStatusOptions = getNextStatusOptionsFor(delivery.deliveryStatus);
    const isFinalState =
        delivery.deliveryStatus === 'DELIVERY_COMPLETED' || delivery.deliveryStatus === 'CANCELLED';

    return (
        <SellerLayout>
            <div className="max-w-2xl mx-auto p-4 sm:p-6">
                <h1 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900">배송 상세 정보</h1>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
                    <h2 className="text-lg font-semibold mb-4 text-gray-900">주문 정보</h2>
                    <div className="space-y-3">
                        <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">주문 ID:</span>
                            <span className="text-gray-900 font-mono">{delivery.orderId}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">구매자 ID:</span>
                            <span className="text-gray-900">{delivery.buyerId}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:items-start gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">상품명:</span>
                            <span className="text-gray-900 break-words">{delivery.productName}</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
                    <h2 className="text-lg font-semibold mb-4 text-gray-900">배송 정보</h2>
                    <div className="space-y-3">
                        <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">현재 상태:</span>
                            <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                                delivery.deliveryStatus === 'DELIVERY_COMPLETED' 
                                    ? 'bg-green-100 text-green-800'
                                    : delivery.deliveryStatus === 'CANCELLED'
                                    ? 'bg-red-100 text-red-800'
                                    : 'bg-blue-100 text-blue-800'
                            }`}>
                                {delivery.deliveryStatus}
                            </span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">배송 시작일:</span>
                            <span className="text-gray-900">{delivery.startDate ?? '-'}</span>
                        </div>
                        <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                            <span className="text-sm font-medium text-gray-600 min-w-[100px]">배송 완료일:</span>
                            <span className="text-gray-900">{delivery.completeDate ?? '-'}</span>
                        </div>
                    </div>
                </div>

                {nextStatusOptions.length > 0 && (
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
                        <h2 className="text-lg font-semibold mb-4 text-gray-900">배송 상태 변경</h2>
                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    새로운 배송 상태
                                </label>
                                <select
                                    value={newStatus}
                                    onChange={(e) => setNewStatus(e.target.value)}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                >
                                    {nextStatusOptions.map((status) => (
                                        <option key={status} value={status}>
                                            {status}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            {(delivery.deliveryStatus === 'DELIVERY_IN_PROGRESS' || newStatus === 'DELIVERY_IN_PROGRESS') && (
                                <>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            송장 번호
                                        </label>
                                        <input
                                            type="text"
                                            value={trackingNumber}
                                            onChange={(e) => setTrackingNumber(e.target.value)}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                            placeholder="송장 번호를 입력하세요"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">
                                            택배사
                                        </label>
                                        <input
                                            type="text"
                                            value={carrier}
                                            onChange={(e) => setCarrier(e.target.value)}
                                            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                            placeholder="택배사명을 입력하세요"
                                        />
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                )}

                <div className="space-y-3">
                    <button
                        onClick={handleStatusChange}
                        className={`w-full py-3 px-4 rounded-lg font-medium transition-colors ${
                            isFinalState 
                                ? 'bg-gray-400 cursor-not-allowed text-white' 
                                : 'bg-blue-600 hover:bg-blue-700 text-white'
                        }`}
                        disabled={isFinalState}
                    >
                        {delivery.deliveryStatus === 'DELIVERY_COMPLETED'
                            ? '배송 완료됨'
                            : delivery.deliveryStatus === 'CANCELLED'
                            ? '배송 취소됨'
                            : '배송 상태 변경'}
                    </button>

                    {delivery.deliveryStatus === 'INIT' && (
                        <button
                            onClick={handleCancel}
                            className="w-full bg-red-600 hover:bg-red-700 text-white py-3 px-4 rounded-lg font-medium transition-colors"
                        >
                            배송 취소하기
                        </button>
                    )}
                </div>
            </div>
        </SellerLayout>
    );
}
