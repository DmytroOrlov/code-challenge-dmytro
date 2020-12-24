package eyeem.shopping

import distage._
import eyeem.shopping.fixture.{DiscountDocker, DiscountDockerSvc}
import izumi.distage.testkit.TestConfig
import izumi.distage.testkit.scalatest.DistageBIOEnvSpecScalatest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, OptionValues}
import sttp.client._
import zio._

import scala.concurrent.duration._
import scala.io.Source

abstract class BlackboxTest extends DistageBIOEnvSpecScalatest[ZIO] with OptionValues with EitherValues with TypeCheckedTripleEquals {
  "Calculate logic" must {
    "apply discounts in provided lineitems.csv" in {
      for {
        csv <- IO(Source.fromResource("lineitems.csv"))
        total <- Calculate.total(csv).mapError(_ continue AsThrowable)
        _ <- IO {
          assert(total === BigDecimal("1887.08"))
        }
      } yield ()
    }
  }
}

final class DockerTest extends BlackboxTest {
  override def config: TestConfig = super.config.copy(
    moduleOverrides = new ModuleDef {
      make[AppCfg].fromEffect { service: DiscountDockerSvc =>
        for {
          url <- Task(uri"http://${service.es.hostV4}:${service.es.port}/api/discounts")
        } yield AppCfg(url.toJavaUri, 1.second, 4)
      }
    },
    memoizationRoots = Set(
      DIKey.get[DiscountDocker.Container],
    ),
  )
}

object AsThrowable extends CsvErr.AsThrowable with DiscountErr.AsThrowable
