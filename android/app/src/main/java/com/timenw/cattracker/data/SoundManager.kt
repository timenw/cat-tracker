package com.timenw.cattracker.data

import android.content.Context
import android.media.MediaPlayer
import com.timenw.cattracker.R
import com.timenw.cattracker.data.model.CatAction

/**
 * 音效管理器 - 播放猫叫声音效
 */
class SoundManager(private val context: Context) {

    private var soundEnabled = true
    private var currentPlayer: MediaPlayer? = null

    fun setSoundEnabled(enabled: Boolean) { soundEnabled = enabled }

    fun playActionSound(action: CatAction) {
        if (!soundEnabled) return
        val resId = when (action) {
            CatAction.PET_HEAD -> R.raw.meow_pet
            CatAction.SCRATCH_CHIN -> R.raw.meow_scratch
            CatAction.RUB_BELLY -> R.raw.meow_belly
            CatAction.FEED_FOOD -> R.raw.meow_feed
            CatAction.FEED_SNACK -> R.raw.meow_snack
            CatAction.FEED_CAN -> R.raw.meow_can
            CatAction.PLAY_CAT_TEASE -> R.raw.meow_play
            CatAction.PLAY_BALL -> R.raw.meow_ball
            CatAction.PLAY_LASER -> R.raw.meow_laser
            CatAction.CLEAN_BATH -> R.raw.meow_bath
            CatAction.CLEAN_BRUSH -> R.raw.meow_brush
            CatAction.SLEEP -> R.raw.meow_sleep
        }
        playSound(resId)
    }

    private fun playSound(resId: Int) {
        try {
            currentPlayer?.let {
                try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {}
            }
            currentPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { mp -> try { mp.release() } catch (_: Exception) {} }
                setOnErrorListener { mp, _, _ -> try { mp.release() } catch (_: Exception) {}; true }
                start()
            }
        } catch (_: Exception) {}
    }

    fun playAchievementSound() {
        if (soundEnabled) playSound(R.raw.sound_achievement)
    }

    fun playPurchaseSound() {
        if (soundEnabled) playSound(R.raw.sound_purchase)
    }

    fun release() {
        currentPlayer?.let { try { if (it.isPlaying) it.stop(); it.release() } catch (_: Exception) {} }
        currentPlayer = null
    }
}
