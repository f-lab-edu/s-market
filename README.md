# S-market

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

3. 상품(Product) 도메인
- 상품 생성
- 상품 조회
- 재고 증가 및 감소

# 구현할 기능

- 장바구니 도메인
- 주문 도메인
- 결제 도메인

# 기술적인 고민
[Consumer 재시도 전략](https://sangyunpark99.tistory.com/entry/s-market-Consumer-%EC%9E%AC%EC%8B%9C%EB%8F%84-%EC%A0%84%EB%9E%B5)  
[이벤트성 상품 재고 차감 전략](https://sangyunpark99.tistory.com/entry/s-market-Redis-%EC%9D%B4%EB%B2%A4%ED%8A%B8%EC%84%B1-%EC%83%81%ED%92%88-%EC%9E%AC%EA%B3%A0-%EC%B0%A8%EA%B0%90-%EC%A0%84%EB%9E%B5)  
[Consumer 실패시 Redis 복구 전략](https://sangyunpark99.tistory.com/entry/s-market-Kafka-Consumer-Redis-%EB%B3%B5%EA%B5%AC-%EA%B0%9C%EC%84%A0)  
[Kafka 이벤트 순서 보장](https://sangyunpark99.tistory.com/entry/s-market-Kafka-%ED%8C%8C%ED%8B%B0%EC%85%98-%ED%82%A4-%EC%A0%84%EB%9E%B5%EC%9C%BC%EB%A1%9C-%EC%9D%B4%EB%B2%A4%ED%8A%B8-%EC%88%9C%EC%84%9C-%EB%B3%B4%EC%9E%A5%ED%95%98%EA%B8%B0)  
[실시간 재고 시스템에서 안전한 Redis 재고 조작법](https://sangyunpark99.tistory.com/entry/s-market-Redis-%EC%9E%AC%EA%B3%A0-%EC%8B%9C%EC%8A%A4%ED%85%9C%EC%97%90%EC%84%9C-%EC%82%AC%EC%9A%A9%ED%95%98%EB%8A%94-setQuantity)  


