package ohnosequences.metagenomica

import ohnosequencesBundles.statika.blastAPI._
import java.io.File

case object instructions {

  abstract class BlastCommand(
    val refDB: File,
    val input: File,
    val output: File,
    val blastnOptions: List[AnyBlastOption]
  )
  {

    // TODO blastn -task megablast -max_target_seqs 1 -outfmt 6 -show_gis
    val cmd = blastCmd(
      blastn,
      blastnOptions ++ List[AnyBlastOption](db(refDB), query(input), out(output))
    )
  }
}
