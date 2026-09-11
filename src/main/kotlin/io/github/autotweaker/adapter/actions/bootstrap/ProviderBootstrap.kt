package io.github.autotweaker.adapter.actions.bootstrap

import io.github.autotweaker.adapter.actions.github.WorkflowCommand
import io.github.autotweaker.adapter.actions.input.ActionInputs
import io.github.autotweaker.api.UUID
import io.github.autotweaker.api.adapter.CoreAPI
import io.github.autotweaker.api.types.Url.Companion.toUrl
import io.github.autotweaker.api.types.llm.ModelData
import io.github.autotweaker.api.types.llm.ProviderData
import java.util.*

object ProviderBootstrap {
	suspend fun configure(core: CoreAPI, inputs: ActionInputs): UUID = with(core.config) {
		val apiKeyId = addApiKey(inputs.providerType, inputs.apiKey)
		val info = getProviderMeta(inputs.providerType)
		
		val providerId = UUID()
		setProvider(
			ProviderData(
				id = providerId,
				displayName = inputs.providerType,
				providerType = info.name,
				apiKey = apiKeyId,
				baseUrl = inputs.baseUrl?.toUrl() ?: info.baseUrl,
				errorHandlingRules = info.errorHandlingRules,
			)
		)
		
		val modelInfo = info.models.find { it.modelId == inputs.modelId }
			?: fallback(inputs.modelId).also {
				WorkflowCommand.warning(
					"Unknown model ${inputs.modelId}  falling back to default capabilities"
				)
			}
		
		val modelId = UUID()
		setModel(
			ModelData(
				id = modelId,
				displayName = inputs.modelId,
				modelInfo = modelInfo,
				providerId = providerId,
			)
		)
		
		WorkflowCommand.notice("Provider ${info.name}  model ${inputs.modelId}")
		return@with modelId
	}
	
	private fun fallback(modelId: String) = ModelData.ModelInfo(
		modelId = modelId,
		contextWindow = 1_000_000,
		maxOutputTokens = 1_000_000,
		supportsStreaming = true,
		supportsToolCalls = true,
		supportsReasoning = true,
		supportsJsonOutput = true,
		supportsImage = true,
		supportsAudio = true,
		supportsVideo = true
	)
}
