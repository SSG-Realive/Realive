/** 판매자 로그인 요청 페이로드 */
export interface LoginRequest {
  /** 로그인 이메일 */
  email: string;
  /** 로그인 비밀번호(평문) */
  password: string;
}
