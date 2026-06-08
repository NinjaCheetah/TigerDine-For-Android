package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.components.LoadingScreen

@Composable
fun MenuScreen(
    navController: NavHostController,
    viewModel: DiningModel
) {
    val updateTopBar = LocalTopBarStateUpdater.current

    LaunchedEffect(Unit) {
        updateTopBar(
            TopBarState(
                title = "Menu",
                actions = {}
            )
        )

        viewModel.getOpenPeriods()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (!viewModel.menuIsLoaded) {
            LoadingScreen()
        } else {
            for (item in viewModel.menuItems) {
                Text(item.name)
            }
        }
    }
}
