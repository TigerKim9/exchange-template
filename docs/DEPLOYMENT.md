# 프로덕션 배포 가이드

암호화폐 거래소 플랫폼을 프로덕션 환경에 안전하게 배포하는 방법을 설명합니다.

## 목차
- [사전 준비](#사전-준비)
- [서버 설정](#서버-설정)
- [HTTPS 설정 (Let's Encrypt)](#https-설정-lets-encrypt)
- [Docker 프로덕션 설정](#docker-프로덕션-설정)
- [환경 변수 관리](#환경-변수-관리)
- [Nginx 리버스 프록시](#nginx-리버스-프록시)
- [데이터베이스 보안](#데이터베이스-보안)
- [모니터링 및 로깅](#모니터링-및-로깅)
- [백업 전략](#백업-전략)
- [보안 체크리스트](#보안-체크리스트)

---

## 사전 준비

### 필수 요구사항

1. **서버**
   - Ubuntu 20.04 LTS 이상 (권장)
   - 최소 2 CPU, 4GB RAM
   - 50GB 이상 디스크 공간

2. **도메인**
   - 등록된 도메인 필요
   - DNS A 레코드 설정 완료

3. **소프트웨어**
   - Docker 20.10+
   - Docker Compose 1.29+
   - Nginx (리버스 프록시용)

### 포트 설정

다음 포트가 열려있어야 합니다:
- `80`: HTTP (Let's Encrypt 인증용)
- `443`: HTTPS
- `22`: SSH (관리용)

---

## 서버 설정

### 1. 서버 업데이트

```bash
sudo apt update
sudo apt upgrade -y
```

### 2. Docker 설치

```bash
# Docker 설치
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Docker 서비스 시작
sudo systemctl enable docker
sudo systemctl start docker

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
```

### 3. 방화벽 설정

```bash
# UFW 설치 및 활성화
sudo apt install ufw
sudo ufw enable

# 필수 포트 열기
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS

# 상태 확인
sudo ufw status
```

---

## HTTPS 설정 (Let's Encrypt)

### 1. Certbot 설치

```bash
sudo apt install certbot python3-certbot-nginx -y
```

### 2. SSL 인증서 발급

**방법 1: Standalone (권장 - 초기 설정)**

```bash
# Nginx가 실행중이면 중지
sudo systemctl stop nginx

# 인증서 발급
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# 이메일 입력 및 약관 동의
```

**방법 2: Nginx 플러그인**

```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

### 3. 인증서 위치 확인

발급된 인증서는 다음 위치에 저장됩니다:

```
/etc/letsencrypt/live/your-domain.com/fullchain.pem
/etc/letsencrypt/live/your-domain.com/privkey.pem
```

### 4. 자동 갱신 설정

Let's Encrypt 인증서는 90일마다 갱신이 필요합니다.

```bash
# 자동 갱신 테스트
sudo certbot renew --dry-run

# Cron 작업 추가 (매일 확인)
sudo crontab -e

# 다음 줄 추가
0 3 * * * certbot renew --quiet --post-hook "systemctl reload nginx"
```

---

## Docker 프로덕션 설정

### 1. 프로덕션용 docker-compose.yml

`docker-compose.prod.yml` 생성:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: crypto-exchange-db
    restart: always
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backups:/backups
    networks:
      - exchange-network
    # 외부 노출 제거 (보안)
    # ports는 제거

  backend:
    build: ./backend
    container_name: crypto-exchange-backend
    restart: always
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    networks:
      - exchange-network
    # 외부 노출 제거 (Nginx가 프록시)
    expose:
      - "8080"

  frontend:
    build:
      context: ./frontend
      args:
        - VITE_API_URL=${API_URL}
    container_name: crypto-exchange-frontend
    restart: always
    depends_on:
      - backend
    networks:
      - exchange-network
    expose:
      - "80"

volumes:
  postgres_data:

networks:
  exchange-network:
    driver: bridge
```

### 2. 프로덕션용 환경 변수

`.env.prod` 파일 생성:

```bash
# Database
POSTGRES_DB=crypto_exchange_prod
POSTGRES_USER=crypto_user
POSTGRES_PASSWORD=STRONG_PASSWORD_HERE_CHANGE_THIS

# JWT
JWT_SECRET=YOUR_VERY_LONG_SECRET_KEY_MIN_256_BITS_CHANGE_THIS_IMMEDIATELY

# API URL
API_URL=https://api.your-domain.com

# Application
SPRING_PROFILES_ACTIVE=prod
```

**⚠️ 보안 주의:**
- 절대로 `.env.prod` 파일을 Git에 커밋하지 마세요
- 강력한 비밀번호 생성기 사용
- JWT 시크릿은 최소 256비트

**강력한 비밀번호 생성:**

```bash
# PostgreSQL 비밀번호 생성
openssl rand -base64 32

# JWT 시크릿 생성 (64자 이상)
openssl rand -base64 64
```

### 3. 프로덕션 실행

```bash
# 환경 변수 로드
export $(cat .env.prod | xargs)

# 빌드 및 실행
docker-compose -f docker-compose.prod.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f
```

---

## Nginx 리버스 프록시

### 1. Nginx 설치

```bash
sudo apt install nginx -y
```

### 2. Nginx 설정 파일 생성

`/etc/nginx/sites-available/crypto-exchange` 파일 생성:

```nginx
# HTTP -> HTTPS 리다이렉트
server {
    listen 80;
    listen [::]:80;
    server_name your-domain.com www.your-domain.com;

    # Let's Encrypt 인증을 위한 경로
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # 나머지 모든 요청은 HTTPS로 리다이렉트
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS 서버
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL 인증서
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    # SSL 보안 설정
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # HSTS (HTTP Strict Transport Security)
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # 보안 헤더
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;

    # 프론트엔드 (React)
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 백엔드 API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # CORS 헤더 (필요시)
        add_header Access-Control-Allow-Origin "https://your-domain.com" always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
    }

    # WebSocket
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 타임아웃 설정
        proxy_read_timeout 86400;
    }

    # 파일 업로드 크기 제한
    client_max_body_size 10M;

    # 로그
    access_log /var/log/nginx/crypto-exchange-access.log;
    error_log /var/log/nginx/crypto-exchange-error.log;
}
```

### 3. Nginx 설정 활성화

```bash
# 심볼릭 링크 생성
sudo ln -s /etc/nginx/sites-available/crypto-exchange /etc/nginx/sites-enabled/

# 기본 설정 제거
sudo rm /etc/nginx/sites-enabled/default

# 설정 테스트
sudo nginx -t

# Nginx 재시작
sudo systemctl restart nginx
```

### 4. 포트 매핑 업데이트

`docker-compose.prod.yml`에서 frontend 포트를 localhost에만 바인딩:

```yaml
frontend:
  # ...
  ports:
    - "127.0.0.1:3000:80"  # 로컬호스트에만 바인딩
```

backend도 동일하게:

```yaml
backend:
  # ...
  ports:
    - "127.0.0.1:8080:8080"  # 로컬호스트에만 바인딩
```

---

## 환경 변수 관리

### 1. Docker Secrets 사용 (권장)

Docker Swarm mode에서 사용 가능:

```bash
# Secret 생성
echo "your-strong-password" | docker secret create postgres_password -
echo "your-jwt-secret" | docker secret create jwt_secret -
```

`docker-compose.prod.yml` 수정:

```yaml
services:
  backend:
    secrets:
      - postgres_password
      - jwt_secret
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/postgres_password
      JWT_SECRET_FILE: /run/secrets/jwt_secret

secrets:
  postgres_password:
    external: true
  jwt_secret:
    external: true
```

### 2. .env 파일 보안

```bash
# 파일 권한 설정
chmod 600 .env.prod

# 소유자만 읽기/쓰기 가능
chown $USER:$USER .env.prod

# .gitignore에 추가 확인
echo ".env.prod" >> .gitignore
```

---

## 데이터베이스 보안

### 1. PostgreSQL 설정 강화

`application-prod.properties` 생성:

```properties
# Production Database Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA 설정
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# Security
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
server.error.include-exception=false

# Logging
logging.level.root=WARN
logging.level.com.exchange.crypto=INFO
logging.level.org.springframework.security=WARN
```

### 2. 데이터베이스 백업

자동 백업 스크립트 생성 (`/opt/crypto-exchange/backup.sh`):

```bash
#!/bin/bash

# 설정
BACKUP_DIR="/backups"
CONTAINER_NAME="crypto-exchange-db"
DB_NAME="crypto_exchange_prod"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/backup_$DATE.sql.gz"

# 백업 실행
docker exec $CONTAINER_NAME pg_dump -U crypto_user $DB_NAME | gzip > $BACKUP_FILE

# 30일 이전 백업 삭제
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "Backup completed: $BACKUP_FILE"
```

실행 권한 부여 및 Cron 등록:

```bash
chmod +x /opt/crypto-exchange/backup.sh

# Cron 작업 추가 (매일 새벽 2시)
crontab -e

# 다음 줄 추가
0 2 * * * /opt/crypto-exchange/backup.sh >> /var/log/db-backup.log 2>&1
```

### 3. 데이터베이스 복구

```bash
# 백업에서 복구
gunzip < backup_20241118_020000.sql.gz | docker exec -i crypto-exchange-db psql -U crypto_user crypto_exchange_prod
```

---

## 모니터링 및 로깅

### 1. Docker 로그 설정

`docker-compose.prod.yml`에 로깅 설정 추가:

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 2. 애플리케이션 로그

Logback 설정 (`src/main/resources/logback-spring.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/crypto-exchange/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/crypto-exchange/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 3. 모니터링 도구

**Portainer 설치 (Docker 모니터링):**

```bash
docker volume create portainer_data

docker run -d -p 9000:9000 --name portainer \
  --restart=always \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v portainer_data:/data \
  portainer/portainer-ce
```

접속: `https://your-domain.com:9000`

---

## 백업 전략

### 1. 전체 시스템 백업

```bash
#!/bin/bash
# /opt/crypto-exchange/full-backup.sh

BACKUP_DIR="/backups/full"
DATE=$(date +%Y%m%d)

# 데이터베이스 백업
docker exec crypto-exchange-db pg_dump -U crypto_user crypto_exchange_prod | gzip > "$BACKUP_DIR/db_$DATE.sql.gz"

# Docker 볼륨 백업
docker run --rm -v postgres_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/postgres_volume_$DATE.tar.gz -C /data .

# 설정 파일 백업
tar czf "$BACKUP_DIR/config_$DATE.tar.gz" /opt/crypto-exchange/.env.prod /etc/nginx/sites-available/crypto-exchange

echo "Full backup completed: $DATE"
```

### 2. 원격 백업 (AWS S3 예시)

```bash
# AWS CLI 설치
sudo apt install awscli

# S3로 백업 업로드
aws s3 sync /backups s3://your-bucket-name/crypto-exchange-backups/
```

---

## 보안 체크리스트

### 배포 전 필수 확인사항

- [ ] **환경 변수**
  - [ ] JWT 시크릿 키 변경 (256비트 이상)
  - [ ] DB 비밀번호 강력한 것으로 변경
  - [ ] .env 파일 권한 600으로 설정
  - [ ] .env 파일 Git에서 제외

- [ ] **HTTPS/SSL**
  - [ ] SSL 인증서 설치 완료
  - [ ] HTTP → HTTPS 리다이렉트 설정
  - [ ] HSTS 헤더 설정
  - [ ] TLS 1.2+ 사용

- [ ] **네트워크**
  - [ ] 방화벽 설정 (UFW)
  - [ ] 불필요한 포트 차단
  - [ ] DB 포트 외부 노출 제거
  - [ ] CORS 설정 확인

- [ ] **데이터베이스**
  - [ ] 자동 백업 설정
  - [ ] Connection pool 설정
  - [ ] SQL injection 방어 확인
  - [ ] 민감 데이터 암호화

- [ ] **애플리케이션**
  - [ ] 에러 상세 정보 숨김
  - [ ] SQL 로그 비활성화
  - [ ] Rate limiting 설정 (권장)
  - [ ] CSRF 보호 활성화

- [ ] **모니터링**
  - [ ] 로그 수집 설정
  - [ ] 알림 설정 (에러 발생 시)
  - [ ] 디스크 사용량 모니터링
  - [ ] CPU/메모리 모니터링

- [ ] **백업**
  - [ ] 자동 백업 스크립트 설정
  - [ ] 백업 복구 테스트
  - [ ] 원격 백업 설정 (권장)
  - [ ] 백업 주기 확인 (일일 권장)

---

## 성능 최적화

### 1. Docker 이미지 최적화

멀티 스테이지 빌드 사용 (이미 적용됨):
- 빌드 이미지 vs 런타임 이미지 분리
- 불필요한 파일 제외

### 2. Nginx 캐싱

```nginx
# 정적 파일 캐싱
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

### 3. 데이터베이스 인덱싱

주요 쿼리에 대한 인덱스 확인:
- User: username, email
- Order: user_id, trading_pair_id, status
- Transaction: user_id, type

---

## 트러블슈팅

### 일반적인 문제

**1. Let's Encrypt 인증서 발급 실패**
```bash
# 80 포트가 열려있는지 확인
sudo netstat -tuln | grep :80

# Nginx 중지 후 재시도
sudo systemctl stop nginx
sudo certbot certonly --standalone -d your-domain.com
```

**2. Docker 컨테이너 시작 실패**
```bash
# 로그 확인
docker-compose -f docker-compose.prod.yml logs

# 특정 서비스 로그
docker logs crypto-exchange-backend
```

**3. 데이터베이스 연결 실패**
```bash
# PostgreSQL 컨테이너 상태 확인
docker ps | grep postgres

# 연결 테스트
docker exec -it crypto-exchange-db psql -U crypto_user -d crypto_exchange_prod
```

---

## 업데이트 및 유지보수

### 애플리케이션 업데이트

```bash
# 1. 코드 업데이트
git pull origin main

# 2. 환경 변수 확인
source .env.prod

# 3. 재빌드 및 재시작
docker-compose -f docker-compose.prod.yml up -d --build

# 4. 헬스체크
curl https://your-domain.com/api/trading/pairs
```

### 롤백

```bash
# 이전 버전으로 롤백
git checkout <previous-commit>
docker-compose -f docker-compose.prod.yml up -d --build
```

---

## 참고 자료

- [Let's Encrypt 공식 문서](https://letsencrypt.org/docs/)
- [Nginx 보안 설정](https://nginx.org/en/docs/http/configuring_https_servers.html)
- [Docker 프로덕션 가이드](https://docs.docker.com/engine/security/)
- [Spring Boot 프로덕션 체크리스트](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

---

## 지원

문제 발생 시:
1. 로그 확인 (`docker-compose logs`)
2. GitHub Issues에 문제 보고
3. 백업에서 복구 고려

**긴급 연락처:** [담당자 이메일/연락처]
