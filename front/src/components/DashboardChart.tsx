"use client";
import React from 'react';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { ApexOptions } from 'apexcharts';

const ReactApexChart = dynamic(() => import('react-apexcharts'), { ssr: false });

interface ProductStats {
  totalProducts: number;
  highQualityProducts: number;
  mediumQualityProducts: number;
  lowQualityProducts: number;
  categoryStats: { [key: string]: number };
}

interface DashboardChartProps {
  data: AdminDashboardDTO | ProductStats;
  type: 'member' | 'sales' | 'auction' | 'review' | 'product' | 'category';
}

const DashboardChart: React.FC<DashboardChartProps> = ({ data, type }) => {
  if (!data) {
    return <div className="text-gray-500 text-center py-4">데이터가 없습니다.</div>;
  }
  
  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#AF19FF", "#FF4560"];

  // 타입 가드 함수
  const isAdminDashboard = (data: AdminDashboardDTO | ProductStats): data is AdminDashboardDTO => {
    return 'memberSummaryStats' in data;
  };

  const baseOptions: ApexOptions = {
    chart: {
      height: '100%',
      width: '100%',
      toolbar: {
        show: false,
      },
      animations: {
        enabled: true,
        speed: 800,
      }
    },
    legend: {
      position: 'bottom',
      offsetY: 10,
      itemMargin: {
        horizontal: 10,
        vertical: 5
      }
    },
    grid: {
      borderColor: '#f1f1f1',
    }
  };

  // 회원 통계 차트
  const memberOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'donut' },
    labels: [
      '전체 회원',
      '활성 회원',
      '비활성 회원',
      '전체 판매자',
      '활성 판매자',
      '비활성 판매자'
    ],
    colors: COLORS,
    dataLabels: {
      enabled: true,
      formatter: function (val: number) {
        return val.toFixed(1) + "%"
      },
    },
    tooltip: {
      y: {
        formatter: (val) => `${val} 명`
      }
    }
  };

  const memberSeries = isAdminDashboard(data) ? [
    data.memberSummaryStats?.totalMembers || 0,
    data.memberSummaryStats?.activeMembers || 0,
    data.memberSummaryStats?.inactiveMembers || 0,
    data.memberSummaryStats?.totalSellers || 0,
    data.memberSummaryStats?.activeSellers || 0,
    data.memberSummaryStats?.inactiveSellers || 0,
  ] : [];

  // 판매 통계 차트
  const salesOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'area' },
    colors: [COLORS[1]],
    xaxis: {
      categories: isAdminDashboard(data) ? data.productLog?.salesWithCommissions?.map((sale: any) => 
        new Date(sale.salesLog.soldAt).toLocaleDateString()
      ) || [] : [],
    },
    yaxis: {
      title: {
        text: '판매액 (원)',
        style: {
            color: '#aaa'
        }
      },
    },
    stroke: {
      curve: 'smooth',
      width: 3
    },
    fill: {
      type: 'gradient',
      gradient: {
        shadeIntensity: 1,
        opacityFrom: 0.7,
        opacityTo: 0.3,
        stops: [0, 90, 100]
      }
    },
  };

  const salesSeries = isAdminDashboard(data) ? [{
    name: '판매액',
    data: data.productLog?.salesWithCommissions?.map((sale: any) => sale.salesLog.totalPrice) || [],
  }] : [];

  // 경매 통계 차트
  const auctionOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'bar' },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: '55%',
        borderRadius: 5,
      },
    },
    xaxis: {
      categories: ['총 경매', '총 입찰', '평균 입찰'],
    },
    colors: [COLORS[0], COLORS[2], COLORS[3]],
    dataLabels: {
        enabled: false,
    }
  };

  const auctionSeries = isAdminDashboard(data) ? [{
    name: '경매 통계',
    data: [
      data.auctionSummaryStats?.totalAuctionsInPeriod || 0,
      data.auctionSummaryStats?.totalBidsInPeriod || 0,
      data.auctionSummaryStats?.averageBidsPerAuctionInPeriod || 0,
    ],
  }] : [];

  // 리뷰 통계 차트
  const reviewOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'donut' },
    labels: ['전체 리뷰', '신규 리뷰', '평균 평점 (x20)', '삭제율 (%)'],
    colors: COLORS,
  };

  const reviewSeries = isAdminDashboard(data) ? [
    data.reviewSummaryStats?.totalReviewsInPeriod || 0,
    data.reviewSummaryStats?.newReviewsInPeriod || 0,
    (data.reviewSummaryStats?.averageRatingInPeriod || 0) * 20, // 5점 만점을 100점으로 변환
    (data.reviewSummaryStats?.deletionRate || 0) * 100,
  ] : [];

  // 상품 품질 분포 차트
  const productOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'donut' },
    labels: ['상', '중', '하'],
    colors: ['#10B981', '#F59E0B', '#EF4444'],
    plotOptions: {
      pie: {
        donut: {
          size: '60%',
          labels: {
            show: true,
            name: {
              show: true,
              fontSize: '14px',
              fontWeight: 600,
              color: '#374151'
            },
            value: {
              show: true,
              fontSize: '16px',
              fontWeight: 'bold',
              color: '#111827',
              formatter: function (val: any) {
                return `${val}개`;
              }
            },
            total: {
              show: true,
              label: '총 상품',
              fontSize: '14px',
              fontWeight: 600,
              color: '#374151',
              formatter: function (w: any) {
                const total = w.globals.seriesTotals.reduce((a: number, b: number) => a + b, 0);
                return `${total}개`;
              }
            }
          }
        }
      }
    },
    dataLabels: {
      enabled: false
    },
    tooltip: {
      y: {
        formatter: (val) => `${val || 0} 개`
      }
    },
    legend: {
      position: 'bottom',
      offsetY: 0,
      itemMargin: {
        horizontal: 15,
        vertical: 8
      }
    }
  };

  const productSeries = !isAdminDashboard(data) ? [
    data.highQualityProducts,
    data.mediumQualityProducts,
    data.lowQualityProducts,
  ] : [];

  console.log('상품 시리즈 데이터:', productSeries);

  const categoryLabels = !isAdminDashboard(data) ? Object.keys(data.categoryStats) : [];

  // 카테고리별 분포 차트
  const categoryOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'bar' },
    plotOptions: {
      bar: {
        horizontal: true,
        borderRadius: 8,
        distributed: true,
        dataLabels: {
          position: 'center',
        },
      },
    },
    xaxis: {
      categories: categoryLabels,
      labels: {
        style: {
          fontSize: '12px',
          fontWeight: '500'
        }
      }
    },
    yaxis: {
      labels: {
        style: {
          fontSize: '11px',
          fontWeight: '600'
        }
      }
    },
    colors: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#06B6D4', '#F97316', '#84CC16'],
    dataLabels: {
      enabled: true,
      formatter: function (val: number) {
        return `${val}개`;
      },
      style: {
        fontSize: '11px',
        fontWeight: 'bold',
        colors: ['#fff']
      },
      offsetX: 10
    },
    tooltip: {
      y: {
        formatter: (val) => `${val} 개`
      }
    },
    grid: {
      borderColor: '#f1f1f1',
      xaxis: {
        lines: {
          show: false
        }
      }
    }
  };

  const categorySeries = !isAdminDashboard(data) ? [{
    name: '상품 수',
    data: Object.values(data.categoryStats)
  }] : [];

  const chartMap = {
    member: { options: memberOptions, series: memberSeries, type: 'donut' },
    sales: { options: salesOptions, series: salesSeries, type: 'area' },
    auction: { options: auctionOptions, series: auctionSeries, type: 'bar' },
    review: { options: reviewOptions, series: reviewSeries, type: 'donut' },
    product: { options: productOptions, series: productSeries, type: 'donut' },
    category: { options: categoryOptions, series: categorySeries, type: 'bar' },
  };

  const selectedChart = chartMap[type];

  return (
    <ReactApexChart 
      options={selectedChart.options} 
      series={selectedChart.series} 
      type={selectedChart.type as any} 
      height="100%" 
      width="100%" 
    />
  );
};

export default DashboardChart; 