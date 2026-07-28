package app.newdream.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import app.newdream.data.model.*

private val Context.dataStore by preferencesDataStore(name = "newdream_settings")

class AppSettings(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    // ===== Dark Mode =====
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    // ===== Default Model IDs =====
    val defaultChatModel: Flow<String> = context.dataStore.data.map { it[DEFAULT_CHAT_MODEL] ?: "" }
    suspend fun setDefaultChatModel(id: String) {
        context.dataStore.edit { it[DEFAULT_CHAT_MODEL] = id }
    }

    // ===== API Providers =====
    val providers: Flow<List<ApiProvider>> = context.dataStore.data.map { prefs ->
        val raw = prefs[PROVIDERS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveProviders(providers: List<ApiProvider>) {
        context.dataStore.edit { it[PROVIDERS_JSON] = json.encodeToString(providers) }
    }

    // ===== Characters =====
    val characters: Flow<List<CharacterCard>> = context.dataStore.data.map { prefs ->
        val raw = prefs[CHARACTERS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveCharacters(characters: List<CharacterCard>) {
        context.dataStore.edit { it[CHARACTERS_JSON] = json.encodeToString(characters) }
    }

    // ===== Reading Progress =====
    val readingProgress: Flow<List<ReadingProgress>> = context.dataStore.data.map { prefs ->
        val raw = prefs[READING_PROGRESS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveReadingProgress(progress: List<ReadingProgress>) {
        context.dataStore.edit { it[READING_PROGRESS_JSON] = json.encodeToString(progress) }
    }

    // ===== Chat Sessions =====
    suspend fun saveChatSessions(sessions: List<ChatSession>) {
        context.dataStore.edit { it[CHAT_SESSIONS_JSON] = json.encodeToString(sessions) }
    }

    val chatSessions: Flow<List<ChatSession>> = context.dataStore.data.map { prefs ->
        val raw = prefs[CHAT_SESSIONS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    // ===== Companions =====
    val companions: Flow<List<CompanionProfile>> = context.dataStore.data.map { prefs ->
        val raw = prefs[COMPANIONS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveCompanions(companions: List<CompanionProfile>) {
        context.dataStore.edit { it[COMPANIONS_JSON] = json.encodeToString(companions) }
    }

    // ===== Lorebooks =====
    val lorebooks: Flow<List<Lorebook>> = context.dataStore.data.map { prefs ->
        val raw = prefs[LOREBOOKS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveLorebooks(lorebooks: List<Lorebook>) {
        context.dataStore.edit { it[LOREBOOKS_JSON] = json.encodeToString(lorebooks) }
    }

    // ===== Agent Actions =====
    val agentActions: Flow<List<AgentAction>> = context.dataStore.data.map { prefs ->
        val raw = prefs[AGENT_ACTIONS_JSON] ?: "[]"
        try { json.decodeFromString(raw) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveAgentActions(actions: List<AgentAction>) {
        context.dataStore.edit { it[AGENT_ACTIONS_JSON] = json.encodeToString(actions) }
    }

    companion object {
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val DEFAULT_CHAT_MODEL = stringPreferencesKey("default_chat_model")
        private val PROVIDERS_JSON = stringPreferencesKey("providers_json")
        private val CHARACTERS_JSON = stringPreferencesKey("characters_json")
        private val READING_PROGRESS_JSON = stringPreferencesKey("reading_progress_json")
        private val CHAT_SESSIONS_JSON = stringPreferencesKey("chat_sessions_json")
        private val COMPANIONS_JSON = stringPreferencesKey("companions_json")
        private val LOREBOOKS_JSON = stringPreferencesKey("lorebooks_json")
        private val AGENT_ACTIONS_JSON = stringPreferencesKey("agent_actions_json")
    }
}
