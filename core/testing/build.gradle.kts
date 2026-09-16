// A pure-JVM module on purpose: nothing here touches Android, and keeping it off the Android
// plugin is what lets the JVM-only :core:domain consume it. Android modules can depend on a JVM
// library, but not the other way around.
plugins {
    alias(libs.plugins.pocket.jvm.library)
}

dependencies {
    // Test infrastructure is `api` so consuming modules get JUnit, Turbine and friends
    // transitively on their own test classpaths.
    api(projects.core.domain)
    api(projects.core.model)
    api(libs.junit4)
    api(libs.kotlin.test)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    implementation(libs.kotlinx.coroutines.core)
}
