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

  "buildSlackMessage" should "build the expected string for a parse with errors" in {
    SlackMessageBuilder.buildSlackMessage(
      Seq(
        MatchedMessages(
          Right(
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
          Some(
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
          Right(
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
          Some(
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
          Right(
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
          None
        )
      )
    ) shouldBe ":interrobang:  *1* requests found with no package available message, references: FCL-NR"
  }
}
