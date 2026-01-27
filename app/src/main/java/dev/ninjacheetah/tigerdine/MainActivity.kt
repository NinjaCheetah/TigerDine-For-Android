package dev.ninjacheetah.tigerdine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ninjacheetah.tigerdine.components.formatTigerDine
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.types.OpenStatus
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TigerDineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        HomeScreen()
                    }
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
    val diningData = viewModel.diningData

//    LaunchedEffect(Unit) {
//        viewModel.getHoursByDay()
//    }

    LazyColumn(
        modifier.fillMaxSize()
    ) {
        item {
            Text("TigerDine for Android")
        }
        item {
            Button(onClick = {
                println("button pressed")
                viewModel.getHoursByDay()
            }) {
                Text("Get Data")
            }
        }

        items(
            items = diningData,
            key = { it.id }
        ) { location ->
            Text(location.name, fontWeight = FontWeight.Bold)
            when (location.open) {
                OpenStatus.OPEN -> Text("Open", color = Color.Green)
                OpenStatus.CLOSED -> Text("Closed", color = Color.Red)
                OpenStatus.OPENING_SOON -> Text("Opening Soon", color = Color.Yellow)
                OpenStatus.CLOSING_SOON -> Text("Closing Soon", color = Color.Yellow)
            }
            if (location.diningTimes != null) {
                Column {
                    location.diningTimes.forEach { opening ->
                        Row {
                            Text(opening.openTime.formatTigerDine())
                            Text(" - ")
                            Text(opening.closeTime.formatTigerDine())
                        }
                    }
                }
            } else {
                Text("Not Open Today")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
