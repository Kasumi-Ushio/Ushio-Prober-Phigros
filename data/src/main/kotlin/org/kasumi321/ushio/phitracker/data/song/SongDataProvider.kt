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
 * 从 assets 中加载 difficulty.csv 和 info.csv (来自 phi-plugin)
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

    fun getDifficultyMap(): Map<String, Map<Difficulty, Float>> {
        return getSongs().mapValues { it.value.difficulties }
    }

    fun getSongNameMap(): Map<String, String> {
        return getSongs().mapValues { it.value.name }
    }

    /**
     * 解析 difficulty.csv
     * 格式: id,EZ,HD,IN,AT (第一行为表头)
     */
    private fun loadDifficulties(): Map<String, Map<Difficulty, Float>> {
        val result = mutableMapOf<String, Map<Difficulty, Float>>()
        context.assets.open("difficulty.csv").bufferedReader().useLines { lines ->
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

    /**
     * 解析 info.csv
     * 格式: id,song,composer,illustrator,EZ,HD,IN,AT (第一行为表头)
     */
    private fun loadInfos(): Map<String, Triple<String, String, String>> {
        val result = mutableMapOf<String, Triple<String, String, String>>()
        context.assets.open("info.csv").bufferedReader().useLines { lines ->
            for ((index, line) in lines.withIndex()) {
                if (index == 0) continue // 跳过表头
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.size < 4) continue
                val songId = parts[0] + ".0"
                val name = parts[1]
                val composer = parts[2]
                val illustrator = parts[3]
                result[songId] = Triple(name, composer, illustrator)
            }
        }
        return result
    }

    /**
     * RFC 4180 CSV 行解析器
     *
     * 处理:
     * - 双引号包裹的字段 (含逗号/换行)
     * - "" 转义双引号
     * - 普通无引号字段
     */
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
                        // 查看是否是转义的引号 ""
                        if (i + 1 < line.length && line[i + 1] == '"') {
                            current.append('"')
                            i += 2
                            continue
                        } else {
                            // 引号结束
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
