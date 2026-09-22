package com.example.foodexpress.data.dao

import android.content.ContentValues
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.models.Restaurante
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestauranteDao(private val dbHelper: AdminSQLiteOpenHelper) {
    fun getRestaurantesActivos(): Flow<List<Restaurante>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM restaurantes WHERE activo = 1", null)
        val list = mutableListOf<Restaurante>()
        while (cursor.moveToNext()) {
            list.add(Restaurante(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1))
        }
        cursor.close()
        emit(list)
    }

    fun buscarRestaurantes(query: String): Flow<List<Restaurante>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM restaurantes WHERE nombre LIKE ? AND activo = 1", arrayOf("%$query%"))
        val list = mutableListOf<Restaurante>()
        while (cursor.moveToNext()) {
            list.add(Restaurante(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1))
        }
        cursor.close()
        emit(list)
    }

    fun getRestauranteByUsuarioId(usuarioId: Int): Restaurante? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM restaurantes WHERE usuarioId = ?", arrayOf(usuarioId.toString()))
        var res: Restaurante? = null
        if (cursor.moveToFirst()) {
            res = Restaurante(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1)
        }
        cursor.close()
        return res
    }

    fun insert(r: Restaurante): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("usuarioId", r.usuarioId)
            put("nombre", r.nombre)
            put("categoria", r.categoria)
            put("calificacion", r.calificacion)
            put("tiempo_entrega", r.tiempoEntrega)
            put("imagen", r.imagen)
            put("activo", if (r.activo) 1 else 0)
        }
        return db.insert("restaurantes", null, cv)
    }
}
