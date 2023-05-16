package de.gransoftware.gzip

import de.gransoftware.gzip.MainVerticle.User.Address
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.openapi.RouterBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

class MainVerticle : CoroutineVerticle() {

  companion object {
    val nonExistentUserId = "00000000-0000-0000-0000-000000000000"
    val user = User(
      1,
      "John John John John Doe Doe Doe",
      "John.Doe@doe.com",
      Address("Main Street", "New York", "12345")
    )
    val json = Json {
      serializersModule = SerializersModule {
        contextual(User.serializer())
      }
    }
  }


  @Serializable
  data class User(val id: Int, val name: String, val email: String, val address: Address) {
    @Serializable
    data class Address(val street: String, val city: String, val zip: String)
  }

  override suspend fun start() {

    val routerBuilder = RouterBuilder.create(vertx, "openapi.yaml").await().apply {
      operation("getUser").coHandler { rc ->
        if ((rc.pathParam("id") == nonExistentUserId))
          rc.response().setStatusCode(404).end()
        else
          rc.end(json.encodeToString(user))
      }
    }
    val isGzipEnabled = System.getenv("GZIP_ENABLED")?.toBoolean() ?: false
    vertx
      .createHttpServer(HttpServerOptions().setCompressionSupported(isGzipEnabled))
      .requestHandler(routerBuilder.createRouter())
      .listen(8888)
      .await()
  }
}
