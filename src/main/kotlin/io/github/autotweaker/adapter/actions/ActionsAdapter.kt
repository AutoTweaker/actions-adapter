package io.github.autotweaker.adapter.actions

import com.google.auto.service.AutoService
import io.github.autotweaker.api.adapter.Adapter
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.KebabCase.Companion.toKebab
import io.github.autotweaker.api.types.Url.Companion.toUrl
import io.github.autotweaker.api.types.adapter.AdapterInfo

@AutoService(Adapter::class)
class ActionsAdapter : Adapter {
	private var running = false
	private lateinit var core: CoreAPI
	
	override val isRunning: Boolean
		get() = running
	
	override suspend fun init(core: CoreAPI): AdapterInfo {
		this.core = core
		return AdapterInfo(
			name = "actions-adapter".toKebab(),
			description = "GitHub Actions integration adapter",
			version = ResourcesLoader.version,
			source = "https://github.com/AutoTweaker/actions-adapter".toUrl(),
		)
	}
	
	override suspend fun start() {
		running = true
	}
	
	override suspend fun stop() {
		running = false
	}
}
