# 보안 가이드

암호화폐 거래소 플랫폼의 보안 설정 및 베스트 프랙티스입니다.

## 목차
- [즉시 변경해야 할 설정](#즉시-변경해야-할-설정)
- [보안 체크리스트](#보안-체크리스트)
- [인증 및 권한](#인증-및-권한)
- [데이터 보안](#데이터-보안)
- [네트워크 보안](#네트워크-보안)
- [일반적인 취약점 방어](#일반적인-취약점-방어)
- [보안 모니터링](#보안-모니터링)
- [사고 대응](#사고-대응)

---

## 즉시 변경해야 할 설정

### ⚠️ 필수 변경 사항

프로덕션 배포 전 반드시 변경해야 합니다:

#### 1. JWT 시크릿 키

**현재 값 (개발용):**
```properties
jwt.secret=your-secret-key-change-this-in-production-min-256-bits-long
```

**변경 방법:**
```bash
# 안전한 랜덤 키 생성 (64자 이상)
openssl rand -base64 64

# 결과를 application.properties에 복사
jwt.secret=YOUR_GENERATED_SECRET_HERE
```

**파일 위치:**
- `backend/src/main/resources/application.properties`
- 또는 환경 변수 `JWT_SECRET` 사용 (권장)

#### 2. 데이터베이스 비밀번호

**현재 값 (개발용):**
```properties
spring.datasource.password=postgres
```

**변경 방법:**
```bash
# 강력한 비밀번호 생성
openssl rand -base64 32
```

**변경 위치:**
- `application.properties`
- `docker-compose.yml` 환경 변수
- PostgreSQL 컨테이너 재시작

#### 3. CORS 설정

**현재 값 (개발용):**
```properties
cors.allowed.origins=http://localhost:3000
```

**프로덕션 변경:**
```properties
cors.allowed.origins=https://your-domain.com,https://www.your-domain.com
```

**파일 위치:**
- `backend/src/main/resources/application.properties`

#### 4. 관리자 계정

**최초 관리자 생성:**

데이터베이스에 직접 생성하거나 애플리케이션 초기화 시 생성:

```sql
-- 비밀번호는 BCrypt로 해시화
INSERT INTO users (username, email, password, role, enabled)
VALUES ('admin', 'admin@your-domain.com', '$2a$10$...', 'ADMIN', true);
```

또는 회원가입 후 DB에서 role 변경:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your-username';
```

---

## 보안 체크리스트

### 배포 전 필수 확인

#### 📋 인증 및 권한
- [ ] JWT 시크릿 키 변경 (256비트 이상)
- [ ] JWT 만료 시간 설정 확인 (24시간 권장)
- [ ] 비밀번호 정책 강화 (최소 8자, 특수문자 포함)
- [ ] 관리자 계정 별도 생성
- [ ] 기본 계정 삭제 또는 비활성화

#### 📋 네트워크 보안
- [ ] HTTPS 활성화 (Let's Encrypt)
- [ ] HTTP → HTTPS 리다이렉트 설정
- [ ] HSTS 헤더 설정
- [ ] CORS 정확한 도메인으로 제한
- [ ] 방화벽 설정 (UFW 또는 iptables)
- [ ] 불필요한 포트 차단

#### 📋 데이터베이스 보안
- [ ] DB 비밀번호 변경
- [ ] DB 포트 외부 노출 차단 (5432)
- [ ] DB 접근 IP 화이트리스트 설정
- [ ] 자동 백업 설정
- [ ] 백업 암호화

#### 📋 애플리케이션 보안
- [ ] 에러 메시지 상세정보 숨김
- [ ] SQL 쿼리 로그 비활성화 (프로덕션)
- [ ] 디버그 모드 비활성화
- [ ] 입력값 검증 강화
- [ ] 파일 업로드 제한 설정

#### 📋 Docker 보안
- [ ] Docker 이미지 최신 버전 사용
- [ ] 컨테이너 권한 최소화
- [ ] 볼륨 마운트 권한 제한
- [ ] Docker secrets 사용
- [ ] 이미지 취약점 스캔

#### 📋 환경 변수
- [ ] .env 파일 Git 제외
- [ ] .env 파일 권한 600 설정
- [ ] 민감 정보 환경 변수로 관리
- [ ] 프로덕션/개발 환경 분리

#### 📋 모니터링
- [ ] 로그 수집 활성화
- [ ] 실패한 로그인 시도 모니터링
- [ ] 비정상 트래픽 감지
- [ ] 디스크 사용량 알림
- [ ] 에러 알림 설정

---

## 인증 및 권한

### JWT 보안

#### 권장 설정

```properties
# JWT 만료 시간 (24시간 = 86400000ms)
jwt.expiration=86400000

# 토큰 갱신 전략
# - Access Token: 24시간
# - Refresh Token: 30일 (구현 필요)
```

#### 토큰 저장

**❌ 나쁜 예:**
```javascript
// LocalStorage (XSS 공격에 취약)
localStorage.setItem('token', token);
```

**✅ 좋은 예:**
```javascript
// HttpOnly 쿠키 (XSS 방어)
// 서버에서 Set-Cookie 헤더 사용
```

### 비밀번호 정책

#### Spring Security 설정 강화

`SecurityConfig.java`에 비밀번호 정책 추가:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // 강도 12로 증가
}
```

#### 클라이언트 검증

```javascript
// 프론트엔드 검증
const validatePassword = (password) => {
  const minLength = 8;
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumbers = /\d/.test(password);
  const hasSpecialChar = /[!@#$%^&*]/.test(password);

  return password.length >= minLength &&
         hasUpperCase &&
         hasLowerCase &&
         hasNumbers &&
         hasSpecialChar;
};
```

### 2단계 인증 (2FA)

User 모델에 이미 2FA 필드가 있습니다:
```java
private Boolean twoFaEnabled = false;
private String twoFaSecret;
```

**구현 권장사항:**
- Google Authenticator 통합
- 백업 코드 제공
- SMS 인증 옵션

---

## 데이터 보안

### 민감 데이터 암호화

#### 데이터베이스 레벨

**암호화가 필요한 필드:**
- 사용자 개인정보
- 지갑 주소
- 거래 내역

**JPA Converter 사용:**

```java
@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SECRET_KEY = System.getenv("DB_ENCRYPTION_KEY");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // 암호화 로직
        return encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // 복호화 로직
        return decrypt(dbData);
    }
}

// 사용 예시
@Convert(converter = CryptoConverter.class)
private String walletAddress;
```

### SQL Injection 방어

**✅ 안전한 코드 (JPA 사용):**
```java
// Parameterized query
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);
```

**❌ 위험한 코드 (사용 금지):**
```java
// String concatenation
String query = "SELECT * FROM users WHERE username = '" + username + "'";
```

### XSS 방어

**Spring Security 설정:**
```java
http.headers()
    .contentSecurityPolicy("script-src 'self'")
    .and()
    .xssProtection()
    .and()
    .contentTypeOptions();
```

**프론트엔드:**
```javascript
// React는 기본적으로 XSS 방어
// dangerouslySetInnerHTML 사용 금지

// ❌ 위험
<div dangerouslySetInnerHTML={{__html: userInput}} />

// ✅ 안전
<div>{userInput}</div>
```

---

## 네트워크 보안

### HTTPS 강제

**Nginx 설정:**
```nginx
# HTTP 요청 리다이렉트
server {
    listen 80;
    return 301 https://$server_name$request_uri;
}

# HSTS 헤더
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
```

### 보안 헤더

**필수 보안 헤더:**

```nginx
# XSS 보호
add_header X-XSS-Protection "1; mode=block" always;

# Clickjacking 방지
add_header X-Frame-Options "SAMEORIGIN" always;

# MIME 타입 스니핑 방지
add_header X-Content-Type-Options "nosniff" always;

# Referrer 정책
add_header Referrer-Policy "strict-origin-when-cross-origin" always;

# CSP (Content Security Policy)
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';" always;
```

### Rate Limiting

**Nginx Rate Limiting:**

```nginx
# /etc/nginx/nginx.conf

http {
    # IP당 초당 10 요청
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

    server {
        location /api {
            limit_req zone=api_limit burst=20 nodelay;
            proxy_pass http://backend:8080;
        }
    }
}
```

**Spring Boot Rate Limiting (Bucket4j):**

`build.gradle`에 추가:
```gradle
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0'
```

---

## 일반적인 취약점 방어

### OWASP Top 10 대응

#### 1. Broken Access Control
- ✅ Spring Security 역할 기반 접근 제어 구현
- ✅ `@PreAuthorize("hasRole('ADMIN')")` 사용

#### 2. Cryptographic Failures
- ✅ BCrypt로 비밀번호 해싱
- ✅ HTTPS 사용
- ⚠️ DB 민감 데이터 암호화 필요

#### 3. Injection
- ✅ JPA Parameterized Query 사용
- ✅ 입력값 검증 (`@Valid`, `@NotBlank`)

#### 4. Insecure Design
- ✅ JWT 인증 구현
- ✅ 거래 로직 트랜잭션 처리

#### 5. Security Misconfiguration
- ⚠️ 기본 비밀번호 변경 필수
- ⚠️ 에러 메시지 상세정보 숨김 필요

#### 6. Vulnerable Components
- ✅ 최신 Spring Boot 사용
- ⚠️ 정기적인 의존성 업데이트 필요

#### 7. Authentication Failures
- ✅ 강력한 비밀번호 정책
- ⚠️ 로그인 시도 제한 필요
- ⚠️ 2FA 구현 권장

#### 8. Software & Data Integrity
- ✅ 코드 버전 관리 (Git)
- ⚠️ CI/CD 파이프라인 보안 필요

#### 9. Logging & Monitoring Failures
- ⚠️ 로그 수집 설정 필요
- ⚠️ 알림 시스템 구축 필요

#### 10. SSRF (Server-Side Request Forgery)
- ✅ 외부 API 호출 없음
- ⚠️ 추가 시 URL 검증 필요

---

## 보안 모니터링

### 로그 분석

**모니터링 대상:**

1. **실패한 로그인 시도**
```java
@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 5;
    private LoadingCache<String, Integer> attemptsCache;

    public void loginFailed(String key) {
        int attempts = attemptsCache.get(key);
        attemptsCache.put(key, attempts + 1);

        if (attempts >= MAX_ATTEMPT) {
            // 알림 전송
            alertService.sendAlert("Too many login attempts: " + key);
        }
    }
}
```

2. **비정상 거래 패턴**
- 짧은 시간 내 대량 주문
- 비정상적인 금액의 거래
- 의심스러운 IP에서의 접근

3. **시스템 리소스**
- CPU/메모리 사용률
- 디스크 공간
- 네트워크 트래픽

### 알림 설정

**Slack 통합 예시:**

```java
@Service
public class AlertService {
    private final String SLACK_WEBHOOK = System.getenv("SLACK_WEBHOOK_URL");

    public void sendAlert(String message) {
        // Slack Webhook으로 알림 전송
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> payload = Map.of("text", message);
        restTemplate.postForEntity(SLACK_WEBHOOK, payload, String.class);
    }
}
```

---

## 사고 대응

### 보안 사고 발생 시

1. **즉시 조치**
   - 영향받는 서비스 격리
   - 관련 계정 비활성화
   - 로그 수집 및 보존

2. **조사**
   - 침입 경로 파악
   - 영향 범위 확인
   - 피해 규모 평가

3. **복구**
   - 취약점 패치
   - 시스템 복구
   - 백업에서 복원

4. **사후 조치**
   - 보안 정책 업데이트
   - 모니터링 강화
   - 사용자 통보 (필요시)

### 비상 연락처

**보안 담당자:**
- 이름: [담당자명]
- 이메일: security@your-domain.com
- 전화: [비상 연락처]

**외부 전문가:**
- 보안 컨설턴트
- 법률 자문
- PR 담당

---

## 규정 준수

### 개인정보보호

**GDPR / 개인정보보호법 준수:**
- 사용자 데이터 최소 수집
- 명시적 동의 획득
- 데이터 삭제 요청 처리
- 데이터 이동권 보장

### 금융 규제

**암호화폐 거래소 규제:**
- KYC (Know Your Customer) 구현
- AML (Anti-Money Laundering) 정책
- 거래 기록 보관 (5년 이상)
- 당국 보고 의무

---

## 정기 보안 점검

### 월간 체크리스트

- [ ] 의존성 취약점 스캔 (`npm audit`, `./gradlew dependencyCheckAnalyze`)
- [ ] 로그 검토 (비정상 패턴)
- [ ] 백업 복구 테스트
- [ ] SSL 인증서 만료일 확인
- [ ] 사용자 권한 검토

### 분기별 체크리스트

- [ ] 침투 테스트 (Penetration Testing)
- [ ] 코드 보안 감사
- [ ] 보안 정책 업데이트
- [ ] 직원 보안 교육
- [ ] DR(재해 복구) 훈련

---

## 도구 및 리소스

### 보안 스캔 도구

**취약점 스캔:**
```bash
# NPM 의존성 감사
cd frontend && npm audit

# Gradle 의존성 체크
cd backend && ./gradlew dependencyCheckAnalyze

# Docker 이미지 스캔
docker scan crypto-exchange-backend
```

### 보안 테스트

**OWASP ZAP:**
```bash
# 자동 보안 스캔
docker run -t owasp/zap2docker-stable zap-baseline.py \
  -t https://your-domain.com
```

### 유용한 링크

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [CWE Top 25](https://cwe.mitre.org/top25/)

---

## 문의

보안 취약점 발견 시:
- **이메일:** security@your-domain.com
- **PGP 키:** [공개키 링크]
- **책임 있는 공개 정책:** 90일

**버그 바운티 프로그램:** [운영 여부]
