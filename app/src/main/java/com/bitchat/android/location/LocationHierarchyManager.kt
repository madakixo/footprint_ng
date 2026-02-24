package com.bitchat.android.location

import android.content.Context
import com.google.gson.Gson
import com.bitchat.android.identity.SecureIdentityStateManager

class LocationHierarchyManager(private val context: Context) {
    private val identityManager = SecureIdentityStateManager(context)
    private val gson = Gson()

    fun getCurrentAdminLocation(): NigeriaLocation? {
        val json = identityManager.getSecureValue("user_location") ?: return null
        return try {
            gson.fromJson(json, NigeriaLocation::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateAdminLocation(location: NigeriaLocation) {
        identityManager.storeSecureValue("user_location", gson.toJson(location))
    }

    /**
     * Records a movement entry and returns the compressed footprint
     */
    fun recordMovement(latitude: Double, longitude: Double) {
        val currentHistoryJson = identityManager.getSecureValue("location_history") ?: "[]"
        val history = try {
            gson.fromJson(currentHistoryJson, Array<LocationHistoryEntry>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }

        val newEntry = LocationHistoryEntry(
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            adminLocation = getCurrentAdminLocation()
        )

        history.add(newEntry)

        // Keep only last 100 entries for "compression" / privacy
        val limitedHistory = history.takeLast(100)
        identityManager.storeSecureValue("location_history", gson.toJson(limitedHistory))
    }
}
