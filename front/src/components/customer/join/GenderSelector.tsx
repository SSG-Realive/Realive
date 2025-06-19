'use client';

interface Props {
  gender?: string;
  onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
}

export default function GenderSelector({ gender, onChange }: Props) {
  return (
    <div className="mb-4">
      <label className="block font-semibold mb-1">성별</label>
      <select value={gender || ''} onChange={onChange} className="w-full border p-2 rounded">
        <option value="MALE">남성</option>
        <option value="FEMALE">여성</option>
      </select>
    </div>
  );
}
