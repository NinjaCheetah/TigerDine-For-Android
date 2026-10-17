package dev.ninjacheetah.tigerdine.ui.foodtrucks

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.compose.AsyncImage
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.components.LoadingScreen
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.navigation.Routes.menuItem
import dev.ninjacheetah.tigerdine.util.formatTigerDine
import dev.ninjacheetah.tigerdine.util.formatVisitingChef
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun FoodTruckScreen(
    navController: NavHostController,
    viewModel: DiningModel
) {
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val lifecycleState by navBackStackEntry?.lifecycle?.currentStateFlow?.collectAsStateWithLifecycle(Lifecycle.State.INITIALIZED)
        ?: remember { mutableStateOf(Lifecycle.State.INITIALIZED) }

    LaunchedEffect(navBackStackEntry, lifecycleState) {
        if (navBackStackEntry?.destination?.route == Routes.FOOD_TRUCKS && lifecycleState == Lifecycle.State.RESUMED) {
            updateTopBar(
                TopBarState(
                    title = "Weekend Food Trucks",
                    actions = {}
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getFoodTrucks()
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!viewModel.foodTruckIsLoaded) {
            LoadingScreen(viewModel.loadFailed)
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                for (date in viewModel.getDatesWithTrucks()) {
                    Text(
                        text = date.formatVisitingChef(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        val foodTrucksForDate = viewModel.getFoodTrucksForDate(date)
                        for (truck in foodTrucksForDate) {
                            val availability = truck.availability.first {
                                val timeZone = TimeZone.currentSystemDefault()
                                it.date.toLocalDateTime(timeZone).date == date.toLocalDateTime(
                                    timeZone
                                ).date
                            }

                            SegmentedListItem(
                                verticalAlignment = Alignment.CenterVertically,
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = foodTrucksForDate.indexOf(truck),
                                    count = foodTrucksForDate.count()
                                ),
                                content = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = truck.imageUrl,
                                            contentDescription = truck.imageAltTag,
                                            modifier = Modifier.padding(end = 10.dp)
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                                        ) {
                                            Text(
                                                text = truck.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${availability.openTime.formatTigerDine(use24Hour)} - ${availability.closeTime.formatTigerDine(use24Hour)}"
                                            )
                                            Text(
                                                text = availability.locationName,
                                                color = Color.hsl(32f, 1.00f, 0.48f)
                                            )
                                            Text(
                                                text = truck.description
                                            )
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
    }
}
