package io.github.autotweaker.adapter.actions.bootstrap

import io.github.autotweaker.adapter.actions.input.ActionInputs
import io.github.autotweaker.api.adapter.Agent
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.agent.ModelConfig
import io.github.autotweaker.api.types.llm.ReasoningEffort
import java.nio.file.Path
import java.util.*

object SessionBootstrap {
	suspend fun configure(core: CoreAPI, inputs: ActionInputs, modelId: UUID): Agent {
		val workspace = core.workspace.create("actions", workspacePath(inputs))
		val sessionId = core.session.create(workspace.id, modelConfig(inputs, modelId))
		val session = core.session.restore(sessionId)
		return session.restore(session.agentIndex.value.main.id)
	}
	
	private fun modelConfig(inputs: ActionInputs, modelId: UUID) = ModelConfig(
		model = modelId,
		reasoning = inputs.reasoning?.let { ReasoningEffort(it) },
		summarize = modelId,
		compact = modelId,
		fallback = emptyList(),
	)
	
	private fun workspacePath(inputs: ActionInputs): Path =
		Path.of(inputs.workspace ?: System.getenv("GITHUB_WORKSPACE"))
}
