package uk.gov.hmcts.cmc.claimstore.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest extends BaseNotificationServiceTest {

    private static final String REFERENCE = "reference";
    private static final String TEMPLATE_ID = "templateId";
    private static final Map<String, String> PARAMETERS = new HashMap<>();

    private NotificationService service;

    @Before
    public void beforeEachTest() {
        service = new NotificationService(notificationClient, appInsights, false);
    }

    @Test(expected = NotificationException.class)
    public void shouldThrowNotificationExceptionWhenClientThrowsNotificationClientException() throws Exception {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(mock(NotificationClientException.class));

        try {
            service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);
        } finally {
            verify(notificationClient).sendEmail(anyString(), anyString(), anyMap(), anyString());
        }
    }

    @Test
    public void shouldSendEmailUsingPredefinedTemplate() throws Exception {
        service.sendMail(USER_EMAIL, TEMPLATE_ID, PARAMETERS, REFERENCE);

        verify(notificationClient).sendEmail(eq(TEMPLATE_ID), eq(USER_EMAIL), eq(PARAMETERS), eq(REFERENCE));
    }

    @Test
    public void recoveryShouldNotLogPII() {
        service.logNotificationFailure(
            new NotificationException("expected exception"),
            null,
            "hidden@email.com",
            null,
            "reference"
        );

        assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
        assertWasNotLogged("hidden@email.com");
    }

    @Test(expected = NotificationException.class)
    public void recoveryThrowWhenAsyncEnabled() {
        service = new NotificationService(notificationClient, appInsights, true);
        service.logNotificationFailure(
            new NotificationException("expected exception"),
            null,
            "hidden@email.com",
            null,
            "reference"
        );

        assertWasLogged("Failure: failed to send notification (reference) due to expected exception");
        assertWasNotLogged("hidden@email.com");
    }
}
