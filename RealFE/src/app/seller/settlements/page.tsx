'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import SellerLayout from '@/components/layouts/SellerLayout';
import SellerHeader from '@/components/seller/SellerHeader';
import {
    getSellerSettlementList,
    getSellerSettlementListWithPaging,
    getSellerSettlementListByDate,
    getSellerSettlementListByPeriod,
    getSellerSettlementSummary,
    getSellerSettlementDetail,
} from '@/service/seller/sellerSettlementService';
import { 
    SellerSettlementResponse, 
    PayoutLogDetailResponse,
    DailySettlementItem,
    PageResponse
} from '@/types/seller/sellersettlement/sellerSettlement';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { 
    DollarSign, 
    TrendingUp, 
    Percent, 
    CreditCard, 
    Calendar, 
    Search, 
    RefreshCw, 
    Eye,
    Filter,
    BarChart3,
    Clock,
    CheckCircle,
    Calculator
} from 'lucide-react';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function SellerSettlementPage() {
    const checking = useSellerAuthGuard();
    const router = useRouter();
    
    // 상태 관리 - 원본 데이터와 하루별 그룹핑된 데이터
    const [settlements, setSettlements] = useState<SellerSettlementResponse[]>([]);
    const [dailyPayouts, setDailyPayouts] = useState<DailySettlementItem[]>([]);
    const [selectedPayout, setSelectedPayout] = useState<PayoutLogDetailResponse | null>(null);
    const [filterType, setFilterType] = useState<'all' | 'period'>('all');
    const [filterFrom, setFilterFrom] = useState('');
    const [filterTo, setFilterTo] = useState('');
    const [summary, setSummary] = useState<{
        totalPayoutAmount: number;
        totalCommission: number;
        totalSales: number;
        payoutCount: number;
    } | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    
    // 페이징 상태 관리
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [usePagination, setUsePagination] = useState(false);
    const {show} = useGlobalDialog();

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    // 🎯 최적화된 하루별 그룹핑 로직
    const createDailyPayoutsOptimized = async (payoutList: SellerSettlementResponse[]) => {
        try {
            console.log('📊 하루별 그룹핑 시작 (최적화된 버전)');
            
            // 날짜별로 데이터를 그룹핑할 맵
            const dailyDataMap: { [date: string]: DailySettlementItem } = {};
            
            // 🔥 최적화: 모든 상세 정보를 병렬로 조회
            const detailPromises = payoutList.map(payout => 
                getSellerSettlementDetail(payout.id).catch(err => {
                    console.error(`정산 상세 조회 실패 (ID: ${payout.id}):`, err);
                    return null;
                })
            );
            
            const allDetails = await Promise.all(detailPromises);
            
            // 각 정산 데이터와 상세 정보를 매칭하여 처리
            payoutList.forEach((payout, index) => {
                const detail = allDetails[index];
                
                if (detail && detail.salesDetails) {
                    // 상세 정보가 있는 경우: 날짜별로 분리
                    detail.salesDetails.forEach((saleDetail) => {
                        const saleDate = saleDetail.salesLog.soldAt.split('T')[0];
                        
                        // 필터링 적용 (기간별 필터가 활성화된 경우)
                        if (filterType === 'period' && filterFrom && filterTo) {
                            if (saleDate < filterFrom || saleDate > filterTo) {
                                return; // 해당 건 제외
                            }
                        }
                        
                        if (!dailyDataMap[saleDate]) {
                            dailyDataMap[saleDate] = {
                                id: `daily_${saleDate}`,
                                originalPayoutId: payout.id,
                                sellerId: payout.sellerId,
                                date: saleDate,
                                periodStart: saleDate,
                                periodEnd: saleDate,
                                totalSales: 0,
                                totalCommission: 0,
                                payoutAmount: 0,
                                processedAt: payout.processedAt,
                                salesCount: 0,
                                salesDetails: []
                            };
                        }
                        
                        // 날짜별 합계 누적
                        dailyDataMap[saleDate].totalSales += saleDetail.salesLog.totalPrice;
                        dailyDataMap[saleDate].totalCommission += saleDetail.commissionLog.commissionAmount;
                        dailyDataMap[saleDate].payoutAmount += (saleDetail.salesLog.totalPrice - saleDetail.commissionLog.commissionAmount);
                        dailyDataMap[saleDate].salesCount += 1;
                        dailyDataMap[saleDate].salesDetails.push(saleDetail);
                    });
                } else {
                    // 상세 조회 실패 시: 원본 데이터를 날짜별로 변환
                    const fallbackDate = payout.periodStart;
                    if (!dailyDataMap[fallbackDate]) {
                        dailyDataMap[fallbackDate] = {
                            id: `daily_${fallbackDate}_fallback`,
                            originalPayoutId: payout.id,
                            sellerId: payout.sellerId,
                            date: fallbackDate,
                            periodStart: fallbackDate,
                            periodEnd: payout.periodEnd,
                            totalSales: payout.totalSales,
                            totalCommission: payout.totalCommission,
                            payoutAmount: payout.payoutAmount,
                            processedAt: payout.processedAt,
                            salesCount: 1,
                            salesDetails: []
                        };
                    }
                }
            });
            
            // 맵을 배열로 변환하고 날짜순 정렬 (최신순)
            const dailyData = Object.values(dailyDataMap).sort((a, b) => {
                return new Date(b.date).getTime() - new Date(a.date).getTime();
            });
            
            console.log('📊 하루별 그룹핑 완료:', {
                총정산건수: payoutList.length,
                하루별건수: dailyData.length,
                데이터: dailyData
            });
            
            setDailyPayouts(dailyData);
            
        } catch (error) {
            console.error('하루별 그룹핑 실패:', error);
            setDailyPayouts([]);
        }
    };

    // 전체 정산 내역 조회 (페이징 지원)
    const fetchAll = async (page: number = 0) => {
    try {
        setLoading(true);
        console.log('📊 정산 내역 조회 시작:', { page, usePagination });

        let res: SellerSettlementResponse[] | PageResponse<SellerSettlementResponse>;

        if (usePagination) {
        res = await getSellerSettlementListWithPaging(page, pageSize);
        console.log('📊 페이징 정산 데이터:', res);

        setSettlements(res.content || []);
        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
        setCurrentPage(res.number);

        await createDailyPayoutsOptimized(res.content || []);
        } else {
        res = await getSellerSettlementList();
        console.log('📊 전체 정산 데이터:', res);

        setSettlements(res || []);
        setTotalPages(0);
        setTotalElements(res.length);
        setCurrentPage(0);

        await createDailyPayoutsOptimized(res);
        }

        setSummary(null);
        setError(null);
    } catch (err) {
        console.error('정산 목록 조회 실패:', err);
        setError('정산 데이터를 불러오는 데 실패했습니다.');
    } finally {
        setLoading(false);
    }
    };



    // 기간별 필터링
    const fetchFilteredByPeriod = async (from: string, to: string) => {
        try {
            setLoading(true);
            console.log('📊 기간별 정산 조회:', { from, to });
            
            const [res, summaryRes] = await Promise.all([
                getSellerSettlementListByPeriod(from, to),
                getSellerSettlementSummary(from, to).catch(() => null)
            ]);
            
            console.log('📊 기간별 정산 데이터:', res);
            console.log('📊 요약 정보:', summaryRes);
            
            setSettlements(res || []);
            setSummary(summaryRes);
            
            // 하루별로 그룹핑 (기간 필터 적용)
            await createDailyPayoutsOptimized(res || []);
            setError(null);
        } catch (err: any) {
            console.error('기간 필터 조회 실패:', err);
            setError('해당 기간의 정산 데이터를 불러오지 못했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 정산 상세 정보 조회 (상세보기 모달용)
    const fetchPayoutDetailForModal = async (date: string, dailyItem: DailySettlementItem) => {
        try {
            console.log('하루별 상세 정보 표시:', date);
            
            // 이미 하루별 데이터에 상세 정보가 있는 경우 그대로 사용
            if (dailyItem.salesDetails && dailyItem.salesDetails.length > 0) {
                const mockDetail = {
                    payoutInfo: {
                        id: dailyItem.originalPayoutId,
                        sellerId: dailyItem.sellerId,
                        periodStart: dailyItem.date,
                        periodEnd: dailyItem.date,
                        totalSales: dailyItem.totalSales,
                        totalCommission: dailyItem.totalCommission,
                        payoutAmount: dailyItem.payoutAmount,
                        processedAt: dailyItem.processedAt
                    },
                    salesDetails: dailyItem.salesDetails
                };
                setSelectedPayout(mockDetail);
            } else {
                // 상세 정보가 없는 경우 API 호출
                const res = await getSellerSettlementDetail(dailyItem.originalPayoutId);
            setSelectedPayout(res);
            }
        } catch (err: any) {
            console.error('정산 상세 조회 실패:', err);
            
            let errorMessage = '정산 상세 정보를 불러오지 못했습니다.';
            if (err.response?.status === 500) {
                errorMessage = '서버에서 오류가 발생했습니다. 백엔드 팀에 문의해주세요.';
            } else if (err.response?.status === 404) {
                errorMessage = '해당 정산 정보를 찾을 수 없습니다.';
            } else if (err.response?.status === 403) {
                errorMessage = '정산 정보에 접근할 권한이 없습니다.';
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            }
            
            setError(errorMessage);
            show(errorMessage);
        }
    };

    // 필터 타입 변경 핸들러
    const handleFilterTypeChange = (type: 'all' | 'period') => {
        console.log('필터 타입 변경:', type);
        setFilterType(type);
        
        if (type === 'all') {
            setFilterFrom('');
            setFilterTo('');
        fetchAll();
        }
    };

    // 기간 필터 변경 핸들러
    const handlePeriodChange = (from: string, to: string) => {
        console.log('기간 변경:', { from, to });
        setFilterFrom(from);
        setFilterTo(to);
    };
        
    // 초기 로딩
    useEffect(() => {
        if (checking) return;
        fetchAll();
    }, [checking]);

    // 기간별 필터 자동 실행
    useEffect(() => {
        if (filterType === 'period' && filterFrom && filterTo) {
            console.log('🔄 기간별 필터 자동 실행:', filterFrom, '~', filterTo);
            fetchFilteredByPeriod(filterFrom, filterTo);
        }
    }, [filterType, filterFrom, filterTo]);

    // 페이징 관련 함수들
    const handlePageChange = (newPage: number) => {
        setCurrentPage(newPage);
        fetchAll(newPage);
    };

    const handlePageSizeChange = (newSize: number) => {
        setPageSize(newSize);
        setCurrentPage(0);
        fetchAll(0);
    };

    const togglePagination = () => {
        setUsePagination(!usePagination);
        setCurrentPage(0);
        fetchAll(0);
    };

    // 🎯 하루별 그룹핑된 데이터 기준 통계 계산
    const totalSettlements = dailyPayouts.length;
    const totalSales = dailyPayouts.reduce((sum, item) => sum + item.totalSales, 0);
    const totalCommission = dailyPayouts.reduce((sum, item) => sum + item.totalCommission, 0);
    const totalPayout = dailyPayouts.reduce((sum, item) => sum + item.payoutAmount, 0);

    if (checking || loading) {
        return (
            <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-white flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#a89f91] mx-auto mb-4"></div>
                    <p className="text-[#5b4636]">정산 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="hidden">
                <SellerHeader toggleSidebar={toggleSidebar} />
            </div>
            <SellerLayout>
                <div className="flex-1 w-full h-full px-4 py-8">
                    <div className="mb-6">
                        <h1 className="text-xl md:text-2xl font-bold text-[#374151]">정산 관리</h1>
                        {filterType === 'period' && filterFrom && filterTo && (
                            <p className="text-sm text-[#6b7280] mt-1">
                                조회 기간: {filterFrom} ~ {filterTo} (판매일 기준)
                            </p>
                        )}
                        {filterType === 'all' && (
                            <p className="text-sm text-[#6b7280] mt-1">
                                전체 기간 조회 (하루별 합계 표시)
                            </p>
                        )}
                    </div>

                    {/* 상단 통계 카드 */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                        <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                            <div className="flex items-center gap-3 mb-2">
                                <CheckCircle className="w-8 h-8 text-green-600" />
                                <span className="text-[#374151] text-sm font-semibold">정산 일수</span>
                            </div>
                            <div className="text-2xl font-bold text-green-600">{totalSettlements}일</div>
                            <div className="text-sm font-semibold text-green-600">{totalPayout.toLocaleString()}원</div>
                            <div className="text-xs text-[#6b7280] mt-1">하루별 합계 기준</div>
                        </section>
                        <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                            <div className="flex items-center gap-3 mb-2">
                                <DollarSign className="w-8 h-8 text-[#374151]" />
                                <span className="text-[#374151] text-sm font-semibold">총 정산액</span>
                            </div>
                            <div className="text-2xl font-bold text-[#374151]">
                                {summary ? summary.totalPayoutAmount.toLocaleString() : totalPayout.toLocaleString()}원
                            </div>
                            <div className="text-xs text-[#6b7280] mt-1">
                                {filterType === 'period' && `${filterFrom}~${filterTo}`}
                                {filterType === 'all' && '전체 기간'}
                            </div>
                        </section>
                        <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                            <div className="flex items-center gap-3 mb-2">
                                <TrendingUp className="w-8 h-8 text-blue-600" />
                                <span className="text-[#374151] text-sm font-semibold">일평균 정산액</span>
                    </div>
                            <div className="text-2xl font-bold text-blue-600">
                                {totalSettlements > 0 ? Math.round(totalPayout / totalSettlements).toLocaleString() : 0}원
                            </div>
                            <div className="text-xs text-[#6b7280] mt-1">하루 평균 지급액</div>
                        </section>
                        </div>

                    {/* 필터 섹션 */}
                    <div className="bg-[#f3f4f6] p-4 rounded-lg shadow-sm border-2 border-[#d1d5db] mb-6">
                        <div className="flex items-center gap-2 mb-4">
                            <Filter className="w-5 h-5 text-[#6b7280]" />
                            <h3 className="text-[#374151] font-semibold">필터 옵션</h3>
                            <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full">실시간 적용</span>
                        </div>
                        
                        <div className="flex flex-col md:flex-row gap-4 items-end">
                            {/* 필터 타입 선택 */}
                            <div className="flex gap-2">
                                <button
                                    onClick={() => handleFilterTypeChange('all')}
                                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                        filterType === 'all' 
                                            ? 'bg-[#d1d5db] text-[#374151] shadow-sm'
                                            : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb] hover:text-[#374151]'
                                    }`}
                                >
                                    전체
                                </button>
                                <button
                                    onClick={() => handleFilterTypeChange('period')}
                                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                        filterType === 'period' 
                                            ? 'bg-[#d1d5db] text-[#374151] shadow-sm'
                                            : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb] hover:text-[#374151]'
                                    }`}
                                >
                                    기간별
                                </button>
                            </div>

                            {/* 페이징 토글 */}
                            {filterType === 'all' && (
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={togglePagination}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            usePagination 
                                                ? 'bg-blue-500 text-white shadow-sm'
                                                : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                        }`}
                                    >
                                        {usePagination ? '페이징 ON' : '페이징 OFF'}
                                    </button>
                                    {usePagination && (
                                        <select
                                            value={pageSize}
                                            onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                                            className="border border-[#d1d5db] rounded-md px-2 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white text-[#374151] text-sm"
                                        >
                                            <option value={5}>5개씩</option>
                                            <option value={10}>10개씩</option>
                                            <option value={20}>20개씩</option>
                                            <option value={50}>50개씩</option>
                                        </select>
                                    )}
                                </div>
                            )}

                            {/* 기간별 필터 */}
                            {filterType === 'period' && (
                                <div className="flex items-center gap-2">
                                    <Calendar className="w-5 h-5 text-[#6b7280]" />
                                    <input
                                        type="date"
                                        value={filterFrom}
                                        onChange={(e) => handlePeriodChange(e.target.value, filterTo)}
                                        placeholder="시작일"
                                        className="border border-[#d1d5db] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white text-[#374151] transition-all"
                                    />
                                    <span className="text-[#374151]">~</span>
                                    <input
                                        type="date"
                                        value={filterTo}
                                        onChange={(e) => handlePeriodChange(filterFrom, e.target.value)}
                                        placeholder="종료일"
                                        className="border border-[#d1d5db] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300 bg-white text-[#374151] transition-all"
                            />
                        </div>
                            )}
                        </div>
                    </div>

                    {/* 🎯 하루별 정산 리스트 */}
                    {error ? (
                        <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-lg p-4">
                            <p className="text-[#374151]">{error}</p>
                        </div>
                    ) : dailyPayouts.length === 0 ? (
                        <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-lg p-8 text-center">
                            <CreditCard className="w-12 h-12 text-[#6b7280] mx-auto mb-4" />
                            <p className="text-[#6b7280] text-lg">
                                {filterType === 'period' ? '해당 기간에 판매된 주문의 정산 내역이 없습니다.' : '판매된 주문의 정산 내역이 없습니다.'}
                            </p>
                        </div>
                    ) : (
                        <>
                            {/* 데이터 안내 */}
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
                                <div className="flex items-start gap-3">
                                    <div className="w-5 h-5 bg-blue-500 rounded-full text-white text-xs flex items-center justify-center mt-0.5">i</div>
                                    <div>
                                        <h4 className="font-semibold text-blue-800 mb-1">정산 데이터 안내</h4>
                                        <p className="text-blue-700 text-sm">
                                            각 행은 하루치 판매 합계를 나타내며, "상세 보기"를 클릭하여 해당 날짜의 개별 주문 내역을 확인할 수 있습니다.
                                        </p>
                                    </div>
                                </div>
                            </div>
                            
                        <div className="overflow-x-auto bg-[#f3f4f6] rounded-lg shadow-sm border border-[#d1d5db]">
                            <table className="min-w-full divide-y divide-[#d1d5db]">
                                <thead className="bg-[#f3f4f6]">
                                    <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">판매일</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">판매건수</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">총 매출</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">수수료</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">지급액</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#6b7280] uppercase tracking-wider">상세보기</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-[#f3f4f6] divide-y divide-[#d1d5db]">
                                        {dailyPayouts.map((item) => (
                                            <tr key={item.id} className="bg-white hover:bg-gray-50 transition-colors">
                                            <td className="px-6 py-4 whitespace-nowrap font-medium text-[#374151]">
                                                            <div className="font-semibold">
                                                        {item.date}
                                                        </div>
                                                    </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-[#374151] font-semibold">
                                                    {item.salesCount}건
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap text-[#374151] font-semibold">
                                                {item.totalSales.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-[#374151]">
                                                {item.totalCommission.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap font-semibold text-[#374151]">
                                                {item.payoutAmount.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-center">
                                                <button
                                                        onClick={() => fetchPayoutDetailForModal(item.date, item)}
                                                    className="inline-flex items-center gap-1 bg-[#d1d5db] text-[#374151] px-3 py-1.5 rounded hover:bg-[#e5e7eb] hover:text-[#374151] text-sm transition-colors"
                                                >
                                                    <Eye className="w-4 h-4" /> 상세 보기
                                                </button>
                                            </td>
                                        </tr>
                                        ))}
                                </tbody>
                            </table>
                        </div>

                        {/* 페이징 UI */}
                        {usePagination && totalPages > 1 && (
                            <div className="mt-6 flex items-center justify-between">
                                <div className="text-sm text-[#6b7280]">
                                    총 {totalElements}개 중 {(currentPage * pageSize) + 1}~{Math.min((currentPage + 1) * pageSize, totalElements)}개 표시
                                </div>
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={() => handlePageChange(0)}
                                        disabled={currentPage === 0}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentPage === 0
                                                ? 'bg-[#f3f4f6] text-[#9ca3af] cursor-not-allowed'
                                                : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                        }`}
                                    >
                                        처음
                                    </button>
                                    <button
                                        onClick={() => handlePageChange(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentPage === 0
                                                ? 'bg-[#f3f4f6] text-[#9ca3af] cursor-not-allowed'
                                                : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                        }`}
                                    >
                                        이전
                                    </button>
                                    
                                    {/* 페이지 번호들 */}
                                    <div className="flex gap-1">
                                        {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                            let pageNum;
                                            if (totalPages <= 5) {
                                                pageNum = i;
                                            } else if (currentPage < 3) {
                                                pageNum = i;
                                            } else if (currentPage >= totalPages - 3) {
                                                pageNum = totalPages - 5 + i;
                                            } else {
                                                pageNum = currentPage - 2 + i;
                                            }
                                            
                                            return (
                                                <button
                                                    key={pageNum}
                                                    onClick={() => handlePageChange(pageNum)}
                                                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                                        currentPage === pageNum
                                                            ? 'bg-blue-500 text-white'
                                                            : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                                    }`}
                                                >
                                                    {pageNum + 1}
                                                </button>
                                            );
                                        })}
                                    </div>
                                    
                                    <button
                                        onClick={() => handlePageChange(currentPage + 1)}
                                        disabled={currentPage === totalPages - 1}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentPage === totalPages - 1
                                                ? 'bg-[#f3f4f6] text-[#9ca3af] cursor-not-allowed'
                                                : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                        }`}
                                    >
                                        다음
                                    </button>
                                    <button
                                        onClick={() => handlePageChange(totalPages - 1)}
                                        disabled={currentPage === totalPages - 1}
                                        className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                            currentPage === totalPages - 1
                                                ? 'bg-[#f3f4f6] text-[#9ca3af] cursor-not-allowed'
                                                : 'bg-[#f3f4f6] text-[#374151] hover:bg-[#e5e7eb]'
                                        }`}
                                    >
                                        마지막
                                    </button>
                                </div>
                            </div>
                        )}
                        </>
                    )}

                    {/* 정산 상세 정보 모달 */}
                    {selectedPayout && (
                        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                            <div className="bg-[#f3f4f6] rounded-lg p-6 max-w-4xl w-full mx-4 max-h-[80vh] overflow-y-auto border-2 border-[#d1d5db]">
                                <div className="flex justify-between items-center mb-4">
                                    <h3 className="text-xl font-bold text-[#374151]">
                                        {selectedPayout.payoutInfo.periodStart} 날짜별 정산 상세 내역
                                    </h3>
                                    <button
                                        onClick={() => setSelectedPayout(null)}
                                        className="text-[#374151] hover:text-[#b94a48]"
                                    >
                                        ✕
                                    </button>
                                </div>
                                
                                {/* 정산 기본 정보 */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                                    <div className="bg-white p-4 rounded-lg border border-[#d1d5db]">
                                        <h4 className="font-semibold text-[#374151] mb-2">판매일</h4>
                                        <p className="text-[#374151]">
                                            {selectedPayout.payoutInfo.periodStart}
                                            {selectedPayout.payoutInfo.periodStart !== selectedPayout.payoutInfo.periodEnd && 
                                                ` ~ ${selectedPayout.payoutInfo.periodEnd}`
                                            }
                                        </p>
                                    </div>
                                    <div className="bg-white p-4 rounded-lg border border-[#d1d5db]">
                                        <h4 className="font-semibold text-[#374151] mb-2">총 매출</h4>
                                        <p className="text-[#388e3c] font-bold">
                                            {selectedPayout.payoutInfo.totalSales.toLocaleString()}원
                                                        </p>
                                    </div>
                                    <div className="bg-white p-4 rounded-lg border border-[#d1d5db]">
                                        <h4 className="font-semibold text-[#374151] mb-2">수수료</h4>
                                        <p className="text-[#374151] font-bold">
                                            {selectedPayout.payoutInfo.totalCommission.toLocaleString()}원
                                                        </p>
                                    </div>
                                    <div className="bg-white p-4 rounded-lg border border-[#d1d5db]">
                                        <h4 className="font-semibold text-[#374151] mb-2">지급액</h4>
                                        <p className="text-[#6b7280] font-bold">
                                            {selectedPayout.payoutInfo.payoutAmount.toLocaleString()}원
                                                        </p>
                                    </div>
                                </div>

                                {/* 판매 상세 내역 */}
                                <div className="bg-white rounded-lg p-4 border border-[#d1d5db]">
                                    <h4 className="font-semibold text-[#374151] mb-4 flex items-center gap-2">
                                        <BarChart3 className="w-5 h-5" />
                                        개별 주문 내역 ({selectedPayout.salesDetails.length}건)
                                    </h4>
                                    
                                    {selectedPayout.salesDetails.length === 0 ? (
                                        <p className="text-[#374151] text-center py-4">주문 내역이 없습니다.</p>
                                    ) : (
                                        <div className="overflow-x-auto">
                                            <table className="min-w-full divide-y divide-gray-200">
                                                <thead>
                                                    <tr>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">상품ID</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">고객ID</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">수량</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">단가</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">총액</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">수수료</th>
                                                        <th className="px-4 py-2 text-left text-xs font-medium text-[#374151]">판매일시</th>
                                                    </tr>
                                                </thead>
                                                <tbody className="divide-y divide-gray-200">
                                                    {selectedPayout.salesDetails.map((detail, index) => (
                                                        <tr key={index} className="hover:bg-gray-50">
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.salesLog.productId}
                                                            </td>
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.salesLog.customerId}
                                                            </td>
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.salesLog.quantity}개
                                                            </td>
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.salesLog.unitPrice.toLocaleString()}원
                                                            </td>
                                                            <td className="px-4 py-2 text-sm font-semibold text-[#388e3c]">
                                                                {detail.salesLog.totalPrice.toLocaleString()}원
                                                            </td>
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.commissionLog.commissionAmount.toLocaleString()}원
                                                            </td>
                                                            <td className="px-4 py-2 text-sm text-[#374151]">
                                                                {detail.salesLog.soldAt}
                                                            </td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </SellerLayout>
        </>
    );
}
