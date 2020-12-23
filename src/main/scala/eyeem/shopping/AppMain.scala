package eyeem.shopping

import distage.{Tag, _}
import zio._
import zio.console._

import java.net.URI

object AppMain extends App {
  def run(args: List[String]) = {
    val program = for {
      res <- CsvReader.readLineitems
      dsNames = res.foldLeft(Set.empty[String])((acc, li) => li.discountCode.fold(acc)(acc + _))
      ds <- ZIO.collectParN(4)(dsNames.toList)(Discounts.discount)
      discountMap = ds.map(d => d.name -> d.value).toMap.withDefaultValue(0.0)
      _ = res.foreach(println(_))
    } yield ()

    def provideHas[R: HasConstructor, A: Tag](fn: R => A): ProviderMagnet[A] =
      HasConstructor[R].map(fn)

    val definition = new ModuleDef {
      make[String]
        .named("csv")
        .fromValue("lineitems.csv")

      make[CsvReader].fromResource(CsvReader.make _)
      make[Discounts].fromValue(Discounts.dummy)
      make[Console.Service].fromHas(Console.live)
      make[Task[Unit]].from(provideHas(
        program
          .mapError(_ continue new DiscountErr.AsThrowable {})
          .provide
      ))
    }

    val app = Injector()
      .produceGetF[Task, Task[Unit]](definition)
      .useEffect

    app.exitCode
  }
}

case class AppCfg(url: URI)