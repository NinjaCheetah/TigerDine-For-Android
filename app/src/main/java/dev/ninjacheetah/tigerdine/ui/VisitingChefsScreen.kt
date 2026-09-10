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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.DiningTimes
import dev.ninjacheetah.tigerdine.data.types.FDMPIds
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.data.types.VisitingChef
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import dev.ninjacheetah.tigerdine.util.formatVisitingChef
import dev.ninjacheetah.tigerdine.util.formatWeekdayShort
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@ExperimentalMaterial3ExpressiveApi
@Composable
fun VisitingChefsScreen(
    viewModel: DiningModel = viewModel(),
    navController: NavController
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val uriHandler = LocalUriHandler.current

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
    val lifecycleState by navBackStackEntry?.lifecycle?.currentStateFlow?.collectAsStateWithLifecycle(Lifecycle.State.INITIALIZED)
        ?: remember { mutableStateOf(Lifecycle.State.INITIALIZED) }

    LaunchedEffect(navBackStackEntry, lifecycleState) {
        if (navBackStackEntry?.destination?.route == Routes.VISITING_CHEFS && lifecycleState == Lifecycle.State.RESUMED) {
            updateTopBar(
                TopBarState(
                    title = "Visiting Chefs",
                    actions = {}
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getHoursByDayIfNeeded()
    }

    VisitingChefsScreenContent(
        use24Hour = use24Hour,
        locationsWithChefsByDay = locationsWithChefsByDay,
        daysRepresented = viewModel.daysRepresented,
        onMapClick = { uriHandler.openUri(it) }
    )
}

@ExperimentalMaterial3ExpressiveApi
@Composable
fun VisitingChefsScreenContent(
    use24Hour: Boolean,
    locationsWithChefsByDay: List<List<DiningLocation>>,
    daysRepresented: List<Instant>,
    onMapClick: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        val pagerState = rememberPagerState(pageCount = {
            daysRepresented.size
        })
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        // This block sucks, I feel like the case I have here is like super common and that there
        // should be an easier way to achieve it. Basically, I want the segmented list of buttons
        // to scroll automatically as you swipe through the pages/tap through with the arrows to
        // ensure that the surrounding days are visible in the row regardless of what day is
        // focused.
        // The first thing I tried is just scrolling to the current index, but that doesn't work
        // because it wants to align the current item with the left edge of the screen. This works
        // fine moving forwards, but if you ever decide to go backwards (gasp!), the first day will
        // stay offscreen until you actually get there which uh sucks. So this is the solution!
        LaunchedEffect(pagerState.currentPage) {
            val layoutInfo = listState.layoutInfo
            val viewportWidth = layoutInfo.viewportSize.width
            if (viewportWidth > 0) {
                val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == pagerState.currentPage }
                val itemWidth = visibleItem?.size ?: 0
                listState.animateScrollToItem(
                    index = pagerState.currentPage,
                    scrollOffset = -(viewportWidth / 2 - itemWidth / 2)
                )
            } else {
                listState.animateScrollToItem(pagerState.currentPage)
            }
        }

        if (daysRepresented.isNotEmpty()) {
            Column {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left_24px),
                            contentDescription = "Previous day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        daysRepresented[pagerState.currentPage].formatVisitingChef(),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        enabled = pagerState.currentPage < pagerState.pageCount - 1
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "Next day",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    itemsIndexed(daysRepresented) { index, instant ->
                        ToggleButton(
                            checked = index == pagerState.currentPage,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                daysRepresented.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Text(
                                text = instant.formatWeekdayShort(),
                                softWrap = false,
                                maxLines = 1,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }

                HorizontalPager(state = pagerState) { page ->
                    if (locationsWithChefsByDay[page].isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.hand_meal_24px),
                                    contentDescription = "No visiting chefs",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    "No visiting chefs today.",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Column(
                                Modifier
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                            ) {
                                locationsWithChefsByDay[page].forEach { location ->
                                    if (!location.visitingChefs.isNullOrEmpty()) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(
                                                ListItemDefaults.SegmentedGap
                                            ),
                                            modifier = Modifier.padding(0.dp, 8.dp)
                                        ) {
                                            SegmentedListItem(
                                                verticalAlignment = Alignment.CenterVertically,
                                                supportingContent = { },
                                                trailingContent = { },
                                                onClick = { },
                                                shapes = ListItemDefaults.segmentedShapes(
                                                    index = 0,
                                                    count = location.visitingChefs.count() + 1
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
                                                            onClick = { onMapClick(location.mapsUrl) },
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
                                                    supportingContent = { },
                                                    trailingContent = { },
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

                                                            if (page == 0) {
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
                                                                        color = Color.hsl(
                                                                            32f,
                                                                            1.00f,
                                                                            0.48f
                                                                        ),
                                                                        style = MaterialTheme.typography.bodyLarge
                                                                    )

                                                                    VisitingChefStatus.LEAVING_SOON -> Text(
                                                                        "Leaving Soon",
                                                                        color = Color.hsl(
                                                                            32f,
                                                                            1.00f,
                                                                            0.48f
                                                                        ),
                                                                        style = MaterialTheme.typography.bodyLarge
                                                                    )
                                                                }
                                                            } else {
                                                                Text(
                                                                    "Arriving on ${
                                                                        daysRepresented[page]
                                                                            .toLocalDateTime(
                                                                                TimeZone.currentSystemDefault()
                                                                            )
                                                                            .dayOfWeek
                                                                            .name
                                                                            .lowercase()
                                                                            .replaceFirstChar { it.uppercase() }
                                                                    }",
                                                                    color = Color.Red,
                                                                    style = MaterialTheme.typography.bodyLarge
                                                                )
                                                            }

                                                            Text(
                                                                "${
                                                                    chef.openTime.formatTigerDine(
                                                                        use24Hour
                                                                    )
                                                                } " +
                                                                        "- ${
                                                                            chef.closeTime.formatTigerDine(
                                                                                use24Hour
                                                                            )
                                                                        }",
                                                                style = MaterialTheme.typography.bodyLarge
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
                }
            }
        }
    }
}

@ExperimentalMaterial3ExpressiveApi
@Preview(showBackground = true)
@Composable
fun VisitingChefsScreenPreview() {
    TigerDineTheme {
        CompositionLocalProvider(LocalTopBarStateUpdater provides {}) {
            VisitingChefsScreenContent(
                use24Hour = false,
                daysRepresented = listOf(
                    Instant.fromEpochMilliseconds(1776960000000),
                    Instant.fromEpochMilliseconds(1777046400000),
                    Instant.fromEpochMilliseconds(1777132800000),
                    Instant.fromEpochMilliseconds(1777219200000),
                    Instant.fromEpochMilliseconds(1777305600000),
                    Instant.fromEpochMilliseconds(1777392000000),
                    Instant.fromEpochMilliseconds(1777478400000)
                ),
                locationsWithChefsByDay = listOf(
                    listOf(
                        DiningLocation(
                            id = 23,
                            mdoId = 123,
                            fdmpIds = FDMPIds(7, 7),
                            name = "The Cafe & Market at Crossroads",
                            summary = "Restaurant and Convenience Store",
                            desc = "Description here",
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
                            dailySpecials = emptyList()
                        )
                    ),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList()
                ),
                onMapClick = {}
            )
        }
    }
}
