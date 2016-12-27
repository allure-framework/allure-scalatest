package ru.yandex.qatools.allure.scalatest

import org.scalatest.{BeforeAndAfter, FlatSpec}
import ru.yandex.qatools.allure.Allure
import org.mockito.Mockito._
import org.scalatest.events._
import ru.yandex.qatools.allure.events._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.events.TestStarting
import org.scalatest.events.SuiteStarting
import org.scalatest.events.TestSucceeded
import org.scalatest.events.SuiteCompleted
import org.scalatest.events.TestFailed
import scala.Some
import java.util.UUID

class AllureReporterSpec extends FlatSpec with BeforeAndAfter {

  var allure: Allure = _

  var reporter: AllureReporter = _

  val testUuid = "some-uid"
  val testClassName = "ru.yandex.qatools.allure.scalatest.testclasses.TestSpec"
  val testMethodName = "testMethod"
  val testSuiteId = "some-suite"
  val testMessage = "Some message"
  val testException = new Exception(testMessage)
  val testTimestamp = 0
  val testOrdinal = new Ordinal(testTimestamp)

  before {
    allure = mock(classOf[Allure])
    reporter = spy(new AllureReporter)
    reporter.setLifecycle(allure)
    when(reporter.getSuiteUuid(anyString())).thenReturn(testUuid)
  }

  "AllureReporter" should "fire Allure TestSuiteStarted event on suite start" in {
    reporter.apply(SuiteStarting(
      testOrdinal,
      "",
      testSuiteId,
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))

    verify(allure).fire(new TestSuiteStartedEvent(testUuid, testSuiteId))
  }

  it should "fire TestSuiteFinished event on suite finish" in {
    reporter.apply(SuiteCompleted(
      testOrdinal,
      "",
      "",
      None,
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestSuiteFinishedEvent(testUuid))
  }

  it should "fire TestCaseStarted event on test case start" in {
    reporter.apply(TestStarting(
      testOrdinal,
      "",
      "",
      None,
      testMethodName,
      "",
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))

    // Mockito had trouble with the TestCaseStartedEvent reference.
    // Just verify that the reporter didn't pass garbage arguments.

    val captor = ArgumentCaptor.forClass(classOf[TestCaseStartedEvent])
    verify(allure).fire(captor.capture())

    val event = captor.getValue
    assert(event.getSuiteUid == testUuid)
    assert(event.getName == testMethodName)
  }

  it should "fire TestCaseFinished event on test case success" in {
    reporter.apply(TestSucceeded(
      testOrdinal,
      "",
      "",
      None,
      "",
      "",
      collection.immutable.IndexedSeq.empty[RecordableEvent],
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestCaseFinishedEvent)
  }

  it should "fire TestCaseFailed event with throwable on test case failure when throwable is present" in {
    reporter.apply(TestFailed(
      testOrdinal,
      "",
      "",
      "",
      None,
      "",
      "",
      collection.immutable.IndexedSeq.empty[RecordableEvent],
      Some(testException),
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestCaseFailureEvent().withThrowable(testException))
  }

  it should "fire TestCaseFailed event with runtime exception having message inside on test case failure when throwable is missing" in {
    reporter.apply(TestFailed(
      testOrdinal,
      testMessage,
      "",
      "",
      None,
      "",
      "",
      collection.immutable.IndexedSeq.empty[RecordableEvent],
      None,
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(any(classOf[TestCaseFailureEvent]))
  }

  it should "fire TestCaseCanceled event when test case is skipped" in {
    reporter.apply(TestIgnored(
      testOrdinal,
      "",
      "",
      None,
      "",
      "",
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestCaseCanceledEvent)
  }

  it should "fire TestCasePending event when test case is pending" in {
    reporter.apply(TestPending(
      testOrdinal,
      testMessage,
      "",
      None,
      "",
      "",
      collection.immutable.IndexedSeq.empty[RecordableEvent],
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestCasePendingEvent)
  }

  it should "fire TestCaseCanceled event when test case is canceled" in {
    reporter.apply(TestCanceled(
      testOrdinal,
      testMessage,
      "",
      "",
      None,
      "",
      "",
      collection.immutable.IndexedSeq.empty[RecordableEvent],
      None,
      None,
      None,
      None,
      None,
      None,
      "",
      testTimestamp
    ))
    verify(allure).fire(new TestCaseCanceledEvent)
  }

  it should "return uuid on first and subsequent getSuiteUuid calls" in {
    val reporter = new AllureReporter
    val firstUUID = reporter.getSuiteUuid(testSuiteId)
    assert(firstUUID.length > 0)
    val secondUUID = reporter.getSuiteUuid(testSuiteId)
    assert(firstUUID == secondUUID)
    assert(UUID.fromString(firstUUID).equals(UUID.fromString(secondUUID)))
  }

  it should "return empty list when called with any location except TopOfMethod or TopOfClass or None" in {
    val reporter = new AllureReporter
    assert(reporter.getAnnotations(Some(LineInFile(0, ""))).isEmpty)
    assert(reporter.getAnnotations(None).isEmpty)
  }

  after {
    verifyNoMoreInteractions(allure)
  }

}
