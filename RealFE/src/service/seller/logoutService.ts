import { sellerApi } from "@/lib/apiClient";

export const requestSellerLogout = async (): Promise<void> => {
    // 이 함수는 API 호출 책임만 가집니다.
    await sellerApi.post('/seller/logout');}