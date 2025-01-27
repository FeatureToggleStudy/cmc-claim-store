package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

public interface SampleQueryConstants {
    String mediationQuery = "{"
        + "\"size\": 1000,"
        + "\"query\": {\n"
        + "  \"bool\" : {\n"
        + "    \"must\" : [\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"data.respondents.value.responseFreeMediationOption\" : {\n"
        + "            \"value\" : \"YES\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"data.respondents.value.claimantResponse.freeMediationOption\" : {\n"
        + "            \"value\" : \"YES\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.respondents.value.claimantResponse.submittedOn\" : {\n"
        + "            \"from\" : \"2019-07-07T00:00:00.000Z\",\n"
        + "            \"to\" : \"2019-07-07T23:59:59.999999999Z\",\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"adjust_pure_negative\" : true,\n"
        + "    \"boost\" : 1.0\n"
        + "  }\n"
        + "}}";
}
