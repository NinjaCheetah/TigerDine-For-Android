package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.navigation.Routes

@Composable
fun MenuItemScreen(
    viewModel: DiningModel,
    itemId: Int,
    navController: NavController
) {
    val updateTopBar = LocalTopBarStateUpdater.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.MENU_ITEM) {
            updateTopBar(
                TopBarState(
                    title = "Details",
                    actions = {}
                )
            )
        }
    }

    val item = viewModel.menuItems.find { it.id == itemId }!!

    val labelColour = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainer else Color.White
    val labelText = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else Color.Black

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ))
                {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.price != 0.0) {
                        Box(
                            modifier = Modifier.width((LocalWindowInfo.current.containerDpSize.width / 2))
                        ) {
                            Column {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    item.category,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column {
                            Text(
                                item.name,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                item.category,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (item.price != 0.0) {
                        Text(
                            text = "$%.2f".format(item.price),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = labelColour,
                )) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                Text(
                    "Nutrition Facts",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Justify,
                    color = labelText
                )
                if (item.servingSize != 0) {
                    HorizontalDivider(thickness = 1.dp, color = labelText)
                    Row() {
                        Text(
                            "Serving Size",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Left,
                            color = labelText
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "${item.servingSize} ${item.servingSizeUnit}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right,
                            color = labelText
                        )
                }
                }

                HorizontalDivider(thickness = 16.dp, color = labelText)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column() {
                        Text(
                            "Amount per serving",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Left,
                            color = labelText
                        )
                        Text(
                            "Calories",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Left,
                            color = labelText
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "${item.calories}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Right,
                        color = labelText
                    )
                }
                HorizontalDivider(thickness = 8.dp, color = labelText)
                for (entry in item.nutritionalEntries) {
                    Row {
                        when (entry.type) {
                            "Saturated Fat", "Trans Fat", "Dietary Fiber", "Total Sugars" -> {
                                Text(entry.type, modifier = Modifier
                                    .padding(16.dp, 0.dp, 0.dp, 0.dp,),
                                    color = labelText)
                            }
                            "Calcium", "Iron", "Vitamin A", "Vitamin C" -> {
                                Text(entry.type, color = labelText)
                            }
                            else -> {
                                Text(entry.type, fontWeight = FontWeight.Black, color = labelText)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text("%.1f".format(entry.amount) + entry.unit,
                            color = labelText)
                    }
                    when (entry.type) {
                        "Protein" -> HorizontalDivider(thickness = 16.dp, color = labelText)
                        "Vitamin C" -> HorizontalDivider(thickness = 8.dp, color = labelText)
                        else -> HorizontalDivider(thickness = 1.dp, color = labelText)
                    }
                }
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                )) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Column() {
                        Text(
                            "Contains: ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(item.allergens.joinToString(", "))
                    }
                    Column () {
                        Text(
                            "Ingredients: ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(item.ingredients, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
