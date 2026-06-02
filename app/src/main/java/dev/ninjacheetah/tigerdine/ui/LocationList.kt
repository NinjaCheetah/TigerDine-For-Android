@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.OpenStatus

@Composable
fun LocationList(
    viewModel: DiningModel = viewModel(),
    onClick: ((Int) -> Unit)? = null
) {
    val filteredLocations = remember(viewModel.locationsByDay) {
        fun removeThe(name: String) =
            if (name.startsWith("The ", ignoreCase = true)) name.drop(4) else name

        viewModel.locationsByDay.firstOrNull()
            ?.sortedWith { a, b ->
                removeThe(a.name)
                    .compareTo(removeThe(b.name), ignoreCase = true)
            }
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
                                )
                            }
                        }
                    } else {
                        Text("Not Open Today")
                    }
                },
                onClick = { onClick?.invoke(location.id) },
                shapes = ListItemDefaults.segmentedShapes(index = filteredLocations.indexOf(location), count = filteredLocations.count()),
                content = {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge,
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
