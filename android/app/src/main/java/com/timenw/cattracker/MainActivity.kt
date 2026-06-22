package com.timenw.cattracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timenw.cattracker.data.*
import com.timenw.cattracker.data.model.*
import com.timenw.cattracker.data.repository.CatRepository
import com.timenw.cattracker.notification.NotificationHelper
import com.timenw.cattracker.ui.screens.*
import com.timenw.cattracker.ui.theme.CatTrackerTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var repository: CatRepository
    private lateinit var soundManager: SoundManager
    private lateinit var adManager: AdManager
    private lateinit var billingManager: BillingManager
    private lateinit var socialManager: SocialManager

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = CatRepository(applicationContext)
        soundManager = SoundManager(applicationContext)
        adManager = AdManager(applicationContext)
        socialManager = SocialManager(applicationContext)
        billingManager = BillingManager(
            context = applicationContext,
            onPurchaseSuccess = { sku ->
                when (sku) {
                    BillingManager.SKU_REMOVE_ADS -> adManager.setAdsRemoved(true)
                    BillingManager.SKU_COINS_100 -> {
                        // 金币已通过 billingManager 处理
                    }
                }
            },
            onPurchaseError = { }
        )

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            CatTrackerTheme {
                MainScreen(repository, soundManager, adManager, billingManager, socialManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        billingManager.release()
    }
}

sealed class Screen(
    val route: String, val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
) {
    object Home : Screen("home", "猫窝", { Icon(Icons.Filled.Home, contentDescription = null) }, { Icon(Icons.Outlined.Home, contentDescription = null) })
    object Stats : Screen("stats", "统计", { Icon(Icons.Filled.BarChart, contentDescription = null) }, { Icon(Icons.Outlined.BarChart, contentDescription = null) })
    object Shop : Screen("shop", "商店", { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }, { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) })
    object Social : Screen("social", "社交", { Icon(Icons.Filled.Share, contentDescription = null) }, { Icon(Icons.Outlined.Share, contentDescription = null) })
    object Premium : Screen("premium", "会员", { Icon(Icons.Filled.WorkspacePremium, contentDescription = null) }, { Icon(Icons.Outlined.WorkspacePremium, contentDescription = null) })
    object Settings : Screen("settings", "设置", { Icon(Icons.Filled.Settings, contentDescription = null) }, { Icon(Icons.Outlined.Settings, contentDescription = null) })
}

@Composable
fun MainScreen(
    repository: CatRepository,
    soundManager: SoundManager,
    adManager: AdManager,
    billingManager: BillingManager,
    socialManager: SocialManager
) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Stats, Screen.Shop, Screen.Social, Screen.Premium, Screen.Settings)
    val context = LocalContext.current
    val today = remember { LocalDate.now() }

    // 会员状态
    val isPremium by billingManager.isPremium.collectAsState()
    val adsRemoved by billingManager.adsRemoved.collectAsState()

    var cat by remember { mutableStateOf(repository.getCat()) }
    var settings by remember { mutableStateOf(repository.getSettings()) }
    var todaySummary by remember { mutableStateOf(repository.getDailySummary(today)) }
    var recentRecords by remember { mutableStateOf(repository.getRecords(today)) }
    var weeklyData by remember { mutableStateOf(repository.getWeeklyData()) }
    var monthlyData by remember { mutableStateOf(repository.getMonthlyData()) }
    var unlockedAchievements by remember { mutableStateOf(repository.getUnlockedAchievements(cat)) }
    var lockedAchievements by remember { mutableStateOf(repository.getLockedAchievements(cat)) }
    var consecutiveDays by remember { mutableStateOf(repository.getConsecutiveDays()) }

    // 同步设置到 SoundManager
    LaunchedEffect(settings.soundEnabled) { soundManager.setSoundEnabled(settings.soundEnabled) }
    LaunchedEffect(settings.vibrationEnabled) { soundManager.setVibrationEnabled(settings.vibrationEnabled) }

    // 应用自然衰减
    LaunchedEffect(Unit) { cat = repository.applyNaturalDecay(cat) }

    // 会员双倍金币
    val coinMultiplier = if (isPremium) 2 else 1

    fun refreshData() {
        cat = repository.getCat()
        todaySummary = repository.getDailySummary(today)
        recentRecords = repository.getRecords(today)
        weeklyData = repository.getWeeklyData()
        monthlyData = repository.getMonthlyData()
        unlockedAchievements = repository.getUnlockedAchievements(cat)
        lockedAchievements = repository.getLockedAchievements(cat)
        consecutiveDays = repository.getConsecutiveDays()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                screen.selectedIcon() else screen.unselectedIcon()
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {
                CatHomeTab(
                    cat = cat, todaySummary = todaySummary, recentRecords = recentRecords,
                    settings = settings, isPremium = isPremium,
                    onAction = { action ->
                        val oldIntimacy = cat.intimacy
                        val oldHappiness = cat.happiness
                        cat = repository.performAction(action, cat)
                        // 会员双倍金币
                        if (isPremium && action != CatAction.SLEEP) {
                            val bonusCoins = when (action) {
                                CatAction.PET_HEAD, CatAction.SCRATCH_CHIN, CatAction.RUB_BELLY -> 2
                                CatAction.FEED_FOOD, CatAction.FEED_SNACK, CatAction.FEED_CAN -> 3
                                CatAction.PLAY_CAT_TEASE, CatAction.PLAY_BALL, CatAction.PLAY_LASER -> 3
                                CatAction.CLEAN_BATH, CatAction.CLEAN_BRUSH -> 5
                                else -> 0
                            }
                            cat = cat.copy(coins = cat.coins + bonusCoins)
                            repository.saveCat(cat)
                        }
                        soundManager.playActionSound(action)
                        (context as? MainActivity)?.let { activity -> adManager.tryShowInterstitial(activity) }
                        val record = CatRecord(
                            actionType = action.name, timestamp = System.currentTimeMillis(),
                            date = today.toString(), intimacyBefore = oldIntimacy, intimacyAfter = cat.intimacy,
                            happinessBefore = oldHappiness, happinessAfter = cat.happiness
                        )
                        repository.addRecord(record)
                        refreshData()
                    },
                    onSettingsChanged = { newSettings -> repository.saveSettings(newSettings); settings = newSettings }
                )
            }
            composable(Screen.Stats.route) {
                StatsTab(cat = cat, weeklyData = weeklyData, monthlyData = monthlyData,
                    unlockedAchievements = unlockedAchievements, lockedAchievements = lockedAchievements,
                    consecutiveDays = consecutiveDays)
            }
            composable(Screen.Shop.route) {
                ShopTab(cat = cat, isPremium = isPremium,
                    onBuyItem = { item ->
                        cat = repository.buyItem(item, cat)
                        // 会员购买皮肤免费
                        if (isPremium && item.category == ShopCategory.SKIN && !item.isDefault) {
                            cat = cat.copy(coins = cat.coins + item.price) // 退还金币
                            repository.saveCat(cat)
                        }
                        soundManager.playPurchaseSound()
                        refreshData()
                    })
            }
            composable(Screen.Social.route) {
                SocialTab(cat = cat, socialManager = socialManager,
                    onShowRewardedAd = {
                        val reward = if (isPremium) 100 else 50
                        cat = cat.copy(coins = cat.coins + reward)
                        repository.saveCat(cat); refreshData()
                    })
            }
            composable(Screen.Premium.route) {
                PremiumTab(billingManager = billingManager, isPremium = isPremium, adsRemoved = adsRemoved,
                    onShowRewardedAd = {
                        val reward = if (isPremium) 100 else 50
                        cat = cat.copy(coins = cat.coins + reward)
                        repository.saveCat(cat); refreshData()
                    })
            }
            composable(Screen.Settings.route) {
                SettingsTab(settings = settings, soundManager = soundManager, isPremium = isPremium,
                    onSettingsChanged = { newSettings ->
                        repository.saveSettings(newSettings); settings = newSettings
                        if (newSettings.catName != cat.name) {
                            cat = cat.copy(name = newSettings.catName)
                            repository.saveCat(cat)
                        }
                    })
            }
        }
    }
}
