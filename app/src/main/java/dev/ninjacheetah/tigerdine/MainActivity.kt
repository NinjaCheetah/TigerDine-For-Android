package dev.ninjacheetah.tigerdine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.ui.DetailScreen
import dev.ninjacheetah.tigerdine.ui.DiningLocationRow
import dev.ninjacheetah.tigerdine.ui.VisitingChefsScreen
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TigerDineTheme {
                val navController = rememberNavController()
                val viewModel: DiningModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("TigerDine For Android Beta") },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.getHoursByDay() },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.refresh_24px),
                                        contentDescription = "Refresh icon",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onLocationClick = { locationId ->
                                    navController.navigate("detail/$locationId")
                                },
                                onVisitingChefClick = {
                                    navController.navigate("visitingChefs")
                                }
                            )
                        }
                        composable(
                            "detail/{locationId}",
                            arguments = listOf(navArgument("locationId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val locationId = backStackEntry.arguments?.getInt("locationId")!!
                            DetailScreen(
                                viewModel = viewModel,
                                locationId = locationId
                            )
                        }
                        composable("visitingChefs") {
                            VisitingChefsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DiningModel = viewModel(),
    onLocationClick: (Int) -> Unit,
    onVisitingChefClick: () -> Unit
) {
    val diningData by remember { derivedStateOf { viewModel.sortedDiningData } }

    // Automatically fetch hours when home screen loads.
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
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onVisitingChefClick() }
                    ) {
                        Surface(
                            modifier = modifier
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceBright
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Upcoming* Visiting Chefs",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                items(diningData) { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        DiningLocationRow(
                            location = location,
                            onClick = { onLocationClick(location.id) }
                        )
                    }
                }
            }
        }
    }
}
