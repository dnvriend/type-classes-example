name := "type-classes-example"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.12.1"

// functional and typelevel programming
// https://github.com/scalaz/scalaz
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8"

/////////////////
// JSON libraries
// play-json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M1"
// akka-http-spray-json
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.1"

// https://github.com/mpilquist/simulacrum
libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"

// testing
// https://github.com/typelevel/scalaz-scalatest
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.1" % Test
// https://github.com/scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1"

// testing configuration
fork in Test := true
parallelExecution := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

// https://github.com/scalamacros/paradise
// http://docs.scala-lang.org/overviews/macros/paradise.html
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

enablePlugins(AutomateHeaderPlugin, SbtScalariform)

initialize ~= { _ =>
  val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
  if (ansi) System.setProperty("scala.color", "true")
}

initialCommands in console := """
import scalaz._, Scalaz._
import scala.concurrent._
import scala.collection.immutable._
import scala.reflect.runtime.universe._
import scala.concurrent.ExecutionContext.Implicits.global
"""