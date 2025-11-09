package com.proyecto.travelia;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class DetalleArticuloActivity extends AppCompatActivity {

    private TextView tvTituloDetalle, tvValoracion, tvDuracion, tvIncluye;
    private TextView tvServicios, tvIdiomas, tvUbicacionDetalle, tvDescripcion;
    private TextView tvPrecioDetalle, tvFecha;
    private ImageView ivDetalleImagen;
    private Spinner spAdultos, spIdioma;
    private Button btnRegresar, btnVerDisponibilidad, btnReservar;
    private Button btnEscribirOpinion, btnVerMasResenas;
    private LinearLayout btnSeleccionarFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_articulo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        initViews();
        setupBottomNav();
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
        btnSeleccionarFecha = findViewById(R.id.btn_seleccionar_fecha);
    }

    private void loadArticleData() {
        // Obtener datos del Intent
        Intent intent = getIntent();
        String titulo = intent.getStringExtra("titulo");
        String ubicacion = intent.getStringExtra("ubicacion");
        String precio = intent.getStringExtra("precio");
        String rating = intent.getStringExtra("rating");
        int imageRes = intent.getIntExtra("imageRes", R.drawable.mapi);

        // Setear datos básicos
        if (titulo != null) tvTituloDetalle.setText(titulo);
        if (ubicacion != null) tvUbicacionDetalle.setText(ubicacion);
        if (precio != null) tvPrecioDetalle.setText(precio + ".00");
        if (rating != null) tvValoracion.setText("★ " + rating);
        ivDetalleImagen.setImageResource(imageRes);

        // Datos adicionales de ejemplo
        tvDuracion.setText("1 Día Completo");
        tvIncluye.setText("Transporte, guía, entradas");
        tvServicios.setText("Almuerzo incluido");
        tvIdiomas.setText("Español, Inglés");
        tvDescripcion.setText("Descubre una de las maravillas del mundo en un tour único que combina historia, cultura y aventura. Perfecto para quienes buscan una experiencia inolvidable en los Andes.");
    }

    private void setupSpinners() {
        // Spinner de adultos
        String[] adultos = {"Adulto x 1", "Adultos x 2", "Adultos x 3", "Adultos x 4"};
        ArrayAdapter<String> adultosAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, adultos);
        adultosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAdultos.setAdapter(adultosAdapter);

        // Spinner de idioma
        String[] idiomas = {"Español", "Inglés", "Francés", "Alemán"};
        ArrayAdapter<String> idiomasAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, idiomas);
        idiomasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIdioma.setAdapter(idiomasAdapter);
    }

    private void setupListeners() {
        btnRegresar.setOnClickListener(v -> finish());

        btnVerDisponibilidad.setOnClickListener(v -> {
            // Scroll a la sección de reserva
            findViewById(R.id.sp_adultos).requestFocus();
            Toast.makeText(this, "Selecciona fecha y participantes", Toast.LENGTH_SHORT).show();
        });

        btnSeleccionarFecha.setOnClickListener(v -> showDatePicker());

        btnReservar.setOnClickListener(v -> {
            String fecha = tvFecha.getText().toString();
            if (fecha.equals("Seleccionar fecha")) {
                Toast.makeText(this, "Por favor selecciona una fecha", Toast.LENGTH_SHORT).show();
            } else {
                // Agregar al carrito y navegar a confirmar reserva
                Intent intent = new Intent(DetalleArticuloActivity.this, ConfirmarReservaActivity.class);
                intent.putExtra("titulo", getIntent().getStringExtra("titulo"));
                intent.putExtra("fecha", fecha);
                intent.putExtra("adultos", spAdultos.getSelectedItem().toString());
                intent.putExtra("precio", tvPrecioDetalle.getText().toString());
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

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navExplorar = findViewById(R.id.nav_explorar);
        LinearLayout navAdd = findViewById(R.id.nav_add);
        LinearLayout navFavorites = findViewById(R.id.nav_favorites);
        LinearLayout navReserve = findViewById(R.id.nav_reserve);

        if (navHome != null) {
            navHome.setClickable(true);
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(DetalleArticuloActivity.this, InicioActivity.class));
                finish();
            });
        }
        if (navExplorar != null) {
            navExplorar.setClickable(true);
            navExplorar.setOnClickListener(v -> {
                startActivity(new Intent(DetalleArticuloActivity.this, ExplorarActivity.class));
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