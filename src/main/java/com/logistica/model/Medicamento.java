package com.logistica.model;

public class Medicamento {
    private final String idItem;
    private final String descripcion;
    private double alto; // cm (efectivo / rotado)
    private double ancho; // cm (efectivo / rotado)
    private double largo; // cm (efectivo / rotado)
    private final double pesoGramos;

    // Coordenadas 3D calculadas por el Solver dentro de la Nevera
    private double posX;
    private double posY;
    private double posZ;

    public Medicamento(String idItem, String descripcion, double alto, double ancho, double largo, double pesoGramos) {
        this.idItem = idItem;
        this.descripcion = descripcion;
        this.alto = alto;
        this.ancho = ancho;
        this.largo = largo;
        this.pesoGramos = pesoGramos;
    }

    // Constructor copia actualizando las dimensiones efectivas tras rotación
    public Medicamento(Medicamento otro, double anchoEf, double altoEf, double largoEf) {
        this.idItem = otro.idItem;
        this.descripcion = otro.descripcion;
        this.ancho = anchoEf;
        this.alto = altoEf;
        this.largo = largoEf;
        this.pesoGramos = otro.pesoGramos;
        this.posX = otro.posX;
        this.posY = otro.posY;
        this.posZ = otro.posZ;
    }

    public String getIdItem() {
        return idItem;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getAlto() {
        return alto;
    }

    public double getAncho() {
        return ancho;
    }

    public double getLargo() {
        return largo;
    }

    public double getPesoGramos() {
        return pesoGramos;
    }

    public double getVolumenCm3() {
        return alto * ancho * largo;
    }

    public double getAreaBaseCm2() {
        return ancho * largo;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public void setPosicion(double posX, double posY, double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public void setDimensiones(double ancho, double alto, double largo) {
        this.ancho = ancho;
        this.alto = alto;
        this.largo = largo;
    }

    @Override
    public String toString() {
        return idItem + " - " + descripcion + " (" + largo + "x" + ancho + "x" + alto + " cm)";
    }
}