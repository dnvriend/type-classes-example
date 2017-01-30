
trait Command
case class CreatePerson(id: Long, name: String, age: Int) extends Command
case class ChangeName(id: Long, name: String) extends Command
case class ChangeAge(id: Long, age: Int) extends Command
case class DeletePerson(id: Long) extends Command

trait Event
case class PersonCreated(id: Long, name: String, age: Int) extends Event
case class NameChanged(id: Long, oldName: String, newName: String) extends Event
case class AgeChanged(id: Long, oldAge: Int, newAge: Int) extends Event
case class PersonDeleted(id: Long) extends Event

case class Person(id: Long, name: String, age: Int, deleted: Boolean)

import scalaz._
import Scalaz._

def handleEvent(event: Event): State[Option[Person], Unit] = State { maybePerson =>
  event match {
    case PersonCreated(id, name, age) => (Option(Person(id, name, age, deleted = false)), ())
    case NameChanged(id, _, newName) => (maybePerson.map(_.copy(name = newName)), ())
    case AgeChanged(id, _, newAge) => (maybePerson.map(_.copy(age = newAge)), ())
    case PersonDeleted(id) => (maybePerson.map(_.copy(deleted = true)), ())
  }
}

def handleCommand(command: Command): State[Option[Person], Event] = State { maybePerson =>
  command match {
    case CreatePerson(id, name, age) => (Option(Person(id, name, age, deleted = false)), PersonCreated(id, name, age))
    case ChangeName(id, name) => (maybePerson.map(_.copy(name = name)), NameChanged(id, maybePerson.map(_.name).getOrElse(""), name))
    case ChangeAge(id, age) => (maybePerson.map(_.copy(age = age)), AgeChanged(id, maybePerson.map(_.age).getOrElse(0), age))
    case DeletePerson(id) => (maybePerson.map(_.copy(deleted = true)), PersonDeleted(id))
  }
}

val eventLog: List[Event] = List(
  PersonCreated(1, "John Doe", 42),
  NameChanged(1, "John Doe", "Jane Doe"),
  AgeChanged(1, 42, 22),
  PersonDeleted(1)
)

val (personState, _) = eventLog.traverseS(handleEvent).run(Option.empty[Person])

val commandLog: List[Command] = List(
  CreatePerson(1, "John Doe", 42),
  ChangeName(1, "Jane Doe"),
  ChangeAge(1, 22),
  DeletePerson(1)
)

val (otherPersonState, events) = commandLog.traverseS(handleCommand).run(Option.empty[Person])