plugins {
    alias(libs.plugins.pocket.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
