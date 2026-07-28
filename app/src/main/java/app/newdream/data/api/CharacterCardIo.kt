package app.newdream.data.api

import app.newdream.data.model.CharacterCard
import app.newdream.data.model.CharacterCardV2
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray

/**
 * Character Card I/O utilities for SillyTavern V1/V2 spec compatibility.
 *
 * V1 spec: top-level name/description/personality/scenario/first_mes/mes_example
 * V2 spec: nested under "data" with spec = "chara_card_v2"
 */
object CharacterCardIo {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
    }

    /**
     * Import a character card from a JSON file string.
     * Auto-detects V2 spec vs V1 spec.
     */
    fun parse(jsonText: String): CharacterCard? {
        return try {
            val root = json.parseToJsonElement(jsonText) as? JsonObject ?: return null

            val isV2 = root["spec"]?.jsonPrimitive?.content == "chara_card_v2"
            val data = if (isV2) (root["data"] as? JsonObject) ?: root else root

            val name = data["name"]?.jsonPrimitive?.content ?: return null
            val description = data["description"]?.jsonPrimitive?.content ?: ""
            val personality = data["personality"]?.jsonPrimitive?.content ?: ""
            val scenario = data["scenario"]?.jsonPrimitive?.content ?: ""
            val firstMes = (data["first_mes"] ?: data["firstMes"])?.jsonPrimitive?.content ?: ""
            val mesExample = (data["mes_example"] ?: data["mesExample"])?.jsonPrimitive?.content ?: ""
            val postHistory = (data["post_history_instructions"] ?: data["postHistoryInstructions"])?.jsonPrimitive?.content ?: ""
            val systemPrompt = data["system_prompt"]?.jsonPrimitive?.content ?: ""
            val creatorNotes = data["creator_notes"]?.jsonPrimitive?.content ?: ""
            val charVersion = data["character_version"]?.jsonPrimitive?.content ?: data["characterVersion"]?.jsonPrimitive?.content ?: "1.0"

            val tags = (data["tags"] as? kotlinx.serialization.json.JsonArray)
                ?.map { it.jsonPrimitive.content }
                ?: emptyList()

            CharacterCard(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                description = description,
                personality = personality,
                scenario = scenario,
                firstMessage = firstMes,
                exampleMessages = mesExample,
                postHistoryInstructions = postHistory,
                avatar = data["avatar"]?.jsonPrimitive?.content ?: "",
                systemPrompt = systemPrompt,
                tags = tags,
                creatorNotes = creatorNotes,
                source = app.newdream.data.model.CardSource.IMPORTED
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Build a SillyTavern-compatible system prompt from a character card.
     */
    fun buildSystemPrompt(card: CharacterCard): String = buildString {
        if (card.systemPrompt.isNotBlank()) {
            appendLine(card.systemPrompt)
            appendLine()
        }
        appendLine("[角色设定]")
        appendLine("姓名：${card.name}")
        if (card.description.isNotBlank()) {
            appendLine("背景与外形：")
            appendLine(card.description)
        }
        if (card.personality.isNotBlank()) {
            appendLine()
            appendLine("性格：")
            appendLine(card.personality)
        }
        if (card.scenario.isNotBlank()) {
            appendLine()
            appendLine("场景设定：")
            appendLine(card.scenario)
        }
        if (card.exampleMessages.isNotBlank()) {
            appendLine()
            appendLine("[角色输出参考（不要原样复读，只作风格参考）]")
            appendLine(card.exampleMessages)
        }
        if (card.postHistoryInstructions.isNotBlank()) {
            appendLine()
            appendLine("[对话历史之后的系统级指令]")
            appendLine(card.postHistoryInstructions)
        }
        if (card.tags.isNotEmpty()) {
            appendLine()
            appendLine("[标签：${card.tags.joinToString(", ")}]")
        }
    }

    /**
     * Export a character card to V2 spec JSON.
     */
    fun exportAsV2(card: CharacterCard): String {
        val v2 = CharacterCardV2(
            id = card.id,
            name = card.name,
            description = card.description,
            personality = card.personality,
            scenario = card.scenario,
            firstMes = card.firstMessage,
            mesExample = card.exampleMessages,
            postHistoryInstructions = card.postHistoryInstructions,
            avatar = card.avatar,
            systemPrompt = card.systemPrompt,
            tags = card.tags,
            creatorNotes = card.creatorNotes,
            characterVersion = "1.0"
        )
        return json.encodeToString(
            mapOf(
                "spec" to "chara_card_v2",
                "spec_version" to "2.0",
                "data" to json.encodeToString(v2)
            )
        )
    }
}
