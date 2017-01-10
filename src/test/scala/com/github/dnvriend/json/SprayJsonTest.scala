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

package com.github.dnvriend.json

import spray.json._
import com.github.dnvriend.{ Person, TestSpec }

class SprayJsonTest extends TestSpec {
  it should "marshal a product type" in {
    Person("foo", 42).toJson.compactPrint shouldBe """{"name":"foo","age":42}"""
  }

  it should "unmarshal to product type" in {
    """{"name":"foo","age":42}""".parseJson.convertTo[Person] shouldBe Person("foo", 42)
  }
}
