package com.example.foodexpress.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    
    /**
     * Copia una imagen desde una URI a la carpeta interna de la app.
     * Retorna el Path absoluto del nuevo archivo.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "dish_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Carga una imagen en un ImageView. 
     * Soporta tanto rutas de archivos como nombres de recursos (para Seed Data).
     */
    fun loadImage(view: ImageView, path: String) {
        if (path.isEmpty()) return
        
        try {
            if (path.startsWith("/")) {
                // Es un archivo local
                view.setImageURI(Uri.fromFile(File(path)))
            } else {
                // Asumimos que es un ID de recurso (o String del ID)
                val resId = path.toIntOrNull()
                if (resId != null) {
                    view.setImageResource(resId)
                } else {
                    // Fallback a icono por defecto
                    view.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        } catch (e: Exception) {
            view.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }
}
