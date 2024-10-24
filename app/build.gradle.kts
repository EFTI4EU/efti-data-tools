plugins {
    id("data-tools.kotlin-conventions")
    id("application")
}

dependencies {
    implementation(project(":populate"))
    implementation("com.beust:jcommander:1.82")
}

application {
    mainClass.set("eu.efti.datatools.app.MainKt")
}
