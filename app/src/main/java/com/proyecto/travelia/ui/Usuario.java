package com.proyecto.travelia.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.proyecto.travelia.LoginActivity;
import com.proyecto.travelia.data.ReservationsRepository;
import com.proyecto.travelia.R;
import androidx.appcompat.widget.Toolbar;

import com.proyecto.travelia.data.local.ReservationEntity;
import com.proyecto.travelia.data.Constantes;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Usuario extends AppCompatActivity {
    private ReservationsRepository reservationsRepository;

    // Vistas
    private ImageView ivProfilePic, ivHeaderBackground;
    private ImageButton btnChangeProfilePic, btnChangeHeaderPic;
    private TextView tvNameDisplay, tvEmail;
    private EditText etFullName, etPhone, etCity; // Solo datos personales
    private MaterialButton btnEdit, btnSave, btnLogout, btnChangePassword;
    private SwitchMaterial switchOfferAlerts;
    private RecyclerView rvReservations;

    private boolean isEditing = false;
    private Uri selectedProfileImageUri = null;
    private Uri selectedHeaderImageUri = null;
    private SharedPreferences prefs;

    // Selector FOTO PERFIL
    ActivityResultLauncher<PickVisualMediaRequest> pickProfilePic =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedProfileImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivProfilePic);
                }
            });

    // Selector FOTO PORTADA
    ActivityResultLauncher<PickVisualMediaRequest> pickHeaderPic =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedHeaderImageUri = uri;
                    Glide.with(this).load(uri).centerCrop().into(ivHeaderBackground);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario);

        prefs = getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
        reservationsRepository = new ReservationsRepository(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Imágenes
        ivHeaderBackground = findViewById(R.id.iv_header_background);
        btnChangeHeaderPic = findViewById(R.id.btn_change_header_pic);
        ivProfilePic = findViewById(R.id.iv_profile_pic);
        btnChangeProfilePic = findViewById(R.id.btn_change_profile_pic);

        // Datos
        tvNameDisplay = findViewById(R.id.tv_profile_name_display);
        tvEmail = findViewById(R.id.tv_profile_email);

        // Editables
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_profile_phone);
        etCity = findViewById(R.id.et_profile_city);

        // Botones
        btnEdit = findViewById(R.id.btn_edit_profile);
        btnSave = findViewById(R.id.btn_save_profile);
        btnLogout = findViewById(R.id.btn_logout);
        btnChangePassword = findViewById(R.id.btn_change_password);

        switchOfferAlerts = findViewById(R.id.switch_offer_alerts);
        rvReservations = findViewById(R.id.rv_reservations);
        rvReservations.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadUserData() {
        String name = prefs.getString(Constantes.KEY_NAME, "Usuario Travelia");
        String email = prefs.getString(Constantes.KEY_EMAIL, "usuario@travelia.com");
        String phone = prefs.getString(Constantes.KEY_PHONE, "");
        String city = prefs.getString(Constantes.KEY_CITY, "");

        // Mostrar datos
        tvNameDisplay.setText(name);
        tvEmail.setText(email);
        etFullName.setText(name);
        etPhone.setText(phone);
        etCity.setText(city);

        // Cargar Imágenes
        String profileUri = prefs.getString("profile_image_uri", null);
        if (profileUri != null) {
            Glide.with(this).load(Uri.parse(profileUri)).circleCrop().into(ivProfilePic);
        } else {
            Glide.with(this).load(R.drawable.ic_usuario).circleCrop().into(ivProfilePic);
        }

        String headerUri = prefs.getString("header_image_uri", null);
        if (headerUri != null) {
            Glide.with(this).load(Uri.parse(headerUri)).centerCrop().into(ivHeaderBackground);
        }

        switchOfferAlerts.setChecked(prefs.getBoolean(Constantes.KEY_OFFER_ALERTS, true));
        loadReservationsFromDatabase();
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> toggleEditMode(true));
        btnSave.setOnClickListener(v -> saveChanges());

        btnChangeProfilePic.setOnClickListener(v ->
                pickProfilePic.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        btnChangeHeaderPic.setOnClickListener(v ->
                pickHeaderPic.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(Usuario.this, CambiarContrasenaActivity.class);
            startActivity(intent);
        });

        switchOfferAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(Constantes.KEY_OFFER_ALERTS, isChecked).apply()
        );

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Cerrar Sesión")
                    .setMessage("¿Estás seguro?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        prefs.edit().clear().apply();
                        Intent intent = new Intent(Usuario.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;

        etFullName.setEnabled(enable);
        etPhone.setEnabled(enable);
        etCity.setEnabled(enable);

        btnChangeProfilePic.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnChangeHeaderPic.setVisibility(enable ? View.VISIBLE : View.GONE);

        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);

        if (enable) etFullName.requestFocus();
    }

    private void saveChanges() {
        String newName = etFullName.getText().toString().trim();

        if (newName.isEmpty()) { etFullName.setError("Nombre requerido"); return; }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constantes.KEY_NAME, newName);
        editor.putString(Constantes.KEY_PHONE, etPhone.getText().toString());
        editor.putString(Constantes.KEY_CITY, etCity.getText().toString());

        if (selectedProfileImageUri != null) {
            editor.putString("profile_image_uri", selectedProfileImageUri.toString());
        }
        if (selectedHeaderImageUri != null) {
            editor.putString("header_image_uri", selectedHeaderImageUri.toString());
        }

        editor.apply();
        tvNameDisplay.setText(newName);
        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
        toggleEditMode(false);
    }

    private void loadReservationsFromDatabase() {
        reservationsRepository.ioExecutor.execute(() -> {
            List<ReservationEntity> entityList;
            try {
                entityList = reservationsRepository.getAllReservationsSync();
            } catch (Exception e) { return; }

            List<Reservation> allReservations = new ArrayList<>();
            for (ReservationEntity entity : entityList) {
                allReservations.add(new Reservation(
                        entity.title, entity.location, entity.date,
                        String.format(Locale.getDefault(), "$%.2f", entity.price),
                        entity.participants, entity.paymentStatus
                ));
            }

            runOnUiThread(() -> {
                List<Reservation> completed = allReservations.stream()
                        .filter(r -> "COMPLETADO".equalsIgnoreCase(r.getPaymentStatus()))
                        .collect(Collectors.toList());

                ReservationsAdapter adapter = new ReservationsAdapter(completed);
                rvReservations.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public static class Reservation {
        public String name, location, date, price, participants, paymentStatus;
        public Reservation(String name, String location, String date, String price, String participants, String paymentStatus) {
            this.name = name; this.location = location; this.date = date; this.price = price; this.participants = participants; this.paymentStatus = paymentStatus;
        }
        public String getPaymentStatus() { return paymentStatus; }
    }
}