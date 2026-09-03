package com.logistica.solver;

import com.logistica.model.Furgon;
import com.logistica.model.Nevera;
import java.util.*;

public class PackSolverLIFO {

    public static List<Nevera> resolverAcomodo(Furgon furgon, List<Nevera> neveras) {
        // 1. Ordenar por Restricción LIFO: 
        // Las paradas mayores (últimas entregas) van PRIMERO al fondo (Z menor).
        // A igual parada, se ordenan las neveras más grandes (56L) primero para base.
        neveras.sort((a, b) -> {
            if (b.getOrdenParada() != a.getOrdenParada()) {
                return Integer.compare(b.getOrdenParada(), a.getOrdenParada());
            }
            return Double.compare(b.getAlto(), a.getAlto());
        });

        double currentZ = 0;
        double currentX = 0;
        double currentY = 0;
        double maxAltoEnFila = 0;
        double maxLargoEnFranja = 0;

        for (Nevera n : neveras) {
            // Verificar si cabe en la fila actual (ancho X)
            if (currentX + n.getAncho() > furgon.getAncho()) {
                // Siguiente fila en el mismo nivel Y
                currentX = 0;
                currentZ += maxLargoEnFranja;
                maxLargoEnFranja = 0;
            }

            // Verificar si cabe en el fondo (largo Z)
            if (currentZ + n.getLargo() > furgon.getLargo()) {
                // Siguiente nivel de altura Y (Apilamiento)
                currentX = 0;
                currentZ = 0;
                currentY += maxAltoEnFila;
                maxAltoEnFila = 0;
            }

            // Asignar posición (coordenadas locales de la nevera)
            n.setPosicion(currentX, currentY, currentZ);

            // Actualizar punteros
            currentX += n.getAncho();
            maxLargoEnFranja = Math.max(maxLargoEnFranja, n.getLargo());
            maxAltoEnFila = Math.max(maxAltoEnFila, n.getAlto());
        }

        return neveras;
    }
}