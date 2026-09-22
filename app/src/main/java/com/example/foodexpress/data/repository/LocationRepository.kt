package com.example.foodexpress.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.maps.MapPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(private val context: Context) {
    suspend fun restaurantPoint(id: Int): MapPoint? = withContext(Dispatchers.IO) {
        AdminSQLiteOpenHelper(context).use { helper ->
            helper.readableDatabase.rawQuery("SELECT latitud, longitud FROM restaurantes WHERE id = ?", arrayOf(id.toString())).use { c ->
                if (!c.moveToFirst() || c.isNull(0) || c.isNull(1)) null
                else MapPoint.from(c.getDouble(0), c.getDouble(1))
            }
        }
    }

    suspend fun saveRestaurantPoint(id: Int, point: MapPoint) = withContext(Dispatchers.IO) {
        AdminSQLiteOpenHelper(context).use { helper ->
            val values = ContentValues().apply {
                put("latitud", point.latitude)
                put("longitud", point.longitude)
            }
            check(helper.writableDatabase.update("restaurantes", values, "id = ?", arrayOf(id.toString())) == 1)
        }
    }

    // A route must never silently use only the first restaurant in a mixed cart.
    suspend fun restaurantForCart(dishIds: List<Int>): Int? = withContext(Dispatchers.IO) {
        if (dishIds.isEmpty()) return@withContext null
        AdminSQLiteOpenHelper(context).use { helper ->
            val ids = dishIds.distinct()
            val placeholders = ids.joinToString(",") { "?" }
            helper.readableDatabase.rawQuery("SELECT restaurante_id FROM platillos WHERE id IN ($placeholders)", ids.map { it.toString() }.toTypedArray()).use { c ->
                val restaurants = mutableSetOf<Int>()
                while (c.moveToNext()) restaurants.add(c.getInt(0))
                if (c.count == ids.size && restaurants.size == 1) restaurants.first() else null
            }
        }
    }
}
