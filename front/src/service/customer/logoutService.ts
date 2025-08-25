import customerApi from "@/lib/apiClient";

export const requestLogout = async () => {
    // Access Token은 apiClient 인터셉터가 헤더에 자동으로 추가해줍니다.
    await customerApi.post('/customer/logout');
};