package dev.ninjacheetah.tigerdine.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import dev.ninjacheetah.tigerdine.data.state.DiningModel
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
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val favoriteLocations by viewModel.favoriteLocations.collectAsState()

    val filteredLocations = remember(
        viewModel.locationsByDay,
        favoriteLocations,
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
                    !favoriteLocations.contains(it.id)
                }.thenBy {
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

    // The list should handle its own spacing rather than letting the container including it
    // make that call.
    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        if (viewModel.isLoaded) {
            if (filteredLocations.isEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No results found.")
                }
            }

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
                                        text = "${opening.openTime.formatTigerDine(use24Hour)} - ${
                                            opening.closeTime.formatTigerDine(
                                                use24Hour
                                            )
                                        }",
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
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            viewModel.focusedLocationId = location.id
                            navController.navigate(Routes.DETAIL)
                        }
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = filteredLocations.indexOf(
                            location
                        ), count = filteredLocations.count()
                    ),
                    content = {
                        val annotatedName = remember(location.name, favoriteLocations) {
                            buildAnnotatedString {
                                append(location.name)
                                if (favoriteLocations.contains(location.id)) {
                                    // Spacer added to make the star not be too close to the text.
                                    append(" ")
                                    appendInlineContent("favorite", "[favorite]")
                                }
                            }
                        }

                        val inlineContent = if (favoriteLocations.contains(location.id)) {
                            mapOf(
                                "favorite" to InlineTextContent(
                                    Placeholder(
                                        width = 20.sp,
                                        height = 20.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.star_fill_24px),
                                        contentDescription = "Favorite location",
                                        tint = Color.hsl(48.0f, 1.00f, 0.50f)
                                    )
                                }
                            )
                        } else {
                            emptyMap()
                        }

                        Text(
                            text = annotatedName,
                            inlineContent = inlineContent,
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
