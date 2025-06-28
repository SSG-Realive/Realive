import { useParams } from 'next/navigation';

export default function SellerOrderDetailPage() {
  const params = useParams();
  const orderId = params?.orderId;

  return (
    <div style={{ padding: '2rem' }}>
      <h1>주문 상세 페이지</h1>
      <p>주문번호: <b>{orderId}</b></p>
      {/* TODO: 주문 상세/배송 정보 등 추가 구현 */}
    </div>
  );
} 