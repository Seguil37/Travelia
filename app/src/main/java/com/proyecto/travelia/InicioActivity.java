package com.proyecto.travelia;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.proyecto.travelia.data.Constantes;
import com.proyecto.travelia.data.ReviewRepository;
import com.proyecto.travelia.data.TourData;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InicioActivity extends BaseActivity implements OnMapReadyCallback {
    private GoogleMap mMaps;
    private AutoCompleteTextView etSearchMap;

    // UI Elements
    private ExtendedFloatingActionButton btnDirections;
    private ImageButton btnMapLayers;
    private CardView panelMapLayers; // Panel de capas

    // Datos de Ruta
    private LatLng currentDestinationLatLng;
    private LatLng myCurrentLocation;
    private Polyline currentPolyline;

    // Utilidades
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private RequestQueue volleyQueue;
    private FirebaseFirestore db;
    private ReviewRepository reviewRepository;
    private final List<TourData.TourInfo> recommendedTours = new ArrayList<>();
    private final Map<String, RatingSnapshot> ratingSnapshots = new HashMap<>();
    private LinearLayout recommendationsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        setupTopBar();
        // Inicializar cliente de volley
        volleyQueue = Volley.newRequestQueue(this);

        // Inicializar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ajuste edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        // 1. Saludo Personalizado
        TextView tvSaludo = findViewById(R.id.tv_saludo);
        SharedPreferences prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
        String nombreGuardado = prefs.getString(Constantes.KEY_NAME, "Usuario");
        tvSaludo.setText("HOLA " + nombreGuardado.toUpperCase(Locale.ROOT));


        // 2. Buscador
        etSearchMap = findViewById(R.id.et_search_map);
        setupAutoComplete();

        etSearchMap.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchLocation(etSearchMap.getText().toString());
                return true;
            }
            return false;
        });

        // Listener clic en sugerencia
        etSearchMap.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = (String) parent.getItemAtPosition(position);
            searchLocation(selectedPlace);
        });

        // Configuración de Botones
        btnDirections = findViewById(R.id.btn_directions);
        // CAMBIO: Ahora llama a drawRoute en lugar de startNavigation
        btnDirections.setOnClickListener(v -> drawRoute());

        btnMapLayers = findViewById(R.id.btn_map_layers);
        panelMapLayers = findViewById(R.id.panel_map_layers);

        // Toggle del panel
        btnMapLayers.setOnClickListener(v -> {
            if (panelMapLayers.getVisibility() == View.VISIBLE) {
                panelMapLayers.setVisibility(View.GONE);
            } else {
                panelMapLayers.setVisibility(View.VISIBLE);
            }
        });
        // Opciones del panel
        findViewById(R.id.opt_normal).setOnClickListener(v -> changeMapType(GoogleMap.MAP_TYPE_NORMAL));
        findViewById(R.id.opt_satellite).setOnClickListener(v -> changeMapType(GoogleMap.MAP_TYPE_HYBRID));
        findViewById(R.id.opt_terrain).setOnClickListener(v -> changeMapType(GoogleMap.MAP_TYPE_TERRAIN));

        reviewRepository = new ReviewRepository(this);
        recommendationsContainer = findViewById(R.id.container_recomendaciones);
        TextView tvVerTodo = findViewById(R.id.tv_ver_todo);
        if (tvVerTodo != null) {
            tvVerTodo.setOnClickListener(v -> {
                Intent intent = new Intent(this, ExplorarActivity.class);
                startActivity(intent);
            });
        }

        // 4. Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // 5. Cargar Secciones (Usando los métodos auxiliares para no duplicar código)
        setupRecommendations();
        setupBottomNav();
        db = FirebaseFirestore.getInstance();
    }

    private void changeMapType(int type) {
        if (mMaps != null) {
            mMaps.setMapType(type);
            panelMapLayers.setVisibility(View.GONE); // Cerrar panel al seleccionar
        }
    }
    // --- METODO: DIBUJAR RUTA EN EL MAPA (Interno) ---
    private void drawRoute() {
        if (myCurrentLocation == null) {
            Toast.makeText(this, "Obteniendo tu ubicación...", Toast.LENGTH_SHORT).show();
            enableMyLocation();
            return;
        }
        if (currentDestinationLatLng == null) {
            Toast.makeText(this, "Primero busca un destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir URL para Google Directions API
        String url = getDirectionsUrl(myCurrentLocation, currentDestinationLatLng);

        // Petición con Volley
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Parsear JSON
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray routes = jsonResponse.getJSONArray("routes");

                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);

                            // Google nos da una línea codificada en "overview_polyline"
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedString = overviewPolyline.getString("points");

                            // Decodificar y dibujar
                            List<LatLng> list = decodePoly(encodedString);

                            if (currentPolyline != null) currentPolyline.remove(); // Borrar anterior

                            PolylineOptions options = new PolylineOptions()
                                    .addAll(list)
                                    .width(15) // Más gruesa
                                    .color(Color.parseColor("#FF9800")) // Naranja Travelia
                                    .geodesic(true);

                            currentPolyline = mMaps.addPolyline(options);
                            // Zoom para ver toda la ruta
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LatLng p : list) builder.include(p);
                            try {
                                mMaps.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                            } catch (Exception e) { /* Ignorar */ }
                        } else {
                            Toast.makeText(InicioActivity.this, "No se encontró ruta (Revisa API Key/Billing)", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(InicioActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show());
        volleyQueue.add(stringRequest);
        Toast.makeText(this, "Calculando ruta...", Toast.LENGTH_SHORT).show();
    }
    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=driving";
        String key = "key=" + getString(R.string.google_maps_key); // Usa tu API Key
        return "https://maps.googleapis.com/maps/api/directions/json?" + str_origin + "&" + str_dest + "&" + mode + "&" + key;
    }

    // Algoritmo de Google para decodificar la línea del mapa
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
    // ==========================================
    // CONFIGURACIÓN DEL MAPA Y UI
    // ==========================================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMaps = googleMap;
        try {
            // Estilo personalizado (JSON)
            boolean success = mMaps.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) Log.e("Map", "Error en estilo");
        } catch (Resources.NotFoundException e) {
            Log.e("Map", "No se encuentra el estilo", e);
        }
        enableMyLocation();
        mMaps.getUiSettings().setZoomControlsEnabled(true);
        mMaps.getUiSettings().setMapToolbarEnabled(false);
        cargarPublicacionesEnMapa();
    }
    private void searchLocation(String locationName) {
        if (locationName == null || locationName.isEmpty()) return;
        if (mMaps == null) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                currentDestinationLatLng = latLng; // Guardar destino

                mMaps.clear();
                mMaps.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(locationName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                mMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                hideKeyboard();
                btnDirections.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Lugar no encontrado", Toast.LENGTH_SHORT).show();
                btnDirections.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setupAutoComplete() {
        String[] places = {
                "Machu Picchu", "Cusco", "Plaza de Armas Cusco", "Sacsayhuamán",
                "Montaña de 7 Colores", "Laguna Humantay", "Ollantaytambo",
                "Pisac", "Moray", "Salineras de Maras", "Qorikancha",
                "Lima", "Miraflores", "Barranco", "Arequipa", "Lago Titicaca", "Puno"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, places);
        etSearchMap.setAdapter(adapter);
    }
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMaps.setMyLocationEnabled(true);
        mMaps.getUiSettings().setMyLocationButtonEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 14f));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setupRecommendations() {
        recommendedTours.clear();
        recommendedTours.addAll(TourData.getTours());

        for (TourData.TourInfo info : recommendedTours) {
            observeRating(info.id);
        }

        renderRecommendations();
    }

    private void observeRating(String tourId) {
        if (tourId == null || ratingSnapshots.containsKey(tourId)) return;

        reviewRepository.observeAverage(tourId).observe(this, avg -> {
            RatingSnapshot snapshot = ratingSnapshots.computeIfAbsent(tourId, k -> new RatingSnapshot());
            snapshot.average = avg != null ? avg : 0d;
            snapshot.hasAverage = avg != null;
            renderRecommendations();
        });

        reviewRepository.observeCount(tourId).observe(this, count -> {
            RatingSnapshot snapshot = ratingSnapshots.computeIfAbsent(tourId, k -> new RatingSnapshot());
            snapshot.count = count != null ? count : 0;
            renderRecommendations();
        });
    }

    private void renderRecommendations() {
        if (recommendationsContainer == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        recommendationsContainer.removeAllViews();

        List<TourData.TourInfo> sorted = new ArrayList<>(recommendedTours);
        Collections.sort(sorted, (a, b) -> Double.compare(getEffectiveRating(b), getEffectiveRating(a)));

        for (TourData.TourInfo item : sorted) {
            View card = inflater.inflate(R.layout.card_recomendacion, recommendationsContainer, false);
            ImageView iv = card.findViewById(R.id.iv_destino);
            TextView tvNombre = card.findViewById(R.id.tv_nombre_destino);
            TextView tvPrecio = card.findViewById(R.id.tv_precio);
            TextView tvPrecioBadge = card.findViewById(R.id.tv_precio_badge);

            if (iv != null) iv.setImageResource(item.imageRes);
            if (tvNombre != null) tvNombre.setText(item.title);
            String priceText = item.formatPrice();
            if (tvPrecio != null) tvPrecio.setText(priceText);
            if (tvPrecioBadge != null) tvPrecioBadge.setText(priceText);

            card.setOnClickListener(v -> openDetalle(item));
            recommendationsContainer.addView(card);
        }
    }

    private double getEffectiveRating(TourData.TourInfo info) {
        if (info == null) return 0d;
        RatingSnapshot snapshot = ratingSnapshots.get(info.id);
        if (snapshot != null && snapshot.hasAverage && snapshot.count > 0) return snapshot.average;
        return info.defaultRating;
    }

    private void openDetalle(TourData.TourInfo info) {
        Intent intent = new Intent(this, DetalleArticuloActivity.class);
        intent.putExtra("id", info.id);
        intent.putExtra("titulo", info.title);
        intent.putExtra("ubicacion", info.location);
        intent.putExtra("precio", info.formatPrice());
        intent.putExtra("rating", String.format(Locale.getDefault(), "%.1f", getEffectiveRating(info)));
        intent.putExtra("imageRes", info.imageRes);
        startActivity(intent);
    }

    static class RatingSnapshot {
        double average;
        int count;
        boolean hasAverage;
    }

    private void setupBottomNav() {
    }
    // --- NUEVO MÉTODO: LEER DE FIREBASE Y PINTAR EN MAPA ---
    private void cargarPublicacionesEnMapa() {
        if (mMaps == null) return;

        db.collection("publicaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 1. Sacar los datos de la nube
                            Double lat = document.getDouble("latitud");
                            Double lon = document.getDouble("longitud");
                            String titulo = document.getString("titulo");
                            String descripcion = document.getString("descripcion"); // Opcional

                            // 2. Verificar que no sean nulos (o 0.0)
                            if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {

                                // 3. Crear el marcador
                                LatLng posicion = new LatLng(lat, lon);

                                mMaps.addMarker(new MarkerOptions()
                                        .position(posicion)
                                        .title(titulo) // ¡Aquí va el título que pusiste!
                                        .snippet(descripcion)); // Subtítulo al hacer clic
                            }
                        }
                        Log.d("FirebaseMap", "Marcadores cargados exitosamente");
                    } else {
                        Log.e("FirebaseMap", "Error cargando documentos", task.getException());
                    }
                });
    }
}
