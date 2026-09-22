package com.example.foodexpress.ui.viewmodel

import androidx.lifecycle.*
import com.example.foodexpress.data.repository.PlatilloRepository
import com.example.foodexpress.models.Platillo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlatilloViewModel(private val repository: PlatilloRepository) : ViewModel() {

    // Canal para gatillar actualización en tiempo real tras escrituras en SQLite
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    fun refresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun getPlatillosByRestaurante(restauranteId: Int): LiveData<List<Platillo>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPlatillosByRestaurante(restauranteId)
        }.asLiveData()
    }

    fun getPlatillosDisponiblesByRestaurante(restauranteId: Int): LiveData<List<Platillo>> {
        return _refreshTrigger.flatMapLatest {
            repository.getPlatillosDisponiblesByRestaurante(restauranteId)
        }.asLiveData()
    }

    suspend fun getPlatilloById(id: Int) = repository.getPlatilloById(id)

    fun insert(platillo: Platillo) = viewModelScope.launch {
        repository.insert(platillo)
        refresh() // Actualiza la UI en el acto
    }

    fun update(platillo: Platillo) = viewModelScope.launch {
        repository.update(platillo)
        refresh() // Actualiza la UI en el acto
    }

    fun delete(platillo: Platillo) = viewModelScope.launch {
        repository.delete(platillo)
        refresh() // Actualiza la UI en el acto
    }
}

class PlatilloViewModelFactory(private val repository: PlatilloRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlatilloViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlatilloViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}