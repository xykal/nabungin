package dev.xykal.nabungin.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.xykal.nabungin.ui.components.cardShape
import dev.xykal.nabungin.ui.goal.GoalDetailScreen
import dev.xykal.nabungin.ui.goal.GoalEditScreen
import dev.xykal.nabungin.ui.history.HistoryScreen
import dev.xykal.nabungin.ui.home.HomeScreen
import dev.xykal.nabungin.ui.icons.AppIcons
import dev.xykal.nabungin.ui.settings.SettingsScreen
import dev.xykal.nabungin.ui.stats.StatsScreen
import dev.xykal.nabungin.ui.theme.LocalNabunginColors

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val GOAL_EDIT = "goal_edit?goalId={goalId}"
    const val GOAL_DETAIL = "goal/{goalId}"

    fun goalDetail(id: Long) = "goal/$id"
    fun goalEdit(id: Long = -1L) = "goal_edit?goalId=$id"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = currentRoute == Routes.HOME || currentRoute == Routes.HISTORY || currentRoute == Routes.STATS

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenGoal = { id -> navController.navigate(Routes.goalDetail(id)) },
                    onNewGoal = { navController.navigate(Routes.goalEdit()) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenStats = { navController.navigate(Routes.STATS) },
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onOpenGoal = { id -> navController.navigate(Routes.goalDetail(id)) })
            }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.GOAL_DETAIL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
            ) { entry ->
                val goalId = entry.arguments?.getLong("goalId") ?: 0L
                GoalDetailScreen(
                    goalId = goalId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.goalEdit(id)) },
                )
            }
            composable(
                route = Routes.GOAL_EDIT,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType; defaultValue = -1L }),
            ) { entry ->
                val goalId = entry.arguments?.getLong("goalId") ?: -1L
                GoalEditScreen(
                    goalId = goalId,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (showBar) {
            BottomBar(
                currentRoute = currentRoute,
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun BottomBar(
    currentRoute: String?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNabunginColors.current
    val items = listOf(
        Triple(Routes.HOME, "Beranda", AppIcons.Home),
        Triple(Routes.HISTORY, "Riwayat", AppIcons.Ledger),
        Triple(Routes.STATS, "Statistik", AppIcons.Chart),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(cardShape(18.dp))
            .background(colors.surface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(cardShape(14.dp))
                    .clickable {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) colors.accent else colors.muted,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) colors.accent else colors.muted,
                )
            }
        }
    }
}
