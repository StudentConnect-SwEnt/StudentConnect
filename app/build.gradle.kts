import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Locale

plugins {
    jacoco
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinComposeCompiler)
}

val useFirebaseEmulator: Boolean =
    (project.findProperty("useFirebaseEmulator") as? String)?.equals("true", ignoreCase = true)
        ?: false

android {
    namespace = "com.github.se.studentconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.github.se.studentconnect"
        minSdk = 29
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Configure test sharding from gradle properties
        val shardIndex = project.findProperty("testShardIndex")?.toString()?.toIntOrNull()
        val numShards = project.findProperty("testNumShards")?.toString()?.toIntOrNull()

        if (shardIndex != null && numShards != null) {
            testInstrumentationRunnerArguments["numShards"] = numShards.toString()
            testInstrumentationRunnerArguments["shardIndex"] = shardIndex.toString()
        }
    }

    signingConfigs {
        create("release") {
            // Use keystore for CI builds to maintain consistent SHA-1 fingerprint
            // This allows the APK to work with Firebase without additional configuration
            val keystorePassword = project.findProperty("ANDROID_KEYSTORE_PASSWORD")?.toString()

            if (keystorePassword == null) {
                logger.warn("ANDROID_KEYSTORE_PASSWORD is not defined, will not sign the APK...")
                return@create
            }

            val keystoreFilePath = "${System.getProperty("user.home")}/.android/release.keystore"
            val keystoreFile = file(keystoreFilePath)

            if (!keystoreFile.exists()) {
                logger.error("Keystore file \"$keystoreFilePath\" does not exist")
                return@create
            }

            storeFile = keystoreFile
            storePassword = keystorePassword
            keyAlias = "release"
            keyPassword = keystorePassword // same as storePassword for PKCS12
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            buildConfigField("Boolean", "USE_MOCK_MAP", "false")
            buildConfigField("Boolean", "USE_FIREBASE_EMULATOR", "false")

            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            // Use real map in debug builds, mock map for android tests
            buildConfigField("Boolean", "USE_MOCK_MAP", "false")
            buildConfigField(
                "Boolean", "USE_FIREBASE_EMULATOR", useFirebaseEmulator.toString())
        }
    }
    flavorDimensions += "env"
    productFlavors {
        create("normal") {
            dimension = "env"
        }
        create("resOverride") {
            dimension = "env"
            // Ensure we reuse dependencies and fallbacks from the normal flavor
            matchingFallbacks += listOf("normal")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        packaging {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets {
        getByName("test") {
            java.srcDirs("src/test/java")
            resources.srcDirs("src/test/resources")
        }

        getByName("testDebug") {
            java.srcDirs("src/testDebug/java")
            resources.srcDirs("src/testDebug/resources")
        }
        

    }
}




dependencies {
    // Core
    implementation(libs.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    // Jetpack Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.play.services.location)
    implementation(libs.androidx.work.testing)
    implementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.test.core.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)
    // Maps
    implementation("com.mapbox.maps:android-ndk27:11.15.2")
    implementation("com.mapbox.extension:maps-compose-ndk27:11.15.2")

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Google Service and Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.auth)

    // Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)

    // Credential Manager (for Google Sign-In)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Networking with OkHttp
    implementation(libs.okhttp)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Use Guava Android variant for ListenableFuture support
    implementation(libs.guava)
    implementation(libs.mlkit.barcode.scanning)

    // QR Code generation
    implementation(libs.compose.qr.code)

    // WorkManager for background tasks
    implementation(libs.androidx.work.runtime.ktx)

    // Testing Unit
    testImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    testImplementation(libs.mockk)
    testImplementation(libs.json)
    testImplementation(libs.androidx.arch.core.testing)

    // Compose testing for Robolectric unit tests
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Test UI
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.allure.support)
    androidTestImplementation(libs.kaspresso.compose.support)

    testImplementation(libs.kotlinx.coroutines.test)
}

configurations.all {
    resolutionStrategy {
        // Enforce a single Guava version (Android flavor)
        force(libs.guava.get().toString())
        // Force consistent test library versions to avoid conflicts
        force("androidx.test.ext:junit:1.2.1")
        force("androidx.test.espresso:espresso-core:3.6.1")
    }
}

tasks.withType<Test>().configureEach {
    // Disable per-test logging
    testLogging {
        events() // no events -> suppress "PASSED"/"FAILED"/"SKIPPED" lines
    }

    // Print only a summary after all tests
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
            if (desc.parent == null) {
                println(
                    """
                    |
                    |Test summary:
                    |  Total: ${result.testCount}
                    |  Passed: ${result.successfulTestCount}
                    |  Failed: ${result.failedTestCount}
                    |  Skipped: ${result.skippedTestCount}
                    """.trimMargin()
                )
            }
        }
    })

    // Keep Jacoco config
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    val coverageFlavor = (project.findProperty("coverageFlavor")?.toString() ?: "normal").let {
        if (it !in listOf("normal", "resOverride")) {
            logger.warn("Unknown coverage flavor '$it', defaulting to 'normal'")
            return@let "normal"
        }
        return@let it
    }

    val coverageFlavorCapitalized = coverageFlavor.replaceFirstChar { it.titlecase(Locale.ROOT) }

    mustRunAfter("test${coverageFlavorCapitalized}DebugUnitTest", "connected${coverageFlavorCapitalized}DebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/sigchecks/**",
    )
    val debugTree = fileTree("${project.layout.buildDirectory.get().asFile}/tmp/kotlin-classes/${coverageFlavor}Debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get().asFile) {
        include("outputs/unit_test_code_coverage/${coverageFlavor}DebugUnitTest/test${coverageFlavorCapitalized}DebugUnitTest.exec")
        include("outputs/code_coverage/${coverageFlavor}DebugAndroidTest/connected/*/coverage.ec")
    })
}

configurations.forEach { configuration ->
    // Exclude protobuf-lite from all configurations
    // This fixes a fatal exception for tests interacting with Cloud Firestore
    configuration.exclude("com.google.protobuf", "protobuf-lite")
}
