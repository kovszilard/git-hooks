package githooks

import githooks.HookCommitMsg._
import zio._
import zio.console._
import zio.test._
import zio.test.Assertion._
import zio.test.environment._

object CommitMsgSpec extends DefaultRunnableSpec {
  val longMessageEnds =
    """
      |# Please enter the commit message for your changes. Lines starting
      |# with '#' will be ignored, and an empty message aborts the commit.
      |#
      |# On branch master
      |# Your branch is ahead of 'origin/master' by 3 commits.
      |#   (use "git push" to publish your local commits)
      |#
      |# Changes to be committed:
      |#	modified:   a.txt
      |#
      |# Untracked files:
      |#	src/test/
      |#
      |# ------------------------ >8 ------------------------
      |# Do not modify or remove the line above.
      |# Everything below it will be ignored.
      |diff --git a/a.txt b/a.txt
      |index e69de29..5d308e1 100644
      |--- a/a.txt
      |+++ b/a.txt
      |@@ -0,0 +1 @@
      |+aaaa
      |""".stripMargin

  def makeLongCommitMessage(firstLine: String) = firstLine + "\n" + longMessageEnds

  def spec = suite("CommitMsgSpec")(

    testM("Separate subject from body with a blank line") {

      val invalidCases = List("a\nb\nc", makeLongCommitMessage("a\nb\nc"))
      val validCases = List("A\n\nb", makeLongCommitMessage("A\n\nb"))

      for {
        invalids <- ZIO.foreach(invalidCases)(rule1.f)
        valids <- ZIO.foreach(validCases)(rule1.f)
      } yield
        assert(invalids)(contains(true).negate) &&
          assert(valids)(contains(false).negate)
    },

    testM("Limit the subject line to 50 characters") {

      val invalidCases = List("a"*51, makeLongCommitMessage("a"*51))
      val validCases = List("A"*50, makeLongCommitMessage("A"*50))

      for {
        invalids <- ZIO.foreach(invalidCases)(rule2.f)
        valids <- ZIO.foreach(validCases)(rule2.f)
      } yield
        assert(invalids)(contains(true).negate) &&
          assert(valids)(contains(false).negate)
    },

    testM("Capitalize the subject line") {

      val invalidCases = List("a", makeLongCommitMessage("a"))
      val validCases = List("A", makeLongCommitMessage("Aaaa"))

      for {
        invalids <- ZIO.foreach(invalidCases)(rule3.f)
        valids <- ZIO.foreach(validCases)(rule3.f)
      } yield
        assert(invalids)(contains(true).negate) &&
        assert(valids)(contains(false).negate)
    },

    testM("Do not end the subject line with a period") {

      val invalidCases = List(".", "!", "?")
      val validCases = List("a", "b", "", makeLongCommitMessage("aaa"))

      for {
        invalids <- ZIO.foreach(invalidCases)(rule4.f)
        valids <- ZIO.foreach(validCases)(rule4.f)
      } yield
        assert(invalids)(contains(true).negate) &&
        assert(valids)(contains(false).negate)
    },

    testM("Wrap the body at 72 characters") {

      val invalidCases = List("a"*73, ("a"*72)+"\n"+("b"*73), makeLongCommitMessage("a"*73), makeLongCommitMessage(("a"*72)+"\n"+("b"*73)))
      val validCases = List("a"*72, "a\n\nb", "a\nb\nc\n", makeLongCommitMessage("a"*72), makeLongCommitMessage(("a"*72)+"\n"+("b"*72)))

      for {
        invalids <- ZIO.foreach(invalidCases)(rule6.f)
        valids <- ZIO.foreach(validCases)(rule6.f)
      } yield
        assert(invalids)(contains(true).negate) &&
        assert(valids)(contains(false).negate)
    }
  )
}
