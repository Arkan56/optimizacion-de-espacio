package com.logistica.model;

import com.logistica.dto.ItemMedicamentoDTO;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String id;
    private String cliente;
    private int ordenParada;
    private List<Nevera> neveras;
    private List<ItemMedicamentoDTO> medicamentos; // Listado total de la orden

    public Pedido(String id, String cliente, int ordenParada) {
        this.id = id;
        this.cliente = cliente;
        this.ordenParada = ordenParada;
        this.neveras = new ArrayList<>();
        this.medicamentos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public int getOrdenParada() {
        return ordenParada;
    }

    public void setOrdenParada(int ordenParada) {
        this.ordenParada = ordenParada;
    }

    public List<Nevera> getNeveras() {
        return neveras;
    }

    public void agregarNevera(Nevera nevera) {
        this.neveras.add(nevera);
    }

    public List<ItemMedicamentoDTO> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<ItemMedicamentoDTO> medicamentos) {
        this.medicamentos = medicamentos;
    }

    @Override
    public String toString() {
        return id + " - " + cliente + " (Parada " + ordenParada + ")";
    }
}