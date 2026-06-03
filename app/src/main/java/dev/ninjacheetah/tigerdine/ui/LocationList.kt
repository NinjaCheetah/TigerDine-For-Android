package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.ui.navigation.Routes

@ExperimentalMaterial3ExpressiveApi
@Composable
fun LocationList(
    viewModel: DiningModel = viewModel(),
    navController: NavController,
    searchText: String,
    openLocationsOnly: Boolean,
    openLocationsFirst: Boolean
) {
    val filteredLocations = remember(
        viewModel.locationsByDay,
        searchText,
        openLocationsOnly,
        openLocationsFirst
    ) {
        fun removeThe(name: String) =
            if (name.startsWith("The ", ignoreCase = true)) name.drop(4) else name

        viewModel.locationsByDay
            .firstOrNull()
            ?.filter {
               if (openLocationsOnly) {
                   it.open == OpenStatus.OPEN || it.open == OpenStatus.CLOSING_SOON
               } else {
                   true
               }
            }?.filter {
                searchText.isBlank() || it.name.contains(searchText, ignoreCase = true)
            }?.sortedWith(
                compareBy<DiningLocation> {
                    if (openLocationsFirst) {
                        !(it.open == OpenStatus.OPEN || it.open == OpenStatus.CLOSING_SOON)
                    } else {
                        false
                    }
                }.thenBy {
                    removeThe(it.name).lowercase()
                }
            )
            ?: emptyList()
    }

    if (viewModel.isLoaded) {
        for (location in filteredLocations) {
            SegmentedListItem(
                verticalAlignment = Alignment.CenterVertically,
                supportingContent = {
                    when (location.open) {
                        OpenStatus.OPEN -> Text("Open", color = Color.Green)
                        OpenStatus.CLOSED -> Text("Closed", color = Color.Red)
                        OpenStatus.OPENING_SOON -> Text(
                            "Opening Soon",
                            color = Color.hsl(32f, 1.00f, 0.48f)
                        )

                        OpenStatus.CLOSING_SOON -> Text(
                            "Closing Soon",
                            color = Color.hsl(32f, 1.00f, 0.48f)
                        )
                    }
                },
                trailingContent = {
                    if (location.diningTimes != null) {
                        Column {
                            location.diningTimes.forEach { opening ->
                                Text(
                                    text = "${opening.openTime.formatTigerDine()} - ${opening.closeTime.formatTigerDine()}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    } else {
                        Text(
                            "Not Open Today",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                onClick = {
                    navController.navigate(Routes.detail(location.id))
                },
                shapes = ListItemDefaults.segmentedShapes(index = filteredLocations.indexOf(location), count = filteredLocations.count()),
                content = {
                    Text(
                        text = location.name,
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
