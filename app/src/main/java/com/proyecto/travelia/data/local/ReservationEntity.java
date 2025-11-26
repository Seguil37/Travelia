package com.proyecto.travelia.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reservations")
public class ReservationEntity {

    @PrimaryKey
    @NonNull
    public String id; // combination of tour/date to avoid duplicates

    public String tourId;
    public String title;
    public String location;
    public String date;
    public String participants;
    public double price;
    public int imageRes;
    public long createdAt;

    public String paymentStatus;

    public ReservationEntity(@NonNull String id,
                              String tourId,
                              String title,
                              String location,
                              String date,
                              String participants,
                              double price,
                              int imageRes,
                              long createdAt,
                                String paymentStatus) {

        this.id = id;
        this.tourId = tourId;
        this.title = title;
        this.location = location;
        this.date = date;
        this.participants = participants;
        this.price = price;
        this.imageRes = imageRes;
        this.createdAt = createdAt;
        this.paymentStatus = paymentStatus;
    }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getParticipants() { return participants; }
    public double getPrice() { return price; }
    public String getPaymentStatus() { return paymentStatus; }
}
