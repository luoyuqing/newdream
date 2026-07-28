package app.newdream.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ============= Provider / API Models =============

@Serializable
data class ApiProvider(
    val id: String,
    val name: String,
    val baseUrl: String = "",
    val apiKey: String = "",
    val providerType: ProviderType = ProviderType.OPENAI_COMPATIBLE,
    val models: List<AiModel> = emptyList(),
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class AiModel(
    val id: String,
    val name: String = "",
    val providerId: String = "",
    val modalities: List<Modality> = listOf(Modality.TEXT),
    val enabled: Boolean = true
)

@Serializable
enum class ProviderType {
    OPENAI_COMPATIBLE,
    GEMINI,
    ANTHROPIC
}

@Serializable
enum class Modality {
    TEXT,
    IMAGE_GENERATION,
    IMAGE_VISION,
    SPEECH_SYNTHESIS,
    VIDEO_GENERATION,
    EMBEDDING
}

// ============= Chat / Character Card Models =============

@Serializable
data class CharacterCard(
    val id: String,
    val name: String,
    val description: String = "",
    val personality: String = "",
    val scenario: String = "",
    val firstMessage: String = "",
    val exampleMessages: String = "",
    val postHistoryInstructions: String = "",
    val avatar: String = "",      // file path
    val systemPrompt: String = "",
    val tags: List<String> = emptyList(),
    val creatorNotes: String = "",
    val source: CardSource = CardSource.LOCAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class CharacterCardV2(
    val id: String,
    val name: String,
    val description: String = "",
    val personality: String = "",
    val scenario: String = "",
    val firstMes: String = "",           // SillyTavern compatible
    val mesExample: String = "",
    val postHistoryInstructions: String = "",
    val avatar: String = "",
    val systemPrompt: String = "",
    val tags: List<String> = emptyList(),
    val creatorNotes: String = "",
    val characterVersion: String = "1.0",
    val extensions: Map<String, JsonElement> = emptyMap()
)

@Serializable
enum class CardSource { LOCAL, IMPORTED, COMMUNITY }

@Serializable
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val name: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap(),
    val isStreaming: Boolean = false
)

@Serializable
enum class MessageRole { USER, ASSISTANT, SYSTEM, ACTION }

@Serializable
data class ChatSession(
    val id: String,
    val characterId: String,
    val title: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelId: String = "",
    val systemPrompt: String = "",
    val contextSize: Int = 4096
)

// ============= Novel / Reader Models =============

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val coverPath: String = "",
    val filePath: String = "",
    val fileType: BookFileType = BookFileType.TXT,
    val chapters: List<Chapter> = emptyList(),
    val totalChapters: Int = 0,
    val lastReadChapter: Int = 0,
    val lastReadPosition: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class BookFileType { TXT, EPUB, MD }

@Serializable
data class Chapter(
    val index: Int,
    val title: String = "",
    val content: String = "",
    val wordCount: Int = 0
)

@Serializable
data class ReadingProgress(
    val bookId: String,
    val chapterIndex: Int = 0,
    val position: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

// ============= Companion Models =============

@Serializable
data class CompanionProfile(
    val id: String,
    val name: String,
    val characterCardId: String = "",
    val relationship: String = "朋友",
    val avatar: String = "",
    val personality: String = "",
    val background: String = "",
    val greeting: String = "",
    val relationshipLevel: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class CompanionMemory(
    val id: String,
    val companionId: String,
    val event: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1,
    val category: MemoryCategory = MemoryCategory.GENERAL
)

@Serializable
enum class MemoryCategory { GENERAL, IMPORTANT, EMOTIONAL, DAILY }

// ============= VN / Visual Novel Models =============

@Serializable
data class VNScript(
    val id: String,
    val title: String,
    val source: VNSource,
    val sourceId: String = "",        // bookId or chatSessionId
    val scenes: List<VNScene> = emptyList(),
    val characters: List<VNCharacter> = emptyList(),
    val currentSceneIndex: Int = 0
)

@Serializable
enum class VNSource { FROM_BOOK, FROM_CHAT, ORIGINAL }

@Serializable
data class VNScene(
    val index: Int,
    val background: String = "",
    val dialogues: List<VNDialogue> = emptyList(),
    val choices: List<VNChoice> = emptyList(),
    val narration: String = ""
)

@Serializable
data class VNDialogue(
    val characterId: String = "",
    val characterName: String = "",
    val text: String,
    val emotion: String = "neutral",
    val expression: String = ""
)

@Serializable
data class VNChoice(
    val text: String,
    val targetSceneIndex: Int,
    val condition: String = ""
)

@Serializable
data class VNCharacter(
    val id: String,
    val name: String,
    val avatarPath: String = "",
    val color: String = "#6C63FF",
    val expressions: Map<String, String> = emptyMap()
)

// ============= World / Lorebook Models =============

@Serializable
data class Lorebook(
    val id: String,
    val name: String,
    val description: String = "",
    val entries: List<LorebookEntry> = emptyList(),
    val scanDepth: Int = 1024
)

@Serializable
data class LorebookEntry(
    val id: String,
    val keys: List<String>,
    val content: String,
    val enabled: Boolean = true,
    val priority: Int = 0,
    val secondaryKeys: List<String> = emptyList(),
    val constant: Boolean = false
)

// ============= Agent Models =============

@Serializable
data class AgentAction(
    val id: String,
    val name: String,
    val description: String = "",
    val prompt: String = "",
    val category: AgentCategory = AgentCategory.AUTHORING,
    val requiresApproval: Boolean = true,
    val parameters: Map<String, String> = emptyMap()
)

@Serializable
enum class AgentCategory {
    AUTHORING,
    ILLUSTRATION,
    ANALYSIS,
    UTILITY,
    COMPANION,
    CUSTOMIZATION
}

@Serializable
data class AgentExecution(
    val id: String,
    val actionId: String,
    val status: AgentStatus = AgentStatus.PENDING,
    val input: String = "",
    val output: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0,
    val error: String = ""
)

@Serializable
enum class AgentStatus { PENDING, RUNNING, COMPLETED, FAILED, APPROVAL_REQUIRED }

// ============= API Request / Response Models =============

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<CompletionMessage>,
    val temperature: Double = 0.8,
    val maxTokens: Int = 2048,
    val stream: Boolean = true,
    val topP: Double = 0.9,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0,
    val stop: List<String> = emptyList()
)

@Serializable
data class CompletionMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val id: String = "",
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: CompletionMessage? = null,
    val delta: CompletionMessage? = null,
    val finishReason: String? = null
)

@Serializable
data class Usage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0
)

// ============= Navigation =============

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Reader : Screen("reader")
    data object ReaderBook : Screen("reader/{bookId}") {
        fun createRoute(bookId: String) = "reader/$bookId"
    }
    data object Chat : Screen("chat")
    data object ChatSession : Screen("chat/{characterId}") {
        fun createRoute(characterId: String) = "chat/$characterId"
    }
    data object Companion : Screen("companion")
    data object CompanionChat : Screen("companion/{companionId}") {
        fun createRoute(companionId: String) = "companion/$companionId"
    }
    data object VN : Screen("vn")
    data object VNPlayer : Screen("vn/{scriptId}") {
        fun createRoute(scriptId: String) = "vn/$scriptId"
    }
    data object Agent : Screen("agent")
    data object Settings : Screen("settings")
    data object SettingsProviders : Screen("settings/providers")
    data object SettingsProvider : Screen("settings/providers/{providerId}") {
        fun createRoute(providerId: String) = "settings/providers/$providerId"
    }
}
