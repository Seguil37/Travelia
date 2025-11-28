package com.proyecto.travelia.favoritos;

import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Rect;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.proyecto.travelia.BaseActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.ReviewRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.List;

public class FavoritosActivity extends BaseActivity {

    private FavoritesRepository repo;
    private ReviewRepository reviewRepository;
    private FavoritosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favoritos);
        setupTopBar();

        // Edge-to-edge: el BottomNav maneja el margen inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        // Recycler
        repo = new FavoritesRepository(this);
        reviewRepository = new ReviewRepository(this);
        RecyclerView rv = findViewById(R.id.rvFavoritos);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);

        // Agregar espaciado entre items
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int spacing = getResources().getDimensionPixelSize(R.dimen.card_grid_spacing);
                outRect.left = spacing;
                outRect.right = spacing;
                outRect.top = spacing;
                outRect.bottom = spacing;
            }
        });

        adapter = new FavoritosAdapter(repo, reviewRepository, this, this);
        rv.setAdapter(adapter);

        // Observa cambios de Room en tiempo real
        repo.observeAll().observe(this, (List<FavoriteEntity> list) -> {
            adapter.submit(list);
            TextView title = findViewById(R.id.tvTitle);
            if (title != null) title.setText("Mis favoritos (" + list.size() + ")");
        });

        // BottomNav usa la acción predeterminada para agregar
    }
}