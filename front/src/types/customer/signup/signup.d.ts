export interface MemberJoinDTO {
  email: string;
  password: string;
  name: string;
  phone: string;
  address: string;
  birth: string;      // 예: "1999-01-01"
  gender: 'M' | 'F';     // 예: "MALE" | "FEMALE"
}