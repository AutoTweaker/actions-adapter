package io.github.autotweaker.adapter.actions.github

object WorkflowCommand {
	fun group(title: String) = emit("::group::${escapeData(title)}")
	
	fun endGroup() = emit("::endgroup::")
	
	fun notice(message: String) = emit("::notice::${escapeData(message)}")
	
	fun warning(message: String) = emit("::warning::${escapeData(message)}")
	
	fun error(message: String) = emit("::error::${escapeData(message)}")
	
	fun debug(message: String) = emit("::debug::${escapeData(message)}")
	
	private fun escapeData(value: String) = value
		.replace("%", "%25")
		.replace("\r", "%0D")
		.replace("\n", "%0A")
	
	private fun emit(line: String) = println(line)
}
