'use client';

import { useEffect, useState, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { fetchReviewDetail, updateReview } from '@/service/customer/reviewService';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import StarRating from '@/components/customer/review/StarRating';
import Modal from '@/components/Modal';
import { uploadReviewImages } from '@/service/customer/reviewImageService';

export default function EditReviewPage() {
  const { id } = useParams();
  const router = useRouter();

  const [review, setReview] = useState<ReviewResponseDTO | null>(null);
  const [content, setContent] = useState('');
  const [rating, setRating] = useState(5);
  const [tempRating, setTempRating] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [existingImages, setExistingImages] = useState<string[]>([]);
  const [newImages, setNewImages] = useState<File[]>([]);
  const [removedImages, setRemovedImages] = useState<string[]>([]);
  const [showSuccess, setShowSuccess] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!id) return;
    fetchReviewDetail(Number(id))
        .then((data) => {
          setReview(data);
          setContent(data.content);
          setRating(data.rating);
          setExistingImages(data.imageUrls ?? []);
        })
        .catch(() => setError('리뷰 정보를 불러올 수 없습니다.'))
        .finally(() => setLoading(false));
  }, [id]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      const arrFiles = Array.from(files);
      setNewImages((prev) => [...prev, ...arrFiles]);
    }
    e.target.value = '';
  };

  const handleRemoveExistingImage = (url: string) => {
    setExistingImages((prev) => prev.filter((img) => img !== url));
    setRemovedImages((prev) => [...prev, url]);
  };

  const handleRemoveNewImage = (file: File) => {
    setNewImages((prev) => prev.filter((f) => f !== file));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !review) return;

    try {
      setLoading(true);
      const uploadedUrls = await uploadReviewImages(newImages, Number(id));
      const filteredExisting = existingImages.filter((url) => !removedImages.includes(url));
      const finalImageUrls = [...filteredExisting, ...uploadedUrls];

      await updateReview(Number(id), {
        content,
        rating,
        imageUrls: finalImageUrls,
      });

      setShowSuccess(true);
      setTimeout(() => {
        router.push(`/customer/mypage/reviews/${id}`);
      }, 2000);
    } catch (err) {
      console.error(err);
      alert('리뷰 수정 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div className="text-red-500">{error}</div>;
  if (!review) return <div>리뷰가 존재하지 않습니다.</div>;

  return (
      <div>
        <Modal
            isOpen={showSuccess}
            onClose={() => setShowSuccess(false)}
            message="리뷰가 성공적으로 수정되었습니다."
            type="success"
            className="bg-black/30 backdrop-blur-sm"
            titleClassName="text-blue-900"
            buttonClassName="bg-blue-600"
            title="리뷰 수정 완료"
        />
        <div className="max-w-2xl mx-auto px-6 py-10 bg-white-50">
          <form onSubmit={handleSubmit} className="space-y-6">
            <StarRating rating={rating} setRating={setRating} setTempRating={setTempRating} />

            <div>
              <label className="block font-light mb-2 text-gray-700">리뷰 내용</label>
              <textarea
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="border border-gray-300 rounded px-4 py-3 w-full min-h-[140px] text-gray-800 focus:outline-none focus:ring-2 focus:ring-teal-400"
                  placeholder="리뷰 내용을 입력하세요."
              />
            </div>

            {/* 이미지 업로드 */}
            {/* 파일 선택 버튼 */}
            <div>
              <label className="block font-light mb-2 text-gray-700">이미지 첨부</label>
              <div className="w-full">
                <label
                    htmlFor="file-upload"
                    className="inline-block px-4 py-2 border border-gray-300 text-gray-700 text-sm font-medium rounded-md cursor-pointer hover:bg-gray-100 transition"
                >
                  파일 선택
                </label>
                <input
                    id="file-upload"
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleFileChange}
                    ref={fileInputRef}
                    className="hidden"
                />
              </div>

              {/* 선택된 새 이미지 파일명 보여주기 */}
              {newImages.length > 0 && (
                  <ul className="mt-2 space-y-1 text-sm text-gray-600">
                    {newImages.map((file, idx) => (
                        <li key={idx}>{file.name}</li>
                    ))}
                  </ul>
              )}
            </div>

            {/* 기존 이미지 미리보기 */}
            {existingImages.length > 0 && (
                <div className="flex flex-wrap gap-3 mt-2">
                  {existingImages.map((url) => (
                      <div key={url} className="relative w-24 h-24 border rounded overflow-hidden">
                        <img src={url} alt="기존 리뷰 이미지" className="object-cover w-full h-full" />
                        <button
                            type="button"
                            onClick={() => handleRemoveExistingImage(url)}
                            className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-800"
                            title="이미지 삭제"
                        >
                          ×
                        </button>
                      </div>
                  ))}
                </div>
            )}

            {/* 새 이미지 미리보기 */}
            {newImages.length > 0 && (
                <div className="flex flex-wrap gap-3 mt-2">
                  {newImages.map((file) => {
                    const objectUrl = URL.createObjectURL(file);
                    const key = file.name + '_' + file.lastModified;
                    return (
                        <div key={key} className="relative w-24 h-24 border rounded overflow-hidden">
                          <img src={objectUrl} alt="새 리뷰 이미지" className="object-cover w-full h-full" />
                          <button
                              type="button"
                              onClick={() => handleRemoveNewImage(file)}
                              className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-800"
                              title="이미지 삭제"
                          >
                            ×
                          </button>
                        </div>
                    );
                  })}
                </div>
            )}

            <button
                type="submit"
                className="w-full px-4 py-2 bg-black text-white hover:bg-neutral-800 transition-colors"
                disabled={loading}
            >
              {loading ? '저장 중...' : '저장'}
            </button>
          </form>
        </div>
      </div>
  );
}