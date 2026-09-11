package io.github.autotweaker.adapter.actions.github

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE

object WorkflowFile {
	@Suppress("unused")
	fun setOutput(name: String, value: String) = append("GITHUB_OUTPUT", "$name=$value")
	
	fun appendSummary(markdown: String) = append("GITHUB_STEP_SUMMARY", markdown)
	
	private fun append(envName: String, text: String) {
		val target = System.getenv(envName) ?: return
		Files.writeString(Path.of(target), "$text\n", CREATE, APPEND)
	}
}
