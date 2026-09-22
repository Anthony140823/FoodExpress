package com.example.foodexpress

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.foodexpress.utils.HashUtils

class AdminSQLiteOpenHelper(
    context: Context,
    name: String? = "FoodExpress.db",
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = 5
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, correo TEXT UNIQUE, telefono TEXT, password TEXT, rol TEXT)")
        db?.execSQL("CREATE TABLE restaurantes (id INTEGER PRIMARY KEY AUTOINCREMENT, usuarioId INTEGER, nombre TEXT, categoria TEXT, calificacion REAL, tiempo_entrega TEXT, imagen TEXT, activo INTEGER)")
        db?.execSQL("CREATE TABLE platillos (id INTEGER PRIMARY KEY AUTOINCREMENT, restaurante_id INTEGER, nombre TEXT, descripcion TEXT, precio REAL, categoria TEXT, imagen TEXT, disponible INTEGER)")
        db?.execSQL("CREATE TABLE carrito (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER, platillo_id INTEGER, cantidad INTEGER, precio_unitario REAL, nombre_platillo TEXT, imagen TEXT)")
        db?.execSQL("CREATE TABLE pedidos (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER, restauranteId INTEGER, repartidorId INTEGER, direccionEntrega TEXT, fecha TEXT, subtotal REAL, costo_envio REAL, total REAL, metodo_pago TEXT, estado TEXT)")
        db?.execSQL("CREATE TABLE detalles_pedido (id INTEGER PRIMARY KEY AUTOINCREMENT, pedido_id INTEGER, platillo_id INTEGER, cantidad INTEGER, precio_unitario REAL, nombre_platillo TEXT, subtotal REAL)")
        db?.execSQL("CREATE TABLE calificaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, pedido_id INTEGER, clienteId INTEGER, restauranteId INTEGER, estrellas INTEGER, comentario TEXT)")

        insertSeedData(db)
        agregarCoordenadas(db)
    }

    private fun insertSeedData(db: SQLiteDatabase?) {
        val passHash = HashUtils.sha256("123456")
        db?.execSQL("INSERT INTO usuarios (nombre, correo, telefono, password, rol) VALUES ('Admin User', 'admin@foodexpress.com', '987654321', '$passHash', 'CLIENTE')")
        db?.execSQL("INSERT INTO usuarios (nombre, correo, telefono, password, rol) VALUES ('Restaurante Test', 'rest@foodexpress.com', '987654322', '$passHash', 'RESTAURANTE')")
        db?.execSQL("INSERT INTO usuarios (nombre, correo, telefono, password, rol) VALUES ('Repartidor Test', 'delivery@foodexpress.com', '987654323', '$passHash', 'REPARTIDOR')")

        db?.execSQL("INSERT INTO restaurantes (usuarioId, nombre, categoria, calificacion, tiempo_entrega, imagen, activo) VALUES (2, 'La Pizzeria', 'Italiana', 4.5, '30-40 min', '2131165281', 1)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion >= 4) {
            if (oldVersion < 5) agregarCoordenadas(db)
            return
        }
        db?.execSQL("DROP TABLE IF EXISTS usuarios")
        db?.execSQL("DROP TABLE IF EXISTS restaurantes")
        db?.execSQL("DROP TABLE IF EXISTS platillos")
        db?.execSQL("DROP TABLE IF EXISTS carrito")
        db?.execSQL("DROP TABLE IF EXISTS pedidos")
        db?.execSQL("DROP TABLE IF EXISTS detalles_pedido")
        db?.execSQL("DROP TABLE IF EXISTS calificaciones")
        onCreate(db)
    }

    private fun agregarCoordenadas(db: SQLiteDatabase?) {
        db?.execSQL("ALTER TABLE restaurantes ADD COLUMN latitud REAL")
        db?.execSQL("ALTER TABLE restaurantes ADD COLUMN longitud REAL")
        for (column in listOf("entregaLat", "entregaLng", "origenLat", "origenLng")) {
            db?.execSQL("ALTER TABLE pedidos ADD COLUMN $column REAL")
        }
    }
}
