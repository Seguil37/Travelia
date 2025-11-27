package com.proyecto.travelia.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String tourId;
    public String userName;
    public float rating;
    public String comment;
    public long createdAt;

    public ReviewEntity(String tourId, String userName, float rating, String comment, long createdAt) {
        this.tourId = tourId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
