package com.timenw.cattracker.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timenw.cattracker.data.model.*
import java.time.LocalDate

class CatRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("cat_tracker", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ==================== 猫的数据 ====================

    fun getCat(): Cat {
        val json = prefs.getString("cat", null)
        return if (json != null) {
            try { gson.fromJson(json, Cat::class.java) } catch (e: Exception) { Cat() }
        } else {
            Cat()
        }
    }

    fun saveCat(cat: Cat) {
        prefs.edit().putString("cat", gson.toJson(cat)).apply()
    }

    // ==================== 撸猫记录 ====================

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

    fun removeRecord(id: Long, date: LocalDate = LocalDate.now()) {
        val records = getRecords(date).toMutableList()
        records.removeAll { it.id == id }
        saveRecords(records, date)
    }

    private fun saveRecords(records: List<CatRecord>, date: LocalDate) {
        prefs.edit().putString("records_${date}", gson.toJson(records)).apply()
    }

    // ==================== 每日统计 ====================

    fun getDailySummary(date: LocalDate = LocalDate.now()): DailyCatSummary {
        val records = getRecords(date)
        if (records.isEmpty()) return DailyCatSummary(date = date.toString())
        val interactions = records.count {
            it.actionType in listOf("PET_HEAD", "SCRATCH_CHIN", "RUB_BELLY")
        }
        val feeds = records.count {
            it.actionType in listOf("FEED_FOOD", "FEED_SNACK", "FEED_CAN")
        }
        val plays = records.count {
            it.actionType in listOf("PLAY_CAT_TEASE", "PLAY_BALL", "PLAY_LASER")
        }
        val cleans = records.count {
            it.actionType in listOf("CLEAN_BATH", "CLEAN_BRUSH")
        }
        val intimacyGain = records.sumOf { it.intimacyAfter - it.intimacyBefore }
        val avgHappy = if (records.isNotEmpty()) {
            records.map { (it.happinessBefore + it.happinessAfter) / 2 }.average().toInt()
        } else 0
        return DailyCatSummary(
            date = date.toString(),
            interactionCount = interactions,
            feedCount = feeds,
            playCount = plays,
            cleanCount = cleans,
            totalIntimacyGain = intimacyGain,
            avgHappiness = avgHappy,
            coinsEarned = interactions * 2 + feeds * 3 + plays * 3 + cleans * 5
        )
    }

    fun getWeeklyData(): List<DailyCatSummary> {
        val today = LocalDate.now()
        return (0..6).map { daysAgo ->
            getDailySummary(today.minusDays(daysAgo.toLong()))
        }.reversed()
    }

    fun getMonthlyData(): List<DailyCatSummary> {
        val today = LocalDate.now()
        return (0..29).map { daysAgo ->
            getDailySummary(today.minusDays(daysAgo.toLong()))
        }.reversed()
    }

    // ==================== 成就 ====================

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

    // ==================== 签到 ====================

    fun getConsecutiveDays(): Int {
        val today = LocalDate.now()
        var count = 0
        var date = today
        while (hasRecords(date)) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    private fun hasRecords(date: LocalDate): Boolean {
        return getRecords(date).isNotEmpty()
    }

    fun getLastSignInDate(): LocalDate? {
        val dateStr = prefs.getString("last_sign_in", null) ?: return null
        return try { LocalDate.parse(dateStr) } catch (e: Exception) { null }
    }

    fun signIn() {
        prefs.edit().putString("last_sign_in", LocalDate.now().toString()).apply()
    }

    // ==================== 设置 ====================

    fun getSettings(): UserSettings {
        val json = prefs.getString("settings", null)
        return if (json != null) {
            try { gson.fromJson(json, UserSettings::class.java) } catch (e: Exception) { UserSettings() }
        } else {
            UserSettings()
        }
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit().putString("settings", gson.toJson(settings)).apply()
    }

    // ==================== 互动冷却检查 ====================

    fun isActionOnCooldown(action: CatAction, cat: Cat): Boolean {
        val lastTime = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> cat.lastInteractionTime
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> cat.lastFeedTime
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> cat.lastPlayTime
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> cat.lastCleanTime
            CatAction.SLEEP -> 0L
        }
        val elapsed = System.currentTimeMillis() - lastTime
        return elapsed < action.cooldownMs
    }

    fun getCooldownRemaining(action: CatAction, cat: Cat): Long {
        val lastTime = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> cat.lastInteractionTime
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> cat.lastFeedTime
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> cat.lastPlayTime
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> cat.lastCleanTime
            CatAction.SLEEP -> 0L
        }
        val elapsed = System.currentTimeMillis() - lastTime
        return (action.cooldownMs - elapsed).coerceAtLeast(0L)
    }

    // ==================== 执行互动 ====================

    fun performAction(action: CatAction, cat: Cat): Cat {
        val newCat = when (action) {
            CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> {
                val intimacyGain = action.intimacyBonus
                val newIntimacy = cat.intimacy + intimacyGain
                val newLevel = (newIntimacy / 100) + 1
                val newExp = newIntimacy % 100
                cat.copy(
                    intimacy = newIntimacy,
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    energy = (cat.energy - action.energyCost).coerceIn(0, 100),
                    lastInteractionTime = System.currentTimeMillis(),
                    totalInteractions = cat.totalInteractions + 1,
                    level = newLevel.coerceIn(1, 6),
                    exp = newExp,
                    coins = cat.coins + 2
                )
            }
            CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> {
                val hungerBonus = -action.hungerCost
                val weightGain = when (action) {
                    CatAction.FEED_SNACK -> 0.1f
                    CatAction.FEED_CAN -> 0.15f
                    else -> 0.05f
                }
                cat.copy(
                    hunger = (cat.hunger + hungerBonus).coerceIn(0, 100),
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    weight = (cat.weight + weightGain).coerceIn(1f, 15f),
                    lastFeedTime = System.currentTimeMillis(),
                    totalFeeds = cat.totalFeeds + 1,
                    coins = cat.coins + 3
                )
            }
            CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> {
                cat.copy(
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    energy = (cat.energy - action.energyCost).coerceIn(0, 100),
                    hunger = (cat.hunger + action.hungerCost).coerceIn(0, 100),
                    lastPlayTime = System.currentTimeMillis(),
                    totalPlays = cat.totalPlays + 1,
                    coins = cat.coins + 3
                )
            }
            CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> {
                val cleanGain = if (action == CatAction.CLEAN_BATH) 50 else 30
                cat.copy(
                    cleanliness = (cat.cleanliness + cleanGain).coerceIn(0, 100),
                    happiness = (cat.happiness + action.happinessBonus).coerceIn(0, 100),
                    lastCleanTime = System.currentTimeMillis(),
                    totalCleans = cat.totalCleans + 1,
                    coins = cat.coins + 5
                )
            }
            CatAction.SLEEP -> {
                cat.copy(
                    energy = 100,
                    hunger = (cat.hunger - 10).coerceIn(0, 100)
                )
            }
        }
        saveCat(newCat)
        return newCat
    }

    // ==================== 商店 ====================

    fun buyItem(item: ShopItem, cat: Cat): Cat {
        if (cat.coins < item.price) return cat
        val newCat = when (item.category) {
            ShopCategory.FOOD -> {
                when (item.id) {
                    1 -> cat.copy(hunger = (cat.hunger + 40).coerceIn(0, 100), coins = cat.coins - item.price)
                    2 -> cat.copy(hunger = (cat.hunger + 20).coerceIn(0, 100), happiness = (cat.happiness + 5).coerceIn(0, 100), coins = cat.coins - item.price)
                    3 -> cat.copy(happiness = (cat.happiness + 15).coerceIn(0, 100), coins = cat.coins - item.price)
                    4 -> cat.copy(hunger = (cat.hunger + 50).coerceIn(0, 100), coins = cat.coins - item.price)
                    5 -> cat.copy(hunger = (cat.hunger + 10).coerceIn(0, 100), happiness = (cat.happiness + 10).coerceIn(0, 100), cleanliness = (cat.cleanliness + 10).coerceIn(0, 100), energy = (cat.energy + 10).coerceIn(0, 100), coins = cat.coins - item.price)
                    else -> cat.copy(hunger = (cat.hunger + 30).coerceIn(0, 100), coins = cat.coins - item.price)
                }
            }
            ShopCategory.TOY -> {
                when (item.id) {
                    12 -> cat.copy(happiness = (cat.happiness + 15).coerceIn(0, 100), energy = (cat.energy - 5).coerceIn(0, 100), coins = cat.coins - item.price)
                    13 -> cat.copy(happiness = (cat.happiness + 18).coerceIn(0, 100), energy = (cat.energy - 8).coerceIn(0, 100), coins = cat.coins - item.price)
                    14 -> cat.copy(happiness = (cat.happiness + 5).coerceIn(0, 100), energy = (cat.energy + 5).coerceIn(0, 100), hunger = (cat.hunger + 5).coerceIn(0, 100), coins = cat.coins - item.price)
                    else -> cat.copy(happiness = (cat.happiness + 10).coerceIn(0, 100), energy = (cat.energy - 5).coerceIn(0, 100), coins = cat.coins - item.price)
                }
            }
            ShopCategory.SKIN -> {
                val skins = if (cat.unlockedSkins.isEmpty()) mutableSetOf() else cat.unlockedSkins.split(",").mapNotNull { it.toIntOrNull() }.toMutableSet()
                skins.add(item.id)
                cat.copy(unlockedSkins = skins.joinToString(","), skinId = item.id, coins = cat.coins - item.price)
            }
            ShopCategory.FURNITURE -> {
                cat.copy(coins = cat.coins - item.price)
            }
        }
        saveCat(newCat)
        return newCat
    }

    fun isItemOwned(item: ShopItem, cat: Cat): Boolean {
        if (item.isDefault) return true
        return when (item.category) {
            ShopCategory.SKIN -> {
                val skins = cat.unlockedSkins.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                item.id in skins
            }
            ShopCategory.TOY -> {
                val toys = cat.unlockedToys.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                item.id in toys
            }
            ShopCategory.FOOD -> {
                val foods = cat.unlockedFoods.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                item.id in foods
            }
            ShopCategory.FURNITURE -> false
        }
    }

    // ==================== 自然衰减 ====================

    fun applyNaturalDecay(cat: Cat): Cat {
        val hoursSinceLastInteraction = (System.currentTimeMillis() - cat.lastInteractionTime) / (1000 * 60 * 60)
        val decayFactor = hoursSinceLastInteraction.coerceAtMost(24)
        val newCat = cat.copy(
            hunger = (cat.hunger - decayFactor * 2).coerceIn(0, 100),
            happiness = (cat.happiness - decayFactor * 1).coerceIn(0, 100),
            energy = (cat.energy - decayFactor * 1).coerceIn(0, 100),
            cleanliness = (cat.cleanliness - decayFactor * 1).coerceIn(0, 100)
        )
        if (newCat != cat) saveCat(newCat)
        return newCat
    }
}
