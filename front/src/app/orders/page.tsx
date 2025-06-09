
import { redirect } from 'next/navigation';

interface CustomPageProps {
    // searchParams는 Record<string, string | string[] | undefined> 타입입니다.
    // Next.js는 이 객체를 props로 직접 넘겨줍니다.
    searchParams?: Record<string, string | string[] | undefined>;
}

async function fetchOrders(
    page: number,
    size: number,
    sort: string
): Promise<any> {
    const url = `http://localhost:8080/api/orders?page=${page}&size=${size}&sort=${sort}`;
    console.log("Fetching orders from:", url);

    const response = await fetch(url, {
        cache: "no-store",
        redirect: 'follow', // fetch가 리다이렉션을 자동으로 따라가도록 유지
    });

    // --- 백엔드 401 응답 처리 로직 ---
    // 백엔드에서 401 Unauthorized 또는 403 Forbidden 응답을 보낼 경우
    if (response.status === 401 || response.status === 403) {
        console.warn(`API 요청이 인증/인가 실패 상태 코드 ${response.status}를 반환했습니다. 로그인 페이지로 리다이렉트합니다.`);
        // 이 URL은 Spring Security의 OAuth2 로그인 시작 URL과 일치해야 합니다.
        redirect('http://localhost:8080/oauth2/authorization/kakao');
        return; // 이 함수 실행을 여기서 명시적으로 종료 (매우 중요!)
    }

    // 만약 401/403이 아닌 다른 이유로 로그인 페이지 HTML을 받았다면 (매우 드문 경우)
    // 예를 들어, CORS 문제 등으로 인해 백엔드가 HTML 에러 페이지를 반환하는 경우를 대비
    const isKakaoLoginPageHtml = response.headers.get("content-type")?.includes("text/html") &&
        (response.url.includes("kauth.kakao.com/oauth/authorize") || response.url.includes("accounts.kakao.com/login"));

    if (isKakaoLoginPageHtml) {
        console.warn(`API 요청이 JSON이 아닌 카카오 로그인 페이지 HTML을 최종 응답으로 받았습니다. 최종 URL: ${response.url}. 로그인 페이지로 리다이렉트합니다.`);
        redirect(response.url);
        return; // 이 함수 실행을 여기서 명시적으로 종료
    }

    // 리다이렉션도 아니고, 401/403도 아니고, 응답이 성공적이지 않다면 오류 처리 (5xx 또는 다른 4xx)
    if (!response.ok) {
        const errorData = await response.text();
        console.error(`API Error: ${response.status} - ${errorData}`);
        throw new Error(`주문 데이터를 불러오는 데 실패했습니다: ${response.statusText || '알 수 없는 오류'}`);
    }

    // 응답이 JSON이 아닐 경우를 대비한 최종 검사 (이제 이 부분에 도달하지 않아야 합니다.)
    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
        const textResponse = await response.text();
        console.error("API 응답이 JSON 형식이 아닙니다 (예상치 못한 응답):", textResponse);
        throw new Error("API 응답이 JSON 형식이 아닙니다.");
    }

    return response.json();
}

export default async function OrdersPage({ searchParams }: CustomPageProps) {
    // searchParams 경고를 최소화하기 위한 가장 직접적인 접근 방식입니다.
    // Next.js 13의 서버 컴포넌트에서 searchParams는 이미 "resolved"된 상태로 넘어오므로,
    // 대부분의 경우 직접 접근해도 됩니다. 경고는 내부 추적 시스템에서 오는 경우가 많습니다.
    const pageParam = searchParams?.page;
    const sizeParam = searchParams?.size;
    const sortParam = searchParams?.sort;

    // URL에서 여러 개의 동일한 파라미터가 올 경우 (예: ?page=1&page=2), 첫 번째 값만 사용합니다.
    const pageFromUrl = pageParam ? Number(Array.isArray(pageParam) ? pageParam[0] : pageParam) : 1;
    const sizeFromUrl = sizeParam ? Number(Array.isArray(sizeParam) ? sizeParam[0] : sizeParam) : 10;
    const sortFromUrl = sortParam ? String(Array.isArray(sortParam) ? sortParam[0] : sortParam) : "orderedAt,desc";

    const springPageNumber = pageFromUrl - 1;

    let ordersData: any[] = [];
    let totalElements: number = 0;
    let totalPages: number = 0;
    let error: string | null = null;

    try {
        const data = await fetchOrders(
            springPageNumber,
            sizeFromUrl,
            sortFromUrl
        );
        ordersData = data.content || [];
        totalElements = data.totalElements || 0;
        totalPages = data.totalPages || 0;
    } catch (err) {
        // redirect() 함수가 throw하는 내부 에러를 잡아서 처리 (NEXT_REDIRECT)
        // 이 에러는 실제 애플리케이션 오류가 아니므로 사용자에게 표시할 필요는 없습니다.
        if (err && typeof err === 'object' && 'message' in err && (err as Error).message.includes("NEXT_REDIRECT")) {
            console.log("Next.js redirect 에러가 발생하여 페이지 전환 중단.");
        } else {
            console.error("Failed to fetch orders in OrdersPage:", err);
            error = err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.";
            ordersData = [];
        }
    }

    // `redirect`가 성공적으로 발생했다면 이 return은 실행되지 않습니다.
    // `error`가 설정되었다면 에러 메시지를 표시합니다.
    if (error) {
        return (
            <div className="container mx-auto p-4">
                <h1 className="text-3xl font-bold mb-6 text-center text-gray-800">
                    주문 목록
                </h1>
                <div
                    className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
                    role="alert"
                >
                    <strong className="font-bold">오류: </strong>
                    <span className="block sm:inline">{error}</span>
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-6 text-center text-gray-800">
                주문 목록
            </h1>

            {ordersData.length === 0 && (
                <p className="text-lg text-gray-700 text-center">
                    주문 내역이 없습니다. 로그인하거나 데이터를 추가해주세요.
                </p>
            )}

            {ordersData.length > 0 && (
                <>
                    <p className="text-lg text-gray-700 mb-4">
                        총 주문 건수:{" "}
                        <span className="font-semibold">{totalElements}</span>건,{" "}
                        <span className="font-semibold">{totalPages}</span> 페이지
                    </p>

                    <div className="overflow-x-auto bg-white shadow-md rounded-lg">
                        <table className="min-w-full leading-normal">
                            <thead>
                            <tr>
                                <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    주문 번호
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    회원 이메일
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    총 가격
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    주문 일시
                                </th>
                                <th className="px-5 py-3 border-b-2 border-gray-200 bg-gray-100 text-left text-xs font-semibold text-gray-600 uppercase tracking-wider">
                                    주문 상태
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {ordersData.map((order: any) => (
                                <tr key={order.orderId}>
                                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                        {order.orderId}
                                    </td>
                                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                        {order.memberEmail}
                                    </td>
                                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                        {order.totalPrice.toLocaleString()}원
                                    </td>
                                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                                        {new Date(order.orderedAt).toLocaleString()}
                                    </td>
                                    <td className="px-5 py-5 border-b border-gray-200 bg-white text-sm">
                      <span
                          className={`relative inline-block px-3 py-1 font-semibold leading-tight ${
                              order.orderStatus === "ORDER"
                                  ? "text-green-900"
                                  : "text-red-900"
                          }`}
                      >
                        <span
                            aria-hidden
                            className={`absolute inset-0 opacity-50 rounded-full ${
                                order.orderStatus === "ORDER"
                                    ? "bg-green-200"
                                    : "bg-red-200"
                            }`}
                        ></span>
                        <span className="relative">
                          {order.orderStatus === "ORDER" ? "주문 완료" : "주문 취소"}
                        </span>
                      </span>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이지네이션 컴포넌트 추가 */}
                    <div className="flex justify-center mt-6">
                        <nav className="block">
                            <ul className="flex pl-0 rounded list-none flex-wrap">
                                {Array.from({ length: totalPages }, (_, i) => (
                                    <li key={i}>
                                        <a
                                            href={`/orders?page=${i + 1}&size=${sizeFromUrl}&sort=${sortFromUrl}`}
                                            className={`relative block py-2 px-3 leading-tight bg-white border border-gray-300 text-blue-700 mr-1 hover:bg-gray-200 ${
                                                pageFromUrl === i + 1 ? "bg-blue-500 text-white" : ""
                                            }`}
                                        >
                                            {i + 1}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </nav>
                    </div>
                </>
            )}
        </div>
    );
}
