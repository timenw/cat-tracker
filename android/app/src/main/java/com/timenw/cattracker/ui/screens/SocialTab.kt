package com.timenw.cattracker.ui.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.data.SocialManager
import com.timenw.cattracker.data.model.Cat
import com.timenw.cattracker.data.model.CatBreed
import com.timenw.cattracker.ui.components.EmptyStateView
import com.timenw.cattracker.ui.components.SummaryCard
import com.timenw.cattracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialTab(
    cat: Cat,
    socialManager: SocialManager,
    onShowRewardedAd: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("晒猫", "猫友圈", "排行榜", "配对")

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("社交", fontWeight = FontWeight.Bold)
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
            0 -> ShareCardContent(cat, socialManager)
            1 -> CatFriendsContent(socialManager)
            2 -> LeaderboardContent(cat, socialManager)
            3 -> MatchContent(cat, socialManager)
        }
    }
}

/**
 * 晒猫卡片
 */
@Composable
fun ShareCardContent(cat: Cat, socialManager: SocialManager) {
    val context = LocalContext.current
    var shareMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 卡片预览
        item {
            Text("📸 晒猫卡片", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🐱 撸了喵", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CatOrange)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(cat.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("${cat.levelTitle} Lv.${cat.level}", fontSize = 16.sp, color = CatGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(cat.moodEmoji, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                    ) {
                        Text(
                            text = cat.moodText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💕", fontSize = 20.sp)
                            Text("${cat.intimacy}", fontWeight = FontWeight.Bold, color = CatPink)
                            Text("亲密度", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🍖", fontSize = 20.sp)
                            Text("${cat.hunger}", fontWeight = FontWeight.Bold, color = CatSafe)
                            Text("饱食度", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😊", fontSize = 20.sp)
                            Text("${cat.happiness}", fontWeight = FontWeight.Bold, color = CatWarning)
                            Text("心情", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚖️", fontSize = 20.sp)
                            Text("${String.format("%.1f", cat.weight)}", fontWeight = FontWeight.Bold, color = CatTeal)
                            Text("体重", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 分享按钮
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val path = socialManager.generateShareCard(cat)
                            socialManager.shareImage(path, "来看看我的猫！${cat.name} ${cat.moodText} 🐱 #撸了喵")
                            shareMessage = "分享成功！"
                        } catch (e: Exception) {
                            shareMessage = "分享失败: ${e.message}"
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CatOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("分享卡片", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        shareMessage = "卡片已保存到相册！"
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存卡片")
                }
            }
        }

        if (shareMessage.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatSafe.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = shareMessage,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        textAlign = TextAlign.Center,
                        color = CatSafe,
                        fontWeight = FontWeight.Medium
                    )
                }
                LaunchedEffect(shareMessage) {
                    kotlinx.coroutines.delay(3000)
                    shareMessage = ""
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * 猫友圈
 */
@Composable
fun CatFriendsContent(socialManager: SocialManager) {
    val friends = remember { socialManager.getCatFriends() }
    var likedIds by remember { mutableStateOf(setOf<Int>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text("🐈 猫友圈", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("看看其他铲屎官的猫", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(friends, key = { it.id }) { friend ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 猫头像
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape)
                            .background(CatOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(friend.moodEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(friend.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lv.${friend.level}", style = MaterialTheme.typography.labelSmall, color = CatGold)
                        }
                        Text("主人: ${friend.ownerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(friend.signature, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            likedIds = if (friend.id in likedIds) likedIds - friend.id else likedIds + friend.id
                        }) {
                            Icon(
                                if (friend.id in likedIds) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "点赞",
                                tint = if (friend.id in likedIds) CatDanger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${friend.likes + if (friend.id in likedIds) 1 else 0}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * 排行榜
 */
@Composable
fun LeaderboardContent(cat: Cat, socialManager: SocialManager) {
    val leaderboard = remember { socialManager.getLeaderboard(cat) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text("🏆 撸猫排行榜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("亲密度 + 互动次数 + 等级 综合评分", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(leaderboard, key = { it.rank }) { entry ->
            val isMe = entry.ownerName == "我"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe) CatOrange.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 排名
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(
                                when (entry.rank) {
                                    1 -> Color(0xFFFFD700)
                                    2 -> Color(0xFFC0C0C0)
                                    3 -> Color(0xFFCD7F32)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${entry.rank}",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (entry.rank <= 3) 16.sp else 14.sp,
                            color = if (entry.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(entry.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.catName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMe) CatOrange else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            entry.ownerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${entry.score}分",
                        fontWeight = FontWeight.Bold,
                        color = if (isMe) CatOrange else CatGold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/**
 * 猫配对
 */
@Composable
fun MatchContent(myCat: Cat, socialManager: SocialManager) {
    val friends = remember { socialManager.getCatFriends() }
    var selectedFriend by remember { mutableStateOf<SocialManager.CatFriend?>(null) }
    var matchResult by remember { mutableStateOf<SocialManager.MatchResult?>(null) }
    var showResult by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text("💕 猫配对", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("选择一只猫进行配对，看看能不能生出小猫！", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // 我的猫
        item {
            Text("我的猫", style = MaterialTheme.typography.labelLarge, color = CatOrange)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(myCat.moodEmoji, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(myCat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${myCat.levelTitle} Lv.${myCat.level} · 亲密度${myCat.intimacy}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 选择配对对象
        item {
            Text("选择配对对象", style = MaterialTheme.typography.labelLarge, color = CatOrange)
        }

        items(friends, key = { it.id }) { friend ->
            val isSelected = selectedFriend?.id == friend.id
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CatPink.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                onClick = { selectedFriend = if (isSelected) null else friend }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(friend.moodEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(friend.name, fontWeight = FontWeight.Bold)
                        Text("Lv.${friend.level} · ${friend.breed.displayName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("主人: ${friend.ownerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CatPink)
                    }
                }
            }
        }

        // 配对按钮
        item {
            Button(
                onClick = {
                    selectedFriend?.let { friend ->
                        val friendCat = Cat(
                            name = friend.name,
                            breed = friend.breed,
                            level = friend.level,
                            intimacy = friend.intimacy
                        )
                        matchResult = socialManager.matchCats(myCat, friendCat)
                        showResult = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = selectedFriend != null,
                colors = ButtonDefaults.buttonColors(containerColor = CatPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💕 开始配对", fontWeight = FontWeight.Bold)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // 配对结果对话框
    if (showResult && matchResult != null) {
        val result = matchResult!!
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = {
                Text(if (result.success) "🎉 配对成功！" else "😿 配对失败")
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(result.message, textAlign = TextAlign.Center)
                    if (result.success) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(result.babyEmoji, fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("宝宝: ${result.babyName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("品种: ${result.babyBreed.displayName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showResult = false }) {
                    Text("确定")
                }
            }
        )
    }
}
