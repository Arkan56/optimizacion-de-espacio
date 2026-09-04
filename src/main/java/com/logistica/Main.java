package com.logistica;

import com.logistica.gui.FurgonViewer3D;
import com.logistica.model.Furgon;
import com.logistica.model.Nevera;
import com.logistica.model.Pedido;
import com.logistica.solver.PackSolverLIFO;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

        private static class ConfiguracionNevera {

                private final double ancho;
                private final double alto;
                private final double largo;
                private final double peso;

                public ConfiguracionNevera(
                                double ancho,
                                double alto,
                                double largo,
                                double peso) {
                        this.ancho = ancho;
                        this.alto = alto;
                        this.largo = largo;
                        this.peso = peso;
                }
        }

        private StackPane visorContainer;

        private TextField anchoField;
        private TextField altoField;
        private TextField largoField;
        private Label volumenUtilizadoLabel;
        private Label neverasTotalesLabel;
        private Label pesoTotalLabel;
        private Label paradasTotalesLabel;
        private Label pesoVolumetricoLabel;

        private Furgon furgonActual;

        private final List<Pedido> pedidos = new ArrayList<>();

        private ListView<Pedido> pedidosListView;
        private ListView<Nevera> neverasListView;

        private Pedido pedidoSeleccionado;

        @Override
        public void start(Stage primaryStage) {

                // ==========================================
                // CAMPOS PARA LAS DIMENSIONES
                // ==========================================

                Label anchoLabel = new Label("Ancho:");
                anchoField = new TextField("200");
                anchoField.setPrefWidth(80);

                Label altoLabel = new Label("Alto:");
                altoField = new TextField("220");
                altoField.setPrefWidth(80);

                Label largoLabel = new Label("Largo:");
                largoField = new TextField("420");
                largoField.setPrefWidth(80);

                Button actualizarButton = new Button("Actualizar furgón");
                Button optimizarButton = new Button("🚀 Optimizar carga");

                volumenUtilizadoLabel = new Label("0%");
                neverasTotalesLabel = new Label("0");
                pesoTotalLabel = new Label("0 kg");
                paradasTotalesLabel = new Label("0");
                pesoVolumetricoLabel = new Label("0 kg");

                VBox volumenIndicador = crearIndicador(
                                "📦 Volumen utilizado",
                                volumenUtilizadoLabel);

                VBox neverasIndicador = crearIndicador(
                                "🧊 Neveras totales",
                                neverasTotalesLabel);

                VBox pesoIndicador = crearIndicador(
                                "⚖ Peso total",
                                pesoTotalLabel);

                VBox paradasIndicador = crearIndicador(
                                "📍 Paradas totales",
                                paradasTotalesLabel);

                VBox pesoVolumetricoIndicador = crearIndicador(
                                "📐 Peso volumétrico",
                                pesoVolumetricoLabel);

                HBox indicadores = new HBox(
                                30,
                                volumenIndicador,
                                neverasIndicador,
                                pesoIndicador,
                                paradasIndicador,
                                pesoVolumetricoIndicador);

                indicadores.setAlignment(Pos.CENTER);
                indicadores.setPadding(new Insets(10));

                // ==========================================
                // EVENTO DEL BOTÓN
                // ==========================================

                actualizarButton.setOnAction(
                                event -> actualizarFurgon());

                optimizarButton.setOnAction(
                                event -> optimizarCarga());

                // ==========================================
                // BARRA SUPERIOR
                // ==========================================

                HBox controles = new HBox(
                                10,
                                anchoLabel,
                                anchoField,
                                altoLabel,
                                altoField,
                                largoLabel,
                                largoField,
                                actualizarButton,
                                optimizarButton);

                controles.setAlignment(Pos.CENTER);
                controles.setPadding(new Insets(15));

                // ==========================================
                // CONTENEDOR DEL VISOR 3D
                // ==========================================

                visorContainer = new StackPane();

                pedidosListView = new ListView<>();

                Button nuevoPedidoButton = new Button("+ Nuevo pedido");
                Button editarPedidoButton = new Button("✏ Editar pedido");

                Button eliminarPedidoButton = new Button("🗑 Eliminar pedido");

                nuevoPedidoButton.setMaxWidth(Double.MAX_VALUE);
                nuevoPedidoButton.setMaxWidth(Double.MAX_VALUE);
                editarPedidoButton.setMaxWidth(Double.MAX_VALUE);
                eliminarPedidoButton.setMaxWidth(Double.MAX_VALUE);

                nuevoPedidoButton.setOnAction(event -> {

                        crearPedido();
                });

                editarPedidoButton.setOnAction(event -> {
                        editarPedido();
                });

                eliminarPedidoButton.setOnAction(event -> {
                        eliminarPedido();
                });

                Label pedidosTitulo = new Label("PEDIDOS");

                VBox pedidosPanel = new VBox(
                                10,
                                pedidosTitulo,
                                pedidosListView,
                                nuevoPedidoButton,
                                editarPedidoButton,
                                eliminarPedidoButton);

                pedidosPanel.setPadding(new Insets(10));
                pedidosPanel.setPrefWidth(250);

                pedidosListView
                                .getSelectionModel()
                                .selectedItemProperty()
                                .addListener((observable, pedidoAnterior, pedidoNuevo) -> {

                                        pedidoSeleccionado = pedidoNuevo;

                                        actualizarListaNeveras();
                                });

                neverasListView = new ListView<>();

                Button agregarNeveraButton = new Button("+ Agregar nevera");
                Button editarNeveraButton = new Button("✏ Editar nevera");

                Button eliminarNeveraButton = new Button("🗑 Eliminar nevera");

                agregarNeveraButton.setMaxWidth(Double.MAX_VALUE);
                agregarNeveraButton.setMaxWidth(Double.MAX_VALUE);
                editarNeveraButton.setMaxWidth(Double.MAX_VALUE);
                eliminarNeveraButton.setMaxWidth(Double.MAX_VALUE);

                agregarNeveraButton.setOnAction(event -> {

                        agregarNevera();
                });
                editarNeveraButton.setOnAction(event -> {
                        editarNevera();
                });

                eliminarNeveraButton.setOnAction(event -> {
                        eliminarNevera();
                });

                Label neverasTitulo = new Label("NEVERAS DEL PEDIDO");

                VBox neverasPanel = new VBox(
                                10,
                                neverasTitulo,
                                neverasListView,
                                agregarNeveraButton,
                                editarNeveraButton,
                                eliminarNeveraButton);

                neverasPanel.setPadding(new Insets(10));
                neverasPanel.setPrefWidth(300);

                SplitPane splitPane = new SplitPane();

                splitPane.getItems().addAll(
                                pedidosPanel,
                                neverasPanel,
                                visorContainer);

                splitPane.setDividerPositions(
                                0.25,
                                0.50);

                // ==========================================
                // LAYOUT PRINCIPAL
                // ==========================================

                VBox parteSuperior = new VBox(
                                controles,
                                indicadores);

                BorderPane root = new BorderPane();

                root.setTop(parteSuperior);
                root.setCenter(splitPane);

                Scene scene = new Scene(root, 1000, 700);

                primaryStage.setTitle("Optimizador de Carga 3D - Furgón");
                primaryStage.setScene(scene);

                // Crear el furgón inicial
                crearPedidosPrueba();
                actualizarFurgon();
                actualizarIndicadores();

                primaryStage.show();
        }

        private VBox crearIndicador(
                        String titulo,
                        Label valorLabel) {

                Label tituloLabel = new Label(titulo);

                tituloLabel.setStyle(
                                "-fx-font-size: 12px;"
                                                + "-fx-text-fill: #666666;");

                valorLabel.setStyle(
                                "-fx-font-size: 18px;"
                                                + "-fx-font-weight: bold;");

                VBox indicador = new VBox(
                                5,
                                tituloLabel,
                                valorLabel);

                indicador.setAlignment(Pos.CENTER);

                indicador.setPadding(new Insets(10));

                return indicador;
        }

        private void actualizarIndicadores() {

                // ==========================================
                // OBTENER TODAS LAS NEVERAS
                // ==========================================

                List<Nevera> neveras = obtenerNeverasDePedidos();

                // ==========================================
                // NEVERAS TOTALES
                // ==========================================

                int totalNeveras = neveras.size();

                neverasTotalesLabel.setText(
                                String.valueOf(totalNeveras));

                // ==========================================
                // PESO TOTAL
                // ==========================================

                double pesoTotal = 0;

                for (Nevera nevera : neveras) {

                        pesoTotal += nevera.getPesoKg();
                }

                pesoTotalLabel.setText(
                                String.format("%.2f kg", pesoTotal));

                // ==========================================
                // PARADAS TOTALES
                // ==========================================

                int totalParadas = pedidos.size();

                paradasTotalesLabel.setText(
                                String.valueOf(totalParadas));

                // ==========================================
                // PESO VOLUMÉTRICO
                // Fórmula: Volumen (m³) × 400
                // ==========================================

                double pesoVolumetricoTotal = 0;

                final double FACTOR_PESO_VOLUMETRICO = 400.0;

                for (Nevera nevera : neveras) {

                        // Convertir centímetros a metros
                        double largoMetros = nevera.getLargo() / 100.0;
                        double anchoMetros = nevera.getAncho() / 100.0;
                        double altoMetros = nevera.getAlto() / 100.0;

                        // Calcular volumen en m³
                        double volumen = largoMetros
                                        * anchoMetros
                                        * altoMetros;

                        // Peso volumétrico = Volumen × 400
                        pesoVolumetricoTotal += volumen * FACTOR_PESO_VOLUMETRICO;
                }

                pesoVolumetricoLabel.setText(
                                String.format("%.2f kg", pesoVolumetricoTotal));

                // ==========================================
                // VOLUMEN UTILIZADO
                // ==========================================

                if (furgonActual != null) {

                        double volumenFurgon = furgonActual.getLargo()
                                        * furgonActual.getAncho()
                                        * furgonActual.getAlto();

                        double volumenNeveras = 0;

                        for (Nevera nevera : neveras) {

                                volumenNeveras += nevera.getLargo()
                                                * nevera.getAncho()
                                                * nevera.getAlto();
                        }

                        double porcentaje = (volumenNeveras / volumenFurgon) * 100;

                        volumenUtilizadoLabel.setText(
                                        String.format("%.2f %%", porcentaje));

                } else {

                        volumenUtilizadoLabel.setText("0%");
                }
        }

        // ==========================================
        // ACTUALIZAR FURGÓN
        // ==========================================

        private void actualizarFurgon() {

                try {

                        double ancho = Double.parseDouble(anchoField.getText());

                        double alto = Double.parseDouble(altoField.getText());

                        double largo = Double.parseDouble(largoField.getText());

                        // ==========================================
                        // VALIDACIÓN
                        // ==========================================

                        if (ancho <= 0 || alto <= 0 || largo <= 0) {

                                mostrarError(
                                                "Las dimensiones deben ser mayores que cero.");

                                return;
                        }

                        // ==========================================
                        // CREAR FURGÓN
                        // ==========================================

                        furgonActual = new Furgon(
                                        largo,
                                        ancho,
                                        alto);

                        actualizarIndicadores();

                        mostrarInformacion(
                                        "Furgón actualizado correctamente.");

                } catch (NumberFormatException e) {

                        mostrarError(
                                        "Por favor ingresa valores numéricos válidos.");
                }
        }

        private void optimizarCarga() {

                // ==========================================
                // VALIDAR FURGÓN
                // ==========================================

                if (furgonActual == null) {

                        mostrarError(
                                        "Primero debes configurar el furgón.");

                        return;
                }

                // ==========================================
                // OBTENER NEVERAS
                // ==========================================

                List<Nevera> neveras = obtenerNeverasDePedidos();

                if (neveras.isEmpty()) {

                        mostrarError(
                                        "No hay neveras para optimizar.");

                        return;
                }

                // ==========================================
                // EJECUTAR ALGORITMO LIFO
                // ==========================================

                List<Nevera> acomodadas = PackSolverLIFO.resolverAcomodo(
                                furgonActual,
                                neveras);

                // ==========================================
                // MOSTRAR ESCENA
                // ==========================================

                mostrarResultado3D(
                                furgonActual,
                                acomodadas);

                mostrarInformacion(
                                "Optimización completada.\n\n"
                                                + "Neveras solicitadas: "
                                                + neveras.size()
                                                + "\n"
                                                + "Neveras acomodadas: "
                                                + acomodadas.size());
        }

        private void mostrarResultado3D(
                        Furgon furgon,
                        List<Nevera> acomodadas) {

                SubScene escena3D = FurgonViewer3D.crearEscena3D(
                                furgon,
                                acomodadas,
                                visorContainer.getWidth(),
                                visorContainer.getHeight());

                // Limpiar visor anterior
                visorContainer.getChildren().clear();

                // Agregar nueva escena
                visorContainer.getChildren().add(
                                escena3D);

                // Hacer responsive
                escena3D.widthProperty()
                                .bind(
                                                visorContainer.widthProperty());

                escena3D.heightProperty()
                                .bind(
                                                visorContainer.heightProperty());
        }

        private void mostrarInformacion(String mensaje) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);

                alert.setTitle("Información");

                alert.setHeaderText(null);

                alert.setContentText(mensaje);

                alert.showAndWait();
        }

        // ==========================================
        // NEVERAS DE PRUEBA
        // ==========================================

        private void crearPedidosPrueba() {

                // ==========================================
                // PEDIDO 1
                // ==========================================

                Pedido pedido1 = new Pedido(
                                "P001",
                                "Cliente A",
                                3);

                Nevera.Tipo tipoPedido1 = Nevera.Tipo.CAPACIDAD_56L;

                ConfiguracionNevera configPedido1 = obtenerConfiguracionNevera(tipoPedido1);

                pedido1.agregarNevera(
                                new Nevera(
                                                "N1",
                                                tipoPedido1,
                                                configPedido1.largo,
                                                configPedido1.ancho,
                                                configPedido1.alto,
                                                configPedido1.peso,
                                                pedido1.getOrdenParada()));

                // ==========================================
                // PEDIDO 2
                // ==========================================

                Pedido pedido2 = new Pedido(
                                "P002",
                                "Cliente B",
                                2);

                Nevera.Tipo tipoPedido2 = Nevera.Tipo.CAPACIDAD_18L;

                ConfiguracionNevera configPedido2 = obtenerConfiguracionNevera(tipoPedido2);

                pedido2.agregarNevera(
                                new Nevera(
                                                "N2",
                                                tipoPedido2,
                                                configPedido2.largo,
                                                configPedido2.ancho,
                                                configPedido2.alto,
                                                configPedido2.peso,
                                                pedido2.getOrdenParada()));

                // ==========================================
                // PEDIDO 3
                // ==========================================

                Pedido pedido3 = new Pedido(
                                "P003",
                                "Cliente C",
                                1);

                // Nevera N3 - L18

                Nevera.Tipo tipoN3 = Nevera.Tipo.CAPACIDAD_18L;

                ConfiguracionNevera configN3 = obtenerConfiguracionNevera(tipoN3);

                pedido3.agregarNevera(
                                new Nevera(
                                                "N3",
                                                tipoN3,
                                                configN3.largo,
                                                configN3.ancho,
                                                configN3.alto,
                                                configN3.peso,
                                                pedido3.getOrdenParada()));

                // Nevera N4 - L56

                Nevera.Tipo tipoN4 = Nevera.Tipo.CAPACIDAD_56L;

                ConfiguracionNevera configN4 = obtenerConfiguracionNevera(tipoN4);

                pedido3.agregarNevera(
                                new Nevera(
                                                "N4",
                                                tipoN4,
                                                configN4.largo,
                                                configN4.ancho,
                                                configN4.alto,
                                                configN4.peso,
                                                pedido3.getOrdenParada()));

                // ==========================================
                // AGREGAR PEDIDOS
                // ==========================================

                pedidos.add(pedido1);
                pedidos.add(pedido2);
                pedidos.add(pedido3);

                pedidosListView.getItems().addAll(pedidos);
        }

        private void editarPedido() {

                Pedido pedido = pedidosListView
                                .getSelectionModel()
                                .getSelectedItem();

                if (pedido == null) {

                        mostrarError(
                                        "Debes seleccionar un pedido para editar.");

                        return;
                }

                // ==========================================
                // CAMPOS
                // ==========================================

                TextField clienteField = new TextField(
                                pedido.getCliente());

                TextField paradaField = new TextField(
                                String.valueOf(
                                                pedido.getOrdenParada()));

                VBox contenido = new VBox(
                                10,
                                new Label("Cliente:"),
                                clienteField,
                                new Label("Número de parada:"),
                                paradaField);

                contenido.setPadding(
                                new Insets(15));

                // ==========================================
                // DIÁLOGO
                // ==========================================

                Dialog<ButtonType> dialog = new Dialog<>();

                dialog.setTitle("Editar pedido");

                dialog.setHeaderText(
                                "Editar " + pedido.getId());

                dialog.getDialogPane()
                                .setContent(contenido);

                dialog.getDialogPane()
                                .getButtonTypes()
                                .addAll(
                                                ButtonType.OK,
                                                ButtonType.CANCEL);

                dialog.showAndWait().ifPresent(resultado -> {

                        if (resultado == ButtonType.OK) {

                                try {

                                        String cliente = clienteField.getText().trim();

                                        int nuevaParada = Integer.parseInt(
                                                        paradaField.getText().trim());

                                        if (cliente.isEmpty()) {

                                                mostrarError(
                                                                "El cliente no puede estar vacío.");

                                                return;
                                        }

                                        if (nuevaParada <= 0) {

                                                mostrarError(
                                                                "La parada debe ser mayor que cero.");

                                                return;
                                        }

                                        // ==========================================
                                        // VALIDAR PARADA DUPLICADA
                                        // ==========================================

                                        for (Pedido otroPedido : pedidos) {

                                                if (otroPedido != pedido
                                                                && otroPedido.getOrdenParada() == nuevaParada) {

                                                        mostrarError(
                                                                        "Ya existe otro pedido en la parada "
                                                                                        + nuevaParada);

                                                        return;
                                                }
                                        }

                                        // ==========================================
                                        // ACTUALIZAR PEDIDO
                                        // ==========================================

                                        pedido.setCliente(cliente);

                                        pedido.setOrdenParada(
                                                        nuevaParada);

                                        // ==========================================
                                        // ACTUALIZAR PARADA DE SUS NEVERAS
                                        // ==========================================

                                        for (Nevera nevera : pedido.getNeveras()) {

                                                nevera.setOrdenParada(
                                                                nuevaParada);
                                        }

                                        actualizarVistaPedidos();

                                        actualizarListaNeveras();

                                } catch (NumberFormatException e) {

                                        mostrarError(
                                                        "La parada debe ser un número válido.");
                                }
                        }
                });
        }

        private void actualizarVistaPedidos() {

                Pedido seleccionado = pedidosListView
                                .getSelectionModel()
                                .getSelectedItem();

                pedidosListView.getItems().clear();

                pedidosListView.getItems().addAll(pedidos);

                if (seleccionado != null && pedidos.contains(seleccionado)) {

                        pedidosListView
                                        .getSelectionModel()
                                        .select(seleccionado);

                } else if (!pedidos.isEmpty()) {

                        pedidosListView
                                        .getSelectionModel()
                                        .selectFirst();
                }
        }

        private void eliminarPedido() {

                Pedido pedido = pedidosListView
                                .getSelectionModel()
                                .getSelectedItem();

                if (pedido == null) {

                        mostrarError(
                                        "Debes seleccionar un pedido para eliminar.");

                        return;
                }

                Alert confirmacion = new Alert(
                                Alert.AlertType.CONFIRMATION);

                confirmacion.setTitle(
                                "Eliminar pedido");

                confirmacion.setHeaderText(
                                "¿Eliminar " + pedido.getId() + "?");

                confirmacion.setContentText(
                                "También se eliminarán todas las neveras "
                                                + "asociadas a este pedido.");

                confirmacion.showAndWait()
                                .ifPresent(resultado -> {

                                        if (resultado == ButtonType.OK) {

                                                // Eliminar de la lista principal
                                                pedidos.remove(pedido);

                                                // Limpiar la selección actual
                                                pedidosListView
                                                                .getSelectionModel()
                                                                .clearSelection();

                                                // Actualizar la vista completa
                                                actualizarVistaPedidos();

                                                // Limpiar selección anterior
                                                pedidoSeleccionado = null;

                                                // Limpiar lista de neveras
                                                neverasListView
                                                                .getItems()
                                                                .clear();

                                                // Si todavía quedan pedidos,
                                                // seleccionar automáticamente el primero
                                                if (!pedidos.isEmpty()) {

                                                        pedidosListView
                                                                        .getSelectionModel()
                                                                        .selectFirst();

                                                        pedidoSeleccionado = pedidosListView
                                                                        .getSelectionModel()
                                                                        .getSelectedItem();

                                                        actualizarListaNeveras();
                                                }

                                                actualizarIndicadores();
                                        }
                                });
        }

        // ==========================================
        // MOSTRAR ERROR
        // ==========================================

        private void mostrarError(String mensaje) {

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(mensaje);

                alert.showAndWait();
        }

        public static void main(String[] args) {
                launch(args);
        }

        private List<Nevera> obtenerNeverasDePedidos() {

                List<Nevera> neveras = new ArrayList<>();

                for (Pedido pedido : pedidos) {

                        neveras.addAll(
                                        pedido.getNeveras());
                }

                return neveras;
        }

        private void actualizarListaNeveras() {

                neverasListView.getItems().clear();

                if (pedidoSeleccionado != null) {

                        neverasListView.getItems().addAll(
                                        pedidoSeleccionado.getNeveras());
                }
        }

        private void editarNevera() {

                if (pedidoSeleccionado == null) {

                        mostrarError(
                                        "Primero debes seleccionar un pedido.");

                        return;
                }

                Nevera nevera = neverasListView
                                .getSelectionModel()
                                .getSelectedItem();

                if (nevera == null) {

                        mostrarError(
                                        "Debes seleccionar una nevera para editar.");

                        return;
                }

                // ==========================================
                // CAMPOS
                // ==========================================

                TextField idField = new TextField(
                                nevera.getId());

                ComboBox<Nevera.Tipo> tipoComboBox = new ComboBox<>();

                tipoComboBox.getItems().addAll(
                                Nevera.Tipo.values());

                tipoComboBox.setValue(
                                nevera.getTipo());

                TextField largoField = new TextField();

                TextField anchoField = new TextField();

                TextField altoField = new TextField();

                TextField pesoField = new TextField();

                largoField.setEditable(false);
                anchoField.setEditable(false);
                altoField.setEditable(false);
                pesoField.setEditable(false);

                // ==========================================
                // FUNCIÓN DE ACTUALIZACIÓN
                // ==========================================

                tipoComboBox.setOnAction(event -> {

                        ConfiguracionNevera config = obtenerConfiguracionNevera(
                                        tipoComboBox.getValue());

                        largoField.setText(
                                        String.valueOf(config.largo));

                        anchoField.setText(
                                        String.valueOf(config.ancho));

                        altoField.setText(
                                        String.valueOf(config.alto));

                        pesoField.setText(
                                        String.valueOf(config.peso));
                });

                // Valores iniciales

                ConfiguracionNevera config = obtenerConfiguracionNevera(
                                nevera.getTipo());

                largoField.setText(
                                String.valueOf(config.largo));

                anchoField.setText(
                                String.valueOf(config.ancho));

                altoField.setText(
                                String.valueOf(config.alto));

                pesoField.setText(
                                String.valueOf(config.peso));

                // ==========================================
                // FORMULARIO
                // ==========================================

                VBox contenido = new VBox(
                                10,
                                new Label("ID:"),
                                idField,

                                new Label("Tipo:"),
                                tipoComboBox,

                                new Label("Largo:"),
                                largoField,

                                new Label("Ancho:"),
                                anchoField,

                                new Label("Alto:"),
                                altoField,

                                new Label("Peso:"),
                                pesoField);

                contenido.setPadding(
                                new Insets(15));

                // ==========================================
                // DIÁLOGO
                // ==========================================

                Dialog<ButtonType> dialog = new Dialog<>();

                dialog.setTitle(
                                "Editar nevera");

                dialog.setHeaderText(
                                "Editar " + nevera.getId());

                dialog.getDialogPane()
                                .setContent(contenido);

                dialog.getDialogPane()
                                .getButtonTypes()
                                .addAll(
                                                ButtonType.OK,
                                                ButtonType.CANCEL);

                dialog.showAndWait().ifPresent(resultado -> {

                        if (resultado == ButtonType.OK) {

                                String nuevoId = idField.getText().trim();

                                if (nuevoId.isEmpty()) {

                                        mostrarError(
                                                        "El ID no puede estar vacío.");

                                        return;
                                }

                                Nevera.Tipo nuevoTipo = tipoComboBox.getValue();

                                ConfiguracionNevera nuevaConfig = obtenerConfiguracionNevera(
                                                nuevoTipo);

                                // Actualizar datos

                                nevera.setId(nuevoId);

                                nevera.setTipo(nuevoTipo);

                                nevera.setLargo(
                                                nuevaConfig.largo);

                                nevera.setAncho(
                                                nuevaConfig.ancho);

                                nevera.setAlto(
                                                nuevaConfig.alto);

                                nevera.setPesoKg(
                                                nuevaConfig.peso);

                                actualizarListaNeveras();
                                actualizarIndicadores();
                        }
                });
        }

        private void eliminarNevera() {

                if (pedidoSeleccionado == null) {

                        mostrarError(
                                        "Primero debes seleccionar un pedido.");

                        return;
                }

                Nevera nevera = neverasListView
                                .getSelectionModel()
                                .getSelectedItem();

                if (nevera == null) {

                        mostrarError(
                                        "Debes seleccionar una nevera para eliminar.");

                        return;
                }

                Alert confirmacion = new Alert(
                                Alert.AlertType.CONFIRMATION);

                confirmacion.setTitle(
                                "Eliminar nevera");

                confirmacion.setHeaderText(
                                "¿Eliminar " + nevera.getId() + "?");

                confirmacion.setContentText(
                                "La nevera será eliminada del pedido "
                                                + pedidoSeleccionado.getId());

                confirmacion.showAndWait()
                                .ifPresent(resultado -> {

                                        if (resultado == ButtonType.OK) {

                                                pedidoSeleccionado
                                                                .getNeveras()
                                                                .remove(nevera);

                                                actualizarListaNeveras();
                                                actualizarIndicadores();
                                        }
                                });
        }

        private void crearPedido() {

                // ==========================================
                // CAMPOS DEL FORMULARIO
                // ==========================================

                TextField clienteField = new TextField();

                TextField paradaField = new TextField();

                // ==========================================
                // CONTENEDOR
                // ==========================================

                VBox contenido = new VBox(10);

                contenido.setPadding(new Insets(15));

                contenido.getChildren().addAll(

                                new Label("Nombre del cliente:"),
                                clienteField,

                                new Label("Número de parada:"),
                                paradaField);

                // ==========================================
                // CREAR DIÁLOGO
                // ==========================================

                Dialog<ButtonType> dialog = new Dialog<>();

                dialog.setTitle("Nuevo pedido");

                dialog.setHeaderText(
                                "Crear nuevo pedido");

                dialog.getDialogPane()
                                .setContent(contenido);

                dialog.getDialogPane()
                                .getButtonTypes()
                                .addAll(
                                                ButtonType.OK,
                                                ButtonType.CANCEL);

                // ==========================================
                // RESULTADO
                // ==========================================

                dialog.showAndWait().ifPresent(resultado -> {

                        if (resultado == ButtonType.OK) {

                                try {

                                        String cliente = clienteField.getText().trim();

                                        String paradaTexto = paradaField.getText().trim();

                                        // ==========================================
                                        // VALIDAR CLIENTE
                                        // ==========================================

                                        if (cliente.isEmpty()) {

                                                mostrarError(
                                                                "Debes ingresar el nombre del cliente.");

                                                return;
                                        }

                                        // ==========================================
                                        // VALIDAR PARADA
                                        // ==========================================

                                        if (paradaTexto.isEmpty()) {

                                                mostrarError(
                                                                "Debes ingresar el número de parada.");

                                                return;
                                        }

                                        int parada = Integer.parseInt(paradaTexto);

                                        if (parada <= 0) {

                                                mostrarError(
                                                                "El número de parada debe ser mayor que cero.");

                                                return;
                                        }

                                        // ==========================================
                                        // VALIDAR PARADA DUPLICADA
                                        // ==========================================

                                        if (existeParada(parada)) {

                                                mostrarError(
                                                                "Ya existe un pedido asignado a la parada "
                                                                                + parada);

                                                return;
                                        }

                                        // ==========================================
                                        // GENERAR ID
                                        // ==========================================

                                        String id = "P"
                                                        + String.format(
                                                                        "%03d",
                                                                        pedidos.size() + 1);

                                        // ==========================================
                                        // CREAR PEDIDO
                                        // ==========================================

                                        Pedido pedido = new Pedido(
                                                        id,
                                                        cliente,
                                                        parada);

                                        pedidos.add(pedido);

                                        pedidosListView
                                                        .getItems()
                                                        .add(pedido);

                                        // Seleccionar automáticamente
                                        pedidosListView
                                                        .getSelectionModel()
                                                        .select(pedido);
                                        actualizarIndicadores();

                                } catch (NumberFormatException e) {

                                        mostrarError(
                                                        "El número de parada debe ser un número entero válido.");
                                }
                        }
                });
        }

        private boolean existeParada(int parada) {

                for (Pedido pedido : pedidos) {

                        if (pedido.getOrdenParada() == parada) {

                                return true;
                        }
                }

                return false;
        }

        private void agregarNevera() {

                // ==========================================
                // VALIDAR PEDIDO SELECCIONADO
                // ==========================================

                if (pedidoSeleccionado == null) {

                        mostrarError(
                                        "Primero debes seleccionar un pedido.");

                        return;
                }

                // ==========================================
                // CAMPOS DEL FORMULARIO
                // ==========================================

                ComboBox<Nevera.Tipo> tipoComboBox = new ComboBox<>();

                tipoComboBox.getItems().addAll(
                                Nevera.Tipo.values());

                TextField cantidadField = new TextField("1");

                // Campos automáticos

                TextField anchoNeveraField = new TextField();
                TextField altoNeveraField = new TextField();
                TextField largoNeveraField = new TextField();
                TextField pesoField = new TextField();

                // No permitir modificar manualmente

                anchoNeveraField.setEditable(false);
                altoNeveraField.setEditable(false);
                largoNeveraField.setEditable(false);
                pesoField.setEditable(false);

                // ==========================================
                // ACTUALIZAR CONFIGURACIÓN SEGÚN EL TIPO
                // ==========================================

                tipoComboBox.setOnAction(event -> {

                        Nevera.Tipo tipo = tipoComboBox.getValue();

                        if (tipo != null) {

                                ConfiguracionNevera config = obtenerConfiguracionNevera(tipo);

                                anchoNeveraField.setText(
                                                String.valueOf(config.ancho));

                                altoNeveraField.setText(
                                                String.valueOf(config.alto));

                                largoNeveraField.setText(
                                                String.valueOf(config.largo));

                                pesoField.setText(
                                                String.valueOf(config.peso));
                        }
                });

                // ==========================================
                // SELECCIONAR PRIMER TIPO
                // ==========================================

                tipoComboBox.getSelectionModel().selectFirst();

                Nevera.Tipo tipoInicial = tipoComboBox.getValue();

                ConfiguracionNevera configInicial = obtenerConfiguracionNevera(tipoInicial);

                anchoNeveraField.setText(
                                String.valueOf(configInicial.ancho));

                altoNeveraField.setText(
                                String.valueOf(configInicial.alto));

                largoNeveraField.setText(
                                String.valueOf(configInicial.largo));

                pesoField.setText(
                                String.valueOf(configInicial.peso));

                // ==========================================
                // FORMULARIO
                // ==========================================

                VBox contenido = new VBox(10);

                contenido.setPadding(new Insets(15));

                contenido.getChildren().addAll(

                                new Label("Tipo:"),
                                tipoComboBox,

                                new Label("Cantidad:"),
                                cantidadField,

                                new Label("Ancho:"),
                                anchoNeveraField,

                                new Label("Alto:"),
                                altoNeveraField,

                                new Label("Largo:"),
                                largoNeveraField,

                                new Label("Peso (kg):"),
                                pesoField);

                // ==========================================
                // CREAR DIÁLOGO
                // ==========================================

                Dialog<ButtonType> dialog = new Dialog<>();

                dialog.setTitle("Agregar neveras");

                dialog.setHeaderText(
                                "Agregar neveras al pedido "
                                                + pedidoSeleccionado.getId());

                dialog.getDialogPane()
                                .setContent(contenido);

                dialog.getDialogPane()
                                .getButtonTypes()
                                .addAll(
                                                ButtonType.OK,
                                                ButtonType.CANCEL);

                // ==========================================
                // RESULTADO
                // ==========================================

                dialog.showAndWait().ifPresent(resultado -> {

                        if (resultado == ButtonType.OK) {

                                try {

                                        // ==========================================
                                        // VALIDAR CANTIDAD
                                        // ==========================================

                                        int cantidad = Integer.parseInt(
                                                        cantidadField.getText().trim());

                                        if (cantidad <= 0) {

                                                mostrarError(
                                                                "La cantidad debe ser mayor que cero.");

                                                return;
                                        }

                                        Nevera.Tipo tipo = tipoComboBox.getValue();

                                        ConfiguracionNevera config = obtenerConfiguracionNevera(tipo);

                                        // ==========================================
                                        // CREAR LAS NEVERAS
                                        // ==========================================

                                        for (int i = 0; i < cantidad; i++) {

                                                String id = generarIdNevera();

                                                Nevera nevera = new Nevera(

                                                                id,
                                                                tipo,

                                                                config.largo,
                                                                config.ancho,
                                                                config.alto,
                                                                config.peso,

                                                                pedidoSeleccionado.getOrdenParada());

                                                pedidoSeleccionado.agregarNevera(
                                                                nevera);
                                        }

                                        // ==========================================
                                        // ACTUALIZAR INTERFAZ
                                        // ==========================================

                                        actualizarListaNeveras();

                                        actualizarIndicadores();

                                        mostrarInformacion(
                                                        cantidad
                                                                        + " nevera(s) agregada(s) correctamente.");

                                } catch (NumberFormatException e) {

                                        mostrarError(
                                                        "La cantidad debe ser un número entero válido.");
                                }
                        }
                });
        }

        private String generarIdNevera() {

                int mayorNumero = 0;

                List<Nevera> todasLasNeveras = obtenerNeverasDePedidos();

                for (Nevera nevera : todasLasNeveras) {

                        String id = nevera.getId();

                        if (id.startsWith("N")) {

                                try {

                                        int numero = Integer.parseInt(
                                                        id.substring(1));

                                        if (numero > mayorNumero) {

                                                mayorNumero = numero;
                                        }

                                } catch (NumberFormatException e) {

                                        // Ignorar IDs que no tengan formato N1, N2, etc.
                                }
                        }
                }

                return "N" + (mayorNumero + 1);
        }

        private ConfiguracionNevera obtenerConfiguracionNevera(
                        Nevera.Tipo tipo) {

                switch (tipo) {

                        case CAPACIDAD_56L:
                                return new ConfiguracionNevera(
                                                20.2,
                                                19.6,
                                                28.2,
                                                45);

                        case CAPACIDAD_18L:
                                return new ConfiguracionNevera(
                                                28.2,
                                                13,
                                                27.5,
                                                25);

                        default:
                                throw new IllegalArgumentException(
                                                "Tipo de nevera no configurado");
                }
        }

}
