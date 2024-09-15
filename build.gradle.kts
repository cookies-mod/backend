plugins {
    id("java")
    id("org.springframework.boot").version("3.3.3")
    id("io.spring.dependency-management").version("1.1.6")
    kotlin("jvm")
    `cookies-get-version`
}

group = "dev.morazzer.cookies"
version = "0.0.4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations.compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/cookies-mod/entities") {
        credentials {
            username = project.findProperty("gpr.usr") as String? ?: System.getenv("USER")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // Redis cache
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // web + auth
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // jwt and json
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.jsonwebtoken:jjwt:0.12.6")

    // Websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Messaging between server instances
    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Communication with client
    implementation("dev.morazzer.cookies:entities:0.2.0")

    // Lombok (not much to say)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    //Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))

}

tasks.test {
    useJUnitPlatform()
}

springBoot {
    buildInfo()
}