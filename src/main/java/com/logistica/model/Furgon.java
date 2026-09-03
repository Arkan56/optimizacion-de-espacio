package com.logistica.model;

public class Furgon {
    private double largo;  // Eje Z (Fondo a Puerta)
    private double ancho;  // Eje X (Izquierda a Derecha)
    private double alto;   // Eje Y (Piso a Techo)

    public Furgon(double largo, double ancho, double alto) {
        this.largo = largo;
        this.ancho = ancho;
        this.alto = alto;
    }

    public double getLargo() { return largo; }
    public double getAncho() { return ancho; }
    public double getAlto() { return alto; }
    public double getVolumenTotal() { return largo * ancho * alto; }
}