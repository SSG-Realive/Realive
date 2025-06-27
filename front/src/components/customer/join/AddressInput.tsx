import React, { useState } from 'react';
import DaumPostcode, { Address, DaumPostcodeEmbedProps } from 'react-daum-postcode';

interface AddressInputProps {
  onAddressChange?: (fullAddress: string) => void;
}

const AddressInput: React.FC<AddressInputProps> = ({ onAddressChange }) => {
  const [zonecode, setZonecode] = useState('');
  const [address, setAddress] = useState('');
  const [detailedAddress, setDetailedAddress] = useState('');
  const [isOpen, setIsOpen] = useState(false);

  const postCodeStyle: React.CSSProperties = {
    width: '360px',
    height: '480px',
  };

  const updateFullAddress = (base: string, detail: string) => {
    const full = `${base} ${detail}`.trim();
    if (onAddressChange) onAddressChange(full);
  };

  const completeHandler = (data: Address) => {
    const { address, zonecode } = data;
    setZonecode(zonecode);
    setAddress(address);

    updateFullAddress(address, detailedAddress);
    setIsOpen(false);
  };

  const inputChangeHandler = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setDetailedAddress(value);
    updateFullAddress(address, value);
  };
  

  return (
    <div className="space-y-2">
      <div className="flex gap-2 items-center">
        <input
          type="text"
          value={zonecode}
          placeholder="우편번호"
          readOnly
          className="border rounded px-2 py-1 w-32"
        />
        <button
          type="button"
          onClick={() => setIsOpen(prev => !prev)}
          className="border rounded px-2 py-1 text-sm bg-gray-100 hover:bg-gray-200"
        >
          주소 찾기
        </button>
      </div>

      {isOpen && (
        <div className="mt-2 border p-2">
          <DaumPostcode
            style={postCodeStyle}
            onComplete={completeHandler}
            onClose={() => setIsOpen(false)}
          />
        </div>
      )}

      <input
        type="text"
        value={address}
        placeholder="기본 주소"
        readOnly
        className="border rounded px-2 py-1 w-full"
      />
      <input
        type="text"
        value={detailedAddress}
        onChange={inputChangeHandler}
        placeholder="상세 주소 입력"
        className="border rounded px-2 py-1 w-full"
      />
    </div>
  );
};

export default AddressInput;
