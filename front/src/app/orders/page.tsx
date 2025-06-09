// src/app/orders/page.tsx

import { redirect } from 'next/navigation';
import { cookies } from 'next/headers';
import { NextRequest } from 'next/server';

// API 요청 함수
async function fetchOrders(
    page: number,
    size: number,
    sort: string
): Promise<any> {
    const url = `http://localhost:8080/api/orders?page=${page}&size=${size}&sort=${sort}`;
    console.log("Fetching orders from:", url);

    const response = await fetch(url, {
        cache: "no-store",
        redirect: "follow",
        credentials: "include",
    });

    const finalUrl = response.url;
    const isKakaoLoginPage =
        finalUrl.includes("kauth.kakao.com/oauth/authorize") ||
        finalUrl.includes("accounts.kakao.com/login");

    if (isKakaoLoginPage) {
        console.warn(`카카오 로그인 페이지로 리디렉션됩니다: ${finalUrl}`);
        redirect(finalUrl); // ✅ 로그인 페이지로 리디렉션
    }

    if (response.status === 401 || response.status === 403) {
        console.warn(`인증 실패: ${response.status}`);
        redirect('http://localhost:8080/oauth2/authorization/kakao');
    }

    if (!response.ok) {
        const text = await response.text();
        throw new Error(`API 에러: ${response.status} - ${text}`);
    }

    const contentType = response.headers.get("content-type") || "";
    if (!contentType.includes("application/json")) {
        const text = await response.text();
        throw new Error(`JSON 형식이 아닙니다: ${text}`);
    }

    return response.json();
}

// 페이지 컴포넌트
export default async function OrdersPage(req: { searchParams: URLSearchParams }) {
    // searchParams 안전하게 가져오기
    const searchParams = req.searchParams;

    const pageParam = searchParams.get("page");
    const sizeParam = searchParams.get("size");
    const sortParam = searchParams.get("sort");

    const pageFromUrl = pageParam ? Number(pageParam) : 1;
    const sizeFromUrl = sizeParam ? Number(sizeParam) : 10;
    const sortFromUrl = sortParam || "orderedAt,desc";

    const springPageNumber = pageFromUrl - 1;

    let ordersData: any[] = [];
    let totalElements = 0;
    let totalPages = 0;
    let error: string | null = null;

    try {
        const data = await fetchOrders(springPageNumber, sizeFromUrl, sortFromUrl);
        ordersData = data.content || [];
        totalElements = data.totalElements || 0;
        totalPages = data.totalPages || 0;
    } catch (err) {
        if (err instanceof Error) {
            error = err.message;
        } else {
            error = "알 수 없는 오류가 발생했습니다.";
        }
    }

    if (error) {
        return (
            <div className="container mx-auto p-6">
                <h1 className="text-3xl font-bold text-red-600 mb-4">에러 발생</h1>
                <p className="text-lg text-gray-800">{error}</p>
            </div>
        );
    }

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-6 text-center text-gray-800">
                주문 목록
            </h1>

            {ordersData.length === 0 ? (
                <p className="text-lg text-gray-600 text-center">
                    주문 내역이 없습니다.
                </p>
            ) : (
                <>
                    <p className="mb-4 text-gray-700">
                        총 주문 건수: <strong>{totalElements}</strong>건, 총 페이지:{" "}
                        <strong>{totalPages}</strong>
                    </p>

                    <div className="overflow-x-auto bg-white shadow rounded">
                        <table className="min-w-full table-auto">
                            <thead>
                            <tr>
                                <th className="px-4 py-2 border">주문 번호</th>
                                <th className="px-4 py-2 border">회원 이메일</th>
                                <th className="px-4 py-2 border">총 가격</th>
                                <th className="px-4 py-2 border">주문 일시</th>
                                <th className="px-4 py-2 border">주문 상태</th>
                            </tr>
                            </thead>
                            <tbody>
                            {ordersData.map((order: any) => (
                                <tr key={order.orderId}>
                                    <td className="px-4 py-2 border">{order.orderId}</td>
                                    <td className="px-4 py-2 border">{order.memberEmail}</td>
                                    <td className="px-4 py-2 border">
                                        {order.totalPrice.toLocaleString()}원
                                    </td>
                                    <td className="px-4 py-2 border">
                                        {new Date(order.orderedAt).toLocaleString()}
                                    </td>
                                    <td className="px-4 py-2 border">
                                        {order.orderStatus === "ORDER" ? "주문 완료" : "주문 취소"}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* 페이지네이션 */}
                    <div className="flex justify-center mt-6">
                        <ul className="flex space-x-2">
                            {Array.from({ length: totalPages }, (_, i) => (
                                <li key={i}>
                                    <a
                                        href={`/orders?page=${i + 1}&size=${sizeFromUrl}&sort=${sortFromUrl}`}
                                        className={`px-3 py-2 border rounded ${
                                            pageFromUrl === i + 1
                                                ? "bg-blue-500 text-white"
                                                : "bg-white text-blue-500 hover:bg-blue-100"
                                        }`}
                                    >
                                        {i + 1}
                                    </a>
                                </li>
                            ))}
                        </ul>
                    </div>
                </>
            )}
        </div>
    );
}
