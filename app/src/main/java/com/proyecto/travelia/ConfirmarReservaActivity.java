package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfirmarReservaActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnContinuarCompra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_reserva);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvTotal = findViewById(R.id.tv_total);
        btnContinuarCompra = findViewById(R.id.btn_continuar_compra);
    }

    private void setupListeners() {
        btnContinuarCompra.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmarReservaActivity.this, ComprarActivity.class);
            startActivity(intent);
        });
    }
}