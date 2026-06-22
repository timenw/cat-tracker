package com.timenw.cattracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timenw.cattracker.data.SoundManager
import com.timenw.cattracker.data.model.CatAction
import com.timenw.cattracker.data.model.CatBreed
import com.timenw.cattracker.data.model.UserSettings
import com.timenw.cattracker.ui.theme.CatOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    settings: UserSettings,
    soundManager: SoundManager,
    isPremium: Boolean = false,
    onSettingsChanged: (UserSettings) -> Unit
) {
    var showCatInfoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                                Text(settings.catName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { showCatInfoDialog = true }) { Text("修改") }
                        }
                        Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(8.dp))
                        Text("品种", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(try { CatBreed.valueOf(settings.catBreed).displayName } catch (e: Exception) { "未知" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("生日", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(settings.catBirthday, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 提醒设置
            item { Text("提醒设置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SwitchRow("撸猫提醒", if (settings.reminderEnabled) "已开启 — 定期提醒你撸猫" else "已关闭", settings.reminderEnabled) { onSettingsChanged(settings.copy(reminderEnabled = it)) }
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        SwitchRow("喂食提醒", if (settings.feedReminderEnabled) "已开启" else "已关闭", settings.feedReminderEnabled) { onSettingsChanged(settings.copy(feedReminderEnabled = it)) }
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        SwitchRow("玩耍提醒", if (settings.playReminderEnabled) "已开启" else "已关闭", settings.playReminderEnabled) { onSettingsChanged(settings.copy(playReminderEnabled = it)) }
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        SwitchRow("清洁提醒", if (settings.cleanReminderEnabled) "已开启" else "已关闭", settings.cleanReminderEnabled) { onSettingsChanged(settings.copy(cleanReminderEnabled = it)) }
                    }
                }
            }

            // 音效与震动
            item { Text("音效与震动", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SwitchRow("音效", if (settings.soundEnabled) "已开启" else "已关闭", settings.soundEnabled) {
                            onSettingsChanged(settings.copy(soundEnabled = it))
                            soundManager.setSoundEnabled(it)
                        }
                        if (settings.soundEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { soundManager.playActionSound(CatAction.PET_HEAD) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("🔊 摸头音效") }
                                OutlinedButton(
                                    onClick = { soundManager.playActionSound(CatAction.FEED_FOOD) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("🔊 喂食音效") }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { soundManager.playActionSound(CatAction.PLAY_CAT_TEASE) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("🔊 玩耍音效") }
                                OutlinedButton(
                                    onClick = { soundManager.playAchievementSound() },
                                    modifier = Modifier.weight(1f)
                                ) { Text("🔊 成就音效") }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(); Spacer(modifier = Modifier.height(12.dp))
                        SwitchRow("震动", if (settings.vibrationEnabled) "已开启" else "已关闭", settings.vibrationEnabled) {
                            onSettingsChanged(settings.copy(vibrationEnabled = it))
                            soundManager.setVibrationEnabled(it)
                        }
                        if (settings.vibrationEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { soundManager.playActionSound(CatAction.PET_HEAD) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("📳 短震动") }
                                OutlinedButton(
                                    onClick = { soundManager.playActionSound(CatAction.PLAY_CAT_TEASE) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("📳 长震动") }
                            }
                        }
                    }
                }
            }

            // 关于
            item { Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("撸了喵", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("版本 1.2.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("一款专为爱猫人士设计的电子宠物猫养成软件。撸猫、喂食、玩耍、清洁，让你的小猫健康快乐成长！", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🐱 愿每一只猫都被温柔以待 🐱", style = MaterialTheme.typography.bodySmall, color = CatOrange, fontWeight = FontWeight.Medium)
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
                    OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("猫的名字") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("品种", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(expanded = showBreedDropdown, onExpandedChange = { showBreedDropdown = it }) {
                        OutlinedTextField(
                            value = try { CatBreed.valueOf(tempBreed).displayName } catch (e: Exception) { "未知" },
                            onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = showBreedDropdown, onDismissRequest = { showBreedDropdown = false }) {
                            CatBreed.entries.forEach { breed ->
                                val locked = breed.isPremium && !isPremium
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${breed.emoji} ${breed.displayName}")
                                            if (locked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("🔒", fontSize = 12.sp)
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (!locked) {
                                            tempBreed = breed.name
                                            showBreedDropdown = false
                                        }
                                    },
                                    enabled = !locked
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onSettingsChanged(settings.copy(catName = tempName.ifEmpty { "小咪" }, catBreed = tempBreed)); showCatInfoDialog = false }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showCatInfoDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

