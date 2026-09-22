package com.example.foodexpress.data.dao

import android.content.ContentValues
import com.example.foodexpress.AdminSQLiteOpenHelper
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.models.DetallePedido
import com.example.foodexpress.models.Calificacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PedidoDao(private val dbHelper: AdminSQLiteOpenHelper) {
    private fun leerPedido(c: android.database.Cursor): Pedido {
        fun coordenada(name: String): Double? {
            val index = c.getColumnIndexOrThrow(name)
            return if (c.isNull(index)) null else c.getDouble(index)
        }
        return Pedido(c.getInt(0), c.getInt(1), c.getInt(2),
            if (c.isNull(3)) null else c.getInt(3), c.getString(4), c.getString(5),
            c.getDouble(6), c.getDouble(7), c.getDouble(8), c.getString(9), c.getString(10),
            coordenada("entregaLat"), coordenada("entregaLng"),
            coordenada("origenLat"), coordenada("origenLng"))
    }

    fun insertPedido(p: Pedido): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("usuario_id", p.clienteId)
            put("restauranteId", p.restauranteId)
            put("repartidorId", p.repartidorId)
            put("direccionEntrega", p.direccionEntrega)
            put("fecha", p.fecha)
            put("subtotal", p.subtotal)
            put("costo_envio", p.costoEnvio)
            put("total", p.total)
            put("metodo_pago", p.metodoPago)
            put("estado", p.estado)
            put("entregaLat", p.entregaLat)
            put("entregaLng", p.entregaLng)
            put("origenLat", p.origenLat)
            put("origenLng", p.origenLng)
        }
        return db.insertOrThrow("pedidos", null, cv)
    }

    fun insertDetalle(list: List<DetallePedido>) {
        val db = dbHelper.writableDatabase
        list.forEach { d ->
            val cv = ContentValues().apply {
                put("pedido_id", d.pedidoId)
                put("platillo_id", d.platilloId)
                put("cantidad", d.cantidad)
                put("precio_unitario", d.precioUnitario)
                put("nombre_platillo", d.nombrePlatillo)
                put("subtotal", d.subtotal)
            }
            db.insertOrThrow("detalles_pedido", null, cv)
        }
    }

    fun crearPedidoCompleto(p: Pedido, detalles: List<DetallePedido>): Long {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val id = insertPedido(p)
            insertDetalle(detalles.map { it.copy(pedidoId = id.toInt()) })
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun updatePedido(p: Pedido) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("repartidorId", p.repartidorId)
            put("estado", p.estado)
        }
        db.update("pedidos", cv, "id = ?", arrayOf(p.id.toString()))
    }

    fun getPedidoById(id: Int): Pedido? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pedidos WHERE id = ?", arrayOf(id.toString()))
        var p: Pedido? = null
        if (cursor.moveToFirst()) {
            p = leerPedido(cursor)
        }
        cursor.close()
        return p
    }

    fun getPedidosByCliente(id: Int): Flow<List<Pedido>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pedidos WHERE usuario_id = ? ORDER BY id DESC", arrayOf(id.toString()))
        val list = mutableListOf<Pedido>()
        while (cursor.moveToNext()) {
            list.add(leerPedido(cursor))
        }
        cursor.close()
        emit(list)
    }

    fun getPedidosByRestaurante(id: Int): Flow<List<Pedido>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pedidos WHERE restauranteId = ? ORDER BY id DESC", arrayOf(id.toString()))
        val list = mutableListOf<Pedido>()
        while (cursor.moveToNext()) {
            list.add(leerPedido(cursor))
        }
        cursor.close()
        emit(list)
    }

    fun getPedidosDisponiblesParaRepartidor(): Flow<List<Pedido>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pedidos WHERE estado = 'LISTO_PARA_ENVIO' AND repartidorId IS NULL", null)
        val list = mutableListOf<Pedido>()
        while (cursor.moveToNext()) {
            list.add(leerPedido(cursor))
        }
        cursor.close()
        emit(list)
    }

    fun getPedidosAsignados(id: Int): Flow<List<Pedido>> = flow {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM pedidos WHERE repartidorId = ? AND estado = 'EN_CAMINO'", arrayOf(id.toString()))
        val list = mutableListOf<Pedido>()
        while (cursor.moveToNext()) {
            list.add(leerPedido(cursor))
        }
        cursor.close()
        emit(list)
    }

    fun getDetallesByPedido(id: Int): List<DetallePedido> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM detalles_pedido WHERE pedido_id = ?", arrayOf(id.toString()))
        val list = mutableListOf<DetallePedido>()
        while (cursor.moveToNext()) {
            list.add(DetallePedido(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getString(5), cursor.getDouble(4), cursor.getInt(3), cursor.getDouble(6)))
        }
        cursor.close()
        return list
    }

    fun calificar(c: Calificacion) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("pedido_id", c.pedidoId)
            put("clienteId", c.clienteId)
            put("restauranteId", c.restauranteId)
            put("estrellas", c.estrellas)
            put("comentario", c.comentario)
        }
        db.insert("calificaciones", null, cv)
    }

    fun getCalificacionByPedido(id: Int): Calificacion? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM calificaciones WHERE pedido_id = ?", arrayOf(id.toString()))
        var c: Calificacion? = null
        if (cursor.moveToFirst()) {
            c = Calificacion(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5))
        }
        cursor.close()
        return c
    }
}
