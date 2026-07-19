package dev.ninjacheetah.tigerdine.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
fun FeedbackScreen(
    navController: NavHostController
) {
    val uriHandler = LocalUriHandler.current

    val updateTopBar = LocalTopBarStateUpdater.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Routes.FEEDBACK) {
            updateTopBar(
                TopBarState(
                    title = "Feedback",
                    actions = {}
                )
            )
        }
    }

    FeedbackScreenContent(
        onOpenUri = { uriHandler.openUri(it) }
    )
}

@ExperimentalMaterial3Api
@ExperimentalMaterial3ExpressiveApi
@Composable
fun FeedbackScreenContent(
    onOpenUri: (String) -> Unit
) {
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
            Column {
                Text(
                    "Did I break something? Oops.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Or maybe you just have a suggestion to make TigerDine even cooler. Either " +
                            "way, I'd love to hear your feedback! (Or maybe the hours for a " +
                            "location are off, in which case that feedback is RIT's to handle.)"
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Incorrect Location Hours",
                    fontWeight = FontWeight.SemiBold
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.globe_2_question_24px),
                            contentDescription = "Icon of a globe with a question mark in the bottom right",
                        )
                    },
                    content = {
                        Text("Confirm Against the RIT Website")
                    },
                    supportingContent = {
                        Text("Check that the hours displayed in TigerDine match RIT's website.")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://www.rit.edu/dining/locations")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.send_24px),
                            contentDescription = "Icon of a paper airplane",
                        )
                    },
                    content = {
                        Text("Submit an ITS Ticket")
                    },
                    supportingContent = {
                        Text("If hours are also incorrect on RIT's website, submit a ticket to ITS.")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://www.rit.edu/its/support")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                Text(
                    "If the hours do not match between TigerDine and RIT's website, please " +
                            "contact me instead and I'll look into it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "App Issues and Feedback",
                    fontWeight = FontWeight.SemiBold
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.bug_report_24px),
                            contentDescription = "Icon of a bug",
                        )
                    },
                    content = {
                        Text("Submit a GitHub Issue")
                    },
                    supportingContent = {
                        Text("Report a bug or suggest a feature on TigerDine for Android's GitHub repository.")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("https://github.com/NinjaCheetah/TigerDine-for-Android/issues")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                SegmentedListItem(
                    verticalAlignment = Alignment.CenterVertically,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.mail_24px),
                            contentDescription = "Icon of an envelope",
                        )
                    },
                    content = {
                        Text("Send Me an Email")
                    },
                    supportingContent = {
                        Text("Not a GitHub user? Feel free to submit feedback via email.")
                    },
                    trailingContent = {
                        Icon(
                            painter = painterResource(R.drawable.chevron_right_24px),
                            contentDescription = "An icon of a chevron pointing to the right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = {
                        onOpenUri("mailto:campbell@ninjacheetah.dev")
                    },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = 1,
                        count = 2
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                )

                Text(
                    "Just don't spam my inbox, please and thank you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FeedbackScreenPreview() {
    TigerDineTheme {
        CompositionLocalProvider(LocalTopBarStateUpdater provides {}) {
            FeedbackScreenContent(onOpenUri = {})
        }
    }
}
