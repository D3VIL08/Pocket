plugins {
    alias(libs.plugins.pocket.android.library)
    alias(libs.plugins.pocket.android.compose)
}

android {
    namespace = "dev.pocket.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.robolectric)
}
