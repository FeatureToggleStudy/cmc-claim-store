package uk.gov.hmcts.cmc.ccd.domain.response;

public enum CCDDefenceType {
    DISPUTE("dispute"),
    ALREADY_PAID("already paid");

    private String value;

    CCDDefenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
