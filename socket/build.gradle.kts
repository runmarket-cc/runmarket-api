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
        image = "gudrb963/runmarket-pacer-socket"
        tags = setOf("latest")
    }
}

dependencies {
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    runtimeOnly(project(":infrastructure"))
    runtimeOnly(project(":event-bus"))
}
