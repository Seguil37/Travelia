package com.proyecto.travelia.data;

import com.proyecto.travelia.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Centraliza los datos base de cada destino/tour para mantener consistencia
 *  entre tarjetas, favoritos y recomendaciones. */
public final class TourData {

    private TourData() { }

    private static final List<TourInfo> TOURS = Collections.unmodifiableList(Arrays.asList(
            new TourInfo("T-001", "Machu Picchu Full Day", "Cusco, Perú", 280, 4.8, 230, R.drawable.mapi),
            new TourInfo("T-002", "Lago Titicaca", "Puno, Perú", 380, 4.7, 156, R.drawable.lagotiticaca),
            new TourInfo("T-003", "Montaña de 7 Colores", "Cusco, Perú", 350, 4.6, 190, R.drawable.montanacolores),
            new TourInfo("H-101", "Cabañas en el Valle Sagrado", "Urubamba, Perú", 420, 4.9, 0, R.drawable.casa_valle_sagrado),
            new TourInfo("A-550", "Ruta Gastronómica Limeña", "Lima, Perú", 180, 4.4, 0, R.drawable.ruta_gastronomica_lime_a)
    ));

    public static List<TourInfo> getTours() {
        return new ArrayList<>(TOURS);
    }

    public static TourInfo findById(String id) {
        if (id == null) return null;
        for (TourInfo info : TOURS) {
            if (id.equals(info.id)) return info;
        }
        return null;
    }

    public static class TourInfo {
        public final String id;
        public final String title;
        public final String location;
        public final double price;
        public final double defaultRating;
        public final int defaultReviewCount;
        public final int imageRes;

        private TourInfo(String id, String title, String location, double price,
                         double defaultRating, int defaultReviewCount, int imageRes) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.price = price;
            this.defaultRating = defaultRating;
            this.defaultReviewCount = defaultReviewCount;
            this.imageRes = imageRes;
        }

        public String formatPrice() {
            return String.format(Locale.getDefault(), "S/%.0f", price);
        }
    }
}
