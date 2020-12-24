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

  make[Console.Service].fromHas(Console.live)
  make[Sttp].fromResource(Sttp.make)

  make[HttpServer].fromHas(HttpServer.make _)
  make[Endpoints].fromValue(Endpoints.make)
  many[HttpRoutes[Task]]
    .addHas(AppMain.routes)

  make[DiscountSvc].from(DiscountSvc.make _)
  make[Discounts].fromHas(Discounts.make)
  make[CsvReader].fromValue(CsvReader.make)
  make[Calculate].fromHas(Calculate.make _)

  make[UIO[Unit]].from(provideHas(
    program.provide
  ))
}
