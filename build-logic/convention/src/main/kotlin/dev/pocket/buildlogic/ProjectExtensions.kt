package dev.pocket.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** The single `gradle/libs.versions.toml` catalog, reachable from inside convention plugins. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow { IllegalStateException("Missing version alias '$alias' in libs.versions.toml") }
        .requiredVersion

internal fun VersionCatalog.intVersion(alias: String): Int = version(alias).toInt()
