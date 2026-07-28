package app.newdream.ui.screens.reader

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.newdream.NewDreamApp
import app.newdream.data.local.BundledAssetsLoader
import app.newdream.data.model.*
import app.newdream.ui.components.*
import app.newdream.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Shelves - Filter chip state for the library view.
 */
private enum class ShelfFilter(val label: String) {
    All("全部"),
    Featured("精选"),
    Ongoing("连载中"),
    Completed("已完结");
}

/**
 * Reader list screen with multiple categories.
 */
@Composable
fun ReaderListScreen(onNavigateToBook: (String) -> Unit) {
    val context = LocalContext.current
    val settings = NewDreamApp.instance.settings
    val scope = rememberCoroutineScope()
    val worlds by settings.worlds.collectAsState(initial = emptyList())
    val progress by settings.readingProgress.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf(ShelfFilter.All) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Initialize worlds from bundled assets on first run.
    LaunchedEffect(Unit) {
        val bundle = BundledAssetsLoader.loadSampleWorlds(context)
        if (bundle != null) {
            // Promote the sample worlds into the user's library on first run, generating
            // a starter chapter for each one so the reader has content to display.
            val withContent = bundle.worlds.map { populateFromSample(it) }
            settings.initializeWorldsIfEmpty(withContent)
        }
    }

    val categories = remember(worlds) {
        listOf("全部") + worlds.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filtered = remember(worlds, selectedFilter, selectedCategory) {
        worlds.filter { w ->
            val categoryOK = selectedCategory == null || selectedCategory == "全部" || w.category == selectedCategory
            val filterOK = when (selectedFilter) {
                ShelfFilter.All -> true
                ShelfFilter.Featured -> w.isFeatured
                ShelfFilter.Ongoing -> w.branchDraft < w.branchTotal
                ShelfFilter.Completed -> w.branchDraft == w.branchTotal
            }
            categoryOK && filterOK
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "小说阅读") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* TODO: file picker */ },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                text = { Text("导入书籍") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Reading Stats Card
            ReadingStatsCard(worlds = worlds, progress = progress)

            // Filter row
            ShelfFilterRow(
                current = selectedFilter,
                onChange = { selectedFilter = it },
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Category chips
            CategoryRow(
                categories = categories,
                selected = selectedCategory ?: "全部",
                onSelect = {
                    selectedCategory = if (it == "全部") null else it
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    title = "书架是空的",
                    subtitle = "选择一本书开始阅读，或导入本地 TXT/EPUB 文件",
                    actionLabel = "导入书籍",
                    onAction = { /* TODO */ }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { world ->
                        BookshelfItem(
                            world = world,
                            onClick = { onNavigateToBook(world.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingStatsCard(worlds: List<World>, progress: List<ReadingProgress>) {
    val activeCount = progress.size
    val totalRead = worlds.size
    val featuredCount = worlds.count { it.isFeatured }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "我的书架",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    StatItem(label = "在读", value = activeCount.toString())
                    StatItem(label = "书籍", value = totalRead.toString())
                    StatItem(label = "精选", value = featuredCount.toString())
                }
            }
            Icon(
                Icons.Default.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ShelfFilterRow(
    current: ShelfFilter,
    onChange: (ShelfFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShelfFilter.values().forEach { filter ->
            FilterChip(
                selected = current == filter,
                onClick = { onChange(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@Composable
private fun BookshelfItem(world: World, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Cover placeholder with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                )
                // Featured badge
                if (world.isFeatured) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "精选",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                // New/Best badge
                world.badge?.let { badge ->
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 4.dp, bottomEnd = 12.dp),
                        color = if (badge == "best") BadgeBest else BadgeNew,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (badge == "best") "Best" else "New",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = world.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${world.author} · ${world.vibe}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(world.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = formatWordCount(world.wordCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Progress bar
                Column {
                    LinearProgressIndicator(
                        progress = { world.readProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${world.branchDraft}/${world.branchTotal} 章",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = world.lastReadAgo.ifBlank { "未读" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reader screen for a specific book/world.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderBookScreen(bookId: String, onBack: () -> Unit) {
    val settings = NewDreamApp.instance.settings
    val scope = rememberCoroutineScope()
    val worlds by settings.worlds.collectAsState(initial = emptyList())
    val world = worlds.firstOrNull { it.id == bookId }
    var currentChapterIndex by remember(world) {
        val saved = world?.chapters?.indexOfFirst { it.index == 0 } ?: 0
        mutableIntStateOf(saved.coerceAtLeast(0))
    }
    var showAIDialog by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(17) }
    var showChapterList by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = world?.name ?: bookId,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (world != null) {
                            Text(
                                text = world.author,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(Icons.Default.List, contentDescription = "章节")
                    }
                    IconButton(onClick = { fontSize = if (fontSize == 17) 22 else if (fontSize == 22) 13 else 17 }) {
                        Icon(Icons.Default.TextFields, contentDescription = "字号")
                    }
                    IconButton(onClick = { showAIDialog = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI续写")
                    }
                }
            )
        }
    ) { padding ->
        if (world == null) {
            EmptyStateView(
                icon = Icons.Default.ErrorOutline,
                title = "找不到这本书",
                subtitle = "书籍可能已被移除"
            )
        } else {
            val chapter = world.chapters.getOrNull(currentChapterIndex)
                ?: world.chapters.firstOrNull()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (chapter != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "第 ${chapter.index + 1} 章 ${chapter.title}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${chapter.wordCount} 字",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Divider()
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (chapter == null || chapter.content.isBlank()) {
                        EmptyStateView(
                            icon = Icons.Default.EditNote,
                            title = "正文待续写",
                            subtitle = "点击右上角的 AI 续写按钮生成正文内容"
                        )
                    } else {
                        ReaderContent(
                            content = chapter.content,
                            fontSize = fontSize
                        )
                    }
                }

                // Chapter navigation
                if (world.chapters.size > 1) {
                    ChapterNavigationBar(
                        currentIndex = currentChapterIndex,
                        totalChapters = world.chapters.size,
                        onPrev = {
                            currentChapterIndex = currentChapterIndex.coerceAtLeast(1) - 1
                        },
                        onNext = {
                            currentChapterIndex = (currentChapterIndex + 1)
                                .coerceAtMost(world.chapters.lastIndex)
                        }
                    )
                }
            }
        }
    }

    if (showAIDialog) {
        AIContinueDialog(
            currentText = world?.chapters?.getOrNull(currentChapterIndex)?.content?.take(200) ?: "",
            onDismiss = { showAIDialog = false },
            onContinue = {
                showAIDialog = false
                // TODO: trigger AI continuation
            }
        )
    }

    if (showChapterList && world != null) {
        ModalBottomSheet(
            onDismissRequest = { showChapterList = false }
        ) {
            ChapterListSheet(
                chapters = world.chapters,
                currentIndex = currentChapterIndex,
                onSelect = { idx ->
                    currentChapterIndex = idx
                    showChapterList = false
                }
            )
        }
    }
}

@Composable
private fun ReaderContent(content: String, fontSize: Int) {
    val paragraphs = remember(content) { content.split("\n\n") }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        items(paragraphs) { paragraph ->
            Text(
                text = paragraph.trim(),
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.8).sp,
                modifier = Modifier.padding(vertical = 6.dp),
                fontStyle = if (paragraph.startsWith("「")) FontStyle.Italic else FontStyle.Normal
            )
        }
    }
}

@Composable
private fun ChapterNavigationBar(
    currentIndex: Int,
    totalChapters: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrev,
                enabled = currentIndex > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("上一章")
            }
            Text(
                text = "${currentIndex + 1} / $totalChapters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onNext,
                enabled = currentIndex < totalChapters - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("下一章")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ChapterListSheet(
    chapters: List<WorldChapter>,
    currentIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = "章节目录",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyColumn {
            items(chapters) { chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(chapter.index) }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chapter.index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (chapter.index == currentIndex) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${chapter.wordCount} 字 ${if (chapter.isDraft) "· 待续写" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (chapter.index == currentIndex) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                Text(currentText.take(200),
                    style = MaterialTheme.typography.bodySmall,
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

private fun formatWordCount(count: Int): String = when {
    count >= 10000 -> "%.1f万字".format(count / 10000.0)
    count >= 1000 -> "${count / 1000}k字"
    else -> "${count}字"
}
