package app.newdream.ui.screens.settings

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
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SettingsScreen(
    onNavigateToProviders: () -> Unit,
    onNavigateToProvider: (String) -> Unit
) {
    val settings = NewDreamApp.instance.settings
    val darkMode by settings.darkMode.collectAsState(initial = false)
    val providers by settings.providers.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "设置") }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            // AI Models section
            item { SectionHeader("AI模型与服务") }
            item {
                SettingItem(
                    icon = Icons.Default.Cloud,
                    title = "模型供应商",
                    subtitle = "${providers.size} 个服务商已配置",
                    onClick = onNavigateToProviders,
                    trailing = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
            }
            item {
                SettingItem(
                    icon = Icons.Default.ModelTraining,
                    title = "默认模型配置",
                    subtitle = "按模态设置默认模型",
                    onClick = { onNavigateToProvider("defaults") }
                )
            }

            // Appearance
            item { SectionHeader("外观") }
            item {
                SettingItem(
                    icon = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "深色模式",
                    subtitle = if (darkMode) "当前为深色主题" else "当前为浅色主题",
                    trailing = {
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { scope.launch { settings.setDarkMode(it) } }
                        )
                    }
                )
            }

            // About
            item { SectionHeader("关于") }
            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "关于 NewDream",
                    subtitle = "v0.1.0 · 个人学习版"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(onNavigateToProvider: (String) -> Unit) {
    val settings = NewDreamApp.instance.settings
    val providers by settings.providers.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "模型供应商") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val newProvider = ApiProvider(
                        id = UUID.randomUUID().toString(),
                        name = "新服务商",
                        baseUrl = "https://api.openai.com/v1",
                        providerType = ProviderType.OPENAI_COMPATIBLE
                    )
                    scope.launch {
                        settings.saveProviders(providers + newProvider)
                        onNavigateToProvider(newProvider.id)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加服务商", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (providers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CloudOff,
                title = "还没有API服务商",
                subtitle = "添加一个OpenAI兼容的API服务商开始使用",
                actionLabel = "添加服务商",
                onAction = {
                    val newProvider = ApiProvider(
                        id = UUID.randomUUID().toString(),
                        name = "新服务商",
                        baseUrl = "https://api.openai.com/v1",
                        providerType = ProviderType.OPENAI_COMPATIBLE
                    )
                    scope.launch {
                        settings.saveProviders(listOf(newProvider))
                        onNavigateToProvider(newProvider.id)
                    }
                }
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(providers) { provider ->
                    ProviderItem(
                        provider = provider,
                        onClick = { onNavigateToProvider(provider.id) },
                        onToggle = {
                            scope.launch {
                                val updated = providers.map { p ->
                                    if (p.id == provider.id) p.copy(enabled = !p.enabled) else p
                                }
                                settings.saveProviders(updated)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                val updated = providers.filter { it.id != provider.id }
                                settings.saveProviders(updated)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderItem(
    provider: ApiProvider,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (provider.providerType) {
                    ProviderType.OPENAI_COMPATIBLE -> Icons.Default.Api
                    ProviderType.GEMINI -> Icons.Default.Psychology
                    ProviderType.ANTHROPIC -> Icons.Default.Hub
                },
                contentDescription = null,
                tint = if (provider.enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    provider.baseUrl.take(50),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    "${provider.models.size} 个模型 · ${provider.providerType.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = provider.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "删除服务商",
            message = "确定删除「${provider.name}」？\n关联的模型和池也会一并删除。",
            confirmText = "删除",
            onConfirm = { onDelete(); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(providerId: String, onBack: () -> Unit) {
    val settings = NewDreamApp.instance.settings
    val providers by settings.providers.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var provider by remember(providerId, providers) {
        mutableStateOf(providers.find { it.id == providerId })
    }

    if (provider == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("服务商未找到")
        }
        return
    }

    var testResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    fun save(p: ApiProvider) {
        provider = p
        scope.launch {
            val updated = providers.map { if (it.id == p.id) p else it }
            settings.saveProviders(updated)
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = provider!!.name, onBack = onBack) }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Provider Name
            item {
                OutlinedTextField(
                    value = provider!!.name,
                    onValueChange = { save(provider!!.copy(name = it)) },
                    label = { Text("服务商名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Provider Type
            item {
                Spacer(Modifier.height(12.dp))
                Text("协议类型", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProviderType.entries.forEach { type ->
                        FilterChip(
                            selected = provider!!.providerType == type,
                            onClick = { save(provider!!.copy(providerType = type)) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }

            // Base URL
            item {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = provider!!.baseUrl,
                    onValueChange = { save(provider!!.copy(baseUrl = it)) },
                    label = { Text("API Host") },
                    placeholder = { Text("https://api.openai.com/v1") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("必须包含完整版本前缀（如 /v1）") }
                )
            }

            // API Key
            item {
                Spacer(Modifier.height(12.dp))
                var showKey by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = provider!!.apiKey,
                    onValueChange = { save(provider!!.copy(apiKey = it)) },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None
                    else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            // Test Connection
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        isTesting = true
                        testResult = "测试中..."
                        scope.launch {
                            val api = app.newdream.data.api.ApiService()
                            val result = api.testConnection(provider!!)
                            testResult = if (result.isSuccess) "✅ 连接成功！" else "❌ ${result.exceptionOrNull()?.message}"
                            isTesting = false
                        }
                    },
                    enabled = !isTesting && provider!!.baseUrl.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("测试连接")
                }
                if (testResult.isNotEmpty()) {
                    Text(testResult, modifier = Modifier.padding(top = 8.dp))
                }
            }

            // Model management
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader("模型列表")
            }

            items(provider!!.models) { model ->
                ModelItem(
                    model = model,
                    onToggle = {
                        save(provider!!.copy(
                            models = provider!!.models.map { m ->
                                if (m.id == model.id) m.copy(enabled = !m.enabled) else m
                            }
                        ))
                    },
                    onDelete = {
                        save(provider!!.copy(
                            models = provider!!.models.filter { it.id != model.id }
                        ))
                    }
                )
            }

            // Add model button
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val newModel = AiModel(
                            id = "new-model",
                            name = "新模型",
                            providerId = provider!!.id
                        )
                        save(provider!!.copy(models = provider!!.models + newModel))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("添加模型")
                }
            }

            // Batch import from upstream
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val api = app.newdream.data.api.ApiService()
                            api.testConnection(provider!!).onSuccess {
                                val exampleModel = listOf(
                                    AiModel("gpt-4o", "GPT-4o", provider!!.id, listOf(Modality.TEXT, Modality.IMAGE_VISION)),
                                    AiModel("gpt-4o-mini", "GPT-4o Mini", provider!!.id, listOf(Modality.TEXT)),
                                    AiModel("dall-e-3", "DALL-E 3", provider!!.id, listOf(Modality.IMAGE_GENERATION)),
                                    AiModel("tts-1", "TTS-1", provider!!.id, listOf(Modality.SPEECH_SYNTHESIS)),
                                )
                                save(provider!!.copy(models = exampleModel))
                            }.onFailure {
                                testResult = "导入失败：${it.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDownload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("从上游拉取模型（示例）")
                }
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: AiModel,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(model.id, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    model.modalities.forEach { mod ->
                        Text(mod.name.take(4), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Switch(checked = model.enabled, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
