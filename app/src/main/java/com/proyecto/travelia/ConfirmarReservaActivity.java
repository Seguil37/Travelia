package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.proyecto.travelia.data.ReservationsRepository;
import com.proyecto.travelia.data.local.ReservationEntity;
import com.proyecto.travelia.ui.BottomNavView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ConfirmarReservaActivity extends AppCompatActivity {

    private TextView tvTotal;
    private Button btnContinuarCompra;
    private LinearLayout layoutReservas;
    private TextView tvEmpty;

    private ReservationsRepository reservationsRepository;
    private List<ReservationEntity> currentReservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_reserva);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        initViews();
        setupBottomNav();
        initReservationsObserver();
        setupListeners();
    }

    private void initViews() {
        tvTotal = findViewById(R.id.tv_total);
        btnContinuarCompra = findViewById(R.id.btn_continuar_compra);
        layoutReservas = findViewById(R.id.layout_reservas);
        tvEmpty = findViewById(R.id.tv_empty_reservas);
        updateContinueButtonState(false);
    }

    private void setupListeners() {
        btnContinuarCompra.setOnClickListener(v -> {
            if (currentReservations.isEmpty()) {
                Toast.makeText(this, "Agrega un tour antes de continuar", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, ComprarActivity.class));
        });
    }

    private void setupBottomNav() {
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.highlight(BottomNavView.Tab.RESERVE);
        }
    }

    private void initReservationsObserver() {
        reservationsRepository = new ReservationsRepository(this);
        reservationsRepository.observeAll().observe(this, this::renderReservations);
    }

    private void renderReservations(List<ReservationEntity> reservations) {
        layoutReservas.removeAllViews();

        if (reservations == null || reservations.isEmpty()) {
            currentReservations = Collections.emptyList();
            tvEmpty.setVisibility(View.VISIBLE);
            tvTotal.setText(String.format(Locale.getDefault(), "S/%.2f", 0f));
            updateContinueButtonState(false);
            return;
        }

        currentReservations = new ArrayList<>(reservations);
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        double total = 0;

        for (ReservationEntity entity : currentReservations) {
            View view = inflater.inflate(R.layout.item_reserva, layoutReservas, false);

            TextView tvTitulo = view.findViewById(R.id.tv_reserva_titulo);
            TextView tvUbicacion = view.findViewById(R.id.tv_reserva_ubicacion);
            TextView tvFecha = view.findViewById(R.id.tv_reserva_fecha);
            TextView tvParticipantes = view.findViewById(R.id.tv_reserva_participantes);
            TextView tvPrecio = view.findViewById(R.id.tv_reserva_precio);
            ImageView ivImagen = view.findViewById(R.id.iv_reserva);
            ImageButton btnEliminar = view.findViewById(R.id.btn_eliminar_reserva);

            tvTitulo.setText(entity.title);
            tvUbicacion.setText(entity.location);
            tvFecha.setText(entity.date);
            tvParticipantes.setText(entity.participants);
            tvPrecio.setText(String.format(Locale.getDefault(), "S/%.2f", entity.price));
            if (entity.imageRes != 0) {
                ivImagen.setImageResource(entity.imageRes);
            }

            btnEliminar.setOnClickListener(v -> reservationsRepository.remove(entity.id));
            layoutReservas.addView(view);

            total += entity.price;
        }

        tvTotal.setText(String.format(Locale.getDefault(), "S/%.2f", total));
        updateContinueButtonState(true);
    }

    private void updateContinueButtonState(boolean enabled) {
        btnContinuarCompra.setEnabled(enabled);
        btnContinuarCompra.setAlpha(enabled ? 1f : 0.5f);
    }
}
