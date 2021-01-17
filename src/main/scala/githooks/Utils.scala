package githooks

import zio._
import zio.stream._
import zio.blocking.Blocking
import java.nio.file.Paths

object Utils {
  
  def readTextFile(path: String): ZIO[Blocking, Throwable, String] = 
    Stream
      .fromFile(Paths.get(path))
      .transduce(ZTransducer.utf8Decode)
      .runCollect
      .map(_.mkString)

  def runComand(cmd: String): UIO[String] = UIO {
    import sys.process._
    cmd.!!
  }

  val workDirPath = runComand("git worktree list --porcelain").map(a => a.split("\n").head.split(" ")(1))

  def filterCommitMessage(message: String): String =
    message
      .split("\n")
      .takeWhile(!_.startsWith("# ------------------------ >8 ------------------------"))
      .filterNot(_.startsWith("#"))
      .mkString("\n")
}
