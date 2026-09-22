package com.example.foodexpress

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityDetailBinding
import com.example.foodexpress.models.CarritoItem
import com.example.foodexpress.models.Platillo
import com.example.foodexpress.ui.viewmodel.CarritoViewModel
import com.example.foodexpress.ui.viewmodel.CarritoViewModelFactory
import com.example.foodexpress.ui.viewmodel.PlatilloViewModel
import com.example.foodexpress.ui.viewmodel.PlatilloViewModelFactory
import com.example.foodexpress.utils.DialogUtils
import com.example.foodexpress.utils.ImageUtils
import com.example.foodexpress.utils.SessionManager
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var platillo: Platillo? = null
    private var cantidad = 1
    private lateinit var sessionManager: SessionManager
    
    private val platilloViewModel: PlatilloViewModel by viewModels {
        PlatilloViewModelFactory((application as FoodExpressApp).platilloRepository)
    }
    private val carritoViewModel: CarritoViewModel by viewModels {
        CarritoViewModelFactory((application as FoodExpressApp).carritoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val platilloId = intent.getIntExtra("PLATILLO_ID", -1)

        cargarDetallePlatillo(platilloId)

        binding.btnPlus.setOnClickListener {
            cantidad++
            binding.tvCantidad.text = cantidad.toString()
        }

        binding.btnMinus.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                binding.tvCantidad.text = cantidad.toString()
            }
        }

        binding.btnAddToCart.setOnClickListener {
            agregarAlCarrito()
        }
    }

    private fun cargarDetallePlatillo(id: Int) {
        lifecycleScope.launch {
            platillo = platilloViewModel.getPlatilloById(id)
            platillo?.let {
                binding.tvDetailNombre.text = it.nombre
                binding.tvDetailPrecio.text = "S/ %.2f".format(it.precio)
                binding.tvDetailDescripcion.text = it.descripcion
                ImageUtils.loadImage(binding.ivDetailPlatillo, it.imagen)
            }
        }
    }

    private fun agregarAlCarrito() {
        val p = platillo ?: return
        val userId = sessionManager.getUserId()
        
        val item = CarritoItem(
            usuarioId = userId,
            platilloId = p.id,
            nombrePlatillo = p.nombre,
            precioUnitario = p.precio,
            cantidad = cantidad,
            imagen = p.imagen
        )
        
        carritoViewModel.agregarAlCarrito(item)

        DialogUtils.mostrarAlerta(this, "¡Agregado!", "${p.nombre} se añadió a tu carrito.", true) {
            finish()
        }
    }
}
