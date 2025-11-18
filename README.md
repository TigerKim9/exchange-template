# Crypto Exchange Platform

풀 스택 암호화폐 거래소 플랫폼 템플릿입니다. Spring Boot, PostgreSQL, React로 구축되었습니다.

## 주요 기능

### 백엔드 (Spring Boot)
- **인증 시스템**: JWT 기반 인증 및 권한 관리
- **지갑 관리**: 다중 통화 지갑, 입금/출금 기능
- **거래 엔진**: 실시간 주문 매칭 시스템
  - Market Orders (시장가 주문)
  - Limit Orders (지정가 주문)
  - 자동 주문 매칭
- **실시간 업데이트**: WebSocket을 통한 실시간 데이터
- **관리자 패널**: 사용자 관리 및 시스템 모니터링
- **거래 내역**: 모든 거래 기록 추적

### 프론트엔드 (React)
- **사용자 인증**: 로그인/회원가입 페이지
- **대시보드**: 계정 개요 및 시장 현황
- **거래 인터페이스**:
  - 실시간 주문장
  - 주문 생성 및 관리
  - 거래 쌍 선택
- **지갑 관리**: 입금/출금 인터페이스
- **거래 내역**: 전체 거래 기록 조회
- **관리자 대시보드**: 시스템 통계 및 사용자 관리

## 기술 스택

### 백엔드
- Java 17
- Spring Boot 3.2.0
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- WebSocket (STOMP)
- Maven

### 프론트엔드
- React 18
- Redux Toolkit
- React Router
- Axios
- Tailwind CSS
- Vite
- SockJS & STOMP

## 시작하기

### 필수 요구사항
- Docker & Docker Compose
- Java 17 (로컬 개발용)
- Node.js 20+ (로컬 개발용)
- PostgreSQL 16 (로컬 개발용)

### Docker를 사용한 빠른 시작

1. 저장소 클론
```bash
git clone <repository-url>
cd exchange-template
```

2. Docker Compose로 전체 스택 실행
```bash
docker-compose up -d
```

3. 애플리케이션 접속
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

### 로컬 개발 환경 설정

#### 백엔드 설정

1. PostgreSQL 데이터베이스 생성
```sql
CREATE DATABASE crypto_exchange;
```

2. 애플리케이션 설정 파일 업데이트 (필요시)
```bash
cd backend/src/main/resources
# application.properties 파일에서 데이터베이스 연결 정보 확인
```

3. 백엔드 실행
```bash
cd backend
mvn spring-boot:run
```

#### 프론트엔드 설정

1. 의존성 설치
```bash
cd frontend
npm install
```

2. 개발 서버 실행
```bash
npm run dev
```

3. 브라우저에서 접속
```
http://localhost:3000
```

## API 엔드포인트

### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/register` - 회원가입

### 지갑
- `GET /api/wallets` - 사용자 지갑 목록
- `GET /api/wallets/{currency}` - 특정 통화 지갑 조회
- `POST /api/wallets/create/{currency}` - 지갑 생성
- `POST /api/wallets/deposit` - 입금
- `POST /api/wallets/withdraw` - 출금

### 거래
- `GET /api/trading/pairs` - 거래 쌍 목록
- `POST /api/trading/orders` - 주문 생성
- `GET /api/trading/orders` - 사용자 주문 목록
- `DELETE /api/trading/orders/{id}` - 주문 취소

### 거래 내역
- `GET /api/transactions` - 거래 내역 조회
- `GET /api/transactions/paged` - 페이징된 거래 내역

### 관리자
- `GET /api/admin/users` - 전체 사용자 목록
- `GET /api/admin/stats` - 시스템 통계
- `PUT /api/admin/users/{id}/enable` - 사용자 활성화
- `PUT /api/admin/users/{id}/disable` - 사용자 비활성화

## 데이터베이스 스키마

### 주요 테이블
- **users**: 사용자 정보
- **wallets**: 지갑 정보
- **trading_pairs**: 거래 쌍 정보
- **orders**: 주문 정보
- **transactions**: 거래 내역

## 보안 설정

### JWT 시크릿 키 변경
프로덕션 환경에서는 반드시 JWT 시크릿 키를 변경하세요:

```properties
# backend/src/main/resources/application.properties
jwt.secret=your-secure-secret-key-here-min-256-bits
```

### CORS 설정
프로덕션 도메인으로 CORS 설정을 업데이트하세요:

```properties
# backend/src/main/resources/application.properties
cors.allowed.origins=https://your-production-domain.com
```

## 개발 로드맵

- [ ] 실시간 차트 통합 (TradingView)
- [ ] 2FA 인증
- [ ] KYC/AML 검증
- [ ] 외부 블록체인 지갑 통합
- [ ] 이메일 알림
- [ ] 모바일 앱
- [ ] API Rate Limiting
- [ ] 거래 봇 API

## 프로덕션 배포

1. 환경 변수 설정
2. JWT 시크릿 키 변경
3. HTTPS 설정
4. 데이터베이스 백업 설정
5. 로그 모니터링 설정
6. 리버스 프록시 설정 (Nginx)

## 라이선스

MIT License

## 기여

Pull Request를 환영합니다!

## 문의

문제가 발생하면 GitHub Issues를 통해 문의해주세요.
