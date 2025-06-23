'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { fetchMyProfile, updateMyProfile } from '@/service/customer/customerService';
import { MemberModifyDTO, MemberReadDTO } from '@/types/customer/member/member';  // ← 타입 경로 확인

export default function EditProfilePage() {
    const router = useRouter();

    /* 읽기용·폼용 상태 */
    const [profile, setProfile] = useState<MemberReadDTO | null>(null);
    const [form, setForm] = useState<MemberModifyDTO>({ phone: '', address: '' });
    const [loading, setLoading] = useState(false);

    /* ① 내 정보 불러오기 */
    useEffect(() => {
        (async () => {
            try {
                const data = await fetchMyProfile();
                setProfile(data);                    // 화면 표시
                setForm({                            // 폼 기본값
                    phone: data.phone ?? '',
                    address: data.address ?? '',
                });
            } catch (e) {
                console.error('프로필 조회 실패', e);
            }
        })();
    }, []);

    /* 인풋 핸들러 */
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    /* 제출 핸들러 */
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            await updateMyProfile(form);        // ① 프로필 수정 API 호출
            router.replace('/customer/mypage'); // ② 성공 → 마이페이지로 이동
        } catch (err) {
            console.error('정보 수정 실패:', err); // ③ 실패 시 콘솔만 출력
        } finally {
            setLoading(false);
        }
    };

    // ① 프로필이 아직 없으면 로딩 화면만
    if (!profile) {
        return <p className="p-6">로딩 중...</p>;
    }


    /* ---------- UI ---------- */
    return (
        <main className="mx-auto max-w-md p-6">
            <h1 className="mb-6 text-xl font-bold">정보 수정</h1>

            {/* 1. 현재 정보 표시 */}
            <section className="rounded-lg bg-gray-50 p-4 shadow-sm">
                <h2 className="mb-4 text-sm font-semibold text-gray-600">현재 정보</h2>
                <ul className="space-y-1 text-gray-700 text-sm">
                    <li><strong>이름:</strong> {profile.name}</li>
                    <li><strong>이메일:</strong> {profile.email}</li>
                    <li><strong>전화번호:</strong> {profile.phone}</li>
                    <li><strong>주소:</strong> {profile.address}</li>
                    <li><strong>생년월일:</strong> {profile.birth}</li>
                    <li><strong>가입일:</strong> {new Date(profile.created).toLocaleDateString()}</li>
                </ul>
            </section>

            {/* 2. 수정 폼 */}
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="mb-1 block text-sm">전화번호</label>
                    <input
                        name="phone"
                        type="tel"
                        value={form.phone}
                        onChange={handleChange}
                        className="w-full rounded border p-2"
                    />
                </div>

                <div>
                    <label className="mb-1 block text-sm">주소</label>
                    <input
                        name="address"
                        value={form.address}
                        onChange={handleChange}
                        className="w-full rounded border p-2"
                    />
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full rounded bg-blue-600 py-2 text-white hover:bg-blue-700 disabled:opacity-50"
                >
                    {loading ? '저장 중...' : '저장'}
                </button>
            </form>
        </main>
    );
}
