package app.newdream.ui.screens.agent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.newdream.NewDreamApp
import app.newdream.data.model.*
import app.newdream.ui.components.*

@Composable
fun AgentScreen() {
    val settings = NewDreamApp.instance.settings
    val actions by settings.agentActions.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var executionHistory by remember { mutableStateOf(listOf<AgentExecution>()) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "智能体工作台",
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "新建技能")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Agent skills grid
            SectionHeader("可用技能")
            LazyColumn {
                items(builtinAgentActions) { action ->
                    AgentActionItem(
                        action = action,
                        onClick = { /* execute */ },
                        onEdit = { /* edit */ }
                    )
                }

                if (actions.isNotEmpty()) {
                    item { SectionHeader("自定义技能") }
                    items(actions) { action ->
                        AgentActionItem(
                            action = action,
                            onClick = { /* execute */ },
                            onEdit = { /* edit */ }
                        )
                    }
                }

                if (builtinAgentActions.isEmpty() && actions.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.AutoAwesome,
                            title = "智能体技能",
                            subtitle = "使用AI辅助完成创作、分析和自定义任务",
                            actionLabel = "查看可用技能"
                        )
                    }
                }

                // Execution history
                if (executionHistory.isNotEmpty()) {
                    item { SectionHeader("执行记录") }
                    items(executionHistory) { exec ->
                        ExecutionItem(exec = exec)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentActionItem(
    action: AgentAction,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (action.category) {
                    AgentCategory.AUTHORING -> Icons.Default.Edit
                    AgentCategory.ILLUSTRATION -> Icons.Default.Image
                    AgentCategory.ANALYSIS -> Icons.Default.Analytics
                    AgentCategory.UTILITY -> Icons.Default.Build
                    AgentCategory.COMPANION -> Icons.Default.Favorite
                    AgentCategory.CUSTOMIZATION -> Icons.Default.Tune
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(action.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (action.description.isNotEmpty()) {
                    Text(action.description.take(80), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (action.requiresApproval) {
                Icon(Icons.Default.Lock, contentDescription = "需确认",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ExecutionItem(exec: AgentExecution) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (exec.status) {
                    AgentStatus.COMPLETED -> Icons.Default.CheckCircle
                    AgentStatus.FAILED -> Icons.Default.Error
                    AgentStatus.RUNNING -> Icons.Default.Sync
                    else -> Icons.Default.HourglassEmpty
                },
                contentDescription = null,
                tint = when (exec.status) {
                    AgentStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    AgentStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(exec.input.take(60), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}

val builtinAgentActions = listOf(
    AgentAction(
        id = "card-author",
        name = "角色卡创作",
        description = "根据描述生成SillyTavern兼容的角色卡，含人设/开场白/例对话",
        prompt = "请根据用户提供的角色描述，生成一张角色卡。",
        category = AgentCategory.AUTHORING,
        requiresApproval = false
    ),
    AgentAction(
        id = "story-continue",
        name = "AI续写",
        description = "基于上下文继续创作小说/故事内容",
        prompt = "请基于上文继续写作。注意保持风格一致。",
        category = AgentCategory.AUTHORING,
        requiresApproval = false
    ),
    AgentAction(
        id = "image-prompt",
        name = "配图提示词",
        description = "为当前场景生成AI绘图提示词",
        prompt = "请为以下场景生成详细的图像生成提示词。",
        category = AgentCategory.ILLUSTRATION,
        requiresApproval = false
    ),
    AgentAction(
        id = "card-analyze",
        name = "角色卡分析",
        description = "分析角色卡的人设、优势和可改进之处",
        prompt = "请分析以下角色卡的内容。",
        category = AgentCategory.ANALYSIS,
        requiresApproval = false
    ),
    AgentAction(
        id = "world-build",
        name = "世界观构建",
        description = "辅助构建世界观设定和世界书条目",
        prompt = "请帮助构建一个详细的世界观设定。",
        category = AgentCategory.UTILITY,
        requiresApproval = false
    ),
)
