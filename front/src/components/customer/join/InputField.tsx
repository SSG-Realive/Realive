'use client';

import React from 'react';

interface InputFieldProps {
  label: string;
  name: string;
  type?: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

export default function InputField({
  label,
  name,
  type = 'text',
  value,
  onChange
}: InputFieldProps) {
  return (
    <div className="mb-4">
      <label htmlFor={name} className="block font-semibold mb-1">{label}</label>
      <input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        className="w-full border p-2 rounded"
      />
    </div>
  );
}
