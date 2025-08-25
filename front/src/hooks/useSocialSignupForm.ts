// hooks/useSocialSignupForm.ts
import { useState } from 'react';

export type Gender = 'M' | 'F' | 'UNSELECTED';

export function useSocialSignupForm(initialEmail: string) {
  const [userName, setUserName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [birth, setBirth] = useState('');
  const [gender, setGender] = useState<Gender>('UNSELECTED');
  const [loading, setLoading] = useState(false);

  const isValid = () => !!userName && !!phone && !!address && !!birth && gender !== 'UNSELECTED';

  return {
    form: { userName, phone, address, birth, gender, email: initialEmail },
    setUserName,
    setPhone,
    setAddress,
    setBirth,
    setGender,
    loading,
    setLoading,
    isValid,
  };
}
