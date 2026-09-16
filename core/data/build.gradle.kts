plugins {
    alias(libs.plugins.pocket.android.library)
    alias(libs.plugins.pocket.android.hilt)
}

android {
    namespace = "dev.pocket.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.model)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    // Room is deliberately not on the main compile classpath here — the repository talks to
    // DAOs only. The tests need it to stand up a real in-memory database to test against.
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
}
