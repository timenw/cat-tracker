package com.timenw.cattracker.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.data.BillingManager
import com.timenw.cattracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTab(
    billingManager: BillingManager,
    onShowRewardedAd: () -> Unit
) {
    val context = LocalContext.current
    val isPremium by billingManager.isPremium.collectAsState()
    val adsRemoved by billingManager.adsRemoved.collectAsState()
    var purchaseMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = CatGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("会员中心", fontWeight = FontWeight.Bold)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 当前状态
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPremium) CatGold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isPremium) "👑 会员用户" else "🐱 免费用户",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) CatGold else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isPremium) "感谢支持！享受完整体验吧"
                            else "升级会员，解锁全部功能",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 会员特权
            item {
                Text("🎁 会员特权", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PremiumFeature("🚫", "移除所有广告", "纯净体验，无广告干扰", adsRemoved || isPremium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PremiumFeature("🐱", "解锁全部猫品种", "8种稀有品种任你选", isPremium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PremiumFeature("👗", "专属皮肤", "限定皮肤免费解锁", isPremium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PremiumFeature("💰", "金币加成", "互动获得双倍金币", isPremium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        PremiumFeature("🏆", "专属称号", "会员专属称号和徽章", isPremium)
                    }
                }
            }

            // 购买选项
            item {
                Text("💎 购买选项", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 移除广告
            item {
                PurchaseCard(
                    title = "移除广告",
                    description = "一次性购买，永久移除广告",
                    price = billingManager.getSkuPrice(BillingManager.SKU_REMOVE_ADS),
                    emoji = "🚫",
                    purchased = adsRemoved,
                    onClick = {
                        billingManager.purchase(context as Activity, BillingManager.SKU_REMOVE_ADS)
                    }
                )
            }

            // 月度会员
            item {
                PurchaseCard(
                    title = "月度会员",
                    description = "享受全部会员特权",
                    price = billingManager.getSkuPrice(BillingManager.SKU_PREMIUM_MONTHLY),
                    emoji = "👑",
                    purchased = isPremium,
                    onClick = {
                        billingManager.purchase(context as Activity, BillingManager.SKU_PREMIUM_MONTHLY)
                    },
                    isHighlighted = true
                )
            }

            // 金币包
            item {
                Text("💰 金币包", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CoinPackCard(
                        name = "小包",
                        coins = "100",
                        price = billingManager.getSkuPrice(BillingManager.SKU_COINS_100),
                        emoji = "🪙",
                        onClick = { billingManager.purchase(context as Activity, BillingManager.SKU_COINS_100) },
                        modifier = Modifier.weight(1f)
                    )
                    CoinPackCard(
                        name = "中包",
                        coins = "500",
                        price = billingManager.getSkuPrice(BillingManager.SKU_COINS_500),
                        emoji = "💰",
                        onClick = { billingManager.purchase(context as Activity, BillingManager.SKU_COINS_500) },
                        modifier = Modifier.weight(1f),
                        isHighlighted = true
                    )
                    CoinPackCard(
                        name = "大包",
                        coins = "1000",
                        price = billingManager.getSkuPrice(BillingManager.SKU_COINS_1000),
                        emoji = "💎",
                        onClick = { billingManager.purchase(context as Activity, BillingManager.SKU_COINS_1000) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 看广告得金币
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatOrange.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📺", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("看广告得金币", fontWeight = FontWeight.Bold)
                            Text("观看一段视频广告，获得50金币", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onShowRewardedAd,
                            colors = ButtonDefaults.buttonColors(containerColor = CatOrange),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("观看", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 购买消息
            if (purchaseMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CatSafe.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = purchaseMessage,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            textAlign = TextAlign.Center,
                            color = CatSafe,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LaunchedEffect(purchaseMessage) {
                        kotlinx.coroutines.delay(3000)
                        purchaseMessage = ""
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PremiumFeature(emoji: String, title: String, description: String, unlocked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            if (unlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (unlocked) CatSafe else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PurchaseCard(
    title: String,
    description: String,
    price: String,
    emoji: String,
    purchased: Boolean,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) CatGold.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (purchased) {
                Text("已购买", color = CatSafe, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighlighted) CatGold else CatOrange
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(price, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CoinPackCard(
    name: String,
    coins: String,
    price: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) CatGold.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(coins, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, fontWeight = FontWeight.Bold, color = CatOrange, style = MaterialTheme.typography.labelMedium)
        }
    }
}
