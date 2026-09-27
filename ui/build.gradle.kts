import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(25)

    android {
        namespace = "${project.group}.ui"
        compileSdk = 37
        minSdk = 28
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.resources)
            api(project(":core"))
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation(libs.ui.test)
            implementation(libs.navigationevent.compose)
            runtimeOnly(libs.junit.platform.launcher)
        }
    }
}

tasks.withType<Test>().configureEach {
    systemProperty("rwx.ui.windowTests", providers.systemProperty("rwx.ui.windowTests").getOrElse("false"))
}

// WORKAROUND (CMP-9547): Compose Multiplatform 1.12.0 doesn't wire
// CopyResourcesToAndroidAssetsTask.outputDirectory when the Android target uses
// com.android.kotlin.multiplatform.library (AGP 9.x), so :ui composeResources
// never reach the APK and the app crashes with MissingResourceException.
// Set the output dir via reflection; remove once upstream fixes it.
project.afterEvaluate {
    tasks.named("copyAndroidMainComposeResourcesToAndroidAssets").configure {
        val outputDir = project.layout.buildDirectory.dir(
            "generated/compose/resourceGenerator/androidMain/assets"
        )
        val method = this::class.java.getMethod("getOutputDirectory")
        val property = method.invoke(this) as org.gradle.api.file.DirectoryProperty
        property.set(outputDir)
    }
}
