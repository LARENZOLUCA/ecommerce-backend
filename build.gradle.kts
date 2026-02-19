import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    // Убраны дубликаты!
}

group = "com.ecommerce"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Ktor - единая версия 2.3.12
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-auth:2.3.12")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.12")
    implementation("io.ktor:ktor-server-config-yaml:2.3.12")
    implementation("io.ktor:ktor-server-swagger:2.3.12")
    implementation("io.ktor:ktor-server-openapi:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")

    // Swagger аннотации
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")

    // Database
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.41.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.16.0")

    // Сериализация
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")

    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.18.0")

    // Security
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.8")

    // Тесты
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.22")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.8.22")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:rabbitmq:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation(project)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}