package com.chinthakad.samples.fmt.api;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

  private AccountHandlerProvider accountHandlerProvider;

  private static final int port = 8080;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    this.accountHandlerProvider = new AccountHandlerProvider(vertx);
    ConfigRetriever retriever = ConfigRetriever.create(vertx);

    HttpServer httpServer = vertx.createHttpServer();
    retriever.getConfig(
      rar -> {
        if (rar.succeeded()) {
          int port = rar.result().getInteger("port");
          System.out.println("port:==" + port);
          httpServer.requestHandler(router())
            .listen(port, har -> {
              if (har.succeeded()) {
                log.info("HTTP server running on port " + port);
                log.info("=== VERTICLE DEPLOYED : {}", this.getClass().getSimpleName());
                startPromise.complete();
              } else {
                log.error("Could not start a HTTP server", har.cause());
                startPromise.fail(har.cause());
              }
            });
        } else {
          log.error("Unable to load configuration");
        }
      }
    );

  }

  private Router router() {
    Router router = Router.router(vertx);
    router.get("/accounts").handler(accountHandlerProvider.getAccounts());
    router.get("/accounts/transfers").handler(accountHandlerProvider.getTransfers());
    router.post("/accounts/transfers").handler(accountHandlerProvider.transferMoney());
    // TODO: Add sub-router.
    return router;
  }

}
