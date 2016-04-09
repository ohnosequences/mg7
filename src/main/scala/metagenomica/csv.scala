package ohnosequences.mg7.csv

// Some minimal CSV utils, will be replaced by a specialized library based on cosas

case class Row[H](
  val header: Seq[H],
  val values: Seq[String]
) {

  def toMap: Map[H, String] = header.zip(values).toMap

  def select(column: H): String = this.toMap.apply(column)
}
