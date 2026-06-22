package com.timenw.cattracker.data

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.timenw.cattracker.data.model.CatAction

/**
 * 音效 + 震动管理器
 * 使用 SoundPool 播放短音效，Vibrator 提供震动反馈
 * 所有音效使用系统内置声音模拟（无需 raw 资源文件）
 */
class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var vibrator: Vibrator? = null
    private var soundEnabled = true
    private var vibrationEnabled = true

    // 系统音效 ID（使用 AudioManager 的音效类型模拟）
    private val soundMap = mutableMapOf<Int, Int>()

    init {
        initSoundPool()
        initVibrator()
    }

    private fun initSoundPool() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attrs)
            .build()
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
     * 不同动作有不同的震动模式
     */
    fun playActionSound(action: CatAction) {
        if (soundEnabled) playSoundForAction(action)
        if (vibrationEnabled) vibrateForAction(action)
    }

    private fun playSoundForAction(action: CatAction) {
        // 使用 ToneGenerator 播放不同音调来模拟不同音效
        try {
            val toneType = when (action) {
                CatAction.PET_HEAD -> 1      // 轻柔
                CatAction.SCRATCH_CHIN -> 2  // 中等
                CatAction.RUB_BELLY -> 3     // 低沉
                CatAction.FEED_FOOD,
                CatAction.FEED_SNACK,
                CatAction.FEED_CAN -> 4      // 咀嚼
                CatAction.PLAY_CAT_TEASE,
                CatAction.PLAY_BALL,
                CatAction.PLAY_LASER -> 5    // 欢快
                CatAction.CLEAN_BATH -> 6    // 水声
                CatAction.CLEAN_BRUSH -> 7   // 轻柔
                CatAction.SLEEP -> 8         // 安静
            }
            val duration = when (action) {
                CatAction.PET_HEAD -> 100
                CatAction.SCRATCH_CHIN -> 150
                CatAction.RUB_BELLY -> 200
                CatAction.FEED_FOOD,
                CatAction.FEED_SNACK,
                CatAction.FEED_CAN -> 300
                CatAction.PLAY_CAT_TEASE,
                CatAction.PLAY_BALL,
                CatAction.PLAY_LASER -> 250
                CatAction.CLEAN_BATH -> 400
                CatAction.CLEAN_BRUSH -> 200
                CatAction.SLEEP -> 500
            }
            // 使用系统 ToneGenerator 播放提示音
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 80
                )
                val tone = when (toneType) {
                    1 -> android.media.ToneGenerator.TONE_PROP_BEEP
                    2 -> android.media.ToneGenerator.TONE_PROP_BEEP2
                    3 -> android.media.ToneGenerator.TONE_CDMA_CONFIRM
                    4 -> android.media.ToneGenerator.TONE_CDMA_ANSWER
                    5 -> android.media.ToneGenerator.TONE_PROP_ACK
                    6 -> android.media.ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP
                    7 -> android.media.ToneGenerator.TONE_PROP_NACK
                    else -> android.media.ToneGenerator.TONE_CDMA_ONE_MIN_BEEP
                }
                toneGen.startTone(tone, duration)
                // 延迟释放
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGen.release()
                }, duration + 50L)
            } catch (_: Exception) {
                // ToneGenerator 不可用时静默处理
            }
        } catch (_: Exception) {
            // 静默处理所有音效错误
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
                    CatAction.FEED_FOOD,
                    CatAction.FEED_SNACK,
                    CatAction.FEED_CAN -> VibrationEffect.createOneShot(100, 100)
                    CatAction.PLAY_CAT_TEASE,
                    CatAction.PLAY_BALL,
                    CatAction.PLAY_LASER -> VibrationEffect.createWaveform(
                        longArrayOf(0, 60, 40, 60), -1
                    )
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
                    CatAction.FEED_FOOD,
                    CatAction.FEED_SNACK,
                    CatAction.FEED_CAN -> 100L
                    CatAction.PLAY_CAT_TEASE,
                    CatAction.PLAY_BALL,
                    CatAction.PLAY_LASER -> 150L
                    CatAction.CLEAN_BATH -> 200L
                    CatAction.CLEAN_BRUSH -> 60L
                    CatAction.SLEEP -> 30L
                }
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {
            // 震动不可用时静默处理
        }
    }

    /**
     * 播放成就解锁音效
     */
    fun playAchievementSound() {
        if (soundEnabled) {
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 100
                )
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 200)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 200)
                }, 250)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGen.release()
                }, 550)
            } catch (_: Exception) {}
        }
        if (vibrationEnabled) {
            vibrator?.let {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(
                            longArrayOf(0, 100, 50, 100, 50, 200), -1
                        ))
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
        if (soundEnabled) {
            try {
                val toneGen = android.media.ToneGenerator(
                    android.media.AudioManager.STREAM_MUSIC, 80
                )
                toneGen.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 150)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 150)
                }, 200)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    toneGen.release()
                }, 400)
            } catch (_: Exception) {}
        }
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
        soundPool?.release()
        soundPool = null
    }
}
