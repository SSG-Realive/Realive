'use client';

import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import StarRating from '@/components/customer/review/StarRating';
import { createReview } from '@/service/customer/reviewService';
import Modal from '@/components/Modal';




function ReviewPage() {
  const router = useRouter();
  const params = useParams();
  const searchParams = useSearchParams();

  const orderIdParam = params.orderId;  
  const sellerIdParam = searchParams.get('sellerId');

  const [orderId, setOrderId] = useState<number | null>(null);
  const [sellerId, setSellerId] = useState<number | null>(null);
  const [rating, setRating] = useState<number>(5);
  const [tempRating, setTempRating] = useState<number | null>(null);
  const [content, setContent] = useState('');
  const [images, setImages] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [alreadyReviewed, setAlreadyReviewed] = useState<boolean | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);


  const fileInputRef = useRef<HTMLInputElement>(null);

  const getAccessToken = () => {
    try {
      const stored = localStorage.getItem('auth-storage');
      return stored ? JSON.parse(stored)?.state?.accessToken : null;
    } catch {
      return null;
    }
  };

  useEffect(() => {
    if (alreadyReviewed) {
      // 이미 리뷰를 작성했다면 주문 목록 페이지로 리디렉션
      router.replace('/customer/mypage/orders');
    }
  }, [alreadyReviewed, router]);


  useEffect(() => {
    if (orderIdParam && sellerIdParam) {
      const parsedOrderId = Number(orderIdParam);
      const parsedSellerId = Number(sellerIdParam);
      setOrderId(parsedOrderId);
      setSellerId(parsedSellerId);

      const accessToken = getAccessToken();
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
      };

      axios
        .get('https://www.realive-ssg.click/api/customer/reviews/check-exists', {
          params: { orderId: parsedOrderId, sellerId: parsedSellerId },
          headers,
          withCredentials: true,
        })
        .then((res) => setAlreadyReviewed(res.data.exists))
        .catch((err) => {
          if (axios.isAxiosError(err) && err.response?.status === 401) {
            alert('로그인이 필요합니다.');
            router.push('/login');
          } else {
            setAlreadyReviewed(null);
          }
        });
    }
  }, [orderIdParam, sellerIdParam]);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.target.files && setImages((prev) => [...prev, ...Array.from(e.target.files!)]);
    };


  const handleRemoveImage = (file: File) => {
    setImages((prev) => prev.filter((f) => f !== file));
  };

  const uploadImages = async (): Promise<string[]> => {
    const urls: string[] = [];
    const accessToken = getAccessToken();

    for (const file of images) {
      const formData = new FormData();
      formData.append('file', file);
      try {
        const res = await axios.post('https://www.realive-ssg.click/api/customer/reviews/images/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
          },
        });
        urls.push(res.data); // 문자열 그대로 받기
      } catch (err) {
        console.error('이미지 업로드 실패:', err);
      }
    }

    return urls;
  };


const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();

  const currentOrderId = orderIdParam ? Number(orderIdParam) : null;
  const currentSellerId = sellerIdParam ? Number(sellerIdParam) : null;

  if (!currentOrderId || !currentSellerId) {
    setMessage('주문 ID 또는 판매자 ID가 누락되었습니다.');
    return;
  }

  setLoading(true);
  try {
    const uploadedUrls = await uploadImages();

    const payload = {
      orderId: currentOrderId,
      sellerId: currentSellerId,
      rating,
      content,
      imageUrls: uploadedUrls,
    };

    await createReview(payload);

    setShowSuccess(true);
    setTimeout(() => {
      router.push(`/customer/mypage/orders/${currentOrderId}`);
    }, 2000);
  } catch (error) {
    console.error('리뷰 등록 실패:', error);
    setMessage('리뷰 등록 중 오류가 발생했습니다.');

    if (axios.isAxiosError(error) && error.response?.status === 401) {
      alert('로그인이 필요하거나 세션이 만료되었습니다.');
      router.push('/login');
    }
  } finally {
    setLoading(false);
  }
};


  return (
    <div className="container max-w-xl mx-auto py-10 px-6 bg-blue-50 rounded-md shadow-md py-16">
    {/* <Navbar /> */}
    <h1 className="text-3xl font-semibold mb-6 text-blue-900">리뷰 작성</h1>

    <Modal
      isOpen={showSuccess}
      onClose={() => setShowSuccess(false)}
      title="리뷰 등록 완료"
      message="리뷰가 성공적으로 등록되었습니다."
      type="success"
      className="bg-black/30 backdrop-blur-sm"
      titleClassName="text-blue-900"
      buttonClassName="bg-blue-600 "
    />


    <form onSubmit={handleSubmit} className="space-y-6">
        {/* 별점 */}
        <div>
        <label className="block font-semibold text-gray-700 mb-2">
            평점: {(tempRating ?? rating).toFixed(1)}점
        </label>
        <StarRating
            rating={rating}
            setRating={setRating}
            setTempRating={setTempRating}
        />
        </div>

        {/* 내용 */}
        <div>
        <label className="block font-semibold text-gray-700 mb-2">리뷰 내용</label>
        <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="w-full border border-gray-300 rounded-md px-4 py-3 text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400 min-h-[140px]"
            placeholder="상품에 대한 솔직한 후기를 남겨주세요."
        />
        </div>

        {/* 이미지 업로드 */}
        <div>
        <label className="block font-semibold text-gray-700 mb-2">이미지 첨부</label>
        <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageChange}
            className="block w-full border rounded-md text-gray-700 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-blue-600 file:text-white hover:file:bg-blue-700 transition"
        />

        {/* 이미지 미리보기 및 삭제 버튼 */}
        {images.length > 0 && (
          <div className="flex flex-wrap gap-3 mt-3">
            {images.map((file) => {
              const objectUrl = URL.createObjectURL(file);
              return (
                <div
                  key={objectUrl}
                  className="relative w-24 h-24 border rounded overflow-hidden shadow-sm"
                >
                  <img
                    src={objectUrl}
                    alt="리뷰 이미지 미리보기"
                    className="object-cover w-full h-full"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveImage(file)}
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

        </div>

        {/* 제출 버튼 */}
        <button
        type="submit"
        disabled={loading}
        className="w-full py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors duration-200 font-semibold"
        >
        {loading ? '등록 중...' : '리뷰 등록'}
        </button>

        {/* 메시지 */}
        {message && <p className="text-red-500 text-sm mt-2">{message}</p>}
    </form>
    </div>

  );
}

export default ReviewPage;