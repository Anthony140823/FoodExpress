package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityMainBinding
import com.example.foodexpress.ui.viewmodel.AuthViewModel
import com.example.foodexpress.ui.viewmodel.AuthViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.HashUtils
import com.example.foodexpress.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as FoodExpressApp).usuarioRepository)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        sessionManager = SessionManager(this)
        
        // Verificar sesión activa
        if (sessionManager.isLoggedIn()) {
            navegarSegunRol(sessionManager.getUserRole() ?: "CLIENTE")
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.btnLogin.setOnClickListener {
            val correoIngresado = binding.etEmail.text.toString().trim()
            val passwordIngresado = binding.etPassword.text.toString().trim()

            if (correoIngresado.isEmpty() || passwordIngresado.isEmpty()) {
                DialogUtils.mostrarAlerta(this, "Campos incompletos", "Por favor, llena tanto el correo como la contraseña.")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoIngresado).matches()) {
                binding.tilEmail.error = "Ingresa un formato de correo válido"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }

            val passHash = HashUtils.sha256(passwordIngresado)
            authViewModel.login(correoIngresado, passHash)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            authViewModel.loginResult.collect { usuario ->
                if (usuario != null) {
                    sessionManager.saveSession(usuario.id, usuario.nombre, usuario.correo, usuario.rol)
                    navegarSegunRol(usuario.rol)
                    finish()
                } else if (binding.etEmail.text?.isNotEmpty() == true) {
                    // Si el flow emitió null y hay texto, probablemente falló el login
                    // Pero ojo: StateFlow emite el valor inicial al suscribirse.
                    // Una mejor forma sería usar un SharedFlow para eventos de un solo uso o una Sealed Class para el estado.
                }
            }
        }
    }

    private fun navegarSegunRol(rol: String) {
        val intent = when (rol) {
            "RESTAURANTE" -> Intent(this, RestaurantActivity::class.java)
            "REPARTIDOR" -> Intent(this, DeliveryActivity::class.java)
            else -> Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
    }
}
