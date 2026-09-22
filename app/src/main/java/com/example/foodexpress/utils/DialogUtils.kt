package com.example.foodexpress.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.foodexpress.R
import com.example.foodexpress.databinding.DialogAlertaBinding

object DialogUtils {

    fun mostrarAlerta(
        context: Context,
        titulo: String,
        mensaje: String,
        esExito: Boolean = false,
        onAceptar: (() -> Unit)? = null
    ) {
        val binding = DialogAlertaBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvTituloDialog.text = titulo
        binding.tvMensajeDialog.text = mensaje

        if (esExito) {
            binding.ivIconoDialog.setImageResource(android.R.drawable.checkbox_on_background)
            binding.ivIconoDialog.setColorFilter(context.getColor(R.color.verde_exito))
            binding.btnAceptarDialog.setBackgroundColor(context.getColor(R.color.verde_exito))
        } else {
            binding.ivIconoDialog.setImageResource(android.R.drawable.ic_dialog_alert)
            binding.ivIconoDialog.setColorFilter(context.getColor(R.color.rojo_carmesí))
            binding.btnAceptarDialog.setBackgroundColor(context.getColor(R.color.rojo_carmesí))
        }

        binding.btnAceptarDialog.setOnClickListener {
            dialog.dismiss()
            onAceptar?.invoke()
        }

        dialog.show()
    }

    fun mostrarConfirmacion(
        context: Context,
        titulo: String,
        mensaje: String,
        onConfirmar: () -> Unit
    ) {
        val binding = DialogAlertaBinding.inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)
        builder.setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.tvTituloDialog.text = titulo
        binding.tvMensajeDialog.text = mensaje
        
        // Icono de información azul para confirmación
        binding.ivIconoDialog.setImageResource(R.drawable.ic_info_circle)
        binding.ivIconoDialog.setColorFilter(context.getColor(R.color.azul_oscuro))
        
        binding.btnAceptarDialog.text = "Confirmar"
        binding.btnAceptarDialog.setBackgroundColor(context.getColor(R.color.azul_oscuro))

        binding.btnAceptarDialog.setOnClickListener {
            dialog.dismiss()
            onConfirmar()
        }

        dialog.show()
    }
}
