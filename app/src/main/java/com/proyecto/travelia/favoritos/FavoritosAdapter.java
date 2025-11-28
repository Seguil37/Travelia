package com.proyecto.travelia.favoritos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.proyecto.travelia.DetalleArticuloActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.ReviewRepository;
import com.proyecto.travelia.data.TourData;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.VH> {

    private final FavoritesRepository repo;
    private final ReviewRepository reviewRepo;
    private final LifecycleOwner lifecycleOwner;
    private final Context ctx;
    private final List<FavoriteEntity> data = new ArrayList<>();
    private final Map<String, RatingSnapshot> ratingSnapshots = new HashMap<>();
    private final Set<String> observedKeys = new HashSet<>();

    public FavoritosAdapter(FavoritesRepository repo, ReviewRepository reviewRepo,
                            LifecycleOwner lifecycleOwner, Context ctx) {
        this.repo = repo;
        this.reviewRepo = reviewRepo;
        this.lifecycleOwner = lifecycleOwner;
        this.ctx = ctx;
    }

    /** Reemplaza toda la lista y refresca */
    public void submit(List<FavoriteEntity> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_destino, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FavoriteEntity f = data.get(pos);
        String reviewKey = f.itemId != null ? f.itemId : f.title;
        ensureReviewObservation(reviewKey);

        TourData.TourInfo info = TourData.findById(f.itemId);
        double priceValue = info != null ? info.price : (f.price != null ? f.price : 0d);
        double effectiveRating = getEffectiveRating(reviewKey, info, f);

        // Texto
        h.tvTitulo.setText(f.title != null ? f.title : (info != null ? info.title : "--"));
        h.tvUbic.setText(f.location != null ? f.location : (info != null ? info.location : "--"));
        h.tvPrecio.setText(formatPrice(priceValue));
        h.tvEstrellas.setText(buildStars(effectiveRating));
        h.tvRatingTxt.setText(buildRatingText(reviewKey, info, f));

        // Imagen por itemId (T-001, T-002, T-003, ...)
        if (info != null) {
            h.ivFoto.setImageResource(info.imageRes);
        } else {
            h.ivFoto.setImageResource(imageFor(f));
        }

        // Estado de favorito (seleccionado = en favoritos)
        h.ivFav.setSelected(true);

        // Quitar de favoritos con opción de deshacer
        h.ivFav.setOnClickListener(v -> {
            h.ivFav.setSelected(false);
            Executors.newSingleThreadExecutor().execute(() -> repo.remove(f.itemId));

            Snackbar.make(h.itemView, "Quitado de favoritos", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", a -> {
                        h.ivFav.setSelected(true);
                        Executors.newSingleThreadExecutor().execute(() -> repo.add(f));
                    }).show();
        });

        // Abrir detalle
        h.btnDetalles.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, DetalleArticuloActivity.class);
            intent.putExtra("id", f.itemId);
            intent.putExtra("titulo", f.title != null ? f.title : (info != null ? info.title : "--"));
            intent.putExtra("ubicacion", f.location != null ? f.location : (info != null ? info.location : "--"));
            intent.putExtra("precio", formatPrice(priceValue));
            intent.putExtra("rating", String.format(Locale.getDefault(), "%.1f", effectiveRating));
            intent.putExtra("imageRes", info != null ? info.imageRes : imageFor(f)); // << manda la imagen correcta
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void ensureReviewObservation(String key) {
        if (key == null || observedKeys.contains(key)) return;
        observedKeys.add(key);

        reviewRepo.observeAverage(key).observe(lifecycleOwner, avg -> {
            RatingSnapshot snap = ratingSnapshots.computeIfAbsent(key, k -> new RatingSnapshot());
            snap.average = avg != null ? avg : 0d;
            snap.hasAverage = avg != null;
            notifyDataSetChanged();
        });

        reviewRepo.observeCount(key).observe(lifecycleOwner, count -> {
            RatingSnapshot snap = ratingSnapshots.computeIfAbsent(key, k -> new RatingSnapshot());
            snap.count = count != null ? count : 0;
            notifyDataSetChanged();
        });
    }

    private double getEffectiveRating(String key, TourData.TourInfo info, FavoriteEntity f) {
        RatingSnapshot snap = ratingSnapshots.get(key);
        if (snap != null && snap.hasAverage && snap.count > 0) return snap.average;
        if (info != null) return info.defaultRating;
        if (f.rating != null) return f.rating;
        return 0d;
    }

    private String buildRatingText(String key, TourData.TourInfo info, FavoriteEntity f) {
        RatingSnapshot snap = ratingSnapshots.get(key);
        if (snap != null && snap.count > 0 && snap.hasAverage) {
            String label = snap.count == 1 ? "reseña" : "reseñas";
            return String.format(Locale.getDefault(), "%.1f • %d %s", snap.average, snap.count, label);
        }
        if (info != null && info.defaultReviewCount > 0) {
            return String.format(Locale.getDefault(), "%.1f • %d reseñas", info.defaultRating, info.defaultReviewCount);
        }
        if (f.rating != null) {
            return String.format(Locale.getDefault(), "%.1f", f.rating);
        }
        return "Sin reseñas";
    }

    private String buildStars(double rating) {
        int filled = (int) Math.round(rating);
        if (filled < 0) filled = 0;
        if (filled > 5) filled = 5;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 5; i++) b.append(i < filled ? '★' : '☆');
        return b.toString();
    }

    private String formatPrice(double price) {
        return price <= 0 ? "S/0" : String.format(Locale.getDefault(), "S/%.0f", price);
    }

    /** Devuelve el drawable correcto según el itemId del favorito */
    private int imageFor(FavoriteEntity f) {
        if (f == null || f.itemId == null) return R.drawable.mapi; // fallback
        switch (f.itemId) {
            case "T-001": // Machu Picchu
                return R.drawable.mapi;
            case "T-002": // Lago Titicaca
                return R.drawable.lagotiticaca;
            case "T-003": // Montaña de 7 Colores
                return R.drawable.montanacolores;
            default:
                return R.drawable.mapi; // placeholder genérico si no coincide
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivFoto, ivFav;
        TextView tvTitulo, tvUbic, tvPrecio, tvEstrellas, tvRatingTxt;
        Button btnDetalles;

        VH(@NonNull View itemView) {
            super(itemView);
            ivFoto      = itemView.findViewById(R.id.iv_destino_big);
            ivFav       = itemView.findViewById(R.id.iv_favorito);
            tvTitulo    = itemView.findViewById(R.id.tv_titulo);
            tvUbic      = itemView.findViewById(R.id.tv_ubicacion);
            tvPrecio    = itemView.findViewById(R.id.tv_precio_desde);
            tvEstrellas = itemView.findViewById(R.id.tv_rating_estrellas);
            tvRatingTxt = itemView.findViewById(R.id.tv_rating_texto);
            btnDetalles = itemView.findViewById(R.id.btn_ver_detalles);
        }
    }

    static class RatingSnapshot {
        double average;
        int count;
        boolean hasAverage;
    }
}
