# ⚙️ RunMarket Pacer (`pacer` / `runmarket-api`)

> **RunMarket 백엔드 핵심 코어 API & 리액티브 WebSocket 중계 서버**  
> Spring Boot 기반의 멀티모듈 프로젝트로, REST API 서비스(`https://api.runmarket.cc`), 고성능 WebFlux 기반의 실시간 위치 중계 WebSocket 서버(`wss://pulse.runmarket.cc`), 그리고 대회의 마라톤 데이터를 수집하는 배치 크롤러로 구성되어 있습니다.

---

## 🌐 RunMarket 생태계 & 연관 프로젝트

RunMarket 백엔드는 모바일 앱(`runmarket-app`), 웹 프론트엔드(`runmarket-front`), 그리고 Kubernetes 인프라(`iac`)의 중심점 역할을 수행합니다.

```
                  ┌─────────────────────────────────┐
                  │   runmarket-front (Web Frontend) │
                  │      https://runmarket.cc       │
                  └────────────────┬────────────────┘
                                   │ (회원가입 & Cloudflare Turnstile CAPTCHA)
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          runmarket-app (Mobile App)                     │
│                        Bundle ID: cc.runmarket.app                      │
└──────────────┬──────────────────────────────────┬───────────────────────┘
               │                                  │
      HTTP REST│ (Bearer JWT)                     │WebSocket Stream
  api.runmarket.cc                                │pulse.runmarket.cc
               ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          pacer (Backend Service)                        │
│                                                                         │
│  ├── [web]     : Spring MVC REST API (인증, 토큰 발급, 레이스/기록)       │
│  ├── [socket]  : Spring WebFlux + Reactive Redis 실시간 위치 중계     │
│  ├── [batch]   : Spring Batch + Jsoup 마라톤/레이스 대회 웹 크롤러     │
│  └── [core]    : application, domain, infrastructure, event-bus         │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                     Kubernetes Helm Deployment
                                 ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         iac (Infrastructure)                            │
│                 Kubernetes / Helm Charts (`helm/runmarket`)             │
│                                                                         │
│  - Docker Images : `gudrb963/runmarket-pacer` (web)                     │
│                    `gudrb963/runmarket-pacer-socket` (socket)          │
│                    `gudrb963/runmarket-pacer-batch` (batch)            │
│  - Database     : PostgreSQL 17 (Flyway DB Migration)                   │
│  - Ingress      : Nginx Ingress Controller (`api.runmarket.cc`)         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ 멀티모듈 구조 및 모듈별 역할 (Multi-Module Architecture)

`pacer`는 관심사의 분리(SoC)와 확장성을 보장하기 위해 7개의 Gradle 하위 모듈로 구성되어 있습니다.

```
pacer/
├── web/            # [모듈] Spring MVC REST API (gudrb963/runmarket-pacer)
├── socket/         # [모듈] Spring WebFlux WebSocket 중계 서버 (gudrb963/runmarket-pacer-socket)
├── batch/          # [모듈] Spring Batch 크롤러 및 데몬 (gudrb963/runmarket-pacer-batch)
├── application/    # [모듈] 비즈니스 서비스 층, 트랜잭션 및 캐싱 처리
├── domain/         # [모듈] 프레임워크 독립적인 순수 도메인 모델 & 엔티티
├── infrastructure/ # [모듈] Spring Data JPA, PostgreSQL, Flyway, JJWT, Mail 구현체
└── event-bus/      # [모듈] 모듈 간 비동기 이벤트 발행 및 트랜잭션 이벤트 핸들러
```

### 🧩 모듈별 상세 역할

| 모듈명 | 주요 역할 & 담당 기능 | 생성 이미지 (Jib) |
|---|---|---|
| **`web`** | **REST API 서버**<br>• 회원 로그인 (`/api/v1/auth/login`) 및 Cloudflare Turnstile CAPTCHA 백엔드 검증<br>• 소켓 접근용 JWT 토큰 발급 (`/api/v1/socket-token`)<br>• 레이스/달리기 목록 조회 및 좋아요 기능 (`/api/v1/races`)<br>• 오프라인 런 기록 업로드 및 동기화 (`/api/v1/runs`) | `gudrb963/runmarket-pacer` |
| **`socket`** | **고성능 WebSocket 실시간 위치 중계 서버**<br>• Spring WebFlux & Reactive Redis 기반 논블로킹 위치 스트리밍<br>• RUNNER 러너 위치 메세지 수신 및 SPECTATOR 관전자 그룹/1:1 채널 브로드캐스트<br>• 소켓 커넥션 레벨 JWT 토큰 검증 및 보안 처리 | `gudrb963/runmarket-pacer-socket` |
| **`batch`** | **레이스/마라톤 대회 정보 수집 배치**<br>• Spring Batch 및 Jsoup 기반 외부 마라톤/레이스 웹 사이트 데이터 자동 수집<br>• 레이스 데이터베이스 자동 최신화 | `gudrb963/runmarket-pacer-batch` |
| **`application`** | **어플리케이션 서비스 레이어**<br>• 도메인 객체 조율, 데이터 유효성 검증, `@Transactional` 트랜잭션 제어, `@Cacheable` 캐싱 | - |
| **`domain`** | **순수 도메인 레이어**<br>• 특정 프레임워크에 의존하지 않는 순수 Java/Kotlin 엔티티, 값 객체(VO), 비즈니스 도메인 규칙 | - |
| **`infrastructure`** | **인프라스트럭처 영속성 & 바운디드 라이브러리**<br>• Spring Data JPA, PostgreSQL 연동, Flyway 데이터베이스 형상 관리/마이그레이션<br>• Spring Security, JJWT, Spring Mail 연동 구현체 제공 | - |
| **`event-bus`** | **이벤트 버스 모듈**<br>• 모듈 간 결합도를 낮추기 위한 비동기 및 트랜잭션 연계 이벤트 핸들러 제공 | - |

---

## 📡 REST API & WebSocket 명세 (API Specifications)

### 1. 주요 REST API 명세 (`https://api.runmarket.cc`)

| 메서드 | 경로 | 설명 | 인증 필요 여부 |
|--------|------|------|:---:|
| **POST** | `/api/v1/auth/login` | 회원 로그인 → JWT 발급 (Turnstile 검증) | ❌ |
| **POST** | `/api/v1/socket-token` | 소켓 통신용 전용 JWT 토큰 발급 | ✅ Bearer |
| **GET** | `/api/v1/races` | 레이스/마라톤 대회 목록 조회 | ✅ Bearer |
| **GET** | `/api/v1/races/{id}` | 레이스 상세 정보 조회 | ✅ Bearer |
| **POST** | `/api/v1/races/{id}/like` | 레이스 좋아요 | ✅ Bearer |
| **DELETE** | `/api/v1/races/{id}/like` | 레이스 좋아요 취소 | ✅ Bearer |
| **POST** | `/api/v1/runs` | 앱 오프라인 런 기록 동기화 저장 | ✅ Bearer |

### 2. WebSocket 엔드포인트 명세 (`wss://pulse.runmarket.cc`)

| WebSocket 경로 | 역할 | 기능 |
|----------------|:---:|------|
| `/ws/runner/{runnerId}?token=<socket-jwt>` | **RUNNER** | 내 실시간 GPS 위치 (위도, 경도, 페이스, 거리, 시간) 퍼블리시 |
| `/ws/runner/{runnerId}?token=<socket-jwt>` | **SPECTATOR** | 특정 러너 1명의 실시간 위치 구독 |
| `/ws/group/{groupId}?token=<socket-jwt>` | **SPECTATOR** | 지정된 그룹 코드 내 모든 러너의 위치 메세지 실시간 구독 |

#### 📤 러너 → 서버 위치 데이터 (Publish Payload)
```json
{
  "lat": 37.5665,
  "lng": 126.9780,
  "pace": "5:30",
  "distance": 3.2,
  "time": 1234
}
```

#### 📥 관전자 ← 서버 브로드캐스트 데이터 (Subscribe Message)
```json
{
  "runnerId": "runner-123",
  "data": {
    "lat": 37.5665,
    "lng": 126.9780,
    "pace": "5:30",
    "distance": 3.2,
    "time": 1234
  }
}
```

---

## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 / 라이브러리 |
|---|---|
| **Language / Platform** | Java `25` |
| **Framework** | Spring Boot `4.1.0-M4` |
| **Web & Async** | Spring MVC, Spring WebFlux, Reactive Redis |
| **Database & Persistence** | PostgreSQL `17`, Spring Data JPA, Flyway Migration |
| **Security & Auth** | Spring Security, JJWT `0.13.0`, Cloudflare Turnstile |
| **Batch & Crawling** | Spring Batch, Jsoup `1.18.3` |
| **Container Build** | Google Jib `3.5.3` |
| **Testing** | JUnit 5, Testcontainers (Redis Testcontainer) |

---

## 🚀 로컬 개발 및 실행 가이드 (Getting Started)

### 1. 사전 요구사항
- **Java**: OpenJDK 25 이상
- **Gradle**: 프로젝트 내 포함된 `./gradlew` Wrapper 사용
- **Database & Cache**: PostgreSQL 및 Redis (로컬 또는 Docker 컨테이너)

### 2. 로컬 실행 명령어

```bash
# REST API 서버 실행 (web 모듈)
./gradlew :web:bootRun

# WebSocket 실시간 중계 서버 실행 (socket 모듈)
./gradlew :socket:bootRun

# 배치 수집 프로그램 실행 (batch 모듈)
./gradlew :batch:bootRun
```

### 3. Jib을 이용한 Docker 컨테이너 이미지 빌드
```bash
# 로컬 Docker 데몬으로 컨테이너 이미지 빌드
./gradlew jibDockerBuild

# Docker Registry (Docker Hub)로 빌드 및 푸시
./gradlew jib
```

---

## ☁️ Kubernetes 배포 연동 (`iac`)

`pacer` 빌드 산출물은 `iac` 프로젝트의 Helm Chart(`helm/runmarket`)를 통해 Kubernetes 클러스터에 배포됩니다.

### 필수 Kubernetes Secret 구성 (`runmarket-app-secrets`, `runmarket-db-credentials`)
```bash
# 앱 및 JWT, Admin Secret 생성
kubectl create secret generic runmarket-app-secrets \
  --from-literal=jwt-secret=<YOUR_JWT_SECRET> \
  --from-literal=mail-password=<YOUR_MAIL_PASSWORD> \
  --from-literal=admin-email=<YOUR_ADMIN_EMAIL> \
  --from-literal=admin-password=<YOUR_ADMIN_PASSWORD>

# PostgreSQL DB 접속 Secret 생성
kubectl create secret generic runmarket-db-credentials \
  --from-literal=username=<DB_USERNAME> \
  --from-literal=password=<DB_PASSWORD> \
  --from-literal=database=<DB_NAME>
```

---

## 📄 라이선스 (License)

본 프로젝트는 RunMarket 서비스 코어 시스템입니다.
