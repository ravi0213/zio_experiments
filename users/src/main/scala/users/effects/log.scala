package users.effects

import zio._

package object log {
  type Logging = Has[Logging.Service]

  object Logging {
    trait Service {
      def info(s: String): UIO[Unit]
      def error[C](cause: C)(s: String): UIO[Unit]
      def debug(s: String): UIO[Unit]
    }

    import zio.console.Console
    object Service {
      val consoleLogger: ZLayer[Console, Nothing, Logging] =
        ZLayer.fromFunction(console =>
          new Service {
            def info(s: String): UIO[Unit] = console.get.putStrLn(s"info - $s")
            def error[C](cause: C)(s: String): UIO[Unit] =
              console.get.putStrLn(s"error - $s. $cause")
            def debug(s: String): UIO[Unit] = console.get.putStrLn(s"debug - $s")
          }
        )
    }

    val consoleLogger: ZLayer[Any, Nothing, Logging] =
      Console.live >>> Service.consoleLogger
  }

  def info(s: String): ZIO[Logging, Nothing, Unit] =
    ZIO.accessM(_.get.info(s))

  def error[C](cause: C)(s: String): ZIO[Logging, Nothing, Unit] =
    ZIO.accessM(_.get.error(cause)(s))

  def debug(s: String): ZIO[Logging, Nothing, Unit] =
    ZIO.accessM(_.get.debug(s))
}
