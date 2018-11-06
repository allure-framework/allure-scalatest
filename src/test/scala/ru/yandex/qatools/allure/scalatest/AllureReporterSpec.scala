package ru.yandex.qatools.allure.scalatest

import java.util.UUID

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.events.{SuiteCompleted, SuiteStarting, TestFailed, TestStarting, TestSucceeded, _}
import org.scalatest.{BeforeAndAfter, FlatSpec}
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import ru.yandex.qatools.allure.utils.AnnotationManager

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

  after {
    verifyNoMoreInteractions(allure)
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
      ordinal = testOrdinal,
      suiteName = "",
      suiteId = "",
      suiteClassName = None,
      duration = None,
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
    ))
    verify(allure).fire(new TestSuiteFinishedEvent(testUuid))
  }

  it should "fire TestCaseStarted event on test case start" in {
    reporter.apply(TestStarting(
      ordinal = testOrdinal,
      suiteName = "",
      suiteId = testSuiteId,
      suiteClassName = None,
      testName = testMethodName,
      testText = "",
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
    )
    )

    val event = new TestCaseStartedEvent(testUuid, testMethodName)

    // TestCaseStartedEvent event will be updated inside AllureReporter.testCaseStarted() method
    // so need to do the same here to have the same object hashcode
    val annotationManager = new AnnotationManager(reporter.getAnnotations(None): _*)
    annotationManager.update(event)

    verify(allure).fire(event)
  }

  it should "fire TestCaseFinished event on test case success" in {
    reporter.apply(TestSucceeded(
      ordinal = testOrdinal,
      suiteName = "",
      suiteId = "",
      suiteClassName = None,
      testName = "",
      testText = "",
      recordedEvents = collection.immutable.IndexedSeq.empty[RecordableEvent],
      duration = None,
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
    ))
    verify(allure).fire(new TestCaseFinishedEvent)
  }

  it should "fire TestCaseFailed event with throwable on test case failure when throwable is present" in {
    reporter.apply(TestFailed(
      ordinal = testOrdinal,
      message = "",
      suiteName = "",
      suiteId = "",
      suiteClassName = None,
      testName = "",
      testText = "",
      recordedEvents = collection.immutable.IndexedSeq.empty[RecordableEvent],
      throwable = Some(testException),
      duration = None,
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
    ))
    verify(allure).fire(new TestCaseFailureEvent().withThrowable(testException))
  }

  it should "fire TestCaseFailed event with runtime exception having message inside on test case failure when throwable is missing" in {
    reporter.apply(TestFailed(
      ordinal = testOrdinal,
      message = testMessage,
      suiteName = "",
      suiteId = "",
      suiteClassName = None,
      testName = "",
      testText = "",
      recordedEvents = collection.immutable.IndexedSeq.empty[RecordableEvent],
      throwable = None,
      duration = None,
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
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
      ordinal = testOrdinal,
      suiteName = testMessage,
      suiteId = "",
      suiteClassName = None,
      testName = "",
      testText = "",
      recordedEvents = collection.immutable.IndexedSeq.empty[RecordableEvent],
      duration = None,
      formatter = None,
      location = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
    ))
    verify(allure).fire(new TestCasePendingEvent)
  }

  it should "fire TestCaseCanceled event when test case is canceled" in {
    reporter.apply(TestCanceled(
      ordinal = testOrdinal,
      message = testMessage,
      suiteName = "",
      suiteId = "",
      suiteClassName = None,
      testName = "",
      testText = "",
      recordedEvents = collection.immutable.IndexedSeq.empty[RecordableEvent],
      throwable = None,
      duration = None,
      formatter = None,
      location = None,
      rerunner = None,
      payload = None,
      threadName = "",
      timeStamp = testTimestamp
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
    assert(reporter.getAnnotations(Some(LineInFile(0, "", None))).isEmpty)
    assert(reporter.getAnnotations(None).isEmpty)
  }

}
