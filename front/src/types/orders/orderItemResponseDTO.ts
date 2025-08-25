
export interface OrderItemResponseDTO {
    productId: number;  // Spring의 Long은 TypeScript에서 number로 매핑됩니다.
    productName: string;
    quantity: number;   // Spring의 int는 TypeScript에서 number로 매핑됩니다.
    price: number;      // Spring의 int는 TypeScript에서 number로 매핑됩니다.
    imageUrl: string;

    sellerId: number; // review 작성용
}