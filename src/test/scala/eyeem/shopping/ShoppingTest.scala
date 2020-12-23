package eyeem.shopping

import izumi.distage.testkit.scalatest.DistageBIOEnvSpecScalatest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, OptionValues}
import zio._

class ShoppingTest extends DistageBIOEnvSpecScalatest[ZIO] with OptionValues with EitherValues with TypeCheckedTripleEquals {
  "1" must {
    "2" in {
      for {
        _ <- IO {
          assert(false)
        }
      } yield ()
    }
  }
}
