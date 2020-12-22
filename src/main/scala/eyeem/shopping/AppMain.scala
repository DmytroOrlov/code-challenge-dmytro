package eyeem.shopping

import com.github.tototoshi.csv.CSVReader
import distage.{Tag, _}
import zio._
import zio.console._
import zio.macros.accessible

import scala.io.Source.fromResource

@accessible
trait CsvReader {
  def readLineitems: Task[Stream[Lineitem]]
}

object CsvReader {
  def make(csv: String@Id("csv")) =
    for {
      reader <- IO(CSVReader.open(fromResource(csv))).toManaged(r => IO(r.close()).ignore)
    } yield new CsvReader {
      def readLineitems = {
        ???
      }
    }
}

object AppMain extends App {
  def run(args: List[String]) = {
    val program = for {
      _ <- putStrLn("123")
    } yield ()

    def provideHas[R: HasConstructor, A: Tag](fn: R => A): ProviderMagnet[A] =
      HasConstructor[R].map(fn)

    val definition = new ModuleDef {
      make[String]
        .named("csv")
        .fromValue("lineitems.csv")
      make[Console.Service].fromHas(Console.live)
      make[UIO[Unit]].from(provideHas(program.provide))
    }

    val app = Injector()
      .produceGetF[Task, UIO[Unit]](definition)
      .useEffect

    app.exitCode
  }
}
