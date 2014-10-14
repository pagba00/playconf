import play.Project._

import net.litola.SassPlugin

name := """hello-play-java"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaEbean,
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.webjars" %% "webjars-play" % "2.2.2",
  "com.google.inject" % "guice" % "3.0",
  "javax.inject" % "javax.inject" % "1",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.jsoup" % "jsoup" % "1.7.2" % "test",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.apache.commons" % "commons-lang3" % "3.3.2")

playJavaSettings ++ SassPlugin.sassSettings