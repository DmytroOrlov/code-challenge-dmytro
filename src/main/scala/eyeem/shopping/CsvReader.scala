package eyeem.shopping

import capture.Capture
import capture.Capture.Constructors
import cats.syntax.option._
import com.github.tototoshi.csv.CSVReader
import eyeem.shopping.CsvErr.exception
import zio.IO
import zio.macros.accessible

import scala.io.Source

@accessible
trait CsvReader {
  def readLineitems(source: Source): IO[Capture[CsvErr], Stream[Lineitem]]
}

case class Lineitem(
    photoId: Int,
    price: BigDecimal,
    discountCode: Option[String],
)

object CsvReader {
  val make = new CsvReader {
    def readLineitems(source: Source) = {
      for {
        reader <- IO(CSVReader.open(source)).mapError(exception("CSVReader.open"))
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
        }.mapError(exception("to Lineitem"))
      } yield res
    }
  }
}

trait CsvErr[+A] {
  def exception(message: String)(e: Throwable): A
}

object CsvErr extends Constructors[CsvErr] {
  def exception(message: String)(e: Throwable) =
    Capture[CsvErr](_.exception(message)(e))

  trait AsThrowable extends CsvErr[Throwable] {
    def exception(message: String)(e: Throwable) = new RuntimeException(s"$message: ${e.getMessage}")
  }

  trait AsFailureResp extends CsvErr[FailureResp] {
    def exception(message: String)(e: Throwable) = FailureResp(s"$message: ${e.getMessage}")
  }

}
