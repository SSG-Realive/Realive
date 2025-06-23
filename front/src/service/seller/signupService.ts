import { sellerApi } from "@/lib/apiClient";


export async function signup(formData: FormData) {
  const response = await sellerApi.post('/seller/signup', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}