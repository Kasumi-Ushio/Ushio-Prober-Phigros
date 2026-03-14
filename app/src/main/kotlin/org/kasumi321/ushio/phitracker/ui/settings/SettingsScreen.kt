package org.kasumi321.ushio.phitracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.kasumi321.ushio.phitracker.ui.home.HomeViewModel

/**
 * 设置页面（全屏版本）
 * 设置从底部 Tab 迁移为独立页面，由首页右上角齿轮按钮触发导航
 */
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val tip = remember { viewModel.getRandomTip() }

    SettingsTab(
        themeMode = state.themeMode,
        showB30Overflow = state.showB30Overflow,
        overflowCount = state.overflowCount,
        onThemeModeChange = { viewModel.setThemeMode(it) },
        onShowB30OverflowChange = { viewModel.setShowB30Overflow(it) },
        onOverflowCountChange = { viewModel.setOverflowCount(it) },
        onClearHighResCache = { viewModel.clearHighResCache() },
        onRedownloadIllustrations = { viewModel.resetIllustrationDownloadAndExit() },
        onNavigateToAbout = onNavigateToAbout,
        onLogout = onLogout,
        tip = tip,
        onNavigateBack = onNavigateBack
    )
}
