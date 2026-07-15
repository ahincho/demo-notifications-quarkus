package pe.edu.nova.demo.notifications.quarkus;

import io.quarkus.runtime.Quarkus;

/**
 * Quarkus main class. The {@code nova-notifications-quarkus-extension} is on
 * the classpath, so {@code NotificationFacade} is exposed as a @Singleton
 * CDI bean and ready to inject.
 */
public class DemoApplication {

    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
