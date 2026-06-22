package com.timenw.cattracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
    onSettingsChanged: (UserSettings) -> Unit,
    onShowRewardedAd: () -> Unit = {}
) {
    var showActionMessage by remember { mutableStateOf("") }
    var showMessageTimestamp by remember { mutableStateOf(0L) }
    var currentAnim by remember { mutableStateOf(cat.currentAnimation) }
    var pendingAction by remember { mutableStateOf<CatAction?>(null) }
    var showAdDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val repository = remember { CatRepository(context) }

    // 动画状态
    LaunchedEffect(showMessageTimestamp) {
        if (showMessageTimestamp > 0) {
            currentAnim = cat.currentAnimation
            kotlinx.coroutines.delay(1500)
            currentAnim = "idle"
            showActionMessage = ""
        }
    }

    // 看广告解锁对话框
    if (showAdDialog && pendingAction != null) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false; pendingAction = null },
            title = { Text("今日免费次数已用完") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐱", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "每项互动每天可免费使用 ${FREE_DAILY_ACTIONS} 次",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "观看一段广告即可继续互动",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CatOrange,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdDialog = false
                        val action = pendingAction
                        pendingAction = null
                        // 执行互动（广告已看完）
                        if (action != null) {
                            onAction(action)
                            showActionMessage = "广告观看成功！互动完成 ✨"
                            showMessageTimestamp = System.currentTimeMillis()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CatOrange)
                ) {
                    Text("📺 观看广告")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdDialog = false; pendingAction = null }) {
                    Text("取消")
                }
            }
        )
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    Text("💰", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${cat.coins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CatGold)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ====== 猫的状态卡片（含动画） ======
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // 动画猫形象
                        AnimatedCatView(cat = cat, animation = currentAnim)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(cat.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${cat.levelTitle} Lv.${cat.level}", style = MaterialTheme.typography.bodyMedium, color = CatGold)
                        Spacer(modifier = Modifier.height(4.dp))
                        // 经验条
                        val expProgress = cat.expProgress
                        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                            drawRect(color = Color.Gray.copy(alpha = 0.2f), topLeft = Offset.Zero, size = Size(size.width, size.height))
                            drawRect(color = CatGold, topLeft = Offset.Zero, size = Size(size.width * expProgress, size.height))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("EXP ${cat.exp}/${cat.expToNext}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        // 对话气泡
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.15f))) {
                            Text(text = cat.moodText, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // ====== 状态条 ======
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatBar(label = "饱食度", value = cat.hunger, color = if (cat.hunger < 30) CatDanger else CatSafe, emoji = "🍖")
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(label = "心情", value = cat.happiness, color = if (cat.happiness < 30) CatWarning else CatSafe, emoji = "😊")
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(label = "精力", value = cat.energy, color = if (cat.energy < 30) CatWarning else CatSafe, emoji = "⚡")
                        Spacer(modifier = Modifier.height(8.dp))
                        StatBar(label = "清洁度", value = cat.cleanliness, color = if (cat.cleanliness < 30) CatWarning else CatSafe, emoji = "✨")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("⚖️ 体重: ${String.format("%.1f", cat.weight)}kg (${cat.weightStatus})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (cat.isOverweight) CatWarning else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("💕 亲密度: ${cat.intimacy}", style = MaterialTheme.typography.bodySmall, color = CatPink)
                        }
                    }
                }
            }

            // ====== 互动操作 ======
            item { Text("互动", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }

            item {
                Text("🤚 撸猫", style = MaterialTheme.typography.labelLarge, color = CatOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        action = CatAction.PET_HEAD, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "摸头 +${action.intimacyBonus}💕"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.SCRATCH_CHIN, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "挠下巴 +${action.intimacyBonus}💕"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.RUB_BELLY, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { val ok = Random().nextFloat() > 0.3f; showActionMessage = if (ok) "撸肚子成功 +${action.intimacyBonus}💕" else "猫翻脸了！😾"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("🍖 喂食", style = MaterialTheme.typography.labelLarge, color = CatOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        action = CatAction.FEED_FOOD, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "喂猫粮 饱食度+30"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.FEED_SNACK, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "喂零食 饱食度+20"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.FEED_CAN, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "喂罐头 饱食度+40"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("🪶 玩耍", style = MaterialTheme.typography.labelLarge, color = CatOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        action = CatAction.PLAY_CAT_TEASE, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "逗猫棒 心情+${action.happinessBonus}"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.PLAY_BALL, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "毛线球 心情+${action.happinessBonus}"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.PLAY_LASER, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "激光笔 心情+${action.happinessBonus}"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("🛁 清洁", style = MaterialTheme.typography.labelLarge, color = CatOrange)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        action = CatAction.CLEAN_BATH, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "洗澡完成 清洁度+50"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.CLEAN_BRUSH, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "梳毛完成 清洁度+30"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                    ActionButton(
                        action = CatAction.SLEEP, cat = cat, repository = repository,
                        onAction = { action -> triggerAction(action, onAction) { showActionMessage = "猫睡着了 精力恢复"; showMessageTimestamp = System.currentTimeMillis() } },
                        onNeedAd = { pendingAction = it; showAdDialog = true },
                        Modifier.weight(1f)
                    )
                }
            }

            // 操作反馈
            item {
                AnimatedVisibility(visible = showActionMessage.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.2f))) {
                        Text(text = showActionMessage, modifier = Modifier.fillMaxWidth().padding(12.dp),
                            textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = CatOrange)
                    }
                }
            }

            // 今日数据
            item {
                Text("今日数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(title = "互动", value = "${todaySummary.interactionCount}次", modifier = Modifier.weight(1f), emoji = "🤚")
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(title = "喂食", value = "${todaySummary.feedCount}次", modifier = Modifier.weight(1f), emoji = "🍖")
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(title = "玩耍", value = "${todaySummary.playCount}次", modifier = Modifier.weight(1f), emoji = "🪶")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    SummaryCard(title = "亲密度", value = "+${todaySummary.totalIntimacyGain}", modifier = Modifier.weight(1f), emoji = "💕")
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(title = "金币", value = "+${todaySummary.coinsEarned}", modifier = Modifier.weight(1f), emoji = "💰")
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(title = "清洁", value = "${todaySummary.cleanCount}次", modifier = Modifier.weight(1f), emoji = "🛁")
                }
            }

            // 最近记录
            item { Text("最近记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }

            if (recentRecords.isEmpty()) {
                item { EmptyStateView(emoji = "🐱", title = "还没有撸猫记录", subtitle = "快和你的小猫互动吧！") }
            } else {
                items(recentRecords.takeLast(10).reversed(), key = { it.id }) { record ->
                    val action = try { CatAction.valueOf(record.actionType) } catch (e: Exception) { null }
                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = action?.emoji ?: "🐱", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = action?.displayName ?: record.actionType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(text = formatter.format(Date(record.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (record.intimacyAfter > record.intimacyBefore) {
                                Text(text = "+${record.intimacyAfter - record.intimacyBefore}💕", style = MaterialTheme.typography.labelMedium, color = CatPink)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

private fun triggerAction(action: CatAction, onAction: (CatAction) -> Unit, onComplete: () -> Unit) {
    onAction(action)
    onComplete()
}

/**
 * 互动按钮 — 显示剩余免费次数，超出后触发广告
 */
@Composable
fun ActionButton(
    action: CatAction, cat: Cat,
    repository: CatRepository,
    onAction: (CatAction) -> Unit,
    onNeedAd: (CatAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnCooldown = remember(cat.lastInteractionTime, cat.lastFeedTime, cat.lastPlayTime, cat.lastCleanTime) {
        repository.isActionOnCooldown(action, cat)
    }
    val cooldownRemaining = if (isOnCooldown) repository.getCooldownRemaining(action, cat) else 0L
    val freeRemaining = remember(cat.dailyActionCounts, cat.dailyActionDate) {
        repository.getFreeUsesRemaining(cat, action)
    }
    val needsAd = remember(cat.dailyActionCounts, cat.dailyActionDate) {
        repository.needsAdForAction(cat, action)
    }

    FilledTonalButton(
        onClick = {
            if (!isOnCooldown) {
                if (needsAd) {
                    onNeedAd(action)
                } else {
                    onAction(action)
                }
            }
        },
        modifier = modifier.height(64.dp),
        enabled = !isOnCooldown,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (needsAd) CatOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = action.emoji, fontSize = 18.sp)
            Text(text = action.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            if (isOnCooldown && cooldownRemaining > 0) {
                CooldownTimer(remainingMs = cooldownRemaining)
            } else if (needsAd) {
                Text(text = "📺 看广告", style = MaterialTheme.typography.labelSmall, color = CatOrange, fontWeight = FontWeight.Bold)
            } else {
                Text(text = "剩余${freeRemaining}次", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ==================== 以下为动画猫形象代码（保持不变） ====================

@Composable
fun AnimatedCatView(cat: Cat, animation: String) {
    val bounceAnim = rememberInfiniteTransition(label = "bounce")
    val bounceY by bounceAnim.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bounceY"
    )

    val breathAnim = rememberInfiniteTransition(label = "breath")
    val breathScale by breathAnim.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breathScale"
    )

    val rotationAnim = rememberInfiniteTransition(label = "rotation")
    val rotation by rotationAnim.animateFloat(
        initialValue = -5f, targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "rotation"
    )

    val catColor = Color(0xFFFFB74D)

    val (scale, offsetY, rot, bgColor) = when (animation) {
        "happy" -> listOf(1.15f, bounceY * 1.5f, 0f, CatOrange.copy(alpha = 0.25f))
        "eating" -> listOf(1.05f, 0f, 0f, CatSafe.copy(alpha = 0.2f))
        "playing" -> listOf(1.1f, bounceY * 2f, rotation, CatPurple.copy(alpha = 0.2f))
        "bathing" -> listOf(0.95f, 0f, rotation * 0.5f, CatTeal.copy(alpha = 0.2f))
        "sleeping" -> listOf(1f, 4f, 0f, Color(0xFF3E2723).copy(alpha = 0.15f))
        else -> listOf(breathScale, bounceY, 0f, catColor.copy(alpha = 0.2f))
    }

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale as Float)
                .graphicsLayer { translationY = offsetY as Float; rotationZ = rot as Float }
                .clip(CircleShape)
                .background(bgColor as Color),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(80.dp)) {
                drawCatFace(animation)
            }
        }
    }
}

fun DrawScope.drawCatFace(animation: String) {
    val cx = size.width / 2
    val cy = size.height / 2
    val r = size.width / 2
    val faceColor = Color(0xFFFFB74D)

    // 脸
    drawCircle(color = faceColor, radius = r * 0.9f, center = Offset(cx, cy))

    // 耳朵
    val earSize = r * 0.3f
    val earOffset = r * 0.5f
    drawCircle(color = faceColor, radius = earSize, center = Offset(cx - earOffset, cy - earOffset))
    drawCircle(color = Color(0xFFFFAB91), radius = earSize * 0.6f, center = Offset(cx - earOffset, cy - earOffset))
    drawCircle(color = faceColor, radius = earSize, center = Offset(cx + earOffset, cy - earOffset))
    drawCircle(color = Color(0xFFFFAB91), radius = earSize * 0.6f, center = Offset(cx + earOffset, cy - earOffset))

    // 眼睛
    val eyeY = cy - r * 0.1f
    val eyeSpacing = r * 0.35f
    val eyeR = r * 0.12f

    when (animation) {
        "sleeping" -> {
            drawLine(color = Color(0xFF5D4037), start = Offset(cx - eyeSpacing - eyeR, eyeY), end = Offset(cx - eyeSpacing + eyeR, eyeY), strokeWidth = 3f)
            drawLine(color = Color(0xFF5D4037), start = Offset(cx + eyeSpacing - eyeR, eyeY), end = Offset(cx + eyeSpacing + eyeR, eyeY), strokeWidth = 3f)
        }
        "happy", "playing" -> {
            drawCircle(color = Color(0xFF5D4037), radius = eyeR * 1.2f, center = Offset(cx - eyeSpacing, eyeY))
            drawCircle(color = Color(0xFF5D4037), radius = eyeR * 1.2f, center = Offset(cx + eyeSpacing, eyeY))
            drawCircle(color = Color.White, radius = eyeR * 0.4f, center = Offset(cx - eyeSpacing + 2, eyeY - 3))
            drawCircle(color = Color.White, radius = eyeR * 0.4f, center = Offset(cx + eyeSpacing + 2, eyeY - 3))
        }
        else -> {
            drawCircle(color = Color.White, radius = eyeR, center = Offset(cx - eyeSpacing, eyeY))
            drawCircle(color = Color.White, radius = eyeR, center = Offset(cx + eyeSpacing, eyeY))
            drawCircle(color = Color(0xFF5D4037), radius = eyeR * 0.6f, center = Offset(cx - eyeSpacing, eyeY))
            drawCircle(color = Color(0xFF5D4037), radius = eyeR * 0.6f, center = Offset(cx + eyeSpacing, eyeY))
            drawCircle(color = Color.White, radius = eyeR * 0.25f, center = Offset(cx - eyeSpacing + 2, eyeY - 2))
            drawCircle(color = Color.White, radius = eyeR * 0.25f, center = Offset(cx + eyeSpacing + 2, eyeY - 2))
        }
    }

    // 鼻子
    val noseY = cy + r * 0.1f
    drawCircle(color = Color(0xFFFFAB91), radius = r * 0.06f, center = Offset(cx, noseY))

    // 嘴巴
    val mouthY = cy + r * 0.25f
    val mouthW = r * 0.15f
    when (animation) {
        "happy", "playing", "eating" -> {
            drawLine(color = Color(0xFF5D4037), start = Offset(cx - mouthW, mouthY), end = Offset(cx, mouthY - 4), strokeWidth = 2.5f)
            drawLine(color = Color(0xFF5D4037), start = Offset(cx, mouthY - 4), end = Offset(cx + mouthW, mouthY), strokeWidth = 2.5f)
        }
        "bathing" -> {
            drawLine(color = Color(0xFF5D4037), start = Offset(cx - mouthW, mouthY + 3), end = Offset(cx, mouthY), strokeWidth = 2.5f)
            drawLine(color = Color(0xFF5D4037), start = Offset(cx, mouthY), end = Offset(cx + mouthW, mouthY + 3), strokeWidth = 2.5f)
        }
        else -> {
            drawLine(color = Color(0xFF5D4037), start = Offset(cx - mouthW, mouthY), end = Offset(cx + mouthW, mouthY), strokeWidth = 2.5f)
        }
    }

    // 胡须
    val whiskerY = cy + r * 0.15f
    val whiskerLen = r * 0.25f
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx - r * 0.7f, whiskerY - 3), end = Offset(cx - r * 0.7f - whiskerLen, whiskerY - 5), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx - r * 0.7f, whiskerY), end = Offset(cx - r * 0.7f - whiskerLen, whiskerY), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx - r * 0.7f, whiskerY + 3), end = Offset(cx - r * 0.7f - whiskerLen, whiskerY + 5), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx + r * 0.7f, whiskerY - 3), end = Offset(cx + r * 0.7f + whiskerLen, whiskerY - 5), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx + r * 0.7f, whiskerY), end = Offset(cx + r * 0.7f + whiskerLen, whiskerY), strokeWidth = 1.5f)
    drawLine(color = Color(0xFF8D6E63), start = Offset(cx + r * 0.7f, whiskerY + 3), end = Offset(cx + r * 0.7f + whiskerLen, whiskerY + 5), strokeWidth = 1.5f)

    // 睡觉时画 Zzz
    if (animation == "sleeping") {
        val zX = cx + r * 0.5f
        val zY = cy - r * 0.6f
        drawLine(color = Color(0xFF7C4DFF), start = Offset(zX, zY), end = Offset(zX + 8, zY), strokeWidth = 2f)
        drawLine(color = Color(0xFF7C4DFF), start = Offset(zX + 8, zY), end = Offset(zX, zY + 8), strokeWidth = 2f)
        drawLine(color = Color(0xFF7C4DFF), start = Offset(zX, zY + 8), end = Offset(zX + 8, zY + 8), strokeWidth = 2f)
    }

    // 玩耍时画星星
    if (animation == "playing") {
        val starColor = Color(0xFFFFD700)
        val starPositions = listOf(
            Offset(cx - r * 0.6f, cy - r * 0.5f),
            Offset(cx + r * 0.6f, cy - r * 0.4f),
            Offset(cx - r * 0.5f, cy + r * 0.4f)
        )
        starPositions.forEach { pos ->
            drawCircle(color = starColor, radius = 3f, center = pos)
        }
    }
}
