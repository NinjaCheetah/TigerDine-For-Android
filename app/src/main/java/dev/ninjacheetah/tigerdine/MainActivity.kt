@file:OptIn(ExperimentalMaterial3Api::class)

package dev.ninjacheetah.tigerdine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import dev.ninjacheetah.tigerdine.ui.LocationList
import dev.ninjacheetah.tigerdine.ui.VisitingChefsScreen
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

class MainActivity : ComponentActivity() {
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
                            title = {
                                Text("TigerDine For Android Beta")
                            },
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
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { navController.navigateUp() },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back_24px),
                                        contentDescription = "Back icon",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceDim
                ) { innerPadding ->
                    // Automatically fetch hours when the app loads.
                    // This is here because putting it inside HomeScreen was making it re-run
                    // every time you navigated back which was a lot of wasted network calls.
                    LaunchedEffect(Unit) {
                        viewModel.getHoursByDay()
                    }

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
                            arguments = listOf(navArgument("locationId") { type = NavType.IntType }),
                            enterTransition = {
                                slideInHorizontally(animationSpec = tween(300)) {
                                        fullWidth -> fullWidth / 1
                                }
                            },
                            exitTransition = {
                                slideOutHorizontally(animationSpec = tween(500)) {
                                        fullWidth -> fullWidth / 1
                                }
                            }
                        ) { backStackEntry ->
                            val locationId = backStackEntry.arguments?.getInt("locationId")!!
                            DetailScreen(
                                viewModel = viewModel,
                                locationId = locationId
                            )
                        }
                        composable(
                            "visitingChefs",
                            enterTransition = {
                                slideInHorizontally(animationSpec = tween(300)) {
                                        fullWidth -> fullWidth / 1
                                }
                            },
                            exitTransition = {
                                slideOutHorizontally(animationSpec = tween(500)) {
                                        fullWidth -> fullWidth / 1
                                }
                            }
                        ) {
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

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: DiningModel = viewModel(),
    onLocationClick: (Int) -> Unit,
    onVisitingChefClick: () -> Unit
) {

    Column {
        Surface(
            color = MaterialTheme.colorScheme.surfaceDim,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
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
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(12.dp)
                        ) {
                            Text(
                                "Upcoming Visiting Chefs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                painter = painterResource(R.drawable.chevron_right_24px),
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                LocationList(
                    viewModel = viewModel,
                    onClick = onLocationClick
                )
            }
        }
    }
}
