package com.example.foodexpress.data

import android.content.Context
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.data.dao.*

class AppDatabase(context: Context) {
    private val dbHelper = AdminSQLiteOpenHelper(context)
    
    val usuarioDao = UsuarioDao(dbHelper)
    val restauranteDao = RestauranteDao(dbHelper)
    val platilloDao = PlatilloDao(dbHelper)
    val carritoDao = CarritoDao(dbHelper)
    val pedidoDao = PedidoDao(dbHelper)

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
