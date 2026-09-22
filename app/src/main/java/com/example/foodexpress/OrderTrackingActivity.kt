package com.example.foodexpress

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityOrderTrackingBinding
import com.example.foodexpress.models.Calificacion
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.ui.viewmodel.PedidoViewModel
import com.example.foodexpress.ui.viewmodel.PedidoViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.NotificationUtils
import com.example.foodexpress.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class OrderTrackingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderTrackingBinding
    private lateinit var sessionManager: SessionManager
    private var orderId: Int = -1
    private var currentPedido: Pedido? = null
    private var lastStatus: String? = null
    
    private val pedidoViewModel: PedidoViewModel by viewModels {
        PedidoViewModelFactory((application as FoodExpressApp).pedidoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        orderId = intent.getIntExtra("PEDIDO_ID", -1)
        binding.tvOrderId.text = "Pedido #$orderId"
        binding.btnViewRoute.setOnClickListener {
            startActivity(MapActivity.route(this, orderId))
        }

        binding.btnBackHome.setOnClickListener {
            finish()
        }

        binding.btnEnviarCalificacion.setOnClickListener {
            enviarCalificacion()
        }

        iniciarSeguimiento()
    }

    private fun iniciarSeguimiento() {
        lifecycleScope.launch {
            while (isActive) {
                currentPedido = pedidoViewModel.getPedidoById(orderId)
                currentPedido?.let {
                    actualizarUI(it.estado)
                    verificarSiYaCalifico()
                }
                delay(2000) // Polling a 2 segundos para reflejar cambios en tiempo real
            }
        }
    }

    private fun verificarSiYaCalifico() {
        lifecycleScope.launch {
            val calif = pedidoViewModel.getCalificacionByPedido(orderId)
            if (calif != null) {
                binding.cvCalificar.visibility = View.GONE
            }
        }
    }

    private fun actualizarUI(status: String) {
        if (lastStatus != null && lastStatus != status) {
            // Notificación en la barra superior
            NotificationUtils.showNotification(this, "FoodExpress", "Tu pedido ahora está: $status")

            // Alerta emergente en pantalla para el cliente
            DialogUtils.mostrarAlerta(
                this,
                "¡Actualización de Pedido!",
                "El restaurante/repartidor ha cambiado tu pedido a: $status",
                true
            )
        }
        lastStatus = status

        binding.tvStatusText.text = status

        val (desc, progress) = when(status) {
            "PENDIENTE" -> "Esperando confirmación del restaurante" to 20
            "EN_PREPARACION" -> "Tu comida se está cocinando" to 40
            "LISTO_PARA_ENVIO" -> "El pedido está listo para ser recogido" to 60
            "EN_CAMINO" -> "El repartidor va hacia tu dirección" to 80
            "ENTREGADO" -> "¡Pedido entregado! Disfruta tu comida" to 100
            else -> "Cargando estado..." to 0
        }

        binding.tvStatusDesc.text = desc
        binding.progressTracking.progress = progress

        if (status == "ENTREGADO") {
            binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
            binding.cvCalificar.visibility = View.VISIBLE
        } else {
            binding.cvCalificar.visibility = View.GONE
        }
    }

    private fun enviarCalificacion() {
        val p = currentPedido ?: return
        val comentario = binding.etComentario.text.toString().trim()
        
        val calificacion = Calificacion(
            pedidoId = p.id,
            clienteId = sessionManager.getUserId(),
            restauranteId = p.restauranteId,
            estrellas = 5,
            comentario = comentario
        )
        
        pedidoViewModel.calificar(calificacion)
        DialogUtils.mostrarAlerta(this, "Gracias", "Tu opinión es muy importante.", true) {
            binding.cvCalificar.visibility = View.GONE
        }
    }
}
