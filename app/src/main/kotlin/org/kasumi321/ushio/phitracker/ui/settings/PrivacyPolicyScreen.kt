package org.kasumi321.ushio.phitracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("隐私政策") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Phi Tracker 隐私政策",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            PolicyText("最后更新日期：2026 年 3 月 8 日")

            Spacer(modifier = Modifier.height(16.dp))
            PolicyText("感谢您使用 Phi Tracker（以下简称\"本应用\"）。我们非常重视您的隐私，并致力于保护您的个人信息。本隐私政策旨在向您说明本应用如何收集、使用和保护您的数据。")

            PolicyTitle("一、我们收集的信息")
            PolicyText("本应用在运行过程中可能涉及以下数据：")
            PolicyText("1. TapTap 账号凭证：当您选择通过 TapTap 扫码登录时，本应用会获取 TapTap 授权的 sessionToken，用于从 Phigros 云端服务器读取您的游戏存档数据。")
            PolicyText("2. Phigros 游戏存档：包括您的游戏昵称、课题模式评级、游玩记录（成绩、准确率、Full Combo 状态等）和 RKS 数值。")
            PolicyText("3. 本地设置偏好：包括您选择的主题模式、自定义头像的本地 URI、B30 溢出显示设置等。")
            PolicyText("4. 运行日志（仅 Debug 版本）：Debug 版本提供导出运行日志功能，日志内容仅包含应用层面的运行信息，不含任何个人敏感数据。")

            PolicyTitle("二、数据的使用方式")
            PolicyText("您的数据仅用于以下目的：")
            PolicyText("• 读取并展示您的 Phigros 游戏存档与成绩分析（B30、RKS 计算等）。")
            PolicyText("• 存储您的个性化设置，以便在下次启动时恢复。")
            PolicyText("• 缓存曲绘资源，减少网络流量消耗。")

            PolicyTitle("三、数据的存储与安全")
            PolicyText("所有数据均存储在您的设备本地。本应用不会将您的数据上传至任何我们所控制的远程服务器。")
            PolicyText("具体而言：")
            PolicyText("• 游戏存档和成绩数据存储在应用本地的 Room 数据库中。")
            PolicyText("• 设置偏好存储在 Android SharedPreferences 中。")
            PolicyText("• 曲绘缓存存储在应用的专用缓存目录中。")
            PolicyText("• 您的 sessionToken 存储在应用的本地数据库中，且不会以任何形式被传输到第三方。")

            PolicyTitle("四、第三方服务")
            PolicyText("本应用在运行过程中会与以下第三方服务进行交互：")
            PolicyText("1. TapTap OAuth 服务：用于实现扫码登录功能，获取授权凭证。交互过程遵循 TapTap 的隐私政策。")
            PolicyText("2. Phigros 云端存档服务：用于读取您的游戏存档数据。")
            PolicyText("3. GitHub（raw.githubusercontent.com）：用于获取曲绘图片资源和曲目数据更新。")
            PolicyText("本应用不使用任何第三方分析、广告或追踪 SDK。")

            PolicyTitle("五、您的权利")
            PolicyText("您可以随时：")
            PolicyText("• 退出登录，清除本地存储的账号凭证。")
            PolicyText("• 在设置中清理本地缓存的曲绘资源。")
            PolicyText("• 通过 Android 系统的\"应用信息\"功能清除本应用的全部本地数据。")
            PolicyText("• 卸载本应用以完全删除所有相关数据。")

            PolicyTitle("六、儿童隐私")
            PolicyText("本应用不会主动收集任何 13 岁以下儿童的个人信息。如果您是未成年人的监护人，且发现您所监护的未成年人在使用本应用时提供了个人信息，请联系我们，我们将协助处理。")

            PolicyTitle("七、隐私政策的更新")
            PolicyText("我们可能会不时更新本隐私政策。更新后的隐私政策将随应用版本更新发布。我们建议您定期查阅本页面以了解最新信息。")

            PolicyTitle("八、联系我们")
            PolicyText("如果您对本隐私政策有任何疑问或建议，请通过以下方式与我们联系：")
            PolicyText("• GitHub Issues：https://github.com/Kasumi-Ushio/Ushio-Prober-Phigros/issues")

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicyTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun PolicyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
