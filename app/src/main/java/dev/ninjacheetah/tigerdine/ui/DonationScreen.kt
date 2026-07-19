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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.ninjacheetah.tigerdine.R
import dev.ninjacheetah.tigerdine.data.state.LocalTopBarStateUpdater
import dev.ninjacheetah.tigerdine.data.state.TopBarState
import dev.ninjacheetah.tigerdine.ui.navigation.Routes
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun DonationScreen(
    navController: NavHostController
) {
    val uriHandler = LocalUriHandler.current

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.DONATE) {
            updateTopBar(
                TopBarState(
                    title = "Donate",
                    actions = {}
                )
            )
        }
    }

    DonationScreenContent(
        onOpenUri = { uriHandler.openUri(it) }
    )
}

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun DonationScreenContent(
    onOpenUri: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceDim,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.favorite_fill_24px),
                        contentDescription = "Donate",
                        tint = Color.Red
                    )

                    Text(
                        "Donate",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "TigerDine is and always will be free and open source software!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "However, app development is time consuming, and the Apple Developer Program " +
                            "membership that I have to pay for to keep the app available for our " +
                            "iPhone-owning friends is expensive. If you can, I'd appreciate it if " +
                            "you wouldn't mind tossing a coin or two my way for my time, and to " +
                            "help make that expense a little less painful."
                )
                Text(
                    "No pressure though.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.kofilogo),
                            contentDescription = "The Ko-Fi logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    },
                    content = {
                        Text("Tip Me on Ko-Fi")
                    },
                    supportingContent = {
                        Text("Chip in as much or as little as you'd like!")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://ko-fi.com/ninjacheetah")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Image(
                            painter = painterResource(R.drawable.paypallogo),
                            contentDescription = "The PayPal logo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    },
                    content = {
                        Text("Send Me Money Directly")
                    },
                    supportingContent = {
                        Text("PayPal won't take a cut this way!")
                    },
                    onClick = {
                        onOpenUri("https://paypal.me/NinjaCheetahX")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DonationScreenPreview() {
    TigerDineTheme {
        CompositionLocalProvider(LocalTopBarStateUpdater provides {}) {
            DonationScreenContent(onOpenUri = {})
        }
    }
}
