package com.example.foodexpress.data.dao

import android.content.ContentValues
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.models.CarritoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CarritoDao(private val dbHelper: AdminSQLiteOpenHelper) {
    fun getCarritoByUsuario(uId: Int): Flow<List<CarritoItem>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM carrito WHERE usuario_id = ?", arrayOf(uId.toString()))
        val list = mutableListOf<CarritoItem>()
        while (cursor.moveToNext()) {
            list.add(CarritoItem(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(5), cursor.getDouble(4), cursor.getInt(3), cursor.getString(6), ""))
        }
        cursor.close()
        emit(list)
    }

    fun insertOrUpdate(i: CarritoItem) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("usuario_id", i.usuarioId)
            put("platillo_id", i.platilloId)
            put("cantidad", i.cantidad)
            put("precio_unitario", i.precioUnitario)
            put("nombre_platillo", i.nombrePlatillo)
            put("imagen", i.imagen)
        }
        val rows = db.update("carrito", cv, "usuario_id = ? AND platillo_id = ?", arrayOf(i.usuarioId.toString(), i.platilloId.toString()))
        if (rows == 0) db.insert("carrito", null, cv)
    }

    fun delete(i: CarritoItem) {
        val db = dbHelper.writableDatabase
        db.delete("carrito", "id = ?", arrayOf(i.id.toString()))
    }

    fun vaciarCarrito(uId: Int) {
        val db = dbHelper.writableDatabase
        db.delete("carrito", "usuario_id = ?", arrayOf(uId.toString()))
    }

    fun getItem(uId: Int, pId: Int): CarritoItem? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM carrito WHERE usuario_id = ? AND platillo_id = ?", arrayOf(uId.toString(), pId.toString()))
        var i: CarritoItem? = null
        if (cursor.moveToFirst()) {
            i = CarritoItem(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(5), cursor.getDouble(4), cursor.getInt(3), cursor.getString(6), "")
        }
        cursor.close()
        return i
    }
}
