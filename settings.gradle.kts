import io.github.autotweaker.plugin.versioning.VersionMode

pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/AutoTweaker/core")
			credentials {
				username = providers.gradleProperty("gpr.user").getOrElse("")
				password = providers.gradleProperty("gpr.key").getOrElse("")
			}
		}
		maven {
			name = "GitHubPackagesVersioning"
			url = uri("https://maven.pkg.github.com/AutoTweaker/gradle-versioning")
			credentials {
				username = providers.gradleProperty("gpr.user").getOrElse("")
				password = providers.gradleProperty("gpr.key").getOrElse("")
			}
		}
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("io.github.autotweaker.plugin.versioning") version "2.0.0"
}

versioning {
	versionMode.set(VersionMode.RAW)
}

rootProject.name = "actions-adapter"
