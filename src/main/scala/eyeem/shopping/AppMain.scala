package eyeem.shopping

import cats.syntax.option._
import distage._
import izumi.distage.plugins.PluginConfig
import izumi.distage.plugins.load.PluginLoader
import zio.Schedule.{elapsed, exponential}
import zio._
import zio.console._
import zio.duration._

import java.net.URI
import scala.concurrent.duration.{Duration => SDuration}
import scala.math.BigDecimal.RoundingMode.UP

object AppMain extends App {
  val program = for {
    cfg <- ZIO.service[AppCfg]
    lineitems <- CsvReader.readLineitems
    dsNames = lineitems.flatMap(_.discountCode).distinct
    discounts <- ZIO.collectParN(cfg.parallelism)(dsNames.toList)(
      Discounts.discount(_)
        .retry((exponential(1.millisecond) >>> elapsed).whileOutput(_ < 20.seconds))
        .bimap(_.some, _.toRight(none))
        .absolve
    )
    discountMap = discounts.map(d => d.name -> d.discount).toMap.withDefaultValue(0)
    res = lineitems.foldLeft(BigDecimal(0))((acc, li) => acc + li.discountCode.fold(li.price) { d =>
      (li.price * (1 - discountMap(d) * BigDecimal(0.01)))
        .setScale(2, UP)
    })
    _ <- putStrLn(s"$res")
  } yield ()

  def run(args: List[String]) = {
    val pluginConfig = PluginConfig.cached(
      packagesEnabled = Seq(
        "eyeem.shopping",
      )
    )
    val appModules = PluginLoader().load(pluginConfig)

    val app = Injector()
      .produceGetF[Task, Task[Unit]](appModules.merge)
      .useEffect

    app.exitCode
  }
}

case class AppCfg(
    url: URI,
    readTimeout: SDuration,
    parallelism: Int,
)
