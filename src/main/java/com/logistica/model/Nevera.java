package com.logistica.model;

import com.logistica.dto.ItemMedicamentoDTO;
import java.util.ArrayList;
import java.util.List;

public class Nevera {

    public enum Tipo {
        CAPACIDAD_56L("56 Litros"),
        CAPACIDAD_18L("18 Litros");

        private final String descripcion;

        Tipo(String descripcion) {
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    private String id;
    private Tipo tipo;
    private double largo;
    private double ancho;
    private double alto;
    private double pesoKg;
    private int ordenParada;

    private double posX;
    private double posY;
    private double posZ;

    // Resumen DTO para listados
    private List<ItemMedicamentoDTO> contenidoMedicamentos = new ArrayList<>();

    // Lista física de cajas individuales con coordenadas (posX, posY, posZ)
    private List<Medicamento> medicamentosEmpacados = new ArrayList<>();

    public Nevera(String id, Tipo tipo, double largo, double ancho, double alto, double pesoKg, int ordenParada) {
        this.id = id;
        this.tipo = tipo;
        this.largo = largo;
        this.ancho = ancho;
        this.alto = alto;
        this.pesoKg = pesoKg;
        this.ordenParada = ordenParada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public double getLargo() {
        return largo;
    }

    public void setLargo(double largo) {
        this.largo = largo;
    }

    public double getAncho() {
        return ancho;
    }

    public void setAncho(double ancho) {
        this.ancho = ancho;
    }

    public double getAlto() {
        return alto;
    }

    public void setAlto(double alto) {
        this.alto = alto;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public int getOrdenParada() {
        return ordenParada;
    }

    public void setOrdenParada(int ordenParada) {
        this.ordenParada = ordenParada;
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

    public List<ItemMedicamentoDTO> getContenidoMedicamentos() {
        return contenidoMedicamentos;
    }

    public void setContenidoMedicamentos(List<ItemMedicamentoDTO> contenidoMedicamentos) {
        this.contenidoMedicamentos = contenidoMedicamentos;
    }

    public List<Medicamento> getMedicamentosEmpacados() {
        return medicamentosEmpacados;
    }

    public void setMedicamentosEmpacados(List<Medicamento> medicamentosEmpacados) {
        this.medicamentosEmpacados = medicamentosEmpacados;
    }

    @Override
    public String toString() {
        return id + " (" + tipo + ") - " + pesoKg + " kg";
    }
}