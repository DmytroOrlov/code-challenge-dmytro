package eyeem.shopping

import buildinfo.BuildInfo.version
import cats.syntax.semigroupk._
import distage._
import izumi.distage.plugins.PluginConfig
import izumi.distage.plugins.load.PluginLoader
import org.http4s.HttpRoutes
import org.http4s.server.Router
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.server.http4s._
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio._
import zio.interop.catz._

import java.net.URI
import scala.concurrent.duration.{Duration => SDuration}
import scala.io.Source

object AppMain extends App {
  val routes = for {
    implicit0(rts: Runtime[Any]) <- ZIO.runtime[Any]
    env <- ZIO.environment[Has[Calculate]]
    total <- Endpoints.total
    docs = Seq(total).toOpenAPI("Shopping total calculator", version)
    router = Router[Task](
      "/" -> ((total.toRoutes { req =>
        Calculate.total(Source.fromBytes(req))
          .bimap(
            _ continue new CsvErr.AsFailureResp with DiscountErr.AsFailureResp {},
            TotalResp.apply)
          .either
          .provide(env)
      }: HttpRoutes[Task]) <+>
        new SwaggerHttp4s(docs.toYaml).routes)
    )
  } yield router

  val program = HttpServer.bindHttp *> IO.never

  def run(args: List[String]) = {
    val pluginConfig = PluginConfig.cached(
      packagesEnabled = Seq(
        "eyeem.shopping",
      )
    )
    val appModules = PluginLoader().load(pluginConfig)

    val app = Injector()
      .produceGetF[Task, UIO[Unit]](appModules.merge)
      .useEffect

    app.exitCode
  }
}

case class AppCfg(
    url: URI,
    readTimeout: SDuration,
    parallelism: Int,
)
