package com.cartwise.app.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object OrderStorage {
    private const val PREFS_NAME = "cartwise_app_prefs"
    private const val KEY_ORDERS = "saved_orders_history_v1"
    private const val KEY_SHIPPING = "saved_shipping_address_v1"
    private const val KEY_BILLING = "saved_billing_address_v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Load all persisted purchases from local app storage.
     * Starts empty if no purchases have been made yet.
     * Automatically filters out any legacy hardcoded demo orders.
     */
    fun getOrders(context: Context): List<CartwiseOrder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ORDERS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<CartwiseOrder>>(raw)
                .filterNot {
                    it.id.startsWith("ord_demo") ||
                    it.id == "ord_demo_98214" ||
                    it.itemTitle.contains("Anker Soundcore P20i", ignoreCase = true)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Save or update an order in local app storage.
     * New orders are prepended so the latest order appears first.
     */
    fun saveOrder(context: Context, order: CartwiseOrder) {
        val current = getOrders(context).toMutableList()
        val index = current.indexOfFirst { it.id == order.id }
        if (index >= 0) {
            current[index] = order
        } else {
            current.add(0, order)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ORDERS, json.encodeToString(current)).apply()
    }

    /**
     * Replace full order history in local app storage.
     */
    fun saveOrders(context: Context, orders: List<CartwiseOrder>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ORDERS, json.encodeToString(orders)).apply()
    }

    /**
     * Clear all saved orders from local app storage.
     */
    fun clearOrders(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ORDERS).apply()
    }

    /**
     * Get saved shipping address, defaulting to prefilled Ada Lovelace in Lagos, Nigeria.
     */
    fun getShippingAddress(context: Context): ShippingAddress {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SHIPPING, null) ?: return ShippingAddress()
        return try {
            json.decodeFromString<ShippingAddress>(raw)
        } catch (e: Exception) {
            ShippingAddress()
        }
    }

    /**
     * Save shipping address to local app storage.
     */
    fun saveShippingAddress(context: Context, address: ShippingAddress) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SHIPPING, json.encodeToString(address)).apply()
    }

    /**
     * Get saved billing address, defaulting to prefilled Ada Lovelace.
     */
    fun getBillingAddress(context: Context): BillingAddress {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_BILLING, null) ?: return BillingAddress()
        return try {
            json.decodeFromString<BillingAddress>(raw)
        } catch (e: Exception) {
            BillingAddress()
        }
    }

    /**
     * Save billing address to local app storage.
     */
    fun saveBillingAddress(context: Context, address: BillingAddress) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BILLING, json.encodeToString(address)).apply()
    }
}
