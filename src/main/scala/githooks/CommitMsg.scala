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
//      _ <- putStrLn("Arguments given to this hook:")
//      _ <- putStrLn(args.zipWithIndex.map{ case (arg, i) => s"$i: $arg"}.mkString("\n"))
      workDir <- workDirPath
      path = s"$workDir/${args.head}"
//      _ <- putStrLn(s"commit message path is: $path")
      msg <- readTextFile(path)
      evaluated <- GitHook.evaluate2(rules, msg)
      _ <- putStrLn("")
      _ <- putStrLn(evaluated.mkString("\n"))
      _ <- putStrLn("")
    } yield exitCode2(evaluated)



  val rule1 = CommitMsg(
    "Separate subject from body with a blank line", 
    { message => 
      UIO{
        val lines = filterCommitMessage(message).split("\n")
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
      UIO(
        filterCommitMessage(message)
          .split("\n")
          .headOption
          .map(_.size <= 50)
          .getOrElse(false)
      )
    }
  )

  val rule3 = CommitMsg(
    "Capitalize the subject line",
    { message => 
      UIO{
        (for {
          title <- filterCommitMessage(message).split("\n").headOption
          firstChar <- title.headOption
        } yield firstChar.isUpper).getOrElse(false)
      }
    }
  )

  val rule4 = CommitMsg(
    "Do not end the subject line with a period",
    { message =>
      UIO {
        filterCommitMessage(message).split("\n").headOption.map(_.trim.lastOption match {
        case Some('.') | Some('!') | Some('?') => false
        case _ => true
      }).getOrElse(false)
      }
    }
  )

  val rule6 = CommitMsg(
    "Wrap the body at 72 characters",
    { message =>
      UIO {
         !filterCommitMessage(message)
           .split("\n")
           .exists(s => s.size > 72)
      }
    }
  )

  val rules = rule1.andThen(rule2).andThen(rule3).andThen(rule4).andThen(rule6)
  
}
