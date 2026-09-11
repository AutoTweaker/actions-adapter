package io.github.autotweaker.adapter.actions.runtime

import io.github.autotweaker.api.adapter.Agent
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.agent.AgentMessage
import io.github.autotweaker.api.types.tool.ToolApprove

class ToolAutoApprover(
	private val core: CoreAPI,
	private val agent: Agent,
) {
	suspend fun run(): Nothing =
		agent.context.collect { ctx ->
			val ids = ctx.index.currentRound?.pendingToolCalls ?: return@collect
			core.persistence.loadMessages(ids.toSet())
				.filterIsInstance<AgentMessage.Tool.Call>()
				.forEach { agent.approve(ToolApprove(it.callId)) }
		}
}
