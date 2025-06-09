// src/app/orders/[orderId]/page.tsx

import { redirect } from 'next/navigation';
import Image from 'next/image'; // Next.js Image 컴포넌트를 사용하여 이미지 최적화

interface PageProps {
    params: {
        orderId: string; // URL 경로에서 오는 주문 ID (예: /orders/123 에서 123)
    };
    searchParams?: Record<string, string | string[] | undefined>; // URL 쿼리 파라미터 (customerId용)
}

// --- DTO 인터페이스 (백엔드의 OrderResponseDTO와 일치하도록 정의) ---
// 백엔드의 OrderItemResponseDTO에 맞춰 정의
interface OrderItemResponseDTO {
    productId: number;
    productName: string;
    quantity: number;
    price: number;
    imageUrl?: string; // 상품 썸네일 URL
}

// 백엔드의 OrderResponseDTO에 맞춰 정의
interface OrderResponseDTO {
    orderId: number;
    customerEmail: string; // `memberEmail`이 `customerEmail`로 매핑되었다고 가정
    totalPrice: number;
    orderedAt: string; // ISO 8601 형식의 날짜 문자열
    orderStatus: 'ORDER' | 'CANCEL'; // 가능한 주문 상태 (ORDER, CANCEL)
    totalDeliveryFee: number;
    paymentType: string;
    currentDeliveryStatus: string;
    items: OrderItemResponseDTO[]; // 주문 상품 목록
}

// --- 데이터 페칭 함수 ---
async function fetchOrderDetail(
    orderId: number,
    customerId: number
): Promise<OrderResponseDTO> {
    // 백엔드 API URL 구성 (customerId를 쿼리 파라미터로 포함)
    const url = `http://localhost:8080/api/orders/${orderId}?customerId=${customerId}`;
    console.log("주문 상세 정보를 가져오는 중:", url);

    const response = await fetch(url, {
        cache: "no-store", // 서버 사이드 렌더링 시 항상 최신 데이터 가져오기
        redirect: 'follow', // fetch가 3xx 리다이렉트를 자동으로 따르도록 허용
    });

    // 인증 리다이렉션 처리 (예: Spring Security가 로그인 페이지로 리다이렉트)
    // 백엔드가 401/403을 반환하거나, 최종적으로 카카오 로그인 페이지 HTML을 반환하는 경우
    const isKakaoLoginPage = response.url.includes("kauth.kakao.com/oauth/authorize") || response.url.includes("accounts.kakao.com/login");

    if (response.status === 401 || response.status === 403 || isKakaoLoginPage) {
        console.warn(`API 요청이 인증/인가 실패 상태 코드 ${response.status}를 반환했거나 로그인 페이지로 리다이렉트되었습니다. 최종 URL: ${response.url}.`);
        // 백엔드의 OAuth2 로그인 시작 URL로 리다이렉트
        redirect('http://localhost:8080/oauth2/authorization/kakao');
        // --- 수정된 부분: return; 대신 throw new Error()를 사용 ---
        // redirect()가 내부적으로 에러를 던지므로, 명시적으로 에러를 던져
        // TypeScript에 이 경로가 정상적인 값 반환 경로가 아님을 알립니다.
        throw new Error("NEXT_REDIRECT_INTERRUPTED_FETCH"); // 고유한 메시지로 식별
    }

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
export default async function OrdersDetailPage({ params, searchParams }: PageProps) {
    // URL 파라미터에서 orderId 추출
    const orderId = Number(params.orderId);

    // URL 쿼리 파라미터에서 customerId 추출
    // 중요: 실제 애플리케이션에서는 customerId는 인증된 사용자의 컨텍스트(예: JWT)에서 가져와야 합니다.
    // URL 파라미터로 직접 전달하는 것은 백엔드에서 엄격하게 유효성 검사를 하지 않으면 보안 위험이 있습니다.
    const customerIdParam = searchParams?.customerId;
    const customerId = customerIdParam ? Number(Array.isArray(customerIdParam) ? customerIdParam[0] : customerIdParam) : undefined;

    // 파라미터에 대한 기본 유효성 검사
    if (isNaN(orderId) || orderId <= 0) {
        return (
            <div className="container mx-auto p-4 text-center text-red-600 font-inter">
                <h1 className="text-3xl font-bold mb-6">오류</h1>
                <p>유효하지 않은 주문 ID입니다.</p>
            </div>
        );
    }

    if (customerId === undefined || isNaN(customerId) || customerId <= 0) {
        // customerId가 없거나 유효하지 않은 경우
        // 실제 시나리오에서 customerId가 인증에서 기대된다면, 이는 비인가 접근을 의미할 수 있습니다.
        return (
            <div className="container mx-auto p-4 text-center text-red-600 font-inter">
                <h1 className="text-3xl font-bold mb-6">오류</h1>
                <p>주문 상세 정보를 조회하려면 고객 ID가 필요합니다. (예: /orders/{orderId}?customerId=1)</p>
            </div>
        );
    }

    let orderData: OrderResponseDTO | null = null;
    let error: string | null = null;

    try {
        orderData = await fetchOrderDetail(orderId, customerId);
    } catch (err) {
        // Next.js의 내부 리다이렉트 오류 또는 우리가 던진 에러를 정상적으로 처리
        if (err && typeof err === 'object' && 'message' in err &&
            (err as Error).message.includes("NEXT_REDIRECT") || (err as Error).message.includes("NEXT_REDIRECT_INTERRUPTED_FETCH")) {
            console.log("Next.js 리다이렉트 오류 발생. 페이지 전환 중단.");
            // 리다이렉트가 처리되었으므로 에러 상태를 설정할 필요 없음
        } else {
            console.error("주문 상세 정보를 가져오는 데 실패:", err);
            error = err instanceof Error ? err.message : "주문 상세 정보를 불러오는 중 알 수 없는 오류가 발생했습니다.";
        }
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
                        <p className="text-lg font-semibold mb-2">회원 이메일: <span className="font-normal">{orderData.customerEmail}</span></p>
                        <p className="text-lg font-semibold mb-2">총 주문 가격: <span className="font-normal text-green-700">{orderData.totalPrice.toLocaleString()}원</span></p>
                    </div>
                    <div>
                        <p className="text-lg font-semibold mb-2">주문 일시: <span className="font-normal">{new Date(orderData.orderedAt).toLocaleString()}</span></p>
                        <p className="text-lg font-semibold mb-2">결제 방식: <span className="font-normal">{orderData.paymentType}</span></p>
                        <p className="text-lg font-semibold mb-2">배송비: <span className="font-normal">{orderData.totalDeliveryFee.toLocaleString()}원</span></p>
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
            {orderData.orderStatus === "ORDER" ? "주문 완료" : "주문 취소"}
          </span>
                    <span className="ml-4 text-gray-600">({orderData.currentDeliveryStatus})</span>
                </p>
            </div>

            <div className="bg-white shadow-xl rounded-lg p-8 border border-gray-200">
                <h2 className="text-3xl font-bold mb-6 text-gray-800 border-b pb-3">주문 상품 목록</h2>
                {orderData.items.length === 0 ? (
                    <p className="text-lg text-gray-600 text-center py-4">주문된 상품이 없습니다.</p>
                ) : (
                    <div className="space-y-6">
                        {orderData.items.map((item) => (
                            <div key={item.productId} className="flex flex-col md:flex-row items-center space-y-4 md:space-y-0 md:space-x-6 p-4 border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
                                <div className="flex-shrink-0">
                                    {/* Next.js Image 컴포넌트를 사용하여 이미지 최적화 */}
                                    {item.imageUrl ? (
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
