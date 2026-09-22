package com.example.foodexpress

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.foodexpress.databinding.ActivityMapBinding
import com.example.foodexpress.maps.MapPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private var selected: MapPoint? = null
    private var origin: MapPoint? = null
    private var picking = true
    private var description = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        picking = !intent.hasExtra("PEDIDO_ID")
        selected = if (savedInstanceState != null) {
            MapPoint.from(savedInstanceState.getDouble("lat", Double.NaN), savedInstanceState.getDouble("lng", Double.NaN))
        } else pointFromResult(intent)
        binding.mapToolbar.title = if (picking) intent.getStringExtra("title") ?: "Ubicación de entrega" else "Ruta del pedido"
        binding.mapToolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.mapToolbar.setNavigationOnClickListener { finish() }
        binding.confirmLocation.visibility = if (picking) View.VISIBLE else View.GONE
        binding.confirmLocation.isEnabled = selected != null
        binding.confirmLocation.setOnClickListener {
            selected?.let {
                setResult(RESULT_OK, Intent().putExtra("lat", it.latitude).putExtra("lng", it.longitude))
                finish()
            }
        }
        binding.mapWebView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "$userAgentString FoodExpress/1.0 (Android; com.example.foodexpress)"
        }
        // No JavaScript interface: the local page can only report a validated point.
        binding.mapWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val config = JSONObject().apply {
                    put("picking", picking)
                    put("point", selected?.let { JSONArray(listOf(it.latitude, it.longitude)) } ?: JSONObject.NULL)
                    put("origin", origin?.let { JSONArray(listOf(it.latitude, it.longitude)) } ?: JSONObject.NULL)
                }
                view.evaluateJavascript("window.initMap && window.initMap($config)", null)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (request.isForMainFrame && picking && uri.scheme == "foodexpress" && uri.host == "point") {
                    MapPoint.from(uri.getQueryParameter("lat")?.toDoubleOrNull(), uri.getQueryParameter("lng")?.toDoubleOrNull())?.let {
                        selected = it
                        binding.confirmLocation.isEnabled = true
                    }
                } else if (request.isForMainFrame && request.hasGesture() && uri.scheme == "https" &&
                    uri.host in setOf("www.openstreetmap.org", "leafletjs.com", "project-osrm.org")) {
                    runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }
                return true
            }
        }
        binding.retryMap.setOnClickListener { loadMap() }
        if (picking) {
            description = "Toca el mapa o arrastra el pin hasta la ubicación exacta. Puedes acercar y alejar el mapa."
            loadMap()
        } else {
            binding.retryMap.isEnabled = false
            lifecycleScope.launch {
                val pedido = (application as FoodExpressApp).pedidoRepository.getPedidoById(intent.getIntExtra("PEDIDO_ID", -1))
                selected = MapPoint.from(pedido?.entregaLat, pedido?.entregaLng)
                origin = MapPoint.from(pedido?.origenLat, pedido?.origenLng)
                description = if (pedido == null) "No se encontró el pedido."
                else "Pedido #${pedido.id} · ${pedido.estado.replace('_', ' ')}\n${pedido.direccionEntrega}\nRuta estimada; no muestra la ubicación en vivo del repartidor."
                binding.retryMap.isEnabled = true
                loadMap()
            }
        }
    }

    private fun loadMap() {
        binding.mapDescription.text = description
        binding.mapWebView.loadDataWithBaseURL("https://foodexpress.local/", assets.open("delivery_map.html").bufferedReader().use { it.readText() }, "text/html", "UTF-8", null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        selected?.let { outState.putDouble("lat", it.latitude); outState.putDouble("lng", it.longitude) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() { super.onResume(); binding.mapWebView.onResume() }
    override fun onPause() { binding.mapWebView.onPause(); super.onPause() }
    override fun onDestroy() {
        binding.mapWebView.stopLoading()
        binding.mapWebView.destroy()
        super.onDestroy()
    }

    companion object {
        fun pick(context: Context, title: String, point: MapPoint? = null): Intent =
            Intent(context, MapActivity::class.java).putExtra("title", title).apply {
                point?.let { putExtra("lat", it.latitude); putExtra("lng", it.longitude) }
            }
        fun route(context: Context, orderId: Int) = Intent(context, MapActivity::class.java).putExtra("PEDIDO_ID", orderId)
        fun pointFromResult(data: Intent?): MapPoint? = MapPoint.from(
            data?.getDoubleExtra("lat", Double.NaN), data?.getDoubleExtra("lng", Double.NaN))
    }
}
