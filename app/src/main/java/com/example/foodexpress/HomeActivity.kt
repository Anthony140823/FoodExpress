package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodexpress.adapters.RestauranteAdapter
import com.example.foodexpress.databinding.ActivityHomeBinding
import com.example.foodexpress.models.Restaurante
import com.example.foodexpress.ui.viewmodel.RestaurantViewModel
import com.example.foodexpress.ui.viewmodel.RestaurantViewModelFactory
import com.example.foodexpress.utils.SessionManager

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: RestauranteAdapter
    
    private val restaurantViewModel: RestaurantViewModel by viewModels {
        RestaurantViewModelFactory((application as FoodExpressApp).restauranteRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        binding.tvWelcome.text = sessionManager.getUserName()

        setupRecyclerView()
        setupFilters()
        setupObservers()
        
        binding.btnGoToCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupObservers() {
        restaurantViewModel.searchResult.observe(this) { lista ->
            adapter.updateList(lista)
        }
    }

    private fun setupFilters() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                restaurantViewModel.buscar(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.chipGroupCategories.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedId)
            val categoria = if (chip != null && chip.text != "Todo") chip.text.toString() else null
            // El ViewModel buscará con el texto actual. Si implementamos categorías en el VM, deberíamos pasarla aquí.
        }
    }

    private fun setupRecyclerView() {
        adapter = RestauranteAdapter(emptyList()) { restaurante ->
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("RESTAURANTE_ID", restaurante.id)
            intent.putExtra("RESTAURANTE_NOMBRE", restaurante.nombre)
            startActivity(intent)
        }
        binding.rvRestaurantes.layoutManager = LinearLayoutManager(this)
        binding.rvRestaurantes.adapter = adapter
    }
}
