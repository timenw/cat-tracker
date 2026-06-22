package com.timenw.cattracker.data.model

import com.google.gson.annotations.SerializedName

// ==================== 猫的核心数据 ====================

data class Cat(
    val id: Long = 1,
    val name: String = "小咪",
    val breed: CatBreed = CatBreed.DOMESTIC_SHORTHAIR,
    val birthday: String = java.time.LocalDate.now().toString(),
    val level: Int = 1,
    val exp: Int = 0,
    val intimacy: Int = 0,
    val hunger: Int = 80,
    val happiness: Int = 70,
    val weight: Float = 4.5f,
    val cleanliness: Int = 80,
    val energy: Int = 80,
    val lastInteractionTime: Long = System.currentTimeMillis(),
    val lastFeedTime: Long = System.currentTimeMillis(),
    val lastPlayTime: Long = System.currentTimeMillis(),
    val lastCleanTime: Long = System.currentTimeMillis(),
    val totalInteractions: Int = 0,
    val totalFeeds: Int = 0,
    val totalPlays: Int = 0,
    val totalCleans: Int = 0,
    val coins: Int = 100,
    val skinId: Int = 0,          // 当前穿戴的皮肤ID
    val unlockedSkins: String = "0",
    val unlockedToys: String = "0",
    val unlockedFoods: String = "0",
    // 仓库：物品ID -> 数量
    val inventory: String = "",
    // 当前动画状态
    val currentAnimation: String = "idle"
) {
    val levelTitle: String
        get() = when (level) {
            1 -> "小奶猫"
            2 -> "幼猫"
            3 -> "少年猫"
            4 -> "青年猫"
            5 -> "成年猫"
            else -> "老猫"
        }

    val expToNext: Int
        get() = level * 100

    val expProgress: Float
        get() = (exp.toFloat() / expToNext.toFloat()).coerceIn(0f, 1f)

    val mood: CatMood
        get() {
            val avg = (hunger + happiness + cleanliness + energy) / 4
            return when {
                avg >= 80 -> CatMood.ECSTATIC
                avg >= 60 -> CatMood.HAPPY
                avg >= 40 -> CatMood.NEUTRAL
                avg >= 20 -> CatMood.SAD
                else -> CatMood.ANGRY
            }
        }

    val moodEmoji: String
        get() = mood.emoji

    val moodText: String
        get() = when (mood) {
            CatMood.ECSTATIC -> "喵~主人最好了！"
            CatMood.HAPPY -> "喵~好开心~"
            CatMood.NEUTRAL -> "喵？"
            CatMood.SAD -> "喵...有点无聊..."
            CatMood.ANGRY -> "哼！不理你了！"
        }

    val weightStatus: String
        get() = when {
            weight < 3.0f -> "偏瘦"
            weight <= 6.0f -> "健康"
            weight <= 8.0f -> "微胖"
            else -> "肥胖"
        }

    val isHungry: Boolean get() = hunger < 30
    val isDirty: Boolean get() = cleanliness < 30
    val isTired: Boolean get() = energy < 30
    val isOverweight: Boolean get() = weight > 7.0f

    // 获取皮肤主色调（用于猫窝显示）
    val skinColor: Long
        get() = when (skinId) {
            21 -> 0xFFFF8A65 // 橘猫 - 橙色
            22 -> 0xFF424242 // 黑猫 - 深灰
            23 -> 0xFFF5F5F5 // 白猫 - 白色
            24 -> 0xFFFFAB91 // 三花 - 浅橙
            25 -> 0xFF7C4DFF // 赛博 - 紫色
            else -> 0xFFFFB74D // 默认 - 金色
        }

    // 获取皮肤名称
    val skinName: String
        get() = when (skinId) {
            21 -> "橘猫"
            22 -> "黑猫"
            23 -> "白猫"
            24 -> "三花猫"
            25 -> "赛博猫"
            else -> "默认"
        }
}

// ==================== 猫品种 ====================

enum class CatBreed(val displayName: String, val emoji: String, val description: String, val isPremium: Boolean = false) {
    DOMESTIC_SHORTHAIR("中华田园猫", "🐱", "活泼好动，适应力强"),
    PERSIAN("波斯猫", "😺", "温顺优雅，长毛飘逸"),
    BRITISH_SHORTHAIR("英短", "😸", "圆脸大眼，性格温和"),
    RAGDOLL("布偶猫", "😻", "温柔粘人，像布偶一样", true),
    SCOTTISH_FOLD("折耳猫", "🐈", "可爱折耳，甜美乖巧", true),
    SIAMESE("暹罗猫", "😼", "聪明活泼，话特别多", true),
    MAINE_COON("缅因猫", "🦁", "体型巨大，温柔巨人", true),
    BENGAL("孟加拉猫", "🐆", "野性花纹，精力充沛", true)
}

// ==================== 心情枚举 ====================

enum class CatMood(val emoji: String, val displayName: String) {
    ECSTATIC("😻", "极度开心"),
    HAPPY("😺", "开心"),
    NEUTRAL("🐱", "平静"),
    SAD("😿", "难过"),
    ANGRY("😾", "生气")
}

// ==================== 互动动作 ====================

enum class CatAction(
    val displayName: String,
    val emoji: String,
    val intimacyBonus: Int,
    val happinessBonus: Int,
    val hungerCost: Int,
    val energyCost: Int,
    val cooldownMs: Long,
    val description: String
) {
    PET_HEAD("摸头", "🤚", 1, 5, 0, 2, 30_000, "轻轻摸摸猫的头"),
    SCRATCH_CHIN("挠下巴", "👆", 2, 8, 0, 2, 60_000, "挠挠猫的下巴"),
    RUB_BELLY("撸肚子", "🖐️", 3, 10, 0, 3, 120_000, "小心！猫可能会翻脸"),
    FEED_FOOD("喂猫粮", "🍖", 1, 3, -30, 0, 1_800_000, "给猫吃猫粮"),
    FEED_SNACK("喂零食", "🍤", 1, 8, -15, 0, 3_600_000, "给猫吃零食"),
    FEED_CAN("喂罐头", "🥫", 2, 6, -40, 0, 7_200_000, "给猫吃罐头"),
    PLAY_CAT_TEASE("逗猫棒", "🪶", 1, 12, 2, 10, 300_000, "用逗猫棒陪猫玩"),
    PLAY_BALL("毛线球", "🧶", 1, 10, 2, 8, 300_000, "和猫玩毛线球"),
    PLAY_LASER("激光笔", "🔴", 1, 15, 3, 12, 300_000, "用激光笔逗猫"),
    CLEAN_BATH("洗澡", "🛁", -1, -5, 0, 0, 86_400_000, "给猫洗澡，猫可能不开心"),
    CLEAN_BRUSH("梳毛", "💇", 1, 5, 0, 0, 43_200_000, "给猫梳毛"),
    SLEEP("睡觉", "😴", 0, 0, 0, -50, 0, "让猫好好休息")
}

// ==================== 商店物品 ====================

data class ShopItem(
    val id: Int,
    val name: String,
    val emoji: String,
    val category: ShopCategory,
    val price: Int,
    val description: String,
    val effect: String,
    val isDefault: Boolean = false
)

enum class ShopCategory(val displayName: String, val emoji: String) {
    FOOD("食物", "🍖"),
    TOY("玩具", "🧸"),
    SKIN("皮肤", "👗"),
    FURNITURE("家具", "🏠")
}

// ==================== 成就系统 ====================

data class Achievement(
    val id: Int,
    val name: String,
    val emoji: String,
    val description: String,
    val requirement: Int,
    val type: AchievementType,
    val reward: Int = 10
)

enum class AchievementType {
    TOTAL_INTERACTIONS,   // 总互动次数
    TOTAL_FEEDS,          // 总喂食次数
    TOTAL_PLAYS,          // 总玩耍次数
    TOTAL_CLEANS,         // 总清洁次数
    INTIMACY_LEVEL,       // 亲密度等级
    CAT_LEVEL,            // 猫等级
    CONSECUTIVE_DAYS,     // 连续签到天数
    COINS_EARNED          // 累计金币
}

// ==================== 撸猫记录 ====================

data class CatRecord(
    val id: Long = System.currentTimeMillis(),
    val actionType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = java.time.LocalDate.now().toString(),
    val intimacyBefore: Int = 0,
    val intimacyAfter: Int = 0,
    val happinessBefore: Int = 0,
    val happinessAfter: Int = 0,
    val note: String = ""
)

// ==================== 每日统计 ====================

data class DailyCatSummary(
    val date: String,
    val interactionCount: Int = 0,
    val feedCount: Int = 0,
    val playCount: Int = 0,
    val cleanCount: Int = 0,
    val totalIntimacyGain: Int = 0,
    val avgHappiness: Int = 0,
    val coinsEarned: Int = 0
)

// ==================== 用户设置 ====================

data class UserSettings(
    val reminderEnabled: Boolean = true,
    val feedReminderEnabled: Boolean = true,
    val playReminderEnabled: Boolean = true,
    val cleanReminderEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 60,
    val catName: String = "小咪",
    val catBreed: String = CatBreed.DOMESTIC_SHORTHAIR.name,
    val catBirthday: String = java.time.LocalDate.now().toString(),
    val soundEnabled: Boolean = true
)

// ==================== 预定义商店物品 ====================

val ALL_SHOP_ITEMS = listOf(
    // 食物
    ShopItem(0, "基础猫粮", "🍖", ShopCategory.FOOD, 0, "普通猫粮，饱食度+30", "饱食度+30", true),
    ShopItem(1, "高级猫粮", "🥩", ShopCategory.FOOD, 20, "优质猫粮，饱食度+40", "饱食度+40"),
    ShopItem(2, "小鱼干", "🐟", ShopCategory.FOOD, 15, "猫咪最爱，饱食度+20，心情+5", "饱食度+20"),
    ShopItem(3, "猫薄荷", "🌿", ShopCategory.FOOD, 25, "让猫超级开心！心情+15", "心情+15"),
    ShopItem(4, "金枪鱼罐头", "🥫", ShopCategory.FOOD, 30, "豪华罐头，饱食度+50", "饱食度+50"),
    ShopItem(5, "猫蛋糕", "🍰", ShopCategory.FOOD, 50, "生日蛋糕，全属性+10", "全属性+10"),
    // 玩具
    ShopItem(10, "逗猫棒", "🪶", ShopCategory.TOY, 0, "基础玩具，心情+12", "心情+12", true),
    ShopItem(11, "毛线球", "🧶", ShopCategory.TOY, 15, "经典玩具，心情+10", "心情+10"),
    ShopItem(12, "激光笔", "🔴", ShopCategory.TOY, 30, "高科技玩具，心情+15", "心情+15"),
    ShopItem(13, "电动老鼠", "🐭", ShopCategory.TOY, 40, "自动老鼠，心情+18", "心情+18"),
    ShopItem(14, "猫爬架", "🏗️", ShopCategory.TOY, 100, "豪华猫爬架，全属性+5", "全属性+5"),
    // 皮肤
    ShopItem(20, "默认皮肤", "🐱", ShopCategory.SKIN, 0, "默认猫咪外观", "默认", true),
    ShopItem(21, "橘猫", "🐈", ShopCategory.SKIN, 50, "大橘为重！", "橘猫皮肤"),
    ShopItem(22, "黑猫", "🐈‍⬛", ShopCategory.SKIN, 50, "神秘黑猫", "黑猫皮肤"),
    ShopItem(23, "白猫", "😺", ShopCategory.SKIN, 50, "纯白如雪", "白猫皮肤"),
    ShopItem(24, "三花猫", "😻", ShopCategory.SKIN, 80, "可爱三花", "三花猫皮肤"),
    ShopItem(25, "赛博猫", "🤖", ShopCategory.SKIN, 200, "未来科技感", "赛博猫皮肤"),
    // 家具
    ShopItem(30, "纸箱", "📦", ShopCategory.FURNITURE, 0, "猫最爱的纸箱！", "基础猫窝", true),
    ShopItem(31, "小木屋", "🛖", ShopCategory.FURNITURE, 80, "温馨小木屋", "舒适猫窝"),
    ShopItem(32, "豪华猫窝", "🏰", ShopCategory.FURNITURE, 200, "皇家级猫窝", "豪华猫窝"),
    ShopItem(33, "猫树", "🌳", ShopCategory.FURNITURE, 150, "多层猫树", "娱乐猫窝")
)

// ==================== 预定义成就 ====================

val ALL_ACHIEVEMENTS = listOf(
    Achievement(1, "初次见面", "👋", "第一次撸猫", 1, AchievementType.TOTAL_INTERACTIONS, 10),
    Achievement(2, "撸猫新手", "🤚", "累计撸猫10次", 10, AchievementType.TOTAL_INTERACTIONS, 20),
    Achievement(3, "撸猫达人", "💪", "累计撸猫50次", 50, AchievementType.TOTAL_INTERACTIONS, 50),
    Achievement(4, "撸猫大师", "🏆", "累计撸猫200次", 200, AchievementType.TOTAL_INTERACTIONS, 100),
    Achievement(5, "撸猫传说", "👑", "累计撸猫1000次", 1000, AchievementType.TOTAL_INTERACTIONS, 500),
    Achievement(6, "喂食新手", "🍖", "累计喂食10次", 10, AchievementType.TOTAL_FEEDS, 20),
    Achievement(7, "饲养员", "👨‍🌾", "累计喂食50次", 50, AchievementType.TOTAL_FEEDS, 50),
    Achievement(8, "超级饲养员", "🦸", "累计喂食200次", 200, AchievementType.TOTAL_FEEDS, 100),
    Achievement(9, "玩耍新手", "🪶", "累计玩耍10次", 10, AchievementType.TOTAL_PLAYS, 20),
    Achievement(10, "游戏达人", "🎮", "累计玩耍50次", 50, AchievementType.TOTAL_PLAYS, 50),
    Achievement(11, "清洁新手", "🛁", "累计清洁5次", 5, AchievementType.TOTAL_CLEANS, 20),
    Achievement(12, "爱干净", "✨", "累计清洁30次", 30, AchievementType.TOTAL_CLEANS, 50),
    Achievement(13, "亲密无间", "💕", "亲密度达到100", 100, AchievementType.INTIMACY_LEVEL, 30),
    Achievement(14, "心有灵犀", "💖", "亲密度达到500", 500, AchievementType.INTIMACY_LEVEL, 100),
    Achievement(15, "形影不离", "💗", "亲密度达到1000", 1000, AchievementType.INTIMACY_LEVEL, 300),
    Achievement(16, "猫生巅峰", "⭐", "猫达到3级", 3, AchievementType.CAT_LEVEL, 50),
    Achievement(17, "老猫传奇", "🌟", "猫达到5级", 5, AchievementType.CAT_LEVEL, 200),
    Achievement(18, "连续签到", "📅", "连续签到7天", 7, AchievementType.CONSECUTIVE_DAYS, 100),
    Achievement(19, "富甲一方", "💰", "累计获得500金币", 500, AchievementType.COINS_EARNED, 100),
    Achievement(20, "猫界首富", "💎", "累计获得2000金币", 2000, AchievementType.COINS_EARNED, 500)
)

// ==================== 仓库工具函数 ====================

/**
 * 解析仓库字符串为 Map
 */
fun parseInventory(inventory: String): Map<Int, Int> {
    if (inventory.isBlank()) return emptyMap()
    return inventory.split(",").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 2) {
            val id = parts[0].toIntOrNull()
            val count = parts[1].toIntOrNull()
            if (id != null && count != null) id to count else null
        } else null
    }.toMap()
}

/**
 * 将仓库 Map 序列化为字符串
 */
fun serializeInventory(map: Map<Int, Int>): String {
    return map.entries.joinToString(",") { "${it.key}:${it.value}" }
}

/**
 * 添加物品到仓库
 */
fun addToInventory(inventory: String, itemId: Int, count: Int = 1): String {
    val map = parseInventory(inventory).toMutableMap()
    map[itemId] = (map[itemId] ?: 0) + count
    return serializeInventory(map)
}

/**
 * 从仓库移除物品
 */
fun removeFromInventory(inventory: String, itemId: Int, count: Int = 1): String {
    val map = parseInventory(inventory).toMutableMap()
    val current = map[itemId] ?: return inventory
    if (current <= count) map.remove(itemId) else map[itemId] = current - count
    return serializeInventory(map)
}

/**
 * 获取仓库中物品数量
 */
fun getItemCount(inventory: String, itemId: Int): Int {
    return parseInventory(inventory)[itemId] ?: 0
}
