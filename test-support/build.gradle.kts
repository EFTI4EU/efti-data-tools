plugins {
    id("data-tools.kotlin-conventions")
}

dependencies {
    api(platform("org.junit:junit-bom:5.11.2"))
    api("org.junit.jupiter:junit-jupiter")
    api("org.hamcrest:hamcrest-core:2.2")
    api("org.xmlunit:xmlunit-matchers:2.10.0")
}
