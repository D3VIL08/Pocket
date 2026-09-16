package dev.pocket.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Settings shared by every Android module, applied from the application and library convention
 * plugins so no module has to repeat them.
 *
 * Note the property-access style: as of AGP 9 `CommonExtension` is no longer generic and exposes
 * these sub-DSLs as plain getters — the `compileOptions { }` style lambdas are declared on the
 * concrete `ApplicationExtension` / `LibraryExtension` types instead.
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    with(commonExtension) {
        compileSdk = libs.intVersion("compileSdk")
        defaultConfig.minSdk = libs.intVersion("minSdk")

        val javaVersion = JavaVersion.toVersion(libs.version("javaTarget"))
        compileOptions.sourceCompatibility = javaVersion
        compileOptions.targetCompatibility = javaVersion
        // minSdk 26 gives us java.time natively, so no core library desugaring is needed.

        testOptions.unitTests.isIncludeAndroidResources = true
        testOptions.unitTests.isReturnDefaultValues = true

        lint.abortOnError = true
        lint.warningsAsErrors = false
        lint.checkDependencies = true
        lint.checkReleaseBuilds = true
        // These report that a newer dependency exists. The versions here are pinned deliberately
        // to the newest that support compileSdk 36 (see the toolchain note in README.md), so the
        // nag is noise that would fail CI on somebody else's release schedule rather than on
        // anything wrong with this code.
        lint.disable += setOf(
            "GradleDependency",
            "NewerVersionAvailable",
            "AndroidGradlePluginVersion",
            "OldTargetApi",
            // Suggests merging mipmap-anydpi-v26 into mipmap-anydpi because minSdk is already 26.
            // Doing so makes the resource merger drop the adaptive icon entirely and the build
            // fails with "resource mipmap/ic_launcher not found" — the qualifier is load-bearing.
            "ObsoleteSdkInt",
        )

        packaging.resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/LICENSE.md",
            "/META-INF/LICENSE-notice.md",
            "META-INF/versions/9/previous-compilation-data.bin",
        )
    }

    configureKotlin<KotlinAndroidProjectExtension>()
}

/** Settings shared by every pure-JVM module. */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        val javaVersion = JavaVersion.toVersion(libs.version("javaTarget"))
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
    val warningsAsErrors = providers.gradleProperty("pocket.warningsAsErrors").orNull.toBoolean()
    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> error("Unsupported project extension $this")
    }.apply {
        jvmTarget = JvmTarget.fromTarget(libs.version("javaTarget"))
        allWarningsAsErrors = warningsAsErrors
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }
}
