package com.example.firsttry.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentCity(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        // 使用高精度优先，请求一次最新位置
        // CancellationTokenSource 用于取消请求（如果需要）
        val cancellationTokenSource = CancellationTokenSource()
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val city = getCityNameFromLocation(location)
                    if (city != null) {
                        onSuccess(city)
                    } else {
                        Log.e("LocationHelper", "Geocoder returned null")
                        onFailure()
                    }
                } else {
                    Log.e("LocationHelper", "Location is null")
                    onFailure()
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationHelper", "Failed to get location", e)
                onFailure()
            }
    }

    private fun getCityNameFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // 优先取 Locality (市)，如果为空取 SubAdminArea (区/县) 或 AdminArea (省)
                address.locality ?: address.subAdminArea ?: address.adminArea
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Geocoder failed", e)
            null
        }
    }
}
