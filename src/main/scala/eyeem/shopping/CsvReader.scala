package eyeem.shopping

import cats.syntax.option._
import com.github.tototoshi.csv.CSVReader
import distage.Id
import zio.macros.accessible
import zio.{IO, UIO}

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
