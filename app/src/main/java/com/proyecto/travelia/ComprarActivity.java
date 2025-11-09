package com.proyecto.travelia;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ComprarActivity extends AppCompatActivity {

    private EditText etNombres, etEmail, etTelefono;
    private Spinner spNacionalidad;
    private CardView cardDebito, cardPaypal, cardTransferencia, cardYape;
    private Button btnPagar, btnCancelar;
    private String metodoSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar);

        initViews();
        setupSpinners();
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
    }

    private void setupSpinners() {
        String[] paises = {"Selecciona tu país", "Perú", "Argentina", "Chile", "Colombia",
                "México", "España", "Estados Unidos", "Brasil"};
        ArrayAdapter<String> paisesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paises);
        paisesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNacionalidad.setAdapter(paisesAdapter);
    }

    private void setupListeners() {
        // Método de pago - Débito/Crédito
        cardDebito.setOnClickListener(v -> {
            metodoSeleccionado = "debito";
            resetMetodosSeleccion();
            cardDebito.setCardElevation(12f);
            mostrarDialogoTarjeta();
        });

        // Método de pago - PayPal
        cardPaypal.setOnClickListener(v -> {
            metodoSeleccionado = "paypal";
            resetMetodosSeleccion();
            cardPaypal.setCardElevation(12f);
            Toast.makeText(this, "PayPal seleccionado", Toast.LENGTH_SHORT).show();
        });

        // Método de pago - Transferencia
        cardTransferencia.setOnClickListener(v -> {
            metodoSeleccionado = "transferencia";
            resetMetodosSeleccion();
            cardTransferencia.setCardElevation(12f);
            Toast.makeText(this, "Transferencia seleccionada", Toast.LENGTH_SHORT).show();
        });

        // Método de pago - Yape/Plin
        cardYape.setOnClickListener(v -> {
            metodoSeleccionado = "yape";
            resetMetodosSeleccion();
            cardYape.setCardElevation(12f);
            Toast.makeText(this, "Yape/Plin seleccionado", Toast.LENGTH_SHORT).show();
        });

        // Botón Pagar
        btnPagar.setOnClickListener(v -> {
            if (validarDatos()) {
                procesarPago();
            }
        });

        // Botón Cancelar
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

        // Formatear número de tarjeta
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int previousLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String input = s.toString().replaceAll("\\s", "");

                if (input.length() <= 16) {
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < input.length(); i++) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ");
                        }
                        formatted.append(input.charAt(i));
                    }

                    s.replace(0, s.length(), formatted.toString());
                }

                isFormatting = false;
            }
        });

        // Formatear fecha de expiración
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String input = s.toString().replaceAll("/", "");

                if (input.length() >= 2) {
                    s.replace(0, s.length(), input.substring(0, 2) + "/" + input.substring(2));
                }

                isFormatting = false;
            }
        });

        btnCancelarTarjeta.setOnClickListener(v -> dialog.dismiss());

        btnConfirmarTarjeta.setOnClickListener(v -> {
            String cardNumber = etCardNumber.getText().toString();
            String expiryDate = etExpiryDate.getText().toString();
            String cvv = etCvv.getText().toString();
            String cardName = etCardName.getText().toString();

            if (cardNumber.replaceAll("\\s", "").length() < 13 ||
                    expiryDate.length() < 5 ||
                    cvv.length() < 3 ||
                    cardName.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos de la tarjeta",
                        Toast.LENGTH_SHORT).show();
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

        if (nombres.isEmpty()) {
            etNombres.setError("Ingresa tu nombre completo");
            etNombres.requestFocus();
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un email válido");
            etEmail.requestFocus();
            return false;
        }

        if (nacionalidadPos == 0) {
            Toast.makeText(this, "Selecciona tu nacionalidad", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (telefono.isEmpty()) {
            etTelefono.setError("Ingresa tu número de contacto");
            etTelefono.requestFocus();
            return false;
        }

        if (metodoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void procesarPago() {
        // Simular procesamiento de pago
        Toast.makeText(this, "Procesando pago...", Toast.LENGTH_SHORT).show();

        // Navegar a confirmación de compra
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(ComprarActivity.this, ConfirmarCompraActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}