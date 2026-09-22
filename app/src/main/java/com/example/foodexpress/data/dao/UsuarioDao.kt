package com.example.foodexpress.data.dao

import android.content.ContentValues
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.models.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UsuarioDao(private val dbHelper: AdminSQLiteOpenHelper) {
    
    fun login(correo: String, passHash: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE correo = ? AND password = ?", arrayOf(correo, passHash))
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5))
        }
        cursor.close()
        return usuario
    }

    fun getUsuarioByCorreo(correo: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE correo = ?", arrayOf(correo))
        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5))
        }
        cursor.close()
        return usuario
    }

    fun registrar(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("correo", usuario.correo)
            put("telefono", usuario.telefono)
            put("password", usuario.passwordHash)
            put("rol", usuario.rol)
        }
        return db.insert("usuarios", null, cv)
    }

    fun update(usuario: Usuario) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("telefono", usuario.telefono)
        }
        db.update("usuarios", cv, "id = ?", arrayOf(usuario.id.toString()))
    }

    fun getUsuarioById(id: Int): Flow<Usuario?> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE id = ?", arrayOf(id.toString()))
        if (cursor.moveToFirst()) {
            emit(Usuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)))
        } else emit(null)
        cursor.close()
    }
}
