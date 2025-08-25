'use client';

import SellerLayout from '@/components/layouts/SellerLayout';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { 
  createAdminInquiry, 
  getAdminInquiryList, 
  getAdminInquiryStatistics,
  AdminInquiryRequest, 
  AdminInquiryResponse,
  AdminInquiryListResponse,
  AdminInquiryStatistics
} from '@/service/seller/adminInquiryService';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { 
  MessageCircle, 
  Send, 
  CheckCircle, 
  Clock,
  Plus,
  Search
} from 'lucide-react';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function SellerAdminQnaPage() {
  const checking = useSellerAuthGuard();
  const router = useRouter();
  const { show } = useGlobalDialog();
  
  const [inquiryList, setInquiryList] = useState<AdminInquiryResponse[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // 통계 상태 추가
  const [statistics, setStatistics] = useState<AdminInquiryStatistics | null>(null);
  const [statisticsLoading, setStatisticsLoading] = useState(false);

  // 폼 상태
  const [form, setForm] = useState<AdminInquiryRequest>({
    title: '',
    content: ''
  });

  // 필터 상태
  const [searchKeyword, setSearchKeyword] = useState('');

  // 통계 데이터 로딩
  const fetchStatistics = async () => {
    try {
      setStatisticsLoading(true);
      const statsData = await getAdminInquiryStatistics();
      setStatistics(statsData);
    } catch (err: any) {
      console.error('통계 조회 실패:', err);
      // 통계 조회 실패 시 기본값 설정
      setStatistics({
        totalCount: 0,
        unansweredCount: 0,
        answeredCount: 0,
        answerRate: 0
      });
    } finally {
      setStatisticsLoading(false);
    }
  };

  // 문의 목록 로딩
  const fetchInquiries = async (page = 0) => {
    try {
      setLoading(true);
      setError(null);
      
      const searchParams: Record<string, any> = {
        page,
        size: 10,
        ...(searchKeyword && { keyword: searchKeyword })
      };

      // 목록과 통계를 병렬로 조회
      const [response, _] = await Promise.all([
        getAdminInquiryList(searchParams),
        fetchStatistics()
      ]);
      
      setInquiryList(response.content || []);
      setTotalPages(response.totalPages || 1);
      setTotalElements(response.totalElements || 0);
      setCurrentPage(page);
    } catch (err: any) {
      console.error('문의 목록 조회 실패:', err);
      setError('문의 목록을 불러오는데 실패했습니다.');
      setInquiryList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!checking) {
      fetchInquiries(0);
    }
  }, [checking]);

  // 문의 등록
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!form.title.trim() || !form.content.trim()) {
      await show('제목과 내용을 모두 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      await createAdminInquiry(form);
      
      // 폼 초기화
      setForm({
        title: '',
        content: ''
      });
      
      await show('문의가 성공적으로 등록되었습니다.');
      fetchInquiries(0); // 목록 및 통계 새로고침
    } catch (err: any) {
      console.error('문의 등록 실패:', err);
      await show('문의 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  // 검색 처리
  const handleSearch = () => {
    fetchInquiries(0);
  };

  // 상태별 배지
  const getStatusBadge = (inquiry: AdminInquiryResponse) => {
    const isAnswered = inquiry.isAnswered || !!inquiry.answer || inquiry.status === 'ANSWERED';
    if (isAnswered) {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium bg-green-50 text-green-600">
          <CheckCircle className="w-3 h-3" />
          답변완료
        </span>
      );
    } else {
      return (
        <span className="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium bg-yellow-50 text-yellow-600">
          <Clock className="w-3 h-3" />
          대기중
        </span>
      );
    }
  };

  if (checking) {
    return (
      <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#6b7280] mx-auto mb-4"></div>
          <p className="text-[#374151]">인증 확인 중...</p>
        </div>
      </div>
    );
  }

  if (checking) {
    return (
      <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#6b7280] mx-auto mb-4"></div>
          <p className="text-[#374151]">인증 확인 중...</p>
        </div>
      </div>
    );
  }

  return (
    <SellerLayout>
      <div className="flex-1 w-full h-full px-4 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-xl md:text-2xl font-bold text-[#374151]">관리자 문의</h1>
          <div className="flex items-center gap-2 text-sm text-[#6b7280]">
            <MessageCircle className="w-4 h-4" />
            총 {statistics?.totalCount || 0}건의 문의 (전체 데이터 기준)
          </div>
        </div>

        {/* 상단 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 min-h-[140px] flex flex-col justify-center items-center">
            <div className="flex items-center gap-3 mb-2">
              <MessageCircle className="w-6 h-6 text-[#6b7280]" />
              <div className="text-center">
                <div className="text-2xl font-bold text-[#374151]">
                  {statisticsLoading ? '...' : (statistics?.totalCount || 0)}
                </div>
                <div className="text-sm text-[#6b7280]">전체 문의</div>
                <div className="text-xs text-[#6b7280] mt-1">전체 데이터 기준</div>
              </div>
            </div>
          </div>
          
          <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 min-h-[140px] flex flex-col justify-center items-center">
            <div className="flex items-center gap-3 mb-2">
              <Clock className="w-6 h-6 text-[#6b7280]" />
              <div className="text-center">
                <div className="text-2xl font-bold text-[#374151]">
                  {statisticsLoading ? '...' : (statistics?.unansweredCount || 0)}
                </div>
                <div className="text-sm text-[#6b7280]">미답변</div>
                <div className="text-xs text-[#6b7280] mt-1">전체 데이터 기준</div>
              </div>
            </div>
          </div>
          
          <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 min-h-[140px] flex flex-col justify-center items-center">
            <div className="flex items-center gap-3 mb-2">
              <CheckCircle className="w-6 h-6 text-[#6b7280]" />
              <div className="text-center">
                <div className="text-2xl font-bold text-[#374151]">
                  {statisticsLoading ? '...' : (statistics?.answeredCount || 0)}
                </div>
                <div className="text-sm text-[#6b7280]">답변완료</div>
                <div className="text-xs text-[#6b7280] mt-1">전체 데이터 기준</div>
              </div>
            </div>
          </div>
          
          <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 min-h-[140px] flex flex-col justify-center items-center">
            <div className="flex items-center gap-3 mb-2">
              <MessageCircle className="w-6 h-6 text-[#6b7280]" />
              <div className="text-center">
                <div className="text-2xl font-bold text-[#374151]">
                  {statisticsLoading ? '...' : `${Math.round(statistics?.answerRate || 0)}%`}
                </div>
                <div className="text-sm text-[#6b7280]">답변률</div>
                <div className="text-xs text-[#6b7280] mt-1">전체 데이터 기준</div>
              </div>
            </div>
          </div>
        </div>

        {/* 문의 등록 폼 */}
        <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 mb-8">
          <h2 className="text-lg font-semibold text-[#374151] mb-4 flex items-center gap-2">
            <Plus className="w-5 h-5" />
            새 문의 등록
          </h2>
          
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* 제목 입력 */}
            <div>
              <label className="block text-sm font-medium text-[#374151] mb-2">제목</label>
              <input
                type="text"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="문의 제목을 입력하세요"
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#d1d5db] bg-white text-[#374151]"
                required
              />
            </div>

            {/* 내용 입력 */}
            <div>
              <label className="block text-sm font-medium text-[#374151] mb-2">문의 내용</label>
              <textarea
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
                placeholder="문의 내용을 상세히 입력하세요"
                rows={6}
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#d1d5db] bg-white text-[#374151] resize-none"
                required
              />
            </div>

            {/* 등록 버튼 */}
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={submitting}
                className="flex items-center gap-2 bg-[#d1d5db] text-[#374151] px-6 py-2 rounded-lg hover:bg-[#e5e7eb] disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <Send className="w-4 h-4" />
                {submitting ? '등록 중...' : '문의 등록'}
              </button>
            </div>
          </form>
        </div>

        {/* 검색 */}
        <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl p-6 mb-6">
          <div className="flex gap-2">
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="제목 또는 내용으로 검색..."
              className="flex-1 px-3 py-2 border border-[#d1d5db] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#d1d5db] bg-white text-[#374151]"
            />
            <button
              onClick={handleSearch}
              className="bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors flex items-center gap-2"
            >
              <Search className="w-4 h-4" />
              검색
            </button>
          </div>
        </div>

        {/* 문의 목록 */}
        <div className="bg-[#f3f4f6] border border-[#d1d5db] rounded-xl shadow-xl overflow-hidden">
          {loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#6b7280] mx-auto mb-4"></div>
              <p className="text-[#374151]">문의 목록을 불러오는 중...</p>
            </div>
          ) : error ? (
            <div className="p-8 text-center">
              <p className="text-red-600 mb-4">{error}</p>
              <button
                onClick={() => fetchInquiries(currentPage)}
                className="bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : inquiryList.length === 0 ? (
            <div className="p-8 text-center">
              <MessageCircle className="w-12 h-12 text-[#6b7280] mx-auto mb-4" />
              <p className="text-[#374151] text-lg">등록된 문의가 없습니다.</p>
              <p className="text-[#6b7280] text-sm mt-2">위 폼을 통해 첫 번째 문의를 등록해보세요.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-[#d1d5db]">
                <thead className="bg-[#f3f4f6]">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-[#374151] uppercase tracking-wider">제목</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-[#374151] uppercase tracking-wider">상태</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-[#374151] uppercase tracking-wider">등록일</th>
                    <th className="px-6 py-3 text-center text-xs font-medium text-[#374151] uppercase tracking-wider">액션</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-[#d1d5db]">
                  {inquiryList.map((inquiry) => (
                    <tr key={inquiry.id} className="hover:bg-[#f3f4f6] transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-[#374151]">{inquiry.title}</div>
                        <div className="text-sm text-[#6b7280] truncate max-w-xs">{inquiry.content}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(inquiry)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-[#374151]">
                        {new Date(inquiry.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-center">
                        <button
                          onClick={() => router.push(`/seller/admin-qna/${inquiry.id}`)}
                          className="text-[#6b7280] hover:text-[#374151] text-sm font-medium transition-colors"
                        >
                          상세보기
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="flex justify-center mt-6">
            <div className="flex gap-2">
              {Array.from({ length: Math.min(totalPages, 10) }, (_, i) => (
                <button
                  key={i}
                  onClick={() => fetchInquiries(i)}
                  className={`px-3 py-2 rounded ${
                    currentPage === i
                      ? 'bg-[#6b7280] text-white'
                      : 'bg-white text-[#374151] border border-[#d1d5db] hover:bg-[#f3f4f6]'
                  } transition-colors`}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </SellerLayout>
  );
} 