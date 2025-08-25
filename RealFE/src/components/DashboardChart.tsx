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
  data: AdminDashboardDTO | ProductStats | any[];
  type: 'member' | 'sales' | 'auction' | 'review' | 'product' | 'category';
  periodType?: 'DAILY' | 'MONTHLY';
}

const DashboardChart: React.FC<DashboardChartProps> = ({ data, type, periodType = 'DAILY' }) => {

  
  if (!data || (Array.isArray(data) && data.length === 0)) {
    return <div className="text-gray-500 text-center py-4">데이터가 없습니다.</div>;
  }
  
  // 대시보드 테마에 맞는 색상 팔레트
  const COLORS = ["#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#06B6D4"];
  
  // 매출 전용 그라데이션 색상
  const SALES_COLORS = {
    primary: '#10B981',      // Emerald-500 (총 매출 카드와 동일)
    secondary: '#059669',    // Emerald-600
    light: '#34D399',        // Emerald-400
    gradient: {
      start: '#10B981',
      end: '#34D399'
    }
  };

  // 타입 가드 함수
  const isAdminDashboard = (data: AdminDashboardDTO | ProductStats | any[]): data is AdminDashboardDTO => {
    return !Array.isArray(data) && 'memberSummaryStats' in data;
  };

  const isProductStats = (data: AdminDashboardDTO | ProductStats | any[]): data is ProductStats => {
    return !Array.isArray(data) && 'highQualityProducts' in data;
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
  let salesOptions: ApexOptions;
  let salesSeries: any[] = [];

  if (type === 'sales') {
    // 실제 총 매출 데이터 사용 (총 매출 카드와 동일한 데이터 소스)
    const totalRevenue = isAdminDashboard(data) ? (data.salesSummaryStats?.totalRevenueInPeriod || 0) : 0;
    
    let xAxisCategories: string[];
    let chartTitle: string;
    let salesData: number[];
    let chartType: 'bar' | 'area';
    
    if (periodType === 'MONTHLY') {
      // 백엔드의 dailyRevenueTrend 데이터를 월별로 집계
      const dailyTrend = isAdminDashboard(data) ? (data.dailyRevenueTrend || []) : [];
      
      // 월별 매출 집계
      const monthlyAggregated = new Map<string, number>();
      
      dailyTrend.forEach(item => {
        if (item.date && item.value) {
          const date = new Date(item.date);
          const monthKey = `${date.getFullYear()}년 ${date.getMonth() + 1}월`;
          const currentAmount = monthlyAggregated.get(monthKey) || 0;
          monthlyAggregated.set(monthKey, currentAmount + item.value);
        }
      });
      
      // Map을 배열로 변환하고 날짜순 정렬
      const monthlyData = Array.from(monthlyAggregated.entries())
        .map(([label, value]) => ({ label, value }))
        .sort((a, b) => {
          // "2024년 7월" 형식을 파싱해서 정렬
          const parseMonth = (str: string) => {
            const match = str.match(/(\d{4})년 (\d{1,2})월/);
            if (!match) return 0;
            return parseInt(match[1]) * 100 + parseInt(match[2]);
          };
          return parseMonth(a.label) - parseMonth(b.label);
        });
      
      if (monthlyData.length > 0) {
        xAxisCategories = monthlyData.map(d => d.label);
        salesData = monthlyData.map(d => Math.round(d.value));
        chartTitle = '월간 매출액';
        chartType = monthlyData.length > 1 ? 'area' : 'bar';
      } else {
        // 데이터가 없으면 현재 월만 표시
        const currentDate = new Date();
        const currentMonthLabel = `${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월`;
        xAxisCategories = [currentMonthLabel];
        salesData = [totalRevenue];
        chartTitle = '월간 매출액';
        chartType = 'bar';
      }
    } else {
      // 일간은 현재 날짜만
      xAxisCategories = [new Date().toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })];
      salesData = [totalRevenue];
      chartTitle = '일간 매출액';
      chartType = 'bar';
    }
    
    salesOptions = {
      ...baseOptions,
      chart: { ...baseOptions.chart, type: chartType },
      colors: [SALES_COLORS.primary],
      xaxis: { 
        categories: xAxisCategories,
        labels: {
          style: {
            colors: '#6B7280',
            fontSize: '12px',
            fontWeight: '500'
          }
        }
      },
      yaxis: {
        title: {
          text: '매출액 (원)',
          style: { 
            color: '#6B7280',
            fontSize: '12px',
            fontWeight: '600'
          }
        },
        labels: {
          style: {
            colors: '#6B7280',
            fontSize: '11px'
          },
          formatter: function (val: number) {
            return val.toLocaleString() + '원';
          }
        }
      },
      stroke: { 
        curve: 'smooth', 
        width: chartType === 'area' ? 3 : 0,
        colors: [SALES_COLORS.primary]
      },
      fill: {
        type: chartType === 'area' ? 'gradient' : 'solid',
        gradient: { 
          shade: 'light',
          type: 'vertical',
          shadeIntensity: 0.5,
          gradientToColors: [SALES_COLORS.light],
          inverseColors: false,
          opacityFrom: 0.8,
          opacityTo: 0.1,
          stops: [0, 100]
        }
      },
      plotOptions: {
        bar: {
          borderRadius: 8,
          columnWidth: '60%',
          colors: {
            backgroundBarColors: ['#F3F4F6'],
            backgroundBarOpacity: 0.3
          }
        }
      },
      dataLabels: {
        enabled: chartType === 'bar',
        style: {
          fontSize: '11px',
          fontWeight: 'bold',
          colors: ['#ffffff']
        },
        background: {
          enabled: true,
          foreColor: SALES_COLORS.secondary,
          borderRadius: 4,
          borderWidth: 0,
          opacity: 0.9
        },
        formatter: function (val: number) {
          return val > 0 ? val.toLocaleString() + '원' : '0원';
        }
      },
      tooltip: {
        theme: 'light',
        style: {
          fontSize: '12px'
        },
        y: {
          formatter: function (val: number) {
            return val > 0 ? val.toLocaleString() + '원' : '0원';
          }
        }
      }
    };
    
    salesSeries = [{
      name: chartTitle,
      data: salesData,
    }];
  } else {
    // 기본값 - 데이터 형식을 인식하지 못한 경우
    const today = new Date().toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
    
    salesOptions = {
      ...baseOptions,
      chart: { ...baseOptions.chart, type: 'bar' },
      colors: [SALES_COLORS.primary],
      xaxis: { 
        categories: [today],
        labels: {
          style: {
            colors: '#6B7280',
            fontSize: '12px',
            fontWeight: '500'
          }
        }
      },
      yaxis: {
        title: {
          text: '매출액 (원)',
          style: { 
            color: '#6B7280',
            fontSize: '12px',
            fontWeight: '600'
          }
        },
        labels: {
          style: {
            colors: '#6B7280',
            fontSize: '11px'
          },
          formatter: function (val: number) {
            return val.toLocaleString() + '원';
          }
        }
      },
      plotOptions: {
        bar: {
          borderRadius: 8,
          columnWidth: '60%',
          colors: {
            backgroundBarColors: ['#F3F4F6'],
            backgroundBarOpacity: 0.3
          }
        }
      },
      dataLabels: {
        enabled: true,
        style: {
          fontSize: '11px',
          fontWeight: 'bold',
          colors: ['#ffffff']
        },
        formatter: function () {
          return '0원';
        }
      },
      tooltip: {
        theme: 'light',
        style: {
          fontSize: '12px'
        },
        y: {
          formatter: function () {
            return '0원';
          }
        }
      }
    };
    salesSeries = [{
      name: '매출액',
      data: [0],
    }];
  }

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

  const productSeries = isProductStats(data) ? [
    data.highQualityProducts,
    data.mediumQualityProducts,
    data.lowQualityProducts,
  ] : [];

  const categoryLabels = isProductStats(data) ? Object.keys(data.categoryStats) : [];

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

  const categorySeries = isProductStats(data) ? [{
    name: '상품 수',
    data: Object.values(data.categoryStats)
  }] : [];

  // 동적으로 차트 타입 결정 (항상 bar)

  const chartMap = {
    member: { options: memberOptions, series: memberSeries, type: 'donut' },
    sales: { options: salesOptions, series: salesSeries, type: 'bar' },
    auction: { options: auctionOptions, series: auctionSeries, type: 'bar' },
    review: { options: reviewOptions, series: reviewSeries, type: 'donut' },
    product: { options: productOptions, series: productSeries, type: 'donut' },
    category: { options: categoryOptions, series: categorySeries, type: 'bar' },
  };

  const selectedChart = chartMap[type];

  if (!selectedChart.options) {
    return <div className="text-gray-500 text-center py-4">데이터가 없습니다.</div>;
  }

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