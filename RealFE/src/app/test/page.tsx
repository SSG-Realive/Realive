'use client';

export default function TestButtonsVisible() {
    return (
        <div className="bg-yellow-100 py-10 px-4 min-h-screen">
            <h2 className="text-lg font-bold text-red-700 mb-4">
                ✅ 테스트용 버튼 (무조건 보여야 함)
            </h2>

            <div className="flex flex-wrap gap-3 bg-green-200 p-4">
                <button className="bg-black text-white px-4 py-2 rounded-full">소파</button>
                <button className="bg-black text-white px-4 py-2 rounded-full">거실장</button>
                <button className="bg-black text-white px-4 py-2 rounded-full">테이블</button>
                <button className="bg-black text-white px-4 py-2 rounded-full">책장</button>
                <button className="bg-black text-white px-4 py-2 rounded-full">기타</button>
            </div>
        </div>
    );
}
