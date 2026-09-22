package com.example.foodexpress.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodexpress.databinding.ItemPedidoBinding
import com.example.foodexpress.models.Pedido

class PedidoAdapter(
    private var pedidos: List<Pedido>,
    private val onItemClick: (Pedido) -> Unit
) : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

    class PedidoViewHolder(val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val p = pedidos[position]
        holder.binding.apply {
            tvOrderDate.text = p.fecha
            tvOrderStatus.text = p.estado
            tvOrderTotal.text = "Total: S/ %.2f".format(p.total)
            root.setOnClickListener { onItemClick(p) }
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun updateList(newList: List<Pedido>) {
        pedidos = newList
        notifyDataSetChanged()
    }
}
