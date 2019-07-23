package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.BaseGetTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.reform.pdf.service.client.exception.PDFServiceClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GenerateDefendantResponseCopyTest extends BaseGetTest {
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};

    @Test
    public void shouldReturnPdfDocumentIfEverythingIsFine() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(claim, SampleResponse.FullDefence.builder().build());

        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willReturn(PDF_BYTES);

        makeRequest("/documents/defendantResponseCopy/" + claim.getExternalId())
            .andExpect(status().isOk())
            .andExpect(content().bytes(PDF_BYTES))
            .andReturn();
    }

    @Test
    public void shouldReturnNotFoundWhenClaimIsNotFound() throws Exception {
        String nonExistingExternalId = "f5b92e36-fc9c-49e6-99f7-74d60aaa8da2";

        makeRequest("/documents/defendantResponseCopy/" + nonExistingExternalId)
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnServerErrorWhenPdfGenerationFails() throws Exception {
        Claim claim = claimStore.saveClaim(SampleClaimData.builder().build());
        claimStore.saveResponse(claim, SampleResponse.FullDefence.builder().build());

        given(pdfServiceClient.generateFromHtml(any(), any()))
            .willThrow(new PDFServiceClientException(new RuntimeException("Something bad happened!")));

        makeRequest("/documents/defendantResponseCopy/" + claim.getExternalId())
            .andExpect(status().isInternalServerError());
    }
}
