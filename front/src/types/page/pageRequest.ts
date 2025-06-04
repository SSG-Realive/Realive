export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'DESC';
  keyword?: string;
  type?: string;
}