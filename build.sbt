name := "tournament-notifier"

version := "0.1"
organization := "janik_dotzel"
scalaVersion := "2.13.1"

// Webscraper for Scala
libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.0"

// STTP - HTTP for Scala
libraryDependencies += "com.softwaremill.sttp.client" %% "core" % "2.0.5"

// Scala-CSV - CSV Reader/Writer for Scala
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.6"
