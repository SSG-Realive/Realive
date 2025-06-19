"use client";
import React from 'react';
import dynamic from 'next/dynamic';
import { AdminDashboardDTO } from '@/types/admin/admin';
import { ApexOptions } from 'apexcharts';

const ReactApexChart = dynamic(() => import('react-apexcharts'), { ssr: false });

interface DashboardChartProps {
  data: AdminDashboardDTO;
}

const DashboardChart: React.FC<DashboardChartProps> = ({ data }) => {
  if (!data) {
    return <div className="text-gray-500 text-center py-4">데이터가 없습니다.</div>;
  }

  // 회원 통계 차트
  const memberOptions: ApexOptions = {
    chart: {
      type: 'donut',
      height: 350,
    },
    labels: [
      '전체 회원',
      '활성 회원',
      '비활성 회원',
      '전체 판매자',
      '활성 판매자',
      '비활성 판매자'
    ],
    title: {
      text: '회원 통계',
      align: 'left',
    },
    colors: ['#4CAF50', '#2196F3', '#FFC107', '#9C27B0', '#00BCD4', '#FF5722'],
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
    chart: {
      type: 'line',
      height: 350,
    },
    xaxis: {
      categories: data.productLog?.salesWithCommissions?.map(sale => 
        new Date(sale.salesLog.soldAt).toLocaleDateString()
      ) || [],
    },
    title: {
      text: '판매 추이',
      align: 'left',
    },
    yaxis: {
      title: {
        text: '판매액',
      },
    },
  };

  const salesSeries = [{
    name: '판매액',
    data: data.productLog?.salesWithCommissions?.map(sale => sale.salesLog.totalPrice) || [],
  }];

  // 경매 통계 차트
  const auctionOptions: ApexOptions = {
    chart: {
      type: 'bar',
      height: 350,
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: '55%',
      },
    },
    title: {
      text: '경매 통계',
      align: 'left',
    },
    xaxis: {
      categories: ['총 경매', '총 입찰', '평균 입찰'],
    },
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
    chart: {
      type: 'donut',
      height: 350,
    },
    labels: ['전체 리뷰', '신규 리뷰', '평균 평점', '삭제율'],
    title: {
      text: '리뷰 통계',
      align: 'left',
    },
    colors: ['#4CAF50', '#2196F3', '#FFC107', '#F44336'],
  };

  const reviewSeries = [
    data.reviewSummaryStats?.totalReviewsInPeriod || 0,
    data.reviewSummaryStats?.newReviewsInPeriod || 0,
    (data.reviewSummaryStats?.averageRatingInPeriod || 0) * 20, // 5점 만점을 100점으로 변환
    (data.reviewSummaryStats?.deletionRate || 0) * 100,
  ];

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '24px', padding: '24px' }}>
      <div style={{ background: '#fff', borderRadius: '12px', boxShadow: '0 2px 8px #eee', padding: '24px' }}>
        <ReactApexChart options={memberOptions} series={memberSeries} type="donut" height={350} />
      </div>
      <div style={{ background: '#fff', borderRadius: '12px', boxShadow: '0 2px 8px #eee', padding: '24px' }}>
        <ReactApexChart options={salesOptions} series={salesSeries} type="line" height={350} />
      </div>
      <div style={{ background: '#fff', borderRadius: '12px', boxShadow: '0 2px 8px #eee', padding: '24px' }}>
        <ReactApexChart options={auctionOptions} series={auctionSeries} type="bar" height={350} />
      </div>
      <div style={{ background: '#fff', borderRadius: '12px', boxShadow: '0 2px 8px #eee', padding: '24px' }}>
        <ReactApexChart options={reviewOptions} series={reviewSeries} type="donut" height={350} />
      </div>
    </div>
  );
};

export default DashboardChart; 