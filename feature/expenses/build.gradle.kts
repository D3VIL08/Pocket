plugins {
    alias(libs.plugins.pocket.android.feature)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.pocket.feature.expenses"
}

dependencies {
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
}
