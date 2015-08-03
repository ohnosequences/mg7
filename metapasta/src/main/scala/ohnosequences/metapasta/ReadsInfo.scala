package ohnosequences.metapasta

import com.amazonaws.services.dynamodbv2.model.{ScalarAttributeType, AttributeDefinition, AttributeValue}

case class ReadInfo(readId: String, gi: String, sequence: String, quality: String, sample: String, chunk: String, tax: String) {

  import ReadInfo._

  def chunkId(c: Int) = c + "-" + chunk

  def toDynamoItem(c: Int): java.util.Map[String, AttributeValue] = {
    val r = new java.util.HashMap[String, AttributeValue]()

    r.put(idAttr, new AttributeValue().withS(readId))
    r.put(sequenceAttr, new AttributeValue().withS(sequence))
    r.put(qualityAttr, new AttributeValue().withS(quality))
    r.put(chunkAttr, new AttributeValue().withS(chunkId(c)))
    if(!gi.isEmpty) {
      r.put(giAttr, new AttributeValue().withS(gi))
    } else {
      r.put(giAttr, new AttributeValue().withS(unassigned))
    }

    if(!tax.isEmpty) {
      r.put(taxAttr, new AttributeValue().withS(tax))
    } else {
      r.put(taxAttr, new AttributeValue().withS(unassigned))
    }
    r

  }
}

object ReadInfo {
  val unassigned = "unassigned"

  val idAttr = "header"
  val sequenceAttr = "seq"
  val qualityAttr = "qual"
  val giAttr = "gi"
  val taxAttr = "tax"
  val chunkAttr = "chunk"

  val hash = new AttributeDefinition().withAttributeName(chunkAttr).withAttributeType(ScalarAttributeType.S)
  val range = new AttributeDefinition().withAttributeName(idAttr).withAttributeType(ScalarAttributeType.S)

}
