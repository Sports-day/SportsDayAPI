val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "1.8.0"
    id("io.ktor.plugin") version "2.2.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

group = "net.sportsday"
version = "1.1.0"

application {
    mainClass.set("net.sportsday.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-rate-limit:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.auth0:jwks-rsa:0.22.0")
    implementation("com.auth0:java-jwt:4.3.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("io.ktor:ktor-server-host-common-jvm:2.2.2")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.2")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    implementation("redis.clients:jedis:4.4.1")
    //  Ktor client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    //  Koin
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(11)
}

ktlint {
    version.set("0.42.1")
    verbose.set(true)
    outputToConsole.set(true)
//    ignoreFailures.set(true)
    disabledRules.set(listOf("no-wildcard-imports"))
}

task("runSeeder", JavaExec::class) {
    group = "seeder"
    mainClass.set("net.sportsday.bin.SeederKt")
    classpath = sourceSets["main"].runtimeClasspath
}
