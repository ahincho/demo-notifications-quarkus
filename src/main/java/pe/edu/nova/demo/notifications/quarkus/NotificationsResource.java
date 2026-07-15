package pe.edu.nova.demo.notifications.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import pe.edu.nova.java.libs.notifications.application.facade.NotificationFacade;
import pe.edu.nova.java.libs.notifications.domain.model.EmailNotification;
import pe.edu.nova.java.libs.notifications.domain.result.NotificationResult;
import pe.edu.nova.java.libs.notifications.domain.vo.EmailAddress;
import pe.edu.nova.java.libs.notifications.domain.vo.MessageBody;
import pe.edu.nova.java.libs.notifications.domain.vo.Subject;

/**
 * JAX-RS resource that triggers a simulated email send through the Nova
 * Notifications library. The {@code NotificationFacade} is auto-wired
 * by the Quarkus colloquial extension.
 *
 * <p>The resource returns a {@code Map<String, Object>} rather than the
 * library's {@code NotificationResult} directly because the library is
 * framework-agnostic and does not carry Jackson or Quarkus Jackson
 * annotations. Wrapping the result in a simple map keeps the library
 * decoupled from the demo's JSON layer. The shape mirrors the relevant
 * {@code NotificationResult} fields (sent status, provider message id,
 * channel, error code, error message).
 */
@Path("/api/notifications")
public class NotificationsResource {

    @Inject
    NotificationFacade facade;

    /** Visible for unit tests: package-private no-arg constructor. */
    NotificationsResource() {
    }

    @GET
    @Path("/email/welcome")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> sendWelcomeEmail() {
        EmailNotification email = EmailNotification.builder()
                .from(new EmailAddress("no-reply@example.com"))
                .to(new EmailAddress("customer@example.com"))
                .subject(new Subject("Welcome"))
                .body(new MessageBody("Thanks for signing up to Nova."))
                .build();
        NotificationResult result = facade.send(email);
        Map<String, Object> body = new HashMap<>();
        body.put("sent", result.isSent());
        body.put("providerMessageId", result.providerMessageId().orElse(null));
        body.put("channel", result.channel().toString().toLowerCase());
        body.put("errorCode", result.errorCode().map(Enum::name).orElse(null));
        body.put("errorMessage", result.errorMessage().orElse(null));
        return body;
    }
}
