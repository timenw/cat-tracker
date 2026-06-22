package com.timenw.cattracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.data.model.*
import com.timenw.cattracker.ui.components.EmptyStateView
import com.timenw.cattracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopTab(
    cat: Cat,
    isPremium: Boolean = false,
    onBuyItem: (ShopItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.FOOD) }
    var showPurchaseDialog by remember { mutableStateOf<ShopItem?>(null) }
    var purchaseMessage by remember { mutableStateOf("") }

    val filteredItems = ALL_SHOP_ITEMS.filter { it.category == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("商店", fontWeight = FontWeight.Bold)
                }
            },
            actions = {
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

        // 分类选择
        ScrollableTabRow(
            selectedTabIndex = ShopCategory.entries.indexOf(selectedCategory),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp
        ) {
            ShopCategory.entries.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text("${category.emoji} ${category.displayName}") }
                )
            }
        }

        // 购买成功消息
        if (purchaseMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CatSafe.copy(alpha = 0.15f))
            ) {
                Text(
                    text = purchaseMessage,
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = CatSafe
                )
            }
            LaunchedEffect(purchaseMessage) {
                kotlinx.coroutines.delay(2000)
                purchaseMessage = ""
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(filteredItems, key = { it.id }) { item ->
                val isOwned = item.isDefault || isItemOwned(item, cat)
                val canAfford = cat.coins >= item.price

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOwned)
                            CatGold.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 物品图标
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isOwned) CatGold.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.effect,
                                style = MaterialTheme.typography.labelSmall,
                                color = CatOrange
                            )
                        }
                        if (isOwned) {
                            Text("已拥有", style = MaterialTheme.typography.labelMedium, color = CatGold, fontWeight = FontWeight.Bold)
                        } else {
                            // 会员购买皮肤免费
                            val isFreeForPremium = isPremium && item.category == ShopCategory.SKIN && !item.isDefault
                            val canBuy = isFreeForPremium || canAfford
                            Button(
                                onClick = { showPurchaseDialog = item },
                                enabled = canBuy,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFreeForPremium) CatGold else CatOrange,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                if (isFreeForPremium) {
                                    Text("👑 免费", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("💰 ${item.price}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // 购买确认对话框
    showPurchaseDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = null },
            title = { Text("购买 ${item.name}") },
            text = {
                Column {
                    Text("${item.emoji} ${item.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    val isFreeForPremium = isPremium && item.category == ShopCategory.SKIN && !item.isDefault
                    if (isFreeForPremium) {
                        Text("👑 会员免费", fontWeight = FontWeight.Bold, color = CatGold)
                    } else {
                        Text("价格: 💰 ${item.price}", fontWeight = FontWeight.Bold)
                    }
                    Text("当前金币: 💰 ${cat.coins}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBuyItem(item)
                        purchaseMessage = if (isPremium && item.category == ShopCategory.SKIN && !item.isDefault)
                            "👑 会员免费获得！${item.emoji} ${item.name}"
                        else "购买成功！${item.emoji} ${item.name}"
                        showPurchaseDialog = null
                    }
                ) {
                    Text(if (isPremium && item.category == ShopCategory.SKIN && !item.isDefault) "免费领取" else "购买")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

// 检查物品是否已拥有
private fun isItemOwned(item: ShopItem, cat: Cat): Boolean {
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
