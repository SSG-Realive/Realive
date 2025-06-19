/** 판매자 로그인 응답(플랫 구조) */
export interface LoginResponse {
  /** 액세스 토큰(JWT) */
  accessToken: string;
  /** 리프레시 토큰(JWT) */
  refreshToken: string;
  /** 판매자 이메일 */
  email: string;
  /** 판매자 이름(표시용) */
  name: string;
}
