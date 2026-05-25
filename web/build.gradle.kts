plugins {
    java
    id("org.springframework.boot")
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:25-jre-alpine"
    }
    to {
        image = "gudrb963/runmarket-pacer"
        tags = setOf("latest")
    }
}

dependencies {
    implementation(project(":application"))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly(project(":infrastructure"))
    runtimeOnly(project(":event-bus"))
}
