# s-market

이벤트 한정 수량 상품에 대한 선착순 판매 기능을 구현을 목적으로 한 이커머스 프로젝트입니다. 
짧은 시간 동안 대량의 주문 요청이 몰리는 상황에서도 안정적인 재고 관리를 목표로 하였습니다.

# 사용 기술
Java, Spring Boot, Spring Cloud Gateway, JPA, QueryDSL, MySQL, Redis, Kafka


# Architecture

![image](https://github.com/user-attachments/assets/95a55fd4-f46c-4c15-a287-eda7a98cecf9)

# DB ERD 구조
![image](https://github.com/user-attachments/assets/f904a22b-d17b-47a1-aab1-80b4f941d91b)
ERD 링크 : https://www.erdcloud.com/d/Wgx8k2m8XzcxEPoMx



# 구현한 기능

1. 회원(User) 도메인
- 로그인
- 회원가입
- 로그아웃

2. 인증(Auth) 도메인
- 액세스, 리프레쉬 토큰(JWT) 발급
- 블랙리스트 처리
- 인증 캐시 처리

2. 상품(Product) 도메인
- 상품 생성
- 상품 조회
- 재고 증가 및 감소

# 구현할 기능

- 장바구니 도메인
- 주문 도메인
- 결제 도메인

