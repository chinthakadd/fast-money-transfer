# Fast-money-transfer

This is a Proof-of-Concept developed to learn and understand building a standard microservice using a
non-spring based stack. I have chosen VertX as the framework for this, since it is a lightweight framework...
more on this later..

== Use-case

Building a Money Transfer API suite that enables an consumer application to transfer money from one account to another.

=== Assumptions

Following assumptions are made to simplify the implementation.

-
-
-

== TODOs

- implement transactions
- Bring guice into the picture
- Write junit test case
    - Write a test case for database verticle - DONE
    - write a test case for server verticle - DONE
    -  Unit test cases
- Read:
    http://qrman.github.io/posts/2015/07/10/vertx3-guice
- Introduce Reactiveness
- Logging - Customization - Done
- Come up with a Standard Communication Payload Model.

== Help

* https://vertx.io/docs/[Vert.x Documentation]
* https://vertx.io/blog/presentation-of-the-vert-x-swagger-project/[Vert.x Swagger Support]
* https://vertx.io/docs/vertx-web/java/ [Vert.x Web]
* https://vertx.io/docs/vertx-jdbc-client/java/ [Vert.x JDBC Client]
* https://stackoverflow.com/questions/8691684/how-to-code-optimistic-and-pessimistic-locking-from-java-code [Simple way to do my own optimistic locks]
More Reading:

https://vertx.io/blog/some-rest-with-vert-x/
https://www.javacodegeeks.com/2018/03/vertx-programming-style-your-reactive-web-companion-rest-api-explained.html


== How to Run

To launch your tests:
```
./gradlew clean test
```

To package your application:
```
./gradlew clean assemble
```

To run your application:
```
./gradlew clean run
```
