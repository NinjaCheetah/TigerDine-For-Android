package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.VisitingChefStatus

@Composable
fun VisitingChefsScreen(viewModel: DiningModel = viewModel()) {
    val locations by remember {
        derivedStateOf { viewModel.locationsWithChefs }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Today's Visiting Chefs",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text("(I lied, it's not upcoming visiting chefs it's just today's right now.)")
        locations.forEach { location ->
            if (location.visitingChefs != null && !location.visitingChefs.isEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Text(
                        location.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    location.visitingChefs.forEach { chef ->
                        Text(
                            chef.name,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row {
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
                            Text(" • ")
                            Text("${chef.openTime.formatTigerDine()} - ${chef.closeTime.formatTigerDine()}")
                        }
                        Text(chef.description)
                    }
                }
            }
        }
    }
}
