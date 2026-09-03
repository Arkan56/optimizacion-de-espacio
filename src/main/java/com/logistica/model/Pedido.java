package com.logistica.model;

import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private String id;
    private String cliente;
    private int ordenParada;

    private List<Nevera> neveras;

    public Pedido(String id, String cliente, int ordenParada) {

        this.id = id;
        this.cliente = cliente;
        this.ordenParada = ordenParada;

        this.neveras = new ArrayList<>();
    }

    public void agregarNevera(Nevera nevera) {

        neveras.add(nevera);
    }

    public String getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public int getOrdenParada() {
        return ordenParada;
    }

    public List<Nevera> getNeveras() {
        return neveras;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public void setOrdenParada(int ordenParada) {
        this.ordenParada = ordenParada;
    }

    @Override
    public String toString() {

        return id
                + " - "
                + cliente
                + " - Parada "
                + ordenParada;
    }
}