package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationClient notificationClient;
    private final AppInsights appInsights;
    private final boolean asyncEventOperationEnabled;

    @Autowired
    public NotificationService(
        NotificationClient notificationClient,
        AppInsights appInsights,
        @Value("${feature_toggles.async_event_operations_enabled:false}") boolean asyncEventOperationEnabled
    ) {
        this.notificationClient = notificationClient;
        this.appInsights = appInsights;
        this.asyncEventOperationEnabled = asyncEventOperationEnabled;
    }

    @LogExecutionTime
    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        String errorMessage = String.format(
            "Failure: failed to send notification (%s) due to %s",
            reference, exception.getMessage()
        );

        logger.info(errorMessage, exception);
        appInsights.trackEvent(AppInsightsEvent.NOTIFICATION_FAILURE, REFERENCE_NUMBER, reference);

        if (asyncEventOperationEnabled) {
            throw exception;
        }
    }
}
