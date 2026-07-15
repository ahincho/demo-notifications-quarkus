plugins {
    java
    id("io.quarkus")
    id("checkstyle")
}

repositories {
    mavenLocal()
    mavenCentral()
    // GitHub Packages of the Quarkus extension (cross-repo dependency).
    // Same rationale as demo-notifications-spring-boot/build.gradle.kts:9-30.
    maven {
        name = "GitHubPackages-NovaNotifications-Quarkus"
        url = uri("https://maven.pkg.github.com/ahincho/nova-java-notifications-quarkus-extension")
        val token = System.getenv("NOVA_PACKAGES_READ_TOKEN")
            ?: System.getenv("NOVA_RELEASE_PAT")
            ?: System.getenv("GITHUB_TOKEN")
        if (!token.isNullOrBlank()) {
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "x-access-token"
                password = token
            }
        }
    }
    maven {
        name = "GitHubPackages-NovaNotifications-Core"
        url = uri("https://maven.pkg.github.com/ahincho/nova-java-notifications")
        val token = System.getenv("NOVA_PACKAGES_READ_TOKEN")
            ?: System.getenv("NOVA_RELEASE_PAT")
            ?: System.getenv("GITHUB_TOKEN")
        if (!token.isNullOrBlank()) {
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: "x-access-token"
                password = token
            }
        }
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-arc")

    // The Nova Quarkus extension (colloquial, locally published). Exposes
    // NotificationFacade as a @Singleton CDI bean.
    implementation("pe.edu.nova.java.starters:nova-notifications-quarkus-extension:1.0.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("io.rest-assured:rest-assured")
}

group = "pe.edu.nova"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    suppressedValidationErrors.add("enforced-platform")
}

checkstyle {
    toolVersion = "10.20.1"
    configFile = file("config/checkstyle/checkstyle.xml")
}