package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.OpenStatus

@Composable
fun DiningLocationRow(
    location: DiningLocation,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            },
        color = MaterialTheme.colorScheme.surfaceBright
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Column(
                modifier = Modifier
                    .padding(all = 4.dp)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                when (location.open) {
                    OpenStatus.OPEN -> Text("Open", color = Color.Green)
                    OpenStatus.CLOSED -> Text("Closed", color = Color.Red)
                    OpenStatus.OPENING_SOON -> Text(
                        "Opening Soon",
                        color = Color.hsl(32f, 1.00f, 0.48f)
                    )
                    OpenStatus.CLOSING_SOON -> Text(
                        "Closing Soon",
                        color = Color.hsl(32f, 1.00f, 0.48f)
                    )
                }
                if (location.diningTimes != null) {
                    Column {
                        location.diningTimes.forEach { opening ->
                            Row {
                                Text(opening.openTime.formatTigerDine() + " - " + opening.closeTime.formatTigerDine())
                            }
                        }
                    }
                } else {
                    Text("Not Open Today")
                }
            }
        }
    }
}
