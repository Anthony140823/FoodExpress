package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.adapters.PlatilloAdapter
import com.example.foodexpress.databinding.ActivityRestaurantBinding
import com.example.foodexpress.databinding.ItemPedidoGestionBinding
import com.example.foodexpress.models.*
import com.example.foodexpress.ui.viewmodel.*
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.SessionManager
import com.example.foodexpress.utils.NotificationUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class RestaurantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRestaurantBinding
    private lateinit var sessionManager: SessionManager
    private var currentTab = 0
    private var restauranteId: Int = -1
    private var lastOrderCount = -1

    private val pedidoViewModel: PedidoViewModel by viewModels {
        PedidoViewModelFactory((application as FoodExpressApp).pedidoRepository)
    }
    private val platilloViewModel: PlatilloViewModel by viewModels {
        PlatilloViewModelFactory((application as FoodExpressApp).platilloRepository)
    }
    private val restaurantViewModel: RestaurantViewModel by viewModels {
        RestaurantViewModelFactory((application as FoodExpressApp).restauranteRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.rvRestaurantItems.layoutManager = LinearLayoutManager(this)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                actualizarVista()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAddDish.setOnClickListener {
            startActivity(Intent(this, AddDishActivity::class.java))
        }

        binding.btnLogoutRest.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            var res = restaurantViewModel.getRestauranteByUsuarioId(userId)
            
            if (res == null) {
                // Si no tiene restaurante, creamos uno por defecto con su nombre de usuario
                val nuevoRes = Restaurante(
                    usuarioId = userId,
                    nombre = sessionManager.getUserName() ?: "Mi Restaurante",
                    categoria = "General",
                    calificacion = 5.0,
                    tiempoEntrega = "30 min",
                    imagen = "2131165281" // ic_menu_gallery
                )
                restaurantViewModel.registrarRestaurante(nuevoRes)
                // Volvemos a buscarlo para obtener el ID generado
                res = restaurantViewModel.getRestauranteByUsuarioId(userId)
            }

            if (res != null) {
                restauranteId = res.id
                actualizarVista()
            }
        }
    }

    private fun actualizarVista() {
        if (restauranteId == -1) return
        
        if (currentTab == 0) {
            binding.fabAddDish.hide()
            setupPedidosObserver()
        } else {
            binding.fabAddDish.show()
            setupPlatillosObserver()
        }
    }

    // 1. Agrega esto dentro de RestaurantActivity para refrescar cuando regrese de otra pantalla
    override fun onResume() {
        super.onResume()
        platilloViewModel.refresh()
        pedidoViewModel.refresh()
    }

    // 2. En tu función setupPedidosObserver(), modifica la acción del botón de los pedidos así:
    private fun setupPedidosObserver() {
        pedidoViewModel.getPedidosByRestaurante(restauranteId).observe(this) { lista ->
            val pendientes = lista.filter { it.estado == "PENDIENTE" }
            if (lastOrderCount != -1 && pendientes.size > lastOrderCount) {
                NotificationUtils.showNotification(this, "FoodExpress", "¡Has recibido un nuevo pedido!")
            }
            lastOrderCount = pendientes.size

            binding.rvRestaurantItems.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val b = ItemPedidoGestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    return object : RecyclerView.ViewHolder(b.root) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val p = lista[position]
                    val b = ItemPedidoGestionBinding.bind(holder.itemView)
                    b.tvOrderInfo.text = "Pedido #${p.id}"
                    b.tvOrderDetails.text = "Fecha: ${p.fecha} - Total: S/ ${p.total}"
                    b.tvStatusLabel.text = "Estado: ${p.estado}"
                    b.btnAccionPedido.text = if (p.estado == "PENDIENTE") "Preparar" else "Enviar"

                    b.btnAccionPedido.setOnClickListener {
                        val nuevoEstado = if (p.estado == "PENDIENTE") "EN_PREPARACION" else "LISTO_PARA_ENVIO"

                        // Actualiza en SQLite y dispara el LiveData
                        pedidoViewModel.actualizarEstado(p, nuevoEstado)

                        // Muestra alerta emergente de confirmación
                        DialogUtils.mostrarAlerta(
                            this@RestaurantActivity,
                            "Estado Actualizado",
                            "El pedido #${p.id} cambió a estado: $nuevoEstado",
                            true
                        )
                    }
                }
                override fun getItemCount() = lista.size
            }
        }
    }

    private fun setupPlatillosObserver() {
        platilloViewModel.getPlatillosByRestaurante(restauranteId).observe(this) { lista ->
            binding.rvRestaurantItems.adapter = PlatilloAdapter(
                platillos = lista,
                onEditClick = { platillo ->
                    val intent = Intent(this, AddDishActivity::class.java)
                    intent.putExtra("PLATILLO_ID", platillo.id)
                    startActivity(intent)
                },
                onDeleteClick = { platillo ->
                    DialogUtils.mostrarConfirmacion(this, "Eliminar Platillo", "¿Estás seguro?") {
                        platilloViewModel.delete(platillo)
                    }
                },
                onItemClick = { }
            )
        }
    }
}
