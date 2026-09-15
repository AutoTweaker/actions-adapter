import io.github.autotweaker.plugin.versioning.VersionMode

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("io.github.autotweaker.plugin.versioning") version "2.0.1"
}

versioning {
	versionMode.set(VersionMode.RAW)
}

rootProject.name = "actions-adapter"
