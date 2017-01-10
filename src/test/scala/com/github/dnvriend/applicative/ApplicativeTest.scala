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

package com.github.dnvriend.applicative

import com.github.dnvriend.TestSpec

import scalaz._
import Scalaz._
import scala.language.postfixOps

class ApplicativeTest extends TestSpec {
  // from: http://eed3si9n.com/learning-scalaz/Applicative.html

  // applicative operations and symbols are all related to applying a function

  // let's build up to Applicatives
  it should "build up the problem" in {
    def methodThatTakesTwoParameters(x: Int, y: Int) = x * y
    val f2: Function2[Int, Int, Int] = methodThatTakesTwoParameters _ // (Int, Int) => Int
    val functionThatTakesTwoParameters: (Int, Int) => Int = (_: Int) * (_: Int)
    // notice that we have a function with 2 parameters, we cannot use that let's curry it:
    val curriedFunction: Int => Int => Int = functionThatTakesTwoParameters.curried
    // notice, the function looks more how Haskell defines functions that take multiple parameters
    // we can now apply the values 1, 2, 3 to the curried function; it will return a function Int => Int
    val functionsInContext: List[Int ⇒ Int] = List(1, 2, 3) map curriedFunction
    // we now have functions in a context that we can apply eg. with the value '9':
    functionsInContext.map(_(9)) shouldBe List(9, 18, 27)
  }

  it should "point - from the Applicative type class" in {
    // The Applicative type class defines the methods 'pure/point'
    // 'pure' takes a value and puts it in a minimal context that still yields that value
    // The 'context' constructor has been absracted
    1.point[List] shouldBe
      List(1)

    1.point[Option] shouldBe
      Some(1)

    1.point[Option].map(_ |+| 2) shouldBe
      Some(3)

    1.point[List].map(_ |+| 2) shouldBe
      List(3)
  }

  it should "<*> - from the Applicative type class" in {
    // <*> is a beefed-up map.
    // 'map' takes a function and a functor, and applies the function inside the functor value
    // '<*>' takes a functor that has a function in it, and another functor with a value in it,
    //    1. extracts that function from the first functor,
    //    2. and then maps it over the second one

    // legend:
    // <* returns the lhs (left-hand-side)
    (None <* None) shouldBe
      None

    (1.some <* None) shouldBe
      None

    (None <* 2.some) shouldBe
      None

    (1.some <* 2.some) shouldBe
      1.some

    val f = (_: Int) + 2
    (1.some <* f.some) shouldBe
      1.some

    // legend:
    // *> returns the rhs (right-hand-side)
    (None *> None) shouldBe
      None

    (1.some *> None) shouldBe
      None

    (None *> 2.some) shouldBe
      None

    (1.some *> 2.some) shouldBe
      2.some

    (1.some *> f.some) shouldBe
      Option(f)

    // legend:
    // <*> combining their results by function application
    (1.some <*> f.some) shouldBe
      3.some

    val curriedSumFunction: Int ⇒ Int ⇒ Int = ((_: Int) + (_: Int)).curried
    val appliedCurriedSumFunction: Option[Int ⇒ Int] = 9.some <*> curriedSumFunction.some
    (3.some <*> appliedCurriedSumFunction) shouldBe
      12.some
  }

  it should "apply" in {
    val appendOne = (x: String) => x |+| "one"
    List("a", "b", "c") <*> appendOne.point[List] shouldBe List("aone", "bone", "cone")
  }

  it should "ApplicativeStyle" in {
    // legend:
    // '^' extracts values from two containers and apply them to a single function
    // It is a symbol for apply2 from the 'Apply' type class
    // This is useful for the 1 function case that we don't need to put inside a container

    // The type signature is the following:
    // (Option[Int], Option[Int]) ((A, B) => C)
    ^(3.some, 5.some)(_ |+| _) shouldBe
      8.some

    ^(3.some, none[Int])(_ |+| _) shouldBe
      None

    // legend:
    // '^^' does the same but then for three x
    ^^(1.some, 2.some, 3.some)(_ |+| _ |+| _) shouldBe
      6.some

    // legend:
    // '^^^' does the same but then for four applicatives
    ^^^(1.some, 2.some, 3.some, 4.some)(_ |+| _ |+| _ |+| _) shouldBe
      10.some

    // legend:
    // '^^^^' .. five applicatives
    ^^^^(1.some, 2.some, 3.some, 4.some, 5.some)(_ |+| _ |+| _ |+| _ |+| _) shouldBe
      15.some

    // legend:
    // '^^^^^' .. six applicatives
    ^^^^^(1.some, 2.some, 3.some, 4.some, 5.some, 6.some)(_ |+| _ |+| _ |+| _ |+| _ |+| _) shouldBe
      21.some

    // legend:
    // '^^^^^^' .. seven applicatives
    ^^^^^^(1.some, 2.some, 3.some, 4.some, 5.some, 6.some, 7.some)(_ |+| _ |+| _ |+| _ |+| _ |+| _ |+| _) shouldBe
      28.some

    // legend:
    // '|@|' is a symbol for constructing Applicative expressions.
    // (f1 |@| f2 |@| ... |@| fn)((v1, v2, ... vn) => ...)
    // (f1 |@| f2 |@| ... |@| fn).tupled

    (3.some |@| 5.some)(_ |+| _) shouldBe
      8.some

    (3.some |@| 4.some |@| 5.some)(_ |+| _ |+| _) shouldBe
      12.some

    (3.some |@| 4.some |@| 5.some |@| 6.some)(_ |+| _ |+| _ |+| _) shouldBe
      18.some

    (List(1, 2, 3) |@| List(4, 5, 6))(_ |+| _) shouldBe
      List(5, 6, 7, 6, 7, 8, 7, 8, 9)

    val xs = ('A' to 'Z').map(_.toString).toList
    val cartesianTwo = (xs |@| xs)(_ |+| _)

    cartesianTwo.size shouldBe
      26 * 26

    cartesianTwo.head shouldBe
      "AA"

    cartesianTwo(26 * 26 - 1) shouldBe
      "ZZ"

    val cartesianThree = (xs |@| xs |@| xs)(_ |+| _ |+| _)

    cartesianThree.size shouldBe
      26 * 26 * 26

    cartesianThree.head shouldBe
      "AAA"

    cartesianThree(26 * 26 * 26 - 1) shouldBe
      "ZZZ"
  }

  it should "tuple applicative operation" in {
    (Option(1) tuple Option(2)) shouldBe
      Option((1, 2))
    (List(1, 2) tuple List(3, 4)) shouldBe
      List((1, 3), (1, 4), (2, 3), (2, 4))
  }
}
