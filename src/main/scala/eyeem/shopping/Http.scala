package eyeem.shopping

import io.circe
import io.circe.generic.auto._
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe._
import zio._
import zio.macros.accessible

import scala.concurrent.duration._

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
          .readTimeout(5.seconds)
      }
    }
}

@accessible
trait Sttp {
  def backend: UIO[SttpBackend[Task, Nothing, NothingT]]
}

object Sttp {
  val make = for {
    impl <- AsyncHttpClientZioBackend.managed()
  } yield new Sttp {
    val backend = IO.succeed(impl)
  }
}
