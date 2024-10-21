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
}
