package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.data.types.FDMenuItem
import dev.ninjacheetah.tigerdine.ui.components.LoadingScreen
import dev.ninjacheetah.tigerdine.ui.navigation.Routes.menuItem

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
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

    val filteredMenuItems = remember(viewModel.menuItems) {
        viewModel.menuItems.sortedWith(
                compareBy {
                    it.name.lowercase()
                }
            )
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!viewModel.menuIsLoaded) {
            LoadingScreen()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                for (item in filteredMenuItems) {
                    SegmentedListItem(
                        verticalAlignment = Alignment.CenterVertically,
                        supportingContent = {
                            Text("${item.calories} Cal")
                        },
                        trailingContent = {
                            if (item.price == 0.0) {
                                Text(
                                    "Price Unavailable",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            } else {
                                Text(
                                    "$%.2f".format(item.price),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        },
                        onClick = { navController.navigate(menuItem(item.id)) },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = filteredMenuItems.indexOf(item),
                            count = filteredMenuItems.count()
                        ),
                        content = {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    )
                }
            }
        }
    }
}
