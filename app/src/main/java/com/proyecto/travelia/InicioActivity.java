package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class InicioActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        // ✅ Ajuste edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // ✅ Elevar Bottom Nav
        CardView bottomCard = findViewById(R.id.card_bottom_nav);
        if (bottomCard != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomCard, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int base = dp(10);
                lp.bottomMargin = base + sys.bottom;
                v.setLayoutParams(lp);
                return insets;
            });
        }

        // ✅ --- MAPA ---
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // ✅ --- NAV BOTTOM ---
        setupBottomNavigation();

        // ✅ ✅ ✅ RECOMENDACIONES DINÁMICAS ✅ ✅ ✅
        LinearLayout container = findViewById(R.id.container_recomendaciones);
        LayoutInflater inflater = LayoutInflater.from(this);

        CardData[] items = new CardData[]{
                new CardData("T-001", "Machu Picchu Full Day", "Cusco, Perú", "S/280", "★★★★☆", "4.8 • 230 reseñas", R.drawable.mapi),
                new CardData("T-002", "Lago Titicaca", "Puno, Perú", "S/380", "★★★★☆", "4.7 • 156 reseñas", R.drawable.lagotiticaca),
                new CardData("T-003", "Montaña de 7 Colores", "Cusco, Perú", "S/350", "★★★★☆", "4.6 • 190 reseñas", R.drawable.montanacolores)
        };

        for (CardData item : items) {
            View card = inflater.inflate(R.layout.card_recomendacion, container, false);

            ImageView iv = card.findViewById(R.id.iv_destino);
            TextView tvNombre = card.findViewById(R.id.tv_nombre_destino);
            TextView tvPrecio = card.findViewById(R.id.tv_precio);

            iv.setImageResource(item.imagen);
            tvNombre.setText(item.nombre);
            tvPrecio.setText(item.precio);

            card.setOnClickListener(v ->
                    Toast.makeText(this, "Abrir: " + item.nombre, Toast.LENGTH_SHORT).show()
            );

            container.addView(card);
        }
    }

    // ✅ BOTTOM NAV
    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navExplorar = findViewById(R.id.nav_explorar);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navFavoritos = findViewById(R.id.nav_favorites);
        LinearLayout navReservar = findViewById(R.id.nav_reserve);

        if (navHome != null)
            navHome.setOnClickListener(v -> Toast.makeText(this, "Ya estás en Home", Toast.LENGTH_SHORT).show());

        if (navExplorar != null)
            navExplorar.setOnClickListener(v -> {
                startActivity(new Intent(this, ExplorarActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });

        if (navAdd != null)
            navAdd.setOnClickListener(v -> Toast.makeText(this, "Función agregar pendiente", Toast.LENGTH_SHORT).show());

        if (navFavoritos != null)
            navFavoritos.setOnClickListener(v -> {
                startActivity(new Intent(this, com.proyecto.travelia.favoritos.FavoritosActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });

        if (navReservar != null) {
            navReservar.setOnClickListener(v -> {
                // Abre la pantalla de carrito/compra (o cambia a ConfirmarReservaActivity si prefieres)
                startActivity(new Intent(this, ComprarActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }

    // ✅ MAPA
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMaps = googleMap;

        LatLng cusco = new LatLng(-13.5163163, -71.9783294);
        mMaps.addMarker(new MarkerOptions().position(cusco).title("Cusco"));

        LatLng ucontinental = new LatLng(-13.5497992, -71.912017);
        mMaps.addMarker(new MarkerOptions().position(ucontinental).title("U. Continental"));

        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(cusco, 12f));
    }
}
