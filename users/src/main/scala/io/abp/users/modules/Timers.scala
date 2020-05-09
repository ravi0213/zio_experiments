package io.abp.users.modules

import scala.concurrent.duration.{FiniteDuration, NANOSECONDS, TimeUnit}

import cats.effect
import zio._

class Timers[Env] {
  implicit final def ioTimer[E]: effect.Timer[ZIO[Env, E, *]] =
    new effect.Timer[ZIO[Env, E, *]] {
      override final def clock: effect.Clock[ZIO[Env, E, *]] =
        new effect.Clock[ZIO[Env, E, *]] {
          override final def monotonic(unit: TimeUnit): ZIO[Env, E, Long] =
            zio.clock.nanoTime.map(unit.convert(_, NANOSECONDS)).provideLayer(ZEnv.live)

          override final def realTime(unit: TimeUnit): ZIO[Env, E, Long] =
            zio.clock.currentTime(unit).provideLayer(ZEnv.live)
        }

      override final def sleep(duration: FiniteDuration): ZIO[Env, E, Unit] =
        zio.clock.sleep(zio.duration.Duration.fromNanos(duration.toNanos)).provideLayer(ZEnv.live)
    }
}
