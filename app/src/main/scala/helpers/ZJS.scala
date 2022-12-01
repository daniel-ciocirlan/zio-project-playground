package helpers

import clients.backend.{BackEndClient, BackEndClientLive}
import com.raquo.airstream.eventbus.EventBus
import zio._
object ZJS {

  type AppEnv = Any with BackEndClient

  val appLayer: ZLayer[Any, Nothing, AppEnv] = ZLayer.make[AppEnv](
    BackEndClientLive.jsProvided
  )

  val runtime: Runtime[Any] = Runtime.default

  implicit class ExtendedZIO[E <: Throwable, A](
      private val zio: ZIO[AppEnv, E, A]
  ) extends AnyVal {

    def runJs: CancelableFuture[A] = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.runToFuture(zio.provideLayer(appLayer))
      }
    }

    def emitTo(eventBus: => EventBus[A]): Unit = {
      Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe
          .fork(
            zio.tap(v => ZIO.attempt(eventBus.emit(v))).provideLayer(appLayer)
          )
      }
    }

  }

}
