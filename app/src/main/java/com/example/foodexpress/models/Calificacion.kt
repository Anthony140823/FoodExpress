package com.example.foodexpress.models

data class Calificacion(
    val id: Int = 0,
    val pedidoId: Int,
    val clienteId: Int,
    val restauranteId: Int,
    val estrellas: Int,
    val comentario: String
)
