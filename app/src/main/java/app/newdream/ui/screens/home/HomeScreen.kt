package app.newdream.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeFeature(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val route: String
)

val homeFeatures = listOf(
    HomeFeature("reader", "小说阅读", "多分支AI续写", Icons.Default.MenuBook,
        androidx.compose.ui.graphics.Color(0xFF6C63FF), "reader"),
    HomeFeature("chat", "角色聊天", "SillyTavern兼容卡", Icons.Default.Forum,
        androidx.compose.ui.graphics.Color(0xFF10B981), "chat"),
    HomeFeature("companion", "AI伴侣", "主动消息与互动", Icons.Default.Favorite,
        androidx.compose.ui.graphics.Color(0xFFFF6B9D), "companion"),
    HomeFeature("vn", "VN剧场", "视觉小说演出", Icons.Default.TheaterComedy,
        androidx.compose.ui.graphics.Color(0xFFF59E0B), "vn"),
    HomeFeature("agent", "智能体", "技能工作台", Icons.Default.AutoAwesome,
        androidx.compose.ui.graphics.Color(0xFF3B82F6), "agent"),
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("NewDream", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Welcome
            Text(
                text = "欢迎回来",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "选择你想使用的功能",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Feature Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(homeFeatures) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = { onNavigate(feature.route) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick actions
            Text(
                text = "快捷操作",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            QuickAction("导入角色卡", Icons.Default.FileUpload) { onNavigate("settings") }
            QuickAction("添加API服务商", Icons.Default.Cloud) { onNavigate("settings/providers") }
            QuickAction("当前设置", Icons.Default.Settings) { onNavigate("settings") }
        }
    }
}

@Composable
private fun FeatureCard(feature: HomeFeature, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = feature.color.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
