package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.concurrent.Executors;

public class ExplorarActivity extends AppCompatActivity {

    private FavoritesRepository favRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explorar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        favRepo = new FavoritesRepository(this);

        setupBottomNav();

        Spinner spOrden = findViewById(R.id.sp_orden);
        String[] opciones = new String[]{"Nombre", "Precio", "Rating", "Nuevos"};
        spOrden.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opciones));

        Button btnFiltros = findViewById(R.id.btn_filtros);
        btnFiltros.setOnClickListener(v ->
                Toast.makeText(this, "Abrir filtros", Toast.LENGTH_SHORT).show());

        Button btnMas = findViewById(R.id.btn_mas_rutas);
        btnMas.setOnClickListener(v ->
                Toast.makeText(this, "Cargar más rutas…", Toast.LENGTH_SHORT).show());

        GridLayout grid = findViewById(R.id.grid_rutas);
        populateGridWithCards(grid);
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navExplorar = findViewById(R.id.nav_explorar);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navFavorites = findViewById(R.id.nav_favorites);
        LinearLayout navReserve = findViewById(R.id.nav_reserve);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(ExplorarActivity.this, InicioActivity.class));
                finish();
            });
        }
        if (navExplorar != null) {
            navExplorar.setOnClickListener(v ->
                    Toast.makeText(this, "Ya estás en Explorar", Toast.LENGTH_SHORT).show());
        }
        if (navAdd != null) {
            navAdd.setOnClickListener(v ->
                    Toast.makeText(this, "Función agregar pendiente", Toast.LENGTH_SHORT).show());
        }
        if (navFavorites != null) {
            navFavorites.setOnClickListener(v -> {
                startActivity(new Intent(this, com.proyecto.travelia.favoritos.FavoritosActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }
        if (navReserve != null) {
            navReserve.setOnClickListener(v ->
                    Toast.makeText(this, "Abrir reservas", Toast.LENGTH_SHORT).show());
        }
    }

    private void populateGridWithCards(GridLayout container) {
        LayoutInflater inflater = LayoutInflater.from(this);

        CardData[] items = new CardData[]{
                new CardData("T-001", "Machu Picchu Full Day", "Cusco, Perú", "S/280", "★★★★☆", "4.8 • 230 reseñas", R.drawable.mapi),
                new CardData("T-002", "Lago Titicaca", "Puno, Perú", "S/380", "★★★★☆", "4.7 • 156 reseñas", R.drawable.lagotiticaca),
                new CardData("T-003", "Montaña de 7 Colores", "Cusco, Perú", "S/350", "★★★★☆", "4.6 • 190 reseñas", R.drawable.montanacolores)
        };

        for (CardData d : items) {
            View card = inflater.inflate(R.layout.card_destino, container, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);

            ImageView img = card.findViewById(R.id.iv_destino_big);
            TextView tvTitulo = card.findViewById(R.id.tv_titulo);
            TextView tvUbic = card.findViewById(R.id.tv_ubicacion);
            TextView tvPrecio = card.findViewById(R.id.tv_precio_desde);
            TextView tvEst = card.findViewById(R.id.tv_rating_estrellas);
            TextView tvRat = card.findViewById(R.id.tv_rating_texto);
            Button btnDetalles = card.findViewById(R.id.btn_ver_detalles);
            ImageView ivFav = card.findViewById(R.id.iv_favorito);

            if (img != null) img.setImageResource(d.imageRes);
            if (tvTitulo != null) tvTitulo.setText(d.titulo);
            if (tvUbic != null) tvUbic.setText(d.ubicacion);
            if (tvPrecio != null) tvPrecio.setText(d.precio);
            if (tvEst != null) tvEst.setText(d.estrellas);
            if (tvRat != null) tvRat.setText(d.ratingTxt);

            // 🔹 FAVORITOS: cargar estado desde Room
            Executors.newSingleThreadExecutor().execute(() -> {
                boolean isFav = favRepo.isFavoriteSync(d.id);
                runOnUiThread(() -> ivFav.setSelected(isFav));
            });

            // 🔸 Evento de favorito
            ivFav.setOnClickListener(v -> {
                boolean nowSelected = !ivFav.isSelected();
                ivFav.setSelected(nowSelected);

                if (nowSelected) {
                    FavoriteEntity e = new FavoriteEntity(
                            "guest", d.id, "TOUR", d.titulo, d.ubicacion,
                            "img_local", Double.valueOf(d.precio.replace("S/", "")), 4.5,
                            System.currentTimeMillis()
                    );
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.add(e));

                    Snackbar.make(card, "¡Añadido a favoritos!", Snackbar.LENGTH_LONG)
                            .setAction("Deshacer", a -> {
                                ivFav.setSelected(false);
                                Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                            }).show();
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                    Snackbar.make(card, "Eliminado de favoritos", Snackbar.LENGTH_SHORT).show();
                }
            });

            // 🎯 Botón detalles
            if (btnDetalles != null) {
                btnDetalles.setOnClickListener(v -> {
                    Intent intent = new Intent(ExplorarActivity.this, DetalleArticuloActivity.class);
                    intent.putExtra("titulo", d.titulo);
                    intent.putExtra("ubicacion", d.ubicacion);
                    intent.putExtra("precio", d.precio);
                    intent.putExtra("rating", d.ratingTxt);
                    intent.putExtra("imageRes", d.imageRes);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            container.addView(card);
        }
    }

    static class CardData {
        final String id, titulo, ubicacion, precio, estrellas, ratingTxt;
        final int imageRes;
        CardData(String id, String t, String u, String p, String e, String r, int img) {
            this.id = id; this.titulo = t; this.ubicacion = u;
            this.precio = p; this.estrellas = e; this.ratingTxt = r;
            this.imageRes = img;
        }
    }
}
