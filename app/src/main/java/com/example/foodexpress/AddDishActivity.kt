package com.example.foodexpress

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityAddDishBinding
import com.example.foodexpress.models.Platillo
import com.example.foodexpress.ui.viewmodel.PlatilloViewModel
import com.example.foodexpress.ui.viewmodel.PlatilloViewModelFactory
import com.example.foodexpress.ui.viewmodel.RestaurantViewModel
import com.example.foodexpress.ui.viewmodel.RestaurantViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.ImageUtils
import com.example.foodexpress.utils.SessionManager
import kotlinx.coroutines.launch

class AddDishActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDishBinding
    private var imagePath: String = android.R.drawable.ic_menu_gallery.toString()
    private lateinit var sessionManager: SessionManager
    private var dishId: Int = -1
    private var restauranteId: Int = -1

    private val platilloViewModel: PlatilloViewModel by viewModels {
        PlatilloViewModelFactory((application as FoodExpressApp).platilloRepository)
    }
    private val restaurantViewModel: RestaurantViewModel by viewModels {
        RestaurantViewModelFactory((application as FoodExpressApp).restauranteRepository)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedPath = ImageUtils.saveImageToInternalStorage(this, it)
            if (savedPath != null) {
                imagePath = savedPath
                ImageUtils.loadImage(binding.ivDishPreview, imagePath)
            } else {
                DialogUtils.mostrarAlerta(this, "Error", "No se pudo procesar la imagen.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        dishId = intent.getIntExtra("PLATILLO_ID", -1)

        binding.toolbarAdd.setNavigationOnClickListener { finish() }

        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        lifecycleScope.launch {
            val res = restaurantViewModel.getRestauranteByUsuarioId(sessionManager.getUserId())
            restauranteId = res?.id ?: -1
            
            if (dishId != -1) {
                binding.toolbarAdd.title = "Editar Platillo"
                binding.btnSaveDish.text = "Actualizar Platillo"
                cargarDatosPlatillo()
            }
        }

        binding.btnSaveDish.setOnClickListener {
            if (dishId == -1) guardarPlatillo() else actualizarPlatillo()
        }
    }

    private fun cargarDatosPlatillo() {
        lifecycleScope.launch {
            val platillo = platilloViewModel.getPlatilloById(dishId)
            platillo?.let {
                binding.etDishName.setText(it.nombre)
                binding.etDishDesc.setText(it.descripcion)
                binding.etDishPrice.setText(it.precio.toString())
                binding.etDishCategory.setText(it.categoria)
                binding.swDisponible.isChecked = it.disponible
                imagePath = it.imagen
                ImageUtils.loadImage(binding.ivDishPreview, imagePath)
            }
        }
    }

    private fun guardarPlatillo() {
        val platillo = getPlatilloFromForm() ?: return
        platilloViewModel.insert(platillo)
        DialogUtils.mostrarAlerta(this, "¡Éxito!", "Platillo registrado.", true) { finish() }
    }

    private fun actualizarPlatillo() {
        val platillo = getPlatilloFromForm() ?: return
        platilloViewModel.update(platillo.copy(id = dishId))
        DialogUtils.mostrarAlerta(this, "¡Éxito!", "Platillo actualizado.", true) { finish() }
    }

    private fun getPlatilloFromForm(): Platillo? {
        val nombre = binding.etDishName.text.toString().trim()
        val desc = binding.etDishDesc.text.toString().trim()
        val precio = binding.etDishPrice.text.toString().trim().toDoubleOrNull()
        val cat = binding.etDishCategory.text.toString().trim()
        val disponible = binding.swDisponible.isChecked

        if (nombre.isEmpty() || desc.isEmpty() || precio == null || cat.isEmpty()) {
            DialogUtils.mostrarAlerta(this, "Incompleto", "Llena todos los campos.")
            return null
        }

        return Platillo(
            restauranteId = restauranteId,
            nombre = nombre,
            descripcion = desc,
            precio = precio,
            categoria = cat,
            imagen = imagePath,
            disponible = disponible
        )
    }
}
