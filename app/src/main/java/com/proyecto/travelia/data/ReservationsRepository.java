package com.proyecto.travelia.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.proyecto.travelia.data.local.AppDatabase;
import com.proyecto.travelia.data.local.ReservationDao;
import com.proyecto.travelia.data.local.ReservationEntity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservationsRepository {

    private final ReservationDao dao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public ReservationsRepository(Context context) {
        dao = AppDatabase.get(context).reservationDao();
    }

    public LiveData<List<ReservationEntity>> observeAll() {
        return dao.observeAll();
    }

    public void upsert(ReservationEntity entity) {
        ioExecutor.execute(() -> dao.upsert(entity));
    }

    public void remove(String reservationId) {
        ioExecutor.execute(() -> dao.deleteById(reservationId));
    }

    public void clearAll() {
        ioExecutor.execute(dao::clearAll);
    }

    public static String buildId(String tourId, String date) {
        String safeTour = tourId == null ? "tour" : tourId.trim();
        String safeDate = date == null ? String.format(Locale.getDefault(), "%d", System.currentTimeMillis()) : date.trim();
        return safeTour + "_" + safeDate.replaceAll("\\s", "_");
    }
}
