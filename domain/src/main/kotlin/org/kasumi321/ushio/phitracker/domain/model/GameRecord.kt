package org.kasumi321.ushio.phitracker.domain.model

import kotlinx.serialization.Serializable

enum class Difficulty {
    EZ, HD, IN, AT;

    companion object {
        fun fromIndex(index: Int): Difficulty = entries[index]
    }
}

@Serializable
data class LevelRecord(
    val score: Int,
    val accuracy: Float,
    val isFullCombo: Boolean
) {
    /**
     * 计算单曲 RKS 贡献
     * 公式: ((acc - 55) / 45)^2 * chartConstant
     * acc < 70 时 RKS = 0
     */
    fun calculateRks(chartConstant: Float): Float {
        if (accuracy < 70f) return 0f
        val factor = (accuracy - 55f) / 45f
        return factor * factor * chartConstant
    }
}

@Serializable
data class SongRecord(
    val songId: String,
    val levels: Map<Difficulty, LevelRecord?>
)

@Serializable
data class BestRecord(
    val songId: String,
    val songName: String,
    val difficulty: Difficulty,
    val score: Int,
    val accuracy: Float,
    val isFullCombo: Boolean,
    val chartConstant: Float,
    val rks: Float
)
