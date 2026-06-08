package dev.ninjacheetah.tigerdine.data.state

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

data class TopBarState(
    val title: String = "TigerDine",
    val actions: @Composable RowScope.() -> Unit = {}
)

val LocalTopBarStateUpdater =
    compositionLocalOf<(TopBarState) -> Unit> {
        error("No TopBarState updater provided")
    }
