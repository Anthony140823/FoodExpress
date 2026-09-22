package com.example.foodexpress.data.repository

import com.example.foodexpress.data.dao.PedidoDao
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.models.DetallePedido
import com.example.foodexpress.models.Calificacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PedidoRepository(private val pedidoDao: PedidoDao) {
    suspend fun crearPedidoCompleto(pedido: Pedido, detalles: List<DetallePedido>): Long =
        withContext(Dispatchers.IO) { pedidoDao.crearPedidoCompleto(pedido, detalles) }
    suspend fun insertPedido(pedido: Pedido): Long = withContext(Dispatchers.IO) { pedidoDao.insertPedido(pedido) }
    suspend fun insertDetalle(detalles: List<DetallePedido>) = withContext(Dispatchers.IO) { pedidoDao.insertDetalle(detalles) }
    suspend fun updatePedido(pedido: Pedido) = withContext(Dispatchers.IO) { pedidoDao.updatePedido(pedido) }
    suspend fun getPedidoById(id: Int) = withContext(Dispatchers.IO) { pedidoDao.getPedidoById(id) }
    fun getPedidosByCliente(clienteId: Int): Flow<List<Pedido>> = pedidoDao.getPedidosByCliente(clienteId)
    fun getPedidosByRestaurante(restauranteId: Int): Flow<List<Pedido>> = pedidoDao.getPedidosByRestaurante(restauranteId)
    fun getPedidosDisponiblesParaRepartidor(): Flow<List<Pedido>> = pedidoDao.getPedidosDisponiblesParaRepartidor()
    fun getPedidosAsignados(repartidorId: Int): Flow<List<Pedido>> = pedidoDao.getPedidosAsignados(repartidorId)
    suspend fun getDetallesByPedido(pedidoId: Int) = withContext(Dispatchers.IO) { pedidoDao.getDetallesByPedido(pedidoId) }
    suspend fun calificar(calificacion: Calificacion) = withContext(Dispatchers.IO) { pedidoDao.calificar(calificacion) }
    suspend fun getCalificacionByPedido(pedidoId: Int) = withContext(Dispatchers.IO) { pedidoDao.getCalificacionByPedido(pedidoId) }
}
