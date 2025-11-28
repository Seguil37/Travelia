package com.proyecto.travelia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.ReviewRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class ExplorarActivity extends BaseActivity {

    private FavoritesRepository favRepo;
    private RutasAdapter adapter;
    private TextView tvContador;
    private Spinner spOrden;
    private String searchQuery = "";
    private double minPrice = 0;
    private double minRating = 0;
    private ReviewRepository reviewRepository;
    private final Set<String> selectedCategories = new HashSet<>();
    private final Set<String> selectedTags = new HashSet<>();
    private final List<CardData> allCards = new ArrayList<>();
    private final Map<String, RatingSnapshot> ratingSnapshots = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explorar);
        setupTopBar();

        // Edge-to-edge: bottom lo maneja el BottomNav
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        favRepo = new FavoritesRepository(this);
        reviewRepository = new ReviewRepository(this);

        // BottomNav se encarga solo de la navegación con la configuración por defecto

        spOrden = findViewById(R.id.sp_orden);
        String[] opciones = new String[]{"Nombre", "Precio", "Rating", "Nuevos"};
        spOrden.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opciones));
        spOrden.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        Button btnFiltros = findViewById(R.id.btn_filtros);
        btnFiltros.setOnClickListener(v -> showFiltersDialog());

        Button btnMas = findViewById(R.id.btn_mas_rutas);
        btnMas.setOnClickListener(v ->
                Toast.makeText(this, "Cargar más rutas…", Toast.LENGTH_SHORT).show());

        RecyclerView rvRutas = findViewById(R.id.rv_rutas);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rvRutas.setLayoutManager(glm);
        int spacingPx = (int) (12 * getResources().getDisplayMetrics().density);
        rvRutas.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = spacingPx;
                outRect.right = spacingPx;
                outRect.top = spacingPx;
                outRect.bottom = spacingPx;
            }
        });
        adapter = new RutasAdapter();
        rvRutas.setAdapter(adapter);
        tvContador = findViewById(R.id.tv_contador);

        setupSearchBar();
        setupCategoryButtons();
        seedCards();
        observeReviewStats();
        applyFilters();
    }

    private void setupSearchBar() {
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupCategoryButtons() {
        Button btnTours = findViewById(R.id.btn_cat_tours);
        Button btnRelax = findViewById(R.id.btn_cat_relax);
        Button btnCabanas = findViewById(R.id.btn_cat_cabanas);
        Button btnRutas = findViewById(R.id.btn_cat_rutas);
        Button btnHoteles = findViewById(R.id.btn_cat_hoteles);
        Button btnAlimentacion = findViewById(R.id.btn_cat_alimentacion);

        setupCategoryListener(btnTours, "Tours");
        setupCategoryListener(btnRelax, "Relax");
        setupCategoryListener(btnCabanas, "Cabañas");
        setupCategoryListener(btnRutas, "Rutas");
        setupCategoryListener(btnHoteles, "Hoteles");
        setupCategoryListener(btnAlimentacion, "Alimentación");
    }

    private void setupCategoryListener(Button button, String category) {
        if (button == null) return;
        button.setOnClickListener(v -> {
            if (selectedCategories.contains(category)) {
                selectedCategories.remove(category);
                button.setBackgroundResource(R.drawable.rounded_button_social);
            } else {
                selectedCategories.add(category);
                button.setBackgroundResource(R.drawable.rounded_button_gold);
            }
            applyFilters();
        });
    }

    private void seedCards() {
        allCards.clear();
        Collections.addAll(allCards,
                new CardData("T-001", "Machu Picchu Full Day", "Cusco, Perú", "S/280", "★★★★☆", "4.8 • 230 reseñas", 280,
                        4.8, Arrays.asList("Tours", "Rutas"), Arrays.asList("Aventura", "Cultural"),
                        R.drawable.mapi, System.currentTimeMillis() - 5000000L),
                new CardData("T-002", "Lago Titicaca", "Puno, Perú", "S/380", "★★★★☆", "4.7 • 156 reseñas", 380,
                        4.7, Arrays.asList("Tours", "Relax"), Arrays.asList("Náutico", "Cultural"),
                        R.drawable.lagotiticaca, System.currentTimeMillis() - 3000000L),
                new CardData("T-003", "Montaña de 7 Colores", "Cusco, Perú", "S/350", "★★★★☆", "4.6 • 190 reseñas", 350,
                        4.6, Arrays.asList("Tours", "Rutas"), Arrays.asList("Aventura", "Altura"),
                        R.drawable.montanacolores, System.currentTimeMillis() - 2000000L),
                new CardData("H-101", "Cabañas en el Valle Sagrado", "Urubamba, Perú", "S/420", "★★★★★", "4.9 • 84 reseñas", 420,
                        4.9, Arrays.asList("Cabañas", "Relax"), Arrays.asList("Familia", "Naturaleza"),
                        R.drawable.montanacolores, System.currentTimeMillis() - 1000000L),
                new CardData("A-550", "Ruta Gastronómica Limeña", "Lima, Perú", "S/180", "★★★★☆", "4.4 • 65 reseñas", 180,
                        4.4, Arrays.asList("Alimentación", "Tours"), Arrays.asList("Gastronomía", "Cultural"),
                        R.drawable.r, System.currentTimeMillis() - 7000000L)
        );
    }

    private void observeReviewStats() {
        for (CardData card : allCards) {
            String key = getReviewKey(card);
            reviewRepository.observeAverage(key).observe(this, avg -> {
                RatingSnapshot snapshot = ratingSnapshots.computeIfAbsent(key, k -> new RatingSnapshot());
                snapshot.average = avg != null ? avg : 0d;
                snapshot.hasAverage = avg != null;
                applyFilters();
            });

            reviewRepository.observeCount(key).observe(this, count -> {
                RatingSnapshot snapshot = ratingSnapshots.computeIfAbsent(key, k -> new RatingSnapshot());
                snapshot.count = count != null ? count : 0;
                applyFilters();
            });
        }
    }

    private void applyFilters() {
        List<CardData> filtered = new ArrayList<>();
        String queryLower = searchQuery.toLowerCase(Locale.ROOT);

        for (CardData card : allCards) {
            boolean matchesQuery = queryLower.isEmpty() ||
                    card.titulo.toLowerCase(Locale.ROOT).contains(queryLower) ||
                    card.ubicacion.toLowerCase(Locale.ROOT).contains(queryLower);
            if (!matchesQuery) continue;

            if (!selectedCategories.isEmpty() && Collections.disjoint(selectedCategories, card.categorias)) {
                continue;
            }

            if (!selectedTags.isEmpty()) {
                boolean hasAnyTag = false;
                for (String tag : selectedTags) {
                    if (card.tags.contains(tag)) {
                        hasAnyTag = true;
                        break;
                    }
                }
                if (!hasAnyTag) continue;
            }

            if (card.precioValor < minPrice) continue;

            double effectiveRating = getEffectiveRating(card);
            if (effectiveRating < minRating) continue;

            filtered.add(card);
        }

        sortFiltered(filtered);
        tvContador.setText(String.format(Locale.getDefault(), "%d artículos encontrados", filtered.size()));
        adapter.submit(filtered);
    }

    private void sortFiltered(List<CardData> filtered) {
        String criterio = spOrden.getSelectedItem() != null ? spOrden.getSelectedItem().toString() : "Nombre";
        Comparator<CardData> comparator;
        switch (criterio) {
            case "Precio":
                comparator = Comparator.comparingDouble(c -> c.precioValor);
                break;
            case "Rating":
                comparator = (c1, c2) -> Double.compare(getEffectiveRating(c2), getEffectiveRating(c1));
                break;
            case "Nuevos":
                comparator = (c1, c2) -> Long.compare(c2.createdAt, c1.createdAt);
                break;
            case "Nombre":
            default:
                comparator = Comparator.comparing(c -> c.titulo.toLowerCase(Locale.ROOT));
        }
        Collections.sort(filtered, comparator);
    }

    private String getReviewKey(CardData card) {
        return card.id != null && !card.id.isEmpty() ? card.id : card.titulo;
    }

    private double getEffectiveRating(CardData card) {
        RatingSnapshot snapshot = ratingSnapshots.get(getReviewKey(card));
        if (snapshot != null && snapshot.hasAverage && snapshot.count > 0) {
            return snapshot.average;
        }
        return card.ratingValor;
    }

    private String formatRatingText(CardData card) {
        RatingSnapshot snapshot = ratingSnapshots.get(getReviewKey(card));
        if (snapshot != null && snapshot.count > 0 && snapshot.hasAverage) {
            String label = snapshot.count == 1 ? "reseña" : "reseñas";
            return String.format(Locale.getDefault(), "%.1f • %d %s", snapshot.average, snapshot.count, label);
        }
        if (snapshot != null && snapshot.count == 0) {
            return "Sin reseñas";
        }
        return card.ratingTxt;
    }

    private String formatStars(CardData card) {
        double rating = getEffectiveRating(card);
        int filledStars = (int) Math.round(rating);
        if (filledStars < 0) filledStars = 0;
        if (filledStars > 5) filledStars = 5;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < filledStars ? '★' : '☆');
        }
        return builder.toString();
    }

    private void renderTags(LinearLayout container, List<String> tags) {
        if (container == null) return;
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String tag : tags) {
            TextView chip = (TextView) inflater.inflate(R.layout.item_tag, container, false);
            chip.setText(tag);
            container.addView(chip);
        }
    }

    private void showFiltersDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_filters_explorar, null);
        EditText etPrecioMin = view.findViewById(R.id.et_precio_min);
        EditText etRatingMin = view.findViewById(R.id.et_rating_min);

        etPrecioMin.setText(minPrice == 0 ? "" : String.valueOf((int) minPrice));
        etRatingMin.setText(minRating == 0 ? "" : String.valueOf(minRating));

        CheckBox cbAventura = view.findViewById(R.id.cb_aventura);
        CheckBox cbCultural = view.findViewById(R.id.cb_cultural);
        CheckBox cbFamilia = view.findViewById(R.id.cb_familia);
        CheckBox cbRelax = view.findViewById(R.id.cb_relax);
        CheckBox cbGastronomia = view.findViewById(R.id.cb_gastronomia);
        CheckBox cbNaturaleza = view.findViewById(R.id.cb_naturaleza);

        cbAventura.setChecked(selectedTags.contains("Aventura"));
        cbCultural.setChecked(selectedTags.contains("Cultural"));
        cbFamilia.setChecked(selectedTags.contains("Familia"));
        cbRelax.setChecked(selectedTags.contains("Relax"));
        cbGastronomia.setChecked(selectedTags.contains("Gastronomía"));
        cbNaturaleza.setChecked(selectedTags.contains("Naturaleza"));

        new AlertDialog.Builder(this)
                .setTitle("Filtros avanzados")
                .setView(view)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    selectedTags.clear();
                    if (cbAventura.isChecked()) selectedTags.add("Aventura");
                    if (cbCultural.isChecked()) selectedTags.add("Cultural");
                    if (cbFamilia.isChecked()) selectedTags.add("Familia");
                    if (cbRelax.isChecked()) selectedTags.add("Relax");
                    if (cbGastronomia.isChecked()) selectedTags.add("Gastronomía");
                    if (cbNaturaleza.isChecked()) selectedTags.add("Naturaleza");

                    minPrice = parseDoubleOrZero(etPrecioMin.getText().toString());
                    minRating = parseDoubleOrZero(etRatingMin.getText().toString());
                    applyFilters();
                })
                .setNeutralButton("Limpiar", (dialog, which) -> {
                    selectedTags.clear();
                    minPrice = 0;
                    minRating = 0;
                    applyFilters();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static class CardData {
        final String id, titulo, ubicacion, precio, estrellas, ratingTxt;
        final double precioValor, ratingValor;
        final List<String> categorias;
        final List<String> tags;
        final int imageRes;
        final long createdAt;
        CardData(String id, String t, String u, String p, String e, String r, double precioValor, double ratingValor,
                 List<String> categorias, List<String> tags, int img, long createdAt) {
            this.id = id; this.titulo = t; this.ubicacion = u;
            this.precio = p; this.estrellas = e; this.ratingTxt = r;
            this.precioValor = precioValor; this.ratingValor = ratingValor;
            this.categorias = categorias; this.tags = tags;
            this.imageRes = img; this.createdAt = createdAt;
        }
    }

    static class RatingSnapshot {
        double average;
        int count;
        boolean hasAverage;
    }

    private class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.VH> {
        private final List<CardData> data = new ArrayList<>();

        void submit(List<CardData> items) {
            data.clear();
            if (items != null) data.addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.card_destino, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            CardData d = data.get(position);

            holder.img.setImageResource(d.imageRes);
            holder.tvTitulo.setText(d.titulo);
            holder.tvUbic.setText(d.ubicacion);
            holder.tvPrecio.setText(d.precio);
            holder.tvEstrellas.setText(formatStars(d));
            holder.tvRating.setText(formatRatingText(d));
            renderTags(holder.tagContainer, d.tags);

            Executors.newSingleThreadExecutor().execute(() -> {
                boolean isFav = favRepo.isFavoriteSync(d.id);
                runOnUiThread(() -> holder.ivFav.setSelected(isFav));
            });

            holder.ivFav.setOnClickListener(v -> {
                boolean nowSelected = !holder.ivFav.isSelected();
                holder.ivFav.setSelected(nowSelected);

                if (nowSelected) {
                    FavoriteEntity e = new FavoriteEntity(
                            "guest", d.id, "TOUR", d.titulo, d.ubicacion,
                            "img_local", d.precioValor, d.ratingValor,
                            System.currentTimeMillis()
                    );
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.add(e));

                    Snackbar.make(holder.itemView, "¡Añadido a favoritos!", Snackbar.LENGTH_LONG)
                            .setAction("Deshacer", a -> {
                                holder.ivFav.setSelected(false);
                                Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                            }).show();
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                    Snackbar.make(holder.itemView, "Eliminado de favoritos", Snackbar.LENGTH_SHORT).show();
                }
            });

            holder.btnDetalles.setOnClickListener(v -> {
                Intent intent = new Intent(ExplorarActivity.this, DetalleArticuloActivity.class);
                intent.putExtra("id", d.id);
                intent.putExtra("titulo", d.titulo);
                intent.putExtra("ubicacion", d.ubicacion);
                intent.putExtra("precio", d.precio);
                intent.putExtra("rating", formatRatingText(d));
                intent.putExtra("imageRes", d.imageRes);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ImageView img, ivFav;
            final TextView tvTitulo, tvUbic, tvPrecio, tvEstrellas, tvRating;
            final Button btnDetalles;
            final LinearLayout tagContainer;

            VH(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.iv_destino_big);
                ivFav = itemView.findViewById(R.id.iv_favorito);
                tvTitulo = itemView.findViewById(R.id.tv_titulo);
                tvUbic = itemView.findViewById(R.id.tv_ubicacion);
                tvPrecio = itemView.findViewById(R.id.tv_precio_desde);
                tvEstrellas = itemView.findViewById(R.id.tv_rating_estrellas);
                tvRating = itemView.findViewById(R.id.tv_rating_texto);
                btnDetalles = itemView.findViewById(R.id.btn_ver_detalles);
                tagContainer = itemView.findViewById(R.id.tag_container);
            }
        }
    }
}
