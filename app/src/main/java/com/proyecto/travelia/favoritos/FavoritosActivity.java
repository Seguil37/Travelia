package com.proyecto.travelia.favoritos;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.proyecto.travelia.ExplorarActivity;
import com.proyecto.travelia.InicioActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.List;

public class FavoritosActivity extends AppCompatActivity {

    private FavoritesRepository repo;
    private FavoritosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        // Elevar el BottomNav por encima del gesto inferior/IME
        CardView bottomCard = findViewById(R.id.card_bottom_nav);
        if (bottomCard != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomCard, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int base = dp(10); // marginBottom original del XML
                lp.bottomMargin = base + sys.bottom;
                v.setLayoutParams(lp);
                return insets;
            });
        }

        // Recycler
        repo = new FavoritesRepository(this);
        RecyclerView rv = findViewById(R.id.rvFavoritos);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new FavoritosAdapter(repo, this);
        rv.setAdapter(adapter);

        // Observar cambios de Room en tiempo real
        repo.observeAll().observe(this, (List<FavoriteEntity> list) -> adapter.submit(list));

        // Bottom Navigation
        setupBottomNavigation();
    }



    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navExplorar = findViewById(R.id.nav_explorar);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navFavorites = findViewById(R.id.nav_favorites);
        LinearLayout navReserve = findViewById(R.id.nav_reserve);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(FavoritosActivity.this, InicioActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
        if (navExplorar != null) {
            navExplorar.setOnClickListener(v -> {
                startActivity(new Intent(FavoritosActivity.this, ExplorarActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
        if (navAdd != null) {
            navAdd.setOnClickListener(v ->
                    Toast.makeText(FavoritosActivity.this, "Función agregar pendiente", Toast.LENGTH_SHORT).show()
            );
        }
        if (navFavorites != null) {
            // Ya estás aquí
            navFavorites.setOnClickListener(v ->
                    Toast.makeText(FavoritosActivity.this, "Ya estás en Favoritos", Toast.LENGTH_SHORT).show()
            );
        }
        if (navReserve != null) {
            navReserve.setOnClickListener(v ->
                    Toast.makeText(FavoritosActivity.this, "Abrir reservas", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }
}
