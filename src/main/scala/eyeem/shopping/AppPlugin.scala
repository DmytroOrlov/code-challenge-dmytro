package eyeem.shopping

import com.typesafe.config.ConfigFactory
import distage.plugins.PluginDef
import distage.{Tag, _}
import eyeem.shopping.AppMain.program
import izumi.distage.config.{AppConfigModule, ConfigModuleDef}
import izumi.distage.effect.modules.ZIODIEffectModule
import izumi.distage.model.definition.StandardAxis.Repo
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
  make[Discounts].tagged(Repo.Prod).fromHas(Discounts.make)
  make[Discounts].tagged(Repo.Dummy).fromHas(
    Ref.make(Map("ACTION_33" -> 10, "CRAZY_54" -> 60, "BF_11" -> 10, "SMART_XTREME" -> 10, "BF_12" -> 25, "XMAS_22" -> 11, "EARLY_SPECIAL" -> 33, "SPRING_220" -> 50))
      .flatMap(s => Discounts.dummy(s))
  )
  make[CsvReader].fromValue(CsvReader.make)
  make[Calculate].fromHas(Calculate.make _)

  make[UIO[Unit]].from(provideHas(
    program.provide
  ))
}
