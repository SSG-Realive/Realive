// 상품 등록
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header'; // ✅ 판매자 전용 헤더 포함
import { createProduct } from '@/service/productService';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function ProductCreatePage() {
     // 판매자 인증 가드를 적용
     const checking = useSellerAuthGuard();

    const router = useRouter();

    // 상품 정보 입력
    const [name, setName] = useState('');
    const [price, setPrice] = useState('');
    const [stock, setStock] = useState('');
    const [description, setDescription] = useState('');
    const [width, setWidth] = useState('');
    const [depth, setDepth] = useState('');
    const [height, setHeight] = useState('');
    const [categoryId, setCategoryId] = useState('');
    const [active, setActive] = useState(true);

    // 파일 업로드
    const [imageThumbnail, setImageThumbnail] = useState<File | null>(null);
    const [videoThumbnail, setVideoThumbnail] = useState<File | null>(null);
    const [subImages, setSubImages] = useState<FileList | null>(null);

    // 배송정책 (A안)
    const [deliveryType, setDeliveryType] = useState('FREE');
    const [deliveryCost, setDeliveryCost] = useState('');
    const [regionLimit, setRegionLimit] = useState('');

    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        const formData = new FormData();
        formData.append('name', name);
        formData.append('price', price);
        formData.append('stock', stock);
        formData.append('description', description);
        formData.append('width', width);
        formData.append('depth', depth);
        formData.append('height', height);
        formData.append('categoryId', categoryId);
        formData.append('active', String(active));

        if (imageThumbnail) formData.append('imageThumbnail', imageThumbnail);
        if (videoThumbnail) formData.append('videoThumbnail', videoThumbnail);
        if (subImages) {
            Array.from(subImages).forEach((file) => {
                formData.append('subImages', file);
            });
        }

        formData.append('deliveryPolicy.type', deliveryType);
        formData.append('deliveryPolicy.cost', deliveryCost);
        formData.append('deliveryPolicy.regionLimit', regionLimit);

        try {
            await createProduct(formData);
            alert('상품이 등록되었습니다!');
            router.push('/seller/products');
        } catch (err) {
            console.error('상품 등록 실패:', err);
            setError('상품 등록 중 오류가 발생했습니다.');
        }
    };
    if (checking) return <div className="p-8">인증 확인 중...</div>;
    
    return (
        <>
            <Header /> {/* ✅ 상단 고정 헤더 렌더링 */}
            <SellerLayout>
            <div style={{ maxWidth: 600, margin: '0 auto', padding: '2rem' }}>
                <h1 style={{ fontSize: '1.5rem', marginBottom: '1.5rem' }}>상품 등록</h1>
                <form onSubmit={handleSubmit}>
                    {[
                        { label: '상품명', value: name, setter: setName, type: 'text' },
                        { label: '가격', value: price, setter: setPrice, type: 'number' },
                        { label: '재고', value: stock, setter: setStock, type: 'number' },
                        { label: '설명', value: description, setter: setDescription, type: 'textarea' },
                        { label: '가로(cm)', value: width, setter: setWidth, type: 'number' },
                        { label: '세로(cm)', value: depth, setter: setDepth, type: 'number' },
                        { label: '높이(cm)', value: height, setter: setHeight, type: 'number' },
                        { label: '카테고리 ID', value: categoryId, setter: setCategoryId, type: 'number' },
                    ].map((field, idx) => (
                        <div key={idx} style={{ marginBottom: '1rem' }}>
                            <label>{field.label}</label>
                            {field.type === 'textarea' ? (
                                <textarea
                                    value={field.value}
                                    onChange={(e) => field.setter(e.target.value)}
                                    required
                                    style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                                />
                            ) : (
                                <input
                                    type={field.type}
                                    value={field.value}
                                    onChange={(e) => field.setter(e.target.value)}
                                    required
                                    style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                                />
                            )}
                        </div>
                    ))}

                    <div style={{ marginBottom: '1rem' }}>
                        <label>
                            <input
                                type="checkbox"
                                checked={active}
                                onChange={(e) => setActive(e.target.checked)}
                                style={{ marginRight: '0.5rem' }}
                            />
                            판매 활성화 여부
                        </label>
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label>대표 이미지 (필수)</label>
                        <input type="file" accept="image/*" onChange={(e) => setImageThumbnail(e.target.files?.[0] || null)} />
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label>대표 영상 (선택)</label>
                        <input type="file" accept="video/*" onChange={(e) => setVideoThumbnail(e.target.files?.[0] || null)} />
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label>서브 이미지들 (선택, 여러 개)</label>
                        <input type="file" accept="image/*" multiple onChange={(e) => setSubImages(e.target.files)} />
                    </div>

                    <div style={{ marginBottom: '1rem' }}>
                        <label>배송 유형</label>
                        <select
                            value={deliveryType}
                            onChange={(e) => setDeliveryType(e.target.value)}
                            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                        >
                            <option value="FREE">무료배송</option>
                            <option value="PAID">유료배송</option>
                        </select>
                    </div>

                    {deliveryType === 'PAID' && (
                        <div style={{ marginBottom: '1rem' }}>
                            <label>배송비</label>
                            <input
                                type="number"
                                value={deliveryCost}
                                onChange={(e) => setDeliveryCost(e.target.value)}
                                style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                            />
                        </div>
                    )}

                    <div style={{ marginBottom: '1rem' }}>
                        <label>지역 제한</label>
                        <textarea
                            value={regionLimit}
                            onChange={(e) => setRegionLimit(e.target.value)}
                            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
                        />
                    </div>

                    {error && <p style={{ color: 'red', marginBottom: '1rem' }}>{error}</p>}

                    <button
                        type="submit"
                        style={{
                            width: '100%',
                            padding: '0.75rem',
                            backgroundColor: '#333',
                            color: '#fff',
                            border: 'none',
                            cursor: 'pointer',
                        }}
                    >
                        상품 등록
                    </button>
                </form>
            </div>
            </SellerLayout>
        </>
    );
}