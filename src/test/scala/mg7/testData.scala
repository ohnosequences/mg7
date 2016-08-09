/*
  # Test data

  We use different 16S datasets as input for mg7 release tests.
*/
package ohnosequences.test.mg7

case object testData {

  /*
    ## PRJEB6592: Illumina 16S bias evaluation

    - [ENA entry](http://www.ebi.ac.uk/ena/data/view/PRJEB6592)
    - **Illumina** data
    - **Run ID** `ERR567374`
  */
  // case object PRJEB6592 {
  //
  // }

  /*
    ## Mock communities

    These are physical mock communities from BEI from which samples have been sequenced in our test data.
  */
  val HM_782D =
    MockCommunity (
      id          = "HM-782D",
      description = "https://www.beiresources.org/ProductInformationSheet/tabid/784/Default.aspx?doc=38933.pdf",
      composition = Map (
        400667  -> "Acinetobacter baumannii ATCC 17978",
        411466  -> "Actinomyces odontolyticus ATCC 17982",
        222523  -> "Bacillus cereus ATCC 10987",
        435590  -> "Bacteroides vulgatus ATCC 8482",
        290402  -> "Clostridium beijerinckii NCIMB 8052",
        243230  -> "Deinococcus radiodurans R1",
        474186  -> "Enterococcus faecalis OG1RF",
        511145  -> "Escherichia coli str. K-12 substr. MG1655",
        85962   -> "Helicobacter pylori 26695",
        324831  -> "Lactobacillus gasseri ATCC 33323 = JCM 1131",
        169963  -> "Listeria monocytogenes EGD-e",
        122586  -> "Neisseria meningitidis MC58",
        267747  -> "Propionibacterium acnes KPA171202",
        208964  -> "Pseudomonas aeruginosa PAO1",
        272943  -> "Rhodobacter sphaeroides 2.4.1",
        451516  -> "Staphylococcus aureus subsp. aureus USA300_TCH1516",
        176280  -> "Staphylococcus epidermidis ATCC 12228",
        208435  -> "Streptococcus agalactiae 2603V/R",
        210007  -> "Streptococcus mutans UA159",
        170187  -> "Streptococcus pneumoniae TIGR4"
      )
    )

  /*
    ## Illumina mock communities

    These are several mock community samples sequenced with Illumina.
  */
  val ERR1049996 =
    MockCommunitySample (
      id        = "ERR1049996",
      community = HM_782D
    )

  case class MockCommunity(val id: String, val description: String, val composition: Map[Int,String])
  case class MockCommunitySample(val id: String, val community: MockCommunity)
}
