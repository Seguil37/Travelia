package com.proyecto.travelia;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;
import com.proyecto.travelia.ui.BottomNavView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;

public class ExplorarActivity extends AppCompatActivity {

    private static final float PRICE_MIN_BOUND = 150f;
    private static final float PRICE_MAX_BOUND = 600f;

    private FavoritesRepository favRepo;
    private GridLayout grid;
    private ChipGroup chipActiveFilters;
    private EditText etSearch;
    private Spinner spOrden;
    private TextView tvCounter;
    private final List<CategoryControl> categoryControls = new ArrayList<>();

    private final List<CardData> allCards = new ArrayList<>();
    private final Set<String> activeTags = new HashSet<>();
    private float minPriceFilter = PRICE_MIN_BOUND;
    private float maxPriceFilter = PRICE_MAX_BOUND;
    private float minRatingFilter = 0f;
    private String currentOrder = "Nombre";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explorar);

        // Edge-to-edge: bottom lo maneja el BottomNav
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, 0);
            return insets;
        });

        favRepo = new FavoritesRepository(this);

        // BottomNav: acción especial para ADD (si quieres)
        BottomNavView bottom = findViewById(R.id.bottom_nav);
        if (bottom != null) {
            bottom.setOnAddClickListener(v ->
                    Toast.makeText(this, "Acción agregar (Explorar)", Toast.LENGTH_SHORT).show()
            );
            // bottom.setFinishOnNavigate(false); // si no quieres cerrar al navegar
        }

        grid = findViewById(R.id.grid_rutas);
        chipActiveFilters = findViewById(R.id.chip_active_filters);
        etSearch = findViewById(R.id.et_search);
        spOrden = findViewById(R.id.sp_orden);
        tvCounter = findViewById(R.id.tv_contador);

        setupCategoryButtons();

        String[] opciones = new String[]{"Nombre", "Precio", "Rating", "Nuevos"};
        spOrden.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opciones));
        spOrden.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentOrder = opciones[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        Button btnFiltros = findViewById(R.id.btn_filtros);
        btnFiltros.setOnClickListener(v -> showFilterDialog());

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                applyFilters();
            }
        });

        Button btnMas = findViewById(R.id.btn_mas_rutas);
        btnMas.setOnClickListener(v ->
                Toast.makeText(this, "Cargar más rutas…", Toast.LENGTH_SHORT).show());

        seedCards();
        applyFilters();
    }

    private void setupCategoryButtons() {
        int[] ids = new int[]{
                R.id.btn_cat_tours,
                R.id.btn_cat_relax,
                R.id.btn_cat_cabanas,
                R.id.btn_cat_rutas,
                R.id.btn_cat_hoteles,
                R.id.btn_cat_alimentacion
        };
        String[] tags = new String[]{"Tours", "Relax", "Cabañas", "Rutas", "Hoteles", "Alimentacion"};

        for (int i = 0; i < ids.length; i++) {
            Button btn = findViewById(ids[i]);
            if (btn == null) continue;
            CategoryControl control = new CategoryControl(btn, tags[i]);
            categoryControls.add(control);
            btn.setOnClickListener(v -> toggleTag(control.tag));
        }
        updateCategoryButtonStates();
    }

    private void seedCards() {
        allCards.clear();
        allCards.add(new CardData("T-001", "Machu Picchu Full Day", "Cusco, Perú", 280,
                4.8, 230, new String[]{"Tours", "Aventura", "Historia"}, R.drawable.mapi));
        allCards.add(new CardData("T-002", "Lago Titicaca", "Puno, Perú", 380,
                4.7, 156, new String[]{"Náutico", "Relax", "Cultural"}, R.drawable.lagotiticaca));
        allCards.add(new CardData("T-003", "Montaña de 7 Colores", "Cusco, Perú", 350,
                4.6, 190, new String[]{"Aventura", "Trekking", "Paisajes", "Rutas"}, R.drawable.montanacolores));
        allCards.add(new CardData("T-004", "Islas Ballestas & Paracas", "Ica, Perú", 260,
                4.5, 120, new String[]{"Náutico", "Naturaleza", "Tours", "Relax"}, R.drawable.lagotiticaca));
        allCards.add(new CardData("T-005", "Huacachina Buggy Experience", "Ica, Perú", 310,
                4.4, 210, new String[]{"Aventura", "Dunas", "Atardecer", "Cabañas"}, R.drawable.montanacolores));
        allCards.add(new CardData("T-006", "Retiro en Cabañas del Bosque", "Oxapampa, Perú", 320,
                4.3, 85, new String[]{"Relax", "Cabañas", "Naturaleza"}, R.drawable.mapi));
        allCards.add(new CardData("T-007", "Ruta Gastronómica Limeña", "Lima, Perú", 240,
                4.5, 140, new String[]{"Alimentacion", "Cultural", "Tours"}, R.drawable.lagotiticaca));
        allCards.add(new CardData("T-008", "Hotel Boutique Andino", "Arequipa, Perú", 520,
                4.9, 310, new String[]{"Hoteles", "Relax", "Cultural"}, R.drawable.mapi));
    }

    private void applyFilters() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        List<CardData> filtered = new ArrayList<>();

        for (CardData c : allCards) {
            if (!query.isEmpty() && !c.matchesQuery(query)) continue;
            if (!activeTags.isEmpty() && !c.hasAnyTag(activeTags)) continue;
            if (c.precio < minPriceFilter || c.precio > maxPriceFilter) continue;
            if (c.rating < minRatingFilter) continue;
            filtered.add(c);
        }

        sortCards(filtered);
        renderCards(filtered);
        renderActiveFilterChips();
        updateCategoryButtonStates();
        updateCounter(filtered.size());
    }

    private void toggleTag(String tag) {
        if (activeTags.contains(tag)) {
            activeTags.remove(tag);
        } else {
            activeTags.add(tag);
        }
        applyFilters();
    }

    private void updateCategoryButtonStates() {
        for (CategoryControl control : categoryControls) {
            boolean selected = activeTags.contains(control.tag);
            control.button.setSelected(selected);
            control.button.setAlpha(selected ? 1f : 0.85f);
            int background = selected ? R.drawable.rounded_button_gold : R.drawable.rounded_button_social;
            control.button.setBackgroundResource(background);
            control.button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void sortCards(List<CardData> cards) {
        switch (currentOrder) {
            case "Precio":
                Collections.sort(cards, Comparator.comparingDouble(c -> c.precio));
                break;
            case "Rating":
                Collections.sort(cards, (a, b) -> Double.compare(b.rating, a.rating));
                break;
            case "Nuevos":
                Collections.sort(cards, (a, b) -> b.id.compareTo(a.id));
                break;
            default:
                Collections.sort(cards, Comparator.comparing(c -> c.titulo));
                break;
        }
    }

    private void renderCards(List<CardData> cards) {
        LayoutInflater inflater = LayoutInflater.from(this);
        grid.removeAllViews();

        for (CardData d : cards) {
            View card = inflater.inflate(R.layout.card_destino, grid, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.setMargins(4, 4, 4, 4);
            card.setLayoutParams(params);

            ImageView img = card.findViewById(R.id.iv_destino_big);
            TextView tvTitulo = card.findViewById(R.id.tv_titulo);
            TextView tvUbic = card.findViewById(R.id.tv_ubicacion);
            TextView tvPrecio = card.findViewById(R.id.tv_precio_desde);
            TextView tvEst = card.findViewById(R.id.tv_rating_estrellas);
            TextView tvRat = card.findViewById(R.id.tv_rating_texto);
            Button btnDetalles = card.findViewById(R.id.btn_ver_detalles);
            ImageView ivFav = card.findViewById(R.id.iv_favorito);
            ChipGroup chipTags = card.findViewById(R.id.chip_tags);

            if (img != null) img.setImageResource(d.imageRes);
            if (tvTitulo != null) tvTitulo.setText(d.titulo);
            if (tvUbic != null) tvUbic.setText(d.ubicacion);
            if (tvPrecio != null) tvPrecio.setText(d.getPrecioLabel());
            if (tvEst != null) tvEst.setText(d.getEstrellas());
            if (tvRat != null) tvRat.setText(d.getRatingLabel());
            if (chipTags != null) populateTagChips(chipTags, d);

            Executors.newSingleThreadExecutor().execute(() -> {
                boolean isFav = favRepo.isFavoriteSync(d.id);
                runOnUiThread(() -> ivFav.setSelected(isFav));
            });

            ivFav.setOnClickListener(v -> {
                boolean nowSelected = !ivFav.isSelected();
                ivFav.setSelected(nowSelected);

                if (nowSelected) {
                    FavoriteEntity e = new FavoriteEntity(
                            "guest", d.id, "TOUR", d.titulo, d.ubicacion,
                            "img_local", d.precio, d.rating,
                            System.currentTimeMillis()
                    );
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.add(e));

                    Snackbar.make(card, "¡Añadido a favoritos!", Snackbar.LENGTH_LONG)
                            .setAction("Deshacer", a -> {
                                ivFav.setSelected(false);
                                Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                            }).show();
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> favRepo.remove(d.id));
                    Snackbar.make(card, "Eliminado de favoritos", Snackbar.LENGTH_SHORT).show();
                }
            });

            if (btnDetalles != null) {
                btnDetalles.setOnClickListener(v -> {
                    Intent intent = new Intent(ExplorarActivity.this, DetalleArticuloActivity.class);
                    intent.putExtra("id", d.id);
                    intent.putExtra("titulo", d.titulo);
                    intent.putExtra("ubicacion", d.ubicacion);
                    intent.putExtra("precio", d.getPrecioLabel());
                    intent.putExtra("rating", d.getRatingLabel());
                    intent.putExtra("imageRes", d.imageRes);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            grid.addView(card);
        }
    }

    private void populateTagChips(ChipGroup chipGroup, CardData cardData) {
        chipGroup.removeAllViews();
        int[] palette = new int[]{
                R.color.chip_soft_blue,
                R.color.chip_soft_green,
                R.color.chip_soft_pink,
                R.color.chip_soft_purple,
                R.color.chip_soft_orange
        };

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < cardData.tags.length; i++) {
            Chip chip = (Chip) inflater.inflate(com.google.android.material.R.layout.mtrl_chip_action, chipGroup, false);
            final String tag = cardData.tags[i];
            chip.setText(tag);
            int colorRes = palette[i % palette.length];
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, colorRes)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setAlpha(activeTags.contains(tag) ? 1f : 0.9f);
            chip.setOnClickListener(v -> toggleTag(tag));
            chipGroup.addView(chip);
        }
    }

    private void updateCounter(int size) {
        if (tvCounter != null) {
            String text = size == 1 ? "1 experiencia encontrada" : size + " experiencias encontradas";
            tvCounter.setText(text);
        }
    }

    private void renderActiveFilterChips() {
        if (chipActiveFilters == null) return;
        chipActiveFilters.removeAllViews();

        boolean hasFilters = false;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String tag : activeTags) {
            Chip chip = (Chip) inflater.inflate(com.google.android.material.R.layout.mtrl_chip_close, chipActiveFilters, false);
            chip.setText(tag);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                activeTags.remove(tag);
                applyFilters();
            });
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.chip_soft_purple)));
            chipActiveFilters.addView(chip);
            hasFilters = true;
        }

        if (minRatingFilter > 0f) {
            Chip ratingChip = (Chip) inflater.inflate(com.google.android.material.R.layout.mtrl_chip_close, chipActiveFilters, false);
            ratingChip.setText(String.format(Locale.getDefault(), "Rating ≥ %.1f", minRatingFilter));
            ratingChip.setOnCloseIconClickListener(v -> {
                minRatingFilter = 0f;
                applyFilters();
            });
            ratingChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.chip_soft_green)));
            chipActiveFilters.addView(ratingChip);
            hasFilters = true;
        }

        if (minPriceFilter > PRICE_MIN_BOUND || maxPriceFilter < PRICE_MAX_BOUND) {
            Chip priceChip = (Chip) inflater.inflate(com.google.android.material.R.layout.mtrl_chip_close, chipActiveFilters, false);
            priceChip.setText(String.format(Locale.getDefault(), "S/%.0f - S/%.0f", minPriceFilter, maxPriceFilter));
            priceChip.setOnCloseIconClickListener(v -> {
                minPriceFilter = PRICE_MIN_BOUND;
                maxPriceFilter = PRICE_MAX_BOUND;
                applyFilters();
            });
            priceChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.chip_soft_orange)));
            chipActiveFilters.addView(priceChip);
            hasFilters = true;
        }

        if (!hasFilters) {
            Chip hintChip = (Chip) inflater.inflate(com.google.android.material.R.layout.mtrl_chip_assist, chipActiveFilters, false);
            hintChip.setText("Sin filtros activos");
            hintChip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.chip_soft_blue)));
            hintChip.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            hintChip.setCloseIconVisible(false);
            hintChip.setCheckable(false);
            chipActiveFilters.addView(hintChip);
        }
    }

    private void showFilterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filtros_explorar, null, false);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_tags);
        RangeSlider priceSlider = view.findViewById(R.id.slider_price);
        Slider ratingSlider = view.findViewById(R.id.slider_rating);
        TextView tvPriceRange = view.findViewById(R.id.tv_price_range);
        TextView tvRatingHint = view.findViewById(R.id.tv_rating_hint);

        Set<String> tempTags = new HashSet<>(activeTags);
        String[] tags = new String[]{"Tours", "Aventura", "Cultural", "Naturaleza", "Relax", "Novedad"};
        for (String tag : tags) {
            Chip chip = (Chip) LayoutInflater.from(this)
                    .inflate(com.google.android.material.R.layout.mtrl_chip_choice, chipGroup, false);
            chip.setText(tag);
            chip.setChecked(tempTags.contains(tag));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tempTags.add(tag);
                } else {
                    tempTags.remove(tag);
                }
            });
            chipGroup.addView(chip);
        }

        priceSlider.setValues(minPriceFilter, maxPriceFilter);
        tvPriceRange.setText(String.format(Locale.getDefault(), "S/%.0f - S/%.0f", minPriceFilter, maxPriceFilter));
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            tvPriceRange.setText(String.format(Locale.getDefault(), "S/%.0f - S/%.0f", values.get(0), values.get(1)));
        });

        ratingSlider.setValue(minRatingFilter);
        tvRatingHint.setText(minRatingFilter > 0 ? String.format(Locale.getDefault(), "Mínimo %.1f", minRatingFilter) : "Sin filtro");
        ratingSlider.addOnChangeListener((slider, value, fromUser) ->
                tvRatingHint.setText(value > 0 ? String.format(Locale.getDefault(), "Mínimo %.1f", value) : "Sin filtro"));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Filtrar resultados")
                .setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        view.findViewById(R.id.btn_limpiar_filtros).setOnClickListener(v -> {
            tempTags.clear();
            activeTags.clear();
            priceSlider.setValues(priceSlider.getValueFrom(), priceSlider.getValueTo());
            ratingSlider.setValue(0f);
            minPriceFilter = priceSlider.getValueFrom();
            maxPriceFilter = priceSlider.getValueTo();
            minRatingFilter = 0f;
            applyFilters();
            dialog.dismiss();
        });

        view.findViewById(R.id.btn_aplicar_filtros).setOnClickListener(v -> {
            activeTags.clear();
            activeTags.addAll(tempTags);
            List<Float> values = priceSlider.getValues();
            minPriceFilter = values.get(0);
            maxPriceFilter = values.get(1);
            minRatingFilter = ratingSlider.getValue();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    static class CategoryControl {
        final Button button;
        final String tag;

        CategoryControl(Button button, String tag) {
            this.button = button;
            this.tag = tag;
        }
    }

    static class CardData {
        final String id, titulo, ubicacion;
        final double precio;
        final double rating;
        final int reviews;
        final int imageRes;
        final String[] tags;

        CardData(String id, String t, String u, double precio, double rating, int reviews, String[] tags, int img) {
            this.id = id;
            this.titulo = t;
            this.ubicacion = u;
            this.precio = precio;
            this.rating = rating;
            this.reviews = reviews;
            this.tags = tags;
            this.imageRes = img;
        }

        String getPrecioLabel() {
            return String.format(Locale.getDefault(), "S/%.0f", precio);
        }

        String getRatingLabel() {
            return String.format(Locale.getDefault(), "%.1f • %d reseñas", rating, reviews);
        }

        String getEstrellas() {
            int filled = (int) Math.round(rating);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sb.append(i < filled ? "★" : "☆");
            }
            return sb.toString();
        }

        boolean matchesQuery(String query) {
            String lowerTitle = titulo.toLowerCase(Locale.ROOT);
            String lowerUbic = ubicacion.toLowerCase(Locale.ROOT);
            return lowerTitle.contains(query) || lowerUbic.contains(query);
        }

        boolean hasAnyTag(Set<String> selected) {
            for (String tag : tags) {
                if (selected.contains(tag)) return true;
            }
            return false;
        }
    }
}
