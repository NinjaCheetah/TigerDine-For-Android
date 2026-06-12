package dev.ninjacheetah.tigerdine.ui.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ninjacheetah.tigerdine.data.state.DiningModel
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState

@Composable
fun MenuItemScreen(
    viewModel: DiningModel,
    itemId: Int
) {
    val updateTopBar = LocalTopBarStateUpdater.current

    LaunchedEffect(Unit) {
        updateTopBar(
            TopBarState(
                title = "Details",
                actions = {}
            )
        )
    }

    val item = viewModel.menuItems.find { it.id == itemId }!!

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
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
            Text(
                "${item.calories} Cal • " + if (item.price == 0.0) "Price Unavailable" else "$%.2f".format(item.price)
            )
        }

        Text(
            "Allergens",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(item.allergens.joinToString(", "))

        Text(
            "Nutrition Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        for (entry in item.nutritionalEntries) {
            Row {
                Text(entry.type)
                Spacer(modifier = Modifier.weight(1f))
                Text("%.1f".format(entry.amount) + entry.unit)
            }
        }

        Text(
            "Ingredients",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(item.ingredients)
    }
}
