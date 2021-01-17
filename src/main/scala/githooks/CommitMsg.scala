package githooks

import zio._
import zio.console._
import GitHook._
import zio.stream._
import Utils._

object HookCommitMsg extends App {

    // Args: message file name

  def run(args: List[String]): zio.URIO[zio.ZEnv,ExitCode] = 
    printArgs(args).orDie

  def printArgs(args: List[String]) = 
  for {
    _ <- putStrLn("Arguments given to this hook:")
    _ <- putStrLn(args.zipWithIndex.map{ case (arg, i) => s"$i: $arg"}.mkString("\n"))
    workDir <- workDirPath
    path = s"$workDir/${args.head}"
    _ <- putStrLn(s"commit message path is: $path")
    msg <- readTextFile(path)
    // _ <- putStrLn(msg)
    evaluated <- GitHook.evaluate2(rules, msg)
    _ <- putStrLn(evaluated.mkString("\n"))
  } yield exitCode2(evaluated)

  
    
  val rule1 = CommitMsg(
    "Separate subject from body with a blank line", 
    { message => 
      UIO{
          val lines = message
            .split("\n")
            .takeWhile(!_.startsWith("# ------------------------ >8 ------------------------"))
            .filterNot(_.startsWith("#"))
          lines.size match {
              case s if s >= 2 => (lines(0).trim != "") && (lines(1) == "")
              case _ => true
          }
      }
    }
  )

  val rule2 = CommitMsg(
    "Limit the subject line to 50 characters",
    { message => 
      UIO(message.split("\n").headOption.map(_.size <= 50).getOrElse(false))
    }
  )

  val rule3 = CommitMsg(
    "Capitalize the subject line",
    { message => 
      UIO{
        (for {
          title <- message.split("\n").headOption
          firstChar <- title.headOption
        } yield firstChar.isUpper).getOrElse(false)
      }
    }
  )

  val rule4 = CommitMsg(
    "Do not end the subject line with a period",
    { message =>
      UIO {
        message.split("\n").headOption.map(_.trim.last match {
        case '.' | '!' | '?' => false
        case _ => true
      }).getOrElse(false)
      }
    }
  )

  val rules = rule1.andThen(rule2).andThen(rule3)
  
}
