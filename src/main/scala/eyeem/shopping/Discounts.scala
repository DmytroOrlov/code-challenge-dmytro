package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import cats.syntax.option._
import eyeem.shopping.DiscountErr.throwable
import sttp.client.{NothingT, SttpBackend}
import sttp.model.StatusCode.NotFound
import zio._
import zio.macros.accessible

case class Discount(name: String, value: Double)

@accessible
trait Discounts {
  def discount(name: String): IO[Capture[DiscountErr], Option[Discount]]
}

object Discounts {
  val make = {
    for {
      implicit0(sb: SttpBackend[Task, Nothing, NothingT]) <- ZIO.service[SttpBackend[Task, Nothing, NothingT]]
      env <- ZIO.environment[Has[DiscountSvc]]
    } yield new Discounts {
      def discount(name: String) =
        (for {
          request <- DiscountSvc.request(name)
          resp <- request.send().mapError(throwable("DiscountSvc.send"))
          disc <-
            if (resp.code == NotFound) IO.succeed(none)
            else IO.fromEither(resp.body).bimap(throwable("DiscountSvc.body"), _.some)
        } yield disc) provide env
    }
  }

  val dummy = new Discounts {
    def discount(name: String) =
      IO.succeed(none)
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
