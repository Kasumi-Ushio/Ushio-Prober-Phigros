package org.kasumi321.ushio.phitracker.domain.model

import kotlinx.serialization.Serializable

/**
 * 曲目信息
 */
@Serializable
data class SongInfo(
    val id: String,
    val name: String,
    val composer: String,
    val illustrator: String,
    val difficulties: Map<Difficulty, Float>
)
