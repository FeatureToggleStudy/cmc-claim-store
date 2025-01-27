package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class CountyCourtJudgmentRule {

    private ClaimDeadlineService claimDeadlineService;
    private static final String CLAIM_OBJECT_CANNOT_BE_NULL = "claim object can not be null";
    private static final String COUNTY_COURT_JUDGMENT_FOR_CLAIM = "County Court Judgment for the claim ";

    @Autowired
    public CountyCourtJudgmentRule(ClaimDeadlineService claimDeadlineService) {
        this.claimDeadlineService = claimDeadlineService;
    }

    public void assertCountyCourtJudgementCanBeRequested(@NotNull Claim claim,
                                                         CountyCourtJudgmentType countyCourtJudgmentType) {
        requireNonNull(claim, CLAIM_OBJECT_CANNOT_BE_NULL);
        String externalId = claim.getExternalId();

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException(COUNTY_COURT_JUDGMENT_FOR_CLAIM + externalId + " was submitted");
        }

        switch (countyCourtJudgmentType) {
            case DEFAULT:
                if (isResponseAlreadySubmitted(claim)) {
                    throw new ForbiddenActionException("Response for the claim " + externalId + " was submitted");
                }

                if (!claimDeadlineService.isPastDeadline(nowInLocalZone(), claim.getResponseDeadline())) {
                    throw new ForbiddenActionException(
                        COUNTY_COURT_JUDGMENT_FOR_CLAIM + externalId + " cannot be requested yet"
                    );
                }
                break;
            case ADMISSIONS:
                if (!claim.getResponse().isPresent()) {
                    throw new IllegalStateException("Claim response cannot be null for judgment type: "
                        + countyCourtJudgmentType);
                }
                break;
            case DETERMINATION:
                // Action pending
                break;
            default:
                throw new ForbiddenActionException(COUNTY_COURT_JUDGMENT_FOR_CLAIM + externalId + " is not supported");

        }
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }

    private boolean isCountyCourtJudgmentAlreadyRedetermined(Claim claim) {
        return claim.getReDeterminationRequestedAt().isPresent();
    }

    public void assertRedeterminationCanBeRequestedOnCountyCourtJudgement(Claim claim) {
        requireNonNull(claim, CLAIM_OBJECT_CANNOT_BE_NULL);

        String externalId = claim.getExternalId();

        if (!isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException(COUNTY_COURT_JUDGMENT_FOR_CLAIM + externalId + " is not yet submitted");
        }

        if (isCountyCourtJudgmentAlreadyRedetermined(claim)) {
            throw new ForbiddenActionException(COUNTY_COURT_JUDGMENT_FOR_CLAIM
                + externalId + " has been already redetermined");
        }

    }

    public boolean isCCJDueToSettlementBreach(Claim claim) {
        requireNonNull(claim, CLAIM_OBJECT_CANNOT_BE_NULL);

        if (claim.getSettlement().isPresent()) {
            PaymentIntention paymentIntention = claim.getSettlement().orElseThrow(IllegalArgumentException::new)
                .getLastStatementOfType(StatementType.OFFER).getOffer().orElseThrow(IllegalArgumentException::new)
                .getPaymentIntention().orElseThrow(IllegalArgumentException::new);

            switch (paymentIntention.getPaymentOption()) {
                case IMMEDIATELY:
                    return nowInLocalZone().toLocalDate().isAfter(
                        paymentIntention.getPaymentDate().orElse(nowInLocalZone().toLocalDate()));
                case BY_SPECIFIED_DATE:
                    return nowInLocalZone().toLocalDate().isAfter(
                        paymentIntention.getPaymentDate().orElse(nowInLocalZone().toLocalDate()));
                case INSTALMENTS:
                    return nowInLocalZone().toLocalDate().isAfter(
                        paymentIntention.getRepaymentPlan()
                        .orElseThrow(IllegalArgumentException::new).getFirstPaymentDate());
                default:
                    throw new IllegalArgumentException("Invalid payment option");
            }

        }

        return false;
    }
}
