plugins {
    alias(libs.plugins.pocket.android.library)
    alias(libs.plugins.pocket.android.hilt)
    alias(libs.plugins.pocket.android.room)
}

android {
    namespace = "dev.pocket.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
}
