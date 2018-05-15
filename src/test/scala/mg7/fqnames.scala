package ohnosequences.test.mg7

class QFNTest extends org.scalatest.FunSuite {

  test("Fully-qualified names") {

    info(s"beimock: ${mock.illumina.pipeline.fullName}")

    assert {
       ohnosequences.test.mg7.mock.illumina.pipeline.flash.fullName ==
      "ohnosequences.test.mg7.mock.illumina.pipeline.flash"
    }

    assert {
       ohnosequences.test.mg7.mock.illumina.pipeline.flash.manager.fullName ==
      "ohnosequences.test.mg7.mock.illumina.pipeline.flash.manager"
    }
  }
}
