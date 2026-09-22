package com.example.foodexpress.ui.viewmodel

import androidx.lifecycle.*
import com.example.foodexpress.data.repository.PedidoRepository
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.models.DetallePedido
import com.example.foodexpress.models.Calificacion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PedidoViewModel(private val repository: PedidoRepository) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    fun refresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun getPedidosByCliente(clienteId: Int): LiveData<List<Pedido>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPedidosByCliente(clienteId)
        }.asLiveData()
    }

    fun getPedidosByRestaurante(restauranteId: Int): LiveData<List<Pedido>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPedidosByRestaurante(restauranteId)
        }.asLiveData()
    }

    fun getPedidosDisponiblesParaRepartidor(): LiveData<List<Pedido>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPedidosDisponiblesParaRepartidor()
        }.asLiveData()
    }

    fun getPedidosAsignados(repartidorId: Int): LiveData<List<Pedido>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPedidosAsignados(repartidorId)
        }.asLiveData()
    }

    fun crearPedido(pedido: Pedido, detalles: List<DetallePedido>) = viewModelScope.launch {
        val id = repository.insertPedido(pedido)
        val detallesConId = detalles.map { it.copy(pedidoId = id.toInt()) }
        repository.insertDetalle(detallesConId)
        refresh()
    }

    fun actualizarEstado(pedido: Pedido, nuevoEstado: String) = viewModelScope.launch {
        repository.updatePedido(pedido.copy(estado = nuevoEstado))
        refresh() // Forzar la emisión del nuevo estado a todos los observadores
    }

    fun asignarRepartidor(pedido: Pedido, repartidorId: Int) = viewModelScope.launch {
        repository.updatePedido(pedido.copy(repartidorId = repartidorId, estado = "EN_CAMINO"))
        refresh()
    }

    suspend fun getPedidoById(id: Int) = repository.getPedidoById(id)
    suspend fun getDetallesByPedido(pedidoId: Int) = repository.getDetallesByPedido(pedidoId)
    suspend fun getCalificacionByPedido(pedidoId: Int) = repository.getCalificacionByPedido(pedidoId)

    fun calificar(calificacion: Calificacion) = viewModelScope.launch {
        repository.calificar(calificacion)
        refresh()
    }
}

class PedidoViewModelFactory(private val repository: PedidoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PedidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PedidoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}