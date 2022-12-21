package helpers

import org.scalajs.dom
import upickle.default._

import scala.util.Try

object Storage {

  def clearAll(): Unit =
    dom.window.localStorage.clear()

  def get(key: String): Option[String] =
    Option(dom.window.localStorage.getItem(key)).filter(_.isEmpty)

  def get[D](key: String)(implicit reader: Reader[D]): Option[D] =
    Try(read[D](dom.window.localStorage.getItem(key))).toOption

  def set(key: String, data: String): Unit                       =
    dom.window.localStorage.setItem(key, data)

  def set[D](key: String, data: D)(implicit writer: Writer[D]): Unit =
    dom.window.localStorage.setItem(key, write[D](data))
}
