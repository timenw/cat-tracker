package com.timenw.cattracker.data

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 广告管理器
 * - Banner 广告（首页底部）
 * - 插屏广告（互动后概率展示）
 * - 激励广告（看广告获得金币/零食）
 * - 移除广告 IAP
 */
class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var adLoadCount = 0

    // 广告移除状态（通过 IAP 购买）
    private val _adsRemoved = MutableStateFlow(false)
    val adsRemoved: StateFlow<Boolean> = _adsRemoved

    // 插屏广告展示间隔（每 N 次互动展示一次）
    private val INTERSTITIAL_INTERVAL = 5

    init {
        MobileAds.initialize(context) {}
        loadInterstitial()
    }

    fun setAdsRemoved(removed: Boolean) {
        _adsRemoved.value = removed
    }

    /**
     * 获取 Banner 广告请求
     */
    fun createBannerAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * 尝试展示插屏广告
     * 每 INTERSTITIAL_INTERVAL 次互动展示一次
     */
    fun tryShowInterstitial(activity: Activity): Boolean {
        if (_adsRemoved.value) return false
        adLoadCount++
        if (adLoadCount % INTERSTITIAL_INTERVAL != 0) return false

        val ad = interstitialAd
        if (ad != null) {
            ad.show(activity)
            loadInterstitial() // 预加载下一个
            return true
        }
        return false
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        // 使用 Google 测试插屏广告 ID
        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * 获取激励广告（看广告获得奖励）
     * 返回 null 表示广告未准备好
     */
    fun createRewardedAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    companion object {
        // Google 测试广告 ID
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
