package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodexpress.adapters.PedidoAdapter
import com.example.foodexpress.databinding.ActivityOrderHistoryBinding
import com.example.foodexpress.ui.viewmodel.PedidoViewModel
import com.example.foodexpress.ui.viewmodel.PedidoViewModelFactory
import com.example.foodexpress.utils.SessionManager

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var adapter: PedidoAdapter
    private lateinit var sessionManager: SessionManager
    
    private val pedidoViewModel: PedidoViewModel by viewModels {
        PedidoViewModelFactory((application as FoodExpressApp).pedidoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = PedidoAdapter(emptyList()) { pedido ->
            val intent = Intent(this, OrderTrackingActivity::class.java)
            intent.putExtra("PEDIDO_ID", pedido.id)
            startActivity(intent)
        }
        binding.rvHistorial.layoutManager = LinearLayoutManager(this)
        binding.rvHistorial.adapter = adapter
    }

    private fun setupObservers() {
        pedidoViewModel.getPedidosByCliente(sessionManager.getUserId()).observe(this) { lista ->
            adapter.updateList(lista)
        }
    }
}
