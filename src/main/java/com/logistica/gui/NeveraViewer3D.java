package com.logistica.gui;

import com.logistica.dto.ItemMedicamentoDTO;
import com.logistica.model.Medicamento;
import com.logistica.model.Nevera;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.*;

public class NeveraViewer3D {

    private static double mouseOldX;
    private static double mouseOldY;
    private static double zoom = -230;

    private static final Color COLOR_BORDE_NEVERA = Color.web("#00bcd4");
    private static final Color COLOR_PISO_NEVERA = Color.web("#37474f");
    private static final Color COLOR_PISO_CAJA_CONT = Color.web("#8d6e63");
    private static final Color COLOR_BORDE_CAJA_CONT = Color.web("#b0bec5");

    public static final Color[] PALETA_MEDICAMENTOS = {
            Color.web("#2ecc71"), // Verde
            Color.web("#9b59b6"), // Morado
            Color.web("#e67e22"), // Naranja
            Color.web("#3498db"), // Azul
            Color.web("#e74c3c"), // Rojo
            Color.web("#1abc9c"), // Turquesa
            Color.web("#f1c40f"), // Amarillo
            Color.web("#fd79a8") // Rosa
    };

    private static class CajaPos {
        double x, y, z, ancho, alto, largo;

        CajaPos(double x, double y, double z, double ancho, double alto, double largo) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ancho = ancho;
            this.alto = alto;
            this.largo = largo;
        }
    }

    public static SubScene crearVistaEmpaquetadoNevera(Nevera nevera, double width, double height) {
        double ancho = (nevera != null) ? nevera.getAncho() : 51.8;
        double alto = (nevera != null) ? nevera.getAlto() : 64.2;
        double largo = (nevera != null) ? nevera.getLargo() : 65.4;
        Nevera.Tipo tipo = (nevera != null) ? nevera.getTipo() : Nevera.Tipo.CAPACIDAD_56L;
        List<Medicamento> empacados = (nevera != null) ? nevera.getMedicamentosEmpacados() : new ArrayList<>();
        List<ItemMedicamentoDTO> resumenDTO = (nevera != null) ? nevera.getContenidoMedicamentos() : new ArrayList<>();

        return construirEscena3D(ancho, alto, largo, tipo, empacados, resumenDTO, width, height);
    }

    private static SubScene construirEscena3D(
            double ancho, double alto, double largo,
            Nevera.Tipo tipo,
            List<Medicamento> empacados,
            List<ItemMedicamentoDTO> resumenDTO,
            double width, double height) {

        Group root3D = new Group();
        Group modelo = new Group();

        // 1. ILUMINACIÓN (Debe agregarse AL INICIO de root3D antes que los modelos)
        AmbientLight luzAmbiente = new AmbientLight(Color.rgb(240, 240, 240));

        // Luz puntual alineada con la posición de la cámara (Z = -200)
        PointLight luzCamara = new PointLight(Color.WHITE);
        luzCamara.setTranslateX(0);
        luzCamara.setTranslateY(-50);
        luzCamara.setTranslateZ(-200);

        root3D.getChildren().addAll(luzAmbiente, luzCamara);

        // 2. Marco exterior cyan de la Nevera
        crearBordesCaja(modelo, 0, 0, 0, ancho, alto, largo, COLOR_BORDE_NEVERA, 1.8);

        // 3. Piso Nevera
        double grosorSuelo = 3.0;
        Box suelo = new Box(ancho, grosorSuelo, largo);
        suelo.setMaterial(crearMaterialBrillante(COLOR_PISO_NEVERA));
        suelo.setTranslateY(alto / 2.0 + grosorSuelo / 2.0);
        modelo.getChildren().add(suelo);

        // 4. Dibujar contenedores
        List<CajaPos> cajasContenedoras = obtenerCajasContenedoras(ancho, alto, largo, tipo);

        for (CajaPos c : cajasContenedoras) {
            boolean tieneMedicamentos = false;

            if (empacados != null && !empacados.isEmpty()) {
                for (Medicamento m : empacados) {
                    if (Math.abs(m.getPosX() - c.x) <= c.ancho / 2.0 + 1.0 &&
                            Math.abs(m.getPosZ() - c.z) <= c.largo / 2.0 + 1.0) {
                        tieneMedicamentos = true;
                        break;
                    }
                }
            } else {
                tieneMedicamentos = true;
            }

            if (tieneMedicamentos) {
                Box pisoCaja = new Box(c.ancho, 1.0, c.largo);
                pisoCaja.setMaterial(crearMaterialBrillante(COLOR_PISO_CAJA_CONT));
                pisoCaja.setTranslateX(c.x);
                pisoCaja.setTranslateY(c.y + c.alto / 2.0 - 0.5);
                pisoCaja.setTranslateZ(c.z);
                modelo.getChildren().add(pisoCaja);

                crearBordesCaja(modelo, c.x, c.y, c.z, c.ancho, c.alto, c.largo, COLOR_BORDE_CAJA_CONT, 1.0);
            }
        }

        // 5. Renderizado de medicamentos
        Map<String, Color> mapaColoresItems = generarMapaColoresItems(resumenDTO);

        if (empacados != null && !empacados.isEmpty()) {
            for (Medicamento m : empacados) {
                Color colorMed = mapaColoresItems.getOrDefault(m.getIdItem(), Color.DODGERBLUE);

                Box cajaMed = new Box(m.getAncho(), m.getAlto(), m.getLargo());
                cajaMed.setMaterial(crearMaterialBrillante(colorMed));

                cajaMed.setTranslateX(m.getPosX());
                cajaMed.setTranslateY(m.getPosY());
                cajaMed.setTranslateZ(m.getPosZ());

                modelo.getChildren().add(cajaMed);

                crearBordesCaja(modelo, m.getPosX(), m.getPosY(), m.getPosZ(),
                        m.getAncho(), m.getAlto(), m.getLargo(), Color.BLACK, 0.7);
            }
        }

        root3D.getChildren().add(modelo);

        // 6. Configuración de Cámara y Zoom inicial
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        zoom = -160; // Acercamiento para llenar mejor el encuadre
        camera.setTranslateZ(zoom);

        SubScene subScene = new SubScene(root3D, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#1e222b"));
        subScene.setCamera(camera);

        Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
        Rotate rotateX = new Rotate(-15, Rotate.X_AXIS);
        modelo.getTransforms().addAll(rotateY, rotateX);

        subScene.setOnMousePressed((MouseEvent event) -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged((MouseEvent event) -> {
            double deltaX = event.getSceneX() - mouseOldX;
            double deltaY = event.getSceneY() - mouseOldY;
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
            rotateY.setAngle(rotateY.getAngle() + deltaX * 0.5);
            rotateX.setAngle(rotateX.getAngle() - deltaY * 0.5);
        });

        subScene.setOnScroll((ScrollEvent event) -> {
            zoom += event.getDeltaY() * 0.8;
            if (zoom > -80)
                zoom = -80;
            if (zoom < -1000)
                zoom = -1000;
            camera.setTranslateZ(zoom);
        });

        return subScene;
    }

    public static Map<String, Color> generarMapaColoresItems(List<ItemMedicamentoDTO> resumenDTO) {
        Map<String, Color> mapa = new LinkedHashMap<>();
        if (resumenDTO != null) {
            for (int i = 0; i < resumenDTO.size(); i++) {
                mapa.put(resumenDTO.get(i).getMedicamento().getIdItem(),
                        PALETA_MEDICAMENTOS[i % PALETA_MEDICAMENTOS.length]);
            }
        }
        return mapa;
    }

    private static List<CajaPos> obtenerCajasContenedoras(double ancho, double alto, double largo, Nevera.Tipo tipo) {
        List<CajaPos> lista = new ArrayList<>();
        if (tipo == Nevera.Tipo.CAPACIDAD_56L) {
            double cW = 20.2, cH = 19.6, cL = 28.2;
            double posY = (alto / 2.0) - (cH / 2.0) - 1.5;
            double offX = cW / 2.0 + 0.6;
            double offZ = cL / 2.0 + 0.6;

            lista.add(new CajaPos(-offX, posY, -offZ, cW, cH, cL));
            lista.add(new CajaPos(offX, posY, -offZ, cW, cH, cL));
            lista.add(new CajaPos(-offX, posY, offZ, cW, cH, cL));
            lista.add(new CajaPos(offX, posY, offZ, cW, cH, cL));
        } else {
            double cW = 21.0, cH = 13.0, cL = 27.5;
            double posY = (alto / 2.0) - (cH / 2.0) - 1.5;
            lista.add(new CajaPos(0, posY, 0, cW, cH, cL));
        }
        return lista;
    }

    private static PhongMaterial crearMaterialBrillante(Color color) {
        PhongMaterial mat = new PhongMaterial(color);
        mat.setSpecularColor(Color.rgb(100, 100, 100));
        return mat;
    }

    private static void crearBordesCaja(Group grupo, double centroX, double centroY, double centroZ, double ancho,
            double alto, double largo, Color colorBorde, double grosor) {
        double x1 = centroX - ancho / 2.0, x2 = centroX + ancho / 2.0;
        double y1 = centroY - alto / 2.0, y2 = centroY + alto / 2.0;
        double z1 = centroZ - largo / 2.0, z2 = centroZ + largo / 2.0;

        crearLinea(grupo, x1, y2, z1, x2, y2, z1, colorBorde, grosor);
        crearLinea(grupo, x2, y2, z1, x2, y2, z2, colorBorde, grosor);
        crearLinea(grupo, x2, y2, z2, x1, y2, z2, colorBorde, grosor);
        crearLinea(grupo, x1, y2, z2, x1, y2, z1, colorBorde, grosor);

        crearLinea(grupo, x1, y1, z1, x2, y1, z1, colorBorde, grosor);
        crearLinea(grupo, x2, y1, z1, x2, y1, z2, colorBorde, grosor);
        crearLinea(grupo, x2, y1, z2, x1, y1, z2, colorBorde, grosor);
        crearLinea(grupo, x1, y1, z2, x1, y1, z1, colorBorde, grosor);

        crearLinea(grupo, x1, y1, z1, x1, y2, z1, colorBorde, grosor);
        crearLinea(grupo, x2, y1, z1, x2, y2, z1, colorBorde, grosor);
        crearLinea(grupo, x1, y1, z2, x1, y2, z2, colorBorde, grosor);
        crearLinea(grupo, x2, y1, z2, x2, y2, z2, colorBorde, grosor);
    }

    private static void crearLinea(Group grupo, double x1, double y1, double z1, double x2, double y2, double z2,
            Color color, double grosor) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double longitud = Math.sqrt(dx * dx + dy * dy + dz * dz);

        Cylinder linea = new Cylinder(grosor, longitud);
        linea.setMaterial(crearMaterialBrillante(color));

        linea.setTranslateX((x1 + x2) / 2);
        linea.setTranslateY((y1 + y2) / 2);
        linea.setTranslateZ((z1 + z2) / 2);

        if (Math.abs(dx) > 0) {
            linea.setRotationAxis(Rotate.Z_AXIS);
            linea.setRotate(90);
        } else if (Math.abs(dz) > 0) {
            linea.setRotationAxis(Rotate.X_AXIS);
            linea.setRotate(90);
        }

        grupo.getChildren().add(linea);
    }
}