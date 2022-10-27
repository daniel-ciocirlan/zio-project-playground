package services.config

import com.typesafe.config.ConfigFactory
import domain.errors.ConfigurationException
import zio._
import zio.config.magnolia.{descriptor, Descriptor}
import zio.config.toKebabCase
import zio.config.typesafe.TypesafeConfig

object ConfigService {

  /** This method will generate a layer containing a config case class of type C
    * auto-mapped to the corresponding reference.conf/application.conf section.
    *
    * If your configuration model has some external types (e.g. a Uri), you will
    * likely need to have an implicit descriptor for those types in scopes when
    * calling this.
    * @param path
    * @param config
    * @param tag
    * @tparam C
    *   The case class of your configuration model
    * @return
    */
  def makeConfig[C](
      path: String
  )(implicit
      config: Descriptor[C],
      tag: Tag[C]
  ): Layer[ConfigurationException, C] = {
    implicit lazy val configDescriptor: _root_.zio.config.ConfigDescriptor[C] =
      descriptor[C].mapKey(toKebabCase)
    TypesafeConfig
      .fromTypesafeConfig(
        ZIO
          .attempt(ConfigFactory.load().getConfig(path)),
        configDescriptor
      )
  }.mapError(e =>
    ConfigurationException(s"Could not load config for path $path", e)
  )

}
