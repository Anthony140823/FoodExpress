package com.example.foodexpress.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.foodexpress.data.repository.UsuarioRepository
import com.example.foodexpress.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _loginResult = MutableStateFlow<Usuario?>(null)
    val loginResult: StateFlow<Usuario?> = _loginResult

    private val _registerResult = MutableStateFlow<Long?>(null)
    val registerResult: StateFlow<Long?> = _registerResult

    fun login(correo: String, passHash: String) {
        viewModelScope.launch {
            _loginResult.value = repository.login(correo, passHash)
        }
    }

    fun registrar(usuario: Usuario) {
        viewModelScope.launch {
            _registerResult.value = repository.registrar(usuario)
        }
    }

    fun actualizar(usuario: Usuario) = viewModelScope.launch {
        repository.update(usuario)
    }

    suspend fun getUsuarioByCorreo(correo: String): Usuario? {
        return repository.getUsuarioByCorreo(correo)
    }

    fun getUsuarioById(id: Int): kotlinx.coroutines.flow.Flow<Usuario?> {
        return repository.getUsuarioById(id)
    }
}

class AuthViewModelFactory(private val repository: UsuarioRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
