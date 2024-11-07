import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.github.jk1.dependency-license-report")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
//    testImplementation(platform("org.junit:junit-bom:5.11.2"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation("org.hamcrest:hamcrest-core:2.2")
//    testImplementation("org.xmlunit:xmlunit-matchers:2.10.0")
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
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
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

licenseReport {
    allowedLicensesFile = rootProject.file("allowed-licenses.json")
}

tasks.checkLicense {
    inputs.file("$rootDir/allowed-licenses.json")
}

tasks.check {
    dependsOn(tasks.checkLicense)
}
