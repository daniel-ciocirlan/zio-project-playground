package helpers

import clients.backend.{BackEndClient, BackendClientLive}
import com.raquo.laminar.api.L._
import com.raquo.airstream.eventbus.EventBus
import zio._

object ZJS {

  type AppEnv = Any with BackEndClient

  val appLayer: ZLayer[Any, Nothing, AppEnv] = ZLayer.make[AppEnv](
    BackendClientLive.jsProvided
  )

  val runtime: Runtime[Any] = Runtime.default

  implicit class ExtendedZIO[E <: Throwable, A >: Any](
      private val zio: ZIO[AppEnv, E, A]
  ) extends AnyVal {

    def runJs: CancelableFuture[A] = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.runToFuture(zio.provideLayer(appLayer))
      }
    }

    def emitTo(eventBus: => EventBus[Any]): Unit = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe
          .fork(
            zio.tap(v => ZIO.attempt(eventBus.emit(v))).provideLayer(appLayer)
          )
      }
    }
  }

  implicit class ExtendedAnyEventStream(
      private val eventStream: EventStream[Any]
  ) extends AnyVal {

    def keep[A]: EventStream[A] =
      eventStream
        .filter(_.isInstanceOf[A])
        .map(_.asInstanceOf[A])

  }

  val client: ZIO.ServiceWithZIOPartiallyApplied[AppEnv] =
    ZIO.serviceWithZIO[BackEndClient]

  val backendBus: EventBus[Any]                            = new EventBus
  val zioBackendBus: EventBus[ZIO[AppEnv, Throwable, Any]] = new EventBus

  val backendStream: EventStream[Any] = EventStream
    .merge(
      backendBus.events,
      zioBackendBus.events.flatMap(z => EventStream.fromFuture(z.runJs))
    )

}
