package dev.ninjacheetah.tigerdine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.volley.toolbox.Volley
import dev.ninjacheetah.tigerdine.data.DiningModel
import dev.ninjacheetah.tigerdine.data.DiningModelFactory
import dev.ninjacheetah.tigerdine.data.DiningRepository
import dev.ninjacheetah.tigerdine.data.SettingsRepository
import dev.ninjacheetah.tigerdine.data.dataStore
import dev.ninjacheetah.tigerdine.ui.navigation.TigerDineNavHost
import dev.ninjacheetah.tigerdine.ui.navigation.routeTitle
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun TigerDineApp() {
    TigerDineTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val context = LocalContext.current

        val requestQueue = remember {
            Volley.newRequestQueue(context.applicationContext)
        }
        val diningRepository = remember {
            DiningRepository(requestQueue)
        }
        val settingsRepository = remember {
            SettingsRepository(context.dataStore)
        }

        val factory = DiningModelFactory(
            diningRepository = diningRepository,
            settingsRepository = settingsRepository
        )
        val viewModel: DiningModel = viewModel(factory = factory)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(routeTitle(currentRoute))
                    },
                    navigationIcon = {
                        if (currentRoute != "home") {
                            IconButton(
                                onClick = { navController.navigateUp() }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back_24px),
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute?.startsWith("detail/") == true) {
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceDim
        ) { innerPadding ->
            TigerDineNavHost(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
