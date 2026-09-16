import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.HostTestBuilder
import dev.pocket.buildlogic.configureKotlinAndroid
import dev.pocket.buildlogic.intVersion
import dev.pocket.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            testOptions.targetSdk = libs.intVersion("targetSdk")
        }

        extensions.configure<LibraryAndroidComponentsExtension> {
            beforeVariants { variant ->
                // Nothing here ships instrumented tests — UI tests run under Robolectric on the
                // JVM — so the androidTest variant is dead weight in the task graph.
                variant.androidTest.enable = false

                // Unit tests only run against debug. Running the identical JVM suite twice buys
                // nothing, and Compose's ui-test-manifest is a debugImplementation dependency by
                // design, so the release unit-test variant cannot host a Compose test at all.
                if (variant.buildType == "release") {
                    variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = false
                }
            }
        }
    }
}
