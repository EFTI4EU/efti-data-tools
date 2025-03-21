plugins {
    id("com.github.jk1.dependency-license-report") version "2.9"
}

group = "eu.efti.datatools"
version = project.property("version") as String

licenseReport {
    allowedLicensesFile = rootProject.file("allowed-licenses.json")
}

tasks.checkLicense {
    inputs.file("$rootDir/allowed-licenses.json")
}

tasks.register("check") {
    dependsOn(tasks.checkLicense)
}
