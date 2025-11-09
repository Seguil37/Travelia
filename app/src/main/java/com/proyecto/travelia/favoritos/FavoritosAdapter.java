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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.proyecto.travelia.DetalleArticuloActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.data.FavoritesRepository;
import com.proyecto.travelia.data.local.FavoriteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.VH> {

    private final FavoritesRepository repo;
    private final Context ctx;
    private final List<FavoriteEntity> data = new ArrayList<>();

    public FavoritosAdapter(FavoritesRepository repo, Context ctx) {
        this.repo = repo;
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

        // Texto
        h.tvTitulo.setText(f.title != null ? f.title : "--");
        h.tvUbic.setText(f.location != null ? f.location : "--");
        h.tvPrecio.setText(f.price != null ? "S/" + f.price.intValue() : "--");
        h.tvEstrellas.setText("★★★★☆");
        h.tvRatingTxt.setText(f.rating != null ? String.valueOf(f.rating) : "4.5");

        // Imagen por itemId (T-001, T-002, T-003, ...)
        h.ivFoto.setImageResource(imageFor(f));

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
            intent.putExtra("titulo", f.title);
            intent.putExtra("ubicacion", f.location);
            intent.putExtra("precio", f.price != null ? "S/" + f.price.intValue() : "--");
            intent.putExtra("rating", f.rating != null ? String.valueOf(f.rating) : "4.5");
            intent.putExtra("imageRes", imageFor(f)); // << manda la imagen correcta
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
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
}
