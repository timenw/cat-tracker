package com.timenw.cattracker.data

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.view.View
import com.timenw.cattracker.data.model.Cat
import com.timenw.cattracker.data.model.CatBreed
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 社交功能管理器
 * - 晒猫卡片生成（Canvas 绘制精美分享图）
 * - 猫友圈（本地模拟数据）
 * - 排行榜
 * - 猫配对
 */
class SocialManager(private val context: Context) {

    data class CatFriend(
        val id: Int,
        val name: String,
        val breed: CatBreed,
        val level: Int,
        val intimacy: Int,
        val moodEmoji: String,
        val ownerName: String,
        val signature: String,
        val likes: Int,
        val isLiked: Boolean = false
    )

    data class RankEntry(
        val rank: Int,
        val ownerName: String,
        val catName: String,
        val score: Int,
        val emoji: String
    )

    data class MatchResult(
        val success: Boolean,
        val message: String,
        val babyName: String = "",
        val babyBreed: CatBreed = CatBreed.DOMESTIC_SHORTHAIR,
        val babyEmoji: String = "🐱"
    )

    /**
     * 生成晒猫卡片图片
     * 返回图片文件路径
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
            color = Color.WHITE
            alpha = 30
            isAntiAlias = true
        }
        for (i in 0..20) {
            val x = Random(i.toLong()).nextFloat() * width
            val y = Random(i.toLong() + 100).nextFloat() * height
            canvas.drawCircle(x, y, Random(i.toLong() + 200).nextFloat() * 30 + 10, dotPaint)
        }

        // 顶部标题
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🐱 撸了喵", width / 2f, 100f, titlePaint)

        // 猫的名字
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 42f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.name, width / 2f, 180f, namePaint)

        // 等级信息
        val levelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            alpha = 200
        }
        canvas.drawText("${cat.levelTitle} Lv.${cat.level}", width / 2f, 230f, levelPaint)

        // 猫的大表情
        val emojiPaint = Paint().apply {
            textSize = 120f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.moodEmoji, width / 2f, 380f, emojiPaint)

        // 对话气泡
        val bubblePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            alpha = 230
        }
        val bubbleRect = RectF(80f, 420f, width - 80f, 520f)
        canvas.drawRoundRect(bubbleRect, 30f, 30f, bubblePaint)

        val speechPaint = Paint().apply {
            color = Color.parseColor("#5D4037")
            textSize = 30f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(cat.moodText, width / 2f, 475f, speechPaint)

        // 属性面板
        val panelPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            alpha = 240
        }
        val panelRect = RectF(60f, 560f, width - 60f, 820f)
        canvas.drawRoundRect(panelRect, 24f, 24f, panelPaint)

        // 属性文字
        val statLabelPaint = Paint().apply {
            color = Color.parseColor("#8D6E63")
            textSize = 24f
            isAntiAlias = true
        }
        val statValuePaint = Paint().apply {
            color = Color.parseColor("#FF8A65")
            textSize = 36f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        val stats = listOf(
            Triple("💕 亲密度", "${cat.intimacy}", 620f),
            Triple("🍖 饱食度", "${cat.hunger}", 680f),
            Triple("😊 心情", "${cat.happiness}", 740f),
            Triple("⚖️ 体重", "${String.format("%.1f", cat.weight)}kg", 800f)
        )

        stats.forEach { (label, value, y) ->
            canvas.drawText(label, 120f, y, statLabelPaint)
            canvas.drawText(value, width - 120f, y, statValuePaint.apply {
                textAlign = Paint.Align.RIGHT
            })
            statValuePaint.textAlign = Paint.Align.LEFT
        }

        // 底部信息
        val bottomPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            alpha = 180
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
        canvas.drawText("撸了喵 · $dateStr · 扫码一起撸猫", width / 2f, 900f, bottomPaint)

        // 保存图片
        val file = File(context.cacheDir, "cat_share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        return file.absolutePath
    }

    /**
     * 分享图片
     */
    fun shareImage(imagePath: String, text: String) {
        val file = File(imagePath)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享我的猫"))
    }

    /**
     * 获取猫友圈数据（本地模拟）
     */
    fun getCatFriends(): List<CatFriend> {
        return listOf(
            CatFriend(1, "咪咪", CatBreed.PERSIAN, 5, 320, "😻", "小明", "今天咪咪超级乖！", 128),
            CatFriend(2, "大橘", CatBreed.DOMESTIC_SHORTHAIR, 4, 250, "😺", "阿橘", "大橘又胖了...", 96),
            CatFriend(3, "小黑", CatBreed.BRITISH_SHORTHAIR, 6, 500, "😸", "黑猫警长", "小黑升到6级啦！", 256),
            CatFriend(4, "布布", CatBreed.RAGDOLL, 3, 180, "😻", "布偶控", "布偶猫真的太美了", 88),
            CatFriend(5, "折耳", CatBreed.SCOTTISH_FOLD, 4, 220, "🐈", "萌萌", "折耳猫好可爱", 64),
            CatFriend(6, "话痨", CatBreed.SIAMESE, 5, 350, "😼", "话痨主人", "暹罗猫真的太能说了", 168),
            CatFriend(7, "大毛", CatBreed.MAINE_COON, 3, 150, "🦁", "巨人", "缅因猫体型真的巨大", 72),
            CatFriend(8, "豹豹", CatBreed.BENGAL, 4, 280, "🐆", "野性美", "孟加拉猫的花纹太酷了", 144)
        )
    }

    /**
     * 获取排行榜数据
     */
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

        // 插入我的猫
        val myScore = myCat.intimacy + myCat.totalInteractions * 10 + myCat.level * 100
        val myEntry = RankEntry(0, myName, myCat.name, myScore, myCat.moodEmoji)
        entries.add(myEntry)

        // 排序
        val sorted = entries.sortedByDescending { it.score }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        return sorted.take(10)
    }

    /**
     * 猫配对
     * 根据两只猫的品种和等级计算配对结果
     */
    fun matchCats(cat1: Cat, cat2: Cat): MatchResult {
        // 配对成功率基于等级和亲密度
        val successRate = ((cat1.level + cat2.level) * 10 + (cat1.intimacy + cat2.intimacy) / 20)
            .coerceIn(30, 95)
        val success = Random().nextInt(100) < successRate

        if (!success) {
            return MatchResult(
                success = false,
                message = "两只猫不太合得来...再试试吧！"
            )
        }

        // 遗传品种
        val breeds = listOf(cat1.breed, cat2.breed)
        val babyBreed = breeds.random()

        // 随机名字
        val babyNames = listOf("小奶猫", "小可爱", "小宝贝", "小团子", "小毛球", "小天使")
        val babyName = babyNames.random()

        return MatchResult(
            success = true,
            message = "恭喜！${cat1.name}和${cat2.name}配对成功！",
            babyName = babyName,
            babyBreed = babyBreed,
            babyEmoji = babyBreed.emoji
        )
    }
}
