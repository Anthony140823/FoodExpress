package com.example.foodexpress.models

data class DetallePedido(
    val id: Int = 0,
    val pedidoId: Int,
    val platilloId: Int,
    val nombrePlatillo: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val subtotal: Double
)
