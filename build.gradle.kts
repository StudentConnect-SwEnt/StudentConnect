import java.util.Locale

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ktfmt) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.sonar)
}

sonar {
    properties {
        property("sonar.projectKey", "StudentConnect-SwEnt_StudentConnect")
        property("sonar.projectName", "StudentConnect")
        property("sonar.organization", "studentconnect-swent")
        property("sonar.host.url", "https://sonarcloud.io")

        val unitTestResultsFlavor =
            if (project.hasProperty("useResOverrideForUnitTestResults")) "ResOverride"
            else "Normal"

        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        val unitTestResultDirectory = "test${unitTestResultsFlavor}DebugUnitTest"
        property("sonar.junit.reportPaths", "${project(":app").layout.buildDirectory.get()}/test-results/${unitTestResultDirectory}/")

        val lintResultsFlavor =
            if (project.hasProperty("useResOverrideForLintResults")) "resOverride"
            else "normal"

        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        val lintResultsFile = "lint-results-${lintResultsFlavor}Debug.xml"
        property("sonar.androidLint.reportPaths", "${project(":app").layout.buildDirectory.get()}/reports/${lintResultsFile}")

        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project(":app").layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// Override Sonar's outdated bouncycastle version
buildscript {
    dependencies {
        classpath(libs.bouncycastle)
    }
}

