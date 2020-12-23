package eyeem.shopping

import cats.syntax.option._
import distage._
import izumi.distage.plugins.PluginConfig
import izumi.distage.plugins.load.PluginLoader
import zio.Schedule.{elapsed, exponential}
import zio._
import zio.duration._

import java.net.URI

object AppMain extends App {
  val program = for {
    lineitems <- CsvReader.readLineitems
    _ <- UIO(lineitems.foreach(println(_)))
    dsNames = lineitems.foldLeft(Set.empty[String])((acc, li) => li.discountCode.fold(acc)(acc + _))
    _ <- UIO(dsNames.foreach(println(_)))
    discounts <- ZIO.collectParN(4)(dsNames.toList)(
      Discounts.discount(_)
        .retry((exponential(1.millisecond) >>> elapsed).whileOutput(_ < 20.seconds))
        .bimap(_.some, _.toRight(none))
        .absolve
    )
    discountMap = discounts.map(d => d.name -> d.discount).toMap.withDefaultValue(0.0)
    _ = discountMap.foreach(println(_))
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
    readTimeout: scala.concurrent.duration.Duration,
)
