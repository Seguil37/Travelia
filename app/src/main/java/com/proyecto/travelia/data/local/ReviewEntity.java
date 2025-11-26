package com.proyecto.travelia.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String tourId;
    public String userId;
    public String userName;
    public double ratingOverall;
    public double ratingGuide;
    public double ratingTransport;
    public double ratingValue;
    public String comment;
    public long createdAt;

    public ReviewEntity(String tourId, String userId, String userName, double ratingOverall,
                        double ratingGuide, double ratingTransport, double ratingValue,
                        String comment, long createdAt) {
        this.tourId = tourId;
        this.userId = userId;
        this.userName = userName;
        this.ratingOverall = ratingOverall;
        this.ratingGuide = ratingGuide;
        this.ratingTransport = ratingTransport;
        this.ratingValue = ratingValue;
        this.comment = comment;
        this.createdAt = createdAt;
    }
}
