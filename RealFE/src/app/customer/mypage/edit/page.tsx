'use client';

import { useRouter } from 'next/navigation';
// ✅ [수정] useCallback을 import 합니다.
import { useEffect, useState, useCallback } from 'react';
import {
  fetchMyProfile,
  updateMyProfile,
} from '@/service/customer/customerService';
import {
  MemberReadDTO,
} from '@/types/customer/member/member';
import AddressInput, { parseAddress } from '@/components/customer/join/AddressInput'; // ✅ parseAddress도 함께 import
import useDialog from '@/hooks/useDialog';
import GlobalDialog from '@/components/ui/GlobalDialog';
import { Pencil } from 'lucide-react';
import MyPageSidebar from '@/components/customer/common/MyPageSidebar';

// ReadOnlyCard와 EditableCard 컴포넌트는 그대로 둡니다.
function ReadOnlyCard({ label, value }: { label: string; value?: string | null }) {
  return (
      <section className="rounded-2xl bg-white/90 p-5 shadow ring-1 ring-gray-100">
        <h2 className="mb-2 text-sm font-light text-gray-500">{label}</h2>
        <p className="text-[17px] sm:text-base text-gray-800 break-words">{value || '-'}</p>
      </section>
  );
}

function EditableCard(props: {
  label: string;
  value: string;
  editing: boolean;
  onEdit: () => void;
  onCancel: () => void;
  input: React.ReactNode;
}) {
  const { label, value, editing, onEdit, onCancel, input } = props;

  return (
      <section className="rounded-2xl bg-white/90 p-5 shadow ring-1 ring-gray-100 space-y-2">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-light text-gray-500">{label}</h2>
          {!editing && (
              <button
                  type="button"
                  onClick={onEdit}
                  className="rounded p-1 text-gray-400 hover:text-amber-600 focus:outline-none focus:ring-2 focus:ring-amber-300"
              >
                <Pencil className="h-4 w-4" />
                <span className="sr-only">수정</span>
              </button>
          )}
        </div>

        {editing ? (
            <>
              {input}
              <button
                  type="button"
                  onClick={onCancel}
                  className="self-start text-sm text-gray-400 hover:text-gray-600"
              >
                취소
              </button>
            </>
        ) : (
            <p className="text-[17px] sm:text-base text-gray-800 break-words">
              {value}
            </p>
        )}
      </section>
  );
}


export default function EditProfilePage() {
  const router = useRouter();
  const { open, message, handleClose, show } = useDialog();

  const [profile, setProfile] = useState<MemberReadDTO | null>(null);
  const [phoneEdit, setPhoneEdit] = useState(false);
  const [addrEdit, setAddrEdit] = useState(false);
  const [phone, setPhone] = useState('');
  const [addrStr, setAddrStr] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const data = await fetchMyProfile();
        setProfile(data);
        setPhone(data.phone ?? '');
        setAddrStr(data.address ?? '');
      } catch {
        show('프로필을 가져오지 못했습니다.');
      }
    })();
  }, [show]); // ✅ show를 의존성 배열에 추가합니다.

  // ✅ [수정] 모든 핸들러 함수를 useCallback으로 감싸서 불필요한 재생성을 방지합니다.
  const handlePhoneCancel = useCallback(() => {
    setPhone(profile?.phone ?? '');
    setPhoneEdit(false);
  }, [profile]);

  const handleAddrCancel = useCallback(() => {
    setAddrStr(profile?.address ?? '');
    setAddrEdit(false);
  }, [profile]);

  const handleSubmit = useCallback(async () => {
    const payload = {
      phone: phone.trim() || undefined,
      address: addrStr.trim() || undefined,
    };
    try {
      setSaving(true);
      await updateMyProfile(payload);
      await show('수정이 완료되었습니다.');
      setPhoneEdit(false);
      setAddrEdit(false);
      setProfile((p) => p ? { ...p, phone: payload.phone!, address: payload.address! } : p);
    } catch {
      show('저장에 실패했습니다.');
    } finally {
        setSaving(false);
    }
  }, [phone, addrStr, show]);

  if (!profile) {
    return <p className="p-6 text-center text-gray-500">로딩 중…</p>;
  }

  const parsedAddr = parseAddress(addrStr);

  return (
      <>
        <GlobalDialog open={open} message={message} onClose={handleClose} />

        <div className="flex max-w-7xl mx-auto px-4">
          <MyPageSidebar />

          <main className="flex-1 py-10 space-y-6 pl-6">
            <ReadOnlyCard label="이름" value={profile.name} />
            <ReadOnlyCard label="이메일" value={profile.email} />
            <ReadOnlyCard label="생년월일" value={profile.birth ?? '-'} />
            <ReadOnlyCard label="가입일" value={new Date(profile.created).toLocaleDateString()} />

            <EditableCard
                label="전화번호"
                editing={phoneEdit}
                value={phone || '-'}
                onEdit={() => setPhoneEdit(true)}
                onCancel={handlePhoneCancel} // ✅ useCallback으로 감싼 함수 사용
                input={
                  <input
                      type="tel"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full rounded border px-3 py-2 text-sm ring-amber-300 focus:border-amber-500 focus:ring"
                  />
                }
            />

            <EditableCard
                label="주소"
                editing={addrEdit}
                value={
                  addrStr
                    ? `${parsedAddr.address} ${parsedAddr.detail}`
                    : '-'
                }
                onEdit={() => setAddrEdit(true)}
                onCancel={handleAddrCancel} // ✅ useCallback으로 감싼 함수 사용
                input={
                  <AddressInput
                      onAddressChange={setAddrStr} // ✅ setAddrStr은 상태 설정 함수라 그대로 사용해도 괜찮습니다.
                      defaultAddress={parsedAddr}
                  />
                }
            />

            {(phoneEdit || addrEdit) && (
                <div className="flex justify-end gap-3">
                  <button
                      type="button"
                      onClick={() => { // 이 부분은 복합적인 동작이라 인라인으로 두어도 괜찮습니다.
                        handlePhoneCancel();
                        handleAddrCancel();
                      }}
                      className="rounded-md px-4 py-2 text-sm ring-1 ring-gray-300 hover:bg-gray-50"
                  >
                    전체 취소
                  </button>
                  <button
                      type="button"
                      onClick={handleSubmit} // ✅ useCallback으로 감싼 함수 사용
                      disabled={saving}
                      className="rounded-md bg-amber-500 px-5 py-2 text-sm font-light text-white shadow hover:bg-amber-600 disabled:opacity-50"
                  >
                    {saving ? '저장 중…' : '변경사항 저장'}
                  </button>
                </div>
            )}
          </main>
        </div>
      </>
  );
}