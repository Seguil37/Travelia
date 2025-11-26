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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationsAdapter.ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        // Mapeando a los IDs de tu item_reserva.xml
        holder.tvTitulo.setText(reservation.name);
        holder.tvUbicacion.setText(reservation.location);
        holder.tvFecha.setText("Fecha: " + reservation.date); // Añadir etiqueta si lo necesitas
        holder.tvParticipantes.setText("Personas: " + reservation.participants);
        holder.tvPrecio.setText(reservation.price);

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

        ImageView ivReserva;
        TextView tvTitulo, tvUbicacion, tvFecha, tvParticipantes, tvPrecio;
        ImageButton btnEliminar;
        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReserva = itemView.findViewById(R.id.iv_reserva);
            tvTitulo = itemView.findViewById(R.id.tv_reserva_titulo);
            tvUbicacion = itemView.findViewById(R.id.tv_reserva_ubicacion);
            tvFecha = itemView.findViewById(R.id.tv_reserva_fecha);
            tvParticipantes = itemView.findViewById(R.id.tv_reserva_participantes);
            tvPrecio = itemView.findViewById(R.id.tv_reserva_precio);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_reserva);
        }
    }
}
