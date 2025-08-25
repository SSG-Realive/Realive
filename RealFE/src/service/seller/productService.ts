import { sellerApi } from '@/lib/apiClient';
import { SellerCategoryDTO } from '@/types/seller/category/sellerCategory';
import { PageResponse } from '@/types/seller/page/pageResponse';
import { ProductDetail } from '@/types/seller/product/product';

import { ProductListItem } from '@/types/seller/product/productList';

// 판매자 상품 목록 조회 API
export async function fetchMyProducts(): Promise<ProductDetail[]> {
    const res = await sellerApi.get('/seller/products');
    return res.data.content; // PageResponseDTO 기준
}

/**
 * 상품 등록 API
 */
export async function createProduct(formData: FormData): Promise<number> {
    const res = await sellerApi.post('/seller/products', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
}

/**
 * 상품 수정 API
 */
export async function updateProduct(id: number, formData: FormData): Promise<void> {
    console.log('=== updateProduct API 호출 ===');
    console.log('상품 ID:', id);
    console.log('요청 URL:', `/seller/products/${id}`);
    
    try {
        const res = await sellerApi.put(`/seller/products/${id}`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        console.log('API 응답 성공:', res.status, res.statusText);
        console.log('응답 데이터:', res.data);
    } catch (error: any) {
        console.error('=== updateProduct API 에러 ===');
        console.error('에러 상태:', error.response?.status);
        console.error('에러 메시지:', error.response?.statusText);
        console.error('에러 데이터:', error.response?.data);
        console.error('전체 에러:', error);
        throw error;
    }
}

/**
 * 상품 단건 조회 API
 */
export async function getProductDetail(id: number): Promise<ProductDetail> {
    const res = await sellerApi.get(`/seller/products/${id}`);
    const data = res.data;
    
    // 백엔드에서 active로 오는 필드를 isActive로 변환
    if (data.active !== undefined) {
        data.isActive = data.active;
        delete data.active;
    }
    
    console.log('=== 상품 상세 데이터 변환 ===');
    console.log('변환된 데이터:', data);
    
    return data;
}

/**
 * 상품 목록 조회 API (판매자)
 * @param searchParams - 페이지, 키워드 등 검색 조건
 */
export async function getMyProducts(searchParams: Record<string, any> = {}): Promise<PageResponse<ProductListItem>> {
    const query = buildSearchParams(searchParams); // ✅ 빈 값 빼고 쿼리스트링 구성
    
    // 임시 디버깅 로그
    console.log('🔍 API 요청 디버깅:');
    console.log('- 입력 파라미터:', searchParams);
    console.log('- 생성된 쿼리:', query);
    console.log('- 최종 URL:', `/seller/products?${query}`);

  const res = await sellerApi.get(`/seller/products?${query}`);
    const data = res.data;
    
    console.log('📦 API 응답 디버깅:');
    console.log('- 응답 데이터:', data);
    console.log('- 상품 개수:', data.dtoList?.length || 0);
    console.log('- 첫 번째 상품 ID:', data.dtoList?.[0]?.id);
    console.log('- 마지막 상품 ID:', data.dtoList?.[data.dtoList?.length - 1]?.id);
    
    // 백엔드에서 active로 오는 필드를 isActive로 변환
    if (data.dtoList && Array.isArray(data.dtoList)) {
        data.dtoList = data.dtoList.map((product: any) => {
            if (product.active !== undefined) {
                product.isActive = product.active;
                delete product.active;
            }
            return product;
        });
    }
    
    console.log('=== 상품 목록 데이터 변환 ===');
    console.log('변환된 목록 개수:', data.dtoList?.length || 0);
    console.log('첫 번째 상품 isActive:', data.dtoList?.[0]?.isActive);
    
    return data;
}

/**
 * 상품 삭제 API
 */
export async function deleteProduct(id: number): Promise<void> {
    await sellerApi.delete(`/seller/products/${id}`);
}

//쿼리 빈값 제거
const buildSearchParams = (params: Record<string, any>): string => {
  const validParams = Object.entries(params)
    .filter(([_, v]) => v !== undefined && v !== null && v !== '') // 빈 값 제거
    .reduce((acc, [k, v]) => ({ ...acc, [k]: v }), {});

  return new URLSearchParams(validParams).toString();
};

//물건 등록시 카테고리 
export async function fetchCategories(): Promise<SellerCategoryDTO[]> {
    const res = await sellerApi.get('/seller/categories');
    return res.data;
}

/**
 * 판매자 전체 상품 통계 조회 API
 * @returns 전체 상품에 대한 판매 상태별 통계
 */
export async function getMyProductStats(): Promise<{
  total: number;
  selling: number;  // isActive=true && stock>0
  suspended: number; // isActive=false
  outOfStock: number; // stock=0
}> {
  try {
    // 전체 상품을 가져오기 위해 충분히 큰 size로 요청
    const res = await sellerApi.get('/seller/products?size=1000');
    let allProducts: ProductListItem[] = res.data.dtoList || [];
    
    // 백엔드에서 active로 오는 필드를 isActive로 변환
    allProducts = allProducts.map((product: any) => {
        if (product.active !== undefined) {
            product.isActive = product.active;
            delete product.active;
        }
        return product;
    });
    
    // 상품 상태별 분류 (중복 제거)
    const sellingProducts = allProducts.filter(p => p.isActive && p.stock > 0); // 판매중: 활성화 + 재고 있음
    const outOfStockProducts = allProducts.filter(p => p.stock === 0); // 품절: 재고 0 (활성화 상태 관계없이)
    const suspendedProducts = allProducts.filter(p => !p.isActive && p.stock > 0); // 판매중지: 비활성화 + 재고 있음
    
    const stats = {
      total: allProducts.length,
      selling: sellingProducts.length,
      suspended: suspendedProducts.length,
      outOfStock: outOfStockProducts.length
    };
    
    console.log('=== 상품 통계 계산 ===');
    console.log('전체 상품:', stats.total);
    console.log('판매중:', stats.selling);
    console.log('판매중지:', stats.suspended);
    console.log('품절:', stats.outOfStock);
    
    // 품절 상품 상세 정보
    if (outOfStockProducts.length > 0) {
      console.log('=== 품절 상품 상세 ===');
      outOfStockProducts.forEach(p => {
        console.log(`상품 "${p.name}": 재고=${p.stock}, isActive=${p.isActive}`);
      });
    }
    
    return stats;
  } catch (error) {
    console.error('판매자 상품 통계 조회 실패:', error);
    // 에러 시 기본값 반환
    return {
      total: 0,
      selling: 0,
      suspended: 0,
      outOfStock: 0
    };
  }
}