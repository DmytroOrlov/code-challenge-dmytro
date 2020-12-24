package eyeem.shopping

import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.json.circe._
import zio.macros.accessible

@accessible
trait Endpoints {
  def total: Endpoint[Array[Byte], FailureResp, TotalResp, Nothing]
}

case class FailureResp(error: String)

case class TotalResp(total: BigDecimal)

object Endpoints {
  val make = new Endpoints {
    val total =
      endpoint.post
        .description("Apply discount codes, calculate total amount")
        .in("total")
        .in(byteArrayBody.description("csv: photo_id,price,discount_code"))
        .out(jsonBody[TotalResp]
          .example(TotalResp(BigDecimal("1.01")))
          .description("total amount with discounts applied"))
        .errorOut(jsonBody[FailureResp]
          .example(FailureResp("DiscountSvc.body: statusCode: 500, response: {\"message\":\"Internal Server Error\",\"exception\":\"Oops\"}"))
          .description("error message")
        )
  }
}
