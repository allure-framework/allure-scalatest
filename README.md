# Allure ScalaTest Adapter
This adapter allows to retrieve test execution data from ScalaTest framework and convert it to the form suitable for Allure report generation.

## Example project
Example project is located at: https://github.com/allure-framework/allure-scalatest-example

## Usage
**In order to use this adapter you need to have JDK 1.7+ installed.** To enabled adapter simply add the following dependency to build.sbt:
```scala
libraryDependencies += "ru.yandex.qatools.allure" % "allure-scalatest_2.10" % "1.3.9-SNAPSHOT"
```

Then attach **AllureReporter** in build.sbt:
```scala
testOptions in Test ++= Seq(
    Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    Tests.Argument(TestFrameworks.ScalaTest, "-C", "ru.yandex.qatools.allure.scalatest.AllureReporter")
)
```

## Publishing
A publicly available (on public keyserver) GPG key should be present in you default GPG keyring. You need to create **sonatype.sbt** file in **~/.sbt/<sbt-version>/**:
```scala
credentials += Credentials("Sonatype Nexus Repository Manager",
                           "oss.sonatype.org",
                           "login",
                           "password")
```
To publish simply run:
```bash
$ sbt publish-signed
```