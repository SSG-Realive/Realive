import apiClient from '@/lib/apiClient';

export async function signup(formData: FormData) {
  const response = await apiClient.post('/seller/signup', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}