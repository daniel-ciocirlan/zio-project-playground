package services.config

import domain.errors.ConfigurationException
import zio._
import zio.test._

object ConfigServiceSpec extends ZIOSpecDefault {

  // Make sure we can load a proper config, but also fail on improper mapping
  val testConfigPath: String = "rock.the.jvm.test-config"
  case class TestConfig(someInt: Int, someString: String)
  case class BadConfig(someFloat: Float, someString: String)

  // A config path that doesn't exist
  val doesNotExistPath: String = "rock.the.jvm.dne"

  val configServiceSpec: Spec[TestEnvironment with Scope, Any] =
    suite("ConfigService")(
      test("TestConfig")(
        for {
          config <- ZIO.service[TestConfig]
        } yield assertTrue(
          config.someInt == 123,
          config.someString.equals("abc")
        )
      ).provideLayer(
        ConfigService.makeConfig[TestConfig](testConfigPath)
      ),
      test("BadConfig")(
        for {
          error <-
            ZIO
              .service[BadConfig]
              .provideLayer(
                ConfigService.makeConfig[BadConfig](testConfigPath)
              ) // Provide this here, because our test will throw an exception when starting!
              .flip
        } yield assertTrue(error.isInstanceOf[ConfigurationException])
      ),
      test("DNE")(
        for {
          error <-
            ZIO
              .service[TestConfig]
              .provideLayer(
                ConfigService.makeConfig[TestConfig](doesNotExistPath)
              ) // Provide this here, because our test will throw an exception when starting!
              .flip
        } yield assertTrue(error.isInstanceOf[ConfigurationException])
      )
    )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ConfigServiceSpec")(
      configServiceSpec
    )
}
