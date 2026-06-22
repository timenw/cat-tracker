package com.timenw.cattracker.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timenw.cattracker.data.model.*
import java.time.LocalDate

class CatRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("cat_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCat(): Cat {
        val json = prefs.getString("cat", null)
        return if (json != null) {
            try { gson.fromJson(json, Cat::class.java) } catch (e: Exception) { Cat() }
        } else Cat()
    }

    fun saveCat(cat: Cat) {
        prefs.edit().putString("cat", gson.toJson(cat)).apply()
    }

    fun getRecords(date: LocalDate = LocalDate.now()): List<CatRecord> {
        val key = "records_${date}"
        val json = prefs.getString(key, "[]") ?: "[]"
        val type = object : TypeToken<List<CatRecord>>() {}.type
        return try { gson.fromJson(json, type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    fun addRecord(record: CatRecord) {
        val records = getRecords(LocalDate.parse(record.date)).toMutableList()
        records.add(record)
        saveRecords(records, LocalDate.parse(record.date))
    }

    private fun saveRecords(records: List<CatRecord>, date: LocalDate) {
        prefs.edit().putString("records_${date}", gson.toJson(records)).apply()
    }

    fun getDailySummary(date: LocalDate = LocalDate.now()): DailyCatSummary {
        val records = getRecords(date)
        if (records.isEmpty()) return DailyCatSummary(date = date.toString())
        val interactions = records.count { it.actionType in listOf("PET_HEAD", "SCRATCH_CHIN", "RUB_BELLY") }
        val feeds = records.count { it.actionType in listOf("FEED_FOOD", "FEED_SNACK", "FEED_CAN") }
        val plays = records.count { it.actionType in listOf("PLAY_CAT_TEASE", "PLAY_BALL", "PLAY_LASER") }
        val cleans = records.count { it.actionType in listOf("CLEAN_BATH", "CLEAN_BRUSH") }
        val intimacyGain = records.sumOf { it.intimacyAfter - it.intimacyBefore }
        val avgHappy = if (records.isNotEmpty()) records.map { (it.happinessBefore + it.happinessAfter) / 2 }.average().toInt() else 0
        return DailyCatSummary(
            date = date.toString(), interactionCount = interactions, feedCount = feeds,
            playCount = plays, cleanCount = cleans, totalIntimacyGain = intimacyGain,
            avgHappiness = avgHappy, coinsEarned = interactions * 2 + feeds * 3 + plays * 3 + cleans * 5
        )
    }

    fun getWeeklyData(): List<DailyCatSummary> {
        val today = LocalDate.now()
        return (0..6).map { getDailySummary(today.minusDays(it.toLong())) }.reversed()
    }

    fun getMonthlyData(): List<DailyCatSummary> {
        val today = LocalDate.now()
        return (0..29).map { getDailySummary(today.minusDays(it.toLong())) }.reversed()
    }

    fun getUnlockedAchievements(cat: Cat): List<Achievement> {
        return ALL_ACHIEVEMENTS.filter { achievement ->
            when (achievement.type) {
                AchievementType.TOTAL_INTERACTIONS -> cat.totalInteractions >= achievement.requirement
                AchievementType.TOTAL_FEEDS -> cat.totalFeeds >= achievement.requirement
                AchievementType.TOTAL_PLAYS -> cat.totalPlays >= achievement.requirement
                AchievementType.TOTAL_CLEANS -> cat.totalCleans >= achievement.requirement
                AchievementType.INTIMACY_LEVEL -> cat.intimacy >= achievement.requirement
                AchievementType.CAT_LEVEL -> cat.level >= achievement.requirement
                AchievementType.CONSECUTIVE_DAYS -> getConsecutiveDays() >= achievement.requirement
                AchievementType.COINS_EARNED -> cat.coins >= achievement.requirement
            }
        }
    }

    fun getLockedAchievements(cat: Cat): List<Achievement> {
        val unlocked = getUnlockedAchievements(cat).map { it.id }.toSet()
        return ALL_ACHIEVEMENTS.filter { it.id !in unlocked }
    }

    fun getConsecutiveDays(): Int {
        val today = LocalDate.now()
        var count = 0
        var date = today
        while (getRecords(date).isNotEmpty()) { count++; date = date.minusDays(1) }
        return count
    }

    fun getSettings(): UserSettings {
        val json = prefs.getString("settings", null)
        return if (json != null) try { gson.fromJson(json, UserSettings::class.java) } catch (e: Exception) { UserSettings() } else UserSettings()
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit().putString("settings", gson.toJson(settings)).apply()
    }

    fun isActionOnCooldown(action: CatAction, cat: Cat): Boolean {
        val lastTime = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> cat.lastInteractionTime
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> cat.lastFeedTime
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> cat.lastPlayTime
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> cat.lastCleanTime
            CatAction.SLEEP -> 0L
        }
        return (System.currentTimeMillis() - lastTime) < action.cooldownMs
    }

    fun getCooldownRemaining(action: CatAction, cat: Cat): Long {
        val lastTime = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> cat.lastInteractionTime
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> cat.lastFeedTime
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> cat.lastPlayTime
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> cat.lastCleanTime
            CatAction.SLEEP -> 0L
        }
        return (action.cooldownMs - (System.currentTimeMillis() - lastTime)).coerceAtLeast(0L)
    }

    /**
     * 执行互动 — 返回更新后的猫（含动画状态）
     */
    fun performAction(action: CatAction, cat: Cat): Cat {
        val animName = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> "happy"
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> "eating"
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> "playing"
            CatAction.CLEAN_BATH -> "bathing"
            CatAction.CLEAN_BRUSH -> "happy"
            CatAction.SLEEP -> "sleeping"
        }
        val newCat = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> {
                val newIntimacy = cat.intimacy + action.intimacyBonus
                cat.copy(
                    intimacy = newIntimacy, happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    energy = (cat.energy - action.energyCost).coerceIn(0, 100),
                    lastInteractionTime = System.currentTimeMillis(), totalInteractions = cat.totalInteractions + 1,
                    level = ((newIntimacy / 100) + 1).coerceIn(1, 6), exp = newIntimacy % 100,
                    coins = cat.coins + 2, currentAnimation = animName
                )
            }
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> {
                val hungerBonus = -action.hungerCost
                val weightGain = when (action) { CatAction.FEED_SNACK -> 0.1f; CatAction.FEED_CAN -> 0.15f; else -> 0.05f }
                cat.copy(
                    hunger = (cat.hunger + hungerBonus).coerceIn(0, 100),
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    weight = (cat.weight + weightGain).coerceIn(1f, 15f),
                    lastFeedTime = System.currentTimeMillis(), totalFeeds = cat.totalFeeds + 1,
                    coins = cat.coins + 3, currentAnimation = animName
                )
            }
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> {
                cat.copy(
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    energy = (cat.energy - action.energyCost).coerceIn(0, 100),
                    hunger = (cat.hunger + action.hungerCost).coerceIn(0, 100),
                    lastPlayTime = System.currentTimeMillis(), totalPlays = cat.totalPlays + 1,
                    coins = cat.coins + 3, currentAnimation = animName
                )
            }
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> {
                val cleanGain = if (action == CatAction.CLEAN_BATH) 50 else 30
                cat.copy(
                    cleanliness = (cat.cleanliness + cleanGain).coerceIn(0, 100),
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    lastCleanTime = System.currentTimeMillis(), totalCleans = cat.totalCleans + 1,
                    coins = cat.coins + 5, currentAnimation = animName
                )
            }
            CatAction.SLEEP -> {
                cat.copy(energy = 100, hunger = (cat.hunger - 10).coerceIn(0, 100), currentAnimation = animName)
            }
        }
        saveCat(newCat)
        return newCat
    }

    /**
     * 购买物品 — 放入仓库，不自动使用
     */
    fun buyItem(item: ShopItem, cat: Cat, isPremium: Boolean = false): Cat {
        // 会员皮肤免费
        val actualPrice = if (isPremium && item.category == ShopCategory.SKIN && !item.isDefault) 0 else item.price
        if (cat.coins < actualPrice) return cat

        var newCat = cat.copy(coins = cat.coins - actualPrice)

        when (item.category) {
            ShopCategory.SKIN -> {
                val skins = if (cat.unlockedSkins.isEmpty()) mutableSetOf()
                    else cat.unlockedSkins.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
                skins.add(item.id)
                newCat = newCat.copy(unlockedSkins = skins.joinToString(","))
            }
            ShopCategory.FOOD, ShopCategory.TOY, ShopCategory.FURNITURE -> {
                // 放入仓库
                newCat = newCat.copy(inventory = addToInventory(cat.inventory, item.id, 1))
            }
        }
        saveCat(newCat)
        return newCat
    }

    /**
     * 使用仓库中的物品
     */
    fun useItem(item: ShopItem, cat: Cat): Cat {
        val count = getItemCount(cat.inventory, item.id)
        if (count <= 0) return cat

        var newCat = cat.copy(inventory = removeFromInventory(cat.inventory, item.id, 1))

        when (item.category) {
            ShopCategory.FOOD -> {
                newCat = when (item.id) {
                    1 -> newCat.copy(hunger = (cat.hunger + 40).coerceIn(0, 100))
                    2 -> newCat.copy(hunger = (cat.hunger + 20).coerceIn(0, 100), happiness = (cat.happiness + 5).coerceIn(0, 100))
                    3 -> newCat.copy(happiness = (cat.happiness + 15).coerceIn(0, 100))
                    4 -> newCat.copy(hunger = (cat.hunger + 50).coerceIn(0, 100))
                    5 -> newCat.copy(hunger = (cat.hunger + 10).coerceIn(0, 100), happiness = (cat.happiness + 10).coerceIn(0, 100),
                        cleanliness = (cat.cleanliness + 10).coerceIn(0, 100), energy = (cat.energy + 10).coerceIn(0, 100))
                    else -> newCat.copy(hunger = (cat.hunger + 30).coerceIn(0, 100))
                }
            }
            ShopCategory.TOY -> {
                newCat = when (item.id) {
                    12 -> newCat.copy(happiness = (cat.happiness + 15).coerceIn(0, 100), energy = (cat.energy - 5).coerceIn(0, 100))
                    13 -> newCat.copy(happiness = (cat.happiness + 18).coerceIn(0, 100), energy = (cat.energy - 8).coerceIn(0, 100))
                    14 -> newCat.copy(happiness = (cat.happiness + 5).coerceIn(0, 100), energy = (cat.energy + 5).coerceIn(0, 100), hunger = (cat.hunger + 5).coerceIn(0, 100))
                    else -> newCat.copy(happiness = (cat.happiness + 10).coerceIn(0, 100), energy = (cat.energy - 5).coerceIn(0, 100))
                }
            }
            ShopCategory.SKIN -> {
                // 穿戴皮肤
                newCat = newCat.copy(skinId = item.id, currentAnimation = "happy")
            }
            ShopCategory.FURNITURE -> {
                // 家具暂时只增加心情
                newCat = newCat.copy(happiness = (cat.happiness + 20).coerceIn(0, 100))
            }
        }
        saveCat(newCat)
        return newCat
    }

    /**
     * 穿戴皮肤
     */
    fun wearSkin(skinId: Int, cat: Cat): Cat {
        val newCat = cat.copy(skinId = skinId, currentAnimation = "happy")
        saveCat(newCat)
        return newCat
    }

    /**
     * 重置动画状态（动画播放完毕后）
     */
    fun resetAnimation(cat: Cat): Cat {
        val newCat = cat.copy(currentAnimation = "idle")
        saveCat(newCat)
        return newCat
    }

    fun applyNaturalDecay(cat: Cat): Cat {
        val hours = ((System.currentTimeMillis() - cat.lastInteractionTime) / (1000 * 60 * 60)).coerceAtMost(24).toInt()
        val newCat = cat.copy(
            hunger = (cat.hunger - hours * 2).coerceIn(0, 100),
            happiness = (cat.happiness - hours * 1).coerceIn(0, 100),
            energy = (cat.energy - hours * 1).coerceIn(0, 100),
            cleanliness = (cat.cleanliness - hours * 1).coerceIn(0, 100)
        )
        if (newCat != cat) saveCat(newCat)
        return newCat
    }
}
