plugins {
    alias(libs.plugins.pocket.android.library)
    alias(libs.plugins.pocket.android.compose)
}

android {
    namespace = "dev.pocket.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.collections.immutable)
    // PreviewData exposes PreviewParameterProvider from its public API.
    api(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.robolectric)
}
