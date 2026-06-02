@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import kotlin.invoke

@Composable
fun DiningLocationRow(
    viewModel: DiningModel = viewModel(),
    onClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
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
        Column() {
            items(filteredLocations) { location ->
                Surface(
                    modifier = modifier
                        .clickable(enabled = onClick != null) {
                            onClick?.invoke()
                        },
                    color = MaterialTheme.colorScheme.surfaceBright
                ) {
                    SegmentedListItem(
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
                                        Row {
                                            Text(opening.openTime.formatTigerDine() + " - " + opening.closeTime.formatTigerDine())
                                        }
                                    }
                                }
                            } else {
                                Text("Not Open Today")
                            }
                        },
                        onClick = { onClick?.invoke() },
                        shapes = ListItemDefaults.segmentedShapes(index = 1, count = 1),
                        content = {
                            Text(
                                text = location.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                    )
                }
            }
        }

    }
}

