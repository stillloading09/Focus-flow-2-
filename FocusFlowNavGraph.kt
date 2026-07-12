package com.focusflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusflow.app.ui.addedit.AddEditBlockScreen
import com.focusflow.app.ui.timeline.TimelineScreen
import java.time.LocalDate

object Routes {
    const val TIMELINE = "timeline"
    const val ADD_EDIT_BLOCK = "add_edit_block?blockId={blockId}&date={date}"
    const val FOCUS_MODE = "focus_mode/{blockId}"

    fun addBlock(date: LocalDate) = "add_edit_block?blockId=new&date=$date"
    fun editBlock(blockId: Long) = "add_edit_block?blockId=$blockId"
}

@Composable
fun FocusFlowNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.TIMELINE) {
        composable(Routes.TIMELINE) {
            TimelineScreen(
                onAddBlock = { date -> navController.navigate(Routes.addBlock(date)) },
                onOpenBlock = { blockId -> navController.navigate(Routes.editBlock(blockId)) }
            )
        }
        composable(
            route = Routes.ADD_EDIT_BLOCK,
            arguments = listOf(
                navArgument("blockId") { type = NavType.StringType; defaultValue = "new" },
                navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            AddEditBlockScreen(onDone = { navController.popBackStack() })
        }
        // Focus Mode screen is built in the next milestone.
    }
}

