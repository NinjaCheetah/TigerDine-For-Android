package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.constant.fdmpMealPeriodsMap
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.components.LoadingScreen
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.navigation.Routes.menuItem

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun MenuScreen(
    navController: NavHostController,
    viewModel: DiningModel
) {
    var showMealPeriodsPicker by rememberSaveable { mutableStateOf(false) }

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.MENU) {
            updateTopBar(
                TopBarState(
                    title = "Menu",
                    actions = {
                        IconButton(
                            onClick = { showMealPeriodsPicker = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.schedule_24px),
                                contentDescription = "Show meal periods",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMealPeriodsPicker,
                            onDismissRequest = { showMealPeriodsPicker = false },
                            shape = MaterialTheme.shapes.large
                        ) {
                            viewModel.openPeriods.forEach { opening ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .selectable(
                                            selected = (opening == viewModel.selectedMealPeriod),
                                            onClick = { viewModel.changeSelectedMealPeriod(opening) },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (opening == viewModel.selectedMealPeriod),
                                        onClick = null
                                    )
                                    fdmpMealPeriodsMap[opening]?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            )
        }

        viewModel.getOpenPeriods()
    }

    var searchText by rememberSaveable { mutableStateOf("") }

    val filteredMenuItems = remember(
        viewModel.menuItems,
        searchText
    ) {
        viewModel.menuItems
            .filter {
                searchText.isBlank() || it.name.contains(searchText, ignoreCase = true)
            }.sortedWith(
                compareBy {
                    it.name.lowercase()
                }
            )
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!viewModel.menuIsLoaded || viewModel.haveMenuForLocationId != viewModel.focusedLocationId) {
            LoadingScreen(viewModel.loadFailed)
        } else if (viewModel.menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.calendar_meal_24px),
                        contentDescription = "No menu available",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No menu is available for the selected meal period today. " +
                            "Try selecting a different meal period.",
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                    ,
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
                                onClick = { println("filter btn tapped") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.filter_list_24px),
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )

                if (filteredMenuItems.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("No results found.")
                    }
                }

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
                        onClick = {
                            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                navController.navigate(menuItem(item.id))
                            }
                        },
                        shapes = ListItemDefaults.segmentedShapes(
                            index = filteredMenuItems.indexOf(item),
                            count = filteredMenuItems.count()
                        ),
                        content = {
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row {
                                    for (dietaryMarker in item.dietaryMarkers) {
                                        val chipColor = when (dietaryMarker) {
                                            "Vegan", "Vegetarian" -> Color.hsl(134.27f, 0.5697f, 0.4922f)
                                            else -> Color.hsl(28.47f, 1.00f, 0.5784f)
                                        }

                                        Surface(
                                            color = chipColor,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = dietaryMarker,
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
        }
    }
}
