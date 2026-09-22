package com.example.foodexpress.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.databinding.ItemRestauranteBinding
import com.example.foodexpress.models.Restaurante
import com.example.foodexpress.utils.ImageUtils

class RestauranteAdapter(
    private var restaurantes: List<Restaurante>,
    private val onItemClick: (Restaurante) -> Unit
) : RecyclerView.Adapter<RestauranteAdapter.RestauranteViewHolder>() {

    class RestauranteViewHolder(val binding: ItemRestauranteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestauranteViewHolder {
        val binding = ItemRestauranteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RestauranteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestauranteViewHolder, position: Int) {
        val res = restaurantes[position]
        holder.binding.apply {
            tvNombreRestaurante.text = res.nombre
            tvCategoriaRestaurante.text = res.categoria
            tvCalificacion.text = res.calificacion.toString()
            tvTiempoEntrega.text = res.tiempoEntrega
            ImageUtils.loadImage(ivRestaurante, res.imagen)
            root.setOnClickListener { onItemClick(res) }
        }
    }

    override fun getItemCount(): Int = restaurantes.size

    fun updateList(newList: List<Restaurante>) {
        restaurantes = newList
        notifyDataSetChanged()
    }
}
