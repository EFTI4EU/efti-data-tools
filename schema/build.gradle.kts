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
    implementation("org.apache.xmlbeans:xmlbeans:5.2.1")

    runtimeOnly(platform("org.apache.logging.log4j:log4j-bom:2.24.1"))
    runtimeOnly("org.apache.logging.log4j:log4j-core")

    testImplementation(project(":test-support"))
}
