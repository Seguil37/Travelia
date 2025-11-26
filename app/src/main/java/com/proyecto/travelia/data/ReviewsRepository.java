package com.proyecto.travelia.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.proyecto.travelia.data.local.AppDatabase;
import com.proyecto.travelia.data.local.ReviewDao;
import com.proyecto.travelia.data.local.ReviewEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewsRepository {

    private final ReviewDao dao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    public ReviewsRepository(Context context) {
        dao = AppDatabase.get(context).reviewDao();
    }

    public LiveData<List<ReviewEntity>> observeByTour(String tourId) {
        return dao.observeByTour(tourId);
    }

    public LiveData<Integer> observeCount(String tourId) { return dao.observeCount(tourId); }
    public LiveData<Double> observeAverage(String tourId) { return dao.observeAverage(tourId); }
    public LiveData<Double> observeGuideAverage(String tourId) { return dao.observeGuideAverage(tourId); }
    public LiveData<Double> observeTransportAverage(String tourId) { return dao.observeTransportAverage(tourId); }
    public LiveData<Double> observeValueAverage(String tourId) { return dao.observeValueAverage(tourId); }

    public void addReview(ReviewEntity entity) {
        ioExecutor.execute(() -> dao.insert(entity));
    }
}
