package uk.gov.nationalarchives.tre

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model._

import java.lang
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.{CollectionHasAsScala, IterableHasAsJava}
object SQSUtils {
   lazy val sqsClient: SqsClient = SqsClient.builder().region(Region.EU_WEST_2).build()
  def receiveMessages(queueUrl: String): Seq[Message] = {
    val receiveMessageRequest = ReceiveMessageRequest.builder
      .queueUrl(queueUrl)
      .maxNumberOfMessages(10)
      .waitTimeSeconds(0)
      .build()
    val response = sqsClient.receiveMessage(receiveMessageRequest)
    response.messages().asScala.toSeq
  }

  @tailrec
  def receiveAllMessages(queueUrl: String, accumulatedMessages: Seq[Message] = Seq.empty[Message]): Seq[Message] = {
    receiveMessages(queueUrl) match {
      case messages: Seq[Message] if messages.nonEmpty => receiveAllMessages(queueUrl, accumulatedMessages ++ messages)
      case _ => accumulatedMessages
    }
  }

  
  def batchDeleteMessages(queueUrl: String, messages: Seq[Message]): Unit = {
    val entries = messages.map { m => 
      DeleteMessageBatchRequestEntry.builder()
        .receiptHandle(m.receiptHandle())
        .id(m.messageId())
        .build()
    }
    entries.grouped(10).foreach { batchedEntries =>
      val deleteMessageBatchRequest = DeleteMessageBatchRequest.builder()
        .queueUrl(queueUrl)
        .entries(batchedEntries.asJavaCollection)
        .build()
      sqsClient.deleteMessageBatch(deleteMessageBatchRequest)
    }
  }
  
  def closeClient(): Unit = sqsClient.close()
  def deriveQueueUrl(queueArn: String): String = {
    val accountId: String = queueArn.split(":")(4)
    val queueName: String = queueArn.split(":")(5).split("/").last
    val regionId: String = Region.EU_WEST_2.id

    s"https://sqs.$regionId.amazonaws.com/$accountId/$queueName"
  }
}
