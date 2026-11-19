package com.kaylakautai.inkle.ui.connection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.kaylakautai.inkle.R
import com.kaylakautai.inkle.ui.theme.AppTheme

/**
 * Wraps the app's screens with a strip that slides in below the status bar while offline (and
 * says "Back online" briefly after). It pushes the screens down rather than covering them.
 */
@Composable
fun WithConnectionBanner(
    banner: ConnectionBanner,
    content: @Composable () -> Unit
) {
    val visible = banner != ConnectionBanner.HIDDEN
    // Keep the last message while the strip slides away, so it doesn't change mid-animation.
    var shown by remember { mutableStateOf(ConnectionBanner.OFFLINE) }
    if (visible) shown = banner

    Column(Modifier.fillMaxSize()) {
        // The status bar area is always handled here (in the page colour, as every screen's
        // top is), so only the strip's own height animates and screens slide smoothly with it.
        Spacer(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.background)
        )
        AnimatedVisibility(
            visible = visible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ConnectionStrip(banner = shown)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                // Already handled above, so screens don't pad for the status bar again.
                .consumeWindowInsets(WindowInsets.statusBars)
        ) {
            content()
        }
    }
}

@Composable
private fun ConnectionStrip(banner: ConnectionBanner, modifier: Modifier = Modifier) {
    val offline = banner == ConnectionBanner.OFFLINE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            // Announced by TalkBack when it appears or changes.
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (offline) Icons.Outlined.CloudOff else Icons.Outlined.CloudDone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(if (offline) R.string.offline_banner else R.string.back_online_banner),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun OfflineStripPreview() {
    AppTheme { ConnectionStrip(ConnectionBanner.OFFLINE) }
}

@PreviewLightDark
@Composable
private fun BackOnlineStripPreview() {
    AppTheme { ConnectionStrip(ConnectionBanner.BACK_ONLINE) }
}