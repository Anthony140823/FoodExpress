package com.example.foodexpress

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityProfileBinding
import com.example.foodexpress.models.Usuario
import com.example.foodexpress.ui.viewmodel.AuthViewModel
import com.example.foodexpress.ui.viewmodel.AuthViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: Usuario? = null
    
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as FoodExpressApp).usuarioRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        cargarDatos()

        binding.btnSaveProfile.setOnClickListener {
            guardarCambios()
        }

        binding.btnOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            val user = authViewModel.getUsuarioById(sessionManager.getUserId()).first()
            user?.let {
                currentUser = it
                binding.etProfileNombre.setText(it.nombre)
                binding.etProfileTelefono.setText(it.telefono)
                binding.tvProfileEmail.text = it.correo
            }
        }
    }

    private fun guardarCambios() {
        val user = currentUser ?: return
        val nuevoNombre = binding.etProfileNombre.text.toString().trim()
        val nuevoTelefono = binding.etProfileTelefono.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
            DialogUtils.mostrarAlerta(this, "Incompleto", "Llena los campos.")
            return
        }

        val actualizado = user.copy(nombre = nuevoNombre, telefono = nuevoTelefono)
        authViewModel.actualizar(actualizado)
        
        DialogUtils.mostrarAlerta(this, "Éxito", "Perfil actualizado correctamente.", true)
    }
}
