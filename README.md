# Fast-money-transfer

This is a Proof-of-Concept developed to learn and practice building a standard microservice using a
non-spring based stack. I have chosen VertX as the framework for this, since it is a lightweight asynchronous
framework for building microservices.

### Use-case

Building a Money Transfer API suite that enables an consumer application to transfer money from one account to another.
This API can be consumed by multiple consumers therefore concurrency is an important aspect.

In order to simplify the implementation, the concept of user principal is not considered and therefore account is considered
the primary domain of the application.

### How to Run

To launch your tests:
```
./gradlew clean test
```

To run your application:
```
./gradlew clean vertxRun
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



