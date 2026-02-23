package org.kasumi321.ushio.phitracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import org.kasumi321.ushio.phitracker.domain.model.SongInfo
import org.kasumi321.ushio.phitracker.ui.theme.DifficultyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsTab(
    songs: List<SongInfo>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    getIllustrationUrl: (String) -> String?,
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("曲目图鉴 (${songs.size})") }
        )

        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("搜索曲名、ID 或作曲家") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                songs,
                key = { it.id },
                contentType = { "song_item" }
            ) { song ->
                SongItem(
                    song = song,
                    illustrationUrl = getIllustrationUrl(song.id),
                    onSongClick = onSongClick
                )
            }
        }
    }
}

@Composable
fun SongItem(
    song: SongInfo,
    illustrationUrl: String?,
    onSongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(illustrationUrl) {
        illustrationUrl?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(168)
                .crossfade(200)
                .build()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
            .clickable { onSongClick(song.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 曲绘缩略图
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = song.composer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 定数标签行
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val orderedDiffs = remember { listOf(Difficulty.EZ, Difficulty.HD, Difficulty.IN, Difficulty.AT) }
                    for (diff in orderedDiffs) {
                        val cc = song.difficulties[diff] ?: continue
                        val ccText = remember(cc) {
                            "${DifficultyColors.labelFor(diff)} ${String.format("%.1f", cc)}"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DifficultyColors.forDifficulty(diff))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ccText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
