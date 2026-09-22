package com.example.foodexpress.models

data class Platillo(
    val id: Int = 0,
    val restauranteId: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val imagen: String,
    val disponible: Boolean = true
)
