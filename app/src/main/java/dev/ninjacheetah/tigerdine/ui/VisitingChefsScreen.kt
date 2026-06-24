package dev.ninjacheetah.tigerdine.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import dev.ninjacheetah.tigerdine.util.formatVisitingChef
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@ExperimentalMaterial3ExpressiveApi
@Composable
fun VisitingChefsScreen(
    viewModel: DiningModel = viewModel(),
    navController: NavController
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val uriHandler = LocalUriHandler.current

    var focusedIndex by rememberSaveable { mutableIntStateOf(0) }

    val locationsWithChefsByDay: List<List<DiningLocation>> = remember(viewModel.locationsByDay) {
        var newLocationsWithChefsByDay: List<List<DiningLocation>> = emptyList()

        for (day in viewModel.locationsByDay) {
            var locationsWithChefs: List<DiningLocation> = emptyList()

            for (location in day) {
                if (!location.visitingChefs.isNullOrEmpty()) {
                    locationsWithChefs = locationsWithChefs + location
                }
            }
            newLocationsWithChefsByDay = newLocationsWithChefsByDay + listOf(locationsWithChefs)
        }

        newLocationsWithChefsByDay
    }

    val updateTopBar = LocalTopBarStateUpdater.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.VISITING_CHEFS) {
            updateTopBar(
                TopBarState(
                    title = "Visiting Chefs",
                    actions = {}
                )
            )
        }

        viewModel.getHoursByDayIfNeeded()
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    IconButton(
                        onClick = { focusedIndex -= 1 },
                        enabled = focusedIndex > 0
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left_24px),
                            contentDescription = "Previous day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Visiting Chefs",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            viewModel.daysRepresented[focusedIndex].formatVisitingChef(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    IconButton(
                        onClick = { focusedIndex += 1 },
                        enabled = focusedIndex < 6
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "Next day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (locationsWithChefsByDay[focusedIndex].isEmpty()) {
                HorizontalDivider()
                Text(
                    "No visiting chefs today",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
            }

            locationsWithChefsByDay[focusedIndex].forEach { location ->
                if (!location.visitingChefs.isNullOrEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(0.dp, 8.dp)
                    ) {
                        SegmentedListItem(
                            verticalAlignment = Alignment.CenterVertically,
                            supportingContent = { },
                            trailingContent = { },
                            onClick = { },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = focusedIndex,
                                count = locationsWithChefsByDay.count()
                            ),
                            content = {
                                Row {
                                    Text(
                                        text = location.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { uriHandler.openUri(location.mapsUrl) },
                                        modifier = Modifier.size(
                                            MaterialTheme.typography.titleLarge.fontSize.value.dp
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.map_24px),
                                            contentDescription = "Show on map",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                        location.visitingChefs.forEachIndexed { index, chef ->
                            SegmentedListItem(
                                verticalAlignment = Alignment.CenterVertically,
                                supportingContent = {
                                    Row {
                                        if (focusedIndex == 0) {
                                            when (chef.status) {
                                                VisitingChefStatus.HERE_NOW -> Text(
                                                    "Here Now",
                                                    color = Color.Green,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                VisitingChefStatus.GONE -> Text(
                                                    "Left For Today",
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                VisitingChefStatus.ARRIVING_LATER -> Text(
                                                    "Arriving Later",
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                VisitingChefStatus.ARRIVING_SOON -> Text(
                                                    "Arriving Soon",
                                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                VisitingChefStatus.LEAVING_SOON -> Text(
                                                    "Leaving Soon",
                                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        } else {
                                            Text(
                                                "Arriving on ${
                                                    viewModel.daysRepresented[focusedIndex]
                                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                                        .dayOfWeek
                                                        .name
                                                        .lowercase()
                                                        .replaceFirstChar { it.uppercase() }
                                                }",
                                                color = Color.Red,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        Text(" • ")
                                        Text(
                                            "${chef.openTime.formatTigerDine(use24Hour)} - ${
                                                chef.closeTime.formatTigerDine(
                                                    use24Hour
                                                )
                                            }"
                                        )
                                    }
                                },
                                trailingContent = {
                                },
                                onClick = { },
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = index + 1,
                                    count = location.visitingChefs.count() + 1
                                ),
                                content = {
                                    Column {
                                        Text(
                                            text = chef.name,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(chef.description)
                                    }

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
    }
}
