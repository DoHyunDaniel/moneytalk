# 💸 MoneyTalk 머니톡

> **소비 분석 기반의 중고거래 & 예산 챗봇 플랫폼**  
> 중고 거래와 지출 관리, 그리고 AI 예산 상담을 하나로 통합한 생활 밀착형 서비스

---

## [ 기능 ]

### # 회원 관리
**사용자 등록 및 인증**
- 회원가입 기능 (이메일, 비밀번호, 닉네임)
- 로그인 / 로그아웃 기능 (JWT 기반)
- 내 정보 조회 및 수정
- 권한 관리 (USER / ADMIN)

> ※ 소셜 로그인, 비밀번호 재설정, 프로필 이미지 등은 향후 확장 가능

### # 중고 거래 시스템
**상품 관리**
- 상품 등록 / 조회 / 수정 / 삭제
- 판매 상태 변경 (판매 중 → 예약 중 → 판매 완료)
- 찜하기 기능

**후기 시스템**
- 거래 완료 후 리뷰 작성 (평점 + 내용)
- 판매자/구매자에 대한 후기 확인

### # 실시간 채팅 시스템
**채팅 기능**
- WebSocket 기반 1:1 실시간 채팅
- Redis Pub/Sub 기반 메시지 송수신
- 채팅방 목록 조회, 메시지 저장

### # 가계부 & 예산 관리 시스템
**지출/수입 기록**
- 금액, 카테고리, 메모, 날짜 등록
- 월별 소비 내역 조회

**예산 설정**
- 사용자별 월 예산 설정
- 예산 초과 시 알림

**소비 분석**
- 카테고리별 소비 통계
- 월별 소비 증감 비교
- 과소비 항목 분석 (TOP 3)

### # AI 예산 챗봇 시스템
**챗봇 예산 상담**
- “이번 달 많이 썼어?”
- “어떻게 줄일 수 있을까?”
- 소비 요약 → ChatGPT API 연동 → 자연어 응답

---
## [ ERD ]

![ERD](./erd.png)

---

## [ 기술 스택 ]

### 백엔드
- Java 17
- Spring Boot
- Spring Security (JWT)
- Spring WebSocket
- Spring Data JPA
- Lombok

### 데이터베이스
- MySQL
- Redis

### AI 연동
- OpenAI ChatGPT API

### 인프라 (계획)
- Docker
- AWS (EC2, S3 등)

### 문서화 & 협업
- Swagger (springdoc-openapi)
- GitHub, Slack, Notion

---

## [ 개발 계획 - 5주 ]

### 1주차: 기획 및 기본 구조 구축
- 요구사항 분석, ERD 설계
- 프로젝트 구조 세팅
- Spring Boot 기본 설정 (Security, JPA 등)
- 회원가입 / 로그인 기능 구현
- Swagger 문서화 적용

### 2주차: 중고 거래 기능
- 상품 등록 / 조회 / 상세 / 찜하기
- 판매 상태 변경 API
- 리뷰 작성 기능 추가
- 기본 UI 데이터 반환 테스트

### 3주차: 채팅 시스템
- WebSocket 설정
- 채팅방 생성, 메시지 송수신
- Redis Pub/Sub 적용
- 채팅 메시지 저장 로직

### 4주차: 가계부 & 예산 관리
- 수입/지출 등록, 월간 소비 조회
- 예산 설정, 예산 초과 계산
- 카테고리별 소비 통계 분석

### 5주차: 챗봇 연동 & 마무리
- ChatGPT API 연동
- 소비 요약 로직 → AI 응답 생성
- 테스트 코드 정리 및 기능 통합
- 배포 준비 및 최종 README 작성

---

## 🛠 최근 개발 일지 / Troubleshooting

### ✅ 2025-03-28

- Spring Security + JWT 기반 로그인/회원가입 기능 구현 완료
- Swagger UI에 JWT 인증 헤더 적용 (`bearerAuth`)
- GlobalExceptionHandler 구현 완료
- Swagger에서 `RestControllerAdvice` 충돌 발생
  - `springdoc-openapi-starter-webmvc-ui:2.2.0` 사용 시 `NoSuchMethodError` 발생
  - 원인: springdoc와 spring-web 간의 버전 불일치
  - 해결: springdoc-openapi-starter-webmvc-ui를 **2.1.0**으로 다운그레이드

```gradle
// build.gradle 의존성 예시
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'
```

- Swagger 문서에서 GlobalExceptionHandler의 에러 응답 예시 추가 완료
- 향후 기능으로 이메일 인증, Google/Naver/Kakao OAuth 로그인 연동도 계획 중


### ✅ 2025-04-11
## ❤️ 찜하기(좋아요) 기능 설계

### ✅ 구현 목적
- 사용자가 마음에 드는 상품을 저장해두고 나중에 다시 확인할 수 있도록 합니다.
- 상품의 인기 순위를 계산하거나, 사용자 취향 기반 추천에 활용할 수 있습니다.

### 🛠️ 기술 선택 배경

| 구현 방식 | 장점 | 단점 |
|-----------|------|------|
| **📦 DB 기반** (`favorite_products` 테이블 생성) | - 데이터 영속성<br>- 쿼리 확장 용이 (찜 목록, 찜 수 통계)<br>- 관리, 통계 시스템 연계 쉬움 | - 데이터량 증가 시 성능 이슈 가능 |
| **⚡ Redis 기반** (Set/SortedSet 활용) | - 실시간 인기 상품 캐싱 용이<br>- 빠른 응답 속도<br>- 서버 확장성과 캐시 처리에 유리 | - 휘발성 데이터 (재시작 시 초기화)<br>- 복합 쿼리 어려움 |

> ✅ **현재는 안정성과 연동 편의성을 고려해 DB 기반으로 구현**  
> ⏩ 이후 트래픽 증가 시 **Redis 캐싱 구조로 확장 예정**

---

### 📐 테이블 설계 (`favorite_products`)

```sql
Table favorite_products {
  id BIGINT [pk, increment]
  user_id BIGINT [ref: > users.id]
  product_id BIGINT [ref: > products.id]
  created_at DATETIME [default: `CURRENT_TIMESTAMP`]
}
```
---

# 🛍️ MoneyTalk - 2주차 백엔드 개발 정리

## 🔧 주요 구현 기능

### 1. 상품 등록 / 조회 / 상세 API
- 상품 등록 시 필드: `title`, `description`, `price`, `category`, `location`, `status`
- 상품 전체 목록 조회: `GET /api/products`
- 상품 단건 상세 조회: `GET /api/products/{id}`
- ✅ JWT 인증 기반으로 Swagger 테스트 완료

---

### 2. 찜하기(좋아요) 기능
- 찜 토글 API: `POST /api/wishlist/{productId}`
- 이미 찜한 상품이면 삭제, 아니라면 추가
- 상품 상세 정보 조회 시 찜 여부 반환 포함

---

### 3. 리뷰 작성 / 조회 / 평균 평점
- 리뷰 작성 시 이미지 업로드 지원 (Amazon S3 연동)
  - 다중 이미지 업로드
  - `multipart/form-data` 형식 처리
- 상품별 리뷰 리스트 조회: `GET /api/reviews/products/{productId}`
- 받은 리뷰 목록 조회: `GET /api/reviews/received`
- 평균 평점 조회: `GET /api/reviews/products/{productId}/average`

---

### 4. 구매 확정 및 구매 기록 저장
- 상품 구매 시 `purchase_history` 테이블에 기록 저장
- 추후 가계부 기능과 연동 예정

---

## 🐞 트러블슈팅 기록

### 1. `ProductResponseDto::from` 오류

> **오류 메시지**  
> `The type ProductResponseDto does not define from(Product)`

- **원인**: DTO 클래스에 정적 팩토리 메소드가 정의되어 있지 않음
- **해결 방법**:
```java
public static ProductResponseDto from(Product product) {
    return ProductResponseDto.builder()
        .id(product.getId())
        .title(product.getTitle())
        .price(product.getPrice())
        // 기타 필드...
        .build();
}
### 2. 리뷰 이미지 업로드 실패

> **문제**  
> 리뷰 작성 시 이미지 업로드가 되지 않음

- **원인**
  - `multipartResolver` 설정 누락
  - Amazon S3 버킷 권한 부족

- **해결 방법**
  - `application.yml`에 multipart 설정 추가
  - IAM 사용자에 `AmazonS3FullAccess` 권한 부여
  - `S3Uploader` 로직 점검 및 예외 처리 보강

---

### 3. Swagger JWT 인증 문제

> **문제**  
> Swagger에서 `Authorization` 헤더가 적용되지 않음

- **해결 방법**: Swagger 설정에 JWT 인증 스키마 명시
```java
.addSecurityItem(new SecurityRequirement().addList("JWT"))
.components(new Components().addSecuritySchemes("JWT",
    new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
))

## 🚧 다음 목표 (예정)

- 🔍 **상품 검색 및 필터 기능 추가**  
  사용자 검색어 및 조건(카테고리, 위치 등)에 따른 상품 필터링 기능 제공

- ❤️ **찜한 상품 리스트 조회 기능**  
  사용자가 찜한 상품 목록을 별도로 조회할 수 있는 API 구현
