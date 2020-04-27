package example

import example.modules.Programs._
import example.modules.Services._
import example.programs.UserProgram
import zio._

object Main extends App {

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    UserProgram
      .createUser(userService)
      .provideLayer(userProgramEnv)
      .fold(_ => 1, _ => 0)
}
