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

val jjwt = "0.13.0"
val tes

dependencies {
    implementation(project(":application"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.jsonwebtoken:jjwt-api:$jjwt")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwt")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwt")
    runtimeOnly(project(":infrastructure"))
    runtimeOnly(project(":event-bus"))
}
