package eyeem.shopping

case class Lineitem(
    photoId: Int,
    price: BigDecimal,
    discountCode: Option[String],
)