package com.timenw.cattracker.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 内购管理器
 * - 移除广告（一次性购买）¥6.99
 * - 月度会员（订阅）¥6/月
 * - 金币包（消耗型）
 */
class BillingManager(
    private val context: Context,
    private val onPurchaseSuccess: (String) -> Unit,
    private val onPurchaseError: (String) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null

    // 商品 ID（需要在 Google Play Console 中创建）
    companion object {
        const val SKU_REMOVE_ADS = "cat_remove_ads"           // 移除广告 ¥6.99
        const val SKU_PREMIUM_MONTHLY = "cat_premium_monthly" // 月度会员 ¥6/月
        const val SKU_COINS_100 = "cat_coins_100"             // 100金币 ¥1.99
        const val SKU_COINS_500 = "cat_coins_500"             // 500金币 ¥6.99
        const val SKU_COINS_1000 = "cat_coins_1000"           // 1000金币 ¥12.99
    }

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _adsRemoved = MutableStateFlow(false)
    val adsRemoved: StateFlow<Boolean> = _adsRemoved

    private val skuDetailsMap = mutableMapOf<String, ProductDetails>()

    init {
        initBilling()
    }

    private fun initBilling() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableProducts()
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                // 重试连接
                retryConnection()
            }
        })
    }

    private fun retryConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    /**
     * 查询可用商品
     */
    private fun queryAvailableProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PREMIUM_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_COINS_100)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_COINS_500)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_COINS_1000)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
                    skuDetailsMap[details.productId] = details
                }
            }
        }
    }

    /**
     * 查询已购买的商品
     */
    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }

        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                for (sku in purchase.products) {
                    when (sku) {
                        SKU_REMOVE_ADS -> {
                            _adsRemoved.value = true
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                        SKU_PREMIUM_MONTHLY -> {
                            _isPremium.value = true
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                        SKU_COINS_100 -> {
                            onPurchaseSuccess("100金币")
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                        SKU_COINS_500 -> {
                            onPurchaseSuccess("500金币")
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                        SKU_COINS_1000 -> {
                            onPurchaseSuccess("1000金币")
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params) { result ->
            // 确认购买完成
        }
    }

    /**
     * 发起购买
     */
    fun purchase(activity: Activity, sku: String) {
        val productDetails = skuDetailsMap[sku] ?: run {
            onPurchaseError("商品未找到，请稍后重试")
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply {
                if (offerToken != null) {
                    setOfferToken(offerToken)
                }
            }
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient?.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchases(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                onPurchaseError("已取消购买")
            }
            else -> {
                onPurchaseError("购买失败: ${result.debugMessage}")
            }
        }
    }

    fun getSkuPrice(sku: String): String {
        return skuDetailsMap[sku]?.let { details ->
            when (sku) {
                SKU_PREMIUM_MONTHLY -> {
                    details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
                        ?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "¥6/月"
                }
                else -> {
                    details.oneTimePurchaseOfferDetails?.formattedPrice ?: "¥--"
                }
            }
        } ?: "¥--"
    }

    fun release() {
        billingClient?.endConnection()
    }
}
