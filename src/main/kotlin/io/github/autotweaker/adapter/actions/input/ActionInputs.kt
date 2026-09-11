package io.github.autotweaker.adapter.actions.input

data class ActionInputs(
	val prompt: String,
	val apiKey: String,
	val providerType: String,
	val modelId: String,
	val reasoning: String?,
	val baseUrl: String?,
	val workspace: String?,
) {
	companion object {
		fun fromEnv(): ActionInputs = ActionInputs(
			prompt = required("INPUT_PROMPT"),
			apiKey = required("INPUT_API_KEY"),
			providerType = required("INPUT_PROVIDER_TYPE"),
			modelId = required("INPUT_MODEL_ID"),
			reasoning = optional("INPUT_REASONING"),
			baseUrl = optional("INPUT_BASE_URL"),
			workspace = optional("INPUT_WORKSPACE"),
		)
		
		private fun required(name: String): String =
			optional(name) ?: error("Missing required input: $name")
		
		private fun optional(name: String): String? = System.getenv(name)?.ifBlank { null }
	}
}
