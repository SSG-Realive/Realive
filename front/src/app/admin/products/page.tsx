"use client";
import React, { useState, useEffect } from "react";
import { Package, DollarSign, Eye, Users, ShoppingCart } from "lucide-react";
import apiClient from "@/lib/apiClient";
import Link from 'next/link';

interface ProductStats {
  totalProducts: number;
  sellerProducts: number;
  adminProducts: number;
  highQualityProducts: number;    // 상
  mediumQualityProducts: number;  // 중
  lowQualityProducts: number;     // 하
  totalValue: number;
  averagePrice: number;
}

export default function ProductDashboardPage() {
  const [stats, setStats] = useState<ProductStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProductStats();
  }, []);

  const fetchProductStats = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('adminToken');
      
      // 전체 상품 통계
      const allProductsRes = await apiClient.get('/admin/products?size=1000', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      // 관리자 매입 상품 통계
      const adminProductsRes = await apiClient.get('/admin/owned-products?size=1000', {
        headers: { Authorization: `Bearer ${token}` }
      });

      const allProducts = allProductsRes.data.dtoList || [];
      const adminProducts = adminProductsRes.data.dtoList || [];
      
      console.log('전체 상품:', allProducts);
      console.log('관리자 매입 상품:', adminProducts);
      
      // 첫 번째 상품의 구조 확인
      if (allProducts.length > 0) {
        console.log('첫 번째 상품 구조:', allProducts[0]);
        console.log('isActive 필드:', allProducts[0].isActive);
        console.log('status 필드:', allProducts[0].status);
      }
      
      if (adminProducts.length > 0) {
        console.log('첫 번째 관리자 상품 구조:', adminProducts[0]);
        console.log('isAuctioned 필드:', adminProducts[0].isAuctioned);
      }
      
      // 판매자 상품 = 전체 상품 (관리자 매입 상품은 별도로 계산)
      const sellerProducts = allProducts;
      const adminProductsCount = adminProducts.length;
      
      // 전체 상품에서 상태별 통계 계산 (관리자 매입 상품 제외)
      const highQualityProducts = sellerProducts.filter((p: any) => p.status === "상").length;
      const mediumQualityProducts = sellerProducts.filter((p: any) => p.status === "중").length;
      const lowQualityProducts = sellerProducts.filter((p: any) => p.status === "하").length;
      
      console.log('상태별 통계:', {
        highQualityProducts,
        mediumQualityProducts,
        lowQualityProducts,
        totalProducts: sellerProducts.length
      });
      
      // 가격 통계 계산
      const totalValue = sellerProducts.reduce((sum: number, p: any) => sum + (p.price || 0), 0);
      const averagePrice = sellerProducts.length > 0 ? totalValue / sellerProducts.length : 0;

      const statsData = {
        totalProducts: sellerProducts.length,
        sellerProducts: sellerProducts.length,
        adminProducts: adminProductsCount,
        highQualityProducts,
        mediumQualityProducts,
        lowQualityProducts,
        totalValue,
        averagePrice
      };
      
      console.log('계산된 통계:', statsData);
      setStats(statsData);
      
      setError(null);
    } catch (error: any) {
      console.error('상품 통계 조회 실패:', error);
      setError('상품 통계를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="bg-white rounded-lg shadow p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
            <p className="text-gray-600">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">상품 통계</h1>
          <p className="text-gray-600">전체 상품 현황과 통계를 확인할 수 있습니다.</p>
        </div>

        {/* 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* 전체 상품 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">전체 상품</p>
                <p className="text-2xl font-bold text-gray-900">{stats?.totalProducts.toLocaleString()}</p>
              </div>
              <div className="p-3 bg-blue-100 rounded-full">
                <Package className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <div className="mt-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">판매자 상품</span>
                <span className="font-medium">{stats?.sellerProducts.toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">관리자 매입</span>
                <span className="font-medium">{stats?.adminProducts.toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* 품질: 상 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 상</p>
              <p className="text-2xl font-bold text-green-600">{stats?.highQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-green-100 rounded-full">
              <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          {/* 품질: 중 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 중</p>
              <p className="text-2xl font-bold text-yellow-600">{stats?.mediumQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-yellow-100 rounded-full">
              <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
          </div>

          {/* 품질: 하 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 하</p>
              <p className="text-2xl font-bold text-red-600">{stats?.lowQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-red-100 rounded-full">
              <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
          </div>

          {/* 총 상품 가치 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">총 상품 가치</p>
                <p className="text-2xl font-bold text-purple-600">
                  {stats?.totalValue.toLocaleString()}원
                </p>
              </div>
              <div className="p-3 bg-purple-100 rounded-full">
                <DollarSign className="w-6 h-6 text-purple-600" />
              </div>
            </div>
            <div className="mt-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">평균 가격</span>
                <span className="font-medium">{stats?.averagePrice.toLocaleString()}원</span>
              </div>
            </div>
          </div>

          {/* 상품 상태 분포 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">상품 상태 분포</p>
              </div>
              <div className="p-3 bg-blue-100 rounded-full">
                <Eye className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">상품질</span>
                </div>
                <span className="font-medium text-green-600">
                  {stats?.totalProducts ? Math.round((stats.highQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">중품질</span>
                </div>
                <span className="font-medium text-yellow-600">
                  {stats?.totalProducts ? Math.round((stats.mediumQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">하품질</span>
                </div>
                <span className="font-medium text-red-600">
                  {stats?.totalProducts ? Math.round((stats.lowQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}