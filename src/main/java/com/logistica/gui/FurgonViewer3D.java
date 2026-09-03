package com.logistica.gui;

import com.logistica.model.Furgon;
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

import java.util.List;

public class FurgonViewer3D {

    private static double mouseOldX;
    private static double mouseOldY;

    private static double zoom = -1000;

    public static SubScene crearEscena3D(
            Furgon furgon,
            List<Nevera> neverasCargadas,
            double width,
            double height) {

        Group root3D = new Group();
        Group modelo = new Group();

        // =====================================================
        // DIMENSIONES
        // =====================================================

        double ancho = furgon.getAncho();
        double alto = furgon.getAlto();
        double largo = furgon.getLargo();

        double centroX = ancho / 2.0;
        double centroY = alto / 2.0;
        double centroZ = largo / 2.0;

        // =====================================================
        // 1. CREAR ESTRUCTURA DEL FURGÓN
        // =====================================================

        crearEstructuraFurgon(modelo, ancho, alto, largo);

        // =====================================================
        // SUELO DEL FURGÓN
        // =====================================================

        double grosorSuelo = 5;

        Box suelo = new Box(ancho, grosorSuelo, largo);

        PhongMaterial materialSuelo =
                new PhongMaterial(Color.rgb(90, 90, 90));

        suelo.setMaterial(materialSuelo);

        // El suelo queda justo debajo del espacio interior
        suelo.setTranslateY(alto / 2.0 + grosorSuelo / 2.0);

        modelo.getChildren().add(suelo);

        // =====================================================
        // 2. CREAR NEVERAS
        // =====================================================

        for (Nevera n : neverasCargadas) {

            Box cajaNevera = new Box(
                    n.getAncho(),
                    n.getAlto(),
                    n.getLargo()
            );

            Color colorParada = getColorPorParada(n.getOrdenParada());

            cajaNevera.setMaterial(
                    new PhongMaterial(colorParada)
            );

            // Convertir coordenadas al sistema centrado
            // X: posición horizontal
            double x = n.getPosX()
                    + n.getAncho() / 2.0
                    - centroX;

            // Y: invertimos el eje porque en JavaFX
            // Y positivo va hacia abajo
            double y = centroY
                    - n.getPosY()
                    - n.getAlto() / 2.0;

            // Z: profundidad
            double z = n.getPosZ()
                    + n.getLargo() / 2.0
                    - centroZ;

            cajaNevera.setTranslateX(x);
            cajaNevera.setTranslateY(y);
            cajaNevera.setTranslateZ(z);

            modelo.getChildren().add(cajaNevera);

            crearBordesCaja(
                    modelo,
                    x,
                    y,
                    z,
                    n.getAncho(),
                    n.getAlto(),
                    n.getLargo()
            );
        }

        root3D.getChildren().add(modelo);

        // =====================================================
        // 3. ILUMINACIÓN
        // =====================================================

        AmbientLight luzAmbiente = new AmbientLight(Color.WHITE);

        PointLight luzPrincipal = new PointLight(Color.WHITE);
        luzPrincipal.setTranslateX(-500);
        luzPrincipal.setTranslateY(-500);
        luzPrincipal.setTranslateZ(-500);

        root3D.getChildren().addAll(
                luzAmbiente,
                luzPrincipal
        );

        // =====================================================
        // 4. CÁMARA
        // =====================================================

        PerspectiveCamera camera = new PerspectiveCamera(true);

        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        camera.setTranslateZ(zoom);

        // =====================================================
        // 5. SUBSCENE
        // =====================================================

        SubScene subScene = new SubScene(
                root3D,
                width,
                height,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.rgb(35, 35, 35));
        subScene.setCamera(camera);

        // =====================================================
        // 6. ROTACIÓN CON MOUSE
        // =====================================================

        Rotate rotateY = new Rotate(-30, Rotate.Y_AXIS);
        Rotate rotateX = new Rotate(-15, Rotate.X_AXIS);

        modelo.getTransforms().addAll(rotateY, rotateX);

        subScene.setOnMousePressed((MouseEvent event) -> {

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();

        });

        subScene.setOnMouseDragged((MouseEvent event) -> {

            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();

            double deltaX = mouseX - mouseOldX;
            double deltaY = mouseY - mouseOldY;

            mouseOldX = mouseX;
            mouseOldY = mouseY;

            rotateY.setAngle(
                    rotateY.getAngle() + deltaX * 0.5
            );

            rotateX.setAngle(
                    rotateX.getAngle() - deltaY * 0.5
            );

        });

        // =====================================================
        // 7. ZOOM CON SCROLL
        // =====================================================

        subScene.setOnScroll((ScrollEvent event) -> {

            zoom += event.getDeltaY();

            if (zoom > -300) {
                zoom = -300;
            }

            if (zoom < -3000) {
                zoom = -3000;
            }

            camera.setTranslateZ(zoom);

        });

        return subScene;
    }

    // =========================================================
    // ESTRUCTURA DEL FURGÓN
    // =========================================================

    private static void crearEstructuraFurgon(
            Group modelo,
            double ancho,
            double alto,
            double largo) {

        double x1 = -ancho / 2;
        double x2 = ancho / 2;

        double y1 = -alto / 2;
        double y2 = alto / 2;

        double z1 = -largo / 2;
        double z2 = largo / 2;

        Color colorEstructura = Color.LIGHTGRAY;

        // ==========================
        // BASE
        // ==========================

        crearLinea(modelo, x1, y2, z1, x2, y2, z1, colorEstructura);
        crearLinea(modelo, x2, y2, z1, x2, y2, z2, colorEstructura);
        crearLinea(modelo, x2, y2, z2, x1, y2, z2, colorEstructura);
        crearLinea(modelo, x1, y2, z2, x1, y2, z1, colorEstructura);

        // ==========================
        // TECHO
        // ==========================

        crearLinea(modelo, x1, y1, z1, x2, y1, z1, colorEstructura);
        crearLinea(modelo, x2, y1, z1, x2, y1, z2, colorEstructura);
        crearLinea(modelo, x2, y1, z2, x1, y1, z2, colorEstructura);
        crearLinea(modelo, x1, y1, z2, x1, y1, z1, colorEstructura);

        // ==========================
        // COLUMNAS VERTICALES
        // ==========================

        crearLinea(modelo, x1, y1, z1, x1, y2, z1, colorEstructura);
        crearLinea(modelo, x2, y1, z1, x2, y2, z1, colorEstructura);
        crearLinea(modelo, x1, y1, z2, x1, y2, z2, colorEstructura);
        crearLinea(modelo, x2, y1, z2, x2, y2, z2, colorEstructura);
    }

    private static void crearBordesCaja(
            Group grupo,
            double centroX,
            double centroY,
            double centroZ,
            double ancho,
            double alto,
            double largo) {

        double x1 = centroX - ancho / 2.0;
        double x2 = centroX + ancho / 2.0;

        double y1 = centroY - alto / 2.0;
        double y2 = centroY + alto / 2.0;

        double z1 = centroZ - largo / 2.0;
        double z2 = centroZ + largo / 2.0;

        Color colorBorde = Color.BLACK;

        // ==========================
        // BASE
        // ==========================

        crearLinea(grupo, x1, y2, z1, x2, y2, z1, colorBorde);
        crearLinea(grupo, x2, y2, z1, x2, y2, z2, colorBorde);
        crearLinea(grupo, x2, y2, z2, x1, y2, z2, colorBorde);
        crearLinea(grupo, x1, y2, z2, x1, y2, z1, colorBorde);

        // ==========================
        // PARTE SUPERIOR
        // ==========================

        crearLinea(grupo, x1, y1, z1, x2, y1, z1, colorBorde);
        crearLinea(grupo, x2, y1, z1, x2, y1, z2, colorBorde);
        crearLinea(grupo, x2, y1, z2, x1, y1, z2, colorBorde);
        crearLinea(grupo, x1, y1, z2, x1, y1, z1, colorBorde);

        // ==========================
        // VERTICALES
        // ==========================

        crearLinea(grupo, x1, y1, z1, x1, y2, z1, colorBorde);
        crearLinea(grupo, x2, y1, z1, x2, y2, z1, colorBorde);
        crearLinea(grupo, x1, y1, z2, x1, y2, z2, colorBorde);
        crearLinea(grupo, x2, y1, z2, x2, y2, z2, colorBorde);
    }

    // =========================================================
    // CREAR UNA LÍNEA 3D
    // =========================================================

    private static void crearLinea(
            Group grupo,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            Color color) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double longitud = Math.sqrt(
                dx * dx +
                dy * dy +
                dz * dz
        );

        Cylinder linea = new Cylinder(2, longitud);

        PhongMaterial material = new PhongMaterial(color);
        linea.setMaterial(material);

        // Punto medio
        linea.setTranslateX((x1 + x2) / 2);
        linea.setTranslateY((y1 + y2) / 2);
        linea.setTranslateZ((z1 + z2) / 2);

        /*
         * Nota:
         * Cylinder por defecto está orientado verticalmente.
         *
         * Por ahora solo necesitamos una estructura visual
         * básica, pero debemos rotarlo según el eje.
         */

        if (Math.abs(dx) > 0) {

            linea.setRotationAxis(Rotate.Z_AXIS);
            linea.setRotate(90);

        } else if (Math.abs(dz) > 0) {

            linea.setRotationAxis(Rotate.X_AXIS);
            linea.setRotate(90);
        }

        grupo.getChildren().add(linea);
    }

    // =========================================================
    // COLORES SEGÚN PARADA
    // =========================================================

    private static Color getColorPorParada(int parada) {

        return switch (parada) {

            case 1 -> Color.DODGERBLUE;
            case 2 -> Color.ORANGE;
            case 3 -> Color.LIMEGREEN;

            default -> Color.PURPLE;
        };
    }
}