package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import cats.syntax.option._
import eyeem.shopping.DiscountErr.throwable
import sttp.client.{NothingT, SttpBackend}
import sttp.model.StatusCode.NotFound
import zio._
import zio.macros.accessible
import zio.random._

@accessible
trait Discounts {
  def discount(name: String): IO[Capture[DiscountErr], Option[Discount]]
}

case class Discount(name: String, discount: Int)

object Discounts {
  val make = {
    for {
      implicit0(sb: SttpBackend[Task, Nothing, NothingT]) <- Sttp.backend
      env <- ZIO.environment[Has[DiscountSvc]]
    } yield new Discounts {
      def discount(name: String) =
        (for {
          request <- DiscountSvc.request(name)
          resp <- request.send().mapError(throwable("DiscountSvc.send"))
          disc <-
            if (resp.code == NotFound) IO.succeed(none)
            else IO.fromEither(resp.body).bimap(
              throwable("DiscountSvc.body"),
              _.some
            )
        } yield disc) provide env
    }
  }

  def dummy(storage: Ref[Map[String, Int]], failRate: Double = 0.5) =
    for {
      env <- ZIO.environment[Random]
    } yield new Discounts {
      def discount(name: String) =
        (for {
          fail <- nextDouble
          _ <- IO.fail(throwable(s"failRate $failRate")(new RuntimeException))
            .when(fail < failRate)
          ds <- storage.get
        } yield ds.get(name).map(Discount(name, _)))
          .provide(env)
    }
}

trait DiscountErr[+A] {
  def throwable(message: String)(e: Throwable): A
}

object DiscountErr extends Constructors[DiscountErr] {
  def throwable(message: String)(e: Throwable) =
    Capture[DiscountErr](_.throwable(message)(e))

  trait AsThrowable extends DiscountErr[Throwable] {
    def throwable(message: String)(e: Throwable) = new RuntimeException(s"$message: ${e.getMessage}")
  }

  trait AsFailureResp extends DiscountErr[FailureResp] {
    def throwable(message: String)(e: Throwable) = FailureResp(s"$message: ${e.getMessage}")
  }

}
