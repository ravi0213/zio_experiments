package io.abp.users.modules

import zio.logging._
import zio.logging.slf4j._

object Logger {

  private val logFormat = "%s"
  private def logFormatWithContext(context: LogContext, message: => String) = {
    logFormat.format(message, context)
  }
  val slf4jLogger = Slf4jLogger.make(logFormatWithContext)
}
