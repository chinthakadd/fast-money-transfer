# Fast-money-transfer

This is a Proof-of-Concept developed to learn and practice building a standard microservice using a
non-spring based stack. I have chosen VertX as the framework for this, since it is a lightweight asynchronous
framework for building microservices.

## Use-case

Building a Money Transfer API suite that enables an consumer application to transfer money from one account to another.
This API can be consumed by multiple consumers therefore concurrency is an important aspect.

In order to simplify the implementation, the concept of user principal is not considered and therefore account is considered
the primary domain of the application.

## Implementation

### How to Run

To launch your tests:
```
./gradlew clean test
```

`MoneyTransferE2eTest` is a test case written to show-case how the application APIs are behaving from E2E perspective.

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

GET `/accounts`

```sh
http GET http://localhost:8080/accounts
```

RESPONSE:

```
HTTP/1.1 200 OK
content-length: 534

[ {
  "accountNumber" : "1234",
  "name" : "test account 1",
  "availableBalance" : "2959.88",
  "currentBalance" : "2959.88",
  "lastUpdatedOn" : "2020-03-01T21:39:52.735055Z"
}, {
  "accountNumber" : "5678",
  "name" : "test account 2",
  "availableBalance" : "1292.12",
  "currentBalance" : "1292.12",
  "lastUpdatedOn" : "2020-03-01T21:39:52.738826Z"
}, {
  "accountNumber" : "6789",
  "name" : "test account 4",
  "availableBalance" : "100.00",
  "currentBalance" : "100.00",
  "lastUpdatedOn" : "2020-03-01T21:37:32.012005Z"
} ]
```

POST `/accounts/transfers`

```sh
http -v POST http://localhost:8080/accounts/transfers fromAccountNumber=1234 toAccountNumber=5678 amount=40.12
```

RESPONSE:
```
POST /accounts/transfers HTTP/1.1
Accept: application/json, */*
Accept-Encoding: gzip, deflate
Connection: keep-alive
Content-Length: 75
Content-Type: application/json
Host: localhost:8080
User-Agent: HTTPie/2.0.0

{
    "amount": "40.12",
    "fromAccountNumber": "1234",
    "toAccountNumber": "5678"
}

HTTP/1.1 202 Accepted
content-length: 0
```

POST `/accounts/transfers`

```sh
http GET http://localhost:8080/accounts/transfers
```

RESPONSE:

```
HTTP/1.1 200 OK
content-length: 179

[ {
  "id" : 1,
  "fromAccountNumber" : "1234",
  "toAccountNumber" : "5678",
  "amount" : "40.12",
  "status" : "COMPLETED",
  "lastUpdatedOn" : "2020-03-01T21:39:52.745528Z"
} ]

```

### Wish List

The following were some of the TODOs in my wish list that I was unable to complete.

#### Business

- Implement a reconciliation mechanism for transfer. Ex: If the transaction fails since the account balance has changed
during the transfer operation (due to concurrency), we can expose an API or automated reconciliation loop that
attempts to retry the transfer operation.

NOTE: Currently, if the money transfer orchestration fails at an intermediate state, account balances will not
reflect a consistent state. In order to make it consistent, aforementioned reconciliation mechanism needs to be implemented.

#### Technical

- Introduce a lightweight depedency injection framework such as Google Guice.
- Implement Reactive Programming into Vert.x using RxJava.
- Design and implement a standard message format between Verticles
- Adopt a light weight Java ORM Framework such ActiveJDBC (JavaLite)
- Further fine tune the architecture based on hexagonal design
- More Java Documentation

### References

* A gentle guide to asynchronous programming with Eclipse Vert.x for Java developers - E-Book
* https://vertx.io/docs/[Vert.x Documentation]
* https://vertx.io/blog/presentation-of-the-vert-x-swagger-project/[Vert.x Swagger Support]
* https://vertx.io/docs/vertx-web/java/ [Vert.x Web]
* https://vertx.io/docs/vertx-jdbc-client/java/ [Vert.x JDBC Client]
* https://stackoverflow.com/questions/8691684/how-to-code-optimistic-and-pessimistic-locking-from-java-code [Simple way to do my own optimistic locks]



