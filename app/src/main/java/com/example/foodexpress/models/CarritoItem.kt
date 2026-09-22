package com.example.foodexpress.models

data class CarritoItem(
    val id: Int = 0,
    val usuarioId: Int,
    val platilloId: Int,
    val nombrePlatillo: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val imagen: String,
    val notas: String = ""
)
