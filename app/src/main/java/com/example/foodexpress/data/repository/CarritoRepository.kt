package com.example.foodexpress.data.repository

import com.example.foodexpress.data.dao.CarritoDao
import com.example.foodexpress.models.CarritoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CarritoRepository(private val carritoDao: CarritoDao) {
    fun getCarritoByUsuario(usuarioId: Int): Flow<List<CarritoItem>> = carritoDao.getCarritoByUsuario(usuarioId)
    suspend fun insertOrUpdate(item: CarritoItem) = withContext(Dispatchers.IO) { carritoDao.insertOrUpdate(item) }
    suspend fun delete(item: CarritoItem) = withContext(Dispatchers.IO) { carritoDao.delete(item) }
    suspend fun vaciarCarrito(usuarioId: Int) = withContext(Dispatchers.IO) { carritoDao.vaciarCarrito(usuarioId) }
    suspend fun getItem(usuarioId: Int, platilloId: Int) = withContext(Dispatchers.IO) { carritoDao.getItem(usuarioId, platilloId) }
}
