package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.databinding.ActivityDeliveryBinding
import com.example.foodexpress.databinding.ItemPedidoGestionBinding
import com.example.foodexpress.models.Pedido
import com.example.foodexpress.ui.viewmodel.PedidoViewModel
import com.example.foodexpress.ui.viewmodel.PedidoViewModelFactory
import com.example.foodexpress.utils.NotificationUtils
import com.example.foodexpress.utils.SessionManager

class DeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryBinding
    private lateinit var sessionManager: SessionManager
    private var lastAvailableCount = -1
    
    private val pedidoViewModel: PedidoViewModel by viewModels {
        PedidoViewModelFactory((application as FoodExpressApp).pedidoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.rvDelivery.layoutManager = LinearLayoutManager(this)

        binding.btnLogoutDel.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        setupObservers()
    }

    private fun setupObservers() {
        val userId = sessionManager.getUserId()
        val disponibles = pedidoViewModel.getPedidosDisponiblesParaRepartidor()
        val asignados = pedidoViewModel.getPedidosAsignados(userId)

        disponibles.observe(this) { lista ->
            if (lastAvailableCount != -1 && lista.size > lastAvailableCount) {
                NotificationUtils.showNotification(this, "FoodExpress", "Hay un pedido listo para recoger.")
            }
            lastAvailableCount = lista.size
        }

        val mediator = MediatorLiveData<List<Pedido>>()
        
        var listDisponibles = listOf<Pedido>()
        var listAsignados = listOf<Pedido>()

        fun updateCombined() {
            mediator.value = listAsignados + listDisponibles
        }

        mediator.addSource(disponibles) {
            listDisponibles = it
            updateCombined()
            binding.tvStatsCount.text = it.size.toString()
        }
        mediator.addSource(asignados) {
            listAsignados = it
            updateCombined()
        }

        mediator.observe(this) { lista ->
            actualizarLista(lista)
        }
    }

    private fun actualizarLista(lista: List<Pedido>) {
        binding.rvDelivery.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val b = ItemPedidoGestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return object : RecyclerView.ViewHolder(b.root) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val p = lista[position]
                val b = ItemPedidoGestionBinding.bind(holder.itemView)
                b.tvOrderInfo.text = "Pedido #${p.id}"
                b.tvOrderDetails.text = "Entrega en: ${p.direccionEntrega} - Total: S/ ${p.total}"
                b.tvStatusLabel.text = "Estado: ${p.estado}"
                
                if (p.estado == "LISTO_PARA_ENVIO") {
                    b.btnAccionPedido.text = "Aceptar Pedido"
                    b.btnAccionPedido.setOnClickListener {
                        pedidoViewModel.asignarRepartidor(p, sessionManager.getUserId())
                    }
                } else if (p.estado == "EN_CAMINO") {
                    b.btnAccionPedido.text = "Marcar Entregado"
                    b.btnAccionPedido.setOnClickListener {
                        pedidoViewModel.actualizarEstado(p, "ENTREGADO")
                    }
                }
            }
            override fun getItemCount() = lista.size
        }
    }
}
