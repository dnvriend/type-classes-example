# type-classes-example
A small study project on type classes, level of entry is 'beginner'.

## Using the example
Just clone the project:

```bash
git clone git@github.com:dnvriend/type-classes-example.git
```

Enter the directory and launch sbt.

## Launching the test
To launch the tests:

```bash
sbt test
```

## Launch the REPL
To launch the REPL with [scalaz](https://github.com/scalaz/scalaz), [simulacrum](https://github.com/mpilquist/simulacrum),
[play-json](https://github.com/playframework/play-json) and [akka-http-spray-json](https://github.com/akka/akka-http)
on the classpath so you can use the abstractions of said libraries:

```bash
sbt console
```

## Type Classes
The following is taken from the fantastic book: [The Type Astronaut's Guide to Shapeless - Underscore](https://github.com/underscoreio/shapeless-guide).

Type classes are a programming pattern borrowed from the programming language [Haskell](https://www.haskell.org/).
The word 'class' has nothing to do with classes in object oriented programming.

We encode type classes in Scala using traits and implicits. A type class is a parameterised trait
representing some general functionality that we would like to apply to a wide range of types:

```scala
trait CsvEncoder[A] {
  def encode(value: A): List[String]
}
```

We implement our type class with instances for each type we care about. If we want the instances to automatically be in scope
we can place them in the type class’ companion object. Otherwise we can place them in a separate library object for the
user to import manually:


```
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait CsvEncoder[A] {
  def encode(value: A): List[String]
}

case class Person(name: String, age: Int, married: Boolean)

object Person {
  implicit val encoder = new CsvEncoder[Person] {
    override def encode(person: Person): List[String] =
      List(
        person.name,
        person.age.toString,
        if(person.married) "yes" else "no"
      )
  }
}

def writeCsv[A](values: List[A])(implicit encoder: CsvEncoder[A]): String =
  values.map(value => encoder.encode(value).mkString(",")).mkString("\n")

val people = List(Person("Foo", 42, false), Person("Bar", 25, true))

writeCsv(people)

// Exiting paste mode, now interpreting.

defined trait CsvEncoder
defined class Person
defined object Person
writeCsv: [A](values: List[A])(implicit encoder: CsvEncoder[A])String
people: List[Person] = List(Person(Foo,42,false), Person(Bar,25,true))
res5: String =
Foo,42,no
Bar,25,yes
```

When we call writeCsv, the compiler calculates the value of the type parameter and searches for an implicit CsvEncoder
of the corresponding type which in this case is CsvEncoder[Person]. We can use writeCsv with any data type we like,
provided we have a corresponding implicit CsvEncoder in scope:

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

case class Address(street: String, houseNumber: Int, zipcode: String)
object Address {
  implicit val encoder = new CsvEncoder[Address] {
    override def encode(address: Address): List[String] =
      List(
        address.street,
        address.houseNumber.toString,
        address.zipcode
      )
  }
}

val addresses = List(Address("FooStreet", 1, "1000AA"), Address("BarStreet", 1, "1500AB"))

writeCsv(addresses)

// Exiting paste mode, now interpreting.

defined class Address
defined object Address
addresses: List[Address] = List(Address(FooStreet,1,1000AA), Address(BarStreet,1,1500AB))
res6: String =
FooStreet,1,1000AA
BarStreet,1,1500AB
```

## Idiomatic Type Class Definitions
The following is taken from the fantastic book: [The Type Astronaut's Guide to Shapeless - Underscore](https://github.com/underscoreio/shapeless-guide).

The commonly accepted idiomatic style for type class definitions starts with a parameterized trait that defines some
general functionality that we would like to apply to a wide range of types.

The next thing developers that create type classes often do is create a companion object that contains some standard methods
that provide the following functionality:

- an 'apply' method that is known as a 'summoner' or 'materializer' that when called returns a type class instance of a given target type,
- an 'instance' method (that is sometimes named 'pure') that provides a terse syntax for creating new type class instances thus reducing
  writing boilerplate code

```scala
object CsvEncoder {
  // The 'summoner' or 'materializer' method, when called with a propert type returns a type class instance
  def apply[A](implicit enc: CsvEncoder[A]): CsvEncoder[A] = enc
  // The 'constructor' method that helps us writing less code when defining type class instances
  def instance[A](f: A => List[String]): CsvEncoder[A] = (value: A) => f(value)
}
```

When using this convention our example will look like:

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait CsvEncoder[A] {
  def encode(value: A): List[String]
}

object CsvEncoder {
  // "Summoner" method
  def apply[A](implicit enc: CsvEncoder[A]): CsvEncoder[A] = enc
  // "Constructor" method
  def instance[A](f: A => List[String]): CsvEncoder[A] = (value: A) => f(value)
  // Globally visible type class instances
}

case class Person(name: String, age: Int, married: Boolean)

object Person {
  implicit val encoder = CsvEncoder.instance[Person] { person =>
    List(
      person.name,
      person.age.toString,
      if(person.married) "yes" else "no"
    )
  }
}

def writeCsv[A](values: List[A])(implicit encoder: CsvEncoder[A]): String =
  values.map(value => encoder.encode(value).mkString(",")).mkString("\n")

val people = List(Person("Foo", 42, false), Person("Bar", 25, true))


case class Address(street: String, houseNumber: Int, zipcode: String)
object Address {
  implicit val encoder = CsvEncoder.instance[Address] { address =>
    List(
      address.street,
      address.houseNumber.toString,
      address.zipcode
    )
  }
}

val addresses = List(Address("FooStreet", 1, "1000AA"), Address("BarStreet", 1, "1500AB"))

val peopleCsv = writeCsv(people)
val addressCsv = writeCsv(addresses)

// Exiting paste mode, now interpreting.

defined trait CsvEncoder
defined object CsvEncoder
defined class Person
defined object Person
writeCsv: [A](values: List[A])(implicit encoder: CsvEncoder[A])String
people: List[Person] = List(Person(Foo,42,false), Person(Bar,25,true))
defined class Address
defined object Address
addresses: List[Address] = List(Address(FooStreet,1,1000AA), Address(BarStreet,1,1500AB))
peopleCsv: String =
Foo,42,no
Bar,25,yes
addressCsv: String =
FooStreet,1,1000AA
BarStreet,1,1500AB
```

## Type classes and libraries
Type classes is a pattern that is also being used by library designers so you will see the pattern used in Scala libraries
as well. For example, the [play-json](https://github.com/playframework/play-json) library uses type classes the same way
as the example above to convert a type but then of course to JSON.

To use play-json, we must first add a dependency to that library so we can use it from our source code:

```
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M1"
```

Play-json uses the `play.api.libs.json.Format[A]` type class to define a generic way to convert types to a
`play.api.libs.json.JsValue`. JsValue is the most abstract definition of what play-json defines being a JsonValue.
Because JsValue a sealed trait, JsValue defines an enumeration of what other types a Json-encoded type consists of.

So play-json can convert any type A to a JsValue (A => JsValue), but only when there is a type class defined
that encapsulates how that should be done.

Play-json already knows how to convert a JsValue to String (JsValue => String) and that is easy:

```scala
def writeJson[A](value: A)(implicit format: Format[A]): String =
  Json.toJson(value).toString
```

So the only thing we need is a type class called 'Format' of the types we care to convert to Json and we can
serialize those types to a Json encoded String.

We can use the same strategy as we did with converting to a CSV so we can add a type class instance to the
companion object of Person and of Address so we can convert those types into a JsValue:

```scala
import play.api.libs.json._

case class Person(name: String, age: Int, married: Boolean)

object Person {
  implicit val format = Json.format[Person]
}
```

We can now write a Person to Json:

```scala
scala> writeJson(Person("foo", 42, false))
res0: String = {"name":"foo","age":42,"married":false}
```

The syntax to create a type class instance for 'Format' is very terse, that is because we are using a macro that has been
provided by play-json that creates the necessary source code for use to be able to convert a Person into Json.

We can also convert a Json encoded String back to a Person case class with the same type class:

```scala
scala> Json.parse("""{"name":"foo","age":42,"married":false}""").as[Person]
res2: Person = Person(foo,42,false)
```

The example below shows how we can convert a list of types to Json

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

import play.api.libs.json._

case class Person(name: String, age: Int, married: Boolean)

object Person {
  implicit val format = Json.format[Person]
}

val people = List(Person("Foo", 42, false), Person("Bar", 25, true))

case class Address(street: String, houseNumber: Int, zipcode: String)
object Address {
  implicit val format = Json.format[Address]
}

val addresses = List(Address("FooStreet", 1, "1000AA"), Address("BarStreet", 1, "1500AB"))

def writeJson[A](value: A)(implicit format: Format[A]): String =
  Json.toJson(value).toString

val peopleJson = writeJson(people)
val addressJSon = writeJson(addresses)

// Exiting paste mode, now interpreting.

import play.api.libs.json._
defined class Person
defined object Person
people: List[Person] = List(Person(Foo,42,false), Person(Bar,25,true))
defined class Address
defined object Address
addresses: List[Address] = List(Address(FooStreet,1,1000AA), Address(BarStreet,1,1500AB))
writeJson: [A](value: A)(implicit format: play.api.libs.json.Format[A])String
peopleJson: String = [{"name":"Foo","age":42,"married":false},{"name":"Bar","age":25,"married":true}]
addressJSon: String = [{"street":"FooStreet","houseNumber":1,"zipcode":"1000AA"},{"street":"BarStreet","houseNumber":1,"zipcode":"1500AB"}]
```

To parse a list of people:

```scala
scala> Json.parse("""[{"name":"Foo","age":42,"married":false},{"name":"Bar","age":25,"married":true}]""").as[List[Person]]
res4: List[Person] = List(Person(Foo,42,false), Person(Bar,25,true))
```

To parse a list of addresses:

```scala
scala> Json.parse("""[{"street":"FooStreet","houseNumber":1,"zipcode":"1000AA"},{"street":"BarStreet","houseNumber":1,"zipcode":"1500AB"}]""").as[List[Address]]
res5: List[Address] = List(Address(FooStreet,1,1000AA), Address(BarStreet,1,1500AB))
```

## Ad-hoc polymorphism
Ad-hoc polymorphism is an alternative to sub-type polymorphism and is a way to convert a type `A` into another type `B`
without the types having the knowledge of convertyping one type into another and vice versa. Instead a third 'adapter' class
will be used that converts type `A` into type `B`. This pattern can be easily implemented using the adapter-pattern
in which we can isolate all the transformation logic in the adapter.

When implemented in Scala using a combination of parameterized trait, implicit parameter and currying, the 'adapter' can
automatically be injected when an appropriate type is needed. When thinking more generic terms, such adapters abstract
over types and can be thought a grouping of general functionality that can be applied to a range of types. This pattern is called
the 'type class' pattern.

## Simulacrum
The type class pattern is used to define some general functionality that we would like to apply to a wide range of types.
The Scala programming language has no first class support for this pattern, which is a shame.

As we have seen, to define a type class we must first know about the type class pattern and then know about the idiomatic way (the commonly agreed upon way)
how to encode them using a combination of trait, companion object and some methods that give us a way to work with them effectively.
Wouldn't it be nice if Scala had first class support for type classes?

Yes it would, but as of Scala 2.12.1 there is still __no__ first class language support for type classes. There is however a
project called [Simulacrum](https://github.com/mpilquist/simulacrum) that adds first class syntax support for type classes
to the Scala language.

When you add simulacrum to your project, you get a very concise way to define type classes and type class instances that
leads to the same implementation (idiomatic way).

First we must alter our `build.sbt` file and add the following lines:

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.10.0"
```

The biggest change for us as a developer when using simulacrum is that we just have to define a parameterized trait
and define the generic functionality and we don't have to know the (current) idiomatic way how to define the
companion object of the trait. The companion object of the trait will be generated for us, which means that
we now have first class support for type classes in Scala which is great!

We can now define a case class like so:

```scala
import simulacrum._

@typeclass trait CsvEncoder[A] {
  def encode(value: A): List[String]
}
```

The compiler plugin will generate a companion object for us containing all the necessary helper methods that makes
working with type classes easy. Of course, we must still provide type class instances for the types we care about that,
if possible, we can put in the companion object of the type we wish to convert.

For example:

```scala
import simulacrum._

@typeclass trait CsvEncoder[A] {
  def encode(value: A): List[String]
}

case class Person(name: String, age: Int, married: Boolean)

object Person {
  implicit val encoder = new CsvEncoder[Person] {
    override def encode(person: Person): List[String] = List(
      person.name,
      person.age.toString,
      if(person.married) "yes" else "no"
    )
  }
}

CsvEncoder[Person].encode(Person("foo", 42, true))

def writeCsv[A](values: List[A])(implicit encoder: CsvEncoder[A]): String =
 values.map(value => encoder.encode(value).mkString(",")).mkString("\n")

val people: List[Person] = List(Person("foo", 42, true), Person("bar", 25, false))

val peopleCsv: String = writeCsv(people)
```

## Haskell Type Classes
There is another functional programming language besides Scala that uses type classes which is [the Haskell programming language](https://www.haskell.org/).
Type classes are a great pattern because they provide general functionality across types. As we have seen we can define any
functionality that we wish to provide for a group of types by just isolating that functionality in a trait and give an
implementation for that functionality. So if we would like to serialize a type to CSV, we can isolate that functionality
in a type class, or if we would like to serialize to JSON, we can isolate that functionality in a type class and so on.

But type classes are not limited to only serializing types. We can isolate __any__ functionality in type classes. For example,
we could choose to provide a way to convert a type into a textual representation by isolating that functionality into
a type class or if we would like to add two types like say two numbers, we can isolate that functionality in a type class.

The standard Haskell libraries feature a number of type classes with algebraic or category-theoretic underpinnings but the
library also provides some technical type classes like the type class `Show`, lets look at that type class.

## Haskell Show
Show is a type class for conversion to textual representation.

```scala
import simulacrum._

@typeclass trait Show[A] {
  def show(value: A): String
}
```

We can use the Show type class to provide a way to convert a type into a textual representation. The only thing we must
do is provide an instance of the type class for example:

```scala
import simulacrum._

@typeclass trait Show[A] {
  def show(value: A): String
}

case class Person(name: String, age: Int)
object Person {
  implicit val show = new Show[Person] {
    override def show(person: Person): String = {
      import person._
      s"""Person(name=$name, age=$age)"""
    }
  }
}

// we can now 'summon' a type class instance of Show[Person] by just typing
// Show[Person], that is a feature that has been created by simulacrum.
Show[Person].show(Person("foo", 42))
```

## Scalaz Type Classes
[Scalaz](https://github.com/scalaz/scalaz) is an extension to the core Scala library for functional programming. Scalaz
contains type classes that have been heavily inspired by the [Haskell Typeclassopedia] and those type classes are mostly
based on algebraic or category-theoretic underpinnings.

Apart from the algebraic or category-theoretic type classes Scalaz has other type classes for example Equal that
provides a typesafe alternative to test equality.

![Haskell Typeclassopedia](/img/typeclassopedia.png)

## Combining, combining, combining...
Functional programming is all about solving a problem by breaking down / decomposing a problem is very small problems
and solve those small problems using functions. To be able to solve the big problem we must combine everything using
combinators. Ehenever possible we must use functions to solve problems because you can easily combine/compose
functions.

For example, to solve a problem like validating a list of input values we can do the following breakdown:

- aggregate values in a List,
- transform those values into an effect eg Validation
- test whether there are errors
  - if there are errors, give me the errors
  - if there are no errors, give me the sum of the values

We could do it as follows:

```scala
import scalaz._
import Scalaz._

scala> def validateInput(input: String): ValidationNel[String, Int] = input.parseInt.leftMap(_.toString).toValidationNel
validateInput: (input: String)scalaz.ValidationNel[String,Int]

scala> List("1").map(validateInput).sequenceU.map(_.sum)
res0: scalaz.Validation[scalaz.NonEmptyList[String],Int] = Success(1)

scala> List("1", "2").map(validateInput).sequenceU.map(_.sum)
res1: scalaz.Validation[scalaz.NonEmptyList[String],Int] = Success(3)

scala> List("a", "b").map(validateInput).sequenceU.map(_.sum)
res2: scalaz.Validation[scalaz.NonEmptyList[String],Int] = Failure(NonEmpty[java.lang.NumberFormatException: For input string: "a",java.lang.NumberFormatException: For input string: "b"])
```

We have validated two strings and when they are numbers we get the Success sum of those numbers, else the
Failure with all the failures.

We can solve this problem a bit shorter by using a Monoid and a utility method of Scalaz.

There exists a Monoid[ValidationNel[String, Int]] instance:

```scala
scala> Monoid[ValidationNel[String, Int]]
res0: scalaz.Monoid[scalaz.ValidationNel[String,Int]] = scalaz.ValidationInstances0$$anon$5@324f3f84
```

There is a convenience method on List that can use this monoid to combine the contents of the validation
using that monoid instance:

```scala
import scalaz._
import Scalaz._

scala> def validateInput(input: String): ValidationNel[String, Int] = input.parseInt.leftMap(_.toString).toValidationNel
validateInput: (input: String)scalaz.ValidationNel[String,Int]

scala> List("1", "2").map(validateInput).suml
res0: scalaz.ValidationNel[String,Int] = Success(3)

scala> List("a", "2").map(validateInput).suml
res1: scalaz.ValidationNel[String,Int] = Failure(NonEmpty[java.lang.NumberFormatException: For input string: "a"])
```

The suml does a left fold and uses a Monoid instance for the element type of the list. We start out with
a List[String], then List[ValidationNel[String, Int]]. The `suml` method needs a Monoid[ValidationNel[String, Int]] and
when one is found then it will be used to sum up all the Ints of the List[ValidationNel[String, Int]].

When no Monoid[ValidationNel[String, Int]] is found, then the whole program won't even compile, because of Scala implicits.

## Making things simple
Scalaz does not only provide a bunch of type classes that are heavily inspired by the type classes as defined by the
Haskell programming language, it also provides a lot of convenience methods that make working with type classes and
combining these type classes with data structures very useful. The learning curve is knowing the which convenience
methods there are, which type classes there are, and when it is logical to combine a type class with a convencience method
and a data structure for solving a problem.

Using type classes from standard libraries from for example Scalaz promotes standardization, developers start to recognize
structures and over time makes the code very simple to read and for the Java runtime very easy to optimize.

Apart from the standardization and recognition, breaking down a problem is reusable parts like Semigroups, Monoids, Functors
for example promotes reusable components because the type classes provide a very general abstraction because for most
type classes you need a function f: A => B to operate on them, this means that the type class most of the time is unbiased
about __what__ you are doing (the function) but is biased about the context of the computation (Validation, JsonFormat, Option) and so on.

## Algebra
Types are at the center of programming. A type is defined by the operations it provides and the laws of these operations. The laws
of the operations is called the algebra of the type. When we think about the types we know like Int, Double or String we know
that these types support arithmic operations like +, -, *, / and %. Moreover, most types share exactly the same behavior and that
is a property that we can take advantage of in generic programming. We can now focus on the rules or rather the algebra of the computation.

### Function
A function is a structure that relates input to output. In the scala programming language, a function can be defined
as something that can be evaluated on demand, so each and every time we want to compute a value, we can apply a
function.

In scala that is very easy to define:

```scala
scala> def addOne(x: Int): Int = x + 1
addOne: (x: Int)Int

scala> addOne(1)
res0: Int = 2
```

We can use some syntactic sugar to transform a method to a function:

```scala
scala> val f = addOne _
f: Int => Int = $$Lambda$1056/1003737182@6aba5d30

scala> f(1)
res1: Int = 2
```

Scala has very concise syntax for defining functions for example:

```scala
scala> val g = (_: Int) + 1
g: Int => Int = $$Lambda$1059/1021656938@2d82408

scala> g(1)
res2: Int = 2
```

But at the other end of the spectrum, we can use the very verbose and
technical way of defining a function:

```scala
scala> val h = new Function1[Int, Int] {
     | override def apply(x: Int): Int = x + 1
     | }
h: Int => Int = <function1>

scala> h(1)
res3: Int = 2
```

__Technical side note:___
As you can see, I am using the Scala 2.12 REPL. You can see that for all but the last example, Scala
has optimized the function to be a Java8-lambda, which is a very resource optimized way for defining and
executing Functions. When explicitly creating a function using the Function(N) trait, we are explicitly
creating functions and those cannot be optimized to use lambdas.

### Promoting methods to functions
In the Scala programming language it is possible to _promote a method to a function_ by means of delegation which means
a function is being created that will call our method. [Scala supports this using a process called 'method values'](http://scala-lang.org/files/archive/spec/2.12/06-expressions.html#method-values).
Methods can be promoted to a function because a method basically does the same as a function in that it relates
input to output and does this on demand. Of course, a plain method isn't a function, but Scala can promote a plain method
to a function.

But we need to define something extra here. Methods are often used in combination with objects to provide something called
the 'universal access principle' and to provide encapsulation so it has everything to do with keeping state.
So that is not the 'method-type' I am referring to; not a class method.

Scala can provide methods in a static context, and those methods operate in a stateless context. When defining methods
on a 'module', which is a way of strucuring/organizing code, you can group related methods (and values/constants), under a
given name. Those methods can be 'converted' to a function for example:

```scala
scala> object Foo {
     | val Pi: Double = 3.1415926
     | def addOne(x: Int): Int = x + 1
     | }
defined object Foo

scala> Foo.addOne _
res0: Int => Int = $$Lambda$1331/111131743@5a8dfd2e
```

We have created a module (an organizational unit) called 'Foo' using the keyword 'object' that makes it possible to
have a stable reference to the module 'Foo' that contains values and methods. Methods defined in such a module can
be promoted to functions using a process called 'exa-expansion' and is described in the [Scala language specification - Method Values](http://scala-lang.org/files/archive/spec/2.12/06-expressions.html#method-values).

So Scala provides several ways to create functions from 'eta-expanding' a method to a function literal.

### Domain
A [domain](http://www.mathsisfun.com/sets/domain-range-codomain.html) is all the values that __can go into__ a function.

For example, when we have the function like the example above, the domain are all the values from Int.MinValue and Int.MaxValue
inclusive. This range of values can best be expressed as [sets](http://www.mathsisfun.com/sets/sets-introduction.html),
which is a unique collection of values.

Sets can be written as:

```
{ ... , -3, -2, -1, 0, 1, 2, 3, ... }
```

Or in Scala:

```
scala> Set(-3, -2, -1, 0, 1, 2, 3)
res0: scala.collection.immutable.Set[Int] = Set(0, -3, 1, 2, 3, -1, -2)
```

Even better is to use the [scala.collection.immutable.Range](http://www.scala-lang.org/api/2.12.1/scala/collection/immutable/Range$.html)
to define a domain.

```scala
scala> Range.inclusive(-3, 3).toList
res1: List[Int] = List(-3, -2, -1, 0, 1, 2, 3)
```

We can define a domain using `scala.collection.immutable.Range` and apply the function to the domain to get
something that is called a `Range` in math terms, or in plain english, 'all the values that come out of a function
when the function is applied on a certain domain'. We will use the `.map` method to apply the function to the domain:

```scala
scala> Range.inclusive(-3, 3).map(addOne)
res2: scala.collection.immutable.IndexedSeq[Int] = Vector(-2, -1, 0, 1, 2, 3, 4)
```

To conclude the discussion, the co-domain is the range `Range.inclusive(Int.MinValue, Int.MaxValue)` so all the possible values
that may come out of the function or in technical terms, the result type of the function which is `Int`.

### Range
A [Range](http://www.mathsisfun.com/sets/domain-range-codomain.html) is all the values that come out of a function when
the function is applied on a certain domain.

### Codomain
A [codomain](http://www.mathsisfun.com/sets/domain-range-codomain.html) is all the values what __may possibly come out__ of a function.
In Scala, the range is defined by the return type of a function/method

### Drawing domain, range and codomain
Domain, range and codomain are each defined as a Set eg. `{ ..., -3, -2, -1, 0, 1, 2, 3, ... }` and these sets can be drawn
using an oval in where you draw the values that do into that set. The circle contains the name of the set.

Between the sets there is an arrow representing a domain object that has been applied to the function and must point to
the resuling range. The range is the output of the function and is another oval containing only the values that is the result
of applying the function to the domain and is often called Y.

For an explanation read: [Domain, Range and Codomain](http://www.mathsisfun.com/sets/domain-range-codomain.html)

## Semigroup
A Semigroup is a pure algebraic structure that is defined by the Semigroup laws. Scalaz provides a type called [scalaz.Semigroup](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Semigroup.scala),
which provides an associative binary operation. Scalaz not only provides the type class but also instances of Semigroup for most
standard scala types and Scalaz types like Validation that conform to the Semigroup laws.

A semigroup is a set of 'A' together with a binary operation `def append(left: A, right: A): A` with symbol `|+|`
which combines elements from A. The `|+|` operator is required to be associative.

A semigroup in type A must satisfy two laws:

- __closure__: '∀ a, b in F, append(a, b)' is also in 'F'. This is enforced by the type system.
- __associativity___: '∀ a, b, c` in F, the equation 'append(append(a, b), c) = append(a, append(b , c))' holds.

For example, the natural numbers under addition form a semigroup: the sum of any two natural numbers is a natural number,
and (a+b)+c = a+(b+c) for any natural numbers a, b, and c,.

```scala
scala> val s = Semigroup[Int]
s: scalaz.Semigroup[Int] = scalaz.std.AnyValInstances$$anon$5@537d2634

scala> s.append(1, s.append(2, 3)) == s.append(s.append(1, 2), 3)
res0: Boolean = true
```

The integers under multiplication also form a semigroup, as do the integers, Boolean values under conjunction and disjunction,
lists under concatenation, functions from a set to itself under composition.

Semigroups show up all over the place, once you know to look for them.

```scala
import scalaz._
import Scalaz._

scala> Semigroup[Int].append(1, 1)
res0: Int = 2

scala> 1 |+| 1
res1: Int = 2

scala> Semigroup[String].append("a", "b")
res2: String = ab

scala> "a" |+| "b"
res3: String = ab

scala> 1 |+| "1"
<console>:18: error: type mismatch;
 found   : String("1")
 required: Int
       1 |+| "1"

```

## Associative Functions
Semigroups are associative functions:

```
f(a, f(b, c)) == f(f(a, b), c)
```

## Monoid
A Monoid is a pure algebraic structure that is defined by the monoid laws. Scalaz provides a type called [scalaz.Monoid](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Monoid.scala),
which provides an associative binary operation with an identity element ('zero'). Scalaz not only provides the type class but also instances of Monoid for most
standard scala types and Scalaz types like Validation that conform to the Monoid laws.

Many semigroups have a special element 'zero' for which the binary operation `def append(left: A, right: A): A` with symbol `|+|` is the identity.
Such a _semigroup-with-identity-element_ is called a monoid.

Monoid instances must satisfy the semigroup law and 2 additional laws:

- __left identity__: 'forall a. append(zero, a) == a'
- __right identity__: 'forall a. append(a, zero) == a'

which translates to:

```scala
scala> Monoid[Int].append(Monoid[Int].zero, 1) == Monoid[Int].append(1, Monoid[Int].zero)
res2: Boolean = true
```

What can we do with Monoids?
- parallel computation,
- build complex calculations from small pieces by combining them

When you look for a Monoid, you'll find it everywhere!

```scala
import scalaz._
import Scalaz._

scala> Monoid[Int].append(1, 2)
res0: Int = 3

scala> Monoid[Int].multiply(20, 5)
res1: Int = 100

scala> Monoid[Int].isMZero(0)
res2: Boolean = true

scala> Monoid[Int].isMZero(1)
res3: Boolean = false

scala> Monoid[String].zero
res4: String = ""

scala> Monoid[Int].zero
res5: Int = 0

scala> Monoid[List[Int]].zero
res6: List[Int] = List()

scala> Monoid[Map[String, String]].zero
res7: Map[String,String] = Map()

scala> Monoid[String].multiply("a", 10)
res8: String = aaaaaaaaaa
```

## 'About Those Monoids' by Eugene Yokota
As you can see from the examples above, a Monoid is something that:

- has a binary operation; so it is something like addition or multiplication,
- the operands of the operation is of the same type; 1 + 1 where the (1) is an operand and both operands are of the same type,
- the operation returns a value of the same type as the operands; so 1 + 1 = 2, the operation returns the value (2) which is of the same type as the operands.
- the monoid has a property called 'zero' that is of the same type as the operands and the return value but the value of zero is chosen so that, when applying the operation on the operands, and one of the operands equals the zero value, the return value is equal to the other operands value.

In practise it seems that both '* together with 1' ,'+ together with 0' and '++ along with List()', share some common properties:

> The function takes two parameters. - The parameters and the returned value have the same type. - There exists such a value that doesn’t change other values when used with the binary function.

```scala
// multiplication
scala> 4 * 1
res0: Int = 4

scala> 1 * 9
res1: Int = 9

// list concatenation
scala> List(1, 2, 3) ++ List()
res2: List[Int] = List(1, 2, 3)

scala> List() ++ List(1, 2, 3)
res3: List[Int] = List(1, 2, 3)

// addition
scala> 0 + 1
res4: Int = 1

scala> 1 + 0
res5: Int = 1

// string concatenation
scala> "" + "foo"
res6: String = foo

scala> "foo" + ""
res7: String = foo

// set with their union
scala> Set(1, 2, 3) ++ Set(2,4)
res8: scala.collection.immutable.Set[Int] = Set(1, 2, 3, 4)

scala> true && true
res9: Boolean = true

scala> true && false
res10: Boolean = false

scala> false && true
res11: Boolean = false
```

> It doesn’t matter if we do (3 * 4) * 5 or 3 * (4 * 5). Either way, the result is 60. The same goes for ++.
> We call this property associativity. * is associative, and so is ++, but -, for example, is not.

The property associativity can be defined in a rule and we can use [ScalaCheck](https://www.scalacheck.org/)
to create property objects that we can use to test it:

```scala
// we must first import the scalacheck classes
scala> import org.scalacheck._
import org.scalacheck._

// we must define a Generator because we will generate some numbers
// and apply the assertion with random numbers to test whether
// or not the invariant holds:
scala> val numbers = Gen.chooseNum(Long.MinValue, Long.MaxValue)
numbers: org.scalacheck.Gen[Long] = org.scalacheck.Gen$$anon$1@129c7443

// we define the association rule in a method
scala> def associationRule(x: Long, y: Long, z: Long): Boolean = x * (y * z) == (x * y) * z
associationRule: (x: Long, y: Long, z: Long)Boolean

// we will create an object called a 'Property' or 'Prop' for short, and we need
// another import for that
scala> import org.scalacheck.Prop.forAll
import org.scalacheck.Prop.forAll

// here we use some Scala syntactic sugar to convert the
// associationRule method to a function of (Long, Long, Long) => Boolean
//
// The forAll() methods needs three generators, so three times our
// numbers generator so the function can be applied
scala> forAll(numbers, numbers, numbers)(associationRule)
res0: org.scalacheck.Prop = Prop

// of course we can write it all in explicit style:
scala> forAll(numbers, numbers, numbers)((x: Long, y: Long, z: Long) => associationRule(x, y, z))
res1: org.scalacheck.Prop = Prop

// we can now test the property
scala> res1.check
+ OK, passed 100 tests.
```

## So what is a Monoid?
A monoid is when you have an associative binary function and a value which acts as an identity with respect to that function.

The binary operation that the monoid supports is `append` or symbolic `|+|`, which It takes two values of the same type and returns a value of that type.

The `identity` value of Monoid is called `zero` in Scalaz:

```scala
scala> Monoid[Int].zero
res0: Int = 0

// sure enough, when applying the zero of the Monoid to the 'plus' operation
// we get a '1' back, which holds true for every value?
scala> 1 + Monoid[Int].zero
res1: Int = 1

import org.scalacheck._
import org.scalacheck.Prop.forAll

scala> val numbers = Gen.chooseNum(Long.MinValue, Long.MaxValue)
numbers: org.scalacheck.Gen[Long] = org.scalacheck.Gen$$anon$1@63d2f26c

scala> forAll(numbers)(x => x + Monoid[Long].zero == x)
res2: org.scalacheck.Prop = Prop

// yes it does!
scala> res2.check
+ OK, passed 100 tests.
```

## Monoids and Multiplication
Scalaz returns a Monoid with a zero for addition when we use the Monoid[Long] summoner, and that zero is of value '0',
but for multiplication we need an identity value of 1. How do we instruct Scalaz to return a Monoid for multiplication?

We must use a Tagged type:

```scala
scala> Monoid[Long @@ Tags.Multiplication].zero
res0: scalaz.@@[Long,scalaz.Tags.Multiplication] = 1

// so we can do the following multiplication
scala> Tags.Multiplication(10) |+| Monoid[Int @@ Tags.Multiplication].zero
res1: scalaz.@@[Int,scalaz.Tags.Multiplication] = 10

// note, when we want to use the 'addition' Monoid we just write
// so we must wrap the left operand in a Tags.Multiplication when we
// want to do multiplication, else we dont.
scala> 10 |+| Monoid[Int].zero
res2: Int = 10
```

We can now do the following:

```scala
// 'add' stuff together using a Monoid that does multiplication
scala> def add[A](xs: List[A @@ Tags.Multiplication])(implicit m: Monoid[A @@ Tags.Multiplication]): A @@ Tags.Multiplication = xs.foldLeft(m.zero)(m.append(_, _))
add: [A](xs: List[scalaz.@@[A,scalaz.Tags.Multiplication]])(implicit m: scalaz.Monoid[scalaz.@@[A,scalaz.Tags.Multiplication]])scalaz.@@[A,scalaz.Tags.Multiplication]

// of course, when we call the method, it needs elements not of 'A' but of 'A @@ Multiplication' so we need to map
// all the elements
scala> add(List(1, 2, 3, 4).map(Tags.Multiplication(_)))
res0: scalaz.@@[Int,scalaz.Tags.Multiplication] = 24

// note that the result is tagged as Multiplication so we
// now that it is the result of a Multiplication
// We can unwrap the tagged value:
scala> val x: Int = Tag.unwrap(res0)
x: Int = 24
```

When you think about it, the Tag or '@@' annotation doesn't let us 'plug-in' the wrong Monoid in our computation. If
we could just replace an 'Addition' Monoid with a 'Multiplication' Monoid, we could accidentally get the wrong result
and as you see, the result wouldn't say anyting about the computation because with normal computation, the type
doesn't tell us anything about the computation.

I think the solution of Scalaz makes the computation explicit and I like that approach.

## Calculating factorials
In mathematics, the factorial of a non-negative integer n, denoted by n!, is the product of all positive integers less than or equal to n. For example:

```
5! = 5 * 4 * 3 * 2 * 1 = 120
```

__Note__: 0! means an 'empty product' and is by convention always equal to '1', so '0! == 1'

Some factorials:

```
0! = 1
1! = 1
2! = 2
3! = 6
4! = 24
5! = 120
6! = 720
```

We can calulate a factorial with our Multiplication Monoid when the contents of the collection contains all the elements of the factorial
so (1, 2, 3, 4, 5) without duplications:

```scala
scala> def factorial[A](xs: List[A @@ Tags.Multiplication])(implicit m: Monoid[A @@ Tags.Multiplication]): A @@ Tags.Multiplication = xs.foldLeft(m.zero)(m.append(_, _))
factorial: [A](xs: List[scalaz.@@[A,scalaz.Tags.Multiplication]])(implicit m: scalaz.Monoid[scalaz.@@[A,scalaz.Tags.Multiplication]])scalaz.@@[A,scalaz.Tags.Multiplication]

scala> factorial(List(1, 2, 3, 4, 5).map(Tags.Multiplication(_)))
res1: scalaz.@@[Int,scalaz.Tags.Multiplication] = 120
```

Calculating factorials can be done using recursion

```scala
def factorial(n: Int): Int =
  if(n == 0) 1
  else n * factorial(n - 1)

scala> factorial(5)
res0: Int = 120

// we can also use pattern matching
def factorial(n: Int): Int = n match {
  case 0 => 1
  case _ => n * factorial(n - 1)
}

scala> factorial(5)
res1: Int = 120

// factorial tail recursive

def factorial(accumulator: Int, number: Int): Int = {
  if(number == 1) accumulator
  else factorial(number * accumulator, number - 1)
}

factorial(1, 5)

// factorial using a fold
scala> List(5, 4, 3, 2, 1).foldLeft(1)(_ * _)
res1: Int = 120
```

## Monoids and fold
Monoids assure that the collection we want to fold doesn't need to guarantee order.

## Monoids and foldable collections
A Monoid can be used with the standard scala collections that support the `foldLeft` and `foldRight` functions for example
Seq, List, Set, Vector, Array, Map[K, V]. The foldLeft operation is left associative and foldRight operation is right associative.

Of course, Scalaz provides the [scalaz.Foldable](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Foldable.scala) type class and
Foldable type class instances that beside the foldLeft and foldRight methods, provides convenience methods that implicitly uses a Monoid to aggregate
results, which makes folding structures less verbose.

Because the monoid is associative, you get the same result whether you use foldLeft or foldRight.

```scala
// without using the Monoid, we must select an operation that we know is associative:
scala> List(1, 2, 3).foldLeft(0)(_ + _)
res0: Int = 6

// with the Monoid, we don't have to worry about the rules, when there is a Monoid of the supporting type
// then its proven to be associative (well, when you have tested the Monoid with the Monoid rules that is...)
// so we can just apply it on our collection
scala> List(1, 2, 3).foldLeft(Monoid[Int].zero)(Monoid[Int].append(_, _))
res1: Int = 6

// we can even define a generic fold method that can fold every 'A',
// when there is a Monoid[A] type class instance
scala> def fold[A](xs: List[A])(implicit m: Monoid[A]): A = xs.fold(m.zero)(m.append(_, _))
fold: [A](xs: List[A])(implicit m: scalaz.Monoid[A])A

scala> fold(List(1, 2, 3))
res2: Int = 6

// the type class Foldable provides a 'fold' method that does just that.
// Foldable 'folds' structures eg. a List structure to an A when there is a
// Monoid of A:
scala> Foldable[List].fold(List(1, 2, 3))
res3: Int = 6

// we can rewrite our generic fold method to:
// we need the context-bound syntax to get a Monoid[A] that will be used by
// the Foldable[A]
def fold[A: Monoid](xs: List[A])(implicit f: Foldable[A]): A = f.fold(xs)

scala> fold(List(1, 2, 3))
res4: Int = 6

// we can even make it more generic and fold every shape
// where we have a foldable for and a Monoid for the element type
scala> def fold[F[_], A: Monoid](xs: F[A])(implicit f: Foldable[F]): A = f.fold(xs)
fold: [F[_], A](xs: F[A])(implicit evidence$1: scalaz.Monoid[A], implicit f: scalaz.Foldable[F])A

scala> fold(List(1, 2, 3))
res5: Int = 6

scala> fold(Set(1, 2, 3))
res6: Int = 6

scala> fold(Vector(1, 2, 3))
res7: Int = 6

// Foldable lets us also map-and-combine in one step using a Monoid that should be available:

scala> List(1, 2, 3).foldMap(_ + 1)
res8: Int = 9

// or calling the Foldable type class explicit
scala> Foldable[List].foldMap(List(1, 2, 3))(_ + 1)
res9: Int = 9
```

## Equal
[scalaz.Equal](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Equal.scala): A type safe alternative to universal equality.

```scala
import scalaz._
import Scalaz._

scala> Equal[Int].equal(1, 1)
res0: Boolean = true

scala> Equal[String].equal("1", "2")
res1: Boolean = false

scala> Equal[String].equal("foo", "foof")
res2: Boolean = false

// the following test for equality
// using the standard 'double-equals-symbol'
// is an untyped equality test
scala> List(1, 2, 3) == "a"
res3: Boolean = false

// scalaz provides the '===' or 'triple-equals-symbol' that is
// a type-safe alternative to the standard 'double-equals-symbol'

scala> List(1, 2, 3) === "a"
<console>:28: error: type mismatch;
 found   : String("a")
 required: List[Int]
       List(1, 2, 3) === "a"

// The compiler agrees that testing a 'List' and 'String' for equality
// makes no sense and shouldn't even compile.

scala> List(1, 2, 3) === List(1, 2, 3)
res4: Boolean = true
```

## Order
[scalaz.Order](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Order.scala):

```scala
import scalaz._
import Scalaz._

scala> Order[Int].min(1, 2)
res0: Int = 1

scala> Order[Int].min(2,1)
res1: Int = 1

scala> Order[Int].order(1, 2)
res2: scalaz.Ordering = LT

scala> Order[Int].order(2, 1)
res3: scalaz.Ordering = GT

scala> Order[Int].order(2, 2)
res4: scalaz.Ordering = EQ

scala> Order[Int].sort(2,1)
res5: (Int, Int) = (1,2)

scala> Order[Int].sort(1, 2)
res6: (Int, Int) = (1,2)

scala> Order[List[Int]].order(List(1, 5, 2), List(1, 2, 5))
res7: scalaz.Ordering = GT
```

## Show
[scalaz.Show](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Show.scala): a type class for conversion to textual representation.

```scala
import scalaz._
import Scalaz._

scala> Show[Int].show(1)
res0: scalaz.Cord = 1

scala> Show[Int].shows(1)
res1: String = 1
```

## Foldable
[scalaz.Foldable](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Foldable.scala): provides ways to combine the elements of a list to a new result.

```scala
// looks for an implicit Monoid[Int] that defines the 'zero' and then folds the List[Int]

scala> Foldable[List].fold(List(1, 2, 3, 4))
res0: Int = 10

// first maps each element using the given function, then based on a Monoid which must
// be of type of the element type so here Monoid[Int], all the elements will be combined
scala> Foldable[List].foldMap(List(1, 2, 3))(_+1)
res1: Int = 9

// using a Monoid, here Monoid[Int], a the contents will be combined
// using a right-associative fold
scala> Foldable[List].sumr(List(1, 2, 3))
res2: Int = 6

// using a Monoid, here Monoid[Int], a the contents will be combined
// using a left-associative fold
scala> Foldable[List].suml(List(1, 2, 3))
res3: Int = 6

// Foldable provides some new operations
scala> List(1, 2, 3).concatenate
res4: Int = 6

// we can also sequence effects
scala> List(1.some, 2.some, 3.some).sequence
res5: Option[List[Int]] = Some(List(1, 2, 3))

scala> def validateNumber(str: String): ValidationNel[String, Int] =
     |   str.parseInt.leftMap(_.toString).toValidationNel
validateNumber: (str: String)scalaz.ValidationNel[String,Int]

scala> List("1", "2", "3").map(validateNumber)
res6: List[scalaz.ValidationNel[String,Int]] = List(Success(1), Success(2), Success(3))

scala> List("1", "2", "3").map(validateNumber).sequenceU
res7: scalaz.Validation[scalaz.NonEmptyList[String],List[Int]] = Success(List(1, 2, 3))

// or all in one go
scala> List("1", "2", "3").traverseU(validateNumber)
res8: scalaz.Validation[scalaz.NonEmptyList[String],List[Int]] = Success(List(1, 2, 3))

scala> List("foo", "bar", "baz").traverseU(validateNumber)
res9: scalaz.Validation[scalaz.NonEmptyList[String],List[Int]] = Failure(NonEmpty[java.lang.NumberFormatException: For input string: "foo",java.lang.NumberFormatException: For input string: "bar",java.lang.NumberFormatException: For input string: "baz"])
```

## Traverse
[scalaz.Traverse](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Traverse.scala): Provides operations for traversing tructures

## Apply
[scalaz.Apply](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Apply.scala): provides the 'app' method. Accepts a Functor and and Applicative Functor.

```scala
import scalaz._
import Scalaz._

scala> val applicativeFunctor = Option((_: Int) + 1)
applicativeFunctor: Option[Int => Int] = Some($$Lambda$2201/638605646@5648494f)

scala> Apply[Option].ap(1.some)(applicativeFunctor)
res0: Option[Int] = Some(2)
```

## Functor
[scalaz.Functor](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Functor.scala): provides a way to map over a value in a context.

```scala
import scalaz._
import Scalaz._

Functor[Option].map(1.some)(_ + 1)
res0: Option[Int] = Some(2)

scala> Functor[List].map(List(1, 2, 3))(_ + 1)
res1: List[Int] = List(2, 3, 4)

scala> Functor[NonEmptyList].map(NonEmptyList(1, 2, 3))(_ + 1)
res2: scalaz.NonEmptyList[Int] = NonEmpty[2,3,4]
```

__Digression:__
A very interesting read about functors [The Many Functions of Functor - PinealServo](http://pinealservo.com/posts/2014-10-22-ManyFunctionsOfFunctor.html)
that goes beyond the meaning of Functor in Category Theory and researches the word 'functor' in other contexts and provides some insight into
the history of the word.

__TL;DR:__
The word has been coined by [Rudolf Carnap (1891—1970)](https://en.wikipedia.org/wiki/Rudolf_Carnap) a German-born philosopher that
first used the word in the 1934 book [The Logical Syntax of Language](http://www.ams.org/journals/bull/1938-44-03/S0002-9904-1938-06694-3/S0002-9904-1938-06694-3.pdf).
After being coined, the word has been reused in category theory by Saunders Mac Lane et al. in 1945, so thats ten years after Carnap coined the word.

## Applicative
[scalaz.Applicative](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Applicative.scala): an Applicative Functor.

```scala
import scalaz._
import Scalaz._

scala> val applicativeFunctor = Option((_: Int) + 1)
applicativeFunctor: Option[Int => Int] = Some($$Lambda$2201/638605646@5648494f)

scala> Applicative[Option].ap(1.some)(applicativeFunctor)
res0: Option[Int] = Some(2)

scala> 1.some <*> applicativeFunctor
res1: Option[Int] = Some(2)

// put a value into a context
scala> Applicative[Option].point(1)
res2: Option[Int] = Some(1)

// put a value into a context
scala> Applicative[Option].pure(1)
res3: Option[Int] = Some(1)
```

## Monad
[scalaz.Monad](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Monad.scala): provides a way to compose multiple monads into one,
so in that sense its a monoid, and provides a way to sequence computation where a computation is dependent on the result of the previous computation.

```scala
import scalaz._
import Scalaz._

// I advice not to use .bind but stick with using 'flatMap'
scala> Monad[Option].bind(1.some)(x => Option(x + 1))
res0: Option[Int] = Some(2)

scala> 1.some >>= (x => Option(x + 1))
res1: Option[Int] = Some(2)

// I advice using the for-yield syntax for composing monads
// and not using '>>='
scala> for {
     | x <- 1.some
     | y <- Option(x + 1)
     | } yield y
res12 Option[Int] = Some(2)
```

__Digression:__
- [Monad](https://en.wikipedia.org/wiki/Monad_(philosophy)) (from Greek 'Monas'): means 'unit' or 'alone'
refers in creation theory to the first being, divinity, or the totality of all beings.
The concept was reportedly conceived by the Pythagoreans and may refer variously to a single source acting alone
and/or an indivisible origin. The concept was later adopted by other philosophers, such as
[Leibniz](https://en.wikipedia.org/wiki/Gottfried_Wilhelm_Leibniz).

## Terms
- Auto (Greek): means 'self'
- Iso (Greek): means 'equal'
- Homos (Greek): means 'same'
- Endos (Greek): means 'inside'
- Morph (Greek): means 'form' or 'shape'
- Morphism (Greek): 'to form' or 'to shape'
- Cata (Greek): means 'downwards' or 'according to'
- Monad (from Greek 'Monas'): means 'unit' or 'alone' refers to the first being, divinity or the totality of all beings
- [Isomorphism](http://mathworld.wolfram.com/Isomorphism.html): Iso='equal' and Morphism='to shape': An [Isomorphism](https://en.wikipedia.org/wiki/Isomorphism)
- [Automorphism](http://mathworld.wolfram.com/Automorphism.html): Auto='self', Morphism='to shape': An [Automorphism](https://en.wikipedia.org/wiki/Automorphism)
- [Homomorphism](http://mathworld.wolfram.com/Homomorphism.html): Homos='same', Morphism='to shape': [Homomorphism](https://en.wikipedia.org/wiki/Homomorphism)
- [Catamorphism](https://wiki.haskell.org/Catamorphisms): Cata='downwards', Morphism='to shape': An [Catamorphisms](https://en.wikipedia.org/wiki/Catamorphism)
- [Endomorphism](http://mathworld.wolfram.com/Endomorphism.html): Endon='inside', Morphism='to shape': An [Endomorphism](https://en.wikipedia.org/wiki/Endomorphism) of a group is a homomorphism from one object to itself.

## YouTube
- [(0'05 hr) Is Math Discovered or Invented - Jeff Dekofsky](https://www.youtube.com/watch?v=X_xR5Kes4Rs)
- [(0'34 hr) (for laughs and learning!) Functional Programming is Terrible - Rúnar Bjarnason](https://www.youtube.com/watch?v=hzf3hTUKk8U)
- [(0'29 hr) Typeclasses in Scala - Dan Rosen](https://www.youtube.com/watch?v=sVMES4RZF-8)
- [(0'31 hr) Introduction to Scalaz - Heiko Seeberger](https://www.youtube.com/watch?v=HW8Cl5-pGlk)
- [(0'29 hr) Learning Scalaz - Eugene Yokota](https://www.youtube.com/watch?v=jyMIvcUxOJ0)
- [(0'54 hr) Scalaz the good parts - Shimi Bandiel](https://www.youtube.com/watch?v=jPdHQZnF56A)
- [(1'29 hr) Scalaz for the Rest of Us - Adam Rosien](https://www.youtube.com/watch?v=kcfIH3GYXMI)
- [(0'41 hr) Scala Typeclassopedia - John Kodumal](https://www.youtube.com/watch?v=IMGCDph1fNY)
- [(0'10 hr) Introduction to Scalaz and Typeclasses - Michele Sciabarra](https://www.youtube.com/watch?v=A63yuSWrxEY)
- [(0'40 hr) From Simulacrum to Typeclassic - Michael Pilquist](https://www.youtube.com/watch?v=Crc2RHWrcLI)
- [(0'26 hr) Life After Monoids - Tom Switzer](https://www.youtube.com/watch?v=xO9AoZNSOH4)
- [(0'41 hr) Monads - Katie Miller](https://www.youtube.com/watch?v=MlZCiiKGbb0)
- [(1'07 hr) Don't fear the Monad - Brian Beckman](https://www.youtube.com/watch?v=ZhuHCtR3xq8)
- [(2'03 hr) Scalaz State Monad - Michael Pilquist](https://www.youtube.com/watch?v=Jg3Uv_YWJqI)
- [(1'06 hr) The Zen of Stateless State - The State Monad - Brian Beckman](https://www.youtube.com/watch?v=XxzzJiXHOJs)
- [(0'32 hr) The Reader Monad for Depencency Injection - Jason Arhart](https://www.youtube.com/watch?v=xPlsVVaMoB0)
- [(0'27 hr) Reader Monad & Free Monad - Rúnar Bjarnason](https://www.youtube.com/watch?v=ZasXwtTRkio)
- [(0'31 hr) Property Based Testing - Amanda Laucher](https://www.youtube.com/watch?v=uF_m6lCQTIs)


## Resources
- [Type Typeclassopedia - Slides](http://typeclassopedia.bitbucket.org/)
- [The Type Astronaut's Guide to Shapeless - Underscore](https://github.com/underscoreio/shapeless-guide)
- [learning Scalaz - Eugene Yokota](http://eed3si9n.com/learning-scalaz/7.0/)
- [Learning Scalaz - Monoids - Eugene Yokota](http://eed3si9n.com/learning-scalaz/Monoid.html)
- [Simulacrum - Michael Pilquist](https://github.com/mpilquist/simulacrum)
- [The Neophyte's Guide to Scala Part 12: Type Classes - Daniel Westheide](http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html)
- [Demystifying Implicits and Typeclasses in Scala - Cake Solutions](http://www.cakesolutions.net/teamblogs/demystifying-implicits-and-typeclasses-in-scala)
- [Scalaz and Typeclasses - Michele Sciabarra](http://michele.sciabarra.com/2015/11/11/scala/Scalaz-and-Typeclasses/)
- [The Haskell Typeclassopedia](https://wiki.haskell.org/Typeclassopedia)
- [The Road to the Typeclassopedia - Channing Walton](http://channingwalton.github.io/typeclassopedia/)
- [Effective Scala - Twitter](http://twitter.github.io/effectivescala/)
- [Monoids Applied - Susan Potter](http://www.slideshare.net/mbbx6spp/functional-algebra-monoids-applied)
- [Monoids for Programmers - A Scala Example - Vlad Patryshev](https://www.safaribooksonline.com/blog/2013/05/15/monoids-for-programmers-a-scala-example/)
- [Aggregators: modeling data queries functionally - Oscar Boykin](https://speakerdeck.com/johnynek/aggregators-modeling-data-queries-functionally)
- [Higher Order - Philosophy and functional programming](http://blog.higher-order.com/)
- [Higher Order - Monoid Morphisms, Products, and Coproducts - ](http://blog.higher-order.com/blog/2014/03/19/monoid-morphisms-products-coproducts/)
- [Of Algebirds, Monoids, Monads, and Other Bestiary for Large-Scale Data Analytics - Michael G. Noll](http://www.michael-noll.com/blog/2013/12/02/twitter-algebird-monoid-monad-for-large-scala-data-analytics/)
- [How Much One Ought to Know Eta Expansion - Jacek Laskowski](http://blog.jaceklaskowski.pl/2013/11/23/how-much-one-ought-to-know-eta-expansion.html)
- [Scala Language Specification - Method Values](http://scala-lang.org/files/archive/spec/2.12/06-expressions.html#method-values)
- [Methods are not Functions - Rob Norris](http://tpolecat.github.io/2014/06/09/methods-functions.html)
- [The Many Functions of Functor - PinealServo](http://pinealservo.com/posts/2014-10-22-ManyFunctionsOfFunctor.html)

## Books
- [Functional programming in Scala - Rúnar Bjarnason](https://www.manning.com/books/functional-programming-in-scala)
- [Scala Design Patterns - Ivan Nikolov](https://www.packtpub.com/application-development/scala-design-patterns)
- [Learn you a Haskell for great good](http://learnyouahaskell.com/)

## Github
- [My tinkering to understand the typeclassopedia - Channing Walton](https://github.com/channingwalton/typeclassopedia)