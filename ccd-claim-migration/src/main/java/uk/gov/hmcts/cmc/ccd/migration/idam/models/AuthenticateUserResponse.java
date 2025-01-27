package uk.gov.hmcts.cmc.ccd.migration.idam.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUserResponse {

    private String code;

    @JsonCreator
    public AuthenticateUserResponse(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
