package io.abp.users.modules

import io.abp.users.config.LoggingConfig
import io.abp.users.config.LoggingConfig.LoggerConfig
import zio._
import zio.clock._
import zio.console._
import zio.logging._
import zio.logging.slf4j._

object Logger {

  def apply(config: LoggingConfig): ULayer[Logging] =
    config.loggerConfig match {
      case LoggerConfig.Mock => Logger.mock
      case LoggerConfig.Live => slf4jLogger
    }

  private val logFormat = "%s"
  private def logFormatWithContext(context: LogContext, message: => String) = {
    logFormat.format(message, context)
  }
  private val slf4jLogger = Slf4jLogger.make(logFormatWithContext)

  val mock = (Clock.live ++ Console.live) >>> Logging.console(logFormatWithContext)
}
