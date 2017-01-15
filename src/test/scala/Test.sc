import scalaz._
import Scalaz._

def push(x: Int): State[List[Int], Unit] =
  State[List[Int], Unit](xs => (x :: xs, ()))

//
def listManip: State[List[Int], Unit] = for {
  _ <- push(1)
  _ <- push(2)
  r <- push(3)
} yield r

val result: (List[Int], Unit) =
  listManip.apply(List())



//def manipPerson: State[Person, Unit] = for {
//  _ <- handleEvent(NameAltered("Dennis"))
//  _ <- handleEvent(AgeAltered(42))
//} yield ()
//
//manipPerson(Person("", 0))






















import scalaz._
import Scalaz._

trait Event
case class NameAltered(name: String) extends Event
case class AgeAltered(age: Int) extends Event

case class Person(name: String, age: Int)

def handleEvent(e: Event): State[Person, Unit] = State {
  case person => e match {
    case NameAltered(name) => (person.copy(name = name), ())
    case AgeAltered(age) => (person.copy(age = age), ())
  }
}

val xs: List[Event] =
  List(
    NameAltered("Foo"),
    AgeAltered(42),
    AgeAltered(43),
    NameAltered("Bar"),
    AgeAltered(44)
  )

val (person, _) = xs.traverseS(handleEvent).run(Person("", 0))

