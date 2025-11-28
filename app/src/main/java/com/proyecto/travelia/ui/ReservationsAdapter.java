package com.proyecto.travelia.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.proyecto.travelia.R;
import com.proyecto.travelia.ui.Usuario.Reservation;

public class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ReservationViewHolder>{
    private List<Reservation> reservationList;
    public ReservationsAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }
    @NonNull
    @Override
    public ReservationsAdapter.ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_tour, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationsAdapter.ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        holder.tvTitulo.setText(reservation.name);
        holder.tvUbicacion.setText(reservation.location);
        holder.metaContainer.setVisibility(View.VISIBLE);
        holder.tvFecha.setText("Fecha: " + reservation.date);
        holder.tvParticipantes.setText("Personas: " + reservation.participants);
        holder.tvPrecio.setText(reservation.price);
        holder.btnEliminar.setVisibility(View.VISIBLE);
        holder.ivFavorito.setVisibility(View.GONE);

        holder.tvEstrellas.setText("☆☆☆☆☆");
        holder.tvRatingTxt.setText("Sin reseñas");

        // Aquí deberías cargar la imagen usando Glide o Picasso si fuera necesario,
        // o usar holder.ivReserva.setImageResource(reservation.imageResource);

        // Manejar el clic del botón de eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            // TODO: Implementar lógica de eliminación
            // Toast.makeText(v.getContext(), "Eliminar: " + reservation.name, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public class ReservationViewHolder extends RecyclerView.ViewHolder {

        ImageView ivReserva, ivFavorito;
        TextView tvTitulo, tvUbicacion, tvFecha, tvParticipantes, tvPrecio, tvEstrellas, tvRatingTxt;
        ImageButton btnEliminar;
        View metaContainer;
        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReserva = itemView.findViewById(R.id.iv_destino_big);
            ivFavorito = itemView.findViewById(R.id.iv_favorito);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion);
            metaContainer = itemView.findViewById(R.id.reservation_meta_container);
            tvFecha = itemView.findViewById(R.id.tv_reserva_fecha);
            tvParticipantes = itemView.findViewById(R.id.tv_reserva_participantes);
            tvPrecio = itemView.findViewById(R.id.tv_precio_desde);
            tvEstrellas = itemView.findViewById(R.id.tv_rating_estrellas);
            tvRatingTxt = itemView.findViewById(R.id.tv_rating_texto);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_reserva);
        }
    }
}
