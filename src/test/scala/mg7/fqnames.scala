package ohnosequences.test.mg7

import ohnosequences.test.mg7.testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.statika._, aws._

class QFNTest extends org.scalatest.FunSuite {

  test("Fully-qualified names") {

    info(s"beimock: ${BeiMock.pipeline.fullName}")

    assert {
       ohnosequences.test.mg7.BeiMock.pipeline.flash.fullName ==
      "ohnosequences.test.mg7.BeiMock.pipeline.flash"
    }

    assert {
       ohnosequences.test.mg7.BeiMock.pipeline.flash.manager.fullName ==
      "ohnosequences.test.mg7.BeiMock.pipeline.flash.manager"
    }
  }
}
