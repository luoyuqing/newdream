package app.newdream.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.newdream.NewDreamApp
import app.newdream.data.api.CharacterCardIo
import app.newdream.data.model.CharacterCard
import app.newdream.ui.components.AppTopBar
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Editor for creating or editing a character card.
 * Fields map to SillyTavern V1/V2 specs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCardEditorScreen(
    existing: CharacterCard?,
    onBack: () -> Unit,
    onSaved: (CharacterCard) -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings = NewDreamApp.instance.settings

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var personality by remember { mutableStateOf(existing?.personality ?: "") }
    var scenario by remember { mutableStateOf(existing?.scenario ?: "") }
    var firstMessage by remember { mutableStateOf(existing?.firstMessage ?: "") }
    var exampleMessages by remember { mutableStateOf(existing?.exampleMessages ?: "") }
    var postHistory by remember { mutableStateOf(existing?.postHistoryInstructions ?: "") }
    var systemPrompt by remember { mutableStateOf(existing?.systemPrompt ?: "") }
    var tagsText by remember { mutableStateOf(existing?.tags?.joinToString(", ") ?: "") }
    var creatorNotes by remember { mutableStateOf(existing?.creatorNotes ?: "") }

    var showAdvanced by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (existing == null) "新建角色卡" else "编辑 ${existing.name}",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileOpen, contentDescription = "导入 JSON")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val card = CharacterCard(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                name = name.ifBlank { "未命名角色" },
                                description = description,
                                personality = personality,
                                scenario = scenario,
                                firstMessage = firstMessage,
                                exampleMessages = exampleMessages,
                                postHistoryInstructions = postHistory,
                                avatar = existing?.avatar ?: "",
                                systemPrompt = systemPrompt,
                                tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                creatorNotes = creatorNotes
                            )
                            val list = settings.characters.first().toMutableList()
                            val idx = list.indexOfFirst { it.id == card.id }
                            if (idx >= 0) list[idx] = card else list.add(card)
                            settings.saveCharacters(list)
                            onSaved(card)
                        }
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("角色名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("角色背景与外形") },
                placeholder = { Text("如：身高、发色、身份、背景故事...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                minLines = 4
            )

            OutlinedTextField(
                value = personality,
                onValueChange = { personality = it },
                label = { Text("性格特征") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = scenario,
                onValueChange = { scenario = it },
                label = { Text("场景设定") },
                placeholder = { Text("对话发生的背景情境...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = firstMessage,
                onValueChange = { firstMessage = it },
                label = { Text("开场白（角色首条消息）") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text("标签（用逗号分隔）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = creatorNotes,
                onValueChange = { creatorNotes = it },
                label = { Text("作者备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Advanced section
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = { showAdvanced = !showAdvanced }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "高级字段",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showAdvanced) {
                OutlinedTextField(
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("系统提示词") },
                    placeholder = { Text("可选，会覆盖自动构建的提示词") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    minLines = 3
                )
                OutlinedTextField(
                    value = exampleMessages,
                    onValueChange = { exampleMessages = it },
                    label = { Text("示例对话（仅作风格参考）") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    minLines = 4
                )
                OutlinedTextField(
                    value = postHistory,
                    onValueChange = { postHistory = it },
                    label = { Text("对话历史之后的指令") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Helper info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 提示",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "本编辑器兼容 SillyTavern V1/V2 角色卡格式。保存后即可与 AI 开始对话，并会自动构建系统提示词。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("导入 V1/V2 角色卡 JSON") },
            text = {
                Column {
                    Text(
                        "粘贴完整的 SillyTavern 角色卡 JSON 文件内容，V1 与 V2 规范均可。",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJson,
                        onValueChange = { importJson = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                        placeholder = { Text("{ ... }") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val card = CharacterCardIo.parse(importJson)
                    if (card != null) {
                        scope.launch {
                            val list = settings.characters.first().toMutableList()
                            list.add(card)
                            settings.saveCharacters(list)
                            onSaved(card)
                        }
                    }
                    showImportDialog = false
                }) { Text("导入") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("取消") }
            }
        )
    }
}
