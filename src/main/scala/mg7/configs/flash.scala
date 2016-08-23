package ohnosequences.mg7

import ohnosequences.loquat._
import ohnosequences.cosas._, types._, klists._
import ohnosequences.datasets._
import ohnosequences.flash.api._


trait AnyFlashConfig extends AnyMG7LoquatConfig {

  /* Data processing parameters */

  val readsLength: illumina.Length

  // TODO: should it be configurable?
  lazy val flashOptions = flash.defaults.update(
    read_len(readsLength.toInt) ::
    max_overlap(readsLength.toInt) ::
    *[AnyDenotation]
  )
}
