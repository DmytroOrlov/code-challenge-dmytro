package eyeem.shopping

import com.typesafe.config.ConfigFactory
import distage.plugins.PluginDef
import distage.{Tag, _}
import eyeem.shopping.AppMain.program
import izumi.distage.config.{AppConfigModule, ConfigModuleDef}
import izumi.distage.effect.modules.ZIODIEffectModule
import org.http4s.HttpRoutes
import zio._
import zio.console._

object AppPlugin extends PluginDef with ZIODIEffectModule with ConfigModuleDef {
  def provideHas[R: HasConstructor, A: Tag](fn: R => A): ProviderMagnet[A] =
    HasConstructor[R].map(fn)

  include(AppConfigModule(ConfigFactory.defaultApplication()))

  makeConfig[AppCfg]("app")
  make[String]
    .named("csv")
    .fromValue("lineitems.csv")

  make[Sttp].fromResource(Sttp.make)
  make[Console.Service].fromHas(Console.live)

  make[DiscountSvc].from(DiscountSvc.make _)
  make[HttpServer].fromHas(HttpServer.make _)
  many[HttpRoutes[Task]]
    // .addHas(Main.logicRoutes)

  make[CsvReader].fromValue(CsvReader.make)
  make[Discounts].fromHas(Discounts.make)

  make[Task[Unit]].from(provideHas(
    program.provide
  ))
}
