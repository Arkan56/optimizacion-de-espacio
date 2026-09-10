package com.logistica.services.carga;

import com.logistica.dto.ItemMedicamentoDTO;
import com.logistica.model.Medicamento;
import com.logistica.model.Pedido;
import com.logistica.solver.EmpaquetadorOrden;

import java.util.ArrayList;
import java.util.List;

public class EmpaqueService {

    public List<Medicamento> obtenerCatalogoRoche() {
        List<Medicamento> catalogo = new ArrayList<>();
        catalogo.add(new Medicamento("10202106", "ACTEMRA 162MG/0.9ML", 12.2, 3.0, 13.5, 92));
        catalogo.add(new Medicamento("10231550", "ACTEMRA 200MG/10ML", 7.7, 5.4, 5.4, 56));
        catalogo.add(new Medicamento("11008137", "ALECENSA 150MG", 16.0, 13.5, 19.0, 38));
        catalogo.add(new Medicamento("10194316", "AVASTIN 100MG/4ML", 7.8, 5.5, 4.0, 33));
        catalogo.add(new Medicamento("10227682", "EVRYSDI 0.75MG/1ML", 15.0, 7.0, 9.7, 237));
        catalogo.add(new Medicamento("10195544", "HERCEPTIN 440MG", 9.7, 5.3, 9.0, 104));
        catalogo.add(new Medicamento("10143338", "MABTHERA 100MG/10ML", 6.5, 5.3, 9.0, 80));
        catalogo.add(new Medicamento("10186580", "MIRCERA 100MCG/0.3ML", 5.0, 3.5, 17.7, 42));
        catalogo.add(new Medicamento("10227162", "PHESGO 1200/600MG", 7.7, 5.4, 5.4, 58));
        catalogo.add(new Medicamento("11003503", "TECENTRIQ 1200MG/20ML", 7.7, 5.4, 5.4, 56));
        catalogo.add(new Medicamento("10251712", "VABYSMO 6MG/0.05ML", 6.5, 13.5, 3.4, 17));
        return catalogo;
    }

    public void consolidarMedicamentosEnPedido(Pedido pedido, List<ItemMedicamentoDTO> items) {
        // 1. Limpiar neveras y medicamentos anteriores para no duplicar
        pedido.getNeveras().clear();
        pedido.getMedicamentos().clear();

        // 2. Procesar el empaquetado desde cero con la nueva lista
        if (items != null && !items.isEmpty()) {
            EmpaquetadorOrden.procesarYAsignarNeveras(pedido, items);
        }
    }
}