import apiClient from '@/lib/apiClient';

export interface TossPaymentResponse {
    orderId: string;
    paymentKey: string;
    status: string;
    totalAmount: number;
    orderName: string;
    approvedAt: string;
    message: string;
}

// 토스페이먼츠 기본 SDK 서비스
export interface TossPaymentConfig {
  clientKey: string;
  customerKey: string;
}

export interface PaymentRequestOptions {
  orderId: string;
  orderName: string;
  amount: number;
  successUrl: string;
  failUrl: string;
  customerEmail?: string;
  customerName?: string;
  customerMobilePhone?: string;
}

export interface PaymentConfirmRequest {
  paymentKey: string;
  orderId: string;
  amount: number;
}

// 토스페이먼츠 기본 SDK 로드
export const loadTossPayments = async (clientKey: string): Promise<any> => {
  if (typeof window === 'undefined') {
    throw new Error('Window object is not available');
  }

  // 이미 로드된 경우
  if ((window as any).TossPayments) {
    console.log('토스페이먼츠 SDK 이미 로드됨');
    return (window as any).TossPayments(clientKey);
  }

  return new Promise((resolve, reject) => {
    // 기존 스크립트 태그가 있는지 확인
    const existingScript = document.querySelector('script[src*="tosspayments.com"]');
    if (existingScript) {
      existingScript.remove();
    }

    const script = document.createElement('script');
    // 토스페이먼츠 기본 SDK URL
    script.src = 'https://js.tosspayments.com/v1/payment';
    script.async = true;
    
    const timeout = setTimeout(() => {
      reject(new Error('토스페이먼츠 SDK 로드 타임아웃'));
    }, 10000);
    
    script.onload = () => {
      clearTimeout(timeout);
      console.log('토스페이먼츠 SDK 로드 완료');
      
      setTimeout(() => {
        if ((window as any).TossPayments) {
          try {
            const tossPayments = (window as any).TossPayments(clientKey);
            console.log('토스페이먼츠 객체 생성 완료');
            resolve(tossPayments);
          } catch (error) {
            console.error('토스페이먼츠 객체 생성 실패:', error);
            reject(new Error('토스페이먼츠 객체 생성 실패'));
          }
        } else {
          reject(new Error('TossPayments 객체를 찾을 수 없습니다'));
        }
      }, 100);
    };
    
    script.onerror = (error) => {
      clearTimeout(timeout);
      console.error('토스페이먼츠 SDK 로드 실패:', error);
      reject(new Error('토스페이먼츠 SDK 로드에 실패했습니다'));
    };
    
    document.head.appendChild(script);
    console.log('토스페이먼츠 SDK 로드 시작...');
  });
};

// 결제 요청
export const requestPayment = async (
  tossPayments: any,
  options: PaymentRequestOptions
) => {
  try {
    console.log('결제 요청 시작:', options);
    console.log('tossPayments 객체:', tossPayments);
    
    // tossPayments 객체 유효성 확인
    if (!tossPayments) {
      throw new Error('토스페이먼츠 객체가 없습니다');
    }

    if (typeof tossPayments.requestPayment !== 'function') {
      console.error('tossPayments.requestPayment가 함수가 아닙니다:', typeof tossPayments.requestPayment);
      throw new Error('토스페이먼츠 requestPayment 함수를 찾을 수 없습니다');
    }
    
    console.log('토스페이먼츠 결제창 호출 시작...');

    const result = await tossPayments.requestPayment('카드', {
      amount: options.amount,
      orderId: options.orderId,
      orderName: options.orderName,
      successUrl: options.successUrl,
      failUrl: options.failUrl,
      customerEmail: options.customerEmail,
      customerName: options.customerName,
      customerMobilePhone: options.customerMobilePhone,
    });
    
    console.log('결제 요청 결과:', result);
    return result;
  } catch (error: any) {
    console.error('결제 요청 실패:', error);
    console.error('에러 상세:', {
      message: error?.message,
      stack: error?.stack,
      tossPayments: !!tossPayments,
      requestPaymentExists: tossPayments && typeof tossPayments.requestPayment === 'function'
    });
    throw error;
  }
};

// 결제 승인 API 호출
export const confirmPayment = async (request: PaymentConfirmRequest) => {
  const response = await fetch('/api/customer/payments/toss/confirm', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || '결제 승인에 실패했습니다');
  }

  return response.json();
};

// 테스트 카드 정보 (샌드박스용)
export const TEST_CARDS = {
  NORMAL: '4330123412341234',
  INSUFFICIENT_FUNDS: '4330123456781234',
  INVALID_CARD: '4000000000000002',
  EXPIRED_CARD: '4000000000000069'
} as const;

// 기본 설정
export const DEFAULT_CONFIG = {
  CLIENT_KEY: process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!,
  SUCCESS_URL: process.env.NEXT_PUBLIC_TOSS_SUCCESS_URL!,
  FAIL_URL: process.env.NEXT_PUBLIC_TOSS_FAIL_URL!,
} as const;

// 샌드박스 가이드
export const SANDBOX_GUIDE = `
🧪 토스페이먼츠 샌드박스 테스트

✅ 성공 테스트 카드:
카드번호: 4111-1111-1111-1111
만료일: 12/25
CVC: 123
비밀번호: 00

❌ 잔액 부족 테스트:
카드번호: 4000-0000-0000-0002

❌ 카드 거절 테스트:
카드번호: 4000-0000-0000-0069

⚠️ 샌드박스 환경에서는 실제 결제가 발생하지 않습니다.
`; 