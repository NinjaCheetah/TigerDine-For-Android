package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.BuildConfig
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun AboutScreen(
    navController: NavHostController
) {
    val uriHandler = LocalUriHandler.current

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.ABOUT) {
            updateTopBar(
                TopBarState(
                    title = "About",
                    actions = {}
                )
            )
        }
    }

    AboutScreenContent(
        onOpenUri = { uriHandler.openUri(it) }
    )
}

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun AboutScreenContent(
    onOpenUri: (String) -> Unit
) {
    val versionCode: Int = BuildConfig.VERSION_CODE
    val versionName: String = BuildConfig.VERSION_NAME

    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_playstore),
                    contentDescription = "The TigerDine logo with a pawprint overlayed over a fork and knife",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Column {
                    Text(
                        "TigerDine for Android",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "An unofficial RIT dining app",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column {
                Text(
                    "Version $versionName (${versionCode})",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Copyright © 2025-2026 Campbell Bagley & Contributors",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Links",
                    fontWeight = FontWeight.SemiBold
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.code_24px),
                            contentDescription = "Icon of a less than symbol followed by a greater than symbol",
                        )
                    },
                    content = {
                        Text("Source Code")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://github.com/NinjaCheetah/TigerDine-for-Android")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 4
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.restaurant_24px),
                            contentDescription = "Icon of a fork and a knife",
                        )
                    },
                    content = {
                        Text("TigerCenter")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://tigercenter.rit.edu")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1,
                        count = 4
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.map_24px),
                            contentDescription = "Icon of a partially folded paper map",
                        )
                    },
                    content = {
                        Text("Official RIT Map")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://maps.rit.edu")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 2,
                        count = 4
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.menu_book_2_24px),
                            contentDescription = "Icon of a menu book",
                        )
                    },
                    content = {
                        Text("FD MealPlanner")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://fdmealplanner.com/")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 3,
                        count = 4
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )
            }

            Column {
                Text(
                    "This app is not affiliated, associated, authorized, endorsed by, or in " +
                            "any way officially connected with the Rochester Institute of " +
                            "Technology. This app is student created and maintained.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    TigerDineTheme {
        CompositionLocalProvider(LocalTopBarStateUpdater provides {}) {
            AboutScreenContent(onOpenUri = {})
        }
    }
}
