package com.example.foodexpress.data.repository

import com.example.foodexpress.data.dao.UsuarioDao
import com.example.foodexpress.models.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UsuarioRepository(private val usuarioDao: UsuarioDao) {
    suspend fun login(correo: String, passHash: String): Usuario? = withContext(Dispatchers.IO) { usuarioDao.login(correo, passHash) }
    suspend fun registrar(usuario: Usuario): Long = withContext(Dispatchers.IO) { usuarioDao.registrar(usuario) }
    suspend fun getUsuarioByCorreo(correo: String): Usuario? = withContext(Dispatchers.IO) { usuarioDao.getUsuarioByCorreo(correo) }
    suspend fun update(usuario: Usuario) = withContext(Dispatchers.IO) { usuarioDao.update(usuario) }
    fun getUsuarioById(id: Int): Flow<Usuario?> = usuarioDao.getUsuarioById(id)
}
