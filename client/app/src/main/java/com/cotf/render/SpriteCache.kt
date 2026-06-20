package com.cotf.render

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

// ────────────────────────────────────────────────────────────────────────────
// CharSpritesConfig — descriptor for loading a sprite sheet
// ────────────────────────────────────────────────────────────────────────────

/**
 * Конфигурация спрайт-листа персонажа.
 *
 * @param drawableResId  R.drawable.XXX
 * @param frameWidth     ширина одного кадра в пикселях
 * @param frameHeight    высота одной строки в пикселях
 * @param clips          именованные анимации: имя → AnimClip(строка, кол-во кадров, fps)
 *
 * Пример (волк, 4 анимации):
 * CharSpritesConfig(
 *     drawableResId = R.drawable.wolf,
 *     frameWidth    = 48,
 *     frameHeight   = 48,
 *     clips = mapOf(
 *         "idle"   to AnimClip(row = 0, frameCount = 3, fps = 4f),
 *         "run"    to AnimClip(row = 1, frameCount = 6, fps = 10f),
 *         "attack" to AnimClip(row = 2, frameCount = 4, fps = 12f),
 *         "howl"   to AnimClip(row = 3, frameCount = 5, fps = 8f),
 *     )
 * )
 */
data class CharSpritesConfig(
    val drawableResId: Int,
    val frameWidth: Int,
    val frameHeight: Int,
    val clips: Map<String, AnimClip>
)

// ────────────────────────────────────────────────────────────────────────────
// CharSpriteCache — cache of loaded sprite sheets
// ────────────────────────────────────────────────────────────────────────────

class CharSpriteCache(
    private val chars: Map<String, CharSprites>
) {
    /** Возвращает спрайты персонажа по ключу ("player", "wolf", "deer", ...) */
    operator fun get(key: String): CharSprites? = chars[key]
}

@Composable
fun rememberCharSpriteCache(
    configs: Map<String, CharSpritesConfig>
): CharSpriteCache {
    val context = LocalContext.current
    return remember(configs) { buildCharSpriteCache(context, configs) }
}

private fun buildCharSpriteCache(
    context: Context,
    configs: Map<String, CharSpritesConfig>
): CharSpriteCache {
    val chars = configs.mapValues { (_, cfg) ->
        val drawable = context.getDrawable(cfg.drawableResId)
            ?: error("Drawable not found: ${cfg.drawableResId}")
        val bitmap = drawable.toBitmap().asImageBitmap()
        CharSprites(
            bitmap      = bitmap,
            frameWidth  = cfg.frameWidth,
            frameHeight = cfg.frameHeight,
            clips       = cfg.clips
        )
    }
    return CharSpriteCache(chars)
}
