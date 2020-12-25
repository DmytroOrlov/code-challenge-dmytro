package eyeem.shopping

import capture.Capture
import cats.syntax.option.{none, _}
import zio.Schedule.{elapsed, exponential}
import zio._
import zio.clock.Clock
import zio.duration.Duration.fromScala
import zio.duration._
import zio.macros.accessible

import scala.io.Source
import scala.math.BigDecimal.RoundingMode.UP

@accessible
trait Calculate {
  def total(csv: Source): IO[Capture[CsvErr with DiscountErr], BigDecimal]
}

object Calculate {
  def make(cfg: AppCfg) =
    for {
      env <- ZIO.environment[Clock with Has[CsvReader] with Has[Discounts]]
    } yield new Calculate {
      def total(csv: Source) =
        (for {
          lineitems <- CsvReader.readLineitems(csv)
          dsNames = lineitems.flatMap(_.discountCode).distinct
          discounts <- ZIO.collectParN(cfg.parallelism)(dsNames.toList)(
            Discounts.discount(_)
              .retry((exponential(1.millisecond) >>> elapsed).whileOutput(_ < fromScala(cfg.retryElapsed)))
              .bimap(_.some, _.toRight(none))
              .absolve
          )
          discountMap = discounts.map(d => d.name -> d.discount).toMap.withDefaultValue(0)
          res = lineitems.foldLeft(BigDecimal(0)) { (acc, li) =>
            acc + li.discountCode.fold(li.price) { d =>
              (li.price * (1 - discountMap(d) * BigDecimal(0.01)))
                .setScale(2, UP)
            }
          }
        } yield res) provide env
    }
}
