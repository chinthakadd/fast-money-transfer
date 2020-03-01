# Fast-money-transfer

This is a Proof-of-Concept developed to learn and practice building a standard microservice using a
non-spring based stack. I have chosen VertX as the framework for this, since it is a lightweight asynchronous
framework for building microservices.

### Use-case

Building a Money Transfer API suite that enables an consumer application to transfer money from one account to another.
This API can be consumed by multiple consumers therefore concurrency is an important aspect.

In order to simplify the implementation, the concept of user principal is not considered and therefore account is considered
the primary domain of the application.

### Implementation

### How to Run

To launch your tests:
```
./gradlew clean test
```

To run your application:
```
./gradlew clean vertxRun
```

### APIs

This application has exposed 3 APIs.

- GET `/accounts` : Returns all accounts configured in the application
- GET `/accounts/transfers`: Returns all transfers related to all accounts
- POST `/accounts/transfer`: Initiate a account transfer

I have used `HTTPie` for testing the APIs from the command line.

The following are some of the sample HTTPie commands to test the application.

```sh
http GET http://localhost:8080/accounts
```

RESPONSE:

```json
[ {
  "accountNumber" : "1234",
  "name" : "test account 1",
  "availableBalance" : "3000.00",
  "currentBalance" : "3000.00",
  "lastUpdatedOn" : "2020-03-01T21:37:31.992438Z"
}, {
  "accountNumber" : "5678",
  "name" : "test account 2",
  "availableBalance" : "1252.00",
  "currentBalance" : "1252.00",
  "lastUpdatedOn" : "2020-03-01T21:37:32.011259Z"
}, {
  "accountNumber" : "6789",
  "name" : "test account 4",
  "availableBalance" : "100.00",
  "currentBalance" : "100.00",
  "lastUpdatedOn" : "2020-03-01T21:37:32.012005Z"
} ]

```

```sh
http GET http://localhost:8080/accounts/transfers
```


### Wish List

The following were some of the TODOs in my wish list that I was unable to complete.

- Introduce a lightweight depedency injection framework such as Google Guice.
- Implement Reactive Programming into Vert.x using RxJava.
- Design and implement a standard message format between Verticles
- Adopt a light weight Java ORM Framework such ActiveJDBC (JavaLite)
- Further fine tune the architecture based on hexagonal design

### References

* A gentle guide to asynchronous programming with Eclipse Vert.x for Java developers - E-Book
* https://vertx.io/docs/[Vert.x Documentation]
* https://vertx.io/blog/presentation-of-the-vert-x-swagger-project/[Vert.x Swagger Support]
* https://vertx.io/docs/vertx-web/java/ [Vert.x Web]
* https://vertx.io/docs/vertx-jdbc-client/java/ [Vert.x JDBC Client]
* https://stackoverflow.com/questions/8691684/how-to-code-optimistic-and-pessimistic-locking-from-java-code [Simple way to do my own optimistic locks]



