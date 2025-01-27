package uk.gov.hmcts.cmc.ccd.migration.idam.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class GeneratePinRequest {

    public final String firstName;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public final String lastName;
    public final List<String> roles;

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = null;
    }
}

