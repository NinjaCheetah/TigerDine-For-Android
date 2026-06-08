package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState

@Composable
fun MenuScreen(
    navController: NavHostController,
    viewModel: DiningModel,
    locationId: Int
) {
    val updateTopBar = LocalTopBarStateUpdater.current

    LaunchedEffect(Unit) {
        updateTopBar(
            TopBarState(
                title = "Menu",
                actions = {}
            )
        )
    }

    Column {
        Text("eel!")
    }
}
