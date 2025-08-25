'use client';

import React, { useEffect, useState } from 'react';
import DaumPostcode, { Address } from 'react-daum-postcode';

/* ────── 헬퍼 ────── */
const SEP = '|';
export const parseAddress = (str = '') => {
  const [zonecode = '', addr = '', detail = ''] = str.split(SEP);
  return { zonecode, address: addr, detail };
};
const joinAddress = (o: { zonecode: string; address: string; detail: string }) =>
  [o.zonecode, o.address, o.detail].join(SEP);

/* ────── props ────── */
interface AddressInputProps {
  onAddressChange?: (full: string) => void;
  defaultAddress?: { zonecode: string; address: string; detail: string };
}

/* ────── component ────── */
const AddressInput: React.FC<AddressInputProps> = ({ onAddressChange, defaultAddress }) => {
  const [zonecode, setZonecode] = useState(defaultAddress?.zonecode ?? '');
  const [address, setAddress]   = useState(defaultAddress?.address  ?? '');
  const [detail,  setDetail]    = useState(defaultAddress?.detail   ?? '');
  const [open, setOpen]         = useState(false);

  /* 전체 문자열 콜백 */
  useEffect(() => {
    onAddressChange?.(joinAddress({ zonecode, address, detail }));
  }, [zonecode, address, detail, onAddressChange]);

  /* 다음 API 완료 */
  const handleComplete = (data: Address) => {
    setZonecode(data.zonecode);
    setAddress(data.address);
    setOpen(false);
    document.getElementById('detailedAddressInput')?.focus();
  };

  return (
    <div className="w-full space-y-2">
      {/* ▣ 한 줄 레이아웃 (wrap ❌) */}
      <div className="flex items-start gap-2">
  {/* 우편번호 input */}
  <input
    type="text"
    value={zonecode}
    readOnly
    placeholder="우편번호"
    className="flex-none basis-[75%] max-w-[110px]
               h-10 rounded-md border border-gray-300 bg-gray-100
               px-3 py-2 text-sm text-center leading-none"   /* ← line-height */
  />

  {/* 주소 검색 버튼 */}
  <button
    type="button"
    onClick={() => setOpen((v) => !v)}
    className="flex-1 h-10 rounded-md border border-gray-300 /* ← 같은 border */
               bg-gray-700 px-3 py-2 text-sm font-medium leading-none
               text-white hover:bg-gray-800 whitespace-nowrap"
  >
          {open ? '닫기' : '주소 검색'}
        </button>
      </div>

      {/* ▣ 다음 주소 API */}
      {open && (
        <div className="rounded-md border overflow-hidden">
          <DaumPostcode
            onComplete={handleComplete}
            style={{ width: '100%', height: 400 }}
            autoClose={false}
          />
        </div>
      )}

      {/* ▣ 기본 주소 (읽기 전용) */}
      <input
        type="text"
        value={address}
        readOnly
        placeholder="주소"
        className="w-full rounded-md border border-gray-300 bg-gray-100 px-3 py-2 text-sm"
      />

      {/* ▣ 상세 주소 */}
      <input
        id="detailedAddressInput"
        type="text"
        value={detail}
        onChange={(e) => setDetail(e.target.value)}
        placeholder="상세주소를 입력해주세요"
        className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-blue-500"
      />
    </div>
  );
};

export default AddressInput;
