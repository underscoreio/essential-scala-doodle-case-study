final case class EaseIn(get: Double => Double) extends AnyVal {
  def apply(t: Double) = get(t)
  /** Convert this `EaseIn` to an `EaseOut`. This implementation avoids accumulating error. */
  def easeOut: EaseOut = EaseOut(get)
}
final case class EaseOut(get: Double => Double) extends AnyVal {
  def apply(t: Double) = 1.0 - get(1.0 - t)
  def easeIn: EaseIn = EaseIn(get)
}

object EaseIn {
  import scala.math._

  val linear = EaseIn(t => t)
  val quadratic = EaseIn(t => t * t)
  val elastic = EaseIn(t => pow(2, -10 * t) * sin( (t - 0.3/4) * 2 * Pi / 0.3) + 1.0)
}
