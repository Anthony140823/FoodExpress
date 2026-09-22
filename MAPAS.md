# Mapas y rutas

## Probar el flujo

1. Entra como restaurante (`rest@foodexpress.com`, contraseña `123456`). Pulsa **Ubicar restaurante en el mapa**, toca el punto real del local y confirma. Crea al menos un plato.
2. Entra como cliente (`admin@foodexpress.com`, contraseña `123456`). Añade platos de ese restaurante. En el carrito escribe la dirección/referencia, pulsa **Elegir entrega en el mapa**, coloca o arrastra el pin y confirma. Después confirma el pedido.
3. Desde el historial abre el seguimiento y **Ver ruta en el mapa**. Se muestran recogida (R), entrega (E), recorrido por calles, kilómetros y minutos aproximados de conducción.
4. Procesa el pedido como restaurante hasta `LISTO_PARA_ENVIO`. Entra como repartidor (`delivery@foodexpress.com`, contraseña `123456`): cada pedido tiene **Ver ruta de entrega**, además de sus acciones de aceptar/entregar.

La selección permite desplazar y ampliar el mapa. Lima es solo el centro inicial, nunca una ubicación seleccionada automáticamente. La dirección escrita sigue siendo la referencia para la entrega; no se realiza geocodificación ni búsqueda de direcciones. El pin se conserva al rotar la pantalla y al volver del selector. Los pedidos nuevos requieren ambos puntos y platos de un solo restaurante.

## Servicios y límites

La pantalla usa un WebView con [Leaflet 1.9.4](https://leafletjs.com/examples/quick-start/), mapas de [OpenStreetMap](https://operations.osmfoundation.org/policies/tiles/) y el [servicio de rutas OSRM](https://project-osrm.org/docs/v5.24.0/api/). No requiere una clave de Google ni cambios en la configuración de Android. Requiere Internet para los recursos y las rutas. El código de Leaflet tiene integridad SRI; se bloquean contenido HTTP, acceso a archivos y navegación arbitraria. No hay interfaz JavaScript con acceso nativo.

OSRM recibe las coordenadas de origen y destino al abrir la ruta; no recibe nombres, teléfono, dirección escrita ni identificadores de pedidos. OpenStreetMap recibe las solicitudes de las zonas visibles. No se solicita GPS. El tiempo mostrado es de conducción, sin preparación ni tráfico en vivo. No se inventa un movimiento del repartidor ni se sincronizan teléfonos.

Son servicios públicos para este prototipo, sin garantía de disponibilidad. Antes de un despliegue comercial, contratar/alojar proveedores adecuados y revisar sus políticas. No hay precarga ni descarga masiva de mapas; se conserva la atribución y la caché HTTP predeterminada, con un User-Agent de FoodExpress. El proveedor y la URL de rutas se encuentran en `app/src/main/assets/delivery_map.html`.

Si falla Internet, Leaflet o el cálculo, la pantalla informa el problema y ofrece **Recargar mapa**. Si no hay una calle a menos de 200 m de los puntos, puede no haber ruta. No se dibuja una línea recta haciéndola pasar por una ruta. Pedidos antiguos sin coordenadas muestran una explicación; no se les asignan puntos ficticios.

## Datos y comprobaciones

SQLite pasa de versión 4 a 5 mediante columnas nuevas, conservando los datos de v4. Se almacenan latitud/longitud del restaurante y una copia de origen/destino en cada pedido. Cambiar posteriormente el restaurante no cambia pedidos existentes. La lógica heredada para versiones anteriores a v4 sigue recreando la base.

Comandos: `gradlew.bat assembleDebug testDebugUnitTest` y, con dispositivo/emulador conectado, `gradlew.bat connectedDebugAndroidTest`. Las pruebas nuevas cubren validación de coordenadas, migración v4→v5 y persistencia de puntos al cambiar el estado. Probar manualmente selección, cancelación, rotación, carrito mixto, falta de ubicación del restaurante, pedido antiguo, falta de conexión y recorrido entre dos puntos cercanos por calles.

También puede ejecutarse `node --test scripts/test-map.cjs`: cinco pruebas de la lógica del mapa (selección, arrastre, coordenadas ausentes, respuesta de rutas y errores), con Leaflet y red simulados. Estas cinco pruebas pasaron y una consulta real a OSRM en Lima devolvió una geometría de calles válida. Se validó la sintaxis JavaScript y XML. La compilación y las pruebas Android quedaron pendientes porque el entorno no tiene configurado el Android SDK (`SDK location not found`); no se ha verificado todavía la pantalla en un teléfono/emulador.
