# Crypto Exchange Platform

풀 스택 암호화폐 거래소 플랫폼 템플릿입니다. Spring Boot, PostgreSQL, React로 구축되었습니다.

## 📚 문서

- **[API 문서](docs/API.md)** - 전체 REST API 엔드포인트 및 사용 예시
- **[배포 가이드](docs/DEPLOYMENT.md)** - HTTPS 설정 및 프로덕션 배포 방법
- **[보안 가이드](docs/SECURITY.md)** - 보안 설정 및 체크리스트

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
- Gradle 8.5

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
./gradlew bootRun
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

**Base URL:** `http://localhost:8080` (개발) / `https://your-domain.com` (프로덕션)

전체 API 문서는 **[API.md](docs/API.md)**를 참조하세요.

### 주요 엔드포인트

**인증 (인증 불필요)**
- `POST /api/auth/login` - 로그인
- `POST /api/auth/register` - 회원가입

**지갑 (인증 필요)**
- `GET /api/wallets` - 지갑 목록
- `POST /api/wallets/deposit` - 입금
- `POST /api/wallets/withdraw` - 출금

**거래 (인증 필요)**
- `GET /api/trading/pairs` - 거래 쌍 목록
- `POST /api/trading/orders` - 주문 생성
- `DELETE /api/trading/orders/{id}` - 주문 취소

**관리자 (ADMIN 역할 필요)**
- `GET /api/admin/stats` - 시스템 통계
- `PUT /api/admin/users/{id}/enable` - 사용자 활성화

> 📖 **상세 문서:** [API.md](docs/API.md)에서 요청/응답 예시 및 전체 엔드포인트 확인

## 데이터베이스 스키마

### 주요 테이블
- **users**: 사용자 정보
- **wallets**: 지갑 정보
- **trading_pairs**: 거래 쌍 정보
- **orders**: 주문 정보
- **transactions**: 거래 내역

## 보안 설정

⚠️ **프로덕션 배포 전 필수 확인사항**

### 즉시 변경 필요
1. **JWT 시크릿 키** - 256비트 이상 랜덤 키로 변경
2. **DB 비밀번호** - 강력한 비밀번호로 변경
3. **CORS 설정** - 프로덕션 도메인으로 제한

```bash
# 안전한 JWT 시크릿 생성
openssl rand -base64 64
```

> 🔒 **전체 보안 가이드:** [SECURITY.md](docs/SECURITY.md) 참조
> 🚀 **프로덕션 배포:** [DEPLOYMENT.md](docs/DEPLOYMENT.md) 참조

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

상세한 배포 가이드는 **[DEPLOYMENT.md](docs/DEPLOYMENT.md)**를 참조하세요.

**빠른 시작:**
```bash
# 1. 환경 변수 설정
cp .env.example .env.prod
nano .env.prod  # JWT 시크릿, DB 비밀번호 변경

# 2. HTTPS 인증서 발급 (Let's Encrypt)
sudo certbot certonly --standalone -d your-domain.com

# 3. Docker Compose로 실행
docker-compose -f docker-compose.prod.yml up -d
```

**필수 체크리스트:**
- [ ] JWT 시크릿 키 변경
- [ ] DB 비밀번호 변경
- [ ] HTTPS 설정 완료
- [ ] 방화벽 설정 (포트 80, 443)
- [ ] 자동 백업 설정
- [ ] Nginx 리버스 프록시 설정

> 📘 **상세 가이드:** [DEPLOYMENT.md](docs/DEPLOYMENT.md)

## 라이선스

MIT License

## 기여

Pull Request를 환영합니다!

## 문의

문제가 발생하면 GitHub Issues를 통해 문의해주세요.
