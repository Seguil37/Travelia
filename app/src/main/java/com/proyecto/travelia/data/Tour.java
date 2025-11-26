package com.proyecto.travelia.data;

public class Tour {
    public final String id;
    public final String title;
    public final String location;
    public final double price;
    public final double rating;
    public final String ratingText;
    public final int imageRes;
    public final String category;
    public final String type;

    public Tour(String id, String title, String location, double price, double rating,
                String ratingText, int imageRes, String category, String type) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.price = price;
        this.rating = rating;
        this.ratingText = ratingText;
        this.imageRes = imageRes;
        this.category = category;
        this.type = type;
    }
}
