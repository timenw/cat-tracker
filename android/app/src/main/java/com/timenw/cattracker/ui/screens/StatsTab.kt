package com.timenw.cattracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.data.model.*
import com.timenw.cattracker.data.repository.CatRepository
import com.timenw.cattracker.ui.components.*
import com.timenw.cattracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTab(
    cat: Cat,
    weeklyData: List<DailyCatSummary>,
    monthlyData: List<DailyCatSummary>,
    unlockedAchievements: List<Achievement>,
    lockedAchievements: List<Achievement>,
    consecutiveDays: Int
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("周统计", "月统计", "成就")

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("数据统计", fontWeight = FontWeight.Bold)
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> WeeklyStatsContent(weeklyData, cat, consecutiveDays)
            1 -> MonthlyStatsContent(monthlyData, cat)
            2 -> AchievementsContent(unlockedAchievements, lockedAchievements, cat)
        }
    }
}

@Composable
fun WeeklyStatsContent(weeklyData: List<DailyCatSummary>, cat: Cat, consecutiveDays: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 本周总览
        item {
            val totalInteractions = weeklyData.sumOf { it.interactionCount }
            val totalFeeds = weeklyData.sumOf { it.feedCount }
            val totalPlays = weeklyData.sumOf { it.playCount }
            val totalCleans = weeklyData.sumOf { it.cleanCount }
            val totalIntimacy = weeklyData.sumOf { it.totalIntimacyGain }
            val totalCoins = weeklyData.sumOf { it.coinsEarned }

            Text("本周总览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "互动", value = "${totalInteractions}次", modifier = Modifier.weight(1f), emoji = "🤚")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "喂食", value = "${totalFeeds}次", modifier = Modifier.weight(1f), emoji = "🍖")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "玩耍", value = "${totalPlays}次", modifier = Modifier.weight(1f), emoji = "🪶")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "清洁", value = "${totalCleans}次", modifier = Modifier.weight(1f), emoji = "🛁")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMax()) {
                SummaryCard(title = "亲密度", value = "+${totalIntimacy}", modifier = Modifier.weight(1f), emoji = "💕")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "金币", value = "+${totalCoins}", modifier = Modifier.weight(1f), emoji = "💰")
            }
        }

        // 连续签到
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "连续签到 $consecutiveDays 天",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CatOrange
                        )
                        Text(
                            if (consecutiveDays >= 7) "太棒了！继续保持！" else "继续每天撸猫签到吧！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 本周互动趋势图
        item {
            Text("本周互动趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (weeklyData.all { it.interactionCount == 0 }) {
                        Text(
                            text = "暂无数据，开始撸猫吧 🐱",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        InteractionBarChart(weeklyData)
                    }
                }
            }
        }

        // 猫的健康报告
        item {
            Text("猫的健康报告", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val healthScore = (cat.hunger + cat.happiness + cat.energy + cat.cleanliness) / 4
                    val healthStatus = when {
                        healthScore >= 80 -> "非常健康" to CatSafe
                        healthScore >= 60 -> "健康" to CatSafe
                        healthScore >= 40 -> "一般" to CatWarning
                        healthScore >= 20 -> "需要关注" to CatWarning
                        else -> "状态不佳" to CatDanger
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("健康评分", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            "${healthScore}分 - ${healthStatus.first}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = healthStatus.second
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    val suggestions = mutableListOf<String>()
                    if (cat.hunger < 30) suggestions.add("🍖 猫饿了，记得喂食！")
                    if (cat.happiness < 30) suggestions.add("🪶 猫不开心，陪它玩玩吧！")
                    if (cat.energy < 30) suggestions.add("😴 猫累了，让它休息一下！")
                    if (cat.cleanliness < 30) suggestions.add("🛁 猫脏了，该洗澡了！")
                    if (cat.isOverweight) suggestions.add("⚖️ 猫有点胖，少喂零食多运动！")
                    if (suggestions.isEmpty()) suggestions.add("✅ 猫的状态很好，继续保持！")
                    suggestions.forEach { suggestion ->
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 猫的基本信息
        item {
            Text("猫的信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("名字", cat.name)
                    InfoRow("品种", cat.breed.displayName)
                    InfoRow("等级", "${cat.levelTitle} Lv.${cat.level}")
                    InfoRow("亲密度", "${cat.intimacy}")
                    InfoRow("体重", "${String.format("%.1f", cat.weight)}kg (${cat.weightStatus})")
                    InfoRow("总互动", "${cat.totalInteractions}次")
                    InfoRow("总喂食", "${cat.totalFeeds}次")
                    InfoRow("总玩耍", "${cat.totalPlays}次")
                    InfoRow("总清洁", "${cat.totalCleans}次")
                    InfoRow("金币", "${cat.coins}")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun MonthlyStatsContent(monthlyData: List<DailyCatSummary>, cat: Cat) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            val totalInteractions = monthlyData.sumOf { it.interactionCount }
            val totalFeeds = monthlyData.sumOf { it.feedCount }
            val totalPlays = monthlyData.sumOf { it.playCount }
            val totalCleans = monthlyData.sumOf { it.cleanCount }
            val totalIntimacy = monthlyData.sumOf { it.totalIntimacyGain }
            val totalCoins = monthlyData.sumOf { it.coinsEarned }
            val activeDays = monthlyData.count { it.interactionCount > 0 }

            Text("本月总览", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "互动", value = "${totalInteractions}次", modifier = Modifier.weight(1f), emoji = "🤚")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "喂食", value = "${totalFeeds}次", modifier = Modifier.weight(1f), emoji = "🍖")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "玩耍", value = "${totalPlays}次", modifier = Modifier.weight(1f), emoji = "🪶")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "清洁", value = "${totalCleans}次", modifier = Modifier.weight(1f), emoji = "🛁")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "亲密度", value = "+${totalIntimacy}", modifier = Modifier.weight(1f), emoji = "💕")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "金币", value = "+${totalCoins}", modifier = Modifier.weight(1f), emoji = "💰")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(title = "活跃天数", value = "${activeDays}天", modifier = Modifier.weight(1f), emoji = "📅")
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard(title = "日均互动", value = "${if (activeDays > 0) totalInteractions / activeDays else 0}次", modifier = Modifier.weight(1f), emoji = "📊")
            }
        }

        // 月度互动热力图（简化版）
        item {
            Text("月度互动热力图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val maxCount = monthlyData.maxOfOrNull { it.interactionCount } ?: 1
                    val chunked = monthlyData.chunked(7)
                    chunked.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            week.forEach { day ->
                                val intensity = if (maxCount > 0) day.interactionCount.toFloat() / maxCount else 0f
                                val color = when {
                                    intensity > 0.75f -> CatOrange
                                    intensity > 0.5f -> CatOrange.copy(alpha = 0.7f)
                                    intensity > 0.25f -> CatOrange.copy(alpha = 0.4f)
                                    intensity > 0f -> CatOrange.copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawRect(
                                            color = color,
                                            topLeft = Offset.Zero,
                                            size = size
                                        )
                                    }
                                }
                            }
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf(0.1f, 0.3f, 0.5f, 0.7f, 1f).forEach { alpha ->
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    drawRect(
                                        color = CatOrange.copy(alpha = alpha),
                                        topLeft = Offset.Zero,
                                        size = size
                                    )
                                }
                            }
                        }
                        Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun AchievementsContent(
    unlockedAchievements: List<Achievement>,
    lockedAchievements: List<Achievement>,
    cat: Cat
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("成就系统", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${unlockedAchievements.size}/${unlockedAchievements.size + lockedAchievements.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 已解锁成就
        if (unlockedAchievements.isNotEmpty()) {
            item {
                Text("🏆 已解锁 (${unlockedAchievements.size})", style = MaterialTheme.typography.labelLarge, color = CatGold)
            }
            items(unlockedAchievements.size) { index ->
                val achievement = unlockedAchievements[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatGold.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(achievement.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                achievement.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                achievement.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "+${achievement.reward}💰",
                            style = MaterialTheme.typography.labelMedium,
                            color = CatGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 未解锁成就
        if (lockedAchievements.isNotEmpty()) {
            item {
                Text("🔒 未解锁 (${lockedAchievements.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(lockedAchievements.take(5).size) { index ->
                val achievement = lockedAchievements[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                achievement.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                achievement.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${achievement.requirement}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InteractionBarChart(data: List<DailyCatSummary>) {
    val maxCount = data.maxOfOrNull { it.interactionCount } ?: 1
    val dayFormatter = SimpleDateFormat("E", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { summary ->
            val barHeight = (summary.interactionCount.toFloat() / maxCount.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${summary.interactionCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(100.dp)) {
                    val barWidth = size.width
                    val barH = size.height * barHeight
                    drawRect(
                        color = CatOrange,
                        topLeft = Offset(0f, size.height - barH),
                        size = Size(barWidth, barH)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = try {
                        dayFormatter.format(
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(summary.date) ?: Date()
                        )
                    } catch (e: Exception) {
                        summary.date.takeLast(2)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
