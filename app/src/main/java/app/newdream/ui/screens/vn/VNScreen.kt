package app.newdream.ui.screens.vn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.newdream.data.model.*
import app.newdream.ui.components.AppTopBar

@Composable
fun VNListScreen(onNavigateToPlayer: (String) -> Unit) {
    Scaffold(
        topBar = { AppTopBar(title = "VN剧场") }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            EmptyStateView(
                icon = Icons.Default.TheaterComedy,
                title = "VN剧场模式",
                subtitle = "将小说或聊天会话以视觉小说形式呈现\n支持角色立绘、对话演出和多分支选择",
                actionLabel = "从当前阅读创建",
                onAction = { /* TODO */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VNPlayerScreen(scriptId: String, onBack: () -> Unit) {
    var currentSceneIndex by remember { mutableStateOf(0) }
    var currentDialogueIndex by remember { mutableStateOf(0) }
    var showChoices by remember { mutableStateOf(false) }

    // Demo scene data
    val demoScene = VNScene(
        index = 0,
        background = "",
        dialogues = listOf(
            VNDialogue(characterId = "char1", characterName = "???", text = "你终于醒了。", emotion = "neutral"),
            VNDialogue(characterId = "char1", characterName = "林悦", text = "这里不是你该来的地方……但既然来了，就陪我聊聊吧。", emotion = "smile"),
            VNDialogue(characterId = "", characterName = "", text = "你环顾四周，发现自己身处一座陌生的庭院中。", emotion = "neutral"),
        ),
        choices = listOf(
            VNChoice(text = "问：这里是什么地方？", targetSceneIndex = 1),
            VNChoice(text = "沉默地打量四周", targetSceneIndex = 2),
            VNChoice(text = "问她是谁", targetSceneIndex = 3),
        )
    )

    val dialogue = demoScene.dialogues.getOrNull(currentDialogueIndex)

    Scaffold(
        topBar = { AppTopBar(title = "VN剧场", onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
                )
                .clickable {
                    if (!showChoices) {
                        if (currentDialogueIndex < demoScene.dialogues.size - 1) {
                            currentDialogueIndex++
                        } else {
                            showChoices = true
                        }
                    }
                }
        ) {
            // Character name & dialogue at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 0f,
                            endY = 300f
                        )
                    )
                    .padding(24.dp)
                    .padding(bottom = 48.dp)
            ) {
                if (dialogue != null) {
                    if (dialogue.characterName.isNotEmpty()) {
                        Text(
                            text = dialogue.characterName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF6C63FF),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = dialogue.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                }

                if (showChoices) {
                    Spacer(Modifier.height(24.dp))
                    demoScene.choices.forEach { choice ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { /* navigate to target scene */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0x33FFFFFF)
                            )
                        ) {
                            Text(
                                text = choice.text,
                                modifier = Modifier.padding(16.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Tap hint
            if (!showChoices) {
                Text(
                    text = "点击继续",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
