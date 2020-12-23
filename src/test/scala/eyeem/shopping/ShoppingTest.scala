package eyeem.shopping

import izumi.distage.testkit.scalatest.DistageBIOEnvSpecScalatest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, OptionValues}
import zio._

class ShoppingTest extends DistageBIOEnvSpecScalatest[ZIO] with OptionValues with EitherValues with TypeCheckedTripleEquals {
  "" must {
    "" in {
      for {
        _ <- IO.unit
        _ <- IO {
          assert(false)
        }
      } yield ()
    }
  }
}
