package dev.ninjacheetah.tigerdine.ui.navigation

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
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                viewModel = viewModel
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
                navArgument("locationId") {
                    type = NavType.IntType
                },
                navArgument("itemId") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val locationId = entry.arguments!!.getInt("locationId")
            val itemId = entry.arguments!!.getInt("itemId")

            MenuItemScreen(
                navController = navController,
                viewModel = viewModel,
                locationId = locationId,
                itemId = itemId
            )
        }

        composable(Routes.VISITING_CHEFS) {
            VisitingChefsScreen(
                viewModel = viewModel
            )
        }
    }
}
