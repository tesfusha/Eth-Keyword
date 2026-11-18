package com.example.engine

import com.example.data.KeyboardLanguage
import kotlin.math.abs

data class InkPoint(val x: Float, val y: Float)

data class InkStroke(val points: List<InkPoint>)

interface DigitalInkRecognizer {
    fun recognize(strokes: List<InkStroke>, language: KeyboardLanguage): List<String>
}

/**
 * 100% offline geometric stroke analyzer implementing the DigitalInkRecognizer interface.
 * Analyzes stroke counts, directional vectors, aspect ratios, and closed loops to propose
 * matching Ethiopic Fidel or Latin/Qubee characters immediately without network downloads.
 */
class OfflineStrokeRecognizer : DigitalInkRecognizer {

    override fun recognize(strokes: List<InkStroke>, language: KeyboardLanguage): List<String> {
        val valid = strokes.filter { it.points.size >= 2 }
        if (valid.isEmpty()) return emptyList()

        val allPoints = valid.flatMap { it.points }
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }
        val width = (maxX - minX).coerceAtLeast(1f)
        val height = (maxY - minY).coerceAtLeast(1f)
        val aspect = width / height

        val firstStroke = valid.first()
        val start = firstStroke.points.first()
        val end = firstStroke.points.last()
        val normStartX = (start.x - minX) / width
        val normStartY = (start.y - minY) / height
        val normEndX = (end.x - minX) / width
        val normEndY = (end.y - minY) / height
        val closedDistance = kotlin.math.hypot((end.x - start.x).toDouble(), (end.y - start.y).toDouble())
        val isClosed = closedDistance < maxOf(width, height) * 0.35

        if (language.isEthiopic) {
            return recognizeEthiopic(
                strokeCount = valid.size,
                aspect = aspect,
                isClosed = isClosed,
                normStartX = normStartX,
                normStartY = normStartY,
                normEndX = normEndX,
                normEndY = normEndY,
                isTigrigna = language == KeyboardLanguage.TIGRIGNA,
            )
        }

        return recognizeLatin(
            strokeCount = valid.size,
            aspect = aspect,
            isClosed = isClosed,
            normStartX = normStartX,
            normStartY = normStartY,
            normEndX = normEndX,
            normEndY = normEndY,
            includeHudhaa = language == KeyboardLanguage.AFAAN_OROMO,
        )
    }

    private fun recognizeEthiopic(
        strokeCount: Int,
        aspect: Float,
        isClosed: Boolean,
        normStartX: Float,
        normStartY: Float,
        normEndX: Float,
        normEndY: Float,
        isTigrigna: Boolean,
    ): List<String> {
        val candidates = linkedSetOf<String>()
        when {
            strokeCount == 1 && isClosed -> {
                candidates.addAll(listOf("ሀ", "ዐ", "መ", "በ", "ፀ"))
            }
            strokeCount == 1 && normStartY < 0.35f && normEndY > 0.65f && normEndX > normStartX -> {
                candidates.addAll(listOf("ለ", "ገ", "ሰ", "ረ", "ደ"))
            }
            strokeCount == 1 && aspect > 1.2f -> {
                candidates.addAll(listOf("ረ", "ገ", "ወ", "ሀ", "።"))
            }
            strokeCount == 1 -> {
                candidates.addAll(listOf("ሀ", "ለ", "ሰ", "ገ", "በ", "ነ"))
            }
            strokeCount == 2 -> {
                if (isClosed) {
                    candidates.addAll(listOf("ቀ", "መ", "በ", "ፈ", "ተ"))
                } else if (abs(normEndY - normStartY) < 0.3f) {
                    candidates.addAll(listOf("ተ", "ነ", "ከ", "የ", "ደ"))
                } else {
                    candidates.addAll(listOf("አ", "ሰ", "ሸ", "ቸ", "ጠ"))
                }
                if (isTigrigna) candidates.add("ቐ")
            }
            strokeCount == 3 -> {
                candidates.addAll(listOf("ሸ", "መ", "ጠ", "ጨ", "ፈ", "ጸ"))
            }
            else -> {
                candidates.addAll(listOf("ጨ", "ጰ", "ፀ", "ዠ", "ጀ", "ኘ"))
            }
        }
        return candidates.toList().take(6)
    }

    private fun recognizeLatin(
        strokeCount: Int,
        aspect: Float,
        isClosed: Boolean,
        normStartX: Float,
        normStartY: Float,
        normEndX: Float,
        normEndY: Float,
        includeHudhaa: Boolean,
    ): List<String> {
        val candidates = linkedSetOf<String>()
        when {
            strokeCount == 1 && isClosed -> {
                candidates.addAll(listOf("o", "a", "e", "d", "q", "g"))
            }
            strokeCount == 1 && aspect < 0.45f -> {
                if (includeHudhaa) candidates.add("'")
                candidates.addAll(listOf("l", "i", "r", "j", "t"))
            }
            strokeCount == 1 && normStartY < 0.3f && normEndY > 0.6f -> {
                candidates.addAll(listOf("s", "c", "u", "n", "m", "h"))
            }
            strokeCount == 1 -> {
                candidates.addAll(listOf("e", "a", "r", "w", "v", "z"))
            }
            strokeCount == 2 -> {
                candidates.addAll(listOf("t", "x", "k", "y", "i", "d"))
            }
            else -> {
                candidates.addAll(listOf("m", "w", "k", "f", "h", "b"))
            }
        }
        return candidates.toList().take(6)
    }
}
