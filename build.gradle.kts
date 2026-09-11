plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.kapt)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.toolgen)
}

val generatedVersionFile = layout.buildDirectory.file("generated/version/version.properties")

val generateVersionProperties = tasks.register("generateVersionProperties") {
	description = "Generates version.properties"
	val outputFile = generatedVersionFile
	val currentVersion = project.version.toString()
	outputs.file(outputFile)
	doLast {
		outputFile.get().asFile.apply {
			parentFile.mkdirs()
			writeText("version=$currentVersion")
		}
	}
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
	implementation(libs.autotweaker.api)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.datetime)
	
	implementation(libs.auto.service.annotations)
	kapt(libs.auto.service)
}

tasks.processResources {
	dependsOn(generateVersionProperties)
	from(generatedVersionFile.map { it.asFile.parentFile })
}
