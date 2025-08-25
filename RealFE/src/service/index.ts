// 중복 제거 + alias 처리한 버전
export * from './categoryService'
export * from './productService'
export * from './customer/productService'
export * from './customer/cartService'
export * from './customer/logoutService'
export * from './customer/publicAuctionService'
export * from './customer/reviewService'
export * from './customer/wishlistService'
export * from './customer/wonAuctionService'
export * from './customer/customerQnaService'
export * from './customer/reviewImageService'

// 중복된 fetchMyProfile 충돌 해결
export { fetchMyProfile as fetchCustomerProfile } from './customer/customerService';
export { fetchMyProfile as fetchAuctionProfile } from './customer/auctionService';

export * from './order/orderService'
export * from './order/tossPaymentService'
export * from './seller/adminInquiryService'
export * from './seller/customerQnaService'
export * from './seller/deliveryService'
export {
    fetchMyProducts as fetchSellerProducts,
    createProduct as createSellerProduct,
    updateProduct as updateSellerProduct,
    getProductDetail as getSellerProductDetail,
    getMyProducts as getSellerMyProducts,
    deleteProduct as deleteSellerProduct,
    fetchCategories as fetchSellerCategories,
    getMyProductStats as getSellerProductStats,
} from './seller/productService';
export * from './seller/reviewService'
export {
    getSellerOrders as fetchSellerOrders,
    getOrderDetail as fetchSellerOrderDetail,
    updateDeliveryStatus as updateSellerOrderDeliveryStatus,
    cancelOrderDelivery as cancelSellerOrderDelivery,
} from './seller/sellerOrderService';
export * from './seller/sellerQnaService'
export {
    login as loginSeller,
    logout as logoutSeller,
    getProfile as getSellerProfile,
    updateProfile as updateSellerProfile,
    getDashboard as getSellerDashboard,
    getSalesStatistics as getSellerSalesStatistics,
    getDailySalesTrend as getSellerDailySalesTrend,
    getMonthlySalesTrend as getSellerMonthlySalesTrend,
    getSellerPublicInfoList,
    getSellerPublicInfoBySellerId,
    getSellerReviews,
    getSellerProducts,
} from './seller/sellerService';
export * from './seller/sellerSettlementService'
export * from './seller/signupService'
export * from './seller/logoutService'
