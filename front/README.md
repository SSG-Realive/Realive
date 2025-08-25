# 🪑 Realive Frontend

**1인 가구를 위한 B2C 중고 가구 거래 플랫폼 - 프론트엔드**

<br><br>

## 📌 프로젝트 개요

Realive 프론트엔드는 **Next.js 15.3.3** 기반의 모던 웹 애플리케이션으로,  
사용자, 판매자, 관리자를 위한 직관적이고 반응형 UI를 제공합니다.  
**TypeScript**, **Tailwind CSS**, **Zustand**를 활용하여  
타입 안전성과 개발 효율성을 모두 확보했습니다.

<br><br>

## 🚀 주요 기능

### 👤 사용자 (Customer)
- **회원가입/로그인**: 소셜 로그인 (Google, Kakao) 지원
- **상품 탐색**: 카테고리별 필터링, 검색, 무한 스크롤
- **상품 상세**: 이미지 갤러리, 상품 정보, 리뷰 시스템
- **경매 시스템**: 실시간 입찰, 경매 현황 모니터링
- **장바구니 & 주문**: Toss Payments 연동 결제
- **마이페이지**: 주문 내역, 찜 목록, 리뷰 관리

### 🏬 판매자 (Seller)
- **대시보드**: 매출 통계, 주문 현황, 상품 관리
- **상품 관리**: 상품 등록/수정/삭제, 이미지 업로드
- **주문 관리**: 주문 확인, 배송 상태 업데이트
- **정산 관리**: 매출 정산 내역 및 처리
- **Q&A 관리**: 고객 문의 답변

### 🛠 관리자 (Admin)
- **통합 대시보드**: 플랫폼 현황 통계
- **사용자 관리**: 회원, 판매자 승인/관리
- **상품 관리**: 상품 승인, 카테고리 관리
- **경매 관리**: 경매 등록, 낙찰 처리
- **신고 처리**: 리뷰, Q&A 신고 관리
- **정산 관리**: 판매자 정산 처리

<br><br>

## 🛠 기술 스택

### ⚛️ Frontend Framework
![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)

### 🎨 Styling & UI
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![Shadcn UI](https://img.shields.io/badge/Shadcn_UI-EFF1F5?style=for-the-badge&logo=ui&logoColor=black)
![Radix UI](https://img.shields.io/badge/Radix_UI-161618?style=for-the-badge&logo=radixui&logoColor=white)

### 📊 State Management
![Zustand](https://img.shields.io/badge/Zustand-000000?style=for-the-badge&logo=Zustand&logoColor=white)
![React Query](https://img.shields.io/badge/React_Query-FF4154?style=for-the-badge&logo=reactquery&logoColor=white)

### 🔧 Development Tools
![ESLint](https://img.shields.io/badge/ESLint-4B32C3?style=for-the-badge&logo=eslint&logoColor=white)
![PostCSS](https://img.shields.io/badge/PostCSS-DD3A0A?style=for-the-badge&logo=postcss&logoColor=white)

### 💳 Payment & Charts
![Toss Payments](https://img.shields.io/badge/Toss_Payments-0064FF?style=for-the-badge&logo=toss&logoColor=white)
![ApexCharts](https://img.shields.io/badge/ApexCharts-FF4560?style=for-the-badge&logo=apexcharts&logoColor=white)

<br><br>

## 📁 프로젝트 구조

```
src/
├── app/                    # Next.js App Router
│   ├── (public)/          # 공개 페이지 (메인, 상품, 경매)
│   ├── admin/             # 관리자 페이지
│   ├── customer/          # 사용자 페이지
│   ├── seller/            # 판매자 페이지
│   └── api/               # API 라우트
├── components/            # 재사용 가능한 컴포넌트
│   ├── ui/               # 기본 UI 컴포넌트 (Shadcn)
│   ├── customer/         # 사용자 전용 컴포넌트
│   ├── seller/           # 판매자 전용 컴포넌트
│   └── layouts/          # 레이아웃 컴포넌트
├── hooks/                # 커스텀 훅
├── lib/                  # 유틸리티 및 설정
├── service/              # API 서비스 레이어
├── store/                # Zustand 상태 관리
└── types/                # TypeScript 타입 정의
```

<br><br>

## 🚀 실행 방법

### 1. 환경 설정
```bash
# Node.js 18 이상 필요
node --version

# npm 또는 yarn 설치
npm --version
```

### 2. 의존성 설치
```bash
# 프로젝트 루트에서
npm install
```

### 3. 환경 변수 설정
```bash
# .env.local 파일 생성
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_TOSS_CLIENT_KEY=your_toss_client_key
NEXT_PUBLIC_TOSS_SUCCESS_URL=http://localhost:3000/orders/success
NEXT_PUBLIC_TOSS_FAIL_URL=http://localhost:3000/orders/fail
```

### 4. 개발 서버 실행
```bash
# 개발 모드
npm run dev

# 프로덕션 빌드
npm run build
npm start
```

### 5. 브라우저에서 확인
```
http://localhost:3000
```

<br><br>

## 📱 페이지 구조

### 👤 사용자 페이지
- `/` - 메인 페이지 (배너, 추천 상품, 경매)
- `/main/products` - 상품 목록
- `/main/products/[id]` - 상품 상세
- `/main/auctions` - 경매 목록
- `/customer/login` - 로그인
- `/customer/signup` - 회원가입
- `/customer/mypage` - 마이페이지
- `/customer/cart` - 장바구니
- `/customer/orders` - 주문 내역

### 🏬 판매자 페이지
- `/seller/login` - 판매자 로그인
- `/seller/dashboard` - 판매자 대시보드
- `/seller/products` - 상품 관리
- `/seller/orders` - 주문 관리
- `/seller/settlements` - 정산 관리
- `/seller/qna` - Q&A 관리

### 🛠 관리자 페이지
- `/admin/login` - 관리자 로그인
- `/admin/dashboard` - 관리자 대시보드
- `/admin/member-management` - 회원 관리
- `/admin/seller-management` - 판매자 관리
- `/admin/auction-management` - 경매 관리
- `/admin/review-management` - 리뷰 관리

<br><br>

## 🎨 UI/UX 특징

### 🎯 디자인 시스템
- **Shadcn UI**: 일관된 디자인 컴포넌트
- **Tailwind CSS**: 유틸리티 퍼스트 CSS 프레임워크
- **반응형 디자인**: 모바일, 태블릿, 데스크톱 지원
- **다크모드**: 시스템 설정 기반 자동 전환

### 🚀 성능 최적화
- **Next.js App Router**: 서버 컴포넌트 활용
- **이미지 최적화**: Next.js Image 컴포넌트
- **코드 스플리팅**: 자동 번들 분할
- **캐싱**: React Query를 통한 데이터 캐싱

<br><br>

## 🔧 개발 가이드

### 컴포넌트 작성 규칙
```typescript
// components/ui/Button.tsx 예시
interface ButtonProps {
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link'
  size?: 'default' | 'sm' | 'lg' | 'icon'
  children: React.ReactNode
  onClick?: () => void
}

export const Button = ({ variant = 'default', size = 'default', children, ...props }: ButtonProps) => {
  return (
    <button className={cn(buttonVariants({ variant, size }))} {...props}>
      {children}
    </button>
  )
}
```

### API 호출 패턴
```typescript
// service/customer/productService.ts 예시
export const getProducts = async (params: ProductSearchParams) => {
  const response = await apiClient.get('/api/products', { params })
  return response.data
}
```

### 상태 관리 패턴
```typescript
// store/customer/authStore.ts 예시
interface AuthStore {
  user: User | null
  isAuthenticated: boolean
  login: (credentials: LoginCredentials) => Promise<void>
  logout: () => void
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  isAuthenticated: false,
  login: async (credentials) => {
    // 로그인 로직
  },
  logout: () => set({ user: null, isAuthenticated: false })
}))
```

<br><br>

## 🧪 테스트

```bash
# 린트 검사
npm run lint

# 타입 체크
npx tsc --noEmit

# 테스트 실행 (Jest 설정 필요)
npm test
```

<br><br>

## 📦 빌드 & 배포

### Vercel 배포 (권장)
```bash
# Vercel CLI 설치
npm i -g vercel

# 배포
vercel
```

### Docker 배포
```dockerfile
# Dockerfile 예시
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

<br><br>

## 🔧 개발 환경 설정

### VS Code 확장 프로그램
- **ES7+ React/Redux/React-Native snippets**
- **Tailwind CSS IntelliSense**
- **TypeScript Importer**
- **Prettier - Code formatter**

### Prettier 설정
```json
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5"
}
```

<br><br>

## 📝 주요 의존성

```json
{
  "dependencies": {
    "next": "15.3.3",
    "react": "^19.0.0",
    "typescript": "^5",
    "tailwindcss": "^4.1.10",
    "zustand": "^5.0.5",
    "@tanstack/react-query": "^5.80.7",
    "@tosspayments/payment-sdk": "^1.9.1",
    "shadcn-ui": "^0.9.5"
  }
}
```

<br><br>

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 생성해주세요.

---

**Realive Frontend** 🚀
