package org.kasumi321.ushio.phitracker.data.song

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import org.kasumi321.ushio.phitracker.domain.model.NoteCount
import org.kasumi321.ushio.phitracker.domain.model.SongInfo
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 曲目信息数据源
 *
 * 从 assets 中加载 difficulty.csv 和 info.csv (来自 phi-plugin)
 * 新增强化：加载 infolist.json 和 notesInfo.json 用于详情页数据
 *
 * CSV 遵循 RFC 4180 格式:
 * - 逗号分隔
 * - 含逗号/引号的字段用双引号包裹
 * - 字段内的双引号用 "" 转义
 *
 * songId 加载时追加 ".0" 以匹配存档格式
 */
@Singleton
class SongDataProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _songs: Map<String, SongInfo>? = null

    private val json = Json { ignoreUnknownKeys = true }

    fun invalidateCache() {
        _songs = null
    }

    private fun openFileStream(fileName: String): InputStream {
        val file = File(context.filesDir, "song_data/$fileName")
        return if (file.exists() && file.isFile) {
            FileInputStream(file)
        } else {
            context.assets.open(fileName)
        }
    }

    fun getSongs(): Map<String, SongInfo> {
        _songs?.let { return it }
        val difficulties = loadDifficulties()
        val infos = loadInfos()
        val additionalInfo = runCatching { loadAdditionalInfo() }.getOrDefault(emptyMap())
        val notesInfo = runCatching { loadNotesInfo() }.getOrDefault(emptyMap())

        val songs = mutableMapOf<String, SongInfo>()

        for ((songId, diffs) in difficulties) {
            val info = infos[songId]
            val rawId = songId.removeSuffix(".0")
            val addInfo = additionalInfo[rawId]
            val noteInfo = notesInfo[rawId]

            val noteCounts = diffs.keys.associateWith { difficulty ->
                val tArray = noteInfo?.get(difficulty.name)?.t ?: emptyList()
                if (tArray.size >= 4) {
                    NoteCount(tArray[0], tArray[1], tArray[2], tArray[3])
                } else {
                    NoteCount()
                }
            }.filter { it.value.total > 0 }

            songs[songId] = SongInfo(
                id = songId,
                name = info?.name ?: songId,
                composer = info?.composer ?: "",
                illustrator = info?.illustrator ?: "",
                difficulties = diffs,
                bpm = addInfo?.bpm ?: "",
                chapter = addInfo?.chapter ?: "",
                length = addInfo?.length ?: "",
                charters = info?.charters ?: emptyMap(),
                noteCounts = noteCounts
            )
        }

        _songs = songs
        return songs
    }

    fun getDifficultyMap(): Map<String, Map<Difficulty, Float>> {
        return getSongs().mapValues { it.value.difficulties }
    }

    fun getSongNameMap(): Map<String, String> {
        return getSongs().mapValues { it.value.name }
    }

    private fun loadDifficulties(): Map<String, Map<Difficulty, Float>> {
        val result = mutableMapOf<String, Map<Difficulty, Float>>()
        openFileStream("difficulty.csv").bufferedReader().useLines { lines ->
            for ((index, line) in lines.withIndex()) {
                if (index == 0) continue // 跳过表头
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.size < 4) continue
                val songId = parts[0] + ".0"
                val diffs = mutableMapOf<Difficulty, Float>()
                parts.getOrNull(1)?.toFloatOrNull()?.let { diffs[Difficulty.EZ] = it }
                parts.getOrNull(2)?.toFloatOrNull()?.let { diffs[Difficulty.HD] = it }
                parts.getOrNull(3)?.toFloatOrNull()?.let { diffs[Difficulty.IN] = it }
                parts.getOrNull(4)?.toFloatOrNull()?.let { diffs[Difficulty.AT] = it }
                if (diffs.isNotEmpty()) {
                    result[songId] = diffs
                }
            }
        }
        return result
    }

    private data class InfoCsvModel(
        val name: String,
        val composer: String,
        val illustrator: String,
        val charters: Map<Difficulty, String>
    )

    private fun loadInfos(): Map<String, InfoCsvModel> {
        val result = mutableMapOf<String, InfoCsvModel>()
        openFileStream("info.csv").bufferedReader().useLines { lines ->
            for ((index, line) in lines.withIndex()) {
                if (index == 0) continue // 跳过表头
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.size < 4) continue
                val songId = parts[0] + ".0"
                val name = parts[1]
                val composer = parts[2]
                val illustrator = parts[3]

                val charters = mutableMapOf<Difficulty, String>()
                parts.getOrNull(4)?.takeIf { it.isNotBlank() }?.let { charters[Difficulty.EZ] = it }
                parts.getOrNull(5)?.takeIf { it.isNotBlank() }?.let { charters[Difficulty.HD] = it }
                parts.getOrNull(6)?.takeIf { it.isNotBlank() }?.let { charters[Difficulty.IN] = it }
                parts.getOrNull(7)?.takeIf { it.isNotBlank() }?.let { charters[Difficulty.AT] = it }

                result[songId] = InfoCsvModel(name, composer, illustrator, charters)
            }
        }
        return result
    }

    @Serializable
    private data class InfoListEntry(
        val bpm: String = "",
        val length: String = "",
        val chapter: String = ""
    )

    private fun loadAdditionalInfo(): Map<String, InfoListEntry> {
        val jsonString = openFileStream("infolist.json").bufferedReader().readText()
        return json.decodeFromString(jsonString)
    }

    @Serializable
    private data class NotesInfoDifficulty(
        val t: List<Int> = emptyList()
    )

    private fun loadNotesInfo(): Map<String, Map<String, NotesInfoDifficulty>> {
        val jsonString = openFileStream("notesInfo.json").bufferedReader().readText()
        return json.decodeFromString(jsonString)
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            current.append('"')
                            i += 2
                            continue
                        } else {
                            inQuotes = false
                        }
                    } else {
                        current.append(c)
                    }
                }
                c == '"' -> {
                    inQuotes = true
                }
                c == ',' -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> {
                    current.append(c)
                }
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }
}
