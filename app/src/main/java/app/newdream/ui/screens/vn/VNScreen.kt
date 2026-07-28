package app.newdream.ui.screens.vn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.newdream.NewDreamApp
import app.newdream.data.model.*
import app.newdream.data.vn.SampleVNScripts
import app.newdream.data.vn.VNInterpreter
import app.newdream.data.vn.VNRuntime
import app.newdream.data.vn.VNStepResult
import app.newdream.ui.components.AppTopBar
import app.newdream.ui.components.EmptyStateView
import kotlinx.coroutines.launch

@Composable
fun VNListScreen(onNavigateToScript: (String) -> Unit) {
    val settings = NewDreamApp.instance.settings
    val scripts by settings.vnScripts.collectAsState(initial = emptyList())
    var showNewDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settings.initializeVNIfEmpty(SampleVNScripts.starterScripts())
    }

    Scaffold(
        topBar = { AppTopBar(title = "VN剧场") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("新建剧本") }
            )
        }
    ) { padding ->
        if (scripts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.TheaterComedy,
                title = "还没有剧本",
                subtitle = "创建或导入一个 VN 剧本开始体验",
                actionLabel = "新建剧本",
                onAction = { showNewDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scripts) { script ->
                    VNScriptCard(
                        script = script,
                        onClick = { onNavigateToScript(script.id) }
                    )
                }
            }
        }
    }

    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text("新建 VN 剧本") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("剧本标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val title = newTitle.ifBlank { "未命名剧本" }
                    val newScript = SampleVNScripts.demoScript().copy(
                        id = java.util.UUID.randomUUID().toString(),
                        title = title,
                        description = "新建的剧本"
                    )
                    scope.launch {
                        settings.saveVNScript(newScript)
                        newTitle = ""
                    }
                    showNewDialog = false
                }) { Text("创建") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun VNScriptCard(script: VNScript, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.TheaterComedy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = script.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = script.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipTag("${script.scenes.size} 场景")
                ChipTag("${script.characters.size} 角色")
                ChipTag("${script.variables.size} 变量")
                ChipTag(
                    when (script.source) {
                        VNSource.ORIGINAL -> "原创"
                        VNSource.FROM_BOOK -> "改编小说"
                        VNSource.FROM_CHAT -> "改编聊天"
                    }
                )
            }
        }
    }
}

@Composable
private fun ChipTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/**
 * VN Player - executes the script runtime and displays choices.
 */
@Composable
fun VNPlayerScreen(scriptId: String, onBack: () -> Unit) {
    val settings = NewDreamApp.instance.settings
    val scripts by settings.vnScripts.collectAsState(initial = emptyList())
    val script = scripts.find { it.id == scriptId }
    val interpreter = remember(scriptId) {
        script?.let { VNInterpreter(it) }
    }
    val runtime = remember(scriptId) {
        interpreter?.createRuntime()
    }

    if (script == null || interpreter == null || runtime == null) {
        Scaffold(topBar = { AppTopBar(title = "剧本不存在", onBack = onBack) }) { padding ->
            EmptyStateView(
                modifier = Modifier.padding(padding),
                icon = Icons.Default.ErrorOutline,
                title = "找不到剧本",
                subtitle = "剧本可能已被删除"
            )
        }
        return
    }

    var currentStep by remember { mutableStateOf<VNStepResult?>(interpreter.step(runtime)) }
    var variableSnapshot by remember { mutableStateOf(runtime.variables.toMap()) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                title = script.title,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        // Show variables
                    }) {
                        Icon(Icons.Default.Code, contentDescription = "变量")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            settings.deleteVNScript(script.id)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top status: scene info + variables
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "第 ${runtime.currentSceneIndex + 1} 场景",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = runtime.currentScene?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (variableSnapshot.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                variableSnapshot.forEach { (key, value) ->
                                    VariableChip(key, value)
                                }
                            }
                        }
                    }
                }
            }

            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (val step = currentStep) {
                    is VNStepResult.ShowDialogue -> DialogueDisplay(
                        dialogue = step.dialogue,
                        characters = script.characters,
                        onNext = { currentStep = advance(interpreter, runtime) }
                    )
                    is VNStepResult.ShowNarration -> NarrationDisplay(
                        text = step.text,
                        onNext = { currentStep = advance(interpreter, runtime) }
                    )
                    is VNStepResult.ShowChoices, is VNStepResult.ShowChoicesFiltered -> {
                        val choices = when (step) {
                            is VNStepResult.ShowChoices -> step.choices
                            is VNStepResult.ShowChoicesFiltered -> step.choices
                            else -> emptyList()
                        }
                        ChoicesDisplay(
                            choices = choices,
                            onChoose = { choice ->
                                currentStep = interpreter.choose(choice, runtime)
                                variableSnapshot = runtime.variables.toMap()
                            }
                        )
                    }
                    VNStepResult.Finished -> FinishedDisplay(
                        onExit = onBack
                    )
                    is VNStepResult.SceneTransition -> {
                        currentStep = advance(interpreter, runtime)
                    }
                    null -> FinishedDisplay(onExit = onBack)
                }
            }
        }
    }
}

private fun advance(interpreter: VNInterpreter, runtime: VNRuntime): VNStepResult? =
    interpreter.step(runtime)

@Composable
private fun VariableChip(key: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "$key: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun DialogueDisplay(
    dialogue: VNDialogue,
    characters: List<VNCharacter>,
    onNext: () -> Unit
) {
    val character = characters.find { it.id == dialogue.characterId }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onNext),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Character avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = parseColorOrDefault(character?.color),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (character?.name ?: dialogue.characterName).take(1),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character?.name ?: dialogue.characterName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = parseColorOrDefault(character?.color)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dialogue.text,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 28.sp
                )
            }
        }
        // Hint
        Text(
            text = "点击任意处继续 ▼",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun NarrationDisplay(text: String, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onNext),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 32.sp,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun ChoicesDisplay(
    choices: List<VNChoice>,
    onChoose: (VNChoice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(choices) { choice ->
            ChoiceCard(choice, onChoose)
        }
    }
}

@Composable
private fun ChoiceCard(choice: VNChoice, onChoose: (VNChoice) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onChoose(choice) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = choice.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FinishedDisplay(onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "剧本体验完毕",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "你已走完整个故事线。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExit) {
            Text("返回剧本列表")
        }
    }
}

private fun parseColorOrDefault(hex: String?, default: Color = Color.Gray): Color {
    if (hex.isNullOrBlank()) return default
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}
