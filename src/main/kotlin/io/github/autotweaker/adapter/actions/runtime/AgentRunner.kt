package io.github.autotweaker.adapter.actions.runtime

import io.github.autotweaker.adapter.actions.github.SessionLogger
import io.github.autotweaker.api.adapter.Agent
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.agent.AgentStatus
import io.github.autotweaker.api.types.agent.ContextInjection
import io.github.autotweaker.api.types.agent.MessageContent
import io.github.autotweaker.api.types.llm.toContentPart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AgentRunner(
	private val core: CoreAPI,
	private val agent: Agent,
	private val prompt: String,
) {
	suspend fun run() = coroutineScope {
		val logger = SessionLogger(core, agent)
		val approveJob = launch { ToolAutoApprover(core, agent).run() }
		val loggerJob = launch { logger.run() }
		agent.send(
			MessageContent(
				content = prompt.toContentPart(),
				injections = listOf(
					ContextInjection(
						"environment",
						"The current environment is a GitHub Actions runner. " +
								"No user is available to interact with. " +
								"You must complete the task autonomously " +
								"and must not stop to ask the user for confirmation."
					)
				),
			)
		).await()
		delay(5.milliseconds)
		agent.status.first { it.stopped }
		approveJob.cancel()
		if (agent.status.value != AgentStatus.FAILED) {
			agent.context.first { it.index.currentRound == null }
		}
		loggerJob.cancel()
	}
}
