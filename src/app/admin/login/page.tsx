'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { adminLogin } from '@/service/admin/adminService';
import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';
import Modal from '@/components/Modal';

function isApiError(error: unknown): error is { response?: { data?: { message?: string } } } {
  return (
      typeof error === 'object' &&
      error !== null &&
      'response' in error &&
      typeof (error as any).response === 'object'
  );
}

const AdminLoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'success' | 'error'>('success');
  const [modalTitle, setModalTitle] = useState('');
  const [modalMessage, setModalMessage] = useState('');
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const data = await adminLogin(email, password);
      if (data && data.accessToken) {
        // 1. localStorage 저장
        localStorage.setItem('adminToken', data.accessToken);
        // 2. zustand 스토어에도 저장
        useAdminAuthStore.setState({
          accessToken: data.accessToken,
          refreshToken: data.refreshToken,
        });

        setModalType('success');
        setModalTitle('로그인 성공');
        setModalMessage('관리자 페이지로 이동합니다.');
        setShowModal(true);
        setTimeout(() => {
          setShowModal(false);
          router.push('/admin/dashboard');
        }, 1200);
      } else {
        setModalType('error');
        setModalTitle('로그인 실패');
        setModalMessage('토큰이 없습니다. 로그인에 실패했습니다.');
        setShowModal(true);
      }
    } catch (err: unknown) {
      setModalType('error');
      setModalTitle('로그인 실패');
      if (isApiError(err)) {
        setModalMessage(err.response?.data?.message || '로그인에 실패했습니다.');
      } else {
        setModalMessage('로그인에 실패했습니다.');
      }
      setShowModal(true);
    }
  };

  const handleModalClose = () => {
    setShowModal(false);
  };

  return (
      <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            관리자 로그인
          </h2>
        </div>
        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
            <form className="space-y-6" onSubmit={handleSubmit}>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  이메일
                </label>
                <div className="mt-1">
                  <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      required
                      value={email}
                      onChange={e => setEmail(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>
              </div>
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                  비밀번호
                </label>
                <div className="mt-1">
                  <input
                      id="password"
                      name="password"
                      type="password"
                      autoComplete="current-password"
                      required
                      value={password}
                      onChange={e => setPassword(e.target.value)}
                      className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                  />
                </div>
              </div>
              <div>
                <button
                    type="submit"
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                >
                  로그인
                </button>
              </div>
            </form>
          </div>
        </div>
        <Modal
            isOpen={showModal}
            onClose={handleModalClose}
            title={modalTitle}
            message={modalMessage}
            type={modalType}
        />
      </div>
  );
};

export default AdminLoginPage;