package com.proyecto.travelia;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.proyecto.travelia.data.ReservationsRepository;
import com.proyecto.travelia.data.local.ReservationEntity;
import com.proyecto.travelia.ui.BottomNavView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ComprarActivity extends AppCompatActivity {

    private EditText etNombres, etEmail, etTelefono;
    private Spinner spNacionalidad;
    private CardView cardDebito, cardPaypal, cardTransferencia, cardYape;
    private Button btnPagar, btnCancelar;
    private LinearLayout layoutResumen;
    private TextView tvEmptyResumen;
    private View viewResumenDivider;
    private TextView tvTotal;

    private String metodoSeleccionado = "";
    private ReservationsRepository reservationsRepository;
    private List<ReservationEntity> currentReservations = new ArrayList<>();
    private double totalActual = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comprar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        initViews();
        setupSpinners();
        setupBottomNav();
        initReservationsObserver();
        setupListeners();
    }

    private void initViews() {
        etNombres = findViewById(R.id.et_nombres);
        etEmail = findViewById(R.id.et_email);
        etTelefono = findViewById(R.id.et_telefono);
        spNacionalidad = findViewById(R.id.sp_nacionalidad);

        cardDebito = findViewById(R.id.card_debito);
        cardPaypal = findViewById(R.id.card_paypal);
        cardTransferencia = findViewById(R.id.card_transferencia);
        cardYape = findViewById(R.id.card_yape);

        btnPagar = findViewById(R.id.btn_pagar);
        btnCancelar = findViewById(R.id.btn_cancelar);

        layoutResumen = findViewById(R.id.layout_resumen_reservas);
        tvEmptyResumen = findViewById(R.id.tv_empty_resumen);
        viewResumenDivider = findViewById(R.id.view_resumen_divider);
        tvTotal = findViewById(R.id.tv_total);
    }

    private void setupSpinners() {
        String[] paises = {"Selecciona tu país", "Perú", "Argentina", "Chile", "Colombia",
                "México", "España", "Estados Unidos", "Brasil"};
        ArrayAdapter<String> paisesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paises);
        paisesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNacionalidad.setAdapter(paisesAdapter);
    }

    private void setupBottomNav() {
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.setOnAddClickListener(v ->
                    Toast.makeText(this, "Acción agregar (Compra)", Toast.LENGTH_SHORT).show()
            );
            bottom.highlight(BottomNavView.Tab.RESERVE);
        }
    }

    private void initReservationsObserver() {
        reservationsRepository = new ReservationsRepository(this);
        reservationsRepository.observeAll().observe(this, this::renderResumenReservas);
    }

    private void renderResumenReservas(List<ReservationEntity> reservas) {
        layoutResumen.removeAllViews();

        if (reservas == null || reservas.isEmpty()) {
            currentReservations = new ArrayList<>();
            tvEmptyResumen.setVisibility(View.VISIBLE);
            viewResumenDivider.setVisibility(View.GONE);
            totalActual = 0d;
            tvTotal.setText(String.format(Locale.getDefault(), "S/%.2f", totalActual));
            return;
        }

        currentReservations = new ArrayList<>(reservas);
        tvEmptyResumen.setVisibility(View.GONE);
        viewResumenDivider.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        totalActual = 0d;

        for (ReservationEntity entity : currentReservations) {
            View view = inflater.inflate(R.layout.item_reserva, layoutResumen, false);

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

            btnEliminar.setVisibility(View.GONE);

            layoutResumen.addView(view);
            totalActual += entity.price;
        }

        tvTotal.setText(String.format(Locale.getDefault(), "S/%.2f", totalActual));
    }

    private void setupListeners() {
        cardDebito.setOnClickListener(v -> {
            metodoSeleccionado = "debito";
            resetMetodosSeleccion();
            cardDebito.setCardElevation(12f);
            mostrarDialogoTarjeta();
        });

        cardPaypal.setOnClickListener(v -> {
            metodoSeleccionado = "paypal";
            resetMetodosSeleccion();
            cardPaypal.setCardElevation(12f);
            Toast.makeText(this, "PayPal seleccionado", Toast.LENGTH_SHORT).show();
        });

        cardTransferencia.setOnClickListener(v -> {
            metodoSeleccionado = "transferencia";
            resetMetodosSeleccion();
            cardTransferencia.setCardElevation(12f);
            Toast.makeText(this, "Transferencia seleccionada", Toast.LENGTH_SHORT).show();
        });

        cardYape.setOnClickListener(v -> {
            metodoSeleccionado = "yape";
            resetMetodosSeleccion();
            cardYape.setCardElevation(12f);
            Toast.makeText(this, "Yape/Plin seleccionado", Toast.LENGTH_SHORT).show();
        });

        btnPagar.setOnClickListener(v -> {
            if (currentReservations.isEmpty()) {
                Toast.makeText(this, "Tu carrito de reservas está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            if (validarDatos()) procesarPago();
        });

        btnCancelar.setOnClickListener(v -> finish());
    }

    private void resetMetodosSeleccion() {
        cardDebito.setCardElevation(4f);
        cardPaypal.setCardElevation(4f);
        cardTransferencia.setCardElevation(4f);
        cardYape.setCardElevation(4f);
    }

    private void mostrarDialogoTarjeta() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_tarjeta_credito);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etCardNumber = dialog.findViewById(R.id.et_card_number);
        EditText etExpiryDate = dialog.findViewById(R.id.et_expiry_date);
        EditText etCvv = dialog.findViewById(R.id.et_cvv);
        EditText etCardName = dialog.findViewById(R.id.et_card_name);
        Button btnCancelarTarjeta = dialog.findViewById(R.id.btn_cancelar_tarjeta);
        Button btnConfirmarTarjeta = dialog.findViewById(R.id.btn_confirmar_tarjeta);

        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String input = s.toString().replaceAll("\\s", "");
                if (input.length() <= 16) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(" ");
                        formatted.append(input.charAt(i));
                    }
                    s.replace(0, s.length(), formatted.toString());
                }
                isFormatting = false;
            }
        });

        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String input = s.toString().replaceAll("/", "");
                if (input.length() >= 2) {
                    s.replace(0, s.length(), input.substring(0, 2) + "/" + (input.length() > 2 ? input.substring(2) : ""));
                }
                isFormatting = false;
            }
        });

        btnCancelarTarjeta.setOnClickListener(v -> dialog.dismiss());

        btnConfirmarTarjeta.setOnClickListener(v -> {
            String cardNumber = etCardNumber.getText().toString().replaceAll("\\s", "");
            String expiryDate = etExpiryDate.getText().toString();
            String cvv = etCvv.getText().toString();
            String cardName = etCardName.getText().toString();

            if (cardNumber.length() < 13 || expiryDate.length() < 5 || cvv.length() < 3 || cardName.isEmpty()) {
                Toast.makeText(this, "Completa los datos de la tarjeta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tarjeta confirmada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validarDatos() {
        String nombres = etNombres.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        int nacionalidadPos = spNacionalidad.getSelectedItemPosition();

        if (nombres.isEmpty()) { etNombres.setError("Ingresa tu nombre completo"); etNombres.requestFocus(); return false; }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Email no válido"); etEmail.requestFocus(); return false; }
        if (nacionalidadPos == 0) { Toast.makeText(this, "Selecciona tu nacionalidad", Toast.LENGTH_SHORT).show(); return false; }
        if (telefono.isEmpty()) { etTelefono.setError("Ingresa tu número de contacto"); etTelefono.requestFocus(); return false; }
        if (metodoSeleccionado.isEmpty()) { Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private void procesarPago() {
        Toast.makeText(this, "Procesando pago...", Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(ComprarActivity.this, ConfirmarCompraActivity.class);
            intent.putExtra("total_reservas", totalActual);
            startActivity(intent);
            finish();
        }, 1500);
    }
}
