package com.proyecto.travelia.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.proyecto.travelia.R;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CrearPublicacionActivity extends AppCompatActivity {
    // Vistas
    private TextInputEditText etTitulo, etDescripcion;
    private ImageView ivPortada;
    private LinearLayout placeholderPortada;
    private TextView tvCoordenadas, tvFecha;
    private View btnUbicacion, btnFecha, btnSeleccionarPortada;
    private MaterialButton btnPublicar, btnAgregarMasFotos;
    private ChipGroup chipGroupCategorias;
    private RatingBar ratingBar;
    private RecyclerView rvGaleria;

    // Datos
    private Uri uriPortada = null;
    private List<Uri> listaFotosGaleria = new ArrayList<>();
    private Location ubicacionActual = null;
    private String fechaSeleccionada = "";
    private FusedLocationProviderClient fusedLocationClient;
    private FotosAdapter fotosAdapter;

    // Variables Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // 1. Selector para PORTADA (Solo 1 foto)
    ActivityResultLauncher<PickVisualMediaRequest> pickPortada =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uriPortada = uri;
                    ivPortada.setImageURI(uri);
                    placeholderPortada.setVisibility(View.GONE);
                }
            });

    // 2. Selector para GALERÍA (Múltiples fotos)
    ActivityResultLauncher<PickVisualMediaRequest> pickMultiple =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                if (!uris.isEmpty()) {
                    listaFotosGaleria.addAll(uris);
                    fotosAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Fotos añadidas: " + uris.size(), Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_publicacion);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews();
        setupRecycler();
        setupListeners();
    }
    private void initViews() {
        etTitulo = findViewById(R.id.et_titulo);
        etDescripcion = findViewById(R.id.et_descripcion);

        ivPortada = findViewById(R.id.iv_portada);
        placeholderPortada = findViewById(R.id.layout_placeholder_portada);
        btnSeleccionarPortada = findViewById(R.id.btn_seleccionar_portada);

        tvCoordenadas = findViewById(R.id.tv_coordenadas);
        tvFecha = findViewById(R.id.tv_fecha);

        btnUbicacion = findViewById(R.id.btn_obtener_ubicacion);
        btnFecha = findViewById(R.id.btn_fecha);
        btnPublicar = findViewById(R.id.btn_publicar);
        btnAgregarMasFotos = findViewById(R.id.btn_agregar_mas_fotos);

        chipGroupCategorias = findViewById(R.id.chip_group_categorias);
        ratingBar = findViewById(R.id.rating_bar);
        rvGaleria = findViewById(R.id.rv_galeria_fotos);
    }

    private void setupRecycler() {
        fotosAdapter = new FotosAdapter(listaFotosGaleria);
        rvGaleria.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvGaleria.setAdapter(fotosAdapter);
    }

    private void setupListeners() {
        // Portada
        btnSeleccionarPortada.setOnClickListener(v ->
                pickPortada.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        // Galería Extra
        btnAgregarMasFotos.setOnClickListener(v ->
                pickMultiple.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        // Fecha
        btnFecha.setOnClickListener(v -> showDatePicker());

        // Ubicación
        btnUbicacion.setOnClickListener(v -> obtenerUbicacionActual());

        // Publicar
        btnPublicar.setOnClickListener(v -> validarYPublicar());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            fechaSeleccionada = day + "/" + (month + 1) + "/" + year;
            tvFecha.setText(fechaSeleccionada);
            tvFecha.setTextColor(getColor(R.color.black));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        tvCoordenadas.setText("Buscando...");
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                ubicacionActual = location;
                tvCoordenadas.setText(String.format(Locale.getDefault(), "%.3f, %.3f", location.getLatitude(), location.getLongitude()));
                tvCoordenadas.setTextColor(getColor(R.color.black));
            } else {
                tvCoordenadas.setText("Sin señal GPS");
            }
        });
    }

    private void validarYPublicar() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (titulo.isEmpty()) { etTitulo.setError("Título requerido"); return; }

        // OJO: Comenté esta validación también por si quieres probar sin seleccionar foto
        // if (uriPortada == null) { Toast.makeText(this, "Falta foto de portada", Toast.LENGTH_SHORT).show(); return; }

        // Bloquear botón y mostrar estado
        btnPublicar.setEnabled(false);
        btnPublicar.setText("PUBLICANDO...");

        // =================================================================================
        // [INICIO] BLOQUE COMENTADO: SUBIDA DE FOTO A STORAGE (Descomentar cuando funcione)
        // =================================================================================
        /*
        String nombreFoto = "viajes/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(nombreFoto);

        ref.putFile(uriPortada)
                .addOnSuccessListener(taskSnapshot -> {
                    // 2. Obtener URL real de la nube
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String urlFoto = uri.toString();
                        guardarEnFirestore(titulo, descripcion, urlFoto);
                    });
                })
                .addOnFailureListener(e -> {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("PUBLICAR");
                    Toast.makeText(this, "Error foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        */
        // =================================================================================
        // [FIN] BLOQUE COMENTADO
        // =================================================================================


        // ============================================================
        // [TEMPORAL] GUARDADO DIRECTO SOLO DE TEXTO (SIN SUBIR FOTO)
        // ============================================================

        // Usamos una imagen de relleno de internet para que no se rompa la app al mostrarla
        String urlFotoTemporal = "https://placehold.co/600x400/orange/white?text=Imagen+Pendiente";

        // Llamamos directo a guardar los datos
        guardarEnFirestore(titulo, descripcion, urlFotoTemporal);
    }
    private void guardarEnFirestore(String titulo, String desc, String urlFoto) {
        // Preparar datos
        Map<String, Object> viaje = new HashMap<>();
        viaje.put("titulo", titulo);
        viaje.put("descripcion", desc);
        // viaje.put("precio", precio); // <-- ELIMINADO PORQUE YA NO USAMOS PRECIO
        viaje.put("fotoUrl", urlFoto);
        viaje.put("fechaViaje", fechaSeleccionada);
        viaje.put("rating", ratingBar.getRating());

        // Categoría
        String categoria = "General";
        int chipId = chipGroupCategorias.getCheckedChipId();
        if (chipId != -1) {
            Chip c = findViewById(chipId);
            categoria = c.getText().toString();
        }
        viaje.put("categoria", categoria);

        // Ubicación
        if (ubicacionActual != null) {
            viaje.put("latitud", ubicacionActual.getLatitude());
            viaje.put("longitud", ubicacionActual.getLongitude());
        } else {
            viaje.put("latitud", 0.0);
            viaje.put("longitud", 0.0);
        }

        // Guardar en Firestore
        db.collection("publicaciones")
                .add(viaje)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "¡Publicado con éxito!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnPublicar.setEnabled(true);
                    btnPublicar.setText("PUBLICAR");
                    Toast.makeText(this, "Error BD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) obtenerUbicacionActual();
    }

    // --- ADAPTER INTERNO PARA GALERÍA ---
    private class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {
        private List<Uri> fotos;
        public FotosAdapter(List<Uri> fotos) { this.fotos = fotos; }

        @NonNull @Override
        public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(300, 300)); // Tamaño de miniatura
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setPadding(8, 0, 8, 0);
            return new FotoViewHolder(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
            holder.iv.setImageURI(fotos.get(position));
        }

        @Override
        public int getItemCount() { return fotos.size(); }

        class FotoViewHolder extends RecyclerView.ViewHolder {
            ImageView iv;
            public FotoViewHolder(@NonNull View itemView) { super(itemView); iv = (ImageView) itemView; }
        }
    }
}