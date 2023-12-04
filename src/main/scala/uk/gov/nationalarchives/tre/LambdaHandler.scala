package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.{LambdaDestinationEvent, SNSEvent, SQSEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class LambdaHandler() extends RequestHandler[SQSEvent, Unit] {

  override def handleRequest(event: SQSEvent, context: Context): Unit = {
    event match {
      case sqsEvent: SQSEvent => sqsEvent.getRecords.forEach(record => context.getLogger.log(s"Record received: ${record.getBody}"))
    }
  }
}
