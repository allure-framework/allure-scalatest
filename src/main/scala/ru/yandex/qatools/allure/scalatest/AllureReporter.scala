package ru.yandex.qatools.allure.scalatest

import org.scalatest.Reporter
import org.scalatest.events._
import ru.yandex.qatools.allure.Allure
import ru.yandex.qatools.allure.events._
import java.util.UUID
import ru.yandex.qatools.allure.utils.AnnotationManager
import java.lang.annotation.Annotation
import org.scalatest.events.TestStarting
import org.scalatest.events.Event
import org.scalatest.events.SuiteStarting
import org.scalatest.events.TestSucceeded
import org.scalatest.events.SuiteCompleted
import org.scalatest.events.TopOfMethod
import org.scalatest.events.TestIgnored
import org.scalatest.events.TestFailed
import scala.Some
import org.scalatest.events.TopOfClass

class AllureReporter extends Reporter {
  
  private var lc = Allure.LIFECYCLE

  private val suiteIDToUUIDMap = scala.collection.mutable.HashMap[String, String]()

  def apply(event: Event) = event match {

    case TestStarting(_, _, suiteId, _, _, _, _, location, _, _, _, _) => testCaseStarted(suiteId, location)

    case TestSucceeded(_, _, _, _, _, _, _, _, _, _, _, _, _, _) => testCaseFinished()

    case TestFailed(_, message, _, _, _, _, _, _, throwable, _, _, _, _, _, _, _) => testCaseFailed(throwable match {
      case Some(t) => t
      case None => new RuntimeException(message)
    })

    case TestIgnored(_, _, _, _, _, _, _, _, _, _, _) => testCaseSkipped()

    case SuiteStarting(_, _, suiteId, _, _, location, _, _, _, _) => testSuiteStarted(getSuiteUuid(suiteId), suiteId, location)

    case SuiteCompleted(_, _, suiteId, _, _, _, _, _, _, _, _) => testSuiteFinished(getSuiteUuid(suiteId))
    
    case _ => ()

  }

  def getSuiteUuid(suiteId: String): String = suiteIDToUUIDMap.get(suiteId) match {
    case Some(uuid) => uuid
    case None =>
      val uuid = UUID.randomUUID().toString
      suiteIDToUUIDMap += suiteId -> uuid
      uuid
  }

  def lifecycle = lc

  def setLifecycle(lifecycle: Allure) {
    lc = lifecycle
  }

  private def testSuiteStarted(uuid: String, suiteId: String, location: Option[Location]) {
    val event = new TestSuiteStartedEvent(uuid, suiteId)
    val annotationManager = new AnnotationManager(getAnnotations(location):_*)
    annotationManager.update(event)
    lifecycle.fire(event)
  }

  private def testSuiteFinished(uuid: String) {
    lifecycle.fire(new TestSuiteFinishedEvent(uuid))
  }

  private def testCaseStarted(suiteId: String, location: Option[Location]) {
    val uuid = getSuiteUuid(suiteId)
    val methodName = getMethodName(location)
    val event = new TestCaseStartedEvent(uuid, methodName)
    val annotationManager = new AnnotationManager(getAnnotations(location):_*)
    annotationManager.update(event)
    lifecycle.fire(event)
  }

  private def testCaseFinished() {
    lifecycle.fire(new TestCaseFinishedEvent())
  }

  private def testCaseFailed(throwable: Throwable) {
    lifecycle.fire(new TestCaseFailureEvent().withThrowable(throwable))
  }

  private def testCaseSkipped() {
    lifecycle.fire(new TestCaseSkippedEvent())
  }

  def getAnnotations(location: Option[Location]): List[Annotation] = location match {
    case Some(ln) => ln match {
      case TopOfClass(className) => Class.forName(className).getAnnotations.toList
      case TopOfMethod(className, methodName) => Class.forName(className).getMethod(methodName).getDeclaredAnnotations.toList
      case _ => List()
    }
    case _ => List()
  }
  
  def getMethodName(location: Option[Location]): String = location match {
    case Some(ln) => ln match {
      case TopOfMethod(_, methodName) => methodName
      case _ => ""
    }
    case _ => ""
  }

}