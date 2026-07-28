package app.newdream.data.local

import android.content.Context
import app.newdream.data.model.SampleWorldsBundle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.decodeFromString

/**
 * Loader for bundled assets like sample_worlds_v12.json
 */
object BundledAssetsLoader {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    /**
     * Load the sample worlds bundle from res/raw/sample_worlds_v12.json
     */
    fun loadSampleWorlds(context: Context): SampleWorldsBundle? {
        return try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier("sample_worlds_v12", "raw", context.packageName)
            )
            val text = inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<SampleWorldsBundle>(text)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load JSON resource as string
     */
    fun loadRawJson(context: Context, resourceName: String): String? {
        return try {
            val inputStream = context.resources.openRawResource(
                context.resources.getIdentifier(resourceName, "raw", context.packageName)
            )
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse JSON string to element
     */
    fun parseJsonElement(text: String): JsonElement? {
        return try {
            json.parseToJsonElement(text)
        } catch (e: Exception) {
            null
        }
    }
}
