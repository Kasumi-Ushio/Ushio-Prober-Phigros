package org.kasumi321.ushio.phitracker.data.parser

import net.lingala.zip4j.ZipFile
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import org.kasumi321.ushio.phitracker.domain.model.GameProgress
import org.kasumi321.ushio.phitracker.domain.model.LevelRecord
import org.kasumi321.ushio.phitracker.domain.model.Save
import org.kasumi321.ushio.phitracker.domain.model.SongRecord
import org.kasumi321.ushio.phitracker.domain.model.Summary
import org.kasumi321.ushio.phitracker.domain.model.UserSettings
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phigros 存档解析器
 *
 * 存档结构: ZIP -> { gameRecord, gameProgress, gameKey, user, settings }
 * 每个文件: byte[0]=version, byte[1..]=AES-256-CBC密文
 *
 * 基于 PhigrosLibrary (GPLv3) 的 save.c 移植
 */
@Singleton
class SaveParser @Inject constructor(
    private val aesDecryptor: AesDecryptor
) {

    /**
     * 从 ZIP 数据解析完整存档
     */
    fun parseSave(saveData: ByteArray, tempDir: File): Save {
        val zipFile = File(tempDir, "save.zip")
        try {
            zipFile.writeBytes(saveData)
            val zip = ZipFile(zipFile)
            val extractDir = File(tempDir, "extracted")
            zip.extractAll(extractDir.absolutePath)

            val gameRecord = parseGameRecord(
                readAndDecrypt(File(extractDir, "gameRecord"))
            )

            val gameProgress = parseGameProgress(
                readAndDecrypt(File(extractDir, "gameProgress"))
            )

            val user = parseUser(
                readAndDecrypt(File(extractDir, "user"))
            )

            return Save(
                gameRecord = gameRecord,
                gameProgress = gameProgress,
                user = user,
                summary = null
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    /**
     * 解析 Summary (Base64 编码, 来自 API)
     */
    fun parseSummary(summaryBase64: String): Summary {
        val data = android.util.Base64.decode(summaryBase64, android.util.Base64.DEFAULT)
        val reader = BinaryReader(data)
        return Summary(
            saveVersion = reader.readByte(),
            challengeModeRank = reader.readShort(),
            rks = reader.readFloat(),
            gameVersion = reader.readVarShort(),
            avatar = reader.readString(),
            progress = List(12) { reader.readShort() }
        )
    }

    private fun readAndDecrypt(file: File): Pair<Int, ByteArray> {
        return aesDecryptor.decrypt(file.readBytes())
    }

    /**
     * 解析 gameRecord
     *
     * 格式 (C 库中 deserializationMap 的 end=2 模式):
     * - varshort: 曲目数量
     * - 每首曲目:
     *   - string: songId (包含 ".0" 后缀, 如 "Glaciaxion.SunsetRay.0")
     *   - byte: 该曲目数据总长度
     *   - byte: 难度 bitmask (bit 0-3 分别表示 EZ/HD/IN/AT 是否有记录)
     *   - byte: FC bitmask (bit 0-3 分别表示 EZ/HD/IN/AT 是否满 combo)
     *   - 对每个存在记录的难度:
     *     - int32: score
     *     - float: accuracy
     */
    private fun parseGameRecord(versionAndData: Pair<Int, ByteArray>): Map<String, SongRecord> {
        val reader = BinaryReader(versionAndData.second)
        val songCount = reader.readVarShort()
        val records = mutableMapOf<String, SongRecord>()

        for (i in 0 until songCount) {
            val songId = reader.readString()
            val dataLength = reader.readByte()
            val startPos = reader.position

            val levelMask = reader.readByte()
            val fcMask = reader.readByte()

            val levels = mutableMapOf<Difficulty, LevelRecord?>()
            for (level in 0 until 4) {
                if (levelMask shr level and 1 == 1) {
                    val score = reader.readInt()
                    val acc = reader.readFloat()
                    val fc = fcMask shr level and 1 == 1
                    levels[Difficulty.fromIndex(level)] = LevelRecord(
                        score = score,
                        accuracy = acc,
                        isFullCombo = fc
                    )
                } else {
                    levels[Difficulty.fromIndex(level)] = null
                }
            }

            // 跳到下一首曲目的起始位置
            val consumed = reader.position - startPos
            if (consumed < dataLength) {
                reader.skip(dataLength - consumed)
            }

            records[songId] = SongRecord(songId = songId, levels = levels)
        }

        return records
    }

    /**
     * 解析 gameProgress
     *
     * 格式:
     * - version 控制需要解析的字段组:
     *   - v1: 基础字段 (isFirstRun .. flagOfSongRecordKey)
     *   - v2+: randomVersionUnlocked
     *   - v3+: chapter8 相关字段
     *
     * 布尔值: bit-packed (多个 bool 共享一个字节, 按位排列)
     */
    private fun parseGameProgress(versionAndData: Pair<Int, ByteArray>): GameProgress {
        val version = versionAndData.first
        val reader = BinaryReader(versionAndData.second)

        // v1 字段 - bool 值是 bit-packed 的
        val boolByte1 = reader.readByte()
        val isFirstRun = boolByte1 and 1 != 0
        val legacyChapterFinished = boolByte1 shr 1 and 1 != 0
        val alreadyShowCollectionTip = boolByte1 shr 2 and 1 != 0
        val alreadyShowAutoUnlockINTip = boolByte1 shr 3 and 1 != 0
        // 4 个 bool 用完了一个字节, 下一个字段是 string, 会推进 ptr

        val completed = reader.readString()
        val songUpdateInfo = reader.readByte()
        val challengeModeRank = reader.readShort()
        val money = List(5) { reader.readVarShort() }
        val unlockFlagOfSpasmodic = reader.readByte()
        val unlockFlagOfIgallta = reader.readByte()
        val unlockFlagOfRrharil = reader.readByte()
        val flagOfSongRecordKey = reader.readByte()

        // v2+ 字段
        var randomVersionUnlocked: Int? = null
        if (version >= 2) {
            randomVersionUnlocked = reader.readByte()
        }

        // v3+ 字段
        var chapter8UnlockBegin: Boolean? = null
        var chapter8UnlockSecondPhase: Boolean? = null
        var chapter8Passed: Boolean? = null
        var chapter8SongUnlocked: Int? = null
        if (version >= 3) {
            val boolByte2 = reader.readByte()
            chapter8UnlockBegin = boolByte2 and 1 != 0
            chapter8UnlockSecondPhase = boolByte2 shr 1 and 1 != 0
            chapter8Passed = boolByte2 shr 2 and 1 != 0
            // 3 个 bool 后面跟的是 u8, 需要新字节
            chapter8SongUnlocked = reader.readByte()
        }

        return GameProgress(
            isFirstRun = isFirstRun,
            legacyChapterFinished = legacyChapterFinished,
            alreadyShowCollectionTip = alreadyShowCollectionTip,
            alreadyShowAutoUnlockINTip = alreadyShowAutoUnlockINTip,
            completed = completed,
            songUpdateInfo = songUpdateInfo,
            challengeModeRank = challengeModeRank,
            money = money,
            unlockFlagOfSpasmodic = unlockFlagOfSpasmodic,
            unlockFlagOfIgallta = unlockFlagOfIgallta,
            unlockFlagOfRrharil = unlockFlagOfRrharil,
            flagOfSongRecordKey = flagOfSongRecordKey,
            randomVersionUnlocked = randomVersionUnlocked,
            chapter8UnlockBegin = chapter8UnlockBegin,
            chapter8UnlockSecondPhase = chapter8UnlockSecondPhase,
            chapter8Passed = chapter8Passed,
            chapter8SongUnlocked = chapter8SongUnlocked
        )
    }

    /**
     * 解析 user 字段
     * 格式: showPlayerId(bool), selfIntro(string), avatar(string), background(string)
     * 第 1 字节为 version, 已在 decrypt 中分离
     */
    private fun parseUser(versionAndData: Pair<Int, ByteArray>): UserSettings {
        val reader = BinaryReader(versionAndData.second)

        val boolByte = reader.readByte()
        val showPlayerId = boolByte and 1 != 0

        val selfIntro = reader.readString()
        val avatar = reader.readString()
        val background = reader.readString()

        return UserSettings(
            showPlayerId = showPlayerId,
            selfIntro = selfIntro,
            avatar = avatar,
            background = background
        )
    }
}
