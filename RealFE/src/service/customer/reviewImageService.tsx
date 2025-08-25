import apiClient from '@/lib/apiClient';

// 이미지 업로드 - 파일 여러 개 개별 업로드
// reviewImageService.ts
export async function uploadReviewImages(
  files: File[] | null | undefined,
  reviewId?: number
): Promise<string[]> {
  if (!files || files.length === 0) return [];

  const urls: string[] = [];

  for (const file of files) {
    const formData = new FormData();
    formData.append('file', file);

    try {
      const endpoint = reviewId
        ? `/customer/reviews/images/upload?reviewId=${reviewId}`
        : `/customer/reviews/images/upload`;

      const res = await apiClient.post(endpoint, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      // 응답 구조가 { url: string }일 경우
      const uploadedUrl = res.data.url ?? res.data;
      urls.push(uploadedUrl);
    } catch (error) {
      console.error('이미지 업로드 실패:', error);
    }
  }

  return urls;
}


// 이미지 삭제 - 서버 API에 맞게 URL 또는 ID로 삭제 요청
export async function deleteReviewImage(imageUrl: string): Promise<void> {
  await apiClient.delete('/customer/reviews/images/delete', {
    params: { imageUrl },
  });
}
