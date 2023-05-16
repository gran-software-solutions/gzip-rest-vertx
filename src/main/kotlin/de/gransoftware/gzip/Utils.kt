package de.gransoftware.gzip

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.openapi.Operation
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Operation.coHandler(fn: suspend (RoutingContext) -> Unit): Operation {
  return handler { ctx ->
    CoroutineScope(Dispatchers.IO).launch(ctx.vertx().dispatcher()) {
      try {
        fn(ctx)
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
}
