package eyeem.shopping

import distage.{Tag, _}
import zio._
import zio.console._

object AppMain extends App {
  def run(args: List[String]) = {
    val program = for {
      res <- CsvReader.readLineitems
      _ = res.foreach(println(_))
    } yield ()

    def provideHas[R: HasConstructor, A: Tag](fn: R => A): ProviderMagnet[A] =
      HasConstructor[R].map(fn)

    val definition = new ModuleDef {
      make[String]
        .named("csv")
        .fromValue("lineitems.csv")

      make[CsvReader].fromResource(CsvReader.make _)
      make[Console.Service].fromHas(Console.live)
      make[UIO[Unit]].from(provideHas(program.provide))
    }

    val app = Injector()
      .produceGetF[Task, UIO[Unit]](definition)
      .useEffect

    app.exitCode
  }
}
