package com.logistica.dto;

import com.logistica.model.Medicamento;

public class ItemMedicamentoDTO {
    private final Medicamento medicamento;
    private int cantidad;

    public ItemMedicamentoDTO(Medicamento medicamento, int cantidad) {
        this.medicamento = medicamento;
        this.cantidad = cantidad;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getVolumenTotalCm3() {
        return medicamento.getVolumenCm3() * cantidad;
    }

    public double getPesoTotalGramos() {
        return medicamento.getPesoGramos() * cantidad;
    }

    @Override
    public String toString() {
        return cantidad + "x " + medicamento.getDescripcion() +
                " (" + String.format("%.1f", getVolumenTotalCm3()) + " cm³)";
    }
}