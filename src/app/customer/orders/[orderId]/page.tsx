import { redirect } from 'next/navigation';
import Image from 'next/image';
import { OrderResponseDTO } from '@/types/orders/orderResponseDTO';
import { cookies } from 'next/headers';

interface PageProps {
    params: {
        orderId: string; // URL 경로에서 오는 주문 ID (예: /orders/123 에서 123)
    };
}

// --- 데이터 페칭 함수 ---
async function fetchOrderDetail(orderId: number): Promise<OrderResponseDTO> {
    // 이 부분을 수정합니다.
    const url = `http://localhost:8080/api/customer/orders/${orderId}`;
    console.log("주문 상세 정보를 가져오는 중:", url);

    // 서버 컴포넌트에서 쿠키를 통해 토큰 가져오기
    const cookieStore = await cookies();
    const token = cookieStore.get('token')?.value;

    const response = await fetch(url, {
        cache: "no-store",
        redirect: 'follow',
        headers: {
            'Authorization': token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json'
        }
    });

    // 2xx 범위가 아닌 다른 응답 (예: 404 Not Found, 500 Internal Server Error) 처리
    if (!response.ok) {
        const errorData = await response.text();
        console.error(`주문 상세 API 오류: ${response.status} - ${errorData}`);
        throw new Error(`주문 상세 정보를 불러오는 데 실패했습니다: ${response.statusText || '알 수 없는 오류'}`);
    }

    // JSON 파싱 전에 Content-Type이 JSON인지 확인
    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
        const textResponse = await response.text();
        console.error("API 응답이 JSON 형식이 아닙니다 (예상치 못한 응답):", textResponse);
        throw new Error("API 응답이 JSON 형식이 아닙니다.");
    }

    return response.json(); // JSON 데이터 파싱 및 반환
}

// --- 주문 상세 페이지 컴포넌트 ---
export default async function OrdersDetailPage({ params }: PageProps) {
    // URL 파라미터에서 orderId 추출
    const orderId = Number(params.orderId);

    // 파라미터에 대한 기본 유효성 검사
    if (isNaN(orderId) || orderId <= 0) {
        return (
            <div className="container mx-auto p-4 text-center text-red-600 font-inter">
                <h1 className="text-3xl font-bold mb-6">오류</h1>
                <p>유효하지 않은 주문 ID입니다.</p>
            </div>
        );
    }

    let orderData: OrderResponseDTO | null = null;
    let error: string | null = null;

    try {
        orderData = await fetchOrderDetail(orderId);
    } catch (err) {
        console.error("주문 상세 정보를 가져오는 데 실패:", err);
        error = err instanceof Error ? err.message : "주문 상세 정보를 불러오는 중 알 수 없는 오류가 발생했습니다.";
    }

    // 오류가 있고 리다이렉트가 발생하지 않은 경우 에러 메시지 표시
    if (error) {
        return (
            <div className="container mx-auto p-4 font-inter">
                <h1 className="text-3xl font-bold mb-6 text-center text-gray-800">
                    주문 상세
                </h1>
                <div
                    className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4"
                    role="alert"
                >
                    <strong className="font-bold">오류: </strong>
                    <span className="block sm:inline">{error}</span>
                </div>
            </div>
        );
    }

    // 주문 데이터가 없으면 (예: 찾을 수 없거나 리다이렉트 후 데이터가 설정되지 않은 경우)
    if (!orderData) {
        return (
            <div className="container mx-auto p-4 text-center text-gray-700 font-inter">
                <h1 className="text-3xl font-bold mb-6">주문 상세</h1>
                <p>주문 정보를 찾을 수 없습니다.</p>
            </div>
        );
    }

    // --- 주문 상세 정보 UI 렌더링 ---
    return (
        <div className="container mx-auto p-4 bg-gray-50 min-h-screen font-inter">
            <h1 className="text-4xl font-extrabold mb-8 text-center text-gray-900 leading-tight">
                주문 상세 정보
            </h1>

            <div className="bg-white shadow-xl rounded-lg p-8 mb-8 border border-gray-200">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700">
                    <div>
                        <p className="text-lg font-semibold mb-2">주문 번호: <span className="font-normal text-blue-600">{orderData.orderId}</span></p>
                        <p className="text-lg font-semibold mb-2">고객 ID: <span className="font-normal">{orderData.customerId}</span></p>
                        <p className="text-lg font-semibold mb-2">배송 주소: <span className="font-normal">{orderData.deliveryAddress}</span></p>
                        <p className="text-lg font-semibold mb-2">총 주문 가격: <span className="font-normal text-green-700">{orderData.totalPrice.toLocaleString()}원</span></p>
                    </div>
                    <div>
                        <p className="text-lg font-semibold mb-2">주문 일시: <span className="font-normal">{new Date(orderData.orderCreatedAt).toLocaleString()}</span></p>
                        <p className="text-lg font-semibold mb-2">최종 업데이트: <span className="font-normal">{new Date(orderData.updatedAt).toLocaleString()}</span></p>
                        <p className="text-lg font-semibold mb-2">결제 방식: <span className="font-normal">{orderData.paymentType}</span></p>
                        <p className="text-lg font-semibold mb-2">배송비: <span className="font-normal">{orderData.deliveryFee.toLocaleString()}원</span></p>
                        <p className="text-lg font-semibold mb-2">수령인: <span className="font-normal">{orderData.receiverName}</span></p>
                        <p className="text-lg font-semibold mb-2">연락처: <span className="font-normal">{orderData.phone}</span></p>
                    </div>
                </div>
                <p className="text-lg font-semibold mt-4">
                    주문 상태:{" "}
                    <span
                        className={`relative inline-block px-4 py-1 font-bold leading-tight rounded-full ${
                            orderData.orderStatus === "ORDER"
                                ? "bg-green-100 text-green-800"
                                : "bg-red-100 text-red-800"
                        }`}
                    >
            {orderData.orderStatus === "ORDER" ? "주문 완료" : orderData.orderStatus === "CANCEL" ? "주문 취소" : orderData.orderStatus}
          </span>
                    <span className="ml-4 text-gray-600">({orderData.deliveryStatus})</span>
                </p>
            </div>

            <div className="bg-white shadow-xl rounded-lg p-8 border border-gray-200">
                <h2 className="text-3xl font-bold mb-6 text-gray-800 border-b pb-3">주문 상품 목록</h2>
                {orderData.orderItems.length === 0 ? (
                    <p className="text-lg text-gray-600 text-center py-4">주문된 상품이 없습니다.</p>
                ) : (
                    <div className="space-y-6">
                        {orderData.orderItems.map((item) => (
                            <div key={item.productId} className="flex flex-col md:flex-row items-center space-y-4 md:space-y-0 md:space-x-6 p-4 border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
                                <div className="flex-shrink-0">
                                    {item.imageUrl && item.imageUrl.trim() !== '' ? (
                                        <Image
                                            src={item.imageUrl}
                                            alt={item.productName}
                                            width={96} // Tailwind 'w-24' corresponds to 96px
                                            height={96} // Tailwind 'h-24' corresponds to 96px
                                            className="rounded-lg object-cover w-24 h-24"
                                            onError={(e) => {
                                                e.currentTarget.src = `https://placehold.co/96x96/aabbcc/ffffff?text=No+Image`;
                                                e.currentTarget.onerror = null; // Prevent infinite loop if fallback also fails
                                            }}
                                        />
                                    ) : (
                                        <div className="flex items-center justify-center bg-gray-200 rounded-lg w-24 h-24 text-gray-500 text-sm">
                                            이미지 없음
                                        </div>
                                    )}
                                </div>
                                <div className="flex-grow text-center md:text-left">
                                    <h3 className="text-xl font-semibold text-gray-800 mb-1">{item.productName}</h3>
                                    <p className="text-lg text-gray-600 mb-1">{item.quantity}개</p>
                                    <p className="text-xl font-bold text-blue-700">{item.price.toLocaleString()}원</p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}