package de.gransoftware.gzip

import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  private lateinit var webClient: WebClient

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle(), testContext.succeeding { _ ->
      webClient = WebClient.create(vertx)
      testContext.completeNow()
    })
  }

  @Test
  fun `getUser returns 404 for no user found`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val httpResponse = webClient.get(8888, "localhost", "/users/${MainVerticle.nonExistentUserId}")
      .send()
      .await()
    assert(httpResponse.statusCode() == 404)
  }

  @Test
  fun `getUser should return a user`(vertx: Vertx): Unit = runBlocking(vertx.dispatcher()) {
    val httpResponse = webClient.get(8888, "localhost", "/users/${UUID.randomUUID()}")
      .send()
      .await()
    assert(httpResponse.statusCode() == 200)
    MainVerticle.json.decodeFromString(
      MainVerticle.User.serializer(),
      httpResponse.bodyAsString()
    ) shouldBe MainVerticle.user
  }
}
