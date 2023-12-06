package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import scala.sys.env
import scala.util.Try

class LambdaHandler() extends RequestHandler[ScheduledEvent, Unit] {

  override def handleRequest(event: ScheduledEvent, context: Context): Unit = {
    context.getLogger.log(s"Logging successfully.")
    context.getLogger.log(s"Scheduled event received: $event")
    val monitoringQueueArn = env("MONITORING_QUEUE_ARN")
    val monitoringQueueUrl = SQSUtils.deriveQueueUrl(monitoringQueueArn)
    Try(SQSUtils.receiveAllMessages(monitoringQueueUrl))
      .fold(
        t => context.getLogger.log(s"Something went wrong here: ${t.toString}"),
        messages => {
          messages.foreach(m => context.getLogger.log(s"Received message: ${m.body()} \n"))
          SQSUtils.batchDeleteMessages(monitoringQueueUrl, messages)
          SQSUtils.closeClient()
        }
      )
  }
}
