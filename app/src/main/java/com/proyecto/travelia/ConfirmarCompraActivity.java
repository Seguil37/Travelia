package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfirmarCompraActivity extends AppCompatActivity {

    private Button btnVerViajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_compra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        initViews();
        setupBottomNav();
        setupListeners();
        setupBackPressedHandler();
    }

    private void initViews() {
        btnVerViajes = findViewById(R.id.btn_ver_viajes);
    }

    private void setupListeners() {
        btnVerViajes.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmarCompraActivity.this, InicioActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBackPressedHandler() {
        // Manejo moderno del botón atrás (AndroidX)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Evitar que el usuario regrese atrás después de confirmar la compra
                Intent intent = new Intent(ConfirmarCompraActivity.this, InicioActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navExplorar = findViewById(R.id.nav_explorar);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navFavorites = findViewById(R.id.nav_favorites);
        LinearLayout navReserve = findViewById(R.id.nav_reserve);

        if (navHome != null) {
            navHome.setClickable(true);
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(ConfirmarCompraActivity.this, InicioActivity.class));
                finish();
            });
        }
        if (navExplorar != null) {
            navExplorar.setClickable(true);
            navExplorar.setOnClickListener(v -> {
                startActivity(new Intent(ConfirmarCompraActivity.this, ExplorarActivity.class));
                finish();
            });
        }
        if (navAdd != null) {
            navAdd.setClickable(true);
            navAdd.setOnClickListener(v ->
                    Toast.makeText(this, "Función agregar pendiente", Toast.LENGTH_SHORT).show());
        }
        if (navFavorites != null) {
            navFavorites.setClickable(true);
            navFavorites.setOnClickListener(v ->
                    Toast.makeText(this, "Abrir favoritos", Toast.LENGTH_SHORT).show());
        }
        if (navReserve != null) {
            navReserve.setClickable(true);
            navReserve.setOnClickListener(v ->
                    Toast.makeText(this, "Abrir reservas", Toast.LENGTH_SHORT).show());
        }
    }
}