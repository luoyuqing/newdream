package app.newdream.ui.screens.reader

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
import java.util.UUID

@Composable
fun ReaderListScreen(onNavigateToBook: (String) -> Unit) {
    val settings = NewDreamApp.instance.settings
    val progress by settings.readingProgress.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { AppTopBar(title = "小说阅读") }
    ) { padding ->
        if (progress.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MenuBook,
                title = "还没有书籍",
                subtitle = "导入TXT/EPUB文件开始阅读",
                actionLabel = "导入书籍",
                onAction = { /* TODO: file picker */ }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(progress) { item ->
                    BookItem(item = item, onClick = { onNavigateToBook(item.bookId) })
                }
            }
        }
    }
}

@Composable
private fun BookItem(item: ReadingProgress, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.bookId, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("第${item.chapterIndex + 1}章", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun ReaderBookScreen(bookId: String, onBack: () -> Unit) {
    var content by remember { mutableStateOf("") }
    var chapterIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "阅读 - $bookId",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { /* AI continue */ }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI续写")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (content.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    title = "阅读器",
                    subtitle = "选择一本小说开始阅读。\n支持AI续写、划线笔记等功能。"
                )
            } else {
                Text(content, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

// AI Continue Dialog
@Composable
fun AIContinueDialog(
    currentText: String,
    onDismiss: () -> Unit,
    onContinue: (String) -> Unit
) {
    var prompt by remember { mutableStateOf("继续写下一段...") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI续写") },
        text = {
            Column {
                Text("当前文段:", style = MaterialTheme.typography.bodySmall)
                Text(currentText.take(200), style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("续写方向") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onContinue(prompt) }) { Text("生成") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
