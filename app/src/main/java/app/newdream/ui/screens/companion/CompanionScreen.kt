package app.newdream.ui.screens.companion

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
import app.newdream.data.model.CompanionProfile
import app.newdream.ui.components.*
import java.util.UUID

@Composable
fun CompanionListScreen(onNavigateToDetail: (String) -> Unit) {
    val settings = NewDreamApp.instance.settings
    val companions by settings.companions.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "AI伴侣") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: create companion wizard */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "创建伴侣", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (companions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FavoriteBorder,
                title = "还没有AI伴侣",
                subtitle = "从角色卡创建或新建一位专属伴侣",
                actionLabel = "创建伴侣",
                onAction = { /* TODO: wizard */ }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(companions) { companion ->
                    CompanionItem(companion = companion, onClick = { onNavigateToDetail(companion.id) })
                }
            }
        }
    }
}

@Composable
private fun CompanionItem(companion: CompanionProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarImage(text = companion.name, modifier = Modifier.size(56.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(companion.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("关系: ${companion.relationship}", style = MaterialTheme.typography.bodySmall)
                if (companion.background.isNotEmpty()) {
                    Text(companion.background.take(60), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Lv.${companion.relationshipLevel}", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanionDetailScreen(companionId: String, onBack: () -> Unit) {
    val settings = NewDreamApp.instance.settings
    val companions by settings.companions.collectAsState(initial = emptyList())
    val companion = companions.find { it.id == companionId }

    Scaffold(
        topBar = { AppTopBar(title = companion?.name ?: "伴侣详情", onBack = onBack) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(onClick = { }, label = { Text("聊天") }, leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) })
                    AssistChip(onClick = { }, label = { Text("小手机") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) })
                    AssistChip(onClick = { }, label = { Text("礼物") }, leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp)) })
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (companion != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarImage(text = companion.name, modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(companion.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(companion.relationship, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("等级", style = MaterialTheme.typography.labelSmall)
                                Text("${companion.relationshipLevel}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("活跃", style = MaterialTheme.typography.labelSmall)
                                Text(if (companion.isActive) "在线" else "离线", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (companion.background.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("背景故事", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(companion.background, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
