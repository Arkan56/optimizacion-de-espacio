package com.logistica.controllers.carga;

import com.logistica.dto.ItemMedicamentoDTO;
import com.logistica.gui.FurgonViewer3D;
import com.logistica.gui.NeveraViewer3D;
import com.logistica.model.Furgon;
import com.logistica.model.Nevera;
import com.logistica.model.Pedido;
import com.logistica.services.carga.EmpaqueService;
import com.logistica.solver.PackSolverLIFO;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CargaController {

    private static class ConfiguracionNevera {
        final double ancho, alto, largo, peso;

        ConfiguracionNevera(double ancho, double alto, double largo, double peso) {
            this.ancho = ancho;
            this.alto = alto;
            this.largo = largo;
            this.peso = peso;
        }
    }

    @FXML
    private TextField anchoField;
    @FXML
    private TextField altoField;
    @FXML
    private TextField largoField;

    @FXML
    private Label volumenUtilizadoLabel;
    @FXML
    private Label neverasTotalesLabel;
    @FXML
    private Label pesoTotalLabel;
    @FXML
    private Label paradasTotalesLabel;
    @FXML
    private Label pesoVolumetricoLabel;

    @FXML
    private ListView<Pedido> pedidosListView;
    @FXML
    private ListView<Nevera> neverasListView;
    @FXML
    private StackPane visorContainer;

    private final EmpaqueService empaqueService = new EmpaqueService();
    private Furgon furgonActual;
    private final List<Pedido> pedidos = new ArrayList<>();
    private Pedido pedidoSeleccionado;

    @FXML
    public void initialize() {
        pedidosListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, pedidoAnterior, pedidoNuevo) -> {
                    pedidoSeleccionado = pedidoNuevo;
                    actualizarListaNeveras();
                });

        actualizarFurgon();
        actualizarIndicadores();
    }

    // ==========================================
    // CONSULTAS DE MEDICAMENTOS Y CONTENIDO
    // ==========================================

    @FXML
    private void verContenidoNevera() {
        Nevera seleccionada = neverasListView.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una nevera para consultar su contenido.");
            return;
        }

        List<ItemMedicamentoDTO> contenido = seleccionada.getContenidoMedicamentos();
        if (contenido.isEmpty()) {
            mostrarInformacion("La nevera " + seleccionada.getId() + " no tiene medicamentos registrados.");
            return;
        }

        ListView<String> listView = new ListView<>();
        for (ItemMedicamentoDTO item : contenido) {
            listView.getItems().add(item.getCantidad() + "x " + item.getMedicamento().getDescripcion());
        }

        VBox layout = new VBox(10, new Label("Tipo: " + seleccionada.getTipo()), listView);
        layout.setPadding(new Insets(15));

        Dialog<ButtonType> dialog = crearDialogo("Contenido de Nevera", "Nevera: " + seleccionada.getId(), layout);
        dialog.showAndWait();
    }

    @FXML
    private void mostrarVista3DNevera() {
        Nevera seleccionada = neverasListView.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Selecciona una nevera para visualizar su distribución 3D interna.");
            return;
        }

        SubScene escenaNevera3D = NeveraViewer3D.crearVistaEmpaquetadoNevera(
                seleccionada, visorContainer.getWidth(), visorContainer.getHeight());

        visorContainer.getChildren().clear();
        visorContainer.getChildren().add(escenaNevera3D);

        escenaNevera3D.widthProperty().bind(visorContainer.widthProperty());
        escenaNevera3D.heightProperty().bind(visorContainer.heightProperty());

        // Overlay con el contenido de la nevera
        VBox overlay = construirOverlayContenidoNevera(seleccionada);
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        overlay.setMouseTransparent(true);
        visorContainer.getChildren().add(overlay);
    }

    private VBox construirOverlayContenidoNevera(Nevera nevera) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle(
                "-fx-background-color: rgba(15, 20, 28, 0.85); -fx-background-radius: 8; -fx-border-color: #00bcd4; -fx-border-radius: 8; -fx-border-width: 1;");

        Label titulo = new Label("📦 Nevera " + nevera.getId() + " (" + nevera.getTipo() + ")");
        titulo.setStyle("-fx-text-fill: #00bcd4; -fx-font-weight: bold; -fx-font-size: 13px;");
        box.getChildren().add(titulo);

        List<ItemMedicamentoDTO> contenido = nevera.getContenidoMedicamentos();
        Map<String, Color> mapaColores = NeveraViewer3D.generarMapaColoresItems(contenido);

        if (contenido.isEmpty()) {
            Label vacio = new Label("(Sin asignación de medicamentos)");
            vacio.setStyle("-fx-text-fill: #cccccc; -fx-font-style: italic;");
            box.getChildren().add(vacio);
        } else {
            for (ItemMedicamentoDTO item : contenido) {
                Color colorMedicamento = mapaColores.getOrDefault(item.getMedicamento().getIdItem(), Color.WHITE);

                Rectangle swatch = new Rectangle(12, 12, colorMedicamento);
                swatch.setStroke(Color.WHITE);

                Label txt = new Label(item.getCantidad() + "x " + item.getMedicamento().getDescripcion());
                txt.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

                HBox fila = new HBox(8, swatch, txt);
                fila.setAlignment(Pos.CENTER_LEFT);
                box.getChildren().add(fila);
            }
        }

        return box;
    }

    // ==========================================
    // DEMÁS MÉTODOS EXISTENTES
    // ==========================================

    @FXML
    private void abrirVentanaEmpaquetado() {
        if (pedidoSeleccionado == null) {
            mostrarError("Selecciona un pedido de la lista para consolidar sus medicamentos.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/logistica/views/carga/ConsolidacionMedicamentosView.fxml"));
            Parent root = loader.load();
            ConsolidacionMedicamentosController dialogController = loader.getController();

            // 1. Cargar la lista previa de medicamentos del pedido seleccionado
            dialogController.cargarMedicamentosExistentes(pedidoSeleccionado.getMedicamentos());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Consolidación de Medicamentos - Roche");
            dialog.setHeaderText("Selecciona medicamentos para: " + pedidoSeleccionado.getId() + " ("
                    + pedidoSeleccionado.getCliente() + ")");
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // 2. Obtener la lista completa (existentes + nuevos agregados)
                    List<ItemMedicamentoDTO> seleccionados = dialogController.getItemsSeleccionados();

                    // 3. Consolidar el pedido y recalcular las neveras
                    empaqueService.consolidarMedicamentosEnPedido(pedidoSeleccionado, seleccionados);

                    // 4. Actualizar la vista e indicadores
                    actualizarListaNeveras();
                    actualizarIndicadores();

                    if (seleccionados.isEmpty()) {
                        mostrarInformacion("Se han retirado los medicamentos y actualizado las neveras del pedido.");
                    } else {
                        mostrarInformacion("Se han recalculado y asignado automáticamente las neveras necesarias.");
                    }
                }
            });

        } catch (IOException e) {
            mostrarError("No se pudo cargar la vista de consolidación: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarFurgon() {
        try {
            double ancho = Double.parseDouble(anchoField.getText());
            double alto = Double.parseDouble(altoField.getText());
            double largo = Double.parseDouble(largoField.getText());

            if (ancho <= 0 || alto <= 0 || largo <= 0) {
                mostrarError("Las dimensiones deben ser mayores que cero.");
                return;
            }

            furgonActual = new Furgon(largo, ancho, alto);
            actualizarIndicadores();

        } catch (NumberFormatException e) {
            mostrarError("Por favor ingresa valores numéricos válidos.");
        }
    }

    @FXML
    private void optimizarCarga() {
        if (furgonActual == null) {
            mostrarError("Primero debes configurar el furgón.");
            return;
        }

        List<Nevera> neveras = obtenerNeverasDePedidos();

        if (neveras.isEmpty()) {
            mostrarError("No hay neveras para optimizar.");
            return;
        }

        List<Nevera> acomodadas = PackSolverLIFO.resolverAcomodo(furgonActual, neveras);
        mostrarResultado3D(furgonActual, acomodadas);

        mostrarInformacion("Optimización completada.\n\n"
                + "Neveras solicitadas: " + neveras.size() + "\n"
                + "Neveras acomodadas: " + acomodadas.size());
    }

    @FXML
    private void crearPedido() {
        TextField clienteField = new TextField();
        TextField paradaField = new TextField();

        VBox contenido = new VBox(10,
                new Label("Nombre del cliente:"), clienteField,
                new Label("Número de parada:"), paradaField);
        contenido.setPadding(new Insets(15));

        Dialog<ButtonType> dialog = crearDialogo("Nuevo pedido", "Crear nuevo pedido", contenido);

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                try {
                    String cliente = clienteField.getText().trim();
                    String paradaTexto = paradaField.getText().trim();

                    if (cliente.isEmpty() || paradaTexto.isEmpty()) {
                        mostrarError("Todos los campos son obligatorios.");
                        return;
                    }

                    int parada = Integer.parseInt(paradaTexto);
                    if (parada <= 0) {
                        mostrarError("El número de parada debe ser mayor que cero.");
                        return;
                    }

                    if (existeParada(parada)) {
                        mostrarError("Ya existe un pedido asignado a la parada " + parada);
                        return;
                    }

                    String id = "P" + String.format("%03d", pedidos.size() + 1);
                    Pedido pedido = new Pedido(id, cliente, parada);

                    pedidos.add(pedido);
                    pedidosListView.getItems().add(pedido);
                    pedidosListView.getSelectionModel().select(pedido);
                    actualizarIndicadores();

                } catch (NumberFormatException e) {
                    mostrarError("El número de parada debe ser un número entero válido.");
                }
            }
        });
    }

    @FXML
    private void editarPedido() {
        Pedido pedido = pedidosListView.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            mostrarError("Debes seleccionar un pedido para editar.");
            return;
        }

        TextField clienteField = new TextField(pedido.getCliente());
        TextField paradaField = new TextField(String.valueOf(pedido.getOrdenParada()));

        VBox contenido = new VBox(10,
                new Label("Cliente:"), clienteField,
                new Label("Número de parada:"), paradaField);
        contenido.setPadding(new Insets(15));

        Dialog<ButtonType> dialog = crearDialogo("Editar pedido", "Editar " + pedido.getId(), contenido);

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                try {
                    String cliente = clienteField.getText().trim();
                    int nuevaParada = Integer.parseInt(paradaField.getText().trim());

                    if (cliente.isEmpty() || nuevaParada <= 0) {
                        mostrarError("Datos inválidos.");
                        return;
                    }

                    for (Pedido otro : pedidos) {
                        if (otro != pedido && otro.getOrdenParada() == nuevaParada) {
                            mostrarError("Ya existe otro pedido en la parada " + nuevaParada);
                            return;
                        }
                    }

                    pedido.setCliente(cliente);
                    pedido.setOrdenParada(nuevaParada);

                    for (Nevera nevera : pedido.getNeveras()) {
                        nevera.setOrdenParada(nuevaParada);
                    }

                    actualizarVistaPedidos();
                    actualizarListaNeveras();

                } catch (NumberFormatException e) {
                    mostrarError("La parada debe ser un número válido.");
                }
            }
        });
    }

    @FXML
    private void eliminarPedido() {
        Pedido pedido = pedidosListView.getSelectionModel().getSelectedItem();
        if (pedido == null) {
            mostrarError("Debes seleccionar un pedido para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar pedido");
        confirmacion.setHeaderText("¿Eliminar " + pedido.getId() + "?");
        confirmacion.setContentText("También se eliminarán todas las neveras asociadas.");

        confirmacion.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                pedidos.remove(pedido);
                pedidosListView.getSelectionModel().clearSelection();
                actualizarVistaPedidos();
                pedidoSeleccionado = null;
                neverasListView.getItems().clear();

                if (!pedidos.isEmpty()) {
                    pedidosListView.getSelectionModel().selectFirst();
                    pedidoSeleccionado = pedidosListView.getSelectionModel().getSelectedItem();
                    actualizarListaNeveras();
                }
                actualizarIndicadores();
            }
        });
    }

    @FXML
    private void agregarNevera() {
        if (pedidoSeleccionado == null) {
            mostrarError("Primero debes seleccionar un pedido.");
            return;
        }

        ComboBox<Nevera.Tipo> tipoComboBox = new ComboBox<>();
        tipoComboBox.getItems().addAll(Nevera.Tipo.values());
        tipoComboBox.getSelectionModel().selectFirst();

        TextField cantidadField = new TextField("1");
        TextField anchoNeveraField = new TextField();
        TextField altoNeveraField = new TextField();
        TextField largoNeveraField = new TextField();
        TextField pesoField = new TextField();

        anchoNeveraField.setEditable(false);
        altoNeveraField.setEditable(false);
        largoNeveraField.setEditable(false);
        pesoField.setEditable(false);

        Runnable actualizarCampos = () -> {
            Nevera.Tipo tipo = tipoComboBox.getValue();
            if (tipo != null) {
                ConfiguracionNevera config = obtenerConfiguracionNevera(tipo);
                anchoNeveraField.setText(String.valueOf(config.ancho));
                altoNeveraField.setText(String.valueOf(config.alto));
                largoNeveraField.setText(String.valueOf(config.largo));
                pesoField.setText(String.valueOf(config.peso));
            }
        };

        tipoComboBox.setOnAction(e -> actualizarCampos.run());
        actualizarCampos.run();

        VBox contenido = new VBox(10,
                new Label("Tipo:"), tipoComboBox,
                new Label("Cantidad:"), cantidadField,
                new Label("Ancho:"), anchoNeveraField,
                new Label("Alto:"), altoNeveraField,
                new Label("Largo:"), largoNeveraField,
                new Label("Peso (kg):"), pesoField);
        contenido.setPadding(new Insets(15));

        Dialog<ButtonType> dialog = crearDialogo("Agregar neveras", "Agregar neveras a " + pedidoSeleccionado.getId(),
                contenido);

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                try {
                    int cantidad = Integer.parseInt(cantidadField.getText().trim());
                    if (cantidad <= 0) {
                        mostrarError("La cantidad debe ser mayor que cero.");
                        return;
                    }

                    Nevera.Tipo tipo = tipoComboBox.getValue();
                    ConfiguracionNevera config = obtenerConfiguracionNevera(tipo);

                    for (int i = 0; i < cantidad; i++) {
                        String id = generarIdNevera();
                        Nevera nevera = new Nevera(
                                id, tipo, config.largo, config.ancho, config.alto, config.peso,
                                pedidoSeleccionado.getOrdenParada());
                        pedidoSeleccionado.agregarNevera(nevera);
                    }

                    actualizarListaNeveras();
                    actualizarIndicadores();

                } catch (NumberFormatException e) {
                    mostrarError("La cantidad debe ser un número entero válido.");
                }
            }
        });
    }

    @FXML
    private void editarNevera() {
        if (pedidoSeleccionado == null) {
            mostrarError("Primero debes seleccionar un pedido.");
            return;
        }

        Nevera nevera = neverasListView.getSelectionModel().getSelectedItem();
        if (nevera == null) {
            mostrarError("Debes seleccionar una nevera para editar.");
            return;
        }

        TextField idField = new TextField(nevera.getId());
        ComboBox<Nevera.Tipo> tipoComboBox = new ComboBox<>();
        tipoComboBox.getItems().addAll(Nevera.Tipo.values());
        tipoComboBox.setValue(nevera.getTipo());

        TextField largoField = new TextField();
        TextField anchoField = new TextField();
        TextField altoField = new TextField();
        TextField pesoField = new TextField();

        largoField.setEditable(false);
        anchoField.setEditable(false);
        altoField.setEditable(false);
        pesoField.setEditable(false);

        Runnable actualizarCampos = () -> {
            ConfiguracionNevera config = obtenerConfiguracionNevera(tipoComboBox.getValue());
            largoField.setText(String.valueOf(config.largo));
            anchoField.setText(String.valueOf(config.ancho));
            altoField.setText(String.valueOf(config.alto));
            pesoField.setText(String.valueOf(config.peso));
        };

        tipoComboBox.setOnAction(e -> actualizarCampos.run());
        actualizarCampos.run();

        VBox contenido = new VBox(10,
                new Label("ID:"), idField,
                new Label("Tipo:"), tipoComboBox,
                new Label("Largo:"), largoField,
                new Label("Ancho:"), anchoField,
                new Label("Alto:"), altoField,
                new Label("Peso:"), pesoField);
        contenido.setPadding(new Insets(15));

        Dialog<ButtonType> dialog = crearDialogo("Editar nevera", "Editar " + nevera.getId(), contenido);

        dialog.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                String nuevoId = idField.getText().trim();
                if (nuevoId.isEmpty()) {
                    mostrarError("El ID no puede estar vacío.");
                    return;
                }

                Nevera.Tipo nuevoTipo = tipoComboBox.getValue();
                ConfiguracionNevera nuevaConfig = obtenerConfiguracionNevera(nuevoTipo);

                nevera.setId(nuevoId);
                nevera.setTipo(nuevoTipo);
                nevera.setLargo(nuevaConfig.largo);
                nevera.setAncho(nuevaConfig.ancho);
                nevera.setAlto(nuevaConfig.alto);
                nevera.setPesoKg(nuevaConfig.peso);

                actualizarListaNeveras();
                actualizarIndicadores();
            }
        });
    }

    @FXML
    private void eliminarNevera() {
        if (pedidoSeleccionado == null) {
            mostrarError("Primero debes seleccionar un pedido.");
            return;
        }

        Nevera nevera = neverasListView.getSelectionModel().getSelectedItem();
        if (nevera == null) {
            mostrarError("Debes seleccionar una nevera para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar nevera");
        confirmacion.setHeaderText("¿Eliminar " + nevera.getId() + "?");
        confirmacion.setContentText("La nevera será eliminada del pedido " + pedidoSeleccionado.getId());

        confirmacion.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                pedidoSeleccionado.getNeveras().remove(nevera);
                actualizarListaNeveras();
                actualizarIndicadores();
            }
        });
    }

    private void actualizarIndicadores() {
        List<Nevera> neveras = obtenerNeverasDePedidos();
        neverasTotalesLabel.setText(String.valueOf(neveras.size()));

        double pesoTotal = neveras.stream().mapToDouble(Nevera::getPesoKg).sum();
        pesoTotalLabel.setText(String.format("%.2f kg", pesoTotal));

        paradasTotalesLabel.setText(String.valueOf(pedidos.size()));

        final double FACTOR_PESO_VOLUMETRICO = 400.0;
        double pesoVolumetricoTotal = 0;

        for (Nevera nevera : neveras) {
            double volumen = (nevera.getLargo() / 100.0) * (nevera.getAncho() / 100.0) * (nevera.getAlto() / 100.0);
            pesoVolumetricoTotal += volumen * FACTOR_PESO_VOLUMETRICO;
        }
        pesoVolumetricoLabel.setText(String.format("%.2f kg", pesoVolumetricoTotal));

        if (furgonActual != null) {
            double volumenFurgon = furgonActual.getLargo() * furgonActual.getAncho() * furgonActual.getAlto();
            double volumenNeveras = neveras.stream().mapToDouble(n -> n.getLargo() * n.getAncho() * n.getAlto()).sum();
            double porcentaje = (volumenNeveras / volumenFurgon) * 100;
            volumenUtilizadoLabel.setText(String.format("%.2f %%", porcentaje));
        } else {
            volumenUtilizadoLabel.setText("0%");
        }
    }

    private void mostrarResultado3D(Furgon furgon, List<Nevera> acomodadas) {
        SubScene escena3D = FurgonViewer3D.crearEscena3D(
                furgon, acomodadas, visorContainer.getWidth(), visorContainer.getHeight());

        visorContainer.getChildren().clear();
        visorContainer.getChildren().add(escena3D);

        escena3D.widthProperty().bind(visorContainer.widthProperty());
        escena3D.heightProperty().bind(visorContainer.heightProperty());

        VBox leyenda = construirLeyenda(acomodadas);
        StackPane.setAlignment(leyenda, Pos.TOP_LEFT);
        leyenda.setMouseTransparent(true);

        visorContainer.getChildren().add(leyenda);
    }

    private VBox construirLeyenda(List<Nevera> acomodadas) {
        Map<Integer, Color> mapaColores = FurgonViewer3D.generarMapaColoresParadas(acomodadas);
        VBox leyenda = new VBox(5);
        leyenda.setPadding(new Insets(10));
        leyenda.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-background-radius: 8;");

        Label titulo = new Label("Leyenda (parada → pedido)");
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        leyenda.getChildren().add(titulo);

        List<Integer> paradasOrdenadas = new ArrayList<>(mapaColores.keySet());
        Collections.sort(paradasOrdenadas);

        for (Integer parada : paradasOrdenadas) {
            Color color = mapaColores.get(parada);
            String clienteTexto = pedidos.stream()
                    .filter(p -> p.getOrdenParada() == parada)
                    .map(Pedido::getCliente)
                    .findFirst()
                    .orElse("?");

            Rectangle swatch = new Rectangle(14, 14, color);
            swatch.setStroke(Color.WHITE);

            Label texto = new Label("Parada " + parada + " - " + clienteTexto);
            texto.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            HBox fila = new HBox(8, swatch, texto);
            fila.setAlignment(Pos.CENTER_LEFT);
            leyenda.getChildren().add(fila);
        }

        return leyenda;
    }

    private void actualizarVistaPedidos() {
        Pedido seleccionado = pedidosListView.getSelectionModel().getSelectedItem();
        pedidosListView.getItems().clear();
        pedidosListView.getItems().addAll(pedidos);

        if (seleccionado != null && pedidos.contains(seleccionado)) {
            pedidosListView.getSelectionModel().select(seleccionado);
        } else if (!pedidos.isEmpty()) {
            pedidosListView.getSelectionModel().selectFirst();
        }
    }

    private void actualizarListaNeveras() {
        neverasListView.getItems().clear();
        if (pedidoSeleccionado != null) {
            neverasListView.getItems().addAll(pedidoSeleccionado.getNeveras());
        }
    }

    private List<Nevera> obtenerNeverasDePedidos() {
        List<Nevera> neveras = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            neveras.addAll(pedido.getNeveras());
        }
        return neveras;
    }

    private boolean existeParada(int parada) {
        return pedidos.stream().anyMatch(p -> p.getOrdenParada() == parada);
    }

    private String generarIdNevera() {
        int mayorNumero = 0;
        for (Nevera nevera : obtenerNeverasDePedidos()) {
            String id = nevera.getId();
            if (id.startsWith("N")) {
                try {
                    int numero = Integer.parseInt(id.substring(1));
                    if (numero > mayorNumero)
                        mayorNumero = numero;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "N" + (mayorNumero + 1);
    }

    private ConfiguracionNevera obtenerConfiguracionNevera(Nevera.Tipo tipo) {
        switch (tipo) {
            case CAPACIDAD_56L:
                return new ConfiguracionNevera(20.2, 19.6, 28.2, 45);
            case CAPACIDAD_18L:
                return new ConfiguracionNevera(28.2, 13, 27.5, 25);
            default:
                throw new IllegalArgumentException("Tipo de nevera no configurado");
        }
    }

    private Dialog<ButtonType> crearDialogo(String titulo, String cabecera, VBox contenido) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecera);
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialog;
    }

    private void mostrarInformacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alertError = new Alert(Alert.AlertType.ERROR);
        alertError.setTitle("Error");
        alertError.setHeaderText(null);
        alertError.setContentText(mensaje);
        alertError.showAndWait();
    }
}