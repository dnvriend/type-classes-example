# type-classes-example
A small study project on type classes, level of entry is 'beginner'.

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

```
```

## Semigroup
[scalaz.Semigroup](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Semigroup.scala): an associative binary operation

A semigroup in type F must satisfy two laws:

- __closure__: '∀ a, b in F, append(a, b)' is also in 'F'. This is enforced by the type system.
- __associativity___: '∀ a, b, c` in F, the equation 'append(append(a, b), c) = append(a, append(b , c))' holds.

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

We could add two `Person` types with a Semigroup:

```scala
import scalaz._
import Scalaz._
case class Person(name: String, age: Int)
object Person {
  implicit val semi = new Semigroup[Person] {
    override def append(p1: Person, p2: => Person): Person =
      Person(p1.name |+| p2.name, p1.age |+| p2.age)
  }
}

scala> Person("a", 42) |+| Person("b", 42)
res0: Person = Person(ab,84)
```

## Monoid
[scalaz.Monoid](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Monoid.scala): An associative binary operation with an identity element ('zero')

Monoid instances must satisfy [[scalaz.Semigroup.SemigroupLaw]] and 2 additional laws:

- __left identity__: 'forall a. append(zero, a) == a'
- __right identity__: 'forall a. append(a, zero) == a'

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
[scalaz.Functor](https://github.com/scalaz/scalaz/blob/v7.2.8/core/src/main/scala/scalaz/Functor.scala): provides a way to map over a value in a context

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

## YouTube
- [(0'29 hr) Typeclasses in Scala - Dan Rosen](https://www.youtube.com/watch?v=sVMES4RZF-8)
- [(0'31 hr) Introduction to Scalaz - Heiko Seeberger](https://www.youtube.com/watch?v=HW8Cl5-pGlk)
- [(0'29 hr) Learning Scalaz - Eugene Yokota](https://www.youtube.com/watch?v=jyMIvcUxOJ0)
- [(0'54 hr) Scalaz the good parts - Shimi Bandiel](https://www.youtube.com/watch?v=jPdHQZnF56A)
- [(1'29 hr) Scalaz for the Rest of Us - Adam Rosien](https://www.youtube.com/watch?v=kcfIH3GYXMI)
- [(0'41 hr) Scala Typeclassopedia - John Kodumal](https://www.youtube.com/watch?v=IMGCDph1fNY)
- [(0'10 hr) Introduction to Scalaz and Typeclasses - Michele Sciabarra](https://www.youtube.com/watch?v=A63yuSWrxEY)
- [(0'40 hr) From Simulacrum to Typeclassic - Michael Pilquist](https://www.youtube.com/watch?v=Crc2RHWrcLI)


## Resources
- [Type Typeclassopedia - Slides](http://typeclassopedia.bitbucket.org/)
- [The Type Astronaut's Guide to Shapeless - Underscore](https://github.com/underscoreio/shapeless-guide)
- [learning Scalaz - Eugene Yokota](http://eed3si9n.com/learning-scalaz/7.0/)
- [Simulacrum - Michael Pilquist](https://github.com/mpilquist/simulacrum)
- [The Neophyte's Guide to Scala Part 12: Type Classes - Daniel Westheide](http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html)
- [Demystifying Implicits and Typeclasses in Scala - Cake Solutions](http://www.cakesolutions.net/teamblogs/demystifying-implicits-and-typeclasses-in-scala)
- [Scalaz and Typeclasses - Michele Sciabarra](http://michele.sciabarra.com/2015/11/11/scala/Scalaz-and-Typeclasses/)
- [The Road to the Typeclassopedia - Channing Walton](http://channingwalton.github.io/typeclassopedia/)

## Github
- [My tinkering to understand the typeclassopedia - Channing Walton](https://github.com/channingwalton/typeclassopedia)