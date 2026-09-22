package com.example.foodexpress

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityRegisterBinding
import com.example.foodexpress.models.Restaurante
import com.example.foodexpress.models.Usuario
import com.example.foodexpress.ui.viewmodel.AuthViewModel
import com.example.foodexpress.ui.viewmodel.AuthViewModelFactory
import com.example.foodexpress.ui.viewmodel.RestaurantViewModel
import com.example.foodexpress.ui.viewmodel.RestaurantViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.HashUtils
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var selectedRole: String = "CLIENTE"
    
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as FoodExpressApp).usuarioRepository)
    }
    private val restaurantViewModel: RestaurantViewModel by viewModels {
        RestaurantViewModelFactory((application as FoodExpressApp).restauranteRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedRole = intent.getStringExtra("SELECTED_ROLE") ?: "CLIENTE"
        
        // Feedback visual del rol seleccionado
        binding.tvTitleRegister.text = when(selectedRole) {
            "RESTAURANTE" -> {
                binding.llRestauranteExtra.visibility = View.VISIBLE
                "Registro Restaurante"
            }
            "REPARTIDOR" -> "Registro Repartidor"
            else -> "Crea tu cuenta"
        }

        setupObservers()

        binding.btnRegister.setOnClickListener {
            registrarUsuario()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            authViewModel.registerResult.collect { result ->
                if (result != null) {
                    if (result != -1L) {
                        if (selectedRole == "RESTAURANTE") {
                            crearPerfilRestaurante(result.toInt())
                        } else {
                            mostrarExito()
                        }
                    } else {
                        DialogUtils.mostrarAlerta(this@RegisterActivity, "Error", "No se pudo completar el registro.")
                    }
                }
            }
        }
    }

    private fun crearPerfilRestaurante(userId: Int) {
        val nombreRest = binding.etNombreRest.text.toString().trim()
        val catRest = binding.etCategoriaRest.text.toString().trim()

        val restaurante = Restaurante(
            usuarioId = userId,
            nombre = if (nombreRest.isNotEmpty()) nombreRest else "Mi Restaurante",
            categoria = if (catRest.isNotEmpty()) catRest else "Varios",
            calificacion = 5.0,
            tiempoEntrega = "30-45 min",
            imagen = "2131165281", // ic_menu_gallery
            activo = true
        )
        restaurantViewModel.registrarRestaurante(restaurante)
        mostrarExito()
    }

    private fun mostrarExito() {
        DialogUtils.mostrarAlerta(this, "Registro Exitoso", "Tu cuenta ha sido creada. Ahora puedes iniciar sesión.", true) {
            finish()
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombre.text.toString().trim()
        val correo = binding.etEmailRegister.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val pass = binding.etPasswordRegister.text.toString().trim()
        val confirmPass = binding.etConfirmPassword.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            DialogUtils.mostrarAlerta(this, "Campos incompletos", "Por favor completa todos los campos.")
            return
        }

        if (selectedRole == "RESTAURANTE") {
            if (binding.etNombreRest.text.toString().isEmpty() || binding.etCategoriaRest.text.toString().isEmpty()) {
                DialogUtils.mostrarAlerta(this, "Datos del Restaurante", "Por favor completa el nombre y categoría de tu local.")
                return
            }
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            DialogUtils.mostrarAlerta(this, "Correo inválido", "El formato del correo no es válido.")
            return
        }

        if (pass != confirmPass) {
            DialogUtils.mostrarAlerta(this, "Error", "Las contraseñas no coinciden.")
            return
        }

        lifecycleScope.launch {
            // Verificar si el correo ya existe
            val existente = authViewModel.getUsuarioByCorreo(correo)
            if (existente != null) {
                DialogUtils.mostrarAlerta(this@RegisterActivity, "Error", "Este correo ya está registrado.")
                return@launch
            }

            val usuario = Usuario(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                passwordHash = HashUtils.sha256(pass),
                rol = selectedRole
            )
            authViewModel.registrar(usuario)
        }
    }
}
