package com.proyecto.travelia.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.proyecto.travelia.DetalleArticuloActivity;
import com.proyecto.travelia.LoginActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.SessionManager;
import com.proyecto.travelia.data.Tour;
import com.proyecto.travelia.data.TourDataSource;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.VH> {

    public interface EmptyListener { void onEmpty(boolean isEmpty); }

    private final List<Tour> data = new ArrayList<>();
    private final FavoritesRepository favoritesRepository;
    private final Context context;
    private EmptyListener emptyListener;

    public TourAdapter(Context context, FavoritesRepository favoritesRepository) {
        this.context = context;
        this.favoritesRepository = favoritesRepository;
    }

    public void setEmptyListener(EmptyListener listener) { this.emptyListener = listener; }

    public void submit(List<Tour> tours) {
        data.clear();
        if (tours != null) data.addAll(tours);
        notifyDataSetChanged();
        if (emptyListener != null) emptyListener.onEmpty(data.isEmpty());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_destino, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Tour tour = data.get(position);

        holder.tvTitulo.setText(tour.title);
        holder.tvUbic.setText(tour.location);
        holder.tvPrecio.setText(String.format(Locale.getDefault(), "S/%.0f", tour.price));
        holder.tvEstrellas.setText("★★★★☆");
        holder.tvRatingTxt.setText(tour.ratingText);
        holder.ivFoto.setImageResource(tour.imageRes);

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean isFav = favoritesRepository.isFavoriteSync(tour.id);
            holder.itemView.post(() -> holder.ivFav.setSelected(isFav));
        });

        View.OnClickListener openDetail = v -> {
            Intent intent = new Intent(context, DetalleArticuloActivity.class);
            intent.putExtra("id", tour.id);
            intent.putExtra("titulo", tour.title);
            intent.putExtra("ubicacion", tour.location);
            intent.putExtra("precio", String.format(Locale.getDefault(), "S/%.0f", tour.price));
            intent.putExtra("rating", tour.ratingText);
            intent.putExtra("imageRes", tour.imageRes);
            context.startActivity(intent);
        };

        holder.btnDetalles.setOnClickListener(openDetail);
        holder.itemView.setOnClickListener(openDetail);
        holder.ivFoto.setOnClickListener(openDetail);

        holder.ivFav.setOnClickListener(v -> {
            if (!SessionManager.isLoggedIn(context)) {
                Toast.makeText(context, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }
            boolean nowSelected = !holder.ivFav.isSelected();
            holder.ivFav.setSelected(nowSelected);
            if (nowSelected) {
                String userId = SessionManager.getUserEmail(context);
                if (userId == null) userId = "guest";
                FavoriteEntity entity = new FavoriteEntity(
                        userId,
                        tour.id,
                        "TOUR",
                        tour.title,
                        tour.location,
                        "img_local",
                        tour.price,
                        tour.rating,
                        System.currentTimeMillis()
                );
                Executors.newSingleThreadExecutor().execute(() -> favoritesRepository.add(entity));
                Snackbar.make(holder.itemView, "Añadido a favoritos", Snackbar.LENGTH_SHORT).show();
            } else {
                Executors.newSingleThreadExecutor().execute(() -> favoritesRepository.remove(tour.id));
                Snackbar.make(holder.itemView, "Quitado de favoritos", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void showTour(String tourId) {
        Tour tour = TourDataSource.findById(tourId);
        if (tour != null) {
            submit(java.util.Collections.singletonList(tour));
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivFoto, ivFav;
        TextView tvTitulo, tvUbic, tvPrecio, tvEstrellas, tvRatingTxt;
        Button btnDetalles;

        VH(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.iv_destino_big);
            ivFav = itemView.findViewById(R.id.iv_favorito);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvUbic = itemView.findViewById(R.id.tv_ubicacion);
            tvPrecio = itemView.findViewById(R.id.tv_precio_desde);
            tvEstrellas = itemView.findViewById(R.id.tv_rating_estrellas);
            tvRatingTxt = itemView.findViewById(R.id.tv_rating_texto);
            btnDetalles = itemView.findViewById(R.id.btn_ver_detalles);
        }
    }
}
