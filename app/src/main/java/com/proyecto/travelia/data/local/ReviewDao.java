package com.proyecto.travelia.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReviewDao {

    @Query("SELECT * FROM reviews WHERE tourId = :tourId ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> observeByTour(String tourId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ReviewEntity entity);

    @Query("SELECT AVG(rating) FROM reviews WHERE tourId = :tourId")
    LiveData<Double> observeAverage(String tourId);

    @Query("SELECT COUNT(*) FROM reviews WHERE tourId = :tourId")
    LiveData<Integer> observeCount(String tourId);
}
