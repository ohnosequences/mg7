package ohnosequences.test.mg7

import ohnosequences.test.mg7.testDefaults._
import ohnosequences.mg7._, loquats._
import ohnosequences.statika._, aws._

class QFNTest extends org.scalatest.FunSuite {

  test("Fully-qualified names") {

    info(s"beimock: ${BeiMock.pipeline.toString}")
    info(s"beimock: ${BeiMock.pipeline.flash.manager.fullName}")
  }
}
