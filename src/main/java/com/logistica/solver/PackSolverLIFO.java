package com.logistica.solver;

import com.logistica.model.Furgon;
import com.logistica.model.Nevera;

import java.util.*;

public class PackSolverLIFO {

    private static final double EPS = 1e-6;

    // Punto candidato donde se podría apoyar una nueva nevera
    private static class Punto {
        double x, y, z;

        Punto(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Caja ya colocada (para chequear colisiones y soporte)
    private static class CajaColocada {
        double x, y, z; // esquina de menor X, menor Y (piso) y menor Z
        double ancho, alto, largo;

        CajaColocada(double x, double y, double z, double ancho, double alto, double largo) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ancho = ancho;
            this.alto = alto;
            this.largo = largo;
        }

        double xMax() {
            return x + ancho;
        }

        double yMax() {
            return y + alto;
        }

        double zMax() {
            return z + largo;
        }
    }

    public static List<Nevera> resolverAcomodo(Furgon furgon, List<Nevera> neveras) {

        // 1. Orden LIFO: paradas mayores primero (van al fondo),
        // y a igual parada, las más altas primero (sirven de base).
        List<Nevera> ordenadas = new ArrayList<>(neveras);

        ordenadas.sort((a, b) -> {
            if (b.getOrdenParada() != a.getOrdenParada()) {
                return Integer.compare(b.getOrdenParada(), a.getOrdenParada());
            }
            return Double.compare(b.getAlto(), a.getAlto());
        });

        List<CajaColocada> colocadas = new ArrayList<>();
        List<Nevera> acomodadas = new ArrayList<>();

        // Puntos candidatos donde se puede intentar apoyar la siguiente nevera.
        // Empezamos solo con la esquina del piso del furgón.
        List<Punto> candidatos = new ArrayList<>();
        candidatos.add(new Punto(0, 0, 0));

        for (Nevera n : ordenadas) {

            // Preferimos llenar el piso antes de apilar:
            // ordenamos por menor Y, luego menor Z, luego menor X.
            candidatos.sort(
                    Comparator.comparingDouble((Punto p) -> p.y)
                            .thenComparingDouble(p -> p.z)
                            .thenComparingDouble(p -> p.x));

            Punto elegido = null;

            for (Punto p : candidatos) {

                if (cabeEnFurgon(furgon, p, n)
                        && !hayColision(colocadas, p, n)
                        && tieneSoporte(colocadas, p, n)) {

                    elegido = p;
                    break;
                }
            }

            if (elegido == null) {
                // No encontramos un lugar físicamente válido:
                // la dejamos fuera en vez de "flotarla".
                continue;
            }

            n.setPosicion(elegido.x, elegido.y, elegido.z);

            CajaColocada caja = new CajaColocada(
                    elegido.x, elegido.y, elegido.z,
                    n.getAncho(), n.getAlto(), n.getLargo());

            colocadas.add(caja);
            acomodadas.add(n);

            // A partir de la caja recién puesta generamos nuevos candidatos:
            // a su derecha (X), detrás de ella (Z) y encima de ella (Y).
            candidatos.add(new Punto(caja.xMax(), caja.y, caja.z));
            candidatos.add(new Punto(caja.x, caja.y, caja.zMax()));
            candidatos.add(new Punto(caja.x, caja.yMax(), caja.z));
        }

        return acomodadas;
    }

    private static boolean cabeEnFurgon(Furgon furgon, Punto p, Nevera n) {

        return p.x + n.getAncho() <= furgon.getAncho() + EPS
                && p.y + n.getAlto() <= furgon.getAlto() + EPS
                && p.z + n.getLargo() <= furgon.getLargo() + EPS;
    }

    private static boolean hayColision(List<CajaColocada> colocadas, Punto p, Nevera n) {

        double x1 = p.x, x2 = p.x + n.getAncho();
        double y1 = p.y, y2 = p.y + n.getAlto();
        double z1 = p.z, z2 = p.z + n.getLargo();

        for (CajaColocada c : colocadas) {

            boolean separadasEnX = x2 <= c.x + EPS || c.xMax() <= x1 + EPS;
            boolean separadasEnY = y2 <= c.y + EPS || c.yMax() <= y1 + EPS;
            boolean separadasEnZ = z2 <= c.z + EPS || c.zMax() <= z1 + EPS;

            if (!separadasEnX && !separadasEnY && !separadasEnZ) {
                return true; // se superponen
            }
        }

        return false;
    }

    /**
     * Verifica que la nevera tenga apoyo real debajo:
     * - Si va al piso (y ≈ 0) siempre tiene soporte (el piso del furgón).
     * - Si va apilada (y > 0), su huella (X-Z) debe quedar cubierta
     * por el techo de una o varias cajas que estén justo a esa altura.
     */
    private static boolean tieneSoporte(List<CajaColocada> colocadas, Punto p, Nevera n) {

        if (p.y <= EPS) {
            return true; // apoyada en el piso del furgón
        }

        double x1 = p.x, x2 = p.x + n.getAncho();
        double z1 = p.z, z2 = p.z + n.getLargo();

        double areaNecesaria = (x2 - x1) * (z2 - z1);
        double areaCubierta = 0;

        for (CajaColocada c : colocadas) {

            if (Math.abs(c.yMax() - p.y) > EPS) {
                continue; // el techo de esta caja no está a la altura requerida
            }

            double ix1 = Math.max(x1, c.x);
            double ix2 = Math.min(x2, c.xMax());
            double iz1 = Math.max(z1, c.z);
            double iz2 = Math.min(z2, c.zMax());

            if (ix2 > ix1 && iz2 > iz1) {
                areaCubierta += (ix2 - ix1) * (iz2 - iz1);
            }
        }

        // Exigimos que toda la huella quede soportada
        // (con un pequeño margen por errores de redondeo).
        return areaCubierta >= areaNecesaria - EPS;
    }
}