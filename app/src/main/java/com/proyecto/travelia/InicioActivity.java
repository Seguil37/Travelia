package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.proyecto.travelia.ui.BottomNavView;

public class InicioActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        // ✅ Ajuste edge-to-edge (padding superior/inferior del contenedor raíz)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0); // bottom = 0, el BottomNav maneja su propio margen
            return insets;
        });

        // ✅ MAPA
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // ✅ RECOMENDACIONES DINÁMICAS (igual que ya tenías)
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

            card.setOnClickListener(v -> Toast.makeText(this, "Abrir: " + item.nombre, Toast.LENGTH_SHORT).show());
            container.addView(card);
        }

        // ✅ BottomNavView: acción especial para ADD (opcional)
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.setOnAddClickListener(v -> {
                // TODO: cambia a tu Activity real para crear/publicar
                // startActivity(new Intent(this, CrearPublicacionActivity.class));
                Toast.makeText(this, "Acción agregar", Toast.LENGTH_SHORT).show();
            });
            // Si quieres evitar que cierre esta activity al navegar:
            // bottom.setFinishOnNavigate(false);
        }
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

    // Clase auxiliar para las cards (si no la tienes ya)
    static class CardData {
        String id, nombre, lugar, precio, estrellas, resumen;
        int imagen;
        CardData(String id, String nombre, String lugar, String precio, String estrellas, String resumen, int imagen) {
            this.id = id; this.nombre = nombre; this.lugar = lugar; this.precio = precio; this.estrellas = estrellas; this.resumen = resumen; this.imagen = imagen;
        }
    }
}
