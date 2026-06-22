package com.timenw.cattracker.data

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.timenw.cattracker.R
import com.timenw.cattracker.data.model.CatAction

/**
 * 音效 + 震动管理器
 * 使用 MediaPlayer 播放 raw 资源中的猫叫声音效
 * 使用 Vibrator 提供震动反馈
 */
class SoundManager(private val context: Context) {

    private var vibrator: Vibrator? = null
    private var soundEnabled = true
    private var vibrationEnabled = true
    private var currentPlayer: MediaPlayer? = null

    init {
        initVibrator()
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setSoundEnabled(enabled: Boolean) { soundEnabled = enabled }
    fun setVibrationEnabled(enabled: Boolean) { vibrationEnabled = enabled }

    /**
     * 播放互动音效 + 震动
     */
    fun playActionSound(action: CatAction) {
        if (soundEnabled) {
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
        if (vibrationEnabled) vibrateForAction(action)
    }

    private fun playSound(resId: Int) {
        try {
            // 停止当前播放
            currentPlayer?.let {
                try {
                    if (it.isPlaying) it.stop()
                    it.release()
                } catch (_: Exception) {}
            }
            currentPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { mp ->
                    try { mp.release() } catch (_: Exception) {}
                }
                setOnErrorListener { mp, _, _ ->
                    try { mp.release() } catch (_: Exception) {}
                    true
                }
                start()
            }
        } catch (_: Exception) {
            // 音效播放失败静默处理
        }
    }

    private fun vibrateForAction(action: CatAction) {
        val vibrator = this.vibrator ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (action) {
                    CatAction.PET_HEAD -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    CatAction.SCRATCH_CHIN -> VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                    CatAction.RUB_BELLY -> VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE)
                    CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN ->
                        VibrationEffect.createOneShot(100, 100)
                    CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER ->
                        VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 60), -1)
                    CatAction.CLEAN_BATH -> VibrationEffect.createOneShot(200, 80)
                    CatAction.CLEAN_BRUSH -> VibrationEffect.createOneShot(60, 60)
                    CatAction.SLEEP -> VibrationEffect.createOneShot(30, 30)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val duration = when (action) {
                    CatAction.PET_HEAD -> 50L
                    CatAction.SCRATCH_CHIN -> 80L
                    CatAction.RUB_BELLY -> 120L
                    CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> 100L
                    CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> 150L
                    CatAction.CLEAN_BATH -> 200L
                    CatAction.CLEAN_BRUSH -> 60L
                    CatAction.SLEEP -> 30L
                }
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    /**
     * 播放成就解锁音效
     */
    fun playAchievementSound() {
        if (soundEnabled) playSound(R.raw.sound_achievement)
        if (vibrationEnabled) {
            vibrator?.let {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100, 50, 200), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(longArrayOf(0, 100, 50, 100, 50, 200), -1)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * 播放购买成功音效
     */
    fun playPurchaseSound() {
        if (soundEnabled) playSound(R.raw.sound_purchase)
        if (vibrationEnabled) {
            vibrator?.let {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot(80, 150))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(80L)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun release() {
        currentPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) {}
        }
        currentPlayer = null
    }
}
