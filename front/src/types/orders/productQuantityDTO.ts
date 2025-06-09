
export interface ProductQuantityDTO {
    productId: number; // @NotNull -> 필수 필드, Spring의 Long은 TypeScript에서 number로 매핑됩니다.
    quantity: number;  // @Min(value = 1) -> 최소 1 이상, Spring의 int는 TypeScript에서 number로 매핑됩니다.
}