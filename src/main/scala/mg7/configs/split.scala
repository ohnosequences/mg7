package ohnosequences.mg7

trait AnySplitConfig extends AnyMG7LoquatConfig {

  /* Data processing parameters */

  /* This is the number of reads in each chunk after the `split` step */
  val splitChunkSize: Int
  val splitInputFormat: SplitInputFormat
}
