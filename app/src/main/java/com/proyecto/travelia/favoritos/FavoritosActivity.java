package com.proyecto.travelia.favoritos;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;
import com.proyecto.travelia.ui.BottomNavView;

import java.util.List;

public class FavoritosActivity extends AppCompatActivity {

    private FavoritesRepository repo;
    private FavoritosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favoritos);

        // Edge-to-edge: el BottomNav maneja el margen inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        // Recycler
        repo = new FavoritesRepository(this);
        RecyclerView rv = findViewById(R.id.rvFavoritos);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new FavoritosAdapter(repo, this);
        rv.setAdapter(adapter);

        // Observa cambios de Room en tiempo real
        repo.observeAll().observe(this, (List<FavoriteEntity> list) -> {
            adapter.submit(list);
            TextView title = findViewById(R.id.tvTitle);
            if (title != null) title.setText("Mis favoritos (" + list.size() + ")");
        });

        // BottomNav: acción especial para Agregar (opcional)
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.setOnAddClickListener(v -> {
                // TODO: abre tu Activity de creación/publicación si aplica
                // startActivity(new Intent(this, CrearPublicacionActivity.class));
            });
            // bottom.setFinishOnNavigate(false); // si no quieres cerrar al navegar
        }
    }
}
