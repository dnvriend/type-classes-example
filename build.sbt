name := "type-classes-example"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.12.4"

// functional and typelevel programming
// https://github.com/scalaz/scalaz
val scalazVersion = "7.2.16"
val playJsonVersion = "2.6.7"
libraryDependencies += "org.scalaz" %% "scalaz-core" % scalazVersion
libraryDependencies += "org.scalaz" %% "scalaz-effect" % scalazVersion
libraryDependencies += "org.scalaz" %% "scalaz-concurrent" % scalazVersion
libraryDependencies += "com.typesafe.play" %% "play-json" % playJsonVersion
libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.11.0"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.2" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test

// testing configuration
fork in Test := true
parallelExecution := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
// Scalariform settings
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)

// enable updating file headers //
// enable updating file headers //
organizationName := "Dennis Vriend"
startYear := Some(2017)
licenses := Seq(("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")))
headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.CppStyleLineComment)

// https://github.com/scalamacros/paradise
// http://docs.scala-lang.org/overviews/macros/paradise.html
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

enablePlugins(SbtScalariform)

initialize ~= { _ =>
  val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
  if (ansi) System.setProperty("scala.color", "true")
}

initialCommands in console := """
import scalaz._, Scalaz._
import org.scalacheck._
import org.scalacheck.Prop.forAll
val numbers = Gen.chooseNum(Long.MinValue, Long.MaxValue)
import scala.concurrent._
import scala.collection.immutable._
import scala.reflect.runtime.universe._
import scala.concurrent.ExecutionContext.Implicits.global
"""