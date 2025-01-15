plugins {
    id("data-tools.kotlin-conventions")
}

dependencies {
    api(project(":schema"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = "populate"
            version = rootProject.version as String

            from(components["java"])
        }
    }
}
