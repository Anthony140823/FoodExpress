package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodexpress.adapters.PlatilloAdapter
import com.example.foodexpress.databinding.ActivityMenuBinding
import com.example.foodexpress.ui.viewmodel.PlatilloViewModel
import com.example.foodexpress.ui.viewmodel.PlatilloViewModelFactory

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var adapter: PlatilloAdapter
    private var restauranteId: Int = -1
    
    private val platilloViewModel: PlatilloViewModel by viewModels {
        PlatilloViewModelFactory((application as FoodExpressApp).platilloRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restauranteId = intent.getIntExtra("RESTAURANTE_ID", -1)
        val nombreRestaurante = intent.getStringExtra("RESTAURANTE_NOMBRE")
        binding.tvMenuTitle.text = nombreRestaurante ?: "Menú"

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = PlatilloAdapter(emptyList()) { platillo ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("PLATILLO_ID", platillo.id)
            startActivity(intent)
        }
        binding.rvPlatillos.layoutManager = LinearLayoutManager(this)
        binding.rvPlatillos.adapter = adapter
    }

    private fun setupObservers() {
        platilloViewModel.getPlatillosDisponiblesByRestaurante(restauranteId).observe(this) { lista ->
            adapter.updateList(lista)
        }
    }
}
