
export interface OrderConfirmRequestDTO {
    orderId: number;    // Spring의 Long은 TypeScript에서 number로 매핑됩니다.
    customerId: number; // Spring의 Long은 TypeScript에서 number로 매핑됩니다.
}