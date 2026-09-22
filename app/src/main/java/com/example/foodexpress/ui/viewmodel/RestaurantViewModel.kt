package com.example.foodexpress.ui.viewmodel

import androidx.lifecycle.*
import com.example.foodexpress.data.repository.RestauranteRepository
import com.example.foodexpress.models.Restaurante
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModel(private val repository: RestauranteRepository) : ViewModel() {

    val restaurantesActivos = repository.getRestaurantesActivos().asLiveData()

    private val _searchQuery = MutableStateFlow("")
    val searchResult = _searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) repository.getRestaurantesActivos()
        else repository.buscarRestaurantes(query)
    }.asLiveData()

    fun buscar(query: String) {
        _searchQuery.value = query
    }

    suspend fun getRestauranteByUsuarioId(usuarioId: Int) = repository.getRestauranteByUsuarioId(usuarioId)
    fun registrarRestaurante(restaurante: Restaurante) = viewModelScope.launch { repository.insert(restaurante) }
}

class RestaurantViewModelFactory(private val repository: RestauranteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
