plugins {
    kotlin("jvm") version "2.2.20"
    id("io.kotest") version "6.0.3"
}

group = "pl.wst.training"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:6.1.0")
    implementation("com.googlecode.lanterna:lanterna:3.1.3")
    implementation("com.ezylang:EvalEx:3.1.0")

    testImplementation("io.kotest:kotest-framework-engine:6.0.3")
    testImplementation("io.kotest:kotest-assertions-core:6.0.3")
    testImplementation("io.kotest:kotest-runner-junit5:6.0.3")
}

tasks.test {
    useJUnitPlatform()
}
