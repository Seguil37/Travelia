package com.proyecto.travelia.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.proyecto.travelia.data.local.AppDatabase;
import com.proyecto.travelia.data.local.FavoriteEntity;
import com.proyecto.travelia.data.local.FavoritesDao;
import java.util.List;

public class FavoritesRepository {

    private final FavoritesDao dao;
    private final String userId = "guest";

    public FavoritesRepository(Context ctx) {
        this.dao = AppDatabase.get(ctx).favoritesDao();
    }

    public boolean isFavoriteSync(String itemId) {
        return dao.existsSync(itemId, "TOUR") > 0;
    }

    public void add(FavoriteEntity e) {
        dao.insert(e);
    }

    public void remove(String itemId) {
        dao.deleteByKey(itemId, "TOUR");
    }

    public LiveData<List<FavoriteEntity>> observeAll() {
        return dao.observeAll();
    }
}
