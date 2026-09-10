package com.logistica.controllers.carga;

import com.logistica.dto.ItemMedicamentoDTO;
import com.logistica.model.Medicamento;
import com.logistica.services.carga.EmpaqueService;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class ConsolidacionMedicamentosController {

    @FXML
    private ListView<Medicamento> catalogoListView;
    @FXML
    private Spinner<Integer> cantidadSpinner;
    @FXML
    private ListView<ItemMedicamentoDTO> seleccionadosListView;
    @FXML
    private Label resumenLabel;

    private final EmpaqueService empaqueService = new EmpaqueService();

    @FXML
    public void initialize() {
        // Configurar el Spinner de cantidad (min: 1, max: 500, inicial: 1)
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 1);
        cantidadSpinner.setValueFactory(valueFactory);

        // Cargar catálogo Roche
        catalogoListView.getItems().setAll(empaqueService.obtenerCatalogoRoche());

        actualizarResumen();
    }

    /**
     * Carga los medicamentos que el pedido ya tiene asignados previamente.
     */
    public void cargarMedicamentosExistentes(List<ItemMedicamentoDTO> itemsExistentes) {
        seleccionadosListView.getItems().clear();
        if (itemsExistentes != null) {
            for (ItemMedicamentoDTO item : itemsExistentes) {
                // Crear una copia DTO para permitir cancelar cambios sin alterar el pedido
                // original
                seleccionadosListView.getItems().add(new ItemMedicamentoDTO(item.getMedicamento(), item.getCantidad()));
            }
        }
        seleccionadosListView.refresh();
        actualizarResumen();
    }

    @FXML
    private void agregarItem() {
        Medicamento seleccionado = catalogoListView.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        int cantidad = cantidadSpinner.getValue();

        // Buscar si ya está en la lista para acumular la cantidad
        boolean existe = false;
        for (ItemMedicamentoDTO item : seleccionadosListView.getItems()) {
            if (item.getMedicamento().getIdItem().equals(seleccionado.getIdItem())) {
                item.setCantidad(item.getCantidad() + cantidad);
                existe = true;
                break;
            }
        }

        if (!existe) {
            seleccionadosListView.getItems().add(new ItemMedicamentoDTO(seleccionado, cantidad));
        }

        seleccionadosListView.refresh();
        actualizarResumen();
    }

    @FXML
    private void quitarItem() {
        ItemMedicamentoDTO seleccionado = seleccionadosListView.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            seleccionadosListView.getItems().remove(seleccionado);
            actualizarResumen();
        }
    }

    private void actualizarResumen() {
        int totalItems = 0;
        double volumenTotal = 0;

        for (ItemMedicamentoDTO item : seleccionadosListView.getItems()) {
            totalItems += item.getCantidad();
            volumenTotal += item.getVolumenTotalCm3();
        }

        resumenLabel.setText(String.format("Total: %d unidad(es) | Volumen estimado: %.2f cm³",
                totalItems, volumenTotal));
    }

    public List<ItemMedicamentoDTO> getItemsSeleccionados() {
        return new ArrayList<>(seleccionadosListView.getItems());
    }
}