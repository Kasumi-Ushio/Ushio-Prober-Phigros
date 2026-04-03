package org.kasumi321.ushio.phitracker.ui.b30

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import org.kasumi321.ushio.phitracker.domain.model.BestRecord
import org.kasumi321.ushio.phitracker.utils.ImageStorageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun B30ImageScreen(
    b30: List<BestRecord>,
    displayRks: Float,
    nickname: String,
    challengeModeRank: Int,
    moneyString: String,
    clearCounts: Map<String, Int>,
    fcCount: Int,
    phiCount: Int,
    avatarUri: String?,
    lowIllustrationUrlProvider: (String) -> String,
    standardIllustrationUrlProvider: (String) -> String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(true) }
    var scale by remember { mutableFloatStateOf(0.8f) }
    var showBackgroundSelector by remember { mutableStateOf(false) }
    var selectedBackgroundSongId by remember { mutableStateOf<String?>(null) }
    var customBackgroundUri by remember { mutableStateOf<String?>(null) }
    val backgroundCandidates = remember(b30) { b30.map { it.songId to it.songName }.distinctBy { it.first } }
    val albumPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        customBackgroundUri = uri?.toString()
        if (uri != null) selectedBackgroundSongId = null
    }

    // 异步生成图片
    LaunchedEffect(
        b30,
        displayRks,
        nickname,
        challengeModeRank,
        moneyString,
        clearCounts,
        fcCount,
        phiCount,
        avatarUri,
        selectedBackgroundSongId,
        customBackgroundUri
    ) {
        isGenerating = true
        bitmap = B30ImageGenerator.generate(
            context = context,
            imageLoader = context.imageLoader,
            b30 = b30,
            displayRks = displayRks,
            nickname = nickname,
            challengeModeRank = challengeModeRank,
            moneyString = moneyString,
            clearCounts = clearCounts,
            fcCount = fcCount,
            phiCount = phiCount,
            avatarUri = avatarUri,
            selectedBackgroundSongId = selectedBackgroundSongId,
            customBackgroundUri = customBackgroundUri,
            lowIllustrationUrlProvider = lowIllustrationUrlProvider,
            standardIllustrationUrlProvider = standardIllustrationUrlProvider
        )
        isGenerating = false
    }

    if (showBackgroundSelector) {
        BackgroundSelectorDialog(
            candidates = backgroundCandidates,
            selectedSongId = selectedBackgroundSongId,
            illustrationUrlProvider = lowIllustrationUrlProvider,
            onDismiss = { showBackgroundSelector = false },
            onSelect = { songId ->
                selectedBackgroundSongId = songId
                customBackgroundUri = null
                showBackgroundSelector = false
            },
            onPickAlbum = {
                showBackgroundSelector = false
                albumPicker.launch("image/*")
            },
            onAuto = {
                selectedBackgroundSongId = null
                customBackgroundUri = null
                showBackgroundSelector = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("B30 图片") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showBackgroundSelector = true }) {
                        Icon(Icons.Filled.Image, contentDescription = "选择背景")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在生成 B30 图片...")
                    }
                }
            } else {
                bitmap?.let { bmp ->
                    // 可缩放预览
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.1f, 2f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "B30 成绩图",
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                        )
                    }

                    // 操作按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val path = ImageStorageHelper.saveToPictures(context, bmp, "B30_${System.currentTimeMillis()}.png")
                                if (path != null) {
                                    Toast.makeText(context, "已保存到 Pictures/PhiTracker", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Text("  保存", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                val path = ImageStorageHelper.saveToPictures(context, bmp, "B30_${System.currentTimeMillis()}.png")
                                if (path != null) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "分享 B30"))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Text("  分享", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundSelectorDialog(
    candidates: List<Pair<String, String>>,
    selectedSongId: String?,
    illustrationUrlProvider: (String) -> String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onPickAlbum: () -> Unit,
    onAuto: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("选择背景", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onAuto) { Text("默认背景") }
                    OutlinedButton(onClick = onPickAlbum) { Text("相册图片") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(360.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(candidates, key = { it.first }) { item ->
                        val songId = item.first
                        val songName = item.second
                        val isSelected = selectedSongId == songId
                        Column(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .clickable { onSelect(songId) }
                                .padding(6.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(illustrationUrlProvider(songId))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = songName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .clip(MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = songName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
