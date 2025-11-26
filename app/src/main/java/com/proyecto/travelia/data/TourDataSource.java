package com.proyecto.travelia.data;

import com.proyecto.travelia.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TourDataSource {

    private static final List<Tour> TOURS;

    static {
        List<Tour> list = new ArrayList<>();
        list.add(new Tour("T-001", "Machu Picchu Full Day", "Cusco, Perú", 280, 4.8, "4.8 • 230 reseñas", R.drawable.mapi, "Tours", "Rutas"));
        list.add(new Tour("T-002", "Lago Titicaca", "Puno, Perú", 380, 4.7, "4.7 • 156 reseñas", R.drawable.lagotiticaca, "Relax", "Tours"));
        list.add(new Tour("T-003", "Montaña de 7 Colores", "Cusco, Perú", 350, 4.6, "4.6 • 190 reseñas", R.drawable.montanacolores, "Rutas", "Tours"));
        list.add(new Tour("T-004", "Cabañas del Valle Sagrado", "Urubamba, Perú", 520, 4.9, "4.9 • 80 reseñas", R.drawable.mapi, "Cabañas", "Relax"));
        TOURS = Collections.unmodifiableList(list);
    }

    public static List<Tour> getTours() {
        return new ArrayList<>(TOURS);
    }

    public static Tour findById(String id) {
        if (id == null) return null;
        for (Tour t : TOURS) {
            if (id.equals(t.id)) return t;
        }
        return null;
    }

    public static List<Tour> filter(String query, String category, String order) {
        List<Tour> filtered = TOURS.stream().filter(tour -> {
            boolean matchesCategory = category == null || category.isEmpty() || category.equalsIgnoreCase("Todos") ||
                    category.equalsIgnoreCase(tour.category) || category.equalsIgnoreCase(tour.type);
            boolean matchesQuery = query == null || query.isEmpty() ||
                    tour.title.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault())) ||
                    tour.location.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()));
            return matchesCategory && matchesQuery;
        }).collect(Collectors.toList());

        if (order != null) {
            switch (order) {
                case "Precio":
                    filtered.sort(Comparator.comparingDouble(t -> t.price));
                    break;
                case "Rating":
                    filtered.sort((a, b) -> Double.compare(b.rating, a.rating));
                    break;
                case "Nombre":
                    filtered.sort(Comparator.comparing(t -> t.title));
                    break;
                default:
                    break;
            }
        }

        return filtered;
    }
}
