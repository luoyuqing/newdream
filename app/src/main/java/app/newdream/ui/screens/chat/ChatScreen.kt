package app.newdream.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.newdream.NewDreamApp
import app.newdream.data.api.ApiService
import app.newdream.data.model.*
import app.newdream.ui.components.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToEditor: (String?) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val settings = NewDreamApp.instance.settings
    val characters by settings.characters.collectAsState(initial = emptyList())
    val sessions by settings.chatSessions.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "角色聊天") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建聊天", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (characters.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Forum,
                title = "还没有角色",
                subtitle = "创建或导入一张SillyTavern兼容的角色卡开始聊天",
                actionLabel = "新建角色",
                onAction = { onNavigateToEditor(null) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToEditor(null) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("新建")
                        }
                        OutlinedButton(
                            onClick = { onNavigateToEditor(characters.firstOrNull()?.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("管理")
                        }
                    }
                }
                items(characters) { character ->
                    val session = sessions.find { it.characterId == character.id }
                    CharacterCardItem(
                        character = character,
                        lastMessage = session?.messages?.lastOrNull()?.content ?: "",
                        onClick = { onNavigateToChat(character.id) },
                        onLongClick = { onNavigateToEditor(character.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    characterId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings = NewDreamApp.instance.settings
    val characters by settings.characters.collectAsState(initial = emptyList())
    val sessions by settings.chatSessions.collectAsState(initial = emptyList())

    val character = characters.find { it.id == characterId }
    var session by remember(characterId, sessions) {
        mutableStateOf(sessions.find { it.characterId == characterId }
            ?: ChatSession(
                id = UUID.randomUUID().toString(),
                characterId = characterId,
                title = character?.name ?: "新对话",
                systemPrompt = buildSystemPrompt(character)
            ))
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val apiService = remember { ApiService() }
    val providers by settings.providers.collectAsState(initial = emptyList())
    val defaultModel by settings.defaultChatModel.collectAsState(initial = "")

    // Auto scroll to bottom
    LaunchedEffect(session.messages.size) {
        if (session.messages.isNotEmpty()) {
            listState.animateScrollToItem(session.messages.size - 1)
        }
    }

    fun saveSession() {
        scope.launch {
            val updated = sessions.toMutableList().apply {
                removeAll { it.id == session.id }
                add(session)
            }
            settings.saveChatSessions(updated)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text
        )
        session = session.copy(messages = session.messages + userMsg)
        inputText = ""
        isLoading = true

        scope.launch {
            saveSession()

            val activeProvider = if (defaultModel.isNotEmpty()) {
                providers.firstOrNull { it.models.any { m -> m.id == defaultModel && m.enabled } }
            } else null
            val provider = activeProvider ?: providers.firstOrNull { it.enabled }
            val model = defaultModel.ifEmpty {
                provider?.models?.firstOrNull { it.enabled && Modality.TEXT in it.modalities }?.id ?: ""
            }

            if (provider == null || model.isEmpty()) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ 请先在设置中添加API服务商和模型"
                )
                session = session.copy(messages = session.messages + errorMsg)
                isLoading = false
                saveSession()
                return@launch
            }

            val msgs = buildMessageList(session, character)

            val result = apiService.chatCompletion(
                provider = provider,
                model = model,
                messages = msgs,
                onStream = { delta ->
                    val lastIdx = session.messages.lastIndex
                    val lastMsg = if (lastIdx >= 0) session.messages[lastIdx] else null
                    if (lastMsg?.role == MessageRole.ASSISTANT && lastMsg.isStreaming) {
                        val updated = lastMsg.copy(content = lastMsg.content + delta)
                        session = session.copy(
                            messages = session.messages.toMutableList().apply {
                                set(lastIdx, updated)
                            }
                        )
                    } else {
                        val newMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            role = MessageRole.ASSISTANT,
                            content = delta,
                            isStreaming = true
                        )
                        session = session.copy(messages = session.messages + newMsg)
                    }
                }
            )

            result.onSuccess { response ->
                val content = response.choices.firstOrNull()?.message?.content ?: ""
                val lastIdx = session.messages.lastIndex
                if (lastIdx >= 0) {
                    val lastMsg = session.messages[lastIdx]
                    if (lastMsg.role == MessageRole.ASSISTANT) {
                        session = session.copy(
                            messages = session.messages.toMutableList().apply {
                                set(lastIdx, lastMsg.copy(content = content, isStreaming = false))
                            }
                        )
                    }
                }
            }.onFailure { error ->
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.ASSISTANT,
                    content = "⚠️ 请求失败: ${error.message}"
                )
                session = session.copy(messages = session.messages + errorMsg)
            }

            isLoading = false
            saveSession()
        }
    }

    if (showSettings) {
        ChatSettingsSheet(
            character = character,
            session = session,
            onDismiss = { showSettings = false },
            onUpdateSession = { session = it }
        )
        return
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = character?.name ?: "聊天",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "聊天设置")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() })
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { sendMessage() },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "发送")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // System prompt hint
                if (session.messages.isEmpty()) {
                    item {
                        if (character != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = character.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (character.description.isNotEmpty()) {
                                        Text(
                                            text = character.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                    if (character.firstMessage.isNotEmpty()) {
                                        Spacer(Modifier.height(12.dp))
                                        Divider()
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = character.firstMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(session.messages) { msg ->
                    ChatBubble(message = msg)
                }

                if (isLoading && session.messages.lastOrNull()?.isStreaming != true) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(4, 16, 16, 16),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val isAction = message.role == MessageRole.ACTION
    val isSystem = message.role == MessageRole.SYSTEM

    if (isSystem) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
        return
    }

    if (isAction) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = if (isUser) RoundedCornerShape(16, 4, 16, 16)
            else RoundedCornerShape(4, 16, 16, 16),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser && message.name.isNotEmpty()) {
                    Text(
                        text = message.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun CharacterCardItem(
    character: CharacterCard,
    lastMessage: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    val interactiveModifier = if (onLongClick != null) {
        baseModifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    } else {
        baseModifier.clickable(onClick = onClick)
    }
    Card(
        modifier = interactiveModifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                text = character.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (lastMessage.isNotEmpty()) {
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatSettingsSheet(
    character: CharacterCard?,
    session: ChatSession,
    onDismiss: () -> Unit,
    onUpdateSession: (ChatSession) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "聊天设置", onBack = onDismiss) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("角色信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (character != null) {
                Text("名称: ${character.name}", modifier = Modifier.padding(top = 8.dp))
                Text("描述: ${character.description.take(100)}", modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("系统提示词", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = session.systemPrompt,
                onValueChange = { onUpdateSession(session.copy(systemPrompt = it)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                maxLines = 10
            )

            Spacer(Modifier.height(24.dp))
            Text("上下文长度", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            var contextSize by remember(session) { mutableStateOf(session.contextSize.toFloat()) }
            Text("${contextSize.toInt()} tokens")
            Slider(
                value = contextSize,
                onValueChange = { contextSize = it },
                valueRange = 1024f..32768f,
                steps = 30
            )
            LaunchedEffect(contextSize) {
                onUpdateSession(session.copy(contextSize = contextSize.toInt()))
            }
        }
    }
}

private fun buildSystemPrompt(character: CharacterCard?): String {
    if (character == null) return ""
    return buildString {
        if (character.systemPrompt.isNotEmpty()) {
            appendLine(character.systemPrompt)
            appendLine()
        }
        appendLine("你现在扮演${character.name}。")
        if (character.personality.isNotEmpty()) {
            appendLine("性格：${character.personality}")
        }
        if (character.description.isNotEmpty()) {
            appendLine("描述：${character.description}")
        }
        if (character.scenario.isNotEmpty()) {
            appendLine("场景：${character.scenario}")
        }
        if (character.exampleMessages.isNotEmpty()) {
            appendLine("示例对话：")
            appendLine(character.exampleMessages)
        }
    }
}

private fun buildMessageList(session: ChatSession, character: CharacterCard?): List<CompletionMessage> {
    val messages = mutableListOf<CompletionMessage>()

    // System prompt
    if (session.systemPrompt.isNotEmpty()) {
        messages.add(CompletionMessage("system", session.systemPrompt))
    }

    // Character first message (as assistant)
    if (character?.firstMessage != null &&
        !session.messages.any { it.role == MessageRole.ASSISTANT }
    ) {
        messages.add(CompletionMessage("assistant", character.firstMessage))
    }

    // Chat history
    session.messages.forEach { msg ->
        when (msg.role) {
            MessageRole.USER -> messages.add(CompletionMessage("user", msg.content))
            MessageRole.ASSISTANT -> messages.add(CompletionMessage("assistant", msg.content))
            MessageRole.SYSTEM -> messages.add(CompletionMessage("system", msg.content))
            MessageRole.ACTION -> {} // skip action messages
        }
    }

    return messages
}
