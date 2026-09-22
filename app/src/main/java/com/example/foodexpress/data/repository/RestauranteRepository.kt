package com.example.foodexpress.data.repository

import com.example.foodexpress.data.dao.RestauranteDao
import com.example.foodexpress.models.Restaurante
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RestauranteRepository(private val restauranteDao: RestauranteDao) {
    fun getRestaurantesActivos(): Flow<List<Restaurante>> = restauranteDao.getRestaurantesActivos()
    fun buscarRestaurantes(query: String): Flow<List<Restaurante>> = restauranteDao.buscarRestaurantes(query)
    suspend fun getRestauranteByUsuarioId(usuarioId: Int): Restaurante? = withContext(Dispatchers.IO) { restauranteDao.getRestauranteByUsuarioId(usuarioId) }
    suspend fun insert(restaurante: Restaurante) = withContext(Dispatchers.IO) { restauranteDao.insert(restaurante) }
}
