package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import cats.syntax.option._
import com.github.tototoshi.csv.CSVReader
import eyeem.shopping.CsvErr.error
import zio.IO
import zio.macros.accessible

import scala.io.Source

@accessible
trait CsvReader {
  def readLineitems(source: Source): IO[Capture[CsvErr], Stream[Lineitem]]
}

object CsvReader {
  val make = new CsvReader {
    def readLineitems(source: Source) = {
      for {
        reader <- IO(CSVReader.open(source)).mapError(error("CSVReader.open"))
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
        }.mapError(error("to Lineitem"))
      } yield res
    }
  }
}

trait CsvErr[+A] {
  def error(message: String)(e: Throwable): A
}

object CsvErr extends Constructors[CsvErr] {
  def error(message: String)(e: Throwable) =
    Capture[CsvErr](_.error(message)(e))

  trait AsThrowable extends CsvErr[Throwable] {
    def error(message: String)(e: Throwable) = new RuntimeException(s"$message: ${e.getMessage}")
  }

}
