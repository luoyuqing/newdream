package app.newdream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.newdream.data.model.Screen
import app.newdream.ui.screens.agent.AgentScreen
import app.newdream.ui.screens.chat.ChatListScreen
import app.newdream.ui.screens.chat.ChatScreen
import app.newdream.ui.screens.companion.CompanionDetailScreen
import app.newdream.ui.screens.companion.CompanionListScreen
import app.newdream.ui.screens.home.HomeScreen
import app.newdream.ui.screens.reader.ReaderListScreen
import app.newdream.ui.screens.settings.ProviderDetailScreen
import app.newdream.ui.screens.settings.ProviderListScreen
import app.newdream.ui.screens.settings.SettingsScreen
import app.newdream.ui.screens.vn.VNListScreen
import app.newdream.ui.screens.vn.VNPlayerScreen
import app.newdream.ui.theme.NewDreamTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings = NewDreamApp.instance.settings
            val darkMode by settings.darkMode.collectAsState(initial = false)

            NewDreamTheme(darkTheme = darkMode) {
                MainScreen()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "首页"),
    BottomNavItem(Screen.Reader.route, Icons.Default.MenuBook, "阅读"),
    BottomNavItem(Screen.Chat.route, Icons.Default.Forum, "角色"),
    BottomNavItem(Screen.Companion.route, Icons.Default.Favorite, "伴侣"),
    BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "设置"),
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scope = rememberCoroutineScope()

    // Determine if bottom bar should be visible
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen { route ->
                    navController.navigate(route)
                }
            }

            composable(Screen.Reader.route) {
                ReaderListScreen { bookId ->
                    navController.navigate(Screen.ReaderBook.createRoute(bookId))
                }
            }

            composable(
                route = Screen.ReaderBook.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    app.newdream.ui.screens.reader.ReaderBookScreen(
                        bookId = bookId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Chat.route) {
                ChatListScreen { characterId ->
                    navController.navigate(Screen.ChatSession.createRoute(characterId))
                }
            }

            composable(
                route = Screen.ChatSession.route,
                arguments = listOf(navArgument("characterId") { type = NavType.StringType })
            ) { backStackEntry ->
                val characterId = backStackEntry.arguments?.getString("characterId") ?: ""
                ChatScreen(
                    characterId = characterId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Companion.route) {
                CompanionListScreen { companionId ->
                    navController.navigate(Screen.CompanionChat.createRoute(companionId))
                }
            }

            composable(
                route = Screen.CompanionChat.route,
                arguments = listOf(navArgument("companionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val companionId = backStackEntry.arguments?.getString("companionId") ?: ""
                CompanionDetailScreen(
                    companionId = companionId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.VN.route) {
                VNListScreen { scriptId ->
                    navController.navigate(Screen.VNPlayer.createRoute(scriptId))
                }
            }

            composable(
                route = Screen.VNPlayer.route,
                arguments = listOf(navArgument("scriptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val scriptId = backStackEntry.arguments?.getString("scriptId") ?: ""
                VNPlayerScreen(
                    scriptId = scriptId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Agent.route) {
                AgentScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToProviders = { navController.navigate(Screen.SettingsProviders.route) },
                    onNavigateToProvider = { navController.navigate(Screen.SettingsProvider.createRoute(it)) }
                )
            }

            composable(Screen.SettingsProviders.route) {
                ProviderListScreen { providerId ->
                    navController.navigate(Screen.SettingsProvider.createRoute(providerId))
                }
            }

            composable(
                route = Screen.SettingsProvider.route,
                arguments = listOf(navArgument("providerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                ProviderDetailScreen(
                    providerId = providerId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
