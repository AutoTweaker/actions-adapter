package io.github.autotweaker.adapter.actions

import io.github.autotweaker.api.types.SemVer
import java.util.*

object ResourcesLoader {
	val version: SemVer by lazy {
		val props = Properties()
		javaClass.classLoader.getResourceAsStream("version.properties")?.use { props.load(it) }
		SemVer.parse(props.getProperty("version"))
	}
}
