package com.proyecto.travelia.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReservationDao {

    @Query("SELECT * FROM reservations ORDER BY createdAt DESC")
    LiveData<List<ReservationEntity>> observeAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ReservationEntity entity);

    @Query("DELETE FROM reservations WHERE id = :reservationId")
    void deleteById(String reservationId);

    @Query("DELETE FROM reservations")
    void clearAll();
    @Query("SELECT * FROM reservations")
    List<ReservationEntity> getAllReservations();
}
