package com.timenw.cattracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.data.model.*
import com.timenw.cattracker.data.repository.CatRepository
import com.timenw.cattracker.ui.components.*
import com.timenw.cattracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatHomeTab(
    cat: Cat,
    todaySummary: DailyCatSummary,
    recentRecords: List<CatRecord>,
    settings: UserSettings,
    onAction: (CatAction) -> Unit,
    onSettingsChanged: (UserSettings) -> Unit
) {
    var showActionMessage by remember { mutableStateOf("") }
    var showMessageTimestamp by remember { mutableStateOf(0L) }

    // 自动清除消息
    LaunchedEffect(showMessageTimestamp) {
        if (showMessageTimestamp > 0) {
            kotlinx.coroutines.delay(2000)
            showActionMessage = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🐱", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("撸了喵", fontWeight = FontWeight.Bold)
                }
            },
            actions = {
                // 金币显示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${cat.coins}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CatGold
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ====== 猫的状态卡片 ======
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 猫的大头像
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CatOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.moodEmoji,
                                fontSize = 48.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cat.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cat.levelTitle} Lv.${cat.level}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CatGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 经验条
                        val expProgress = cat.expProgress
                        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                            drawRect(
                                color = Color.Gray.copy(alpha = 0.2f),
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height)
                            )
                            drawRect(
                                color = CatGold,
                                topLeft = Offset.Zero,
                                size = Size(size.width * expProgress, size.height)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EXP ${cat.exp}/${cat.expToNext}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // 猫的对话气泡
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CatOrange.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = cat.moodText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ====== 状态条 ======
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatBar(
                            label = "饱食度",
                            value = cat.hunger,
                            color = if (cat.hunger < 30) CatDanger else CatSafe,
                            emoji = "🍖"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(
                            label = "心情",
                            value = cat.happiness,
                            color = if (cat.happiness < 30) CatWarning else CatSafe,
                            emoji = "😊"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(
                            label = "精力",
                            value = cat.energy,
                            color = if (cat.energy < 30) CatWarning else CatSafe,
                            emoji = "⚡"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(
                            label = "清洁度",
                            value = cat.cleanliness,
                            color = if (cat.cleanliness < 30) CatWarning else CatSafe,
                            emoji = "✨"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "⚖️ 体重: ${String.format("%.1f", cat.weight)}kg (${cat.weightStatus})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (cat.isOverweight) CatWarning else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "💕 亲密度: ${cat.intimacy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CatPink
                            )
                        }
                    }
                }
            }

            // ====== 互动操作 ======
            item {
                Text(
                    "互动",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 撸猫类
            item {
                Text(
                    "🤚 撸猫",
                    style = MaterialTheme.typography.labelLarge,
                    color = CatOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        action = CatAction.PET_HEAD,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "摸头 +${action.intimacyBonus}💕"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.SCRATCH_CHIN,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "挠下巴 +${action.intimacyBonus}💕"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.RUB_BELLY,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            val success = Random().nextFloat() > 0.3f
                            showActionMessage = if (success) "撸肚子成功 +${action.intimacyBonus}💕" else "猫翻脸了！😾"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 喂食类
            item {
                Text(
                    "🍖 喂食",
                    style = MaterialTheme.typography.labelLarge,
                    color = CatOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        action = CatAction.FEED_FOOD,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "喂猫粮 饱食度+30"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.FEED_SNACK,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "喂零食 饱食度+20 心情+"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.FEED_CAN,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "喂罐头 饱食度+40"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 玩耍类
            item {
                Text(
                    "🪶 玩耍",
                    style = MaterialTheme.typography.labelLarge,
                    color = CatOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        action = CatAction.PLAY_CAT_TEASE,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "逗猫棒 心情+${action.happinessBonus}"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.PLAY_BALL,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "毛线球 心情+${action.happinessBonus}"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.PLAY_LASER,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "激光笔 心情+${action.happinessBonus}"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 清洁类
            item {
                Text(
                    "🛁 清洁",
                    style = MaterialTheme.typography.labelLarge,
                    color = CatOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        action = CatAction.CLEAN_BATH,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "洗澡完成 清洁度+50"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.CLEAN_BRUSH,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "梳毛完成 清洁度+30"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.SLEEP,
                        cat = cat,
                        onAction = { action ->
                            onAction(action)
                            showActionMessage = "猫睡着了 精力恢复"
                            showMessageTimestamp = System.currentTimeMillis()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ====== 操作反馈消息 ======
            item {
                AnimatedVisibility(visible = showActionMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = CatOrange.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = showActionMessage,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CatOrange
                        )
                    }
                }
            }

            // ====== 今日数据 ======
            item {
                Text(
                    "今日数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(
                        title = "互动",
                        value = "${todaySummary.interactionCount}次",
                        modifier = Modifier.weight(1f),
                        emoji = "🤚"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "喂食",
                        value = "${todaySummary.feedCount}次",
                        modifier = Modifier.weight(1f),
                        emoji = "🍖"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "玩耍",
                        value = "${todaySummary.playCount}次",
                        modifier = Modifier.weight(1f),
                        emoji = "🪶"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(
                        title = "亲密度",
                        value = "+${todaySummary.totalIntimacyGain}",
                        modifier = Modifier.weight(1f),
                        emoji = "💕"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "金币",
                        value = "+${todaySummary.coinsEarned}",
                        modifier = Modifier.weight(1f),
                        emoji = "💰"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(
                        title = "清洁",
                        value = "${todaySummary.cleanCount}次",
                        modifier = Modifier.weight(1f),
                        emoji = "🛁"
                    )
                }
            }

            // ====== 最近记录 ======
            item {
                Text(
                    "最近记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (recentRecords.isEmpty()) {
                item {
                    EmptyStateView(
                        emoji = "🐱",
                        title = "还没有撸猫记录",
                        subtitle = "快和你的小猫互动吧！"
                    )
                }
            } else {
                val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                items(recentRecords.takeLast(10).reversed(), key = { it.id }) { record ->
                    val action = try { CatAction.valueOf(record.actionType) } catch (e: Exception) { null }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = action?.emoji ?: "🐱",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = action?.displayName ?: record.actionType,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatter.format(Date(record.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (record.intimacyAfter > record.intimacyBefore) {
                                Text(
                                    text = "+${record.intimacyAfter - record.intimacyBefore}💕",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CatPink
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ActionButton(
    action: CatAction,
    cat: Cat,
    onAction: (CatAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { CatRepository(androidx.compose.ui.platform.LocalContext.current) }
    val isOnCooldown = remember(cat.lastInteractionTime, cat.lastFeedTime, cat.lastPlayTime, cat.lastCleanTime) {
        repository.isActionOnCooldown(action, cat)
    }
    val cooldownRemaining = if (isOnCooldown) repository.getCooldownRemaining(action, cat) else 0L

    FilledTonalButton(
        onClick = { if (!isOnCooldown) onAction(action) },
        modifier = modifier.height(56.dp),
        enabled = !isOnCooldown,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = action.emoji, fontSize = 18.sp)
            Text(
                text = action.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            if (isOnCooldown && cooldownRemaining > 0) {
                CooldownTimer(remainingMs = cooldownRemaining)
            }
        }
    }
}
