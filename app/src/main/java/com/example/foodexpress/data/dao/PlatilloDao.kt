package com.example.foodexpress.data.dao

import android.content.ContentValues
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.models.Platillo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PlatilloDao(private val dbHelper: AdminSQLiteOpenHelper) {
    fun getPlatillosByRestaurante(id: Int): Flow<List<Platillo>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM platillos WHERE restaurante_id = ?", arrayOf(id.toString()))
        val list = mutableListOf<Platillo>()
        while (cursor.moveToNext()) {
            list.add(Platillo(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1))
        }
        cursor.close()
        emit(list)
    }

    fun getPlatillosDisponiblesByRestaurante(id: Int): Flow<List<Platillo>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM platillos WHERE restaurante_id = ? AND disponible = 1", arrayOf(id.toString()))
        val list = mutableListOf<Platillo>()
        while (cursor.moveToNext()) {
            list.add(Platillo(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1))
        }
        cursor.close()
        emit(list)
    }

    fun getPlatilloById(id: Int): Platillo? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM platillos WHERE id = ?", arrayOf(id.toString()))
        var p: Platillo? = null
        if (cursor.moveToFirst()) {
            p = Platillo(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7) == 1)
        }
        cursor.close()
        return p
    }

    fun insert(p: Platillo): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("restaurante_id", p.restauranteId)
            put("nombre", p.nombre)
            put("descripcion", p.descripcion)
            put("precio", p.precio)
            put("categoria", p.categoria)
            put("imagen", p.imagen)
            put("disponible", if (p.disponible) 1 else 0)
        }
        return db.insert("platillos", null, cv)
    }

    fun update(p: Platillo) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("nombre", p.nombre)
            put("descripcion", p.descripcion)
            put("precio", p.precio)
            put("categoria", p.categoria)
            put("imagen", p.imagen)
            put("disponible", if (p.disponible) 1 else 0)
        }
        db.update("platillos", cv, "id = ?", arrayOf(p.id.toString()))
    }

    fun delete(p: Platillo) {
        val db = dbHelper.writableDatabase
        db.delete("platillos", "id = ?", arrayOf(p.id.toString()))
    }
}
