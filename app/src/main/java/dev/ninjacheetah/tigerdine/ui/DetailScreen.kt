package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus

@Composable
fun DetailScreen(locationId: Int, viewModel: DiningModel = viewModel()) {
    val location = viewModel.sortedDiningData.find { it.id == locationId }

    if (location != null) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            when (location.open) {
                OpenStatus.OPEN -> Text("Open", color = Color.Green, style = MaterialTheme.typography.titleLarge)
                OpenStatus.CLOSED -> Text("Closed", color = Color.Red, style = MaterialTheme.typography.titleLarge)
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
                    Text("${opening.openTime.formatTigerDine()} - ${opening.closeTime.formatTigerDine()}",
                        style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Text("Not Open Today")
            }
            if (location.visitingChefs != null && !location.visitingChefs.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Today's Visiting Chefs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                location.visitingChefs.forEach { chef ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(chef.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Column {
                            when (chef.status) {
                                VisitingChefStatus.HERE_NOW -> Text("Here Now", color = Color.Green, style = MaterialTheme.typography.bodyLarge)
                                VisitingChefStatus.GONE -> Text("Left For Today", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                                VisitingChefStatus.ARRIVING_LATER -> Text("Arriving Later", color = Color.Red, style = MaterialTheme.typography.bodyLarge)
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
                            Text("${chef.openTime.formatTigerDine()} - ${chef.closeTime.formatTigerDine()}",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    HorizontalDivider()
                }
            }
            if (location.dailySpecials != null && !location.dailySpecials.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Today's Daily Specials",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                location.dailySpecials.forEach { special ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(special.name, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(special.type, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Upcoming Hours",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            // TODO: make this actually show upcoming hours and not just today's
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text("Today")
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    if (location.diningTimes != null) {
                        location.diningTimes.forEach { opening ->
                            Text("${opening.openTime.formatTigerDine()} - ${opening.closeTime.formatTigerDine()}",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Text("Closed")
                    }
                }
            }
            HorizontalDivider() // TODO: make this part of the loop over future dining times
            Spacer(modifier = Modifier.height(16.dp))
            Text(location.desc)
        }
    } else {
        Text("Location not found")
    }
}
