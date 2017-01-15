import scalaz._
import Scalaz._

def addToList(x: Int): State[List[Int], Unit] =
  State[List[Int], Unit](xs => (x :: xs, ()))

//
def listMutationComposition: State[List[Int], Unit] = for {
  _ <- addToList(1)
  _ <- addToList(2)
  _ <- addToList(3)
  r <- addToList(4)
} yield r

val result: (List[Int], _) =
  listMutationComposition.run(List())

trait Event
case class NameAltered(name: String) extends Event
case class AgeAltered(age: Int) extends Event

case class Person(name: String, age: Int)

def handleEvent(e: Event): State[Person, Unit] = State { person =>
    e match {
      case NameAltered(name) => (person.copy(name = name), ())
      case AgeAltered(age) => (person.copy(age = age), ())
    }
}

def manipPerson: State[Person, Unit] = for {
  _ <- handleEvent(NameAltered("Dennis"))
  _ <- handleEvent(AgeAltered(42))
} yield ()

manipPerson(Person("", 0))






















//import scalaz._
//import Scalaz._
//
//trait Event
//case class PersonCreated(name: String, age: Int) extends Event
//case class NameAltered(name: String) extends Event
//case class AgeAltered(age: Int) extends Event
//
//case class Person(name: String, age: Int)
//
//def handleEvent(e: Event): State[Option[Person], Unit] = State {
//  case maybePerson => e match {
//    case PersonCreated(name, age) => (Option(Person(name, age)), ())
//    case NameAltered(name) => (maybePerson.map(_.copy(name = name)), ())
//    case AgeAltered(age) => (maybePerson.map(_.copy(age = age)), ())
//  }
//}
//
//val xs: List[Event] =
//  List(
//    PersonCreated("Dennis", 42),
//    NameAltered("Foo"),
//    AgeAltered(42),
//    AgeAltered(43),
//    NameAltered("Bar"),
//    AgeAltered(44)
//  )
//
//val (person, _) = xs.traverseS(handleEvent).run(none[Person])
//
