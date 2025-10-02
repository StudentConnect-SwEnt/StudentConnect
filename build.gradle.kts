// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("org.sonarqube") version "6.3.1.5724"
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

sonar {
  properties {
    property("sonar.projectKey", "StudentConnect-SwEnt_StudentConnect")
    property("sonar.organization", "studentconnect-swent")
  }
}