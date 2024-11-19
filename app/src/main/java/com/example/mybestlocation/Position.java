package com.example.mybestlocation;

public class Position {


    int id;
    String pseudo,longitude,latitude,numero;

    public Position( int id,String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }
    public Position(String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }


    @Override
    public String toString() {
        return "position{" +
                "idposition=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public int getId() {
        return this.id;
    }
}
