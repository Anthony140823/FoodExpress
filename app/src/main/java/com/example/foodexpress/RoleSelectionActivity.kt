package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodexpress.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardCliente.setOnClickListener { goToRegister("CLIENTE") }
        binding.cardRestaurante.setOnClickListener { goToRegister("RESTAURANTE") }
        binding.cardRepartidor.setOnClickListener { goToRegister("REPARTIDOR") }

        binding.btnBackToLoginRole.setOnClickListener { finish() }
    }

    private fun goToRegister(role: String) {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("SELECTED_ROLE", role)
        startActivity(intent)
    }
}
