package com.proyecto.travelia.data.local;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites",
        indices = {@Index(value = {"userId","itemId","itemType"}, unique = true)})
public class FavoriteEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String userId;      // "guest" si no hay login
    public String itemId;      // ID del tour o destino
    public String itemType;    // "TOUR"
    public String title;
    public String location;
    public String imageUrl;
    public Double price;
    public Double rating;
    public Long updatedAt;

    public FavoriteEntity(String userId, String itemId, String itemType,
                          String title, String location, String imageUrl,
                          Double price, Double rating, Long updatedAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.title = title;
        this.location = location;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.updatedAt = updatedAt;
    }
}

