package io.github.autotweaker.adapter.actions.github

import io.github.autotweaker.api.adapter.Agent
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.base.session.diff
import io.github.autotweaker.api.base.unifiedDiff
import io.github.autotweaker.api.types.agent.AgentContext
import io.github.autotweaker.api.types.agent.AgentContextIndex.Turn
import io.github.autotweaker.api.types.agent.AgentMessage
import io.github.autotweaker.api.types.agent.AgentOutput
import io.github.autotweaker.api.types.agent.AgentStatus
import io.github.autotweaker.api.types.llm.ContentPart
import io.github.autotweaker.api.types.tool.ToolPresentation
import io.github.autotweaker.api.types.tool.UiBlock
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class SessionLogger(
	private val core: CoreAPI,
	private val agent: Agent,
) {
	private var latest: AgentContext = agent.context.value
	private val showed = mutableSetOf<UUID>()
	
	suspend fun run(): Unit = coroutineScope {
		launch { forwardOutputs() }
		agent.context.collect { context ->
			agent.status.first { it != AgentStatus.THINKING }
			val diff = latest diff context
			latest = context
			diff ?: return@collect
			
			diff.addedHistoryRounds()?.forEach { round ->
				round.userMessage.printIfNew()
				round.turns?.forEach { it.print() }
				round.finalAssistantMessage?.printIfNew()
			}
			
			diff.startedRound()?.let { current ->
				current.userMessage.printIfNew()
				current.turns?.forEach { it.print() }
				current.assistantMessage?.printIfNew()
				current.finishedToolCalls?.forEach { it.result.printIfNew() }
			}
			
			diff.updatedCurrent()?.let { current ->
				current.addedTurns()?.forEach { it.print() }
				current.newAssistantMessage()?.printIfNew()
				current.addedFinishedCalls()?.forEach { it.result.printIfNew() }
			}
		}
	}
	
	private suspend fun forwardOutputs(): Unit = agent.output.collect { output ->
		when (output) {
			is AgentOutput.Error -> WorkflowCommand.error(output.message)
			
			is AgentOutput.LlmError -> WorkflowCommand.warning(
				listOfNotNull(
					output.statusCode?.let { "[HTTP $it]" },
					output.content,
					output.exception?.message,
				).joinToString(" ")
			)
			
			else -> {}
		}
	}
	
	private suspend fun Turn.print() {
		assistantMessage.printIfNew()
		tools.forEach { it.result.printIfNew() }
	}
	
	private suspend fun UUID.printIfNew() {
		if (showed.add(this)) print(this)
	}
	
	private suspend fun print(id: UUID) {
		core.persistence.loadMessages(setOf(id)).firstOrNull()?.let(::print)
	}
	
	private fun print(message: AgentMessage) = when (message) {
		is AgentMessage.User -> message.content.content
			?.filterIsInstance<ContentPart.Text>()
			?.forEach { WorkflowCommand.notice("> ${it.content}") }
		
		is AgentMessage.Assistant -> {
			message.reasoning?.let(WorkflowCommand::debug)
			message.content?.let(WorkflowCommand::notice)
		}
		
		is AgentMessage.Tool.Result -> print(message.presentation)
		
		else -> {}
	}
	
	private fun print(presentation: ToolPresentation) = presentation.forEach(::print)
	
	private fun print(block: UiBlock) = when (block) {
		is UiBlock.Text -> emit(block.content)
		is UiBlock.Command -> emit(block.command)
		is UiBlock.Diff -> unifiedDiff(block.oldContent, block.newContent)?.let(::emit)
		is UiBlock.Error -> WorkflowCommand.error(block.content)
		is UiBlock.Output -> emit(block.content)
	}
	
	private fun emit(text: String) = println(text)
}
