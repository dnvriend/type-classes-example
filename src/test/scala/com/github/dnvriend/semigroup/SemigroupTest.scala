/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.semigroup

import com.github.dnvriend.TestSpec

import scalaz.Semigroup
import scalaz.syntax.semigroup._

object MySemigroups {
  implicit val appendSemigroup = new Semigroup[Int] {
    override def append(f1: Int, f2: ⇒ Int): Int = f1 + f2
  }

  implicit val productSemigroup = new Semigroup[Int] {
    override def append(f1: Int, f2: ⇒ Int): Int = f1 * f2
  }
}

class SemigroupTest extends TestSpec {
  // A Semigroup is a weaker version of Monoid

  // A Semigroup, like all functional structures, must obey certain laws.
  // Fortunately for us, the Semigroup has only one law, which is 'Associativity'.
  //
  // 'Associativity' means: It doesn’t matter if we do (3 * 4) * 5 or 3 * (4 * 5). Either way, the result is 60.
  // So in general, it doesn't matter where we put the parentheses.
  // This property is called 'Associativity'.
  //
  // A thing to note is that not all operators are * is associative, for example the operator '*' is
  // associative, and so is '+', but '-', for example, is not.
  //
  // associativity: (x |+| y) |+| z = x |+| (y |+| z)
  //

  it should "Semigroup with explicitly imported append semigroup" in {
    implicit val appendSemi: Semigroup[Int] = MySemigroups.appendSemigroup
    appendSemi.append(1, 2) shouldBe 3

    1 |+| 2 shouldBe 3
  }

  it should "Semigroup with explicitly imported product semigroup " in {
    implicit val productSemi: Semigroup[Int] = MySemigroups.productSemigroup
    productSemi.append(3, 3) shouldBe 9

    3 |+| 3 shouldBe 9
  }

  it should "Semigroup with default implementation for append using import scalaz.Scalaz._" in {
    import scalaz.std.AllInstances._
    Semigroup[Int].append(1, 2) shouldBe 3
  }

  it should "Semigroup using scalaz syntax classes" in {
    import scalaz.std.AllInstances._
    (1 mappend 2) shouldBe 3

    (1 |+| 2) shouldBe 3
  }

  it should "Appending lists" in {
    import scalaz.std.AllInstances._
    (List(1, 2, 3) |+| List(4, 5, 6)) shouldBe List(1, 2, 3, 4, 5, 6)
  }

  it should "Appending maps" in {
    import scalaz.std.AllInstances._
    (Map(1 → "x") |+| Map.empty) shouldBe Map(1 → "x")

    (Map(1 → "x") |+| Map(2 → "y")) shouldBe Map(1 → "x", 2 → "y")

    (Map(1 → "x") |+| Map(1 → "x")) shouldBe Map(1 → "xx")

    (Map(2 → "y") |+| Map(2 → "y")) shouldBe Map(2 → "yy")

    (Map(1 → "x", 2 → "y") |+| Map(1 → "x", 2 → "y")) shouldBe Map(1 → "xx", 2 → "yy")

    (Map(1 → List(1)) |+| Map(1 → List(2))) shouldBe Map(1 → List(1, 2))
  }

  it should "Appending Map + Option[A]" in {
    import scalaz.std.AllInstances._
    Map(1 → "x") ++ Some(2 → "y") shouldBe Map(1 → "x", 2 → "y")

    Map(1 → "x") ++ Some(1 → "y") shouldBe Map(1 → "y")

    Map(1 → "x") ++ None shouldBe Map(1 → "x")

    (Map(1 → "x") |+| Map(1 → "x")) shouldBe Map(1 → "xx")
  }
}