package uk.gov.nationalarchives.tre

import uk.gov.nationalarchives.common.messages.Producer
import uk.gov.nationalarchives.da.messages.bag.available.BagAvailable
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.Status.{COURT_DOCUMENT_PARSE_NO_ERRORS, COURT_DOCUMENT_PARSE_WITH_ERRORS}
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse
import uk.gov.nationalarchives.tre.MessageParsingUtils.{parseBagAvailable, parseCourtDocumentPackageAvailable, parseGenericMessage, parseRequestCourtDocumentParse}

object SlackMessageBuilder {
  def matchMessages(messageBodies: Seq[String]): Seq[MatchedMessages] = {
    val messagesWithMessageTypeAndProducer = messageBodies.map { m => 
      val properties = parseGenericMessage(m).properties
      (m, properties.messageType, properties.producer)
    }
    val bagAvailableMessages = messagesWithMessageTypeAndProducer.collect { 
      case (message, "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable", _) => parseBagAvailable(message)
    }
    val requestCourtDocumentParseMessages = messagesWithMessageTypeAndProducer.collect {
      case (message, "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse", Producer.FCL) => 
        parseRequestCourtDocumentParse(message)
    }
    val courtDocumentPackageAvailableMessages = messagesWithMessageTypeAndProducer.collect {
      case (message, "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable", _) =>          
        parseCourtDocumentPackageAvailable(message)
    }
    requestCourtDocumentParseMessages.map(rcdp => MatchedMessages(
      Right(rcdp), courtDocumentPackageAvailableMessages.find(_.parameters.reference == rcdp.parameters.reference))
    ) ++ bagAvailableMessages.map(ba => MatchedMessages(
      Left(ba), courtDocumentPackageAvailableMessages.find(_.parameters.reference == ba.parameters.reference))
    )
  }
  
  def buildSlackMessage(matchedMessages: Seq[MatchedMessages]): String = {
    val matchedWithNoErrors = matchedMessages.filter(_.messageOut.exists(_.parameters.status == COURT_DOCUMENT_PARSE_NO_ERRORS))
    val matchedWithErrors = matchedMessages.filter(_.messageOut.exists(_.parameters.status == COURT_DOCUMENT_PARSE_WITH_ERRORS))
    val unmatched = matchedMessages.filter(_.messageOut.isEmpty)
    val noErrorsSummary = if (matchedWithNoErrors.nonEmpty) 
      Some(s":white_check_mark:  Processed *${matchedWithNoErrors.size}* requests with no errors") 
    else None
    val matchedWithErrorsSummary = if (matchedWithErrors.nonEmpty) 
      Some(s":warning:  Processed *${matchedWithErrors.size}* requests with errors, " +
        s"references: ${matchedWithErrors.map(_.messageIn.fold(_.parameters.reference, _.parameters.reference)).mkString(",")}")
    else None
    val unmatchedMessagesSummary = if (unmatched.nonEmpty) 
      Some(s":interrobang:  *${unmatched.size}* requests found with no package available message, " +
        s"references: ${unmatched.map(_.messageIn.fold(_.parameters.reference, _.parameters.reference)).mkString(", ")}")
    else None
    Seq(noErrorsSummary, matchedWithErrorsSummary, unmatchedMessagesSummary).flatten.mkString("\n\n") 
  }
}

case class MatchedMessages(messageIn: Either[BagAvailable, RequestCourtDocumentParse], messageOut: Option[CourtDocumentPackageAvailable])
