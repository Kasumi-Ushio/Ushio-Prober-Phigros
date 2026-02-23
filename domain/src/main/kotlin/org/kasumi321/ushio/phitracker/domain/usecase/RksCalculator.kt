package org.kasumi321.ushio.phitracker.domain.usecase

import org.kasumi321.ushio.phitracker.domain.model.BestRecord
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import org.kasumi321.ushio.phitracker.domain.model.LevelRecord
import org.kasumi321.ushio.phitracker.domain.model.SongRecord

/**
 * RKS 计算引擎
 */
object RksCalculator {

    /**
     * 计算单曲 RKS 贡献
     * 公式: ((acc - 55) / 45)^2 * chartConstant
     * acc < 70 时 RKS = 0
     */
    fun calculateSingleRks(accuracy: Float, chartConstant: Float): Float {
        if (accuracy < 70f) return 0f
        val factor = (accuracy - 55f) / 45f
        return factor * factor * chartConstant
    }

    /**
     * 计算显示 RKS (玩家的总 RKS)
     *
     * 公式: (Best φ + Best 19 之和) / 20
     * Best φ = 所有单曲中 RKS 最高的一首
     * Best 19 = RKS 排名 2-20 的曲目
     */
    fun calculateDisplayRks(allBest: List<BestRecord>): Float {
        if (allBest.isEmpty()) return 0f
        val sorted = allBest.sortedByDescending { it.rks }
        val phi = sorted.first().rks
        val best19Sum = sorted.drop(1).take(19).sumOf { it.rks.toDouble() }
        return ((phi + best19Sum) / 20.0).toFloat()
    }

    /**
     * 从成绩记录中提取 B30
     *
     * @param records 所有曲目的成绩
     * @param difficulties 定数表: songId -> { difficulty -> chartConstant }
     * @param songNames 歌名表: songId -> songName
     */
    fun getB30(
        records: Map<String, SongRecord>,
        difficulties: Map<String, Map<Difficulty, Float>>,
        songNames: Map<String, String>
    ): List<BestRecord> {
        val allRecords = mutableListOf<BestRecord>()

        for ((songId, songRecord) in records) {
            val songDiffs = difficulties[songId] ?: continue
            val songName = songNames[songId] ?: songId

            for ((difficulty, levelRecord) in songRecord.levels) {
                if (levelRecord == null) continue
                val cc = songDiffs[difficulty] ?: continue

                val rks = calculateSingleRks(levelRecord.accuracy, cc)
                allRecords.add(
                    BestRecord(
                        songId = songId,
                        songName = songName,
                        difficulty = difficulty,
                        score = levelRecord.score,
                        accuracy = levelRecord.accuracy,
                        isFullCombo = levelRecord.isFullCombo,
                        chartConstant = cc,
                        rks = rks
                    )
                )
            }
        }

        return allRecords.sortedByDescending { it.rks }.take(30)
    }

    /**
     * 计算推分建议
     * 给定当前 RKS 和一首曲目的定数, 返回需要多少 ACC 才能替换掉 B19 末位使 RKS 提升
     *
     * @param currentB19LastRks 当前 B19 末位 (第20名) 的 RKS
     * @param chartConstant 目标曲目定数
     * @return 需要的 ACC, null 如果不可行 (> 100% 或定数太低)
     */
    fun calculateTargetAcc(currentB19LastRks: Float, chartConstant: Float): Float? {
        if (chartConstant <= 0f) return null
        // 需要的 RKS = currentB19LastRks (至少超过末位)
        // rks = ((acc - 55) / 45)^2 * cc
        // acc = sqrt(rks / cc) * 45 + 55
        val neededRks = currentB19LastRks
        val sqrtPart = Math.sqrt((neededRks / chartConstant).toDouble()).toFloat()
        val neededAcc = sqrtPart * 45f + 55f
        return if (neededAcc in 70f..100f) neededAcc else null
    }
}
