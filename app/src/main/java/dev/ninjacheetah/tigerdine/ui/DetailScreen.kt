package dev.ninjacheetah.tigerdine.ui

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
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
import dev.ninjacheetah.tigerdine.data.types.DailySpecial
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.DiningTimes
import dev.ninjacheetah.tigerdine.data.types.FDMPIds
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.data.types.VisitingChef
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus
import dev.ninjacheetah.tigerdine.data.types.WeeklyHours
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme
import dev.ninjacheetah.tigerdine.util.formatNextOpen
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@ExperimentalMaterial3ExpressiveApi
@Composable
fun DetailScreen(
    viewModel: DiningModel = viewModel(),
    navController: NavController,
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val uriHandler = LocalUriHandler.current

    val location = viewModel.locationsByDay.firstOrNull()?.find { it.id == viewModel.focusedLocationId }
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

    var opensNext = ""

    for (day in viewModel.locationsByDay) {
        if (opensNext != "") {
            break
        }

        if (viewModel.locationsByDay.indexOf(day) == 0) {
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
                                onClick = { viewModel.toggleFavorite(location.id) }
                            ) {
                                val favoriteIcon = if (viewModel.favoriteLocations.collectAsState().value.contains(location.id)) {
                                    R.drawable.star_fill_24px
                                } else {
                                    R.drawable.star_24px
                                }

                                Icon(
                                    painter = painterResource(favoriteIcon),
                                    contentDescription = "Toggle favorite",
                                    tint = Color.hsl(48.0f, 1.00f, 0.50f)
                                )
                            }

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

    DetailScreenContent(
        location = location,
        use24Hour = use24Hour,
        weeklyHours = weeklyHours,
        opensNext = opensNext,
        expandHours = expandHours,
        onExpandHoursChange = { expandHours = it },
        expandChefs = expandChefs,
        onExpandChefsChange = { expandChefs = it },
        expandDailies = expandDailies,
        onExpandDailiesChange = { expandDailies = it }
    )
}

@ExperimentalMaterial3ExpressiveApi
@Composable
fun DetailScreenContent(
    location: DiningLocation?,
    use24Hour: Boolean,
    weeklyHours: List<WeeklyHours>,
    opensNext: String,
    expandHours: Boolean,
    onExpandHoursChange: (Boolean) -> Unit,
    expandChefs: Boolean,
    onExpandChefsChange: (Boolean) -> Unit,
    expandDailies: Boolean,
    onExpandDailiesChange: (Boolean) -> Unit
) {
    val screenWidth = LocalWindowInfo.current.containerDpSize.width
    val isWideScreen = screenWidth >= 600.dp

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
                        Column {
                            Text(
                                location.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                location.summary,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            LocationInfoList(
                                location = location,
                                use24Hour = use24Hour,
                                weeklyHours = weeklyHours,
                                opensNext = opensNext,
                                expandHours = expandHours,
                                onExpandHoursChange = onExpandHoursChange,
                                expandChefs = expandChefs,
                                onExpandChefsChange = onExpandChefsChange,
                                expandDailies = expandDailies,
                                onExpandDailiesChange = onExpandDailiesChange
                            )
                        }
                        DescriptionCard(
                            description = location.desc,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LocationInfoList(
                            location = location,
                            use24Hour = use24Hour,
                            weeklyHours = weeklyHours,
                            opensNext = opensNext,
                            expandHours = expandHours,
                            onExpandHoursChange = onExpandHoursChange,
                            expandChefs = expandChefs,
                            onExpandChefsChange = onExpandChefsChange,
                            expandDailies = expandDailies,
                            onExpandDailiesChange = onExpandDailiesChange
                        )
                        DescriptionCard(
                            description = location.desc
                        )
                    }
                }
            }
        } else {
            Text("Location not found")
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@Composable
private fun LocationInfoList(
    location: DiningLocation,
    use24Hour: Boolean,
    weeklyHours: List<WeeklyHours>,
    opensNext: String,
    expandHours: Boolean,
    onExpandHoursChange: (Boolean) -> Unit,
    expandChefs: Boolean,
    onExpandChefsChange: (Boolean) -> Unit,
    expandDailies: Boolean,
    onExpandDailiesChange: (Boolean) -> Unit
) {
    val hasVisitingChefs = !location.visitingChefs.isNullOrEmpty()
    val hasDailySpecials = !location.dailySpecials.isNullOrEmpty()
    val segmentCount = 1 + (if (hasVisitingChefs) 1 else 0) + (if (hasDailySpecials) 1 else 0)
    var currentSegmentIndex = 0

    Column(
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
    ) {
        SegmentedListItem(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(16.dp),
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.schedule_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
            },
            onClick = {
                onExpandHoursChange(!expandHours)
            },
            shapes = if (segmentCount == 1) ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium) else ListItemDefaults.segmentedShapes(
                index = currentSegmentIndex++,
                count = segmentCount
            ),
            content = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                            contentDescription = if (expandHours) "Collapse Hours" else "Expand Hours",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (expandHours) 0f else 180f)
                        )
                    }
                    AnimatedVisibility(
                        visible = expandHours,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
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
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Column {
                                            for (timeString in day.timeStrings) {
                                                Text(
                                                    text = timeString,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = day.day,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Column {
                                            for (timeString in day.timeStrings) {
                                                Text(
                                                    text = timeString,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
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

        if (hasVisitingChefs) {
            SegmentedListItem(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                contentPadding = PaddingValues(16.dp),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.hand_meal_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                },
                onClick = {
                    onExpandChefsChange(!expandChefs)
                },
                shapes = if (segmentCount == 1) ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium) else ListItemDefaults.segmentedShapes(
                    index = currentSegmentIndex++,
                    count = segmentCount
                ),
                content = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today's Visiting Chefs",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                contentDescription = if (expandChefs) "Collapse Visiting Chefs" else "Expand Visiting Chefs",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (expandChefs) 0f else 180f)
                            )
                        }
                        AnimatedVisibility(
                            visible = expandChefs,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                location.visitingChefs.forEach { chef ->
                                    Row(
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                chef.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            when (chef.status) {
                                                VisitingChefStatus.HERE_NOW -> Text(
                                                    "Here Now",
                                                    color = Color.Green,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                VisitingChefStatus.GONE -> Text(
                                                    "Left For Today",
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                VisitingChefStatus.ARRIVING_LATER -> Text(
                                                    "Arriving Later",
                                                    color = Color.Red,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                VisitingChefStatus.ARRIVING_SOON -> Text(
                                                    "Arriving Soon",
                                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )

                                                VisitingChefStatus.LEAVING_SOON -> Text(
                                                    "Leaving Soon",
                                                    color = Color.hsl(32f, 1.00f, 0.48f),
                                                    style = MaterialTheme.typography.bodyMedium
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
                                                style = MaterialTheme.typography.bodyMedium
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

        if (hasDailySpecials) {
            SegmentedListItem(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                contentPadding = PaddingValues(16.dp),
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.restaurant_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                },
                onClick = {
                    onExpandDailiesChange(!expandDailies)
                },
                shapes = if (segmentCount == 1) ListItemDefaults.shapes(shape = MaterialTheme.shapes.medium) else ListItemDefaults.segmentedShapes(
                    index = currentSegmentIndex++,
                    count = segmentCount
                ),
                content = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today's Daily Specials",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                                contentDescription = if (expandDailies) "Collapse Specials" else "Expand Specials",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(if (expandDailies) 0f else 180f)
                            )
                        }
                        AnimatedVisibility(
                            visible = expandDailies,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                location.dailySpecials.forEach { special ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = special.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = special.type,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Right
                                        )
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
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Text(
            text = description,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    TigerDineTheme {
        CompositionLocalProvider(LocalTopBarStateUpdater provides {}) {
            DetailScreenContent(
                // This sample data is taken from the actual data for Crossroads on April 23, 2026.
                // Why this day? I kept using Imagine's date as a sample, but then I realized that
                // a weekend date is maybe not the best to test with. So I just stepped it back
                // a single day to get a better picture of what a typical day looks like.
                location = DiningLocation(
                    id = 23,
                    mdoId = 123,
                    fdmpIds = FDMPIds(
                        locationId = 7,
                        accountId = 7
                    ),
                    name = "The Cafe & Market at Crossroads",
                    summary = "Restaurant and Convenience Store",
                    desc = """
                        Located in the Crossroads building in Global Village, the Cafe and Market at Crossroads features a food court and convenience store. Inside of Crossroads are eight different food stations: grill, salad bar, pasta toss, pizza, Asian cuisine, deli, chef specials, and visiting chefs.
                        This location is cashless everyday after 7 p.m. Customers using cash may use a Tiger Spend Reload Station to add funds to an existing RIT ID card or a Reload Card.
                    """.trimIndent(),
                    mapsUrl = "https://maps.rit.edu/?mdo_id=123",
                    date = Instant.fromEpochMilliseconds(1776960000000),
                    diningTimes = listOf(
                        DiningTimes(
                            openTime = Instant.fromEpochMilliseconds(1776954600000),
                            closeTime = Instant.fromEpochMilliseconds(1776992400000)
                        )
                    ),
                    open = OpenStatus.OPEN,
                    visitingChefs = listOf(
                        VisitingChef(
                            name = "Esan's Kitchen",
                            description = "Traditional Nigerian cuisine",
                            openTime = Instant.fromEpochMilliseconds(1776956400000),
                            closeTime = Instant.fromEpochMilliseconds(1776967200000),
                            status = VisitingChefStatus.HERE_NOW
                        ),
                        VisitingChef(
                            name = "P.H. Express",
                            description = "Traditional Pakistani cuisine",
                            openTime = Instant.fromEpochMilliseconds(1776974400000),
                            closeTime = Instant.fromEpochMilliseconds(1776985200000),
                            status = VisitingChefStatus.ARRIVING_LATER
                        )
                    ),
                    dailySpecials = listOf(
                        DailySpecial(
                            name = "General Tso Chicken",
                            type = "asian"
                        ),
                        DailySpecial(
                            name = "Poutine",
                            type = "Grill"
                        )
                    )
                ),
                use24Hour = false,
                weeklyHours = listOf(
                    WeeklyHours(
                        day = "Friday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 9:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Saturday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 6:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Sunday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 4:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Monday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 9:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Tuesday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 9:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Wednesday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 9:00 PM"
                        )
                    ),
                    WeeklyHours(
                        day = "Thursday",
                        date = Instant.fromEpochMilliseconds(1776960000000),
                        timeStrings = listOf(
                            "10:30 AM - 9:00 PM"
                        )
                    )
                ),
                opensNext = "",
                expandHours = true,
                onExpandHoursChange = {},
                expandChefs = true,
                onExpandChefsChange = {},
                expandDailies = true,
                onExpandDailiesChange = {}
            )
        }
    }
}
