package eyeem.shopping

import io.circe._
import io.circe.generic.auto._
import sttp.client._
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client.circe._
import zio._
import zio.macros.accessible

@accessible
trait DiscountSvc {
  def request(name: String): UIO[RequestT[Identity, Either[ResponseError[Error], Discount], Nothing]]
}

object DiscountSvc {
  def make(cfg: AppCfg) =
    new DiscountSvc {
      def request(name: String) = IO.succeed {
        basicRequest
          .get(uri"${cfg.url}/$name")
          .response(asJson[Discount])
          .readTimeout(cfg.readTimeout)
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
