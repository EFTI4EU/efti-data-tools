plugins {
    id("data-tools.kotlin-conventions")
    id("application")
}

dependencies {
    implementation(project(":populate"))
}

application {
    mainClass.set("eu.efti.datatools.app.MainKt")
}