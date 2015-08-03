package ohnosequences.nisperon.console

import ohnosequences.nisperon.AWS
import com.amazonaws.services.autoscaling.model.{AutoScalingGroup, DescribeAutoScalingGroupsRequest}
import scala.collection.mutable.ListBuffer
import collection.JavaConversions._
import java.util


object AWSTools {
  def describeAutoScalingGroup(aws: AWS, name: String): List[AutoScalingGroup] = {
    var nextToken = ""
    val res = new scala.collection.mutable.ArrayBuffer[AutoScalingGroup]()
     do {
      val r = aws.as.as.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest()
        .withAutoScalingGroupNames(name)
      )
       if(!nextToken.isEmpty) {
         r.withNextToken(nextToken)
       }
      res.++=(r.getAutoScalingGroups)
      nextToken = r.getNextToken
      //println("next token: " + nextToken)
    } while(nextToken != null && !nextToken.isEmpty)
    res.toList

  }

}
