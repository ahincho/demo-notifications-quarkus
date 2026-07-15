package pe.edu.nova.demo.notifications.quarkus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import pe.edu.nova.java.libs.notifications.application.facade.NotificationFacade;
import pe.edu.nova.java.libs.notifications.domain.vo.EmailAddress;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.EmailConfiguration;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.EmailProvider;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.NotificationConfiguration;
import pe.edu.nova.java.libs.notifications.infrastructure.configuration.ResilienceConfiguration;

/**
 * Unit test for the Quarkus demo's {@link NotificationsResource}. Builds a
 * real {@link NotificationFacade} from a manually-constructed
 * {@link NotificationConfiguration} and exercises the resource's
 * {@code sendWelcomeEmail} method directly.
 *
 * <p>The full HTTP integration coverage for the Quarkus demo is provided
 * by the {@code quarkus:dev} / {@code quarkusRun} workflow, not by a
 * {@code @QuarkusTest} class. The reason: the Quarkus colloquial
 * extension relies on {@code @ConfigMapping} with nested interfaces, and
 * the SmallRye Config binding for nested {@code @ConfigMapping} in
 * Quarkus 3.33.x requires each nested interface to be a separate
 * top-level {@code @ConfigMapping} bean (a non-trivial refactor in the
 * extension). The extension module's own {@code QuarkusExtensionUnitTest}
 * covers the producer logic in isolation; this test covers the resource
 * wiring in isolation.
 */
class DemoApplicationTest {

    @Test
    void resourceBuildsAndSendsEmail() {
        NotificationConfiguration configuration = NotificationConfiguration.builder()
                .email(EmailConfiguration.builder()
                        .provider(EmailProvider.SENDGRID)
                        .apiKey("test-api-key-demo")
                        .defaultSender(new EmailAddress("no-reply@example.com"))
                        .build())
                .resilience(ResilienceConfiguration.disabled())
                .build();
        NotificationFacade facade = NotificationFacade.create(configuration);
        NotificationsResource resource = new NotificationsResource();
        resource.facade = facade;

        var body = resource.sendWelcomeEmail();

        assertNotNull(body);
        assertEquals(Boolean.TRUE, body.get("sent"));
        assertNotNull(body.get("providerMessageId"));
    }
}