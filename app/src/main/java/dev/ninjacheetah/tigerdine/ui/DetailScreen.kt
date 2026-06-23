package dev.ninjacheetah.tigerdine.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.constant.tCtoFDMPMap
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus
import dev.ninjacheetah.tigerdine.data.types.WeeklyHours
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.util.formatNextOpen
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@ExperimentalMaterial3ExpressiveApi
@Composable
fun DetailScreen(
    viewModel: DiningModel = viewModel(),
    navController: NavController,
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val uriHandler = LocalUriHandler.current

    val location = viewModel.locationsByDay.first().find { it.id == viewModel.focusedLocationId }
    var expandHours by rememberSaveable { mutableStateOf(true) }
    var expandChefs by rememberSaveable { mutableStateOf(false) }
    var expandDailies by rememberSaveable { mutableStateOf(false) }

    val weeklyHours: List<WeeklyHours> = remember(viewModel.locationsByDay) {
        var newWeeklyHours: List<WeeklyHours> = emptyList()

        for (day in viewModel.locationsByDay) {
            for (location in day) {
                if (location.id == viewModel.focusedLocationId) {
                    val weekdayStr: String = location.date.toLocalDateTime(TimeZone.currentSystemDefault())
                        .dayOfWeek
                        .name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                    if (!location.diningTimes.isNullOrEmpty()) {
                        var timeStrings: List<String> = emptyList()

                        for (time in location.diningTimes) {
                            timeStrings = timeStrings + "${time.openTime.formatTigerDine(use24Hour)} - ${time.closeTime.formatTigerDine(use24Hour)}"
                        }

                        newWeeklyHours = newWeeklyHours + WeeklyHours(
                            day = weekdayStr,
                            date = location.date,
                            timeStrings = timeStrings
                        )
                    } else {
                        newWeeklyHours = newWeeklyHours + WeeklyHours(
                            day = weekdayStr,
                            date = location.date,
                            timeStrings = listOf("Closed")
                        )
                    }
                }
            }
        }

        newWeeklyHours
    }

    val updateTopBar = LocalTopBarStateUpdater.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.DETAIL) {
            updateTopBar(
                TopBarState(
                    title = "Details",
                    actions = {
                        if (location != null) {
                            IconButton(
                                onClick = { uriHandler.openUri(location.mapsUrl) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.map_24px),
                                    contentDescription = "Show on map",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (tCtoFDMPMap.contains(viewModel.focusedLocationId)) {
                            IconButton(
                                onClick = {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        navController.navigate(Routes.MENU)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.menu_book_2_24px),
                                    contentDescription = "Show menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            )
        }

        viewModel.getHoursByDayIfNeeded()
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        if (location != null) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width((LocalWindowInfo.current.containerDpSize.width / 2))
                        ) {
                            Column {
                                Text(
                                    location.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    location.summary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Column(
                            horizontalAlignment = Alignment.End,
                        ) {
                            when (location.open) {
                                OpenStatus.OPEN -> Text(
                                    "Open",
                                    color = Color.Green,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                OpenStatus.CLOSED -> Text(
                                    "Closed",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                OpenStatus.OPENING_SOON -> Text(
                                    "Opening Soon",
                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                    style = MaterialTheme.typography.titleLarge
                                )

                                OpenStatus.CLOSING_SOON -> Text(
                                    "Closing Soon",
                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            if (location.diningTimes != null) {
                                location.diningTimes.forEach { opening ->
                                    Text(
                                        "${opening.openTime.formatTigerDine(use24Hour)} - ${
                                            opening.closeTime.formatTigerDine(
                                                use24Hour
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            } else {
                                Text("Not Open Today")
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    SegmentedListItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.schedule_24px),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.rotate(if (expandHours) 0F else 180F)
                            )
                        },
                        onClick = {
                            expandHours = !expandHours
                        },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = 0,
                            count = if (!location.visitingChefs.isNullOrEmpty() || !location.dailySpecials.isNullOrEmpty() || expandHours) 2 else 1
                        ),
                        content = {
                            Row {
                                when (location.open) {
                                    OpenStatus.OPEN -> Text(
                                        "Open",
                                        color = Color.Green,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    OpenStatus.CLOSED -> Text(
                                        "Closed",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    OpenStatus.OPENING_SOON -> Text(
                                        "Opening Soon",
                                        color = Color.hsl(32f, 1.00f, 0.48f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    OpenStatus.CLOSING_SOON -> Text(
                                        "Closing Soon",
                                        color = Color.hsl(32f, 1.00f, 0.48f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Text(
                                    text = " • "
                                )
                                if (location.open == OpenStatus.OPEN || location.open == OpenStatus.CLOSING_SOON) {
                                    Text(
                                        text = "Closes ${
                                            location.diningTimes?.first()?.closeTime?.formatTigerDine(
                                                use24Hour
                                            )
                                        }"
                                    )
                                } else {
                                    var opensNext = ""

                                    for (day in viewModel.locationsByDay) {
                                        if (opensNext != "") {
                                            break
                                        }

                                        if (viewModel.locationsByDay.indexOf(day) == 0) {
                                            println("triggered")
                                            continue
                                        }

                                        for (loc in day) {
                                            if (loc.id == viewModel.focusedLocationId) {
                                                if (!loc.diningTimes.isNullOrEmpty()) {
                                                    opensNext = "Opens ${
                                                        loc.diningTimes.first().openTime.formatNextOpen(
                                                            use24Hour
                                                        )
                                                    }"
                                                    break
                                                }
                                            }
                                        }
                                    }
                                    if (opensNext != "") {
                                        Text(
                                            text = opensNext
                                        )
                                    } else {
                                        Text(
                                            text = "Closed this week"
                                        )
                                    }
                                }
                            }

                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    )
                    if (expandHours) {
                        SegmentedListItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            leadingContent = {

                            },
                            trailingContent = {

                            },
                            onClick = {},
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 1,
                                count = if (!location.visitingChefs.isNullOrEmpty() || !location.dailySpecials.isNullOrEmpty()) 3 else 2
                            ),
                            content = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    for (day in weeklyHours) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            if (weeklyHours.indexOf(day) == 0) {
                                                Text(
                                                    text = day.day,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Column {
                                                    for (timeString in day.timeStrings) {
                                                        Text(
                                                            text = timeString,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text = day.day,
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Column {
                                                    for (timeString in day.timeStrings) {
                                                        Text(
                                                            text = timeString,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                    }
                    if (!location.visitingChefs.isNullOrEmpty()) {
                        SegmentedListItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.hand_meal_24px),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.rotate(if (expandChefs) 0F else 180F)
                                )
                            },
                            onClick = {
                                expandChefs = !expandChefs
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 3,
                                count = 2
                            ),
                            content = {
                                Text(
                                    "Today's Visiting Chefs",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                        if (expandChefs) {
                            SegmentedListItem(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                leadingContent = { },
                                trailingContent = { },
                                onClick = { },
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = 4,
                                    count = 6
                                ),
                                content = {
                                    Column {
                                        location.visitingChefs.forEach { chef ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        chef.name,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
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
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                                Column {
                                                    Text(
                                                        "${chef.openTime.formatTigerDine(use24Hour)} - ${
                                                            chef.closeTime.formatTigerDine(
                                                                use24Hour
                                                            )
                                                        }",
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            )
                        }
                    }
                    if (!location.dailySpecials.isNullOrEmpty()) {
                        SegmentedListItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.restaurant_24px),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.rotate(if (expandDailies) 0F else 180F)
                                )
                            },
                            onClick = {
                                expandDailies = !expandDailies
                            },
                            shapes = ListItemDefaults.segmentedShapes(
                                index = 4,
                                count = if (expandDailies) 6 else 5
                            ),
                            content = {
                                Text(
                                    "Today's Daily Specials",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                        if (expandDailies) {
                            SegmentedListItem(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                leadingContent = { },
                                trailingContent = { },
                                onClick = { },
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = 5,
                                    count = if (expandDailies) 6 else 5
                                ),
                                content = {
                                    Column {
                                        location.dailySpecials.forEach { special ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = special.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(
                                                    text = special.type,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    textAlign = TextAlign.Right
                                                )

                                            }
                                        }
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                            )
                        }
                    }
                }


                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                ) {
                    Text(
                        text = location.desc,
                        modifier = Modifier.padding(16.dp)
                    )
                }

            }
        } else {
            Text("Location not found")
        }
    }
}
