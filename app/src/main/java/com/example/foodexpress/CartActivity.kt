package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodexpress.adapters.CarritoAdapter
import com.example.foodexpress.databinding.ActivityCartBinding
import com.example.foodexpress.models.CarritoItem
import com.example.foodexpress.models.DetallePedido
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.ui.viewmodel.CarritoViewModel
import com.example.foodexpress.ui.viewmodel.CarritoViewModelFactory
import com.example.foodexpress.ui.viewmodel.PedidoViewModel
import com.example.foodexpress.ui.viewmodel.PedidoViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CarritoAdapter
    private lateinit var sessionManager: SessionManager
    private var subtotal = 0.0
    private val costoEnvio = 5.0
    private var currentItems = listOf<CarritoItem>()

    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory((application as FoodExpressApp).carritoRepository)
    }
    private val pedidoViewModel: PedidoViewModel by viewModels {
        PedidoViewModelFactory((application as FoodExpressApp).pedidoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupObservers()

        binding.btnConfirmarPedido.setOnClickListener {
            if (subtotal > 0) {
                confirmarPedido()
            } else {
                DialogUtils.mostrarAlerta(this, "Carrito Vacío", "Agrega algunos platillos antes de continuar.")
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(emptyList(), 
            onUpdate = { item, nuevaCant -> carritoViewModel.actualizarCantidad(item, nuevaCant) },
            onDelete = { item -> carritoViewModel.eliminarItem(item) }
        )
        binding.rvCarrito.layoutManager = LinearLayoutManager(this)
        binding.rvCarrito.adapter = adapter
    }

    private fun setupObservers() {
        carritoViewModel.getCarritoByUsuario(sessionManager.getUserId()).observe(this) { items ->
            currentItems = items
            adapter.updateList(items)
            
            subtotal = items.sumOf { it.precioUnitario * it.cantidad }
            actualizarResumen()
        }
    }

    private fun actualizarResumen() {
        binding.tvSubtotal.text = "S/ %.2f".format(subtotal)
        binding.tvEnvio.text = "S/ %.2f".format(costoEnvio)
        binding.tvTotal.text = "S/ %.2f".format(subtotal + costoEnvio)
    }

    private fun confirmarPedido() {
        if (currentItems.isEmpty()) return

        val direccion = binding.etCartDireccion.text.toString().trim()
        if (direccion.isEmpty()) {
            DialogUtils.mostrarAlerta(this, "Dirección requerida", "Indica dónde debemos entregar tu pedido.")
            return
        }

        // 1. Obtenemos el ID del platillo que está en el carrito
        val platilloId = currentItems.first().platilloId
        var restauranteIdReal = 1 // Por defecto por si ocurre un error

        // 2. Consultamos directamente a la BD quién es el dueño de este platillo
        val admin = AdminSQLiteOpenHelper(this)
        val db = admin.readableDatabase
        val cursor = db.rawQuery("SELECT restaurante_id FROM platillos WHERE id = ?", arrayOf(platilloId.toString()))
        if (cursor.moveToFirst()) {
            restauranteIdReal = cursor.getInt(0) // Obtenemos el ID del restaurante correcto
        }
        cursor.close()
        db.close()

        // 3. Creamos el pedido asignándoselo a su verdadero dueño
        val pedido = Pedido(
            clienteId = sessionManager.getUserId(),
            restauranteId = restauranteIdReal, // <--- CORRECCIÓN APLICADA AQUÍ
            direccionEntrega = direccion,
            fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            subtotal = subtotal,
            costoEnvio = costoEnvio,
            total = subtotal + costoEnvio,
            metodoPago = "Efectivo",
            estado = "PENDIENTE"
        )

        val detalles = currentItems.map {
            DetallePedido(
                pedidoId = 0,
                platilloId = it.platilloId,
                nombrePlatillo = it.nombrePlatillo,
                precioUnitario = it.precioUnitario,
                cantidad = it.cantidad,
                subtotal = it.precioUnitario * it.cantidad
            )
        }

        pedidoViewModel.crearPedido(pedido, detalles)
        carritoViewModel.vaciarCarrito(sessionManager.getUserId())

        DialogUtils.mostrarAlerta(this, "Pedido Confirmado", "Tu pedido se ha registrado correctamente.", true) {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
