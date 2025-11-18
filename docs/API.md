# API 문서

암호화폐 거래소 플랫폼 REST API 문서입니다.

## 목차
- [인증](#인증)
- [인증 API](#인증-api)
- [지갑 API](#지갑-api)
- [거래 API](#거래-api)
- [거래내역 API](#거래내역-api)
- [관리자 API](#관리자-api)
- [WebSocket](#websocket)
- [에러 코드](#에러-코드)

## 기본 정보

**Base URL**
```
개발: http://localhost:8080
프로덕션: https://your-domain.com
```

**Content-Type**
```
application/json
```

---

## 인증

대부분의 API는 JWT 토큰 인증이 필요합니다.

### 인증 헤더
```http
Authorization: Bearer {token}
```

### 토큰 획득
로그인 또는 회원가입 시 응답으로 JWT 토큰을 받습니다.

**토큰 유효기간:** 24시간 (86400000ms)

---

## 인증 API

### 회원가입

사용자 계정을 생성합니다.

**Endpoint:** `POST /api/auth/register`

**인증 필요:** 없음

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "010-1234-5678"
}
```

**필수 필드:**
- `username` (3-50자)
- `email` (유효한 이메일)
- `password` (6-100자)

**선택 필드:**
- `firstName`
- `lastName`
- `phoneNumber`

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

**에러 응답:**
```json
"Error: Username is already taken!"
```

---

### 로그인

기존 계정으로 로그인합니다.

**Endpoint:** `POST /api/auth/login`

**인증 필요:** 없음

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "role": "ROLE_USER"
}
```

**에러 응답:**
```json
"Error: Bad credentials"
```

---

## 지갑 API

### 지갑 목록 조회

현재 사용자의 모든 지갑을 조회합니다.

**Endpoint:** `GET /api/wallets`

**인증 필요:** ✅ Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "currency": "BTC",
    "balance": "1.50000000",
    "lockedBalance": "0.20000000",
    "availableBalance": "1.30000000",
    "walletAddress": "BTC_1234567890_5678"
  },
  {
    "id": 2,
    "currency": "USDT",
    "balance": "10000.00000000",
    "lockedBalance": "500.00000000",
    "availableBalance": "9500.00000000",
    "walletAddress": "USDT_1234567890_9012"
  }
]
```

---

### 특정 통화 지갑 조회

특정 통화의 지갑 정보를 조회합니다.

**Endpoint:** `GET /api/wallets/{currency}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `currency`: BTC, ETH, USDT, USD 등

**Response (200 OK):**
```json
{
  "id": 1,
  "currency": "BTC",
  "balance": "1.50000000",
  "lockedBalance": "0.20000000",
  "availableBalance": "1.30000000",
  "walletAddress": "BTC_1234567890_5678"
}
```

**Response (404 Not Found):**
지갑이 없는 경우

---

### 지갑 생성

새로운 통화의 지갑을 생성합니다.

**Endpoint:** `POST /api/wallets/create/{currency}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `currency`: BTC, ETH, USDT, USD 등

**Response (200 OK):**
```json
{
  "id": 3,
  "currency": "ETH",
  "balance": "0.00000000",
  "lockedBalance": "0.00000000",
  "availableBalance": "0.00000000",
  "walletAddress": "ETH_1234567890_3456"
}
```

---

### 입금

지갑에 자산을 입금합니다.

**Endpoint:** `POST /api/wallets/deposit`

**인증 필요:** ✅ Bearer Token

**Request Body:**
```json
{
  "currency": "BTC",
  "amount": 0.5,
  "transactionHash": "abc123def456..." // 선택
}
```

**Response (200 OK):**
```json
{
  "id": 100,
  "type": "DEPOSIT",
  "currency": "BTC",
  "amount": "0.50000000",
  "fee": null,
  "balanceBefore": "1.50000000",
  "balanceAfter": "2.00000000",
  "status": "COMPLETED",
  "transactionHash": "abc123def456...",
  "description": "Deposit 0.5 BTC",
  "createdAt": "2024-11-18T10:30:00",
  "completedAt": "2024-11-18T10:30:00"
}
```

---

### 출금

지갑에서 자산을 출금합니다.

**Endpoint:** `POST /api/wallets/withdraw`

**인증 필요:** ✅ Bearer Token

**Request Body:**
```json
{
  "currency": "BTC",
  "amount": 0.3,
  "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
}
```

**Response (200 OK):**
```json
{
  "id": 101,
  "type": "WITHDRAWAL",
  "currency": "BTC",
  "amount": "0.30000000",
  "fee": null,
  "balanceBefore": "2.00000000",
  "balanceAfter": "1.70000000",
  "status": "COMPLETED",
  "walletAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
  "description": "Withdrawal 0.3 BTC",
  "createdAt": "2024-11-18T10:35:00",
  "completedAt": "2024-11-18T10:35:00"
}
```

**에러 응답:**
```json
"Error: Insufficient balance"
```

---

## 거래 API

### 거래 쌍 목록 조회

활성화된 모든 거래 쌍을 조회합니다.

**Endpoint:** `GET /api/trading/pairs`

**인증 필요:** ✅ Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "baseCurrency": "BTC",
    "quoteCurrency": "USDT",
    "symbol": "BTC/USDT",
    "currentPrice": "45000.50000000",
    "high24h": "46000.00000000",
    "low24h": "44000.00000000",
    "volume24h": "150.50000000",
    "priceChange24h": "500.50000000",
    "priceChangePercent24h": "1.12",
    "minOrderAmount": "0.00100000",
    "maxOrderAmount": "100.00000000",
    "tradingFee": "0.0010",
    "active": true,
    "createdAt": "2024-11-01T00:00:00",
    "updatedAt": "2024-11-18T10:40:00"
  }
]
```

---

### 거래 쌍 상세 조회

특정 거래 쌍의 정보를 조회합니다.

**Endpoint:** `GET /api/trading/pairs/{id}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `id`: 거래 쌍 ID

**Response (200 OK):**
```json
{
  "id": 1,
  "baseCurrency": "BTC",
  "quoteCurrency": "USDT",
  "symbol": "BTC/USDT",
  "currentPrice": "45000.50000000",
  "high24h": "46000.00000000",
  "low24h": "44000.00000000",
  "volume24h": "150.50000000",
  "priceChange24h": "500.50000000",
  "priceChangePercent24h": "1.12",
  "tradingFee": "0.0010"
}
```

---

### 주문 생성

새로운 매수/매도 주문을 생성합니다.

**Endpoint:** `POST /api/trading/orders`

**인증 필요:** ✅ Bearer Token

**Request Body:**
```json
{
  "tradingPairId": 1,
  "type": "LIMIT",
  "side": "BUY",
  "price": 45000.00,
  "amount": 0.5,
  "stopPrice": null
}
```

**필드 설명:**
- `tradingPairId`: 거래 쌍 ID
- `type`: `LIMIT`, `MARKET`, `STOP_LOSS`, `STOP_LIMIT`
- `side`: `BUY`, `SELL`
- `price`: 주문 가격
- `amount`: 주문 수량
- `stopPrice`: 스탑 가격 (선택, STOP 주문 시 필수)

**Response (200 OK):**
```json
{
  "id": 1000,
  "tradingPair": {
    "id": 1,
    "symbol": "BTC/USDT"
  },
  "type": "LIMIT",
  "side": "BUY",
  "price": "45000.00000000",
  "amount": "0.50000000",
  "filledAmount": "0.00000000",
  "remainingAmount": "0.50000000",
  "total": "22500.00000000",
  "fee": "0.00000000",
  "status": "PENDING",
  "timeInForce": "GTC",
  "createdAt": "2024-11-18T10:50:00"
}
```

**에러 응답:**
```json
"Error: Insufficient balance"
```

---

### 내 주문 목록 조회

현재 사용자의 모든 주문을 조회합니다.

**Endpoint:** `GET /api/trading/orders`

**인증 필요:** ✅ Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": 1000,
    "tradingPairSymbol": "BTC/USDT",
    "type": "LIMIT",
    "side": "BUY",
    "price": "45000.00000000",
    "amount": "0.50000000",
    "filledAmount": "0.25000000",
    "remainingAmount": "0.25000000",
    "total": "22500.00000000",
    "fee": "11.25000000",
    "status": "PARTIALLY_FILLED",
    "createdAt": "2024-11-18T10:50:00"
  }
]
```

---

### 페이징된 주문 조회

페이징 처리된 주문 목록을 조회합니다.

**Endpoint:** `GET /api/trading/orders/paged`

**인증 필요:** ✅ Bearer Token

**Query Parameters:**
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기
- `sort`: 정렬 (예: `createdAt,desc`)

**예시:**
```
GET /api/trading/orders/paged?page=0&size=20&sort=createdAt,desc
```

---

### 활성 주문 조회

특정 거래 쌍의 활성 주문을 조회합니다 (주문장).

**Endpoint:** `GET /api/trading/orders/active/{tradingPairId}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `tradingPairId`: 거래 쌍 ID

**Response (200 OK):**
```json
[
  {
    "id": 1001,
    "tradingPairSymbol": "BTC/USDT",
    "type": "LIMIT",
    "side": "BUY",
    "price": "44500.00000000",
    "amount": "1.00000000",
    "filledAmount": "0.00000000",
    "remainingAmount": "1.00000000",
    "status": "PENDING"
  }
]
```

---

### 주문 취소

특정 주문을 취소합니다.

**Endpoint:** `DELETE /api/trading/orders/{orderId}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `orderId`: 주문 ID

**Response (200 OK):**
```json
"Order cancelled successfully"
```

**에러 응답:**
```json
"Error: Cannot cancel this order"
```

---

## 거래내역 API

### 거래 내역 조회

현재 사용자의 모든 거래 내역을 조회합니다.

**Endpoint:** `GET /api/transactions`

**인증 필요:** ✅ Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": 100,
    "type": "TRADE_BUY",
    "currency": "BTC",
    "amount": "0.50000000",
    "fee": "22.50000000",
    "balanceBefore": "1.50000000",
    "balanceAfter": "2.00000000",
    "status": "COMPLETED",
    "description": "Trade: BUY 0.5 BTC/USDT @ 45000.0",
    "createdAt": "2024-11-18T10:55:00",
    "completedAt": "2024-11-18T10:55:00"
  }
]
```

**거래 타입:**
- `DEPOSIT`: 입금
- `WITHDRAWAL`: 출금
- `TRADE_BUY`: 매수 거래
- `TRADE_SELL`: 매도 거래
- `FEE`: 수수료
- `REFUND`: 환불
- `BONUS`: 보너스

**거래 상태:**
- `PENDING`: 대기중
- `PROCESSING`: 처리중
- `COMPLETED`: 완료
- `FAILED`: 실패
- `CANCELLED`: 취소

---

### 페이징된 거래 내역 조회

**Endpoint:** `GET /api/transactions/paged`

**인증 필요:** ✅ Bearer Token

**Query Parameters:**
- `page`: 페이지 번호
- `size`: 페이지 크기

**예시:**
```
GET /api/transactions/paged?page=0&size=50
```

---

### 통화별 거래 내역 조회

특정 통화의 거래 내역만 조회합니다.

**Endpoint:** `GET /api/transactions/currency/{currency}`

**인증 필요:** ✅ Bearer Token

**URL Parameters:**
- `currency`: BTC, ETH, USDT 등

---

## 관리자 API

### 사용자 목록 조회

**Endpoint:** `GET /api/admin/users`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "enabled": true,
    "createdAt": "2024-11-01T00:00:00"
  }
]
```

---

### 페이징된 사용자 조회

**Endpoint:** `GET /api/admin/users/paged`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Query Parameters:**
- `page`, `size`, `sort`

---

### 사용자 상세 조회

**Endpoint:** `GET /api/admin/users/{id}`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

---

### 사용자 활성화

**Endpoint:** `PUT /api/admin/users/{id}/enable`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Response (200 OK):**
```json
"User enabled successfully"
```

---

### 사용자 비활성화

**Endpoint:** `PUT /api/admin/users/{id}/disable`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Response (200 OK):**
```json
"User disabled successfully"
```

---

### 시스템 통계 조회

**Endpoint:** `GET /api/admin/stats`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Response (200 OK):**
```json
{
  "totalUsers": 1500,
  "totalOrders": 25000,
  "totalTransactions": 50000,
  "totalTradingPairs": 10
}
```

---

### 거래 쌍 생성

**Endpoint:** `POST /api/admin/trading-pairs`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Request Body:**
```json
{
  "baseCurrency": "ETH",
  "quoteCurrency": "USDT",
  "symbol": "ETH/USDT",
  "currentPrice": 2500.00,
  "minOrderAmount": 0.01,
  "maxOrderAmount": 100.00,
  "tradingFee": 0.001,
  "active": true
}
```

---

### 거래 쌍 수정

**Endpoint:** `PUT /api/admin/trading-pairs/{id}`

**인증 필요:** ✅ Bearer Token (ADMIN 역할)

**Request Body:**
```json
{
  "currentPrice": 2550.00,
  "high24h": 2600.00,
  "low24h": 2450.00,
  "volume24h": 5000.00,
  "active": true
}
```

---

## WebSocket

실시간 시장 데이터를 위한 WebSocket 연결을 제공합니다.

### 연결

**Endpoint:** `ws://localhost:8080/ws` (개발)
**Endpoint:** `wss://your-domain.com/ws` (프로덕션)

**프로토콜:** STOMP over SockJS

### 구독 채널

**마켓 업데이트:**
```javascript
stompClient.subscribe('/topic/market/{symbol}', (message) => {
  const data = JSON.parse(message.body);
  console.log(data);
});
```

**주문장 업데이트:**
```javascript
stompClient.subscribe('/topic/orderbook/{symbol}', (message) => {
  const orderBook = JSON.parse(message.body);
  console.log(orderBook);
});
```

### 메시지 전송

**마켓 데이터 요청:**
```javascript
stompClient.send('/app/market', {}, JSON.stringify({
  symbol: 'BTC/USDT'
}));
```

---

## 에러 코드

### HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 500 | 서버 오류 |

### 일반적인 에러 응답

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/trading/orders"
}
```

### 비즈니스 로직 에러

에러 메시지는 문자열로 반환됩니다:
- `"Error: Username is already taken!"`
- `"Error: Insufficient balance"`
- `"Error: Cannot cancel this order"`
- `"Error: Wallet not found"`

---

## 사용 예시

### JavaScript (Axios)

```javascript
// 로그인
const login = async () => {
  const response = await axios.post('http://localhost:8080/api/auth/login', {
    username: 'johndoe',
    password: 'password123'
  });

  const token = response.data.token;
  localStorage.setItem('token', token);
};

// 인증이 필요한 요청
const getWallets = async () => {
  const token = localStorage.getItem('token');

  const response = await axios.get('http://localhost:8080/api/wallets', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.data;
};

// 주문 생성
const createOrder = async () => {
  const token = localStorage.getItem('token');

  const response = await axios.post('http://localhost:8080/api/trading/orders', {
    tradingPairId: 1,
    type: 'LIMIT',
    side: 'BUY',
    price: 45000,
    amount: 0.5
  }, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.data;
};
```

### cURL

```bash
# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"password123"}'

# 지갑 조회 (토큰 필요)
curl -X GET http://localhost:8080/api/wallets \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 주문 생성
curl -X POST http://localhost:8080/api/trading/orders \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "tradingPairId": 1,
    "type": "LIMIT",
    "side": "BUY",
    "price": 45000,
    "amount": 0.5
  }'
```

---

## 레이트 리미팅

현재 레이트 리미팅은 구현되지 않았습니다. 프로덕션 환경에서는 다음을 권장합니다:

- 일반 API: 100 요청/분
- 거래 API: 10 요청/초
- WebSocket: 연결당 1개

---

## 보안 고려사항

1. **HTTPS 사용**: 프로덕션에서는 반드시 HTTPS 사용
2. **토큰 저장**: LocalStorage 대신 HttpOnly 쿠키 권장
3. **CORS 설정**: 신뢰할 수 있는 도메인만 허용
4. **입력 검증**: 모든 입력값에 대한 서버 측 검증
5. **SQL Injection**: JPA 사용으로 기본 방어됨
6. **XSS**: 프론트엔드에서 입력값 이스케이프

---

## 변경 이력

- **v1.0.0** (2024-11-18): 초기 API 릴리스
