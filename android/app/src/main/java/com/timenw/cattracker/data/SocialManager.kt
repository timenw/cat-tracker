package com.timenw.cattracker.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.timenw.cattracker.data.model.Cat
import com.timenw.cattracker.data.model.CatBreed
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 社交功能管理器
 */
class SocialManager(private val context: Context) {

    data class CatFriend(
        val id: Int, val name: String, val breed: CatBreed, val level: Int,
        val intimacy: Int, val moodEmoji: String, val ownerName: String,
        val signature: String, val likes: Int, val isLiked: Boolean = false
    )

    data class RankEntry(
        val rank: Int, val ownerName: String, val catName: String,
        val score: Int, val emoji: String
    )

    data class MatchResult(
        val success: Boolean, val message: String,
        val babyName: String = "", val babyBreed: CatBreed = CatBreed.DOMESTIC_SHORTHAIR,
        val babyEmoji: String = "🐱"
    )

    /**
     * 生成晒猫卡片图片，保存到缓存目录
     */
    fun generateShareCard(cat: Cat): String {
        val width = 750
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景渐变
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#FF8A65"),
                    Color.parseColor("#FFB74D"),
                    Color.parseColor("#FFF3E0")
                ),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 装饰圆点
        val dotPaint = Paint().apply {
            color = Color.WHITE; alpha = 30; isAntiAlias = true
        }
        val rng = Random(42) // 固定种子保证每次一样
        for (i in 0..20) {
            val x = rng.nextFloat() * width
            val y = rng.nextFloat() * height
            canvas.drawCircle(x, y, rng.nextFloat() * 30 + 10, dotPaint)
        }

        // 顶部标题
        val titlePaint = Paint().apply {
            color = Color.WHITE; textSize = 56f; isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🐱 撸了喵", width / 2f, 100f, titlePaint)

        // 猫的名字
        val namePaint = Paint().apply {
            color = Color.WHITE; textSize = 42f; isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.name, width / 2f, 180f, namePaint)

        // 等级
        val levelPaint = Paint().apply {
            color = Color.WHITE; textSize = 32f; isAntiAlias = true
            textAlign = Paint.Align.CENTER; alpha = 200
        }
        canvas.drawText("${cat.levelTitle} Lv.${cat.level}", width / 2f, 230f, levelPaint)

        // 猫表情
        val emojiPaint = Paint().apply {
            textSize = 120f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.moodEmoji, width / 2f, 380f, emojiPaint)

        // 对话气泡
        val bubblePaint = Paint().apply { color = Color.WHITE; isAntiAlias = true; alpha = 230 }
        canvas.drawRoundRect(RectF(80f, 420f, width - 80f, 520f), 30f, 30f, bubblePaint)
        val speechPaint = Paint().apply {
            color = Color.parseColor("#5D4037"); textSize = 30f; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.moodText, width / 2f, 475f, speechPaint)

        // 属性面板
        val panelPaint = Paint().apply { color = Color.WHITE; isAntiAlias = true; alpha = 240 }
        canvas.drawRoundRect(RectF(60f, 560f, width - 60f, 820f), 24f, 24f, panelPaint)

        val statLabelPaint = Paint().apply {
            color = Color.parseColor("#8D6E63"); textSize = 24f; isAntiAlias = true
        }
        val statValuePaint = Paint().apply {
            color = Color.parseColor("#FF8A65"); textSize = 36f; isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
        }

        val stats = listOf(
            Triple("💕 亲密度", "${cat.intimacy}", 620f),
            Triple("🍖 饱食度", "${cat.hunger}", 680f),
            Triple("😊 心情", "${cat.happiness}", 740f),
            Triple("⚖️ 体重", "${String.format("%.1f", cat.weight)}kg", 800f)
        )
        stats.forEach { (label, value, y) ->
            canvas.drawText(label, 120f, y, statLabelPaint)
            statValuePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(value, width - 120f, y, statValuePaint)
            statValuePaint.textAlign = Paint.Align.LEFT
        }

        // 底部
        val bottomPaint = Paint().apply {
            color = Color.WHITE; textSize = 22f; isAntiAlias = true
            textAlign = Paint.Align.CENTER; alpha = 180
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        canvas.drawText("撸了喵 · $dateStr · 扫码一起撸猫", width / 2f, 900f, bottomPaint)

        // 保存到缓存
        val file = File(context.cacheDir, "cat_share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file.absolutePath
    }

    /**
     * 分享图片 — 修复 FLAG_ACTIVITY_NEW_TASK 和权限
     */
    fun shareImage(imagePath: String, text: String) {
        try {
            val file = File(imagePath)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "分享我的猫").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // 静默处理
        }
    }

    /**
     * 保存图片到相册 — 使用 MediaStore API
     */
    fun saveToGallery(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "cat_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/撸了喵")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    file.inputStream().copyTo(out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, values, null, null)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCatFriends(): List<CatFriend> = listOf(
        CatFriend(1, "咪咪", CatBreed.PERSIAN, 5, 320, "😻", "小明", "今天咪咪超级乖！", 128),
        CatFriend(2, "大橘", CatBreed.DOMESTIC_SHORTHAIR, 4, 250, "😺", "阿橘", "大橘又胖了...", 96),
        CatFriend(3, "小黑", CatBreed.BRITISH_SHORTHAIR, 6, 500, "😸", "黑猫警长", "小黑升到6级啦！", 256),
        CatFriend(4, "布布", CatBreed.RAGDOLL, 3, 180, "😻", "布偶控", "布偶猫真的太美了", 88),
        CatFriend(5, "折耳", CatBreed.SCOTTISH_FOLD, 4, 220, "🐈", "萌萌", "折耳猫好可爱", 64),
        CatFriend(6, "话痨", CatBreed.SIAMESE, 5, 350, "😼", "话痨主人", "暹罗猫真的太能说了", 168),
        CatFriend(7, "大毛", CatBreed.MAINE_COON, 3, 150, "🦁", "巨人", "缅因猫体型真的巨大", 72),
        CatFriend(8, "豹豹", CatBreed.BENGAL, 4, 280, "🐆", "野性美", "孟加拉猫的花纹太酷了", 144)
    )

    fun getLeaderboard(myCat: Cat, myName: String = "我"): List<RankEntry> {
        val entries = mutableListOf(
            RankEntry(0, "撸猫大神", "咪咪", 1280, "😻"),
            RankEntry(0, "铲屎官", "大橘", 1024, "😺"),
            RankEntry(0, "猫奴", "小黑", 896, "😸"),
            RankEntry(0, "吸猫人", "布布", 768, "😻"),
            RankEntry(0, "撸猫达人", "折耳", 640, "🐈"),
            RankEntry(0, "猫薄荷", "话痨", 512, "😼"),
            RankEntry(0, "铲屎的", "大毛", 384, "🦁"),
            RankEntry(0, "猫主子", "豹豹", 256, "🐆")
        )
        val myScore = myCat.intimacy + myCat.totalInteractions * 10 + myCat.level * 100
        entries.add(RankEntry(0, myName, myCat.name, myScore, myCat.moodEmoji))
        return entries.sortedByDescending { it.score }.mapIndexed { i, e -> e.copy(rank = i + 1) }.take(10)
    }

    fun matchCats(cat1: Cat, cat2: Cat): MatchResult {
        val successRate = ((cat1.level + cat2.level) * 10 + (cat1.intimacy + cat2.intimacy) / 20).coerceIn(30, 95)
        val success = Random().nextInt(100) < successRate
        if (!success) return MatchResult(false, "两只猫不太合得来...再试试吧！")
        val babyBreed = listOf(cat1.breed, cat2.breed).random()
        val babyNames = listOf("小奶猫", "小可爱", "小宝贝", "小团子", "小毛球", "小天使")
        return MatchResult(
            true, "恭喜！${cat1.name}和${cat2.name}配对成功！",
            babyNames.random(), babyBreed, babyBreed.emoji
        )
    }
}
