package extensions

object Conversions {

  implicit class ConversionExtension[A](private val a: A) extends AnyVal {
    def to[B](implicit op: A => B): B = op(a)
  }

  implicit class OptConversionExtension[A](private val a: Option[A])
      extends AnyVal {
    def to[B](implicit op: A => B): Option[B] = a.map(op)
  }

}
