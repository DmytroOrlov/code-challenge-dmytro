package eyeem.shopping

import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import zio.macros.accessible

@accessible
trait Endpoints {
  def total: Endpoint[String, FailureResp, TotalResp, Nothing]
}

case class FailureResp(error: String)

case class TotalResp(total: BigDecimal)

object Endpoints {
  val make =
    new Endpoints {
      val total =
        endpoint.post
          .in("total")
          .in(stringBody)
          .out(jsonBody[TotalResp])
          .errorOut(jsonBody[FailureResp])
    }
}
