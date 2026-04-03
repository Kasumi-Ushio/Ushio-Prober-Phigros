package org.kasumi321.ushio.phitracker.ui.b30

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import org.kasumi321.ushio.phitracker.domain.model.BestRecord
import org.kasumi321.ushio.phitracker.domain.model.Difficulty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object B30ImageGenerator {

    private const val IMAGE_WIDTH = 1080
    private const val PADDING = 20
    private const val HEADER_HEIGHT = 118
    private const val FOOTER_HEIGHT = 56
    private const val GAP = 8
    private const val COLUMNS = 3
    private const val ROWS = 10
    private const val DIVIDER_GAP = 0
    private val SLOT_WIDTH = (IMAGE_WIDTH - PADDING * 2 - GAP * (COLUMNS - 1)) / COLUMNS
    private const val CARD_WIDTH_RATIO = 0.96f
    private val CARD_WIDTH = SLOT_WIDTH * CARD_WIDTH_RATIO
    private val CARD_INSET = (SLOT_WIDTH - CARD_WIDTH) / 2f
    private const val CARD_HEIGHT = 106
    private val IMAGE_HEIGHT = PADDING + HEADER_HEIGHT + ROWS * CARD_HEIGHT + (ROWS - 1) * GAP + DIVIDER_GAP + FOOTER_HEIGHT + PADDING

    suspend fun generate(
        context: Context,
        imageLoader: ImageLoader,
        b30: List<BestRecord>,
        displayRks: Float,
        nickname: String,
        challengeModeRank: Int,
        moneyString: String,
        clearCounts: Map<String, Int>,
        fcCount: Int,
        phiCount: Int,
        avatarUri: String?,
        selectedBackgroundSongId: String?,
        customBackgroundUri: String?,
        lowIllustrationUrlProvider: (String) -> String,
        standardIllustrationUrlProvider: (String) -> String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val phiTop3 = b30.filter { it.isPhi }.sortedByDescending { it.rks }.take(3)
        val best27 = b30.filterNot { it.isPhi }.sortedByDescending { it.rks }.take(27)
        val cards = (phiTop3 + best27).take(30)

        val backgroundBitmap = loadBackgroundBitmap(
            context = context,
            imageLoader = imageLoader,
            customBackgroundUri = customBackgroundUri,
            selectedBackgroundSongId = selectedBackgroundSongId,
            fallbackSongId = cards.firstOrNull()?.songId,
            standardIllustrationUrlProvider = standardIllustrationUrlProvider
        )
        drawBackground(canvas, backgroundBitmap)

        val avatarBitmap = loadBitmap(context, imageLoader, avatarUri, 220, 220)
        val coverMap = cards.associate { record ->
            record.songId to loadBitmap(context, imageLoader, lowIllustrationUrlProvider(record.songId), 256, 256)
        }

        drawHeader(
            canvas = canvas,
            displayRks = displayRks,
            nickname = nickname,
            challengeModeRank = challengeModeRank,
            moneyString = moneyString,
            clearCounts = clearCounts,
            fcCount = fcCount,
            phiCount = phiCount,
            avatarBitmap = avatarBitmap
        )

        for (i in cards.indices) {
            val col = i % COLUMNS
            val row = i / COLUMNS
            val x = PADDING + col * (SLOT_WIDTH + GAP) + CARD_INSET
            val extra = if (row >= 1) DIVIDER_GAP else 0
            val y = PADDING + HEADER_HEIGHT + row * (CARD_HEIGHT + GAP) + extra
            val rankLabel = if (i < phiTop3.size) {
                "P${i + 1}"
            } else {
                "#${(i - phiTop3.size) + 1}"
            }
            drawCard(canvas, cards[i], rankLabel, x.toFloat(), y.toFloat(), coverMap[cards[i].songId])
        }

        drawFooter(canvas)
        return bitmap
    }

    private fun drawBackground(canvas: Canvas, backgroundBitmap: Bitmap?) {
        if (backgroundBitmap != null) {
            val cropped = centerCropBitmap(backgroundBitmap, IMAGE_WIDTH, IMAGE_HEIGHT)
            val blurred = createBlurredLikeBitmap(cropped, IMAGE_WIDTH, IMAGE_HEIGHT)
            canvas.drawBitmap(blurred, null, Rect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT), null)
            if (cropped != backgroundBitmap && !cropped.isRecycled) cropped.recycle()
            if (blurred != backgroundBitmap && !blurred.isRecycled) blurred.recycle()
        } else {
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, IMAGE_HEIGHT.toFloat(),
                    intArrayOf(Color.parseColor("#1a1a2e"), Color.parseColor("#16213e"), Color.parseColor("#0f3460")),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), bgPaint)
        }
        val overlay = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(145, 8, 12, 20) }
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), overlay)
    }

    private fun drawHeader(
        canvas: Canvas,
        displayRks: Float,
        nickname: String,
        challengeModeRank: Int,
        moneyString: String,
        clearCounts: Map<String, Int>,
        fcCount: Int,
        phiCount: Int,
        avatarBitmap: Bitmap?
    ) {
        val top = PADDING.toFloat()
        val bottom = (PADDING + HEADER_HEIGHT - 12).toFloat()
        val left = PADDING.toFloat()
        val right = (IMAGE_WIDTH - PADDING).toFloat()
        val spacing = 10f
        val statCardWidth = 300f
        val profileRect = RectF(left, top, right - statCardWidth - spacing, bottom)
        val statsRect = RectF(profileRect.right + spacing, top, right, bottom)

        val profileBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 24, 34, 48) }
        val statsBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 24, 34, 48) }
        canvas.drawRoundRect(profileRect, 18f, 18f, profileBg)
        canvas.drawRoundRect(statsRect, 18f, 18f, statsBg)

        val avatarSize = 78f
        val avatarRect = RectF(
            profileRect.left + 20f,
            profileRect.top + (profileRect.height() - avatarSize) / 2f,
            profileRect.left + 20f + avatarSize,
            profileRect.top + (profileRect.height() - avatarSize) / 2f + avatarSize
        )
        val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#455A64") }
        canvas.drawOval(avatarRect, avatarPaint)
        if (avatarBitmap != null) {
            canvas.save()
            val ovalPath = android.graphics.Path().apply { addOval(avatarRect, android.graphics.Path.Direction.CW) }
            canvas.clipPath(ovalPath)
            val src = centerCropRect(avatarBitmap.width, avatarBitmap.height, avatarRect.width(), avatarRect.height())
            canvas.drawBitmap(avatarBitmap, src, avatarRect, null)
            canvas.restore()
        }

        val textStart = avatarRect.right + 16f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.DEFAULT
        }
        val dataPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0BEC5")
            textSize = 16f
        }
        val rksPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64B5F6")
            textSize = 24f
            typeface = Typeface.DEFAULT
        }
        val infoTop = avatarRect.top + 20f
        val infoBottom = avatarRect.bottom - 4f
        val infoGap = (infoBottom - infoTop) / 2f - 2f
        val nameY = infoTop
        val dataY = infoTop + infoGap
        val rksY = infoBottom
        canvas.drawText(nickname.ifBlank { "未登录" }, textStart, nameY, titlePaint)
        if (moneyString.isNotBlank()) {
            canvas.drawText("Data: $moneyString", textStart, dataY, dataPaint)
        }
        val rksText = String.format("%.4f", displayRks)
        canvas.drawText(rksText, textStart, rksY, rksPaint)
        drawChallengeBadge(canvas, challengeModeRank, textStart + rksPaint.measureText(rksText) + 14f, rksY)

        val rowTop = statsRect.top + 20f
        val rowBottom = statsRect.bottom - 18f
        val rowGap = (rowBottom - rowTop) / 2f
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15f
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val colGap = 14f
        val innerPadding = 12f
        val colWidth = (statsRect.width() - innerPadding * 2f - colGap) / 2f
        val leftColX = statsRect.left + innerPadding
        val rightColX = leftColX + colWidth + colGap
        val leftColRight = leftColX + colWidth
        val rightColRight = rightColX + colWidth
        drawStatLine(canvas, "EZ", clearCounts["EZ"] ?: 0, Color.parseColor("#70D866"), rowTop + rowGap * 0, leftColX, leftColRight, labelPaint, valuePaint)
        drawStatLine(canvas, "HD", clearCounts["HD"] ?: 0, Color.parseColor("#58B4E3"), rowTop + rowGap * 1, leftColX, leftColRight, labelPaint, valuePaint)
        drawStatLine(canvas, "IN", clearCounts["IN"] ?: 0, Color.parseColor("#E34D4D"), rowTop + rowGap * 2, leftColX, leftColRight, labelPaint, valuePaint)
        drawStatLine(canvas, "AT", clearCounts["AT"] ?: 0, Color.parseColor("#A855F7"), rowTop + rowGap * 0, rightColX, rightColRight, labelPaint, valuePaint)
        drawStatLine(canvas, "FC", fcCount, Color.parseColor("#4FC3F7"), rowTop + rowGap * 1, rightColX, rightColRight, labelPaint, valuePaint)
        drawStatLine(canvas, "φ", phiCount, Color.parseColor("#FFD54F"), rowTop + rowGap * 2, rightColX, rightColRight, labelPaint, valuePaint)
    }

    private fun drawCard(canvas: Canvas, record: BestRecord, rankLabel: String, x: Float, y: Float, cover: Bitmap?) {
        val diffColor = getDiffColor(record.difficulty)
        val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(175, 27, 39, 54) }
        val rect = RectF(x, y, x + CARD_WIDTH.toFloat(), y + CARD_HEIGHT)
        canvas.drawRoundRect(rect, 12f, 12f, cardBg)

        val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = diffColor }
        canvas.drawRoundRect(RectF(x, y, x + 6, y + CARD_HEIGHT), 3f, 3f, stripPaint)

        val thumbPadding = 8f
        val thumbSize = CARD_HEIGHT - thumbPadding * 2
        val thumbRect = RectF(x + 8f, y + thumbPadding, x + 8f + thumbSize, y + thumbPadding + thumbSize)
        val thumbBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#37474F") }
        canvas.drawRoundRect(thumbRect, 8f, 8f, thumbBg)
        if (cover != null) {
            canvas.save()
            canvas.clipRect(thumbRect)
            val src = centerCropRect(cover.width, cover.height, thumbRect.width(), thumbRect.height())
            canvas.drawBitmap(cover, src, thumbRect, null)
            canvas.restore()
        }

        val contentStart = thumbRect.right + 12f
        val contentRight = x + CARD_WIDTH.toFloat() - 12f
        val row1Y = y + 24f
        val row2Y = y + 46f
        val row3Y = y + 70f
        val row4Y = y + 92f

        val rankPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = when (rankLabel) {
                "P1" -> Color.parseColor("#FFD700")
                "P2" -> Color.parseColor("#C0C0C0")
                "P3" -> Color.parseColor("#CD7F32")
                "#1" -> Color.parseColor("#FFD700")
                "#2" -> Color.parseColor("#C0C0C0")
                "#3" -> Color.parseColor("#CD7F32")
                else -> Color.parseColor("#78909C")
            }
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(rankLabel, contentStart, row1Y, rankPaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val rankWidth = rankPaint.measureText(rankLabel)
        val line1Start = contentStart + rankWidth + 10f
        val songName = truncateText(record.songName, namePaint, contentRight - line1Start)
        canvas.drawText(songName, line1Start, row1Y, namePaint)

        val diffLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = diffColor
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val diffLabel = "${getDiffLabel(record.difficulty)} ${String.format("%.1f", record.chartConstant)}"
        canvas.drawText(diffLabel, contentStart, row2Y, diffLabelPaint)

        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val scoreText = String.format("%,d", record.score)
        canvas.drawText(scoreText, contentStart, row3Y, scorePaint)

        val badgeText = when {
            record.accuracy >= 100f -> "φ"
            record.isFullCombo -> "FC"
            else -> null
        }
        badgeText?.let { badge ->
            val badgeBgColor = if (badge == "φ") Color.parseColor("#FFD54F") else Color.parseColor("#4FC3F7")
            val badgeTextColor = if (badge == "φ") Color.parseColor("#5D4037") else Color.WHITE
            val badgeLeft = contentStart + scorePaint.measureText(scoreText) + 8f
            val badgeRect = RectF(badgeLeft, row3Y - 14f, badgeLeft + if (badge == "φ") 20f else 30f, row3Y + 4f)
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = badgeBgColor }
            canvas.drawRoundRect(badgeRect, 4f, 4f, badgePaint)
            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = badgeTextColor
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(badge, badgeRect.centerX(), row3Y - 2f, badgeTextPaint)
        }

        val accPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0BEC5")
            textSize = 14f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("ACC ${String.format("%.4f%%", record.accuracy)}", contentStart, row4Y, accPaint)

        val rksPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64B5F6")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(String.format("%.4f", record.rks), contentRight, row4Y, rksPaint)
    }

    private fun drawStatLine(
        canvas: Canvas,
        label: String,
        value: Int,
        labelColor: Int,
        y: Float,
        left: Float,
        right: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        labelPaint.color = labelColor
        canvas.drawText(label, left, y, labelPaint)
        canvas.drawText(value.toString(), right, y, valuePaint)
    }

    private fun drawChallengeBadge(canvas: Canvas, challengeModeRank: Int, x: Float, baselineY: Float) {
        if (challengeModeRank <= 0) return
        val tier = challengeModeRank / 100
        val level = challengeModeRank % 100
        val bgColor = when (tier) {
            0 -> Color.parseColor("#CCCCCC")
            1 -> Color.parseColor("#4CAF50")
            2 -> Color.parseColor("#2196F3")
            3 -> Color.parseColor("#F44336")
            4 -> Color.parseColor("#FFC107")
            else -> Color.parseColor("#9C27B0")
        }
        val textColor = if (tier == 0) Color.parseColor("#333333") else Color.WHITE
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 18f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valueText = level.toString()
        val badgeWidth = textPaint.measureText(valueText) + 12f
        val badgeHeight = 24f
        val rect = RectF(x, baselineY - badgeHeight + 4f, x + badgeWidth, baselineY + 4f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        canvas.drawText(valueText, rect.centerX(), baselineY, textPaint)
    }

    private fun drawDivider(canvas: Canvas) {
        val y = (PADDING + HEADER_HEIGHT + CARD_HEIGHT + GAP + DIVIDER_GAP / 2).toFloat()
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CFD8DC")
            strokeWidth = 2f
        }
        canvas.drawLine(PADDING.toFloat(), y, (IMAGE_WIDTH - PADDING).toFloat(), y, linePaint)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#ECEFF1")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Phi 3 / Best 27", IMAGE_WIDTH / 2f, y - 6f, labelPaint)
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#546E7A")
            textSize = 20f
        }
        val y = (IMAGE_HEIGHT - PADDING).toFloat()
        footerPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Generated by Phi Tracker", PADDING.toFloat(), y, footerPaint)
        footerPaint.textAlign = Paint.Align.RIGHT
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
        canvas.drawText(time, (IMAGE_WIDTH - PADDING).toFloat(), y, footerPaint)
    }

    private suspend fun loadBackgroundBitmap(
        context: Context,
        imageLoader: ImageLoader,
        customBackgroundUri: String?,
        selectedBackgroundSongId: String?,
        fallbackSongId: String?,
        standardIllustrationUrlProvider: (String) -> String
    ): Bitmap? {
        val source = when {
            !customBackgroundUri.isNullOrBlank() -> customBackgroundUri
            !selectedBackgroundSongId.isNullOrBlank() -> standardIllustrationUrlProvider(selectedBackgroundSongId)
            !fallbackSongId.isNullOrBlank() -> standardIllustrationUrlProvider(fallbackSongId)
            else -> null
        }
        return loadBitmap(context, imageLoader, source, IMAGE_WIDTH, IMAGE_HEIGHT, preferOriginal = true)
    }

    private suspend fun loadBitmap(
        context: Context,
        imageLoader: ImageLoader,
        data: String?,
        targetW: Int,
        targetH: Int,
        preferOriginal: Boolean = false
    ): Bitmap? {
        if (data.isNullOrBlank()) return null
        val builder = ImageRequest.Builder(context)
            .data(data)
            .allowHardware(false)
            .allowRgb565(false)
        if (!preferOriginal) {
            builder.size(targetW, targetH)
        } else {
            builder.size(Size.ORIGINAL)
        }
        val request = builder.build()
        val result = runCatching { imageLoader.execute(request) }.getOrNull() as? SuccessResult ?: return null
        return drawableToBitmapWithoutStretch(result.drawable, targetW, targetH)
    }

    private fun centerCropRect(srcW: Int, srcH: Int, dstW: Float, dstH: Float): Rect {
        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = dstW / dstH
        return if (srcRatio > dstRatio) {
            val cropW = (srcH * dstRatio).toInt()
            val left = (srcW - cropW) / 2
            Rect(left, 0, left + cropW, srcH)
        } else {
            val cropH = (srcW / dstRatio).toInt()
            val top = (srcH - cropH) / 2
            Rect(0, top, srcW, top + cropH)
        }
    }

    private fun createBlurredLikeBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        val downscaled = Bitmap.createScaledBitmap(source, maxOf(640, width * 3 / 5), maxOf(640, height * 3 / 5), true)
        val upscaled = Bitmap.createScaledBitmap(downscaled, width, height, true)
        if (!downscaled.isRecycled && downscaled != source) downscaled.recycle()
        return upscaled
    }

    private fun centerCropBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        val src = centerCropRect(source.width, source.height, width.toFloat(), height.toFloat())
        return Bitmap.createBitmap(source, src.left, src.top, src.width(), src.height())
    }

    private fun drawableToBitmapWithoutStretch(drawable: Drawable, fallbackW: Int, fallbackH: Int): Bitmap? {
        val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: fallbackW
        val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: fallbackH
        return runCatching { drawable.toBitmap(w, h) }.getOrNull()
    }

    private fun getDiffColor(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EZ -> Color.parseColor("#70D866")
        Difficulty.HD -> Color.parseColor("#58B4E3")
        Difficulty.IN -> Color.parseColor("#E34D4D")
        Difficulty.AT -> Color.parseColor("#A855F7")
    }

    private fun getDiffLabel(difficulty: Difficulty): String = when (difficulty) {
        Difficulty.EZ -> "EZ"
        Difficulty.HD -> "HD"
        Difficulty.IN -> "IN"
        Difficulty.AT -> "AT"
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated…") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated…"
    }
}
