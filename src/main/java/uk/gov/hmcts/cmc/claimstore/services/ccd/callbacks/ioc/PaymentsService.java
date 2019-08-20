package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.ioc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeOutcome;
import uk.gov.hmcts.reform.payments.client.PaymentRequest;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.Fee;
import uk.gov.hmcts.reform.payments.client.models.Payment;

import java.math.BigDecimal;

import static java.lang.String.format;

@Service
public class PaymentsService {
    private static final String PAYMENT_RETURN_URL = "%s/claim/pay/%s/receiver";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PaymentsClient paymentsClient;
    private final FeesClient feesClient;
    private final NotificationsProperties notificationsProperties;
    private final String service;
    private final String siteId;
    private final String currency;
    private final String description;

    public PaymentsService(
        PaymentsClient paymentsClient,
        FeesClient feesClient, NotificationsProperties notificationsProperties,
        @Value("${payments.api.service}") String service,
        @Value("${payments.api.siteId}") String siteId,
        @Value("${payments.api.currency}") String currency,
        @Value("${payments.api.description}") String description
    ) {
        this.paymentsClient = paymentsClient;
        this.feesClient = feesClient;
        this.notificationsProperties = notificationsProperties;
        this.service = service;
        this.siteId = siteId;
        this.currency = currency;
        this.description = description;
    }

    public Payment makePayment(
        String authorisation,
        CCDCase ccdCase,
        BigDecimal totalAmount
    ) {

        logger.info("Retrieving fee for case {}",
            ccdCase.getExternalId());

        FeeOutcome feeOutcome = feesClient.lookupFee(
            "online", "issue", totalAmount);

        BigDecimal amountPlusFees = totalAmount.add(feeOutcome.getFeeAmount());

        PaymentRequest paymentRequest = buildPaymentRequest(
            ccdCase,
            buildFees(String.valueOf(ccdCase.getId()), feeOutcome),
            amountPlusFees
        );

        return paymentsClient.createPayment(
            authorisation,
            paymentRequest,
            format(PAYMENT_RETURN_URL,
                notificationsProperties.getFrontendBaseUrl(),
                ccdCase.getExternalId())
        );
    }

    private Fee[] buildFees(String caseId, FeeOutcome feeOutcome) {
        return new Fee[] {
            Fee.builder()
                .ccdCaseNumber(caseId)
                .calculatedAmount(feeOutcome.getFeeAmount())
                .code(feeOutcome.getCode())
                .version(feeOutcome.getVersion())
                .build()
        };
    }

    private PaymentRequest buildPaymentRequest(CCDCase ccdCase, Fee[] fees, BigDecimal amountPlusFees) {
        return PaymentRequest.builder()
            .caseReference(ccdCase.getExternalId())
            .ccdCaseNumber(String.valueOf(ccdCase.getId()))
            .amount(amountPlusFees)
            .fees(fees)
            .service(service)
            .currency(currency)
            .description(description)
            .siteId(siteId)
            .build();
    }
}
