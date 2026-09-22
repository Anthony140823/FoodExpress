package com.example.foodexpress

import android.app.Application
import com.example.foodexpress.data.AppDatabase
import com.example.foodexpress.data.repository.*

class FoodExpressApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val usuarioRepository by lazy { UsuarioRepository(database.usuarioDao) }
    val restauranteRepository by lazy { RestauranteRepository(database.restauranteDao) }
    val platilloRepository by lazy { PlatilloRepository(database.platilloDao) }
    val carritoRepository by lazy { CarritoRepository(database.carritoDao) }
    val pedidoRepository by lazy { PedidoRepository(database.pedidoDao) }
}
