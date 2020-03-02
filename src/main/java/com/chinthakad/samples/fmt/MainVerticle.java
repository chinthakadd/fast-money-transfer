package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.api.HttpServerVerticle;
import com.chinthakad.samples.fmt.client.JdbcClientVerticle;
import com.chinthakad.samples.fmt.core.service.AccountVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Main verticle that performs deployment of all application verticles in a pre-defined order.
 *
 * @author Chinthaka D
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    Promise<String> initHttpServer = Promise.promise();
    // TODO: new DeploymentOptions().setInstances(2)
    vertx.deployVerticle(new HttpServerVerticle(), initHttpServer);
    initHttpServer.future()
      .compose(httpServerVerticleId -> {
        Promise<String> dbClientVerticleDeployment = Promise.promise();
        vertx.deployVerticle(new JdbcClientVerticle(), dbClientVerticleDeployment);
        return dbClientVerticleDeployment.future();
      })
      .compose(httpServerVerticleId -> {
        Promise<String> accountServiceDeployment = Promise.promise();
        vertx.deployVerticle(new AccountVerticle(), accountServiceDeployment);
        return accountServiceDeployment.future();
      }).setHandler(
      ar -> {
        if (ar.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(ar.cause());
        }
      }
    );
  }

}
