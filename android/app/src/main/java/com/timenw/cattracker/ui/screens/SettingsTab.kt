package com.timenw.cattracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timenw.cattracker.data.model.CatBreed
import com.timenw.cattracker.data.model.UserSettings
import com.timenw.cattracker.ui.theme.CatOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    settings: UserSettings,
    onSettingsChanged: (UserSettings) -> Unit
) {
    var showCatInfoDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("设置", fontWeight = FontWeight.Bold)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 猫的信息
            item {
                Text("猫的信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("猫的名字", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    settings.catName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showCatInfoDialog = true }) {
                                Text("修改")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("品种", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            try { CatBreed.valueOf(settings.catBreed).displayName } catch (e: Exception) { "未知" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("生日", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            settings.catBirthday,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 提醒设置
            item {
                Text("提醒设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("撸猫提醒", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.reminderEnabled) "已开启 — 定期提醒你撸猫" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.reminderEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(reminderEnabled = it)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("喂食提醒", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.feedReminderEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.feedReminderEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(feedReminderEnabled = it)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("玩耍提醒", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.playReminderEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.playReminderEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(playReminderEnabled = it)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("清洁提醒", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.cleanReminderEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.cleanReminderEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(cleanReminderEnabled = it)) }
                            )
                        }
                    }
                }
            }

            // 其他设置
            item {
                Text("其他设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("音效", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.soundEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.soundEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(soundEnabled = it)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("震动", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    text = if (settings.vibrationEnabled) "已开启" else "已关闭",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = { onSettingsChanged(settings.copy(vibrationEnabled = it)) }
                            )
                        }
                    }
                }
            }

            // 关于
            item {
                Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("撸了喵", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("版本 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "一款专为爱猫人士设计的电子宠物猫养成软件。撸猫、喂食、玩耍、清洁，让你的小猫健康快乐成长！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🐱 愿每一只猫都被温柔以待 🐱",
                            style = MaterialTheme.typography.bodySmall,
                            color = CatOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // 修改猫信息对话框
    if (showCatInfoDialog) {
        var tempName by remember { mutableStateOf(settings.catName) }
        var tempBreed by remember { mutableStateOf(settings.catBreed) }
        var showBreedDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCatInfoDialog = false },
            title = { Text("修改猫的信息") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("猫的名字") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("品种", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showBreedDropdown,
                        onExpandedChange = { showBreedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = try { CatBreed.valueOf(tempBreed).displayName } catch (e: Exception) { "未知" },
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showBreedDropdown,
                            onDismissRequest = { showBreedDropdown = false }
                        ) {
                            CatBreed.entries.forEach { breed ->
                                DropdownMenuItem(
                                    text = { Text("${breed.emoji} ${breed.displayName}") },
                                    onClick = {
                                        tempBreed = breed.name
                                        showBreedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSettingsChanged(
                            settings.copy(
                                catName = tempName.ifEmpty { "小咪" },
                                catBreed = tempBreed
                            )
                        )
                        showCatInfoDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCatInfoDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
