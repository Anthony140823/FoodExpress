package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.maps.MapPoint
import com.example.foodexpress.data.repository.LocationRepository
import kotlinx.coroutines.launch
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
    private var deliveryPoint: MapPoint? = null
    private var orderPlaced = false
    private val mapPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            MapActivity.pointFromResult(result.data)?.let {
                deliveryPoint = it
                updateMapLabel()
            }
        }
    }

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
        deliveryPoint = MapPoint.from(savedInstanceState?.getDouble("deliveryLat", Double.NaN), savedInstanceState?.getDouble("deliveryLng", Double.NaN))
        updateMapLabel()
        binding.btnSelectLocation.setOnClickListener {
            mapPicker.launch(MapActivity.pick(this, "Ubicación de entrega", deliveryPoint))
        }

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

    private fun updateMapLabel() {
        binding.btnSelectLocation.text = if (deliveryPoint == null) "Elegir entrega en el mapa" else "Cambiar ubicación de entrega ✓"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        deliveryPoint?.let {
            outState.putDouble("deliveryLat", it.latitude)
            outState.putDouble("deliveryLng", it.longitude)
        }
        super.onSaveInstanceState(outState)
    }

    private fun confirmarPedido() {
        if (currentItems.isEmpty()) return

        val direccion = binding.etCartDireccion.text.toString().trim()
        if (direccion.isEmpty()) {
            DialogUtils.mostrarAlerta(this, "Dirección requerida", "Indica dónde debemos entregar tu pedido.")
            return
        }

        val point = deliveryPoint
        if (point == null) {
            DialogUtils.mostrarAlerta(this, "Ubicación requerida", "Elige el punto de entrega en el mapa y confirma el pin.")
            return
        }
        val items = currentItems.toList()
        val orderSubtotal = items.sumOf { it.precioUnitario * it.cantidad }
        binding.btnConfirmarPedido.isEnabled = false
        lifecycleScope.launch {
            try {
                val locations = LocationRepository(applicationContext)
                val restauranteIdReal = locations.restaurantForCart(items.map { it.platilloId })
                if (restauranteIdReal == null) {
                    DialogUtils.mostrarAlerta(this@CartActivity, "Revisa tu carrito", "El pedido debe contener platos de un solo restaurante. Separa los platos de otros restaurantes antes de confirmar.")
                    return@launch
                }
                val origin = locations.restaurantPoint(restauranteIdReal)
                if (origin == null) {
                    DialogUtils.mostrarAlerta(this@CartActivity, "Restaurante sin ubicación", "El restaurante debe guardar su punto de recogida en el mapa antes de recibir pedidos con ruta.")
                    return@launch
                }
                guardarPedido(direccion, point, origin, restauranteIdReal, items, orderSubtotal)
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                DialogUtils.mostrarAlerta(this@CartActivity, "No se pudo confirmar", "Tu pedido no se ha confirmado. Inténtalo nuevamente.")
            } finally {
                binding.btnConfirmarPedido.isEnabled = !orderPlaced
            }
        }
    }

    private suspend fun guardarPedido(direccion: String, point: MapPoint, origin: MapPoint?, restauranteIdReal: Int, items: List<CarritoItem>, orderSubtotal: Double) {
        val pedido = Pedido(
            clienteId = sessionManager.getUserId(),
            restauranteId = restauranteIdReal,
            direccionEntrega = direccion,
            fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            subtotal = orderSubtotal,
            costoEnvio = costoEnvio,
            total = orderSubtotal + costoEnvio,
            metodoPago = "Efectivo",
            estado = "PENDIENTE",
            entregaLat = point.latitude,
            entregaLng = point.longitude,
            origenLat = origin?.latitude,
            origenLng = origin?.longitude
        )

        val detalles = items.map {
            DetallePedido(
                pedidoId = 0,
                platilloId = it.platilloId,
                nombrePlatillo = it.nombrePlatillo,
                precioUnitario = it.precioUnitario,
                cantidad = it.cantidad,
                subtotal = it.precioUnitario * it.cantidad
            )
        }

        pedidoViewModel.crearPedidoConfirmado(pedido, detalles)
        orderPlaced = true
        carritoViewModel.vaciarCarrito(sessionManager.getUserId())

        DialogUtils.mostrarAlerta(this, "Pedido Confirmado", "Tu pedido se ha registrado correctamente.", true) {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
