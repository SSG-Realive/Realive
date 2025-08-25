// components/customer/qna/QnaList.tsx

import { useState } from 'react';
// ✨ 새로 만든 타입 임포트 경로 및 타입명 변경
import { CustomerQnaResponse } from '@/types/customer/qna/customerQnaResponse';

interface QnaListProps {
    qnas: CustomerQnaResponse[]; // ⭐⭐ prop 타입 변경
    initialDisplayCount?: number;
}

export default function QnaList({ qnas, initialDisplayCount = 3 }: QnaListProps) {
    const [showAll, setShowAll] = useState(false);

    const displayedQnas = showAll ? qnas : qnas.slice(0, initialDisplayCount);

    return (
        <div className="space-y-4">
            {displayedQnas.length > 0 ? (
                displayedQnas.map((qna) => (
                    <div key={qna.id} className="p-4 bg-white rounded-md shadow-sm">
                        <div className="flex justify-between items-start mb-2">
                            <h3 className="font-medium text-base text-gray-900 break-words pr-4">{qna.title}</h3>
                            <span className="text-xs text-gray-500 flex-shrink-0">{new Date(qna.createdAt).toLocaleDateString()}</span>
                        </div>
                        {/* content와 answer 필드는 CustomerQnaResponse에 포함되어 있으므로 이 부분은 코드 변경 없음 */}
                        <p className="text-sm text-gray-700 mb-3 whitespace-pre-wrap">{qna.content}</p>

                        {qna.isAnswered ? (
                            <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-md">
                                <span className="font-medium text-sm text-blue-700">답변: </span>
                                <p className="text-sm text-gray-800 whitespace-pre-wrap">{qna.answer}</p>
                            </div>
                        ) : (
                            <p className="text-sm text-gray-500 italic mt-2">아직 답변이 등록되지 않았습니다.</p>
                        )}
                    </div>
                ))
            ) : (
                <p className="text-sm text-gray-600 p-4 border rounded-md shadow-sm bg-white">등록된 QnA가 없습니다.</p>
            )}

            {qnas.length > initialDisplayCount && (
                <div className="text-center mt-4">
                    <button
                        onClick={() => setShowAll(!showAll)}
                        className="px-6 py-2 border border-gray-300 rounded-md text-sm font-light hover:bg-gray-100 text-gray-700 transition duration-150 ease-in-out"
                    >
                        {showAll ? '간략히 보기' : '더 보기'}
                    </button>
                </div>
            )}
        </div>
    );
}