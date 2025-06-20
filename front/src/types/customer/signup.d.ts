
type GenderWithUnselected = 'M' | 'F' | 'UNSELECTED';

export interface MemberJoinDTO {
  email: string;
  password: string;
  name: string;
  phone: string;
  address: string;
  birth: string;      
  gender: 'M' | 'F';    
}