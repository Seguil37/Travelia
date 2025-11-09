package com.proyecto.travelia;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

import com.proyecto.travelia.ui.BottomNavView;

public class ComprarActivity extends AppCompatActivity {

    private EditText etNombres, etEmail, etTelefono;
    private Spinner spNacionalidad;
    private CardView cardDebito, cardPaypal, cardTransferencia, cardYape;
    private Button btnPagar, btnCancelar;
    private String metodoSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comprar);

        // Edge-to-edge: el BottomNav maneja el margen inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        initViews();
        setupSpinners();
        setupBottomNav();
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

    private void setupBottomNav() {
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            // Acción especial para ADD (opcional)
            bottom.setOnAddClickListener(v ->
                    Toast.makeText(this, "Acción agregar (Compra)", Toast.LENGTH_SHORT).show()
            );
            // Si no quieres cerrar esta Activity al cambiar de pestaña:
            // bottom.setFinishOnNavigate(false);
        }
    }

    private void setupListeners() {
        // Método de pago - Débito/Crédito
        cardDebito.setOnClickListener(v -> {
            metodoSeleccionado = "debito";
            resetMetodosSeleccion();
            cardDebito.setCardElevation(12f);
            mostrarDialogoTarjeta();
        });

        // PayPal
        cardPaypal.setOnClickListener(v -> {
            metodoSeleccionado = "paypal";
            resetMetodosSeleccion();
            cardPaypal.setCardElevation(12f);
            Toast.makeText(this, "PayPal seleccionado", Toast.LENGTH_SHORT).show();
        });

        // Transferencia
        cardTransferencia.setOnClickListener(v -> {
            metodoSeleccionado = "transferencia";
            resetMetodosSeleccion();
            cardTransferencia.setCardElevation(12f);
            Toast.makeText(this, "Transferencia seleccionada", Toast.LENGTH_SHORT).show();
        });

        // Yape/Plin
        cardYape.setOnClickListener(v -> {
            metodoSeleccionado = "yape";
            resetMetodosSeleccion();
            cardYape.setCardElevation(12f);
            Toast.makeText(this, "Yape/Plin seleccionado", Toast.LENGTH_SHORT).show();
        });

        // Botón Pagar
        btnPagar.setOnClickListener(v -> {
            if (validarDatos()) procesarPago();
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

        // Formatear fecha MM/AA
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
            startActivity(intent);
            finish();
        }, 1500);
    }
}
