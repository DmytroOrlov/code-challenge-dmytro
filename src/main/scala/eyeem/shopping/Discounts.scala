package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import io.circe
import io.circe.generic.auto._
import sttp.client._
import sttp.client.circe._
import zio._
import zio.macros.accessible

case class Discount(name: String, value: Double)

@accessible
trait DiscountSvc {
  def request(name: String): UIO[RequestT[Identity, Either[ResponseError[circe.Error], Discount], Nothing]]
}

object DiscountSvc {
  def make(cfg: AppCfg) =
    new DiscountSvc {
      def request(name: String) = IO.succeed {
        basicRequest
          .get(uri"$cfg/$name")
          .response(asJson[Discount])
      }
    }
}


@accessible
trait Discounts {
  def discount(name: String): IO[Option[Capture[DiscountErr]], (String, Double)]
}

object Discounts {
  val make = {
    for {
      _ <- zio.IO.unit
    } yield new Discounts {
      def discount(name: String) = {
        ???
      }
    }
  }

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
