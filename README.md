# Allure ScalaTest Adapter
This adapter allows to retrieve test execution data from ScalaTest framework and convert it to the form suitable for Allure report generation.

## Usage
Simply add the following dependency to build.sbt:
```scala
libraryDependencies += "ru.yandex.qatools.allure" % "allure-scalatest" % "1.0.0"
```

Then attach **AllureReporter** in build.sbt:
```scala
testOptions in Test ++= Seq(
    Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
    Tests.Argument(TestFrameworks.ScalaTest, "-r", "ru.yandex.qatools.allure.scalatest.AllureReporter")
)
```

## Contact

- Mailing list [allure@yandex-team.ru](mailto:allure@yandex-team.ru)
