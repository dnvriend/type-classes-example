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

package com.github.dnvriend.traverse

import com.github.dnvriend.TestSpec

import scalaz._
import Scalaz._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class TraverseTest extends TestSpec {

  // see: http://stackoverflow.com/questions/26602611/how-to-understand-traverse-traverseu-and-traversem

  it should "sequence with some(s)" in {
    // An easy to understand first step in using the Traverse TypeClass
    // is the sequence operation, which given a Traverse[F] and
    // Applicative[G] turns F[G[A]] into G[F[A]].  This is like "turning
    // the structure 'inside-out'"
    //
    // sequence is used to gather together 'applicative effects'. More concretely, it lets you "flip" F[G[A]] to G[F[A]],
    // provided G is Applicative and F is Traversable. So we can use it to "pull together" a bunch of Applicative effects
    // (note all Monads are Applicative):
    val list1: List[Option[Int]] = List(Some(1), Some(2), Some(3), Some(4))
    list1.sequence shouldBe
      Some(List(1, 2, 3, 4))

    List(1, 2, 3).some.sequence shouldBe
      List(Some(1), Some(2), Some(3))

    List(1, 2, 3).some.sequence shouldBe
      List(1.some, 2.some, 3.some)

    NonEmptyList(1, 2, 3).some.sequence shouldBe
      NonEmptyList(1.some, 2.some, 3.some)

    // Future.sequence, although it is not using the scalaz.Traverse type class, also turns
    // the List[Future] structure inside-out Future[List
    Await.result(Future.sequence(List(Future.successful(1), Future.successful(2), Future.successful(3))), 1.second) shouldBe
      List(1, 2, 3)
  }

  it should "sequence with none" in {
    val list2: List[Option[Int]] = List(Some(1), Some(2), None, Some(4))
    list2.sequence shouldBe
      None
  }

  it should "double sequence" in {
    val list1: List[Option[Int]] = List(Some(1), Some(2), Some(3), Some(4))
    list1.sequence shouldBe
      Some(List(1, 2, 3, 4))

    list1.sequence.sequence shouldBe list1
  }

  it should "traverse" in {
    // A next step in using the Traverse TypeClass is the traverse
    // method. The traverse method maps function over a structure
    // through the effects of the inner applicative. You can think of
    // this method as combining a map with a sequence, so when you find
    // yourself calling fa.map(f).sequence, it can be replaced with just
    // fa.traverse(f)
    //
    // traverse is equivalent to 'map' then 'sequence', so you can use it when you have
    // a function that returns an Applicative and you want to just get a single instance
    // of your Applicative rather than a list of them:

    def fetchPost(postId: Int): Future[String] = Future.successful("x")
    val xx: Future[List[String]] = List(1, 2).traverse(fetchPost)
    //Fetch each post, but we only want an overall `Future`, not a `List[Future]`
  }

  it should "traverseU" in {
    // traverseU is the same operation as traverse,
    // just with the types expressed differently
    // so that the compiler can infer them more easily.
    def fetchPost(postId: Int): Future[String] = Future.successful("x")
    val xx: Future[List[String]] = List(1, 2).traverseU(fetchPost)
    //

    def logConversion(s: String): Writer[Vector[String], Int] =
      s.toInt.set(Vector(s"Converted $s"))
    List("4", "5").traverseU(logConversion): Writer[Vector[String], List[Int]]
    // = List("4", "5").map(logConversion).sequence
    // = List(4.set("Converted 4"), 5.set("Converted 5")).sequence
    // = List(4, 5).set(Vector("Converted 4", "Converted 5"))
  }

  it should "traverseM" in {
    // traverseM(f) is equivalent to traverse(f).map(_.join),
    // where 'join' is the scalaz name for flatten.
    // It's useful as a kind of "lifting flatMap":
    def multiples(i: Int): Future[List[Int]] =
      Future.successful(List(i, i * 2, i * 3))
    List(1, 10).map(multiples): List[Future[List[Int]]] //hard to work with
    List(1, 10).traverseM(multiples): Future[List[Int]]
    // = List(1, 10).traverse(multiples).map(_.flatten)
    // = List(1, 10).map(multiples).sequence.map(_.flatten)
    // = List(Future.successful(List(1, 2, 3)), Future.successful(List(10, 20, 30)))
    //     .sequence.map(_.flatten)
    // = Future.successful(List(List(1, 2, 3), List(10, 20, 30))).map(_.flatten)
    // = Future.successful(List(1, 2, 3, 10, 20, 30))
  }
}
