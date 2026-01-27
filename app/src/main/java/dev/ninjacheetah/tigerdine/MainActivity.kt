package dev.ninjacheetah.tigerdine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.ninjacheetah.tigerdine.data.types.DiningLocation
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TigerDineTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("TigerDine For Android Beta")
                            },
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

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
                    fontWeight = FontWeight.Bold
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

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DiningModel = viewModel()
) {
    val diningData by remember { derivedStateOf { viewModel.sortedDiningData } }

    LaunchedEffect(Unit) {
        viewModel.getHoursByDay()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp)
            ) {
                items(diningData) { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DiningLocationRow(location)
                    }
                }
            }
        }
    }
}
