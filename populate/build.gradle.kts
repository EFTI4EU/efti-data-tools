plugins {
    id("data-tools.kotlin-conventions")
}

dependencies {
    api(project(":schema"))
    testImplementation(project(":test-support"))
}
