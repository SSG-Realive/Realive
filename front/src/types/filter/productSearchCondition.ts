import { PageRequest } from "../page/pageRequest";


export interface ProductSearchCondition extends PageRequest {
  categoryId?: number;
  status?: string;
  minPrice?: number;
  maxPrice?: number;
  isActive?: boolean;
}