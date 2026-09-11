package io.github.autotweaker.adapter.actions.github

import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.base.unifiedDiff
import io.github.autotweaker.api.types.agent.AgentContextIndex
import io.github.autotweaker.api.types.agent.AgentMessage
import io.github.autotweaker.api.types.llm.ContentPart
import io.github.autotweaker.api.types.tool.ToolPresentation
import io.github.autotweaker.api.types.tool.UiBlock
import java.util.*

class SessionMarkdown(
	private val core: CoreAPI,
	private val ctx: AgentContextIndex,
) {
	private val messages = mutableMapOf<UUID, AgentMessage>()
	private val markdown = StringBuilder()
	
	suspend fun render(): String {
		loadAll()
		ctx.compactedRounds?.toList()?.forEach { (summarizedMessage, rounds) ->
			appendCompleted(rounds)
			appendMessage(summarizedMessage)
		}
		appendCompleted(ctx.historyRounds)
		ctx.currentRound?.let { current ->
			appendMessage(current.userMessage)
			appendTurn(current.turns)
			appendMessage(current.assistantMessage)
		}
		return markdown.toString()
	}
	
	private fun appendCompleted(rounds: List<AgentContextIndex.CompletedRound>?) {
		rounds?.forEach { round ->
			appendMessage(round.userMessage)
			appendTurn(round.turns)
			appendMessage(round.finalAssistantMessage)
		}
	}
	
	private fun appendTurn(turns: List<AgentContextIndex.Turn>?) {
		turns?.forEach { turn ->
			appendMessage(turn.assistantMessage)
			turn.tools.forEach { tool ->
				appendMessage(tool.result)
			}
		}
	}
	
	private fun appendMessage(id: UUID?) {
		if (id == null) return
		val msg = messages[id] ?: return
		when (msg) {
			is AgentMessage.User -> msg.content.content
				?.filterIsInstance<ContentPart.Text>()
				?.forEach { part ->
					part.content.lines().forEach { line -> appendLine("> $line") }
					appendLine()
				}
			
			is AgentMessage.Assistant -> {
				msg.reasoning?.let {
					appendLine("<details><summary>Reasoning</summary>")
					appendLine()
					appendLine(it)
					appendLine("</details>")
					appendLine()
				}
				msg.content?.let {
					appendLine(it)
					appendLine()
				}
			}
			
			is AgentMessage.Compact -> {
				appendLine("<details><summary>Context compacted</summary>")
				appendLine()
				appendLine(msg.content)
				appendLine("</details>")
				appendLine()
			}
			
			is AgentMessage.Tool.Result -> {
				appendPresentation(msg.presentation)
				appendLine()
			}
			
			else -> {}
		}
	}
	
	private fun appendPresentation(presentation: ToolPresentation) = presentation.forEach {
		when (it) {
			is UiBlock.Text -> appendLine(it.content)
			
			is UiBlock.Command -> {
				appendLine("```bash")
				appendLine(it.command)
				appendLine("```")
			}
			
			is UiBlock.Diff -> unifiedDiff(it.oldContent, it.newContent)?.let { diff ->
				appendLine("```diff")
				appendLine(diff)
				appendLine("```")
			}
			
			is UiBlock.Error -> {
				appendLine("<details><summary>Error</summary>")
				appendLine()
				appendLine("```")
				appendLine(it.content)
				appendLine("```")
				appendLine()
				appendLine("</details>")
			}
			
			is UiBlock.Output -> {
				appendLine("<details><summary>Output</summary>")
				appendLine()
				appendLine("```")
				appendLine(it.content)
				appendLine("```")
				appendLine()
				appendLine("</details>")
			}
		}
	}
	
	private fun appendLine(string: String) = markdown.appendLine(string)
	private fun appendLine() = markdown.appendLine()
	
	private suspend fun loadAll() =
		core.persistence.loadMessages(ctx.ids()).forEach {
			messages[it.id] = it
		}
}
