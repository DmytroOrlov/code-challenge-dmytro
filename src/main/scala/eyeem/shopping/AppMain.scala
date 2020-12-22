package eyeem.shopping

import zio._
import zio.console._

object AppMain extends App {
  def run(args: List[String]) = {
    val program = for {
      _ <- putStrLn("123")
    } yield ()

    program.exitCode
  }
}
