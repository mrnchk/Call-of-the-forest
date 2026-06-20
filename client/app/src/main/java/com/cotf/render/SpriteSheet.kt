package com.cotf.render

import android.graphics.Rect as AndroidRect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas

// ────────────────────────────────────────────────────────────────────────────
// AnimClip — one animation inside a sprite sheet (row + frame count)
// ────────────────────────────────────────────────────────────────────────────

/**
 * Описание одной анимации внутри спрайт-листа.
 *
 * @param row        строка (0-based) в спрайт-листе
 * @param frameCount кадров в этой строке
 * @param fps        скорость воспроизведения
 */
data class AnimClip(
    val row: Int,
    val frameCount: Int,
    val fps: Float = 8f
)

/** Текущий кадр клипа по игровому времени (секунды). */
fun AnimClip.frameAt(timeSeconds: Float): Int =
    ((timeSeconds * fps).toInt() % frameCount)

// ────────────────────────────────────────────────────────────────────────────
// CharSprites — character sprite sheet with named animations
// ────────────────────────────────────────────────────────────────────────────

/**
 * Спрайт-лист одного персонажа.
 * Все строки имеют одинаковую высоту [frameHeight].
 * Все кадры имеют одинаковую ширину [frameWidth].
 * У разных строк может быть разное кол-во кадров — задаётся в [clips].
 *
 * @param bitmap      загруженная ImageBitmap
 * @param frameWidth  ширина одного кадра в пикселях
 * @param frameHeight высота одной строки в пикселях
 * @param clips       именованные анимации: "run", "idle", "attack", "howl", ...
 */
data class CharSprites(
    val bitmap: ImageBitmap,
    val frameWidth: Int,
    val frameHeight: Int,
    val clips: Map<String, AnimClip>
) {
    /** Возвращает клип по имени, или первый доступный если не найден. */
    fun clip(name: String): AnimClip =
        clips[name] ?: clips.values.first()
}

// ────────────────────────────────────────────────────────────────────────────
// drawCharSprite — draws a specific frame from an animation clip
// ────────────────────────────────────────────────────────────────────────────

/**
 * Рисует кадр анимации по центру (cx, cy).
 *
 * @param char    спрайт-лист персонажа
 * @param clip    текущий клип (анимация)
 * @param frame   номер кадра (0..clip.frameCount-1)
 * @param cx      центр X на экране
 * @param cy      центр Y на экране
 * @param scale   масштаб (2f = вдвое крупнее, рекомендуется для пиксель-арта)
 * @param flipX   зеркалить по X (для движения влево, если спрайт смотрит вправо)
 */
fun DrawScope.drawCharSprite(
    char: CharSprites,
    clip: AnimClip,
    frame: Int,
    cx: Float,
    cy: Float,
    scale: Float = 2f,
    flipX: Boolean = false
) {
    val fw = char.frameWidth
    val fh = char.frameHeight
    val dstW = (fw * scale).toInt()
    val dstH = (fh * scale).toInt()

    val src = AndroidRect(
        frame * fw,
        clip.row * fh,
        frame * fw + fw,
        clip.row * fh + fh
    )
    val dst = AndroidRect(
        (cx - dstW / 2f).toInt(),
        (cy - dstH / 2f).toInt(),
        (cx + dstW / 2f).toInt(),
        (cy + dstH / 2f).toInt()
    )

    val canvas = drawContext.canvas.nativeCanvas
    val bmp    = char.bitmap.asAndroidBitmap()

    if (flipX) {
        canvas.save()
        canvas.scale(-1f, 1f, cx, cy)
        canvas.drawBitmap(bmp, src, dst, null)
        canvas.restore()
    } else {
        canvas.drawBitmap(bmp, src, dst, null)
    }
}
