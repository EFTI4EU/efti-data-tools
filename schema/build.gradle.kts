plugins {
    id("data-tools.kotlin-conventions")
}

sourceSets {
    main {
        resources {
            srcDir(rootProject.file("xsd"))
        }
    }
}

dependencies {
    implementation("org.apache.xmlbeans:xmlbeans:5.3.0")

    runtimeOnly(platform("org.apache.logging.log4j:log4j-bom:2.24.1"))
    runtimeOnly("org.apache.logging.log4j:log4j-core")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            artifactId = "schema"
            version = rootProject.version as String

            from(components["java"])
        }
    }
}
