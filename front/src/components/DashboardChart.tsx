"use client";
import React from 'react';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { ApexOptions } from 'apexcharts';

const ReactApexChart = dynamic(() => import('react-apexcharts'), { ssr: false });

interface DashboardChartProps {
  data: AdminDashboardDTO;
  type: 'member' | 'sales' | 'auction' | 'review';
}

const DashboardChart: React.FC<DashboardChartProps> = ({ data, type }) => {
  if (!data) {
    return <div className="text-gray-500 text-center py-4">데이터가 없습니다.</div>;
  }
  
  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#AF19FF", "#FF4560"];

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

  const memberSeries = [
    data.memberSummaryStats?.totalMembers || 0,
    data.memberSummaryStats?.activeMembers || 0,
    data.memberSummaryStats?.inactiveMembers || 0,
    data.memberSummaryStats?.totalSellers || 0,
    data.memberSummaryStats?.activeSellers || 0,
    data.memberSummaryStats?.inactiveSellers || 0,
  ];

  // 판매 통계 차트
  const salesOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'area' },
    colors: [COLORS[1]],
    xaxis: {
      categories: data.productLog?.salesWithCommissions?.map(sale => 
        new Date(sale.salesLog.soldAt).toLocaleDateString()
      ) || [],
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

  const salesSeries = [{
    name: '판매액',
    data: data.productLog?.salesWithCommissions?.map(sale => sale.salesLog.totalPrice) || [],
  }];

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

  const auctionSeries = [{
    name: '경매 통계',
    data: [
      data.auctionSummaryStats?.totalAuctionsInPeriod || 0,
      data.auctionSummaryStats?.totalBidsInPeriod || 0,
      data.auctionSummaryStats?.averageBidsPerAuctionInPeriod || 0,
    ],
  }];

  // 리뷰 통계 차트
  const reviewOptions: ApexOptions = {
    ...baseOptions,
    chart: { ...baseOptions.chart, type: 'donut' },
    labels: ['전체 리뷰', '신규 리뷰', '평균 평점 (x20)', '삭제율 (%)'],
    colors: COLORS,
  };

  const reviewSeries = [
    data.reviewSummaryStats?.totalReviewsInPeriod || 0,
    data.reviewSummaryStats?.newReviewsInPeriod || 0,
    (data.reviewSummaryStats?.averageRatingInPeriod || 0) * 20, // 5점 만점을 100점으로 변환
    (data.reviewSummaryStats?.deletionRate || 0) * 100,
  ];

  const chartMap = {
    member: { options: memberOptions, series: memberSeries, type: 'donut' },
    sales: { options: salesOptions, series: salesSeries, type: 'area' },
    auction: { options: auctionOptions, series: auctionSeries, type: 'bar' },
    review: { options: reviewOptions, series: reviewSeries, type: 'donut' },
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