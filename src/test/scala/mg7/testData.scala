/*
  # Test data

  We use different 16S datasets as input for mg7 release tests.
*/
package ohnosequences.test.mg7

import java.net.URL
import ohnosequences.awstools.s3._

case object testData {

  lazy val mg7 = ohnosequences.generated.metadata.mg7

  /* All input test data should go in here; note that this is *not* scoped by mg7 version: it is supposed to be immutable, and we don't want to copy it to a new location for every release. */
  lazy val s3 = S3Folder("resources.ohnosequences.com", mg7.organization) / mg7.artifact / "mock-communities-data" /

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

    The difference between `HM-782D` and `HM-783D` is that `HM-782D` has **even** concentration of **RNA operon counts** per species, while `HM-783D` contains **staggered RNA operon counts**, with a difference of at most a `10^3` factor. The same difference is found between `HM-278D` and `HM-279D` where `HM-278D` has an even concentration of the operon while `HM-279D` has staggered RNA operon units.

  */
  val HM_782D =
    MockCommunity (
      id          = "HM-782D",
      description = new URL("https://www.beiresources.org/Catalog/otherProducts/HM-782D.aspx"),
      composition = Map (
        400667  -> ("Acinetobacter baumannii ATCC 17978"   ->               1 ),
        411466  -> ("Actinomyces odontolyticus ATCC 17982" ->               1 ),
        222523  -> ("Bacillus cereus ATCC 10987" ->                         1 ),
        435590  -> ("Bacteroides vulgatus ATCC 8482" ->                     1 ),
        290402  -> ("Clostridium beijerinckii NCIMB 8052" ->                1 ),
        243230  -> ("Deinococcus radiodurans R1" ->                         1 ),
        474186  -> ("Enterococcus faecalis OG1RF" ->                        1 ),
        511145  -> ("Escherichia coli str. K-12 substr. MG1655" ->          1 ),
        85962   -> ("Helicobacter pylori 26695" ->                          1 ),
        324831  -> ("Lactobacillus gasseri ATCC 33323 = JCM 1131" ->        1 ),
        169963  -> ("Listeria monocytogenes EGD-e" ->                       1 ),
        122586  -> ("Neisseria meningitidis MC58" ->                        1 ),
        267747  -> ("Propionibacterium acnes KPA171202" ->                  1 ),
        208964  -> ("Pseudomonas aeruginosa PAO1" ->                        1 ),
        272943  -> ("Rhodobacter sphaeroides 2.4.1" ->                      1 ),
        451516  -> ("Staphylococcus aureus subsp. aureus USA300_TCH1516" -> 1 ),
        176280  -> ("Staphylococcus epidermidis ATCC 12228" ->              1 ),
        208435  -> ("Streptococcus agalactiae 2603V/R" ->                   1 ),
        210007  -> ("Streptococcus mutans UA159" ->                         1 ),
        170187  -> ("Streptococcus pneumoniae TIGR4" ->                     1 )
      )
    )

  val HM_783D =
    MockCommunity (
      id          = "HM_783D",
      description = new URL("https://www.beiresources.org/Catalog/otherProducts/HM-783D.aspx"),
      composition = Map (
        400667  -> ("Acinetobacter baumannii ATCC 17978"   ->               10    ),
        411466  -> ("Actinomyces odontolyticus ATCC 17982" ->               1     ),
        222523  -> ("Bacillus cereus ATCC 10987" ->                         100   ),
        435590  -> ("Bacteroides vulgatus ATCC 8482" ->                     1     ),
        290402  -> ("Clostridium beijerinckii NCIMB 8052" ->                100   ),
        243230  -> ("Deinococcus radiodurans R1" ->                         1     ),
        474186  -> ("Enterococcus faecalis OG1RF" ->                        1     ),
        511145  -> ("Escherichia coli str. K-12 substr. MG1655" ->          1000  ),
        85962   -> ("Helicobacter pylori 26695" ->                          10    ),
        324831  -> ("Lactobacillus gasseri ATCC 33323 = JCM 1131" ->        10    ),
        169963  -> ("Listeria monocytogenes EGD-e" ->                       10    ),
        122586  -> ("Neisseria meningitidis MC58" ->                        10    ),
        267747  -> ("Propionibacterium acnes KPA171202" ->                  10    ),
        208964  -> ("Pseudomonas aeruginosa PAO1" ->                        100   ),
        272943  -> ("Rhodobacter sphaeroides 2.4.1" ->                      1000  ),
        451516  -> ("Staphylococcus aureus subsp. aureus USA300_TCH1516" -> 100   ),
        176280  -> ("Staphylococcus epidermidis ATCC 12228" ->              1000  ),
        208435  -> ("Streptococcus agalactiae 2603V/R" ->                   100   ),
        210007  -> ("Streptococcus mutans UA159" ->                         1000  ),
        170187  -> ("Streptococcus pneumoniae TIGR4" ->                     1     )
      )
    )

  val HM_278D =
    MockCommunity(
      id = "HM_278D",
      description = new URL("https://www.beiresources.org/Catalog/ItemDetails/tabid/522/Default.aspx?BEINum=HM-278D&Template=otherProducts"),
      composition = Map (
        400667  -> ("Acinetobacter baumannii ATCC 17978"   ->               1 ),
        411466  -> ("Actinomyces odontolyticus ATCC 17982" ->               1 ),
        222523  -> ("Bacillus cereus ATCC 10987" ->                         1 ),
        435590  -> ("Bacteroides vulgatus ATCC 8482" ->                     1 ),
        290402  -> ("Clostridium beijerinckii NCIMB 8052" ->                1 ),
        243230  -> ("Deinococcus radiodurans R1" ->                         1 ),
        474186  -> ("Enterococcus faecalis OG1RF" ->                        1 ),
        511145  -> ("Escherichia coli str. K-12 substr. MG1655" ->          1 ),
        85962   -> ("Helicobacter pylori 26695" ->                          1 ),
        324831  -> ("Lactobacillus gasseri ATCC 33323 = JCM 1131" ->        1 ),
        169963  -> ("Listeria monocytogenes EGD-e" ->                       1 ),
        122586  -> ("Neisseria meningitidis MC58" ->                        1 ),
        267747  -> ("Propionibacterium acnes KPA171202" ->                  1 ),
        208964  -> ("Pseudomonas aeruginosa PAO1" ->                        1 ),
        272943  -> ("Rhodobacter sphaeroides 2.4.1" ->                      1 ),
        450394  -> ("Staphylococcus aureus subsp. aureus USA300_TCH959" ->  1 ),
        176280  -> ("Staphylococcus epidermidis ATCC 12228" ->              1 ),
        208435  -> ("Streptococcus agalactiae 2603V/R" ->                   1 ),
        210007  -> ("Streptococcus mutans UA159" ->                         1 ),
        170187  -> ("Streptococcus pneumoniae TIGR4" ->                     1 )
      )
    )

    val HM_279D =
      MockCommunity(
        id = "HM_279D",
        description = new URL("https://www.beiresources.org/Catalog/ItemDetails/tabid/522/Default.aspx?BEINum=HM-279D&Template=otherProducts"),
        composition = Map (
          400667  -> ("Acinetobacter baumannii ATCC 17978"   ->               10 ),
          411466  -> ("Actinomyces odontolyticus ATCC 17982" ->               1 ),
          222523  -> ("Bacillus cereus ATCC 10987" ->                         100 ),
          435590  -> ("Bacteroides vulgatus ATCC 8482" ->                     1 ),
          290402  -> ("Clostridium beijerinckii NCIMB 8052" ->                100 ),
          243230  -> ("Deinococcus radiodurans R1" ->                         1 ),
          474186  -> ("Enterococcus faecalis OG1RF" ->                        1 ),
          511145  -> ("Escherichia coli str. K-12 substr. MG1655" ->          1000 ),
          85962   -> ("Helicobacter pylori 26695" ->                          10 ),
          324831  -> ("Lactobacillus gasseri ATCC 33323 = JCM 1131" ->        10 ),
          169963  -> ("Listeria monocytogenes EGD-e" ->                       10 ),
          122586  -> ("Neisseria meningitidis MC58" ->                        10 ),
          267747  -> ("Propionibacterium acnes KPA171202" ->                  10 ),
          208964  -> ("Pseudomonas aeruginosa PAO1" ->                        100 ),
          272943  -> ("Rhodobacter sphaeroides 2.4.1" ->                      1000 ),
          450394  -> ("Staphylococcus aureus subsp. aureus USA300_TCH959" ->  100 ),
          176280  -> ("Staphylococcus epidermidis ATCC 12228" ->              1000 ),
          208435  -> ("Streptococcus agalactiae 2603V/R" ->                   100 ),
          210007  -> ("Streptococcus mutans UA159" ->                         1000 ),
          170187  -> ("Streptococcus pneumoniae TIGR4" ->                     1 )
        )
      )

  /*
    ## Illumina mock communities

    16S V3-V4 region from the samples taken from the communities `HM-782D` and `HM-783D` were sequenced with Illumina MiSeq 2x300 as described in the article [http://www.ncbi.nlm.nih.gov/pmc/articles/PMC4636327/](http://www.ncbi.nlm.nih.gov/pmc/articles/PMC4636327/). For each sample 3 technical replicas were obtained. The following table shows the correspondence between the datasets IDs (ERR IDs from SRA database) and the mock community where the sequences come from.

    | ERR ID | BEI ID | Comment |
    | :--- | ---: | ---: |
    | ERR1049996 | HM-782D | BEI Mock even community HM-782D - rep1 |
    | ERR1049997 | HM-782D | BEI Mock even community HM-782D - rep2 |
    | ERR1049998 | HM-782D | BEI Mock even community HM-782D - rep3 |
    | ERR1049999 | HM-783D | BEI Mock staggered community HM-783D - rep1 |
    | ERR1050000 | HM-783D | BEI Mock staggered community HM-783D - rep2 |
    | ERR1050001 | HM-783D | BEI Mock staggered community HM-783D - rep3 |

  */
  val ERR1049996 =
    MockCommunitySample (
      id        = "ERR1049996",
      community = HM_782D
    )

  val ERR1049997 =
    MockCommunitySample (
      id        = "ERR1049997",
      community = HM_782D
    )

    val ERR1049998 =
      MockCommunitySample (
        id        = "ERR1049998",
        community = HM_782D
      )

    val ERR1049999 =
      MockCommunitySample (
        id        = "ERR1049999",
        community = HM_783D
      )

    val ERR1050000 =
      MockCommunitySample (
        id        = "ERR1050000",
        community = HM_783D
      )

    val ERR1050001 =
      MockCommunitySample (
        id        = "ERR1050001",
        community = HM_783D
      )

      /*
        ## PacBio mock communities

        The full 16S gene from the samples coming from the `HM-278D` and `HM-279D` communities was sequenced with PacBio using circular consensus sequences (CCS). One dataset per comunity was obtained. The following table shows the correspondence between the datasets and the communities the data comes from.

        | Sample ID | BEI ID | Comment |
        | :--- | ---: | ---: |
        | even | HM-278D | BEI:HM-278D |
        | stagg | HM-279D | BEI:HM-279D staggered |

      */

      val even =
        MockCommunitySample (
          id        = "even",
          community = HM_278D
        )

      val stagg =
        MockCommunitySample (
          id        = "stagg",
          community = HM_279D 
        )




  /* In the composition map, the values are the taxon name and the relative count of RNA operon copies */
  case class MockCommunity(val id: String, val description: URL, val composition: Map[Int,(String,Int)])
  case class MockCommunitySample(val id: String, val community: MockCommunity)
}
