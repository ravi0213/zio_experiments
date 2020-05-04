package users

import java.time.{DateTimeException, OffsetDateTime}
import java.util.concurrent.TimeUnit

import users.domain.User
import users.effects.idGenerator._
import zio._
import zio.clock._
import zio.duration.Duration

package object mocks {
  def testIdGenerator(fixedUserId: User.Id): ULayer[IdGenerator] =
    ZLayer.succeed(
      new IdGenerator.Service {
        val userId = UIO(fixedUserId)
      }
    )
  def testClock(fixedDateTime: OffsetDateTime): ULayer[Clock] =
    ZLayer.succeed(new Clock.Service {
      val fixedMillis = fixedDateTime.toEpochSecond
      def currentTime(unit: TimeUnit): UIO[Long] =
        IO.effectTotal(fixedMillis).map(l => unit.convert(l, TimeUnit.MILLISECONDS))

      val nanoTime: UIO[Long] = IO.effectTotal(fixedMillis)

      def sleep(duration: Duration): UIO[Unit] = UIO(duration) *> UIO.unit

      def currentDateTime: IO[DateTimeException, OffsetDateTime] = {
        IO.succeed(fixedDateTime)
      }

    })
}
