export interface MemberReadDTO {
  name: string;
  email: string;
  phone: string;
  address: string;
  birth: string;          // 'YYYY-MM-DD'
  created: string;        // ISO-8601
}

export interface MemberModifyDTO {
  phone?: string;
  address?: string;
  birth?: string;
}