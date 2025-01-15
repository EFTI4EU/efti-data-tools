import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val xmlunitVersion = "2.10.0"

plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testImplementation("org.xmlunit:xmlunit-matchers:$xmlunitVersion")
    testImplementation("org.xmlunit:xmlunit-jakarta-jaxb-impl:$xmlunitVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_ERROR,
            TestLogEvent.STANDARD_OUT,
        )
        exceptionFormat = TestExceptionFormat.FULL
    }

    // Always run task even if it has successfully completed earlier
    outputs.upToDateWhen { false }
}

tasks.register<Test>("updateTestExpectations") {
    useJUnitPlatform {
        includeTags("expectation-update")
        ignoreFailures = true
    }

    environment("eu.efti.updateTestExpectations", "true")

    // Always run task even if it has successfully completed earlier
    outputs.upToDateWhen { false }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "LocalMavenRepo"
            url = uri(rootProject.layout.buildDirectory.dir("local-maven-repo"))
        }
    }
}
