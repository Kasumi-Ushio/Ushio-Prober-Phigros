package org.kasumi321.ushio.phitracker.data.song

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import org.kasumi321.ushio.phitracker.domain.model.SongInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 曲目信息数据源
 *
 * 从 assets 中加载 difficulty.tsv 和 info.tsv (来自 PhigrosLibrary)
 *
 * difficulty.tsv 格式: songId, EZ, HD, IN, (AT)
 * info.tsv 格式: songId, name, composer, illustrator, (chartDesigners...)
 */
@Singleton
class SongDataProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _songs: Map<String, SongInfo>? = null

    /**
     * 获取所有曲目信息 (懒加载 + 缓存)
     */
    fun getSongs(): Map<String, SongInfo> {
        _songs?.let { return it }
        val difficulties = loadDifficulties()
        val infos = loadInfos()
        val songs = mutableMapOf<String, SongInfo>()

        for ((songId, diffs) in difficulties) {
            val info = infos[songId]
            songs[songId] = SongInfo(
                id = songId,
                name = info?.first ?: songId,
                composer = info?.second ?: "",
                illustrator = info?.third ?: "",
                difficulties = diffs
            )
        }

        _songs = songs
        return songs
    }

    /**
     * 获取定数表: songId -> { difficulty -> chartConstant }
     */
    fun getDifficultyMap(): Map<String, Map<Difficulty, Float>> {
        return getSongs().mapValues { it.value.difficulties }
    }

    /**
     * 获取歌名表: songId -> songName
     */
    fun getSongNameMap(): Map<String, String> {
        return getSongs().mapValues { it.value.name }
    }

    private fun loadDifficulties(): Map<String, Map<Difficulty, Float>> {
        val result = mutableMapOf<String, Map<Difficulty, Float>>()
        context.assets.open("difficulty.tsv").bufferedReader().useLines { lines ->
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split("\t")
                if (parts.size < 4) continue
                val songId = parts[0]
                val diffs = mutableMapOf<Difficulty, Float>()
                diffs[Difficulty.EZ] = parts[1].toFloatOrNull() ?: continue
                diffs[Difficulty.HD] = parts[2].toFloatOrNull() ?: continue
                diffs[Difficulty.IN] = parts[3].toFloatOrNull() ?: continue
                if (parts.size >= 5) {
                    parts[4].toFloatOrNull()?.let { diffs[Difficulty.AT] = it }
                }
                result[songId] = diffs
            }
        }
        return result
    }

    private fun loadInfos(): Map<String, Triple<String, String, String>> {
        val result = mutableMapOf<String, Triple<String, String, String>>()
        context.assets.open("info.tsv").bufferedReader().useLines { lines ->
            for (line in lines) {
                if (line.isBlank()) continue
                val parts = line.split("\t")
                if (parts.size < 4) continue
                val songId = parts[0]
                val name = parts[1]
                val composer = parts[2]
                val illustrator = parts[3]
                result[songId] = Triple(name, composer, illustrator)
            }
        }
        return result
    }
}
