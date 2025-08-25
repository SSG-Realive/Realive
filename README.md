# 🪑 Realive
**1인 가구를 위한 B2C 중고 가구 거래 플랫폼**

전문 중고 가구 업체와 사용자를 연결하는 **B2C 구조의 중고 가구 거래 시스템**입니다.  
정보 비표준화, 거래 불신, 배송 문제 등 기존 C2C 구조의 한계를 해결하며,  
**정형화된 상품 정보, 업체 직접 배송, 신뢰 기반 리뷰 시스템, 경매 기능** 등을 통해  
**안전하고 편리한 중고 가구 거래 경험**을 제공합니다.

---

## 📦 구성

| 폴더        | 설명                       | 주요 기술 |
|-------------|----------------------------|-----------|
| `RealFE/`   | 사용자/판매자/관리자 UI    | Next.js, Tailwind, Zustand, Toss Payments |
| `RealBE/`   | REST API 서버               | Spring Boot, JPA, JWT, QueryDSL, OAuth2  |

---

## 🚀 주요 기능

### 👤 사용자
- 회원가입 / 로그인 (소셜 로그인 포함)
- 상품 목록 및 조건별 필터링 (크기, 가격, 브랜드 등)
- 상세 정보 확인 (사진, 영상, 사양, 배송 조건)
- 장바구니, 찜, 구매 이력, 마이페이지
- 실시간 경매 입찰
- 결제 연동 (Toss Payments)
- 신호등 기반 리뷰 작성

### 🏬 판매자
- 상품 등록/수정/삭제
- 이미지 및 영상 업로드 (썸네일 자동 생성)
- 주문 처리, 배송 상태 관리
- Q&A 응답, 리뷰 열람
- 정산 내역 확인

### 🛠 관리자
- 사용자 및 판매자 관리
- 상품 승인/거부, 경매 등록 및 낙찰 처리
- 통합 대시보드, 통계 확인
- 리뷰/Q&A 신고 처리

---

## 💻 기술 스택

### 🖥️ Frontend

|||
|--|----|
| **Framework** | ![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white) ![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black) ![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white) |
| **UI & 스타일링** | ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white) ![Shadcn UI](https://img.shields.io/badge/Shadcn_UI-EFF1F5?style=for-the-badge&logo=ui&logoColor=black) ![Radix UI](https://img.shields.io/badge/Radix_UI-161618?style=for-the-badge&logo=radixui&logoColor=white) |
| **상태 관리** | ![Zustand](https://img.shields.io/badge/Zustand-000000?style=for-the-badge&logo=Zustand&logoColor=white) ![React Query](https://img.shields.io/badge/React_Query-FF4154?style=for-the-badge&logo=reactquery&logoColor=white) |
| **기타** | ![Toss Payments](https://img.shields.io/badge/Toss_Payments-0064FF?style=for-the-badge&logo=toss&logoColor=white) ![ApexCharts](https://img.shields.io/badge/ApexCharts-FF4560?style=for-the-badge&logo=apexcharts&logoColor=white) |

---

### ⚙ Backend

|  ||
|----|---------|
| **Framework & 보안** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) |
| **ORM & 쿼리** | ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white) ![QueryDSL](https://img.shields.io/badge/QueryDSL-000000?style=for-the-badge&logo=data&logoColor=white) |
| **인증** | ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white) ![OAuth2](https://img.shields.io/badge/OAuth2-000000?style=for-the-badge&logo=oauth&logoColor=white) |
| **문서화** | ![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black) |
| **개발 도구** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) ![Lombok](https://img.shields.io/badge/Lombok-000000?style=for-the-badge&logo=lombok&logoColor=white) |

---

### 🗃 Database

||
|----------|
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white) |

---

## 🖥️ 프론트엔드 (RealFE)

### 📁 폴더 구조 요약

```
src/
├── app/            # App Router (고객/판매자/관리자 구분)
├── components/     # 재사용 컴포넌트 (UI, 페이지별)
├── hooks/          # 커스텀 훅
├── lib/            # 유틸리티
├── service/        # API 서비스 레이어
├── store/          # Zustand 전역 상태
└── types/          # TypeScript 타입 정의
```

### 🧪 실행 & 배포

```bash
# 1. Node.js 설치 확인
node --version

# 2. 의존성 설치
npm install

# 3. 환경 변수 설정 (.env.local)
NEXT_PUBLIC_API_URL=http://localhost:8080

# 4. 개발 서버 실행
npm run dev

# 5. Vercel 배포
vercel
```

---

## ⚙ 백엔드 (RealBE)

### 📁 패키지 구조

```
src/main/java/com/realive/
├── config/          # 설정
├── controller/      # REST API
├── domain/          # 엔티티
├── dto/             # DTO 객체
├── service/         # 비즈니스 인터페이스
├── serviceimpl/     # 비즈니스 구현체
├── repository/      # DB 접근
├── security/        # 보안 설정
└── util/            # 공통 유틸
```

### 🧪 실행 & 배포

```bash
# 1. Java 17 이상 필요
java -version

# 2. PostgreSQL 설정
CREATE DATABASE realive;

# 3. 환경 변수 설정 (application.yml)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/realive
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key

# 4. 실행
./gradlew bootRun

# 5. API 문서
http://localhost:8080/swagger-ui/index.html
```

---

## 🔗 배포 주소

- 서비스 URL: [Realive_B2C 중고 가구 거래 플랫폼](https://www.realive-ssg.click)
- Swagger API: `http://localhost:8080/swagger-ui/index.html` *(개발 환경 전용)*

---

## 📝 문의

프로젝트 관련 문의사항은 GitHub 이슈로 남겨주세요.

---

© 2025 Realive Team
