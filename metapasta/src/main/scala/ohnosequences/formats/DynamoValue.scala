package ohnosequences.formats

import com.amazonaws.services.dynamodbv2.model.AttributeValue

case class DynamoValue(value: String, tpe: String) {
  def attributeValue = tpe match {
    case DynamoValue.Num => new AttributeValue().withN(value)
    case DynamoValue.Str => new AttributeValue().withS(value)
    case _ => throw new Error("unknown type for DynamoValue: " + tpe)
  }
}

object DynamoValue {
  val Str = "s"
  val Num = "n"

  def long(n: Long): DynamoValue = new DynamoValue(n.toString, Num)
  def string(s: String): DynamoValue = new DynamoValue(s, Str)

  def fromAttributeValue(v: AttributeValue): DynamoValue = {
    if(v.getN != null) {
      DynamoValue(v.getN, Num)
    } else if (v.getS != null) {
      DynamoValue(v.getS, Str)
    } else {
      throw new Error("unknown type for AttributeValue: " + v)
    }

  }
}
