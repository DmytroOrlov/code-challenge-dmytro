package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import zio._
import zio.macros.accessible

case class Discount(name: String, value: Double)

@accessible
trait Discounts {
  def discount(name: String): IO[Option[Capture[DiscountErr]], (String, Double)]
}

object Discounts {
  val dummy = new Discounts {
    def discount(name: String) =
      IO.fail(None)
  }
}

trait DiscountErr[+A] {
  def throwable(message: String)(e: Throwable): A

  def message(message: String): A
}

object DiscountErr extends Constructors[DiscountErr] {
  def throwable(message: String)(e: Throwable) =
    Capture[DiscountErr](_.throwable(message)(e))

  def message(message: String) =
    Capture[DiscountErr](_.message(message))

  trait AsThrowable extends DiscountErr[Throwable] {
    def throwable(message: String)(e: Throwable) = new RuntimeException(s"$message: ${e.getMessage}")

    def message(message: String) = new RuntimeException(message)
  }

}
