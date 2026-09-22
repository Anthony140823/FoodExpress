package com.example.foodexpress.data.repository

import com.example.foodexpress.data.dao.PlatilloDao
import com.example.foodexpress.models.Platillo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlatilloRepository(private val platilloDao: PlatilloDao) {
    fun getPlatillosByRestaurante(restauranteId: Int): Flow<List<Platillo>> = platilloDao.getPlatillosByRestaurante(restauranteId)
    fun getPlatillosDisponiblesByRestaurante(restauranteId: Int): Flow<List<Platillo>> = platilloDao.getPlatillosDisponiblesByRestaurante(restauranteId)
    suspend fun getPlatilloById(id: Int) = withContext(Dispatchers.IO) { platilloDao.getPlatilloById(id) }
    suspend fun insert(platillo: Platillo) = withContext(Dispatchers.IO) { platilloDao.insert(platillo) }
    suspend fun update(platillo: Platillo) = withContext(Dispatchers.IO) { platilloDao.update(platillo) }
    suspend fun delete(platillo: Platillo) = withContext(Dispatchers.IO) { platilloDao.delete(platillo) }
}
