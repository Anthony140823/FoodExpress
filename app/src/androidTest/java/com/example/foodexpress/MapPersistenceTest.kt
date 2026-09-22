package com.example.foodexpress

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.foodexpress.data.dao.PedidoDao
import com.example.foodexpress.models.Pedido
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapPersistenceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun migrationFromVersionFourPreservesExistingData() {
        SQLiteDatabase.create(null).use { db ->
            db.execSQL("CREATE TABLE restaurantes (id INTEGER PRIMARY KEY, nombre TEXT)")
            db.execSQL("CREATE TABLE pedidos (id INTEGER PRIMARY KEY, direccionEntrega TEXT)")
            db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY, nombre TEXT)")
            db.execSQL("INSERT INTO restaurantes VALUES (7, 'Mi restaurante')")
            db.execSQL("INSERT INTO pedidos VALUES (42, 'Mi dirección')")
            db.execSQL("INSERT INTO usuarios VALUES (9, 'Cliente existente')")
            AdminSQLiteOpenHelper(context).use { it.onUpgrade(db, 4, 5) }
            db.rawQuery("SELECT direccionEntrega, entregaLat, origenLng FROM pedidos WHERE id=42", null).use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Mi dirección", c.getString(0))
                assertTrue(c.isNull(1))
                assertTrue(c.isNull(2))
            }
            db.rawQuery("SELECT nombre, latitud FROM restaurantes WHERE id=7", null).use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Mi restaurante", c.getString(0))
                assertTrue(c.isNull(1))
            }
            db.rawQuery("SELECT nombre FROM usuarios WHERE id=9", null).use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("Cliente existente", c.getString(0))
            }
        }
    }

    @Test fun coordinatesRoundTripAndSurviveStatusChanges() {
        AdminSQLiteOpenHelper(context, name = null).use { helper ->
            val dao = PedidoDao(helper)
            val pedido = Pedido(clienteId = 1, restauranteId = 1, fecha = "22/09/2026",
                subtotal = 10.0, costoEnvio = 5.0, total = 15.0, metodoPago = "Efectivo",
                estado = "PENDIENTE", entregaLat = -12.05, entregaLng = -77.04,
                origenLat = -12.04, origenLng = -77.03)
            val id = dao.crearPedidoCompleto(pedido, emptyList()).toInt()
            assertEquals(pedido.copy(id = id), dao.getPedidoById(id))
            dao.updatePedido(pedido.copy(id = id, estado = "EN_CAMINO", repartidorId = 3))
            assertEquals(pedido.copy(id = id, estado = "EN_CAMINO", repartidorId = 3), dao.getPedidoById(id))
        }
    }
}
