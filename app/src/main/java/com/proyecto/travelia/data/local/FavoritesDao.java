package com.proyecto.travelia.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FavoritesDao {

    @Query("SELECT * FROM favorites ORDER BY updatedAt DESC")
    LiveData<List<FavoriteEntity>> observeAll();

    @Query("SELECT COUNT(*) FROM favorites WHERE itemId = :itemId AND itemType = :itemType")
    int existsSync(String itemId, String itemType);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(FavoriteEntity entity);

    @Query("DELETE FROM favorites WHERE itemId = :itemId AND itemType = :itemType")
    void deleteByKey(String itemId, String itemType);
}
