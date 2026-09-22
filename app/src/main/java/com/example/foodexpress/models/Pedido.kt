package com.example.foodexpress.models

data class Pedido(
    val id: Int = 0,
    val clienteId: Int,
    val restauranteId: Int,
    val repartidorId: Int? = null,
    val direccionEntrega: String = "",
    val fecha: String,
    val subtotal: Double,
    val costoEnvio: Double,
    val total: Double,
    val metodoPago: String,
    val estado: String
)
