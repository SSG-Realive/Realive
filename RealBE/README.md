# 🪑 Realive Backend

**1인 가구를 위한 B2C 중고 가구 거래 플랫폼 - 백엔드 API**

<br><br>

## 📌 프로젝트 개요

Realive 백엔드는 **Spring Boot 3.4.5** 기반의 RESTful API 서버로,  
사용자 인증, 상품 관리, 경매 시스템, 결제 처리, 리뷰 관리 등  
전체 플랫폼의 비즈니스 로직을 담당합니다.

<br><br>

## 🚀 주요 기능

### 🔐 인증 & 보안
- JWT 기반 인증 시스템
- Spring Security를 통한 권한 관리
- OAuth2 소셜 로그인 지원 (Google, Kakao)
- Role-based Access Control (USER, SELLER, ADMIN)

### 🛍 상품 관리
- 상품 CRUD (등록, 수정, 삭제, 조회)
- 이미지/영상 업로드 및 썸네일 자동 생성
- 카테고리별 상품 필터링
- 상품 검색 (QueryDSL 활용)

### 🏷 경매 시스템
- 실시간 경매 진행
- 입찰 및 낙찰 처리
- 경매 상태 관리 (진행중, 종료, 낙찰완료)
- Redis 기반 실시간 알림 (준비중)

### 💳 결제 시스템
- Toss Payments 연동
- 주문 생성 및 결제 처리
- 배송 상태 관리
- 정산 처리

### 📝 리뷰 & Q&A
- 신호등 기반 리뷰 시스템
- Q&A 관리
- 신고 처리
- 평점 계산

### 📊 관리자 기능
- 사용자/업체 관리
- 상품 승인/거부
- 통계 대시보드
- 경매 관리

<br><br>

## 🛠 기술 스택

### 🧱 Backend Framework
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### 🗃 Database & ORM
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-007396?style=for-the-badge&logo=hibernate&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-000000?style=for-the-badge&logo=data&logoColor=white)

### 🔐 인증 & 보안
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-000000?style=for-the-badge&logo=oauth&logoColor=white)

### 🛠 개발 도구
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-000000?style=for-the-badge&logo=lombok&logoColor=white)

### 📚 API 문서
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

<br><br>

## 📁 프로젝트 구조

```
src/main/java/com/realive/
├── config/          # 설정 클래스들
├── controller/      # REST API 컨트롤러
├── domain/         # 엔티티 클래스들
├── dto/            # 데이터 전송 객체
├── event/          # 이벤트 처리
├── exception/      # 예외 처리
├── repository/     # 데이터 접근 계층
├── security/       # 보안 관련 설정
├── service/        # 비즈니스 로직 인터페이스
├── serviceimpl/    # 비즈니스 로직 구현체
└── util/           # 유틸리티 클래스
```

<br><br>

## 🚀 실행 방법

### 1. 환경 설정
```bash
# Java 17 이상 필요
java -version

# Gradle 설치 (선택사항)
./gradlew --version
```

### 2. 데이터베이스 설정
```sql
-- PostgreSQL 데이터베이스 생성
CREATE DATABASE realive;
```

### 3. 환경 변수 설정
```bash
# application.yml 또는 환경 변수로 설정
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/realive
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key
```

### 4. 프로젝트 실행
```bash
# 개발 서버 실행
./gradlew bootRun

# 또는 IDE에서 RealiveApplication.java 실행
```

### 5. API 문서 확인
```
http://localhost:8080/swagger-ui.html
```

<br><br>

## 📋 API 엔드포인트

### 🔐 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/refresh` - 토큰 갱신

### 🛍 상품
- `GET /api/products` - 상품 목록 조회
- `GET /api/products/{id}` - 상품 상세 조회
- `POST /api/products` - 상품 등록 (판매자)
- `PUT /api/products/{id}` - 상품 수정 (판매자)
- `DELETE /api/products/{id}` - 상품 삭제 (판매자)

### 🏷 경매
- `GET /api/auctions` - 경매 목록 조회
- `POST /api/auctions/{id}/bid` - 입찰
- `GET /api/auctions/{id}/bids` - 입찰 내역 조회

### 💳 주문/결제
- `POST /api/orders` - 주문 생성
- `POST /api/orders/{id}/payment` - 결제 처리
- `GET /api/orders` - 주문 내역 조회

### 📝 리뷰
- `POST /api/reviews` - 리뷰 작성
- `GET /api/products/{id}/reviews` - 상품 리뷰 조회
- `PUT /api/reviews/{id}` - 리뷰 수정

<br><br>

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests TestClassName

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

<br><br>

## 📦 빌드 & 배포

```bash
# JAR 파일 빌드
./gradlew build

# Docker 이미지 빌드 (Dockerfile 필요)
docker build -t realive-backend .

# Docker 컨테이너 실행
docker run -p 8080:8080 realive-backend
```

<br><br>

## 🔧 개발 환경 설정

### IntelliJ IDEA 설정
1. **Lombok 플러그인 설치**
2. **Annotation Processing 활성화**
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Enable annotation processing 체크

### QueryDSL 설정
```bash
# QueryDSL Q클래스 생성
./gradlew compileJava
```

<br><br>

## 📝 주요 의존성

```gradle
// Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'

// QueryDSL
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'

// Swagger
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
```

<br><br>

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 생성해주세요.

---

**Realive Backend** 🚀 