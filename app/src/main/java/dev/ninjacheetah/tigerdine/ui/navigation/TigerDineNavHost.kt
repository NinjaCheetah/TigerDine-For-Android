package dev.ninjacheetah.tigerdine.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.ui.AboutScreen
import dev.ninjacheetah.tigerdine.ui.DetailScreen
import dev.ninjacheetah.tigerdine.ui.HomeScreen
import dev.ninjacheetah.tigerdine.ui.VisitingChefsScreen
import dev.ninjacheetah.tigerdine.ui.menus.MenuItemScreen
import dev.ninjacheetah.tigerdine.ui.menus.MenuScreen

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun TigerDineNavHost(
    navController: NavHostController,
    viewModel: DiningModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(300)
            ) { it }
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(300)
            ) { it }
        }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                navController = navController
            )
        }

        composable(Routes.DETAIL) {
            DetailScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Routes.MENU) {
            MenuScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = Routes.MENU_ITEM,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val itemId = entry.arguments!!.getInt("itemId")

            MenuItemScreen(
                viewModel = viewModel,
                itemId = itemId,
                navController = navController
            )
        }

        composable(Routes.VISITING_CHEFS) {
            VisitingChefsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}
