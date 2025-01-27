package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.Interest.InterestType.NO_INTEREST;

@Component
public class AmountContentProvider {

    private final InterestContentProvider interestContentProvider;

    public AmountContentProvider(InterestContentProvider interestContentProvider) {
        this.interestContentProvider = interestContentProvider;
    }

    private BigDecimal getPartAdmissionAmount(Claim claim, PartAdmissionResponse response) {
        return response.getAmount().subtract(
            claim.getClaimData().getFeesPaidInPounds())
            .max(ZERO);
    }

    private BigDecimal getPartAdmissionClaimFeeInPounds(Claim claim,
                                                       PartAdmissionResponse response, BigDecimal admissionAmount) {
        if (admissionAmount.equals(ZERO)
            && admissionAmount.compareTo(claim.getClaimData().getFeesPaidInPounds()) < 0) {
            return response.getAmount();
        } else {
            return claim.getClaimData().getFeesPaidInPounds();
        }
    }

    public AmountContent create(Claim claim) {
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();

        Response response = claim.getResponse().orElse(null);

        boolean isPartAdmissionResponse = response instanceof PartAdmissionResponse;
        boolean usePartAdmitAmount = isPartAdmissionResponse && claim.getClaimantResponse()
            .filter(claimantResponse -> ClaimantResponseType.ACCEPTATION.equals(claimantResponse.getType()))
            .isPresent();
        BigDecimal admittedAmount = isPartAdmissionResponse
            ? getPartAdmissionAmount(claim, (PartAdmissionResponse) response)
            : claimAmount;

        BigDecimal feeAmount = isPartAdmissionResponse
            ? getPartAdmissionClaimFeeInPounds(claim, (PartAdmissionResponse) response, admittedAmount)
            : claim.getClaimData().getFeesPaidInPounds();

        BigDecimal ccjAmount = usePartAdmitAmount ? admittedAmount : claimAmount;

        requireNonNull(claim.getCountyCourtJudgment());

        BigDecimal paidAmount = claim.getCountyCourtJudgment().getPaidAmount().orElse(ZERO);
        InterestContent interestContent = null;
        BigDecimal interestRealValue = ZERO;

        if (claim.getClaimData().getInterest().getType() != NO_INTEREST && !isPartAdmissionResponse) {
            interestContent = interestContentProvider.createContent(
                claim.getClaimData().getInterest(),
                claim.getClaimData().getInterest().getInterestDate(),
                claimAmount,
                claim.getIssuedOn(),
                LocalDateTimeFactory.nowInLocalZone().toLocalDate()
            );
            interestRealValue = interestContent.getAmountRealValue();
        }

        return new AmountContent(
            formatMoney(claimAmount),
            formatMoney(ccjAmount
                .add(feeAmount)
                .add(interestRealValue)),
            interestContent,
            formatMoney(feeAmount),
            formatMoney(paidAmount),
            formatMoney(ccjAmount
                .add(feeAmount)
                .add(interestRealValue)
                .subtract(paidAmount)),
            formatMoney(admittedAmount)
        );

    }

}
