package com.logistica.model;

public class Nevera {
    public enum Tipo {
        L18, L56
    }

    private String id;
    private Tipo tipo;
    private double largo, ancho, alto; // en milímetros o cm
    private double pesoKg;
    private int ordenParada; // Restricción LIFO (1 = primera entrega)

    // Posición asignada dentro del furgón (coordenadas X, Y, Z)
    private double posX, posY, posZ;

    public Nevera(String id, Tipo tipo, double largo, double ancho, double alto, double pesoKg, int ordenParada) {
        this.id = id;
        this.tipo = tipo;
        this.largo = largo;
        this.ancho = ancho;
        this.alto = alto;
        this.pesoKg = pesoKg;
        this.ordenParada = ordenParada;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public double getLargo() {
        return largo;
    }

    public double getAncho() {
        return ancho;
    }

    public double getAlto() {
        return alto;
    }

    public int getOrdenParada() {
        return ordenParada;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosicion(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public void setLargo(double largo) {
        this.largo = largo;
    }

    public void setAncho(double ancho) {
        this.ancho = ancho;
    }

    public void setAlto(double alto) {
        this.alto = alto;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public void setOrdenParada(int ordenParada) {
        this.ordenParada = ordenParada;
    }

    @Override
    public String toString() {

        return id
                + " - "
                + tipo
                + " ("
                + ancho
                + " x "
                + alto
                + " x "
                + largo
                + ")";
    }
}