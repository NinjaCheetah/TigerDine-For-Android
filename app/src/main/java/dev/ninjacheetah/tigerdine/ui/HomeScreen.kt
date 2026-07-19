package dev.ninjacheetah.tigerdine.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.components.LoadingScreen
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.util.formatLastRefreshed
import kotlin.time.Instant

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DiningModel = viewModel(),
    navController: NavController
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)

    var searchText by rememberSaveable { mutableStateOf("") }
    var showFilterMenu by rememberSaveable { mutableStateOf(false) }
    var showTopBarMenu by rememberSaveable { mutableStateOf(false) }

    val openLocationsOnly by viewModel.openLocationsOnly.collectAsState()
    val openLocationsFirst by viewModel.openLocationsFirst.collectAsState()

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.HOME) {
            updateTopBar(
                TopBarState(
                    title = "TigerDine for Android",
                    actions = {
                        IconButton(
                            onClick = { showTopBarMenu = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.menu_24px),
                                contentDescription = "Open menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showTopBarMenu,
                            onDismissRequest = { showTopBarMenu = false },
                            shape = MaterialTheme.shapes.large
                        ) {
                            DropdownMenuItem(
                                text = { Text("Refresh") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.refresh_24px),
                                        contentDescription = "Refresh"
                                    )
                                },
                                onClick = { viewModel.getHoursByDay() }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("About") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.info_24px),
                                        contentDescription = "About"
                                    )
                                },
                                onClick = {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        showTopBarMenu = false
                                        navController.navigate(Routes.ABOUT)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Feedback") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.send_24px),
                                        contentDescription = "Feedback"
                                    )
                                },
                                onClick = {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        showTopBarMenu = false
                                        navController.navigate(Routes.FEEDBACK)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Donate") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.favorite_24px),
                                        contentDescription = "Donate"
                                    )
                                },
                                onClick = {
                                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        showTopBarMenu = false
                                        navController.navigate(Routes.DONATE)
                                    }
                                }
                            )
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
        if (!viewModel.isLoaded) {
            LoadingScreen(viewModel.loadFailed)
        } else {
            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.getHoursByDay() },
                modifier = modifier
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.search_24px),
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        placeholder = {
                            Text("Search")
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            Box {
                                IconButton(
                                    onClick = { showFilterMenu = true }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.filter_list_24px),
                                        contentDescription = "Filter",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false },
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open Locations First") },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.swap_vert_24px),
                                                contentDescription = "Open locations first"
                                            )
                                        },
                                        trailingIcon = {
                                            Switch(
                                                checked = openLocationsFirst,
                                                onCheckedChange = viewModel::setOpenLocationsFirst
                                            )
                                        },
                                        onClick = {}
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Hide Closed Locations") },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.visibility_off_24px),
                                                contentDescription = "Hide closed locations"
                                            )
                                        },
                                        trailingIcon = {
                                            Switch(
                                                checked = openLocationsOnly,
                                                onCheckedChange = viewModel::setOpenLocationsOnly
                                            )
                                        },
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    )

                    SegmentedListItem(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            Text(
                                "Upcoming Visiting Chefs",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.chevron_right_24px),
                                contentDescription = "An icon of a chevron pointing to the right",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(Routes.VISITING_CHEFS)
                            }
                        },
                        shapes = ListItemDefaults.shapes(
                            shape = RoundedCornerShape(25),
                            pressedShape = RoundedCornerShape(25),
                            selectedShape = RoundedCornerShape(25),
                            focusedShape = RoundedCornerShape(25),
                            hoveredShape = RoundedCornerShape(25)
                        ),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    )

                    LocationList(
                        viewModel = viewModel,
                        navController = navController,
                        searchText = searchText,
                        openLocationsOnly = openLocationsOnly,
                        openLocationsFirst = openLocationsFirst
                    )

                    if (viewModel.lastRefreshed != null) {
                        Text(
                            "Last refreshed: ${(viewModel.lastRefreshed as Instant).formatLastRefreshed(use24Hour)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
