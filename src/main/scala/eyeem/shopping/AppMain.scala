package eyeem.shopping

import cats.syntax.option._
import com.github.tototoshi.csv.CSVReader
import distage.{Tag, _}
import zio._
import zio.console._
import zio.macros.accessible

import scala.io.Source.fromResource

@accessible
trait CsvReader {
  def readLineitems: UIO[Stream[Lineitem]]
}

object CsvReader {
  def make(csv: String@Id("csv")) =
    for {
      reader <- IO(CSVReader.open(fromResource(csv))).toManaged(r => IO(r.close()).ignore)
      stringsStreamWithHeader = reader.toStream
      res <- IO {
        val stringsStream = stringsStreamWithHeader.tail
        stringsStream.map {
          case id :: price :: dis :: _ =>
            Lineitem(
              id.toInt,
              BigDecimal(price),
              if (dis.isEmpty) none else dis.some,
            )
        }
      }.toManaged_
    } yield new CsvReader {
      def readLineitems = IO.succeed {
        res
      }
    }
}

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
