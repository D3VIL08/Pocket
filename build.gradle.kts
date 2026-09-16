plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
}

// detekt runs over every module from the root, so `./gradlew detekt` is one task for the repo.
subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        parallel = true
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        basePath = rootProject.projectDir.absolutePath
    }

    dependencies {
        add("detektPlugins", rootProject.libs.detekt.formatting)
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        jvmTarget = rootProject.libs.versions.javaTarget.get()
        // `./gradlew detekt -PdetektAutoCorrect=true` rewrites the formatting violations
        // (import order, indentation, trailing whitespace) in place. CI never passes it, so a
        // formatting slip still fails the build there rather than being silently rewritten.
        autoCorrect = providers.gradleProperty("detektAutoCorrect").orNull.toBoolean()
        reports {
            html.required.set(true)
            xml.required.set(true)
            sarif.required.set(false)
            txt.required.set(false)
            md.required.set(false)
        }
        exclude("**/build/**", "**/resources/**")
    }

    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        jvmTarget = rootProject.libs.versions.javaTarget.get()
    }
}
