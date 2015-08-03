package ohnosequences.nisperon

import ohnosequences.nisperon.logging.S3Logger

trait InstructionsAux {

  type I

  type O

  type Context

 // val arity: Int

  def solve(input: I, logger: S3Logger, context: Context): List[O]

  def prepare(): Context
}

  trait Instructions[Input, Output] extends InstructionsAux {


  type I = Input

  type O = Output

}

//results with one message
abstract class MapInstructions[Input, Output] extends Instructions[Input, Output] {

  //val arity = 1

  def solve(input: I, logger: S3Logger, context: Context): List[Output] = {
    List(apply(input, logger, context))
  }

  def apply(input: Input, logger: S3Logger, context: Context): O

}

