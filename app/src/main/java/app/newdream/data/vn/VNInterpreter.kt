package app.newdream.data.vn

import app.newdream.data.model.*

/**
 * Runtime state of a VN playthrough.
 */
data class VNRuntime(
    val script: VNScript,
    val currentSceneIndex: Int = 0,
    val currentDialogueIndex: Int = 0,
    val variables: MutableMap<String, Int> = mutableMapOf(),
    val visitedScenes: MutableSet<Int> = mutableSetOf(),
    val history: MutableList<String> = mutableListOf(),
    val isAwaitingChoice: Boolean = false,
    val availableChoices: List<VNChoice> = emptyList(),
    val finished: Boolean = false
) {
    val currentScene: VNScene?
        get() = script.scenes.getOrNull(currentSceneIndex)

    val currentDialogue: VNDialogue?
        get() = currentScene?.dialogues?.getOrNull(currentDialogueIndex)

    init {
        script.variables.forEach { variables[it.key] = it.initialValue }
        if (script.scenes.isNotEmpty()) {
            visitedScenes.add(0)
        }
    }
}

/**
 * Result of advancing through the script.
 */
sealed class VNStepResult {
    data class ShowDialogue(val dialogue: VNDialogue) : VNStepResult()
    data class ShowNarration(val text: String) : VNStepResult()
    data class ShowChoices(val choices: List<VNChoice>) : VNStepResult()
    data class ShowChoicesFiltered(val choices: List<VNChoice>) : VNStepResult()
    object Finished : VNStepResult()
    data class SceneTransition(val scene: VNScene) : VNStepResult()
}

/**
 * VN Script DSL interpreter.
 *
 * The interpreter advances scene-by-scene, evaluating conditions for choices,
 * running side-effects, and tracking variables. It can be used for both
 * runtime playthrough and script testing.
 */
class VNInterpreter(private val script: VNScript) {

    fun createRuntime(): VNRuntime = VNRuntime(script = script)

    /**
     * Advance runtime to the next visible step.
     * Returns null if execution cannot continue (e.g. malformed script).
     */
    fun step(runtime: VNRuntime): VNStepResult? {
        val scene = runtime.currentScene ?: return VNStepResult.Finished

        // Run on-enter actions for the current scene
        if (!scene.onEnter.isEmpty() && runtime.currentDialogueIndex == 0) {
            for (action in scene.onEnter) {
                applyAction(action, runtime)
            }
        }

        // If awaiting choice, stop here
        if (runtime.isAwaitingChoice) {
            return if (runtime.availableChoices.isNotEmpty()) {
                VNStepResult.ShowChoicesFiltered(runtime.availableChoices)
            } else null
        }

        // Step through dialogues
        val dialogues = scene.dialogues
        if (runtime.currentDialogueIndex < dialogues.size) {
            val dialogue = dialogues[runtime.currentDialogueIndex]
            runtime.history.add("${scene.title}: ${dialogue.text}")
            runtime.currentDialogueIndex++
            return VNStepResult.ShowDialogue(dialogue)
        }

        // Show choices (filtered by conditions)
        val filteredChoices = scene.choices.filter { choice ->
            choice.enabled && evaluateCondition(choice.condition, runtime)
        }
        if (filteredChoices.isNotEmpty()) {
            runtime.isAwaitingChoice = true
            runtime.availableChoices = filteredChoices
            return VNStepResult.ShowChoices(filteredChoices)
        }

        // Auto-advance: if no choices, jump to the next scene (assuming no branching)
        if (scene.index < script.scenes.size - 1) {
            return transitionToScene(scene.index + 1, runtime)
        }

        runtime.finished = true
        return VNStepResult.Finished
    }

    /**
     * Make a choice and advance to the target scene.
     */
    fun choose(choice: VNChoice, runtime: VNRuntime): VNStepResult {
        for (action in choice.effects) {
            applyAction(action, runtime)
        }
        runtime.isAwaitingChoice = false
        runtime.availableChoices = emptyList()
        return transitionToScene(choice.targetSceneIndex, runtime)
    }

    private fun transitionToScene(sceneIndex: Int, runtime: VNRuntime): VNStepResult {
        runtime.currentSceneIndex = sceneIndex
        runtime.currentDialogueIndex = 0
        runtime.visitedScenes.add(sceneIndex)
        val nextScene = script.scenes.getOrNull(sceneIndex)
            ?: return VNStepResult.Finished
        if (nextScene.narration.isNotBlank()) {
            return VNStepResult.ShowNarration(nextScene.narration)
        }
        return step(runtime)
    }

    private fun applyAction(action: VNAction, runtime: VNRuntime) {
        when (action.type) {
            "set" -> runtime.variables[action.key] = action.value
            "inc" -> runtime.variables[action.key] = (runtime.variables[action.key] ?: 0) + action.value
            "dec" -> runtime.variables[action.key] = (runtime.variables[action.key] ?: 0) - action.value
            "goto" -> runtime.currentSceneIndex = action.target
        }
    }

    /**
     * Evaluate a condition expression.
     * Format: `var:KEY>VALUE` or `var:KEY==VALUE`
     * Returns true if the expression matches the runtime state.
     */
    private fun evaluateCondition(expr: String, runtime: VNRuntime): Boolean {
        if (expr.isBlank()) return true
        return try {
            val parts = expr.split(":")
            when (parts[0]) {
                "var" -> {
                    val inner = parts[1]
                    val op = "<= >= == != > <".toRegex().find(inner)?.value ?: "=="
                    val splitted = inner.split(op)
                    val key = splitted[0]
                    val target = splitted.getOrNull(1)?.toIntOrNull() ?: 0
                    val current = runtime.variables[key] ?: 0
                    when (op) {
                        ">" -> current > target
                        "<" -> current < target
                        "==" -> current == target
                        "!=" -> current != target
                        ">=" -> current >= target
                        "<=" -> current <= target
                        else -> false
                    }
                }
                "visited" -> {
                    val sceneIdx = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    runtime.visitedScenes.contains(sceneIdx)
                }
                "always" -> true
                "never" -> false
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Build a sample starter VN script for new users.
 */
object SampleVNScripts {

    fun demoScript(): VNScript = VNScript(
        id = "vn-demo",
        title = "夜班电台",
        description = "凌晨两点，实习生第一次独自守在电台播音室里。",
        source = VNSource.ORIGINAL,
        characters = listOf(
            VNCharacter(id = "host", name = "主持人", color = "#C97A4D"),
            VNCharacter(id = "listener", name = "听众", color = "#3DA876"),
            VNCharacter(id = "narrator", name = "旁白", color = "#A89684")
        ),
        variables = listOf(
            VNVariable("decisions", initialValue = 0, min = 0, max = 20),
            VNVariable("好感度", initialValue = 0, min = -100, max = 100)
        ),
        scenes = listOf(
            VNScene(
                index = 0,
                title = "开场",
                narration = "凌晨两点的电台，霓虹招牌在窗外无声闪烁。",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "欢迎收听『夜航星』。今晚，我一个人守在这间播音室里。"),
                    VNDialogue(characterId = "host", text = "桌上放着一封署名「L」的来信。")
                ),
                choices = listOf(
                    VNChoice(
                        text = "打开信封",
                        targetSceneIndex = 1,
                        effects = listOf(VNAction(type = "inc", key = "decisions", value = 1))
                    ),
                    VNChoice(
                        text = "先不打开，看看窗外",
                        targetSceneIndex = 2,
                        effects = listOf(VNAction(type = "set", key = "好感度", value = 5))
                    )
                )
            ),
            VNScene(
                index = 1,
                title = "L 的信",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "「你不会读出来的，但是请你记在心里…」"),
                    VNDialogue(characterId = "host", text = "我把信纸贴近麦克风读了最后一句——『我一直在听。』")
                ),
                choices = listOf(
                    VNChoice(text = "轻声念出来", targetSceneIndex = 3),
                    VNChoice(text = "放在抽屉里", targetSceneIndex = 2)
                )
            ),
            VNScene(
                index = 2,
                title = "窗外",
                narration = "霓虹灯在积水里碎成一万块，又重组成一万块。",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "听众打来电话，把声音压得很低——\"是念出来的吗？\"")
                ),
                choices = listOf(
                    VNChoice(text = "回答：是的。", targetSceneIndex = 3),
                    VNChoice(text = "回答：没有。", targetSceneIndex = 4)
                )
            ),
            VNScene(
                index = 3,
                title = "结尾 A · 真相",
                narration = "凌晨四点半，节目的尾声只剩下两个人的呼吸声。",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "——你在哪？"),
                    VNDialogue(characterId = "listener", text = "门外。")
                ),
                choices = listOf(
                    VNChoice(text = "（END A）开门", targetSceneIndex = 5)
                )
            ),
            VNScene(
                index = 4,
                title = "结尾 B · 不念",
                narration = "我把信折好，放回抽屉里，把它当作下一期的预告。",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "听众在电话那头笑了一声：\"你总有一天会念的。\"")
                ),
                choices = listOf(
                    VNChoice(text = "（END B）", targetSceneIndex = 5)
                )
            ),
            VNScene(
                index = 5,
                title = "尾声",
                dialogues = listOf(
                    VNDialogue(characterId = "host", text = "凌晨 5 点，电台片尾曲响起，夜班结束。")
                )
            )
        )
    )

    /**
     * Generate a list of starter VN scripts.
     */
    fun starterScripts(): List<VNScript> = listOf(
        demoScript()
    )
}
