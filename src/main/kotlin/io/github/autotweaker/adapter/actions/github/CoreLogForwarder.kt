package io.github.autotweaker.adapter.actions.github

import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.log.ExceptionInfo
import io.github.autotweaker.api.types.log.LogEvent
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class CoreLogForwarder(
	private val core: CoreAPI,
) {
	suspend fun run(): Nothing =
		core.log.flow.collect { event ->
			WorkflowCommand.debug(event.format())
		}
	
	private fun LogEvent<ExceptionInfo.Live>.format(): String = buildString {
		append(timestamp.toTimestamp())
		append(" [").append(thread).append("] ")
		append(level).append(' ')
		append(abbreviateLogger(logger))
		append(" - ").append(message)
		exception?.throwable?.stackTraceToString()?.let {
			append('\n').append(it.trimEnd())
		}
	}
	
	private fun Instant.toTimestamp(): String =
		TIME_FORMAT.format(toLocalDateTime(TimeZone.currentSystemDefault()))
	
	private fun abbreviateLogger(name: String): String {
		if (name.length <= 36) return name
		val parts = name.split('.')
		if (parts.size < 2) return name
		val abbr = parts.toMutableList()
		for (i in 0 until abbr.size - 1) {
			if (abbr.joinToString(".").length <= 36) break
			abbr[i] = abbr[i].first().toString()
		}
		return abbr.joinToString(".")
	}
	
	private companion object {
		val TIME_FORMAT = LocalDateTime.Format {
			hour()
			char(':')
			minute()
			char(':')
			second()
			char('.')
			secondFraction(3)
		}
	}
}
