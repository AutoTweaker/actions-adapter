plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.kapt)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.autotweaker.plugin.sdk)
}

kotlin {
	jvmToolchain(25)
}

repositories {
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/AutoTweaker/core")
		credentials {
			username = providers.gradleProperty("gpr.user").getOrElse("")
			password = providers.gradleProperty("gpr.key").getOrElse("")
		}
	}
}

dependencies {
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.datetime)
	
	implementation(libs.auto.service.annotations)
	kapt(libs.auto.service)
}
