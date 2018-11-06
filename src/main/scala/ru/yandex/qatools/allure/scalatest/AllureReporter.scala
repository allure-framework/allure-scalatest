package ru.yandex.qatools.allure.scalatest

import java.lang.annotation.Annotation
import java.util.UUID

import org.scalatest.Reporter
import org.scalatest.events.{Event, SuiteCompleted, SuiteStarting, TestFailed, TestIgnored, TestStarting, TestSucceeded, TopOfClass, TopOfMethod, _}
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import ru.yandex.qatools.allure.utils.AnnotationManager

class AllureReporter extends Reporter {

  private var lc = Allure.LIFECYCLE

  private val suiteIDToUUIDMap = scala.collection.mutable.HashMap[String, String]()

  def apply(event: Event): Unit = event match {

    case event: TestFailed     => testCaseFailed(event.throwable.getOrElse(new RuntimeException(event.message)))
    case event: TestStarting   => testCaseStarted(event.suiteId, event.testName, event.location)
    case event: SuiteStarting  => testSuiteStarted(getSuiteUuid(event.suiteId), event.suiteId, event.location)
    case event: SuiteCompleted => testSuiteFinished(getSuiteUuid(event.suiteId))
    case event: TestCanceled   => testCaseCanceled(event.throwable)

    case _: TestPending   => testCasePending()
    case _: TestIgnored   => testCaseCanceled(None)
    case _: TestSucceeded => testCaseFinished()

    case _ => ()

  }

  def getSuiteUuid(suiteId: String): String = suiteIDToUUIDMap.getOrElse(suiteId, {
    val uuid = UUID.randomUUID().toString
    suiteIDToUUIDMap += suiteId -> uuid
    uuid
  }
  )

  def lifecycle: Allure = lc

  def setLifecycle(lifecycle: Allure): Unit = {
    lc = lifecycle
  }

  def getAnnotations(location: Option[Location]): List[Annotation] = location match {
    case Some(TopOfClass(className))              => Class.forName(className).getAnnotations.toList
    case Some(TopOfMethod(className, methodName)) => Class.forName(className).getMethod(methodName).getDeclaredAnnotations.toList
    case _                                        => List()
  }

  private def testSuiteStarted(uuid: String, suiteId: String, location: Option[Location]): Unit = {
    val event = new TestSuiteStartedEvent(uuid, suiteId)
    val annotationManager = new AnnotationManager(getAnnotations(location): _*)
    annotationManager.update(event)
    lifecycle.fire(event)
  }

  private def testSuiteFinished(uuid: String): Unit = {
    lifecycle.fire(new TestSuiteFinishedEvent(uuid))
  }

  private def testCaseStarted(suiteId: String, testName: String, location: Option[Location]): Unit = {
    val uuid = getSuiteUuid(suiteId)
    val event = new TestCaseStartedEvent(uuid, testName)
    val annotationManager = new AnnotationManager(getAnnotations(location): _*)
    annotationManager.update(event)
    lifecycle.fire(event)
  }

  private def testCaseFinished(): Unit = {
    lifecycle.fire(new TestCaseFinishedEvent())
  }

  private def testCaseFailed(throwable: Throwable): Unit = {
    lifecycle.fire(new TestCaseFailureEvent().withThrowable(throwable))
  }

  private def testCaseCanceled(throwable: Option[Throwable]): Unit = {
    lifecycle.fire(throwable match {
      case Some(t) => new TestCaseCanceledEvent().withThrowable(t)
      case None    => new TestCaseCanceledEvent()
    })
  }

  private def testCasePending(): Unit = {
    lifecycle.fire(new TestCasePendingEvent())
  }

}