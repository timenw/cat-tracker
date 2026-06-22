package com.timenw.cattracker.ui.screens

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
import com.timenw.cattracker.ui.components.EmptyStateView
import com.timenw.cattracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryTab(
    cat: Cat,
    onUseItem: (ShopItem) -> Unit,
    onWearSkin: (Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.FOOD) }
    val inventory = remember(cat.inventory) { parseInventory(cat.inventory) }

    // 获取当前分类的仓库物品
    val filteredItems = remember(inventory, selectedCategory) {
        ALL_SHOP_ITEMS.filter { item ->
            item.category == selectedCategory && inventory[item.id] != null && inventory[item.id]!! > 0
        }.map { item -> item to (inventory[item.id] ?: 0) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AllInbox, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("我的仓库", fontWeight = FontWeight.Bold)
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

        if (filteredItems.isEmpty()) {
            EmptyStateView(
                emoji = "📦",
                title = "仓库空空如也",
                subtitle = "去商店购买物品吧！\n购买后会存放在这里"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(filteredItems, key = { it.first.id }) { (item, count) ->
                    val isSkin = item.category == ShopCategory.SKIN
                    val isWearing = isSkin && cat.skinId == item.id

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWearing) CatGold.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 物品图标
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape)
                                    .background(
                                        if (isWearing) CatGold.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.emoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    if (isWearing) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("已穿戴", fontSize = 10.sp, color = CatGold, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(text = item.effect, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "数量: $count", style = MaterialTheme.typography.labelSmall, color = CatOrange)
                            }
                            if (isSkin) {
                                if (isWearing) {
                                    Text("✅", fontSize = 20.sp)
                                } else {
                                    Button(
                                        onClick = { onWearSkin(item.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CatGold),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("穿戴", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onUseItem(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CatOrange),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("使用", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
