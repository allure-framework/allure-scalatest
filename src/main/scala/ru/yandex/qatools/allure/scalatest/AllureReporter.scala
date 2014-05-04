package ru.yandex.qatools.allure.scalatest

import org.scalatest.Reporter
import org.scalatest.events._
import org.scalatest.events.TestIgnored
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events.{TestCaseFailureEvent, TestSuiteFinishedEvent, TestSuiteStartedEvent, TestCaseFinishedEvent}
import java.util.UUID

class AllureReporter extends Reporter {

  private val lifecycle = Allure.LIFECYCLE

  private val ordinalToSuiteUIDMap = scala.collection.mutable.HashMap[Int, String]()

  def apply(event: Event) = event match {

    case TestStarting(
      ordinal,
      suiteName,
      suiteId,
      suiteClassName,
      testName,
      testText,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testCaseStarted()

    case TestSucceeded(
      ordinal,
      suiteName,
      suiteId,
      suiteClassName,
      testName,
      testText,
      recordedEvents,
      duration,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testCaseFinished()

    case TestFailed(
      ordinal,
      message,
      suiteName,
      suiteId,
      suiteClassName,
      testName,
      testText,
      recordedEvents,
      throwable,
      duration,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testCaseFailed(throwable match {
      case Some(t) => t
      case None => new RuntimeException("Test case failed")
    })

    case TestIgnored(
      ordinal,
      suiteName,
      suiteId,
      suiteClassName,
      testName,
      testText,
      formatter,
      location,
      payload,
      threadName,
      timeStamp
    ) => testCaseSkipped(new RuntimeException("Test case skipped"))

    case SuiteStarting(
      ordinal,
      suiteName,
      suiteId,
      suiteClassName,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testSuiteStarted(ordinal, suiteName)

    case SuiteCompleted(
      ordinal,
      suiteName,
      suiteId,
      suiteClassName,
      duration,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testSuiteFinished(ordinal)

    case SuiteAborted(
      ordinal,
      message,
      suiteName,
      suiteId,
      suiteClassName,
      throwable,
      duration,
      formatter,
      location,
      rerunner,
      payload,
      threadName,
      timeStamp
    ) => testSuiteFinished(ordinal)

    case _ => ()

  }

  private def testSuiteStarted(ordinal: Ordinal, suiteName: String) {
    val uuid = getSuiteUuid(ordinal)
    lifecycle.fire(new TestSuiteStartedEvent(uuid, suiteName))
  }

  private def testSuiteFinished(ordinal: Ordinal) {
    val uuid = getSuiteUuid(ordinal)
    lifecycle.fire(new TestSuiteFinishedEvent(uuid))
  }

  private def testCaseStarted() {
    //TODO: implement this!
    lifecycle.fire(new TestCaseFinishedEvent())
  }

  private def testCaseFinished() {
    lifecycle.fire(new TestCaseFinishedEvent())
  }

  private def testCaseFailed(throwable: Throwable) {
    lifecycle.fire(new TestCaseFailureEvent().withThrowable(throwable))
  }

  private def testCaseSkipped(throwable: Throwable) {
    lifecycle.fire(new TestCaseFailureEvent().withThrowable(throwable))
  }

  private def getSuiteUuid(ordinal: Ordinal): String = ordinalToSuiteUIDMap.get(ordinal.runStamp) match {
    case Some(uuid) => uuid
    case None => {
      val uuid = UUID.randomUUID().toString
      ordinalToSuiteUIDMap += ordinal.runStamp -> uuid
      uuid
    }
  }

}
