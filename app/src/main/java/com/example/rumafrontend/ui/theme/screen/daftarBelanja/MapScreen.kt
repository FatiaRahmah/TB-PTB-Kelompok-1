package com.example.rumafrontend.ui.theme.screen.daftarBelanja

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.net.URL
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var mapView: MapView? by remember { mutableStateOf(null) }
    var locationCallback: LocationCallback? by remember { mutableStateOf(null) }

    
    DisposableEffect(Unit) {
        onDispose {
            locationCallback?.let { fusedClient.removeLocationUpdates(it) }
            mapView?.onPause()
            mapView?.onDetach()
            mapView = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toko Terdekat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7A2322),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5ECDC))
        ) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->

                    MapView(ctx).apply {
                        mapView = this

                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)

                        
                        controller.setCenter(GeoPoint(-6.200000, 106.816666)) 

                        if (
                            ContextCompat.checkSelfPermission(
                                ctx,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) return@apply

                        
                        val myLocationOverlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(ctx),
                            this
                        )
                        myLocationOverlay.enableMyLocation()
                        overlays.add(myLocationOverlay)

                        var userMarker: Marker? = null
                        val shopMarkers = mutableListOf<Marker>()

                        val request = LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            3000L
                        )
                            .setMinUpdateDistanceMeters(5f)
                            .build()

                        val callback = object : LocationCallback() {
                            override fun onLocationResult(result: LocationResult) {
                                val loc = result.lastLocation ?: return
                                val userPoint = GeoPoint(loc.latitude, loc.longitude)

                                controller.animateTo(userPoint)

                                
                                userMarker?.let { overlays.remove(it) }
                                userMarker = Marker(this@apply).apply {
                                    position = userPoint
                                    title = "Lokasi Anda"
                                    setAnchor(
                                        Marker.ANCHOR_CENTER,
                                        Marker.ANCHOR_BOTTOM
                                    )
                                }
                                overlays.add(userMarker)

                                
                                if (shopMarkers.isEmpty()) {
                                    scope.launch {
                                        val shops = fetchNearbyShops(
                                            lat = userPoint.latitude,
                                            lon = userPoint.longitude
                                        )

                                        shops.forEach { shop ->
                                            val marker = Marker(this@apply).apply {
                                                position = GeoPoint(shop.lat, shop.lon)
                                                title = shop.name
                                                setAnchor(
                                                    Marker.ANCHOR_CENTER,
                                                    Marker.ANCHOR_BOTTOM
                                                )
                                            }
                                            shopMarkers.add(marker)
                                            overlays.add(marker)
                                        }
                                        invalidate()
                                    }
                                }
                            }
                        }

                        locationCallback = callback
                        fusedClient.requestLocationUpdates(
                            request,
                            callback,
                            ctx.mainLooper
                        )
                    }
                }
            )
        }
    }
}

data class NearbyShop(
    val name: String,
    val lat: Double,
    val lon: Double
)

suspend fun fetchNearbyShops(
    lat: Double,
    lon: Double,
    radius: Int = 5000
): List<NearbyShop> = withContext(Dispatchers.IO) {

    try {
        
        val query = """
            [out:json];
            (
              node["shop"~"convenience|supermarket|grocery|general|department_store|wholesale"]
              (around:$radius,$lat,$lon);
            );
            out;
        """.trimIndent()

        val url =
            "https://overpass-api.de/api/interpreter?data=${
                URLEncoder.encode(query, "UTF-8")
            }"

        val response = URL(url).readText()
        val json = JSONObject(response)
        val elements = json.getJSONArray("elements")

        val result = mutableListOf<NearbyShop>()

        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            if (!el.has("lat") || !el.has("lon") || !el.has("tags")) continue

            val tags = el.getJSONObject("tags")

            val name = tags.optString(
                "name",
                tags.optString("shop", "Toko")
            )

            result.add(
                NearbyShop(
                    name = name,
                    lat = el.getDouble("lat"),
                    lon = el.getDouble("lon")
                )
            )
        }

        result
    } catch (e: Exception) {
        emptyList()
    }
}