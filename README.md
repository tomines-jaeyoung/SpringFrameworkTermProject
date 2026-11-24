# 온에어마켓 (OnAir Market) — Spring Framework Term Project

상품 소개 사진·동영상을 올리고, 정해진 기간 동안 입찰받는 경매 웹 서비스입니다.
프레임워크 실습 기말 과제로, 온에어마켓 설계 보고서의 **사진·동영상 기간 경매**와 **회원가입·로그인**을
Spring Boot + JPA로 구현했습니다. (라이브 경매는 보류: 판매 방식 선택 화면에 '준비 중'으로만 표시)

## 실행 방법 (IntelliJ)

1. MySQL 8.0 실행 (localhost:3306, 계정 `root` / `1234` — `application.properties`에서 변경 가능)
2. IntelliJ에서 이 폴더를 열고 Gradle 프로젝트로 불러오기 (JDK 21)
3. `OnAirMarketApplication` 실행 → http://localhost:8080
   - DB 스키마 `onairmarket`은 없으면 자동 생성됩니다 (`createDatabaseIfNotExist=true`).
   - 테이블은 JPA 엔티티로 자동 생성됩니다 (`spring.jpa.hibernate.ddl-auto=update`). 별도 SQL 파일이 필요 없습니다.
   - 처음 실행할 때 체험용 회원 2명과 샘플 경매 8개가 자동으로 들어갑니다.

| 체험 계정 | 아이디 | 비밀번호 |
|---|---|---|
| 판매자 (온에어셀렉트) | `onair_seller` | `onair1234` |
| 구매자 (수집가민지) | `onair_buyer` | `onair1234` |

업로드한 파일은 프로젝트 폴더의 `uploads/` 아래에 저장됩니다 (git에는 올리지 않음).

## 기술 구성

| 영역 | 사용 기술 |
|---|---|
| 서버 | Spring Boot 4.0.3, Java 21, Spring MVC |
| 화면 | Thymeleaf, CSS(기존 온에어마켓 브랜드 색), 약간의 JavaScript(남은 시간, 업로드 진행률) |
| 인증 | Spring Security 폼 로그인 + members 테이블, BCrypt 비밀번호 암호화 |
| DB | MySQL 8.0 + Spring Data JPA (Hibernate) |
| 파일 | 로컬 폴더 저장 + `auction_media` 메타데이터, `/api/media/{id}`로 읽기 (영상 구간 요청 지원) |

## 기능

| 설계 보고서 ID | 기능 | 구현 위치 |
|---|---|---|
| F-01 계정 인증 | 회원가입·로그인·로그아웃 (DB 연동) | `AuthController`, `MemberService`, `SecurityConfig` |
| F-02 목록 탐색 | 카테고리·검색·정렬(최신/마감 임박/입찰 많은 순/현재가)·진행 중 필터 | `HomeController`, `TimedAuctionRepository.search` |
| F-03 판매 방식 선택·등록 | 판매하기 → 방식 선택 → 사진·동영상 경매 등록 (즉시 시작/시작 예약, 7·30·90일/직접 지정) | `SellController`, `AuctionService.create` |
| F-04 파일 업로드 | 사진 8MB(JPG·PNG·WebP), 영상 50MB(MP4·WebM), 파일 앞부분으로 형식 확인, pending → ready, 실패 시 정리, 업로드 진행률 | `MediaService`, `onair.js` |
| F-05 입찰 | 현재가 + 1,000원만 허용, revision 조건부 갱신, 동시 입찰 시 한 건만 성공, 본인 경매·기간 외 입찰 차단 | `AuctionService.bid`, `TimedAuctionRepository.applyBid` |
| F-07 관심 저장 | 저장/해제, 복합 기본 키로 중복 방지 | `AuctionService.toggleBookmark` |
| 내 정보 | 내 판매 목록, 관심 경매, 내 입찰 기록 | `MyPageController` |

## DB 테이블 (JPA 엔티티)

| 테이블 | 엔티티 | 주요 조건 |
|---|---|---|
| `members` | `Member` | username UNIQUE, 비밀번호 BCrypt |
| `timed_auctions` | `TimedAuction` | 가격 원화 정수, 시각 밀리초, revision |
| `timed_bids` | `TimedBid` | UNIQUE (auction_id, revision) |
| `auction_media` | `AuctionMedia` | status pending/ready |
| `bookmarks` | `Bookmark` | 복합 기본 키 (user_id, auction_id) |

설계 보고서처럼 SQL 외래 키 제약은 만들지 않고(`ConstraintMode.NO_CONSTRAINT`), 참조 대상·소유권은 서비스에서 검사합니다.

## 설계 보고서와 다른 점

- **members 테이블 추가**: 보고서는 호스팅 계정 인증(로컬 users 테이블 없음) 기준이지만, 과제 조건(회원가입·로그인 DB 연동)에 맞춰 회원 테이블을 추가했습니다.
- **저장소**: Cloudflare D1 → MySQL, R2 → 로컬 `uploads/` 폴더.
- **구현 범위**: 라이브 경매(auctions·bids·messages·room_presence), 고정가 상품·예약(fixed_products·demo_orders), 관리자(admin_audit·seller_status), 요청 간격 제한(request_gates)은 이번 과제 범위에서 제외했습니다.
- 결제·정산은 없습니다 (체험 경매).
