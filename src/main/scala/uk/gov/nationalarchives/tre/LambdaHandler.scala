package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.apache.http.impl.client.HttpClients
import uk.gov.nationalarchives.tre.MessageParsingUtils.parseStringMap

import scala.sys.env

class LambdaHandler() extends RequestHandler[ScheduledEvent, Unit] {

  override def handleRequest(event: ScheduledEvent, context: Context): Unit = {
    val monitoringQueueArn = env("MONITORING_QUEUE_ARN")
    val monitoringQueueUrl = SQSUtils.deriveQueueUrl(monitoringQueueArn)
    val messages = SQSUtils.receiveAllMessages(monitoringQueueUrl)
    
    val slackEndpoints = parseStringMap(env("NOTIFIABLE_SLACK_MONITORING_ENDPOINTS"))
    
    context.getLogger.log(s"Slack endpoints")
    
    val httpClient = HttpClients.createDefault()
    val slackUtils = new SlackUtils(httpClient)
    val messageText = SlackMessageBuilder.buildSlackMessage(SlackMessageBuilder.matchMessages(messages.map(_.body())))
    
    context.getLogger.log(s"Message text: $messageText")    
    
    slackEndpoints.foreach { case (c, wh) => slackUtils.postMessage(wh, messageText, c, "monitoring-lambda") }
    
    SQSUtils.batchDeleteMessages(monitoringQueueUrl, messages)
    SQSUtils.closeClient()
  }
}
