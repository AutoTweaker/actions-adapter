package io.github.autotweaker.adapter.actions

import com.google.auto.service.AutoService
import io.github.autotweaker.adapter.actions.bootstrap.ProviderBootstrap
import io.github.autotweaker.adapter.actions.bootstrap.SessionBootstrap
import io.github.autotweaker.adapter.actions.github.SessionMarkdown
import io.github.autotweaker.adapter.actions.github.WorkflowFile
import io.github.autotweaker.adapter.actions.input.ActionInputs
import io.github.autotweaker.adapter.actions.runtime.AgentRunner
import io.github.autotweaker.api.*
import io.github.autotweaker.api.adapter.Adapter
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.base.catching
import io.github.autotweaker.api.types.KebabCase.Companion.toKebab
import io.github.autotweaker.api.types.SemVer
import io.github.autotweaker.api.types.Url.Companion.toUrl
import io.github.autotweaker.api.types.adapter.AdapterInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.system.exitProcess

@AutoService(Adapter::class)
class ActionsAdapter : Adapter, Loggable, Traceable {
	private val info = AdapterInfo(
		name = "actions-adapter".toKebab(),
		description = "GitHub Actions integration adapter",
		version = version,
		source = "https://github.com/AutoTweaker/actions-adapter".toUrl(),
	)
	
	private val scope = scope()
	private lateinit var core: CoreAPI
	private var job: Job? = null
	
	override val isRunning get() = job?.isActive == true
	
	override suspend fun init(core: CoreAPI) = info.also { this.core = core }
	
	override suspend fun start() {
		job = scope.launch {
			trace.catching { run() }
				.ensureActive()
				.onFailure {
					log.error("Failed to complete the task", it)
					exitProcess(1)
				}
		}
	}
	
	override suspend fun stop() {
		job?.cancel()
	}
	
	private suspend fun run() {
		val inputs = ActionInputs.fromEnv()
		val modelId = ProviderBootstrap.configure(core, inputs)
		val agent = SessionBootstrap.configure(core, inputs, modelId)
		AgentRunner(core, agent, inputs.prompt).run()
		WorkflowFile.appendSummary(SessionMarkdown(core, agent.context.value.index).render())
		exitProcess(0)
	}
	
	companion object {
		private val version: SemVer by lazy {
			val props = Properties()
			javaClass.classLoader.getResourceAsStream("version.properties")?.use { props.load(it) }
			SemVer.parse(props.getProperty("version"))
		}
	}
}
