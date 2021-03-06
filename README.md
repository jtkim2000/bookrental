# BookRental

# 도서대여 서비스

- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [도서대여 서비스](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
    - [Event Storming 결과](#Event-Storming-결과)
    - [헥사고날 아키텍처 다이어그램 도출](#헥사고날-아키텍처-다이어그램-도출)
  - [구현:](#구현:)
    - [DDD 의 적용](#DDD-의-적용)
    - [기능적 요구사항 검증](#기능적-요구사항-검증)
    - [비기능적 요구사항 검증](#비기능적-요구사항-검증)
    - [Saga](#saga)
    - [CQRS](#cqrs)
    - [Correlation](#correlation)
    - [GateWay](#gateway)
    - [Polyglot](#polyglot)
    - [동기식 호출(Req/Resp) 패턴](#동기식-호출reqresp-패턴)
    - [비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트](#비동기식-호출--시간적-디커플링--장애격리--최종-eventual-일관성-테스트)
  - [운영](#운영)
    - [Deploy / Pipeline](#deploy--pipeline)
    - [Config Map](#configmap)
    - [Secret](#secret)
    - [Circuit Breaker](#circuit-breaker와-fallback-처리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [Zero-downtime deploy (Readiness Probe) 무정지 재배포](#zero-downtime-deploy-readiness-probe-무정지-재배포)
    - [Self-healing (Liveness Probe))](#self-healing-liveness-probe)

# 서비스 시나리오

기능적 요구사항
1. 사용자는 회원 등록을 한다.
2. 사서는 도서를 등록한다.
3. 회원은 도서를 대여 신청한다.
4. 도서가 대여신청되면 회원에게 대여해 준다.
5. 시스템은 대여 신청한 수량만큼 도서 재고를 줄인다.
6. 회원은 대여한 도서를 반납한다.
7. 반납된 도서의 상태가 불량(BAD)이면 해당 회원에게 경고장을 발송한다.
8. 경고장 발송 후 해당 회원의 등급(AAA->BBB)과 상태(GOOD->BAD)를 강등한다.
9. 경고장을 받은 회원은 사유서를 제출한다.
10. 사유서을 제출한 회원은 등급(BBB->AAA)과 상태(BAD->GOOD)가 원상복귀된다.


비기능적 요구사항
1. 트랜잭션
    1. 도서 대여 신청 수량만큼 도서 재고에 즉시 반영되어야 한다. (Sync 호출)
1. 장애격리
    1. 회원/도서대여(BookRental)/경고장/사유서/BookAdmin 관리 기능이 수행되지 않더라도 도서대여신청(BookRentalRequest)은 중단없이 신청 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
    2. 회원등록 시스템이 과중되면 사용자를 잠시동안 받지 않고 재접속하도록 유도한다  Circuit breaker, fallback  <---수정필요


# 분석/설계


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:


### 이벤트 도출
![image](https://user-images.githubusercontent.com/82795757/123209504-a8466e80-d4fb-11eb-8b6b-6aa6657e90da.png)
![image](https://user-images.githubusercontent.com/82795757/123209515-b09ea980-d4fb-11eb-9450-f5098ac8577c.png)

### 중복된 의미 이거나 불필요한 이벤트 제외
![image](https://user-images.githubusercontent.com/82795757/123209661-e5aafc00-d4fb-11eb-9444-f0de37e46e73.png)

### 완성된 모형
![image](https://user-images.githubusercontent.com/82795757/123209949-4cc8b080-d4fc-11eb-8d19-06f32242143e.png)

### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
![image](https://user-images.githubusercontent.com/82795757/123211709-c8c3f800-d4fe-11eb-8305-62a55571c88a.png)

    1. 신규 사용자는 회원 등록을 한다.
    2. 사서는 신규 도서를 등록한다.
    3. 회원은 도서 대여 신청한다.
    4. 도서 대여신청 수량 만큼 재고를 감소시킨다. (Sync 호출)
    5. 회원에게 도서를 대여한다.

![image](https://user-images.githubusercontent.com/82795757/123212289-9070e980-d4ff-11eb-827d-4efe9bf2f430.png)

    1. 회원은 도서를 반납한다.
    2. 반납된 도서 수량 만큼 재고를 증가시킨다.
    3. 반납된 도서의 상태가 불량(BAD)이면 회원에게 경고장을 발송한다.
    4. 경고장을 받는 회원의 등급(AAA->BBB)과 상태(GOOD->BAD)를 강등시킨다.
    
![image](https://user-images.githubusercontent.com/82795757/123212722-1ab94d80-d500-11eb-8596-1fd1b2cec794.png)

    1. 경고장을 받은 회원은 사유서(reasonLetter)를 제출한다.
    2. 사유서를 제출한 회원의 등급(AAA->BBB)과 상태(GOOD->BAD)를 원복시킨다..
    
    
### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/82795757/123218510-c49bd880-d506-11eb-9533-973fe6bde7eb.png)

    1. 도서 대여 요청이 들어오면 도서의 재고를 즉시 Sync 호출을 통해 확인하고 반영함.
    2. 회원/도서대여(BookRental)/경고장/사유서/BookAdmin 서비스 기능이 수행되지 않더라도 도서대여신청(BookRentalRequest) 기능은 중단없이 
       Async(Event-driven) 방식으로 통신하여 다른 서비스의 장애가 도서대여신청에 영향을 주지 않음(장애 격리).
    3. bookAdmin의 bookRentalMonitoringPage를 통해 회원의 도서 대여 상태를 확인할 수 있음
          
### 최종 완성된 모형

![image](https://user-images.githubusercontent.com/82795757/122666276-47f7ba00-d1e7-11eb-9a09-1f2e0ed71ff9.png)


## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/82795757/123220516-00d03880-d509-11eb-9622-bf0547211c0c.png)
    
    - 헥사고날 아키텍처 다이어그램에 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 Pub/Sub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라 각각의 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다. 
(각각의 포트넘버는 8081 ~ 808n 까지 부여되어 있다.)

```
cd gateway
mvn spring-boot:run

cd member
mvn spring-boot:run 

cd book
mvn spring-boot:run  

cd bookRentalRequest
mvn spring-boot:run  

cd bookRental
mvn spring-boot:run  

cd warningLetter
mvn spring-boot:run  

cd reasonLetter
mvn spring-boot:run  

cd bookAdmin
mvn spring-boot:run

```
## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였음. (아래 예시는 bookRentalRequest 마이크로 서비스)

```
package bookrental;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="BookRequest_table")
public class BookRequest {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long requestId;
    private Long memberId;
    private Long bookId;
    private Integer qty;
    private String status = "Book Requested";

    @PostPersist
    public void onPostPersist(){

        BookRentalRequestApplication.applicationContext.getBean(bookrental.external.BookService.class)
        .checkBookQtyAndModifyQty(this.getBookId(), this.getQty());

        BookRequested bookRequested = new BookRequested();
        BeanUtils.copyProperties(this, bookRequested);
        bookRequested.publishAfterCommit();
    }


    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}



```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (H2, SQL Server) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다. (로컬개발환경에서는 모두 H2를, 쿠버네티스에서는 H2와 SQLServer를 각각 사용하였다)
```
[ bookRentalRequest 마이크로 서비스 ] -----
package bookrental;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="bookRequests", path="bookRequests")
public interface BookRequestRepository extends PagingAndSortingRepository<BookRequest, Long>{
}

```
- 적용 후 REST API 의 테스트
```
# bookRentalRequest (도서대여 요청) 서비스의 요청처리
http POST http://localhost:8088/bookRequests memberId=1 bookId=1 qty=10

# 도서 대여 요청 상태 확인
http GET http://localhost:8088/bookRentalMonitoringPages

```

## 기능적 요구사항 검증

1. 신규사용자는 회원 등록한다.

--> 회원 등록을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123230705-7b518600-d512-11eb-9326-58e5d5e61970.png)


2. 사서는 신규 도서를 등록한다.

--> 신규 도서 등록을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123230911-ab008e00-d512-11eb-9931-f55c7657550f.png)


3. 회원이 도서 대여 신청한다.

--> 정상적으로 대여 신청됨을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123231043-cb304d00-d512-11eb-9577-49fa5193aace.png)


4. 도서 대여 신청하면 도서가 대여 된다. 

--> 정상적으로 대여됨을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123231297-0af73480-d513-11eb-8d04-5f9e09655657.png)


5. 회원은 대여한 도서를 반납한다. 

--> 정상적으로 반납됨을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123231549-43970e00-d513-11eb-903e-3e3a663ceb45.png)


6. 반납된 도서의 상태가 불량(BAD)이면 경고장을 발송한다.

--> 경고장 발송을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123231728-6f19f880-d513-11eb-9538-a6409be7401a.png)


7. 경고장 받은 회원의 등급과 상태가 강등된다.

--> 강등됨을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123231854-8d7ff400-d513-11eb-945a-0409637298db.png)


8. 경고장 받은 회원이 사유서를 제출하면 회원의 등급과 상태가 원상복구된다.

--> 경고장 받은 회원의 사유서 제출을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123232655-55c57c00-d514-11eb-869a-51dc7358fbc9.png)


--> 원상복구됨을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123232127-cfa93580-d513-11eb-9d96-87d6831a33ae.png)


## 비기능적 요구사항 검증

1. 트랜잭션

도서 대여 신청하면 도서 재고가 신청 수량만큼 감소된다. (Sync 호출)

--> 신청한 수량만큼 즉시(Sync) 감소됨을 확인함

(도서 대여 신청 --> 당초 수량 200권에서 20권 신청)

![image](https://user-images.githubusercontent.com/82795757/123233138-c53b6b80-d514-11eb-9e98-faf02da22c13.png)

(대여 신청된 수량만큼 도서 수량 감소 --> 200에서 180권으로 20권 감소됨을 확인)

![image](https://user-images.githubusercontent.com/82795757/123233415-0469bc80-d515-11eb-9056-f365bc53a7bb.png)


2. 장애격리
회원등록/도서대여/경고장/사유서/BookAdmin 관리 기능이 수행되지 않더라도 도서대여신청(BookRentalRequest) 기능은 중단없이 Async(Event-driven) 방식으로 통신, 장애 격리가 되어야 한다.
Async (event-driven), Eventual Consistency

--> 회원등록/도서대여/경고장/사유서/BookAdmin 서비스를 모두 내린 상태에서도 도서대여신청을 받을 수 있음을 확인함.

![image](https://user-images.githubusercontent.com/82795757/123244463-eb660900-d51e-11eb-884b-ddeb0730f528.png)


3. 도서(Book) 서비스가 과중되면 사용자를 잠시동안 받지않고 재접속하도록 유도한다. Circuit Breaker, fallback  <---- fallback 구현 후 수정 필요

--> 운영단계의 Hystrix를 통한 Circuit Breaker 구현에서 검증하도록 함.

## Saga
분석/설계 및 구현을 통해 이벤트를 Publish/Subscribe 하도록 구현하였다.
[Publish]
![image](https://user-images.githubusercontent.com/82795757/123245322-abebec80-d51f-11eb-8b02-7c35dbb61e85.png)

[Subscribe]
![image](https://user-images.githubusercontent.com/82795757/123245600-f4a3a580-d51f-11eb-892f-49695f952eb0.png)


## CQRS
Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능하게 구현해 두었다.

이번 과제(Project)에서 View 역할은 BookAdmin 서비스가 수행한다.

CQRS를 구현하여 도서대여(BookRental) 건에 대한 상태는 BookRental 마이크로서비스의 접근없이 BookAdmin의 BookRentalMonitoringPage 페이지를 통해 조회할 수 있도록 구현하였다.

- 도서대여(Book Lent) 실행 후 BookRentalMonitoringPage 화면

![image](https://user-images.githubusercontent.com/82795757/123247574-fff7d080-d521-11eb-97e6-8f9f71a3a4df.png)


- 도서반납(Book Returned) 후 BookRentalMonitoringPage 화면

![image](https://user-images.githubusercontent.com/82795757/123248241-a9d75d00-d522-11eb-940e-70f5a64a9594.png)


위와 같이 대여요청(BookRenralRequest)을 하면 BookRentalRequest -> Book -> BookRentalRequest -> BookRental 로 도서대여가 진행되고

대여된 도서가 반납되면 status가 “Book Returned”로 Update됨을 확인할 수 있다..

## Correlation 
각 이벤트 건(메시지)이 어떤 Policy를 처리할 때 어떤건에 연결된 처리건인지를 구별하기 위한 Correlation-key를 제대로 연결하였는지를 검증하였다.
![image](https://user-images.githubusercontent.com/82795757/123248640-1d796a00-d523-11eb-9b6c-52832ad7ddb3.png)


## Gateway 
API GateWay를 통하여 마이크로 서비스들의 진입점을 통일할 수 있다.
다음과 같이 Gateway를 적용하여 모든 마이크로서비스들은 http://localhost:8088/{context}로 접근할 수 있다.

``` (gateway) application.yaml
============================
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: member
          uri: http://localhost:8081
          predicates:
            - Path=/members/** 
        - id: book
          uri: http://localhost:8082
          predicates:
            - Path=/books/** 
        - id: bookRentalRequest
          uri: http://localhost:8083
          predicates:
            - Path=/bookRequests/** 
        - id: bookRental
          uri: http://localhost:8084
          predicates:
            - Path=/bookRentals/** 
        - id: warningLetter
          uri: http://localhost:8085
          predicates:
            - Path=/warningLetters/** 
        - id: reasonLetter
          uri: http://localhost:8086
          predicates:
            - Path=/reasonLetters/** 
        - id: bookAdmin
          uri: http://localhost:8087
          predicates:
            - Path= /bookRentalMonitoringPages/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: member
          uri: http://member:8080
          predicates:
            - Path=/members/** 
        - id: book
          uri: http://book:8080
          predicates:
            - Path=/books/** 
        - id: bookRentalRequest
          uri: http://bookRentalRequest:8080
          predicates:
            - Path=/bookRequests/** 
        - id: bookRental
          uri: http://bookRental:8080
          predicates:
            - Path=/bookRentals/** 
        - id: warningLetter
          uri: http://warningLetter:8080
          predicates:
            - Path=/warningLetters/** 
        - id: reasonLetter
          uri: http://reasonLetter:8080
          predicates:
            - Path=/reasonLetters/** 
        - id: bookAdmin
          uri: http://bookAdmin:8080
          predicates:
            - Path= /bookRentalMonitoringPages/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```


## Polyglot

마이크로 서비스에 대하여 로컬과 쿠버네티스의 DBMS 를 달리 구성하였다.
BookRental 을 구성하는 마이크로서비스가 사용하는 DBMS를 다음과 같이 2가지 DBMS를 적용하였다.
- 로컬에서의 DB 구성 ---- 모두 H2
- 구버네티스 DB 구성 ---- SQL Server(member, book),  H2 (나머지 서비스)

```
# (Book) application.yml

spring:
  profiles: default
...
---
spring:
  profiles: docker
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://jtkimdbserver.database.windows.net:1433;database=bookrentaldb;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
    username: dbadmin
    password: jtkim2000!!
...

# (member) application.yml

spring:
  profiles: default
...
---

spring:
  profiles: docker
  datasource:
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    url: jdbc:sqlserver://jtkimdbserver.database.windows.net:1433;database=bookrentaldb;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;  
    #username: dbadmin
    #password: jtkim2000!!
    username: ${SQLSERVER_USERNAME}
    password: ${SQLSERVER_PASSWORD}
    
==================
# (BookRentalRequest, BookRental, warningLetter, reasonLetter, bookAdmin) application.yml

spring:
  profiles: default
  <--- 별도 설정이 없으면 H2 DB를 default로 사용하도록 설정되어 있음(pom.xml)
...
---

spring:
  profiles: docker
  <--- 별도 설정이 없으면 H2 DB를 default로 사용하도록 설정되어 있음(pom.xml)
...
  
```


## 동기식 호출(Req/Resp) 패턴

분석단계에서의 조건 중 하나로 도서대여요청(BookRentalRequest)->도서(Book)간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
호출 프로토콜은 RestController를 FeignClient 를 이용하여 호출하도록 한다. 

- 도서 잔여분 확인을 위해 도서 서비스 호출을 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (BoonRentalRequest) BookService.java


package bookrental.external;
...
@FeignClient(name="book", url="http://book:8080")
public interface BookService {

    @RequestMapping(method= RequestMethod.GET, path="/books/checkBookQtyAndModifyQty")
    public void checkBookQtyAndModifyQty(@RequestParam("bookId") Long bookId,
                                        @RequestParam("qty") Integer qty);
   
}
```

- 도서 대여 신청을 받은 직후 도서(Book)의 잔여량을 확인하고 요청 수량만큼 잔여량을 감하도록 처리
```
# BookController.java

package bookrental;
...
@RestController
public class BookController {
        @Autowired
        BookRepository bookRepository;

        @RequestMapping(value = "/books/checkBookQtyAndModifyQty",
                method = RequestMethod.GET,
                produces = "application/json;charset=UTF-8")

        public void checkBookQtyAndModifyQty(HttpServletRequest request, HttpServletResponse response)
                throws Exception {
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("##### /book/checkBookQtyAndModifyQty  called #####");
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
 
                Long bookId = Long.valueOf(request.getParameter("bookId"));
                Integer qty = Integer.parseInt(request.getParameter("qty"));

                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                System.out.println("##### bookId = " + bookId);
                System.out.println("##### qty = " + qty);
                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

                Book book = bookRepository.findByBookId(bookId);
                ...
                if(book.getQty() >= qty) {
                        book.setQty(book.getQty() - qty);
                        bookRepository.save(book);
                }
        }
 }

```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 도서(Book) 서비스가 장애가 나면 도서대여신청(BookRentalRequest)도 못 받은 다는 것을 확인:


```
# 책(Book) 서비스를 잠시 내려놓음 (ctrl+c)

#도서대여요청(BookRentalRequest) 처리
http POST localhost:8088/orders bookId=1 qty=10 customerId=1   #Fail
http POST localhost:8088/orders bookId=2 qty=20 customerId=2   #Fail

#도서(Book) 서비스 재기동
cd Book
mvn spring-boot:run

#도서대여요청(BookRentalRequest) 처리
http POST localhost:8088/orders bookId=1 qty=10 customerId=1   #Success
http POST localhost:8088/orders bookId=2 qty=20 customerId=2   #Success
```
추후 운영단계에서는 Circuit Breaker를 이용하여 도서확인(Book) 서비스에 장애가 발생하여도 도서대여 신청은 받을 수 있도록 개선된 것을 보일 것임.


## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

도서대여요청(BookRentalRequest)이 이루어진 후에 도서대여(BookRental) 서비스에 요청을 알려즈는 이벤트 처리 방식을 비 동기식(Pub/Sub)으로 처리하여 도서대여요청이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 도서대여요청(BookRentalReques)에서의 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package bookrental;
...

@Entity
@Table(name="BookRequest_table")
public class BookRequest {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long requestId;
    private Long memberId;
    private Long bookId;
    private Integer qty;
    private String status = "Book Requested";

    @PostPersist
    public void onPostPersist(){

        BookRentalRequestApplication.applicationContext.getBean(bookrental.external.BookService.class)
        .checkBookQtyAndModifyQty(this.getBookId(), this.getQty());

        BookRequested bookRequested = new BookRequested();
        BeanUtils.copyProperties(this, bookRequested);
        bookRequested.publishAfterCommit();
    }

```
- 도서대여(BookRental) 마이크로 서비스에서는 도서대여요청 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package bookrental;

...

@Service
public class PolicyHandler{
    @Autowired BookRentalRepository bookRentalRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverBookRequested_LendBook(@Payload BookRequested bookRequested){

        if(!bookRequested.validate()) return;

        System.out.println("\n\n##### listener LendBook : " + bookRequested.toJson() + "\n\n");

        // 책을 빌려주면 상태(status)를 "Book Lent"로 변경한다.
        BookRental bookRental = new BookRental();

        bookRental.setBookId(bookRequested.getBookId());
        bookRental.setBookStatus("GOOD");  // 책을 빌려줄때 책 상태는 "GOOD"으로 설정
        bookRental.setMemberId(bookRequested.getMemberId());
        bookRental.setQty(bookRequested.getQty());
        bookRental.setStatus("Book Lent !!");
        bookRentalRepository.save(bookRental);

    }

```

도서대여(BookRental) 서비스는 도서대여신청(BookRentalRequest)/도서(Book) 서비스와 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에 도서대여(BookRental) 서비스가 유지보수로 인해 잠시 내려간 상태라도 도서대여신청(BookRentalRequest)을 받는데 문제가 없다: 없다:
```
# 도서대여 서비스 (BookRental) 를 잠시 내려놓음 (ctrl+c)

#도서대여요청 처리
http POST http://localhost:8088/bookRequests memberId=1 bookId=1 qty=10   #Success
http POST http://localhost:8088/bookRequests memberId=2 bookId=2 qty=20   #Success

#도서대여 요청 상태 확인
http localhost:8088/bookRentalRequests     # 도서대여요청이 누적되어 있고 도서대여로 전달안되고 있음

#도서대여 서비스 기동
cd bookRental
mvn spring-boot:run

#도서대여 상태 확인
http GET http://localhost:8088/bookRentals # 도서대여신청 이벤트가 도서대여에 전달되어 대여실행되고 모든 도서대여의 상태가 "Book Lent"로 되어있음을 확인

```


# 운영

## Deploy / Pipeline

- git에서 소스 가져오기
```
git clone https://github.com/jtkim2000/bookrental.git
```
- Build 하기
```
cd ./member
mvn package

cd ../book
mvn package

cd ../bookrentalrequest
mvn package

cd ../bookrental
mvn package

cd ../warningletter
mvn package

cd ../reasonletter
mvn package

cd ../bookadmin
mvn package

cd ../gateway
mvn package

```

- Docker Image build/Push/
```

cd ../member
ocker build -t jtkimacr.azurecr.io/member:latest .
docker push jtkimacr.azurecr.io/member:latest

cd ../book
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../bookrentalrequest
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../bookrental
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../warningletter
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../reasonletter
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../bookadmin
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

cd ../gateway
docker build -t jtkimacr.azurecr.io/book:latest .
docker push jtkimacr.azurecr.io/book:latest

```

- yml파일 이용한 deploy
```
kubectl apply -f deployment.yml

- bookrental/member/kubernetes/deployment.yml 파일 
apiVersion: apps/v1
kind: Deployment
metadata:
  name: member
  labels:
    app: member
spec:
  replicas: 1
  selector:
    matchLabels:
      app: member
  template:
    metadata:
      labels:
        app: member
    spec:
      containers:
        - name: member
          image: jtkimacr.azurecr.io/member:latest
          ports:
            - containerPort: 8080
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
              #port: 8080 -> 7890
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
          env:
          - name: SQLSERVER_USERNAME
            valueFrom:
              secretKeyRef: 
                name: sqlserver-basic-auth
                key: username
          - name: SQLSERVER_PASSWORD
            valueFrom:
              secretKeyRef: 
                name: sqlserver-basic-auth
                key: password
```	  

- deploy 완료

--> kubectl get all

![image](https://user-images.githubusercontent.com/82795757/123262563-f88cf300-d532-11eb-9367-49b541a445bb.png)


## ConfigMap 
- 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리
- 도서대여서비스 프로젝트 과제에서는 경고장 서비스에서 경고장 발송 시 “경고장 내용”을 ConfigMap처리하기로 함.
  --> configmap 환경변수의 내용은 "PleaseCareBook"으로 설정하고 경고장 내용으로 지정

- Java 소스에 “경고장 내용”을 환경변수 configmap의 값을 읽어와 경고장 내용을 화면 출력 처리.(/warningletter/src/main/java/bookrental/WarningLetter.java) 
  시스템의 환경변수인 configmap의 값("PleaseCareBook")을 가져와서 warningMsg 변수에 저장
        --> private String warningMsg = System.getenv("configmap");
        
  경고장 발송 위해 시스템 환경변수인 configmap의 값으로 경고장을 발송한다.(화면 출력)

![image](https://user-images.githubusercontent.com/82795757/123267754-52dc8280-d538-11eb-9c25-2094155d3c9a.png)


- application.yml 파일에 configmap 연결

![image](https://user-images.githubusercontent.com/82795757/123264650-30953580-d535-11eb-9705-c4183e64befc.png)


- ConfigMap 생성

```
kubectl create configmap warnmsg --from-literal=msg=PleaseCareBook
```

![image](https://user-images.githubusercontent.com/82795757/123270360-d13a2400-d53a-11eb-9477-6a1f431aa16e.png)


- Deployment.yml 에 ConfigMap 적용

![image](https://user-images.githubusercontent.com/82795757/123264907-73570d80-d535-11eb-9f12-a6153111d521.png)



## Secret 
- DBMS 연결에 필요한 username 및 password는 민감한 정보이므로 application.yml에서 Secret 처리하였다.

![image](https://user-images.githubusercontent.com/82795757/123265302-de084900-d535-11eb-9704-cad58ac46e81.png)

- deployment.yml에서 env로 설정하였다.

![image](https://user-images.githubusercontent.com/82795757/123265438-06904300-d536-11eb-824d-230134059b55.png)

- 쿠버네티스에서는 다음과 같이 Secret object를 생성하였다.(secret.yml)

![image](https://user-images.githubusercontent.com/82795757/123265633-32abc400-d536-11eb-8f4a-cbe15c609970.png)


## Circuit Breaker 구현

* Spring FeignClient + Hystrix를 사용하여 구현함

시나리오는 도서대여신청(BookRentalRequest)-->도서(Book) 확인 시 도서 수량 확인 및 수량 반영 요청에 대한 응답이 3초를 넘어설 경우 Circuit Breaker 를 통하여 장애격리.

- Hystrix 를 설정:  FeignClient 요청처리에서 처리시간이 3초가 넘어서면 Circuit Breaker가 동작하도록 (요청을 빠르게 실패처리, 차단) 설정
                    시연을 위해 1번만 timeout이 발생해도 Circuit Breaker가 발생하도록 설정함.
```
# application.yml
```
![image](https://user-images.githubusercontent.com/20077391/120970089-ed516d80-c7a5-11eb-8abb-d57cdbf77065.png)


- 호출 서비스(도서대여요청:BookRentalRequest)에서는 도서수량확인용 API 호출에서 문제 발생 시 타임아웃발생하도록 처리
```
# (BookRentalRequest) BookService.java 
```

![image](https://user-images.githubusercontent.com/82795757/123278996-6b519a80-d542-11eb-9e9d-a6ff61477f58.png)


- 피호출 서비스인 Book서비스(책수량확인:/books/checkBookQtyAndModifyQty())에서 테스트를 위해 bookId가 5인 대여요청 건에 대해 sleep 5초 줌(응답 지연이 3초 넘게)
```
# (Book) BookController.java 
```
![image](https://user-images.githubusercontent.com/82795757/123293604-19fbd800-d54f-11eb-84b9-532813ec72b5.png)



* 서킷 브레이커 동작 확인:

bookId가 6번 인 경우 정상적으로 주문 처리 완료
```
# http POST http://20.41.87.25:8080/bookRequests memberId=1 bookId=6 qty=10

```
![image](https://user-images.githubusercontent.com/82795757/123293815-431c6880-d54f-11eb-8496-80e36177d6fe.png)


bookId가 5번 인 경우 Circuit Breaker에 의한 timeout 발생 확인
```
# http POST http://20.41.87.25:8080/bookRequests memberId=3 bookId=5 qty=30
```
![image](https://user-images.githubusercontent.com/82795757/123293930-5a5b5600-d54f-11eb-9eba-68eb5830011c.png)


일정시간 뒤에는 다시 주문이 정상적으로 수행되는 것을 알 수 있다.

![image](https://user-images.githubusercontent.com/82795757/123294454-d48bda80-d54f-11eb-8c4b-8dc1b6c34f43.png)


- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 Thread 자원 등을 보호하고 있음을 보여줌.



### 오토스케일 아웃
도서대여요청(book 서비스가 몰릴 경우를 대비하여 자동화된 확장 기능을 적용하였다.

- 주문서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 테스트를 위해 CPU 사용량이 50프로를 넘어서면 replica 를 3개까지 늘려준다:
```
hpa.yml
```
![image](https://user-images.githubusercontent.com/20077391/120973949-8aaea080-c7aa-11eb-80ce-eccb3c8cbc0d.png)

- deployment.yml에 resource 관련 설정을 추가해 준다.
```
deployment.yml
```
![image](https://user-images.githubusercontent.com/20077391/121101100-25a08c80-c836-11eb-81f1-a7df0f0dcaeb.png)


- 100명이 60초 동안 주문을 넣어준다.
```
siege -c100 -t60S -r10 --content-type "application/json" 'http://52.141.32.129:8080/orders POST {"bookId":"1","customerId":"1","qty":"1"}
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy -l app=order -w
```

- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다.

![image](https://user-images.githubusercontent.com/20077391/120974885-9babe180-c7ab-11eb-9a84-07bfb408ed34.png)

- siege 의 로그를 보면 오토스케일 확장이 일어나며 주문을 100% 처리완료한 것을 알 수 있었다.
```
** SIEGE 4.0.4
** Preparing 100 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                   2904 hits
Availability:                 100.00 %        
Elapsed time:                  59.64 secs     
Data transferred:               0.90 MB       
Response time:                  2.02 secs     
Transaction rate:              48.69 trans/sec
Throughput:                     0.02 MB/sec   
Concurrency:                   98.52
Successful transactions:        2904
Failed transactions:               0
Longest transaction:           13.62
Shortest transaction:           0.11
```



## Zero-downtime deploy (Readiness Probe) 무정지 재배포

* Zero-downtime deploy를 위해 readiness Probe를 설정함

![image](https://user-images.githubusercontent.com/82795757/123316046-5a675000-d567-11eb-8467-f3090d5190d3.png)


* Zero-downtime deploy 확인을 위해 seige 로 1명이 지속적인 회원등록 작업을 수행함
```
siege -c1 -t180S -r100 --content-type "application/json" 'http://member:8080/members POST {"name": "member99"}'
```

먼저 member 이미지가 v1.0 임을 확인

![image](https://user-images.githubusercontent.com/82795757/123321403-e7ada300-d56d-11eb-89af-c43075594618.png)


새 버전으로 배포(이미지를 v2.0으로 변경)
```
kubectl set image deployment member member=jtkimacr.azurecr.io/member:v2.0
```

member 이미지가 v2.0으로 변경되었임을 확인

![image](https://user-images.githubusercontent.com/82795757/123320638-fcd60200-d56c-11eb-848a-4ac8c2be2220.png)


- seige 의 화면으로 넘어가서 Availability가 100% 인지 확인 (무정지 배포 성공)

![image](https://user-images.githubusercontent.com/82795757/123320868-47f01500-d56d-11eb-8e7d-ae3da9cfd49c.png)


# Self-healing (Liveness Probe)

- Self-healing 확인을 위한 Liveness Probe 옵션 변경 (Port 변경)

bookrental/member/kubernetes/deployment.yml

![image](https://user-images.githubusercontent.com/82795757/123299314-5aaa2000-d554-11eb-899e-4da61f70fd65.png)


- Member pod에 Liveness Probe 옵션 적용 및 작동 확인

![image](https://user-images.githubusercontent.com/82795757/123301819-05bbd900-d557-11eb-9854-b0c56fab1cf0.png)


- Liveness 확인 실패에 따른 retry발생 확인 (서비스 강제 종료 후 이미지 가져와서 Restart 등 진행 확인)

![image](https://user-images.githubusercontent.com/82795757/123301373-8c23eb00-d556-11eb-97f0-292535c0112a.png)


이상으로 12가지 체크포인트가 구현 및 검증 완료되었음 확인하였다.

# [END]
