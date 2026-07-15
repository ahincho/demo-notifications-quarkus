package pe.edu.nova.demo.notifications.quarkus;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
 */
@Path("/api/notifications")
public class NotificationsResource {

    @Inject
    NotificationFacade facade;

    @GET
    @Path("/email/welcome")
    @Produces(MediaType.APPLICATION_JSON)
    public NotificationResult sendWelcomeEmail() {
        EmailNotification email = EmailNotification.builder()
                .from(new EmailAddress("no-reply@example.com"))
                .to(new EmailAddress("customer@example.com"))
                .subject(new Subject("Welcome"))
                .body(new MessageBody("Thanks for signing up to Nova."))
                .build();
        return facade.send(email);
    }
}
