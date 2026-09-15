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
}

dependencies {
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.datetime)
	
	implementation(libs.auto.service.annotations)
	kapt(libs.auto.service)
}
