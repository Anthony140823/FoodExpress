package com.example.foodexpress.models

data class Restaurante(
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val categoria: String,
    val calificacion: Double,
    val tiempoEntrega: String,
    val imagen: String,
    val activo: Boolean = true
)
