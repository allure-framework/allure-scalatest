/* basic project info */
name := "allure-scalatest"

organization := "ru.yandex.qatools.allure"

description := "Scalatest adapter for Allure framework."

homepage := Some(url("https://github.com/allure-framework/allure-scalatest"))

startYear := Some(2014)

licenses := Seq(
  ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/allure-framework/allure-scalatest"),
    "scm:git:https://github.com/allure-framework/allure-scalatest.git",
    Some("scm:git:git@github.com:allure-framework/allure-scalatest.git")
  )
)

organizationName := "Yandex LLC"

/* scala versions and options */
scalaVersion := "2.12.7"

crossScalaVersions := Seq()

// These options will be used for *all* versions.
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-encoding", "UTF-8"
  // "-Xcheckinit" // for debugging only, see https://github.com/paulp/scala-faq/wiki/Initialization-Order
  // "-optimise"   // this option will slow your build
)

// These language flags will be used only for 2.10.x.
// Uncomment those you need, or if you hate SIP-18, all of them.
scalacOptions ++= {
  val sv = scalaVersion.value

  if (sv startsWith "2.10") List(
    "-Xverify",
    "-Ywarn-all",
    "-feature"
    // "-language:postfixOps",
    // "-language:reflectiveCalls",
    // "-language:implicitConversions"
    // "-language:higherKinds",
    // "-language:existentials",
    // "-language:experimental.macros",
    // "-language:experimental.dynamics"
  )
  else Nil
}

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation", "-source", "1.7", "-target", "1.7")

/* dependencies */
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq (
  "org.scalatest" %% "scalatest" % "3.0.5",
  "ru.yandex.qatools.allure" % "allure-java-aspects" % "1.5.4",
  "org.mockito" % "mockito-core" % "2.23.0" % "test"
)

/* testing */
parallelExecution in Test := false

/* sbt behavior */
logLevel in compile := Level.Warn

traceLevel := 5

offline := false

/* publishing */
publishMavenStyle := true

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

import ReleaseTransformations._

releasePublishArtifactsAction := PgpKeys.publishSigned.value // Use publishSigned in publishArtifacts step
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

pomExtra := <developers>
  <developer>
    <id>vania-pooh</id>
    <name>Ivan Krutov</name>
    <email>vania-pooh@yandex-team.ru</email>
    <url>http://allure.qatools.ru/</url>
  </developer>
</developers>
