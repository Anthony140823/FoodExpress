package com.example.foodexpress.models

data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val passwordHash: String,
    val rol: String
)
