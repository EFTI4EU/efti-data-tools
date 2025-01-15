import java.net.URI

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    maven {
        url = URI("file://$rootDir/../../build/local-maven-repo")
    }
    mavenCentral()
}

dependencies {
    implementation("eu.efti.datatools:schema:0.1.0")

    testImplementation(platform("org.junit:junit-bom:5.11.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("eu.efti.datatools:populate:0.1.0")
}

tasks.test {
    useJUnitPlatform()
}
