package uk.gov.hmcts.cmc.ccd.migration.idam.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonCreator
    public TokenExchangeResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
