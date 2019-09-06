package uk.gov.hmcts.cmc.domain.models.sampledata;

import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

public class SampleStartEventResponse {

    private String eventId = "1516189555935242";
    private String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJvdmMyNHFmb2h0ZWs0ZzlsODMzM2cwY3VyMSIsInN1YiI6IjE5IiwiaWF0IjoxNTEzMjUwOTk4LCJldmVudC1pZCI6InN1Ym1pdENsYWltRXZlbnQiLCJjYXNlLXR5cGUtaWQiOiJNb25leUNsYWltQ2FzZSIsImp1cmlzZGljdGlvbi1pZCI6IkNNQyIsImNhc2UtdmVyc2lvbiI6ImJmMjFhOWU4ZmJjNWEzODQ2ZmIwNWI0ZmEwODU5ZTA5MTdiMjIwMmYifQ.wZCq1zHAZq5b-IK7SPIsBThude6DisovSxfME5CTkt4";
    private StartEventResponse.StartEventResponseBuilder startEventResponseBuilder = StartEventResponse.builder()
                                                                                                        .eventId(eventId)
                                                                                                        .token(token);
    private SampleStartEventResponse() {
    }

    public static SampleStartEventResponse builder() {
        return new SampleStartEventResponse();
    }

    public StartEventResponse buildLegalCaseStartEventResponse() {
        return startEventResponseBuilder.caseDetails(SampleCaseDetails.builder().buildLegalCaseDetails()).build();
    }

    public StartEventResponse buildCitizenStartEventResponse() {
        return startEventResponseBuilder.caseDetails(SampleCaseDetails.builder().buildCitizenCaseDetails()).build();
    }
}