package com.proyecto.travelia;

public class CardData {
    public String codigo, nombre, lugar, precio, estrellas, reseñas;
    public int imagen;

    public CardData(String codigo, String nombre, String lugar, String precio,
                    String estrellas, String reseñas, int imagen) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.lugar = lugar;
        this.precio = precio;
        this.estrellas = estrellas;
        this.reseñas = reseñas;
        this.imagen = imagen;
    }
}
