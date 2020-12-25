package eyeem.shopping

import distage._
import eyeem.shopping.fixture.{DiscountDocker, DiscountDockerSvc}
import izumi.distage.model.definition.Activation
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.model.definition.StandardAxis.Repo.{Dummy, Prod}
import izumi.distage.testkit.TestConfig
import izumi.distage.testkit.scalatest.DistageBIOEnvSpecScalatest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, OptionValues}
import sttp.client._
import zio._

import java.net.URI
import scala.concurrent.duration._
import scala.io.Source

abstract class BlackboxTest extends DistageBIOEnvSpecScalatest[ZIO] with OptionValues with EitherValues with TypeCheckedTripleEquals {
  "Calculate logic" must {
    "apply discounts to provided lineitems.csv" in {
      for {
        csv <- IO(Source.fromResource("lineitems.csv"))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("1887.08"))
      } yield ()
    }
    "use price if no discount" in {
      for {
        csv <- IO(Source.fromString(empty + "1,0.42,"))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("0.42"))
      } yield ()
    }
    "ignore wrong discount code" in {
      for {
        csv <- IO(Source.fromString(empty + "19,7.69,GTY_34"))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("7.69"))
      } yield ()
    }
    "apply discount code" in {
      for {
        csv <- IO(Source.fromString(empty + "222,10.0,BF_11"))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("9.0"))
      } yield ()
    }
    "setScale(2, UP) for discount" in {
      for {
        csv <- IO(Source.fromString(empty + "333,0.99,SPRING_220"))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("0.5"))
      } yield ()
    }
    "calculate total" in {
      for {
        csv <- IO(Source.fromString(empty +
          s"""1,0.34,
             |19,7.66,GTY_34
             |222,10.0,BF_11
             |333,0.99,SPRING_220""".stripMargin))
        total <- Calculate.total(csv)
          .mapError(_ continue AsThrowable)
        _ = assert(total === BigDecimal("17.50"))
      } yield ()
    }
  }

  def empty = "photo_id,price,discount_code\n"
}

final class DummyTest extends BlackboxTest with DummyEnv

final class DockerTest extends BlackboxTest with ProdEnv {
  override def config: TestConfig = super.config.copy(
    moduleOverrides = new ModuleDef {
      make[AppCfg].fromEffect { service: DiscountDockerSvc =>
        for {
          url <- Task(uri"http://${service.es.hostV4}:${service.es.port}/api/discounts")
        } yield AppCfg(url.toJavaUri, 1.second, 4, 1.minute)
      }
    },
    memoizationRoots = Set(
      DIKey.get[DiscountDocker.Container],
    ),
  )
}

final class DiscountsFailTest extends DistageBIOEnvSpecScalatest[ZIO] with OptionValues with EitherValues with TypeCheckedTripleEquals {
  "Calculate logic" must {
    "propagate Discounts failure" in {
      for {
        csv <- IO(Source.fromResource("lineitems.csv"))
        fail <- Calculate.total(csv)
          .mapError(_ continue new CsvErr[String] with DiscountErr[String] {
            def exception(message: String)(e: Throwable) = ???

            def throwable(message: String)(e: Throwable) = s"propagate $message $e"
          })
          .either
        _ = assert(fail.left.value === "propagate failRate 1.0 java.lang.RuntimeException")
      } yield ()
    }
  }

  override def config: TestConfig = super.config.copy(
    moduleOverrides = new ModuleDef {
      make[Discounts].fromHas(
        Ref.make(Map.empty[String, Int])
          .flatMap(Discounts.dummy(_, failRate = 1))
      )
      make[AppCfg].fromValue(AppCfg(
        URI.create("http://localhost:9000/api/discounts"),
        500.millis,
        10,
        1.second,
      ))
    })
}

trait DummyEnv extends DistageBIOEnvSpecScalatest[ZIO] {
  override def config: TestConfig = super.config.copy(
    activation = Activation(Repo -> Dummy),
  )
}

trait ProdEnv extends DistageBIOEnvSpecScalatest[ZIO] {
  override def config: TestConfig = super.config.copy(
    activation = Activation(Repo -> Prod), // default
  )
}

object AsThrowable extends CsvErr.AsThrowable with DiscountErr.AsThrowable
