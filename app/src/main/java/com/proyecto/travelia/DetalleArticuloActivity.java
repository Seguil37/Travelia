package com.proyecto.travelia;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.util.Calendar;
import java.util.Locale;

public class DetalleArticuloActivity extends AppCompatActivity {

    private TextView tvTituloDetalle, tvValoracion, tvDuracion, tvIncluye;
    private TextView tvServicios, tvIdiomas, tvUbicacionDetalle, tvDescripcion;
    private TextView tvPrecioDetalle, tvFecha;
    private ImageView ivDetalleImagen;
    private Spinner spAdultos, spIdioma;
    private Button btnRegresar, btnVerDisponibilidad, btnReservar;
    private Button btnEscribirOpinion, btnVerMasResenas;

    private ReservationsRepository reservationsRepository;
    private String tourId;
    private int imageRes;
    private double priceValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_articulo);

        // 🔧 Igual que Favoritos: bottom = 0
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        initViews();
        setupBottomNavNew();
        loadArticleData();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        tvTituloDetalle = findViewById(R.id.tv_titulo_detalle);
        tvValoracion = findViewById(R.id.tv_valoracion);
        tvDuracion = findViewById(R.id.tv_duracion);
        tvIncluye = findViewById(R.id.tv_incluye);
        tvServicios = findViewById(R.id.tv_servicios);
        tvIdiomas = findViewById(R.id.tv_idiomas);
        tvUbicacionDetalle = findViewById(R.id.tv_ubicacion_detalle);
        tvDescripcion = findViewById(R.id.tv_descripcion);
        tvPrecioDetalle = findViewById(R.id.tv_precio_detalle);
        tvFecha = findViewById(R.id.tv_fecha);
        ivDetalleImagen = findViewById(R.id.iv_detalle_imagen);

        spAdultos = findViewById(R.id.sp_adultos);
        spIdioma = findViewById(R.id.sp_idioma);

        btnRegresar = findViewById(R.id.btn_regresar);
        btnVerDisponibilidad = findViewById(R.id.btn_ver_disponibilidad);
        btnReservar = findViewById(R.id.btn_reservar);
        btnEscribirOpinion = findViewById(R.id.btn_escribir_opinion);
        btnVerMasResenas = findViewById(R.id.btn_ver_mas_resenas);
    }

    private void loadArticleData() {
        reservationsRepository = new ReservationsRepository(this);

        Intent intent = getIntent();
        tourId = intent.getStringExtra("id");
        String titulo = intent.getStringExtra("titulo");
        String ubicacion = intent.getStringExtra("ubicacion");
        String precio = intent.getStringExtra("precio");
        String rating = intent.getStringExtra("rating");
        imageRes = intent.getIntExtra("imageRes", R.drawable.mapi);

        if (titulo != null) tvTituloDetalle.setText(titulo);
        if (ubicacion != null) tvUbicacionDetalle.setText(ubicacion);
        priceValue = parsePrice(precio);
        tvPrecioDetalle.setText(String.format(Locale.getDefault(), "S/%.2f", priceValue));
        if (rating != null) tvValoracion.setText("★ " + rating);
        ivDetalleImagen.setImageResource(imageRes);

        tvDuracion.setText("1 Día Completo");
        tvIncluye.setText("Transporte, guía, entradas");
        tvServicios.setText("Almuerzo incluido");
        tvIdiomas.setText("Español, Inglés");
        tvDescripcion.setText("Descubre una de las maravillas del mundo en un tour único que combina historia, cultura y aventura. Perfecto para quienes buscan una experiencia inolvidable en los Andes.");
    }

    private void setupSpinners() {
        String[] adultos = {"Adulto x 1", "Adultos x 2", "Adultos x 3", "Adultos x 4"};
        ArrayAdapter<String> adultosAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, adultos);
        adultosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAdultos.setAdapter(adultosAdapter);

        String[] idiomas = {"Español", "Inglés", "Francés", "Alemán"};
        ArrayAdapter<String> idiomasAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, idiomas);
        idiomasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIdioma.setAdapter(idiomasAdapter);
    }

    private void setupListeners() {
        btnRegresar.setOnClickListener(v -> finish());

        btnVerDisponibilidad.setOnClickListener(v -> {
            findViewById(R.id.sp_adultos).requestFocus();
            Toast.makeText(this, "Selecciona fecha y participantes", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_seleccionar_fecha).setOnClickListener(v -> showDatePicker());

        btnReservar.setOnClickListener(v -> {
            String fecha = tvFecha.getText().toString();
            if ("Seleccionar fecha".contentEquals(fecha)) {
                Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            } else {
                String titulo = tvTituloDetalle.getText().toString();
                String ubicacion = tvUbicacionDetalle.getText().toString();
                String participantes = spAdultos.getSelectedItem().toString();
                String reservationId = ReservationsRepository.buildId(tourId != null ? tourId : titulo, fecha);

                ReservationEntity entity = new ReservationEntity(
                        reservationId,
                        tourId,
                        titulo,
                        ubicacion,
                        fecha,
                        participantes,
                        priceValue,
                        imageRes,
                        System.currentTimeMillis()
                );

                reservationsRepository.upsert(entity);

                Intent intent = new Intent(DetalleArticuloActivity.this, ConfirmarReservaActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Agregado a tu carrito de reservas", Toast.LENGTH_SHORT).show();
            }
        });

        btnEscribirOpinion.setOnClickListener(v ->
                Toast.makeText(this, "Función de escribir opinión", Toast.LENGTH_SHORT).show());

        btnVerMasResenas.setOnClickListener(v ->
                Toast.makeText(this, "Cargar más reseñas...", Toast.LENGTH_SHORT).show());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String fecha = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tvFecha.setText(fecha);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupBottomNavNew() {
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        // Sin lógica extra de insets aquí
    }

    private double parsePrice(String price) {
        if (price == null) return 0d;
        String cleaned = price.replace("S/", "").replace("s/", "");
        cleaned = cleaned.replaceAll("[^0-9.,]", "").replace(",", "");
        if (cleaned.isEmpty()) return 0d;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }
}
