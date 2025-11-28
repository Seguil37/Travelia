package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.proyecto.travelia.data.ReservationsRepository;
import com.proyecto.travelia.data.local.ReservationEntity;
import com.proyecto.travelia.ui.BottomNavView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfirmarCompraActivity extends BaseActivity {

    private Button btnVerViajes;
    private LinearLayout layoutReservas;
    private TextView tvEmpty;
    private TextView tvTotalPagado;
    private android.view.View viewDivider;

    private ReservationsRepository reservationsRepository;
    private List<ReservationEntity> currentReservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirmar_compra);
        setupTopBar();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        initViews();
        setupBottomNavNew();
        initReservationsObserver();
        setupListeners();
        setupBackPressedHandler();
    }

    private void initViews() {
        btnVerViajes = findViewById(R.id.btn_ver_viajes);
        layoutReservas = findViewById(R.id.layout_compra_reservas);
        tvEmpty = findViewById(R.id.tv_empty_compra);
        tvTotalPagado = findViewById(R.id.tv_total_pagado);
        viewDivider = findViewById(R.id.view_compra_divider);
    }

    private void setupListeners() {
        btnVerViajes.setOnClickListener(v -> clearAndGoHome());
    }

    private void initReservationsObserver() {
        reservationsRepository = new ReservationsRepository(this);
        reservationsRepository.observeAll().observe(this, this::renderResumen);
    }

    private void renderResumen(List<ReservationEntity> reservas) {
        layoutReservas.removeAllViews();

        if (reservas == null || reservas.isEmpty()) {
            currentReservations = new ArrayList<>();
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            viewDivider.setVisibility(android.view.View.GONE);
            tvTotalPagado.setText(String.format(Locale.getDefault(), "S/%.2f", 0f));
            return;
        }

        currentReservations = new ArrayList<>(reservas);
        tvEmpty.setVisibility(android.view.View.GONE);
        viewDivider.setVisibility(android.view.View.VISIBLE);

        double total = 0d;

        for (ReservationEntity entity : currentReservations) {
            android.view.View view = getLayoutInflater().inflate(R.layout.item_card_tour, layoutReservas, false);

            TextView tvTitulo = view.findViewById(R.id.tv_titulo);
            TextView tvUbicacion = view.findViewById(R.id.tv_ubicacion);
            View metaContainer = view.findViewById(R.id.reservation_meta_container);
            TextView tvFecha = view.findViewById(R.id.tv_reserva_fecha);
            TextView tvParticipantes = view.findViewById(R.id.tv_reserva_participantes);
            TextView tvPrecio = view.findViewById(R.id.tv_precio_desde);
            TextView tvEstrellas = view.findViewById(R.id.tv_rating_estrellas);
            TextView tvRatingTxt = view.findViewById(R.id.tv_rating_texto);
            ImageView ivImagen = view.findViewById(R.id.iv_destino_big);
            ImageButton btnEliminar = view.findViewById(R.id.btn_eliminar_reserva);
            Button btnVerDetalles = view.findViewById(R.id.btn_ver_detalles);
            ImageView ivFav = view.findViewById(R.id.iv_favorito);

            tvTitulo.setText(entity.title);
            tvUbicacion.setText(entity.location);
            metaContainer.setVisibility(View.VISIBLE);
            tvFecha.setText("Fecha: " + entity.date);
            tvParticipantes.setText("Personas: " + entity.participants);
            tvPrecio.setText(String.format(Locale.getDefault(), "S/%.2f", entity.price));
            tvEstrellas.setText("☆☆☆☆☆");
            tvRatingTxt.setText("Sin reseñas");
            btnVerDetalles.setVisibility(View.GONE);
            ivFav.setVisibility(View.GONE);
            if (entity.imageRes != 0) {
                ivImagen.setImageResource(entity.imageRes);
            }

            btnEliminar.setVisibility(android.view.View.GONE);

            layoutReservas.addView(view);
            total += entity.price;
        }

        tvTotalPagado.setText(String.format(Locale.getDefault(), "S/%.2f", total));
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                clearAndGoHome();
            }
        });
    }

    private void clearAndGoHome() {
        if (reservationsRepository != null) {
            reservationsRepository.clearAll();
        }
        Intent intent = new Intent(ConfirmarCompraActivity.this, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavNew() {
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.highlight(BottomNavView.Tab.RESERVE);
        }
    }
}
