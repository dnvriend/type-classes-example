package com.github.dnvriend

import play.api.libs.json.{ Format, Json }

import scalaz._
import Scalaz._

object Person {
  implicit val format: Format[Person] = Json.format[Person]

  def validateNonEmpty(fieldName: String, value: String): ValidationNel[String, String] =
    Validation.liftNel(value)(_.trim.isEmpty, s"Field '$fieldName' must not be empty")

  def validateName(name: String): ValidationNel[String, String] =
    validateNonEmpty("name", name)

  def validateGt(fieldName: String, maxValue: Int, value: Int): ValidationNel[String, Int] =
    Validation.liftNel(value)(_ <= maxValue, s"Field '$fieldName' with value '$value' must be greater than '$maxValue'")

  def validateLt(fieldName: String, maxValue: Int, value: Int): ValidationNel[String, Int] =
    Validation.liftNel(value)(_ > maxValue, s"Field '$fieldName' with value '$value' must be less than '$maxValue'")

  def validateAge(age: Int): ValidationNel[String, Int] =
    validateGt("age", -1, age) *> validateLt("age", 140, age)

  def validate(name: String, age: Int): ValidationNel[String, Person] =
    (validateName(name) |@| validateAge(age))(Person.apply)
}
case class Person(name: String, age: Int)