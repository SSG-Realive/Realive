

import customerApi from '@/lib/apiClient';
import { MemberModifyDTO, MemberReadDTO } from '@/types/customer/member/member';

// 내 정보 조회
export const fetchMyProfile = async (): Promise<MemberReadDTO> => {
  const res = await customerApi.get('/customer/mypage');
  return res.data;
};

// 내 정보 수정
export const updateMyProfile = async (data: MemberModifyDTO): Promise<void> => {
  await customerApi.put('/customer/mypage', data);
};

// 회원 탈퇴
export const deleteMyAccount = async (): Promise<string> => {
  const res = await customerApi.delete('/customer/mypage');
  return res.data; // "회원 탈퇴가 정상 처리되었습니다."
};
