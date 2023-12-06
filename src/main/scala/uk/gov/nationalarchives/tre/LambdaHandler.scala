package uk.gov.nationalarchives.tre

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

class LambdaHandler() extends RequestHandler[ScheduledEvent, Unit] {

  override def handleRequest(event: ScheduledEvent, context: Context): Unit = {
    context.getLogger.log(s"Logging successfully.")
    context.getLogger.log(s"Scheduled event received: $event")
  }
}
