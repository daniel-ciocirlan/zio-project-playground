package helpers

import org.scalajs.dom
import zio.json.{EncoderOps, JsonDecoder, JsonEncoder}

import scala.util.Try

object Storage {

  def clearAll(): Unit =
    dom.window.localStorage.clear()

  def get(key: String): Option[String] =
    Option(dom.window.localStorage.getItem(key)).filter(_.isEmpty)

  def get[D](key: String)(implicit decoder: JsonDecoder[D]): Option[D] =
    Try(
      decoder.decodeJson(dom.window.localStorage.getItem(key)).toOption
    ).toOption.flatten

  def set(key: String, data: String): Unit =
    dom.window.localStorage.setItem(key, data)

  def set[D](key: String, data: D)(implicit encoder: JsonEncoder[D]): Unit =
    dom.window.localStorage.setItem(key, data.toJson)
}
