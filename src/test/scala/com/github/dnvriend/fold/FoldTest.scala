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

package com.github.dnvriend.fold

import com.github.dnvriend.TestSpec

import scalaz._
import Scalaz._

class FoldTest extends TestSpec {

  // the word 'catamorphism' comes (from Greek: kata = downwards or according to and morphism = form or shape)
  //
  // In functional programming, 'catamorphisms' provide generalizations of folds of lists
  // to arbitrary algebraic data types.
  //
  // The dual of 'catamorphism' is 'anamorphism' which is a concept that generalizes 'unfolds'
  //
  // To fold over data structures (or fold-up a data structure), scalaz provides the 'Foldable' type class.
  //

  def withList(xs: List[Int] = List(1, 2, 3))(f: List[Int] => Unit) = f(xs)

  def withListAndTc(f: List[Int] => Foldable[List] => Unit): Unit =
    withList()(xs => f(xs)(implicitly[Foldable[List]]))

  it should "to" in withList() { xs =>
    Foldable[List].to[Int, Vector](xs) shouldBe Vector(1, 2, 3)
  }

  it should "maximum" in withList() { xs =>
    Foldable[List].maximum(xs) shouldBe 3.some
  }

  it should "maximum with type class" in withListAndTc { xs => tc =>
    tc.maximum(xs) shouldBe 3.some
  }

  it should "fold the list using a Monoid" in withList() { xs =>
    def foldList[A: Monoid](xs: List[A]): A =
      Foldable[List].fold(xs)

    def sumRight[A: Monoid](xs: List[A]): A =
      Foldable[List].sumr(xs)

    def sumLeft[A: Monoid](xs: List[A]): A =
      Foldable[List].suml(xs)

    Foldable[List].fold(xs)(implicitly[Monoid[Int]]) shouldBe 6
    foldList(xs) shouldBe 6
    sumRight(xs) shouldBe 6
    sumLeft(xs) shouldBe 6
  }
}
