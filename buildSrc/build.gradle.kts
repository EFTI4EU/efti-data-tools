plugins {
    `kotlin-dsl`
}

val kotlinVersion = "2.0.10"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
    implementation("com.github.jk1:gradle-license-report:2.9")
}
