package org.kasumi321.ushio.phitracker.ui.b30

import android.graphics.Bitmap
import org.kasumi321.ushio.phitracker.domain.model.BestRecord

data class B30StatsTable(
    val clearCounts: Map<String, Int>,
    val fcCount: Int,
    val phiCount: Int
)

data class ExportCardData(
    val record: BestRecord,
    val illustrationBitmap: Bitmap?
)

data class B30ExportData(
    val nickname: String,
    val rks: Float,
    val challengeLevel: Int,
    val moneyString: String,
    val dateText: String,
    val avatarBitmap: Bitmap?,
    val statsTable: B30StatsTable,
    val phiRecords: List<ExportCardData>,
    val bestRecords: List<ExportCardData>,
    val overflowRecords: List<ExportCardData>,
    val backgroundBitmap: Bitmap?,
    val profileCardWidthDp: Float,
    val profileCardHeightDp: Float,
    val statsCardWidthDp: Float,
    val cardWidthDp: Float,
    val cardHeightDp: Float,
    val cardHorizontalGapDp: Float,
    val cardVerticalGapDp: Float
)
