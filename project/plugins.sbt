//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.4")

resolvers += Resolver.url(
  "rtimush/sbt-plugin-snapshots",
  new URL("https://dl.bintray.com/rtimush/sbt-plugin-snapshots/"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")
