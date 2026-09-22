package com.example.foodexpress.maps

data class MapPoint(val latitude: Double, val longitude: Double) {
    companion object {
        fun from(latitude: Double?, longitude: Double?): MapPoint? {
            if (latitude == null || longitude == null || !latitude.isFinite() || !longitude.isFinite()) return null
            if (latitude !in -85.05112878..85.05112878 || longitude !in -180.0..180.0) return null
            return MapPoint(latitude, longitude)
        }
    }
}
