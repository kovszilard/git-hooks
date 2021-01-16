package githooks

import zio._
import GitHook._

sealed trait GitHook { self =>
  def andThen(that: GitHook): GitHook = self match {
    case CommitMsg(description, f) => AndThen(self, that)
    case AndThen(l, r)                        => AndThen(self, that)
  }
}

object GitHook {
  
  final case class CommitMsg(description: String, f: String => ZIO[Any, Nothing, Boolean]) extends GitHook
  final case class AndThen(left: GitHook, right: GitHook) extends GitHook

  final case class HookResult(description: String, result: ZIO[Any, Nothing, Boolean])

  def evaluate(hook: GitHook, message: String): List[HookResult] = hook match {
    case CommitMsg(description, f) => List(HookResult(description, f(message)))
    case AndThen(left, right) => evaluate(left, message) ++ evaluate(right, message)
  }


  def evaluate2(hook: GitHook, message: String): UIO[List[(String, Boolean)]] = 
    hook match {
      case CommitMsg(description, f) => f(message).map(b => List((description, b)))
      case AndThen(left, right) => 
        for {
          l <- evaluate2(left, message)
          r <- evaluate2(right, message)
        } yield l ++ r
    }


  def exitCode2(results: List[(String, Boolean)]): ExitCode = {
      val isValid = results.map(_._2).foldLeft(true)(_ && _)
      if (isValid) ExitCode.success else ExitCode.failure
    }


  def exitCode(results: List[HookResult]): ZIO[Any, Throwable, ExitCode] =
    results.map(_.result).foldLeft(ZIO(true)){ case (acc, a) => 
      for {
        insideAcc <- acc
        insideA <- a
      } yield insideAcc && insideA
    }.map(isValid => if(isValid) ExitCode.success else ExitCode.failure)

}
