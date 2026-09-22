package com.example.foodexpress.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.databinding.ItemCarritoBinding
import com.example.foodexpress.models.CarritoItem
import com.example.foodexpress.utils.ImageUtils

class CarritoAdapter(
    private var items: List<CarritoItem>,
    private val onUpdate: (CarritoItem, Int) -> Unit,
    private val onDelete: (CarritoItem) -> Unit
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    class CarritoViewHolder(val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvCartNombre.text = item.nombrePlatillo
            tvCartPrecio.text = "S/ %.2f".format(item.precioUnitario)
            tvCartCantidad.text = item.cantidad.toString()
            ImageUtils.loadImage(ivCartPlatillo, item.imagen)

            btnCartPlus.setOnClickListener { onUpdate(item, item.cantidad + 1) }
            btnCartMinus.setOnClickListener { if (item.cantidad > 1) onUpdate(item, item.cantidad - 1) }
            btnRemove.setOnClickListener { onDelete(item) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<CarritoItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
