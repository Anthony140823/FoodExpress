package com.example.foodexpress.ui.viewmodel

import androidx.lifecycle.*
import com.example.foodexpress.data.repository.CarritoRepository
import com.example.foodexpress.models.CarritoItem
import kotlinx.coroutines.launch

class CarritoViewModel(private val repository: CarritoRepository) : ViewModel() {

    fun getCarritoByUsuario(usuarioId: Int) = repository.getCarritoByUsuario(usuarioId).asLiveData()

    fun agregarAlCarrito(item: CarritoItem) = viewModelScope.launch {
        val existente = repository.getItem(item.usuarioId, item.platilloId)
        if (existente != null) {
            val nuevoItem = existente.copy(cantidad = existente.cantidad + item.cantidad)
            repository.insertOrUpdate(nuevoItem)
        } else {
            repository.insertOrUpdate(item)
        }
    }

    fun actualizarCantidad(item: CarritoItem, nuevaCant: Int) = viewModelScope.launch {
        if (nuevaCant > 0) {
            repository.insertOrUpdate(item.copy(cantidad = nuevaCant))
        } else {
            repository.delete(item)
        }
    }

    fun eliminarItem(item: CarritoItem) = viewModelScope.launch { repository.delete(item) }
    fun vaciarCarrito(usuarioId: Int) = viewModelScope.launch { repository.vaciarCarrito(usuarioId) }
}

class CarritoViewModelFactory(private val repository: CarritoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarritoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarritoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
