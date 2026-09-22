package com.example.foodexpress.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.databinding.ItemPlatilloBinding
import com.example.foodexpress.models.Platillo
import com.example.foodexpress.utils.ImageUtils

class PlatilloAdapter(
    private var platillos: List<Platillo>,
    private val onEditClick: ((Platillo) -> Unit)? = null,
    private val onDeleteClick: ((Platillo) -> Unit)? = null,
    private val onItemClick: (Platillo) -> Unit
) : RecyclerView.Adapter<PlatilloAdapter.PlatilloViewHolder>() {

    class PlatilloViewHolder(val binding: ItemPlatilloBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatilloViewHolder {
        val binding = ItemPlatilloBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlatilloViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlatilloViewHolder, position: Int) {
        val p = platillos[position]
        holder.binding.apply {
            tvNombrePlatillo.text = p.nombre
            tvDescripcionPlatillo.text = p.descripcion
            tvPrecioPlatillo.text = "S/ %.2f".format(p.precio)
            ImageUtils.loadImage(ivPlatillo, p.imagen)
            
            if (onEditClick != null && onDeleteClick != null) {
                llAdminActions.visibility = View.VISIBLE
                btnEditDish.setOnClickListener { onEditClick.invoke(p) }
                btnDeleteDish.setOnClickListener { onDeleteClick.invoke(p) }
                root.setOnClickListener(null)
            } else {
                llAdminActions.visibility = View.GONE
                root.setOnClickListener { onItemClick(p) }
            }
        }
    }

    override fun getItemCount(): Int = platillos.size

    fun updateList(newList: List<Platillo>) {
        platillos = newList
        notifyDataSetChanged()
    }
}
