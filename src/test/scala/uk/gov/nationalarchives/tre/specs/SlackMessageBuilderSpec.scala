package uk.gov.nationalarchives.tre.specs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.nationalarchives.common.messages.Producer.{FCL, TRE}
import uk.gov.nationalarchives.common.messages.Properties
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.Status.{COURT_DOCUMENT_PARSE_NO_ERRORS, COURT_DOCUMENT_PARSE_WITH_ERRORS}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Parameters => CDPAParameters}
import uk.gov.nationalarchives.da.messages.request.courtdocument.parse.{ParserInstructions, RequestCourtDocumentParse, Parameters => RCDPParameters}
import uk.gov.nationalarchives.tre.{MatchedMessages, SlackMessageBuilder}

class SlackMessageBuilderSpec extends AnyFlatSpec with MockitoSugar {

  "matchMessages" should "not attempt to match messages with a standard consignment type" in {
    val matchedMessages = SlackMessageBuilder.matchMessages(
      Seq(
        """
          |{
          |	  "properties" : {
          |	      "messageType" : "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable",
          |       "timestamp" : "2023-11-06T15:15:08.443071Z",
          |		    "function" : "",
          |	      "producer" : "TDR",
          |	      "executionId" : "",
          |       "parentExecutionId" : null
          |	  },
          |	  "parameters" : {
          |	      "reference" : "TDR-2021-CF6L",
          |	      "originator" : "TDR",
          |	      "consignmentType" : "COURT_DOCUMENT",
          |	    	"s3Bucket" : "da-transform-sample-data",
          |	      "s3BagKey" : "dc34c025-ca5c-4746-b89a-a05bb451d344/sample-data/judgment/tdr-bag/TDR-2021-CF6L.tar.gz",
          |	      "s3BagSha256Key" : "TDR-2021-CF6L.tar.gz.sha256"
          |	  }
          |}
          |""".stripMargin,
        """
          |{
          |	  "properties" : {
          |	      "messageType" : "uk.gov.nationalarchives.da.messages.bag.available.BagAvailable",
          |       "timestamp" : "2023-11-06T15:15:08.443071Z",
          |		    "function" : "",
          |	      "producer" : "TDR",
          |	      "executionId" : "",
          |       "parentExecutionId" : null
          |	  },
          |	  "parameters" : {
          |	      "reference" : "TDR-2021-CF6L",
          |	      "originator" : "TDR",
          |	      "consignmentType" : "STANDARD",
          |	    	"s3Bucket" : "da-transform-sample-data",
          |	      "s3BagKey" : "dc34c025-ca5c-4746-b89a-a05bb451d344/sample-data/judgment/tdr-bag/TDR-2021-CF6L.tar.gz",
          |	      "s3BagSha256Key" : "TDR-2021-CF6L.tar.gz.sha256"
          |	  }
          |}
          |""".stripMargin
      )
    )
    matchedMessages.size shouldBe 1
  }
  
  "buildSlackMessage" should "build the expected string for a parse with errors" in {
    SlackMessageBuilder.buildSlackMessage(
      Seq(
        MatchedMessages(
          messageIn = Right(
            RequestCourtDocumentParse(
              Properties(
                messageType = "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
                timestamp = "2023-12-06T16:33:03.468456Z",
                producer =FCL,
                function = "",
                executionId = "",
                parentExecutionId = None
              ),
              RCDPParameters(
                s3Bucket = "caselaw-stg-tre-request-parse",
                s3Key = "eat/2022/1/this_file_doesnt_exist.docx",
                reference = "FCL-NR",
                originator = Some("FCL"),
                parserInstructions = ParserInstructions("judgment")))
          ),
          messageOut = Some(
            CourtDocumentPackageAvailable(
              Properties(
                messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
                timestamp = "2023-12-06T16:33:06.127537Z",
                function = "pte-ah-tre-court-document-pack-lambda",
                producer = TRE,
                executionId = "0d789f82-63fe-4998-86f0-2a089d6145a22",
                parentExecutionId = Some("601e6d12-8332-4f99-9361-02e162fb8355")),
              CDPAParameters(
                reference = "FCL-NR",
                originator = Some("FCL"),
                s3Bucket = "pte-ah-tre-court-document-pack-out",
                s3Key = "FCL-NR/0d789f82-63fe-4998-86f0-2a089d6145a2/TRE-FCL-NR.tar.gz",
                metadataFilePath = "/metadata.json",
                metadataFileType = "Json",
                status = COURT_DOCUMENT_PARSE_WITH_ERRORS)
            )
          )
        )
      )
    ) shouldBe ":warning:  Processed *1* requests with errors, references: FCL-NR"
  }

  "buildSlackMessage" should "build the expected string for a parse with no errors" in {
    SlackMessageBuilder.buildSlackMessage(
      Seq(
        MatchedMessages(
          messageIn = Right(
            RequestCourtDocumentParse(
              Properties(
                messageType = "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
                timestamp = "2023-12-06T16:33:03.468456Z",
                producer = FCL,
                function = "",
                executionId = "",
                parentExecutionId = None
              ),
              RCDPParameters(
                s3Bucket = "caselaw-stg-tre-request-parse",
                s3Key = "eat/2022/1/this_file_doesnt_exist.docx",
                reference = "FCL-NR",
                originator = Some("FCL"),
                parserInstructions = ParserInstructions("judgment")))
          ),
          messageOut = Some(
            CourtDocumentPackageAvailable(
              Properties(
                messageType = "uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable",
                timestamp = "2023-12-06T16:33:06.127537Z",
                function = "pte-ah-tre-court-document-pack-lambda",
                producer = TRE,
                executionId = "0d789f82-63fe-4998-86f0-2a089d6145a22",
                parentExecutionId = Some("601e6d12-8332-4f99-9361-02e162fb8355")),
              CDPAParameters(
                reference = "FCL-NR",
                originator = Some("FCL"),
                s3Bucket = "pte-ah-tre-court-document-pack-out",
                s3Key = "FCL-NR/0d789f82-63fe-4998-86f0-2a089d6145a2/TRE-FCL-NR.tar.gz",
                metadataFilePath = "/metadata.json",
                metadataFileType = "Json",
                status = COURT_DOCUMENT_PARSE_NO_ERRORS)
            )
          )
        )
      )
    ) shouldBe ":white_check_mark:  Processed *1* requests with no errors"
  }

  "buildSlackMessage" should "build the expected string where no out message has been found" in {
    SlackMessageBuilder.buildSlackMessage(
      Seq(
        MatchedMessages(
          messageIn = Right(
            RequestCourtDocumentParse(
              Properties(
                messageType = "uk.gov.nationalarchives.da.messages.request.courtdocument.parse.RequestCourtDocumentParse",
                timestamp = "2023-12-06T16:33:03.468456Z",
                producer = FCL,
                function = "",
                executionId = "",
                parentExecutionId = None
              ),
              RCDPParameters(
                s3Bucket = "caselaw-stg-tre-request-parse",
                s3Key = "eat/2022/1/this_file_doesnt_exist.docx",
                reference = "FCL-NR",
                originator = Some("FCL"),
                parserInstructions = ParserInstructions("judgment")))
          ),
          messageOut = None
        )
      )
    ) shouldBe ":interrobang:  *1* requests found with no package available message, references: FCL-NR"
  }
}
