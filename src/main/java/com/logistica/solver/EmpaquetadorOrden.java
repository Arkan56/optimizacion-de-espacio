package com.logistica.solver;

import com.logistica.dto.ItemMedicamentoDTO;
import com.logistica.model.Medicamento;
import com.logistica.model.Nevera;
import com.logistica.model.Pedido;

import java.util.*;

public class EmpaquetadorOrden {

    private static final double EPS = 1e-5;

    // Dimensiones de contenedores internos
    private static final double C56_ANCHO = 20.2, C56_ALTO = 19.6, C56_LARGO = 28.2;
    private static final double C18_ANCHO = 21.0, C18_ALTO = 13.0, C18_LARGO = 27.5;

    private static class PuntoLocal {
        double x, y, z;

        PuntoLocal(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class MedicamentoLocal {
        Medicamento plantilla;
        double x, y, z, w, h, l;

        MedicamentoLocal(Medicamento plantilla, double x, double y, double z, double w, double h, double l) {
            this.plantilla = plantilla;
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.l = l;
        }
    }

    private static class ContenedorInterno {
        double cx, cy, cz; // Centro 3D del contenedor dentro de la nevera
        double cW, cH, cL;
        List<PuntoLocal> candidatos = new ArrayList<>();
        List<MedicamentoLocal> empacadosLocales = new ArrayList<>();

        ContenedorInterno(double cx, double cy, double cz, double cW, double cH, double cL) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.cW = cW;
            this.cH = cH;
            this.cL = cL;
            candidatos.add(new PuntoLocal(0, 0, 0));
        }

        boolean intentarEmpacar(Medicamento medPlantilla) {
            double w = medPlantilla.getAncho();
            double h = medPlantilla.getAlto();
            double l = medPlantilla.getLargo();

            // Probar orientaciones
            double[][] orientaciones = {
                    { w, h, l },
                    { l, h, w },
                    { w, l, h },
                    { h, w, l }
            };

            // Ordenar candidatos: menor Y primero (base/piso), luego menor Z, luego menor X
            candidatos.sort(Comparator.comparingDouble((PuntoLocal p) -> p.y)
                    .thenComparingDouble(p -> p.z)
                    .thenComparingDouble(p -> p.x));

            for (double[] dim : orientaciones) {
                double curW = dim[0], curH = dim[1], curL = dim[2];

                if (curW > cW + EPS || curH > cH + EPS || curL > cL + EPS)
                    continue;

                for (PuntoLocal p : candidatos) {
                    if (cabeEnContenedor(p, curW, curH, curL)
                            && !hayColision(p, curW, curH, curL)
                            && tieneSoporte(p, curW, curH, curL)) {

                        empacadosLocales.add(new MedicamentoLocal(medPlantilla, p.x, p.y, p.z, curW, curH, curL));

                        // Generar nuevos puntos candidatos
                        candidatos.add(new PuntoLocal(p.x + curW, p.y, p.z));
                        candidatos.add(new PuntoLocal(p.x, p.y, p.z + curL));
                        candidatos.add(new PuntoLocal(p.x, p.y + curH, p.z));
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean cabeEnContenedor(PuntoLocal p, double w, double h, double l) {
            return p.x + w <= cW + EPS && p.y + h <= cH + EPS && p.z + l <= cL + EPS;
        }

        private boolean hayColision(PuntoLocal p, double w, double h, double l) {
            double x1 = p.x, x2 = p.x + w, y1 = p.y, y2 = p.y + h, z1 = p.z, z2 = p.z + l;
            for (MedicamentoLocal m : empacadosLocales) {
                boolean sepX = x2 <= m.x + EPS || m.x + m.w <= x1 + EPS;
                boolean sepY = y2 <= m.y + EPS || m.y + m.h <= y1 + EPS;
                boolean sepZ = z2 <= m.z + EPS || m.z + m.l <= z1 + EPS;
                if (!sepX && !sepY && !sepZ)
                    return true;
            }
            return false;
        }

        private boolean tieneSoporte(PuntoLocal p, double w, double h, double l) {
            if (p.y <= EPS)
                return true; // Apoyado en el piso del contenedor

            double x1 = p.x, x2 = p.x + w;
            double z1 = p.z, z2 = p.z + l;
            double areaNecesaria = (x2 - x1) * (z2 - z1);
            double areaCubierta = 0;

            for (MedicamentoLocal m : empacadosLocales) {
                if (Math.abs((m.y + m.h) - p.y) <= EPS) {
                    double ix1 = Math.max(x1, m.x), ix2 = Math.min(x2, m.x + m.w);
                    double iz1 = Math.max(z1, m.z), iz2 = Math.min(z2, m.z + m.l);
                    if (ix2 > ix1 && iz2 > iz1) {
                        areaCubierta += (ix2 - ix1) * (iz2 - iz1);
                    }
                }
            }
            return areaCubierta >= areaNecesaria - EPS;
        }

        List<Medicamento> convertirACoordenadasNevera() {
            List<Medicamento> res = new ArrayList<>();
            for (MedicamentoLocal mLoc : empacadosLocales) {
                Medicamento med = new Medicamento(mLoc.plantilla, mLoc.w, mLoc.h, mLoc.l);

                double globalX = cx - cW / 2.0 + mLoc.x + mLoc.w / 2.0;
                double globalY = (cy + cH / 2.0) - mLoc.y - mLoc.h / 2.0;
                double globalZ = cz - cL / 2.0 + mLoc.z + mLoc.l / 2.0;

                med.setPosicion(globalX, globalY, globalZ);
                res.add(med);
            }
            return res;
        }
    }

    public static void procesarYAsignarNeveras(Pedido pedido, List<ItemMedicamentoDTO> itemsSeleccionados) {
        if (pedido == null || itemsSeleccionados == null || itemsSeleccionados.isEmpty())
            return;

        pedido.setMedicamentos(new ArrayList<>(itemsSeleccionados));

        List<Medicamento> unidadesAEmpacar = new ArrayList<>();
        for (ItemMedicamentoDTO dto : itemsSeleccionados) {
            for (int i = 0; i < dto.getCantidad(); i++) {
                unidadesAEmpacar.add(dto.getMedicamento());
            }
        }

        // Ordenar cajas por área de base descendente y volumen descendente
        unidadesAEmpacar.sort((a, b) -> {
            int cmpArea = Double.compare(b.getAreaBaseCm2(), a.getAreaBaseCm2());
            if (cmpArea != 0)
                return cmpArea;
            return Double.compare(b.getVolumenCm3(), a.getVolumenCm3());
        });

        int contadorNeveras = pedido.getNeveras().size() + 1;

        while (!unidadesAEmpacar.isEmpty()) {

            // 1. Probar si todos los elementos restantes caben en 1 Nevera de 18L
            ContenedorInterno prueba18 = new ContenedorInterno(0, 0, 0, C18_ANCHO, C18_ALTO, C18_LARGO);
            List<Medicamento> faltantes18 = new ArrayList<>();

            for (Medicamento med : unidadesAEmpacar) {
                if (!prueba18.intentarEmpacar(med)) {
                    faltantes18.add(med);
                }
            }

            boolean cabeEn18L = faltantes18.isEmpty();

            Nevera.Tipo tipoNevera = cabeEn18L ? Nevera.Tipo.CAPACIDAD_18L : Nevera.Tipo.CAPACIDAD_56L;
            double nAncho = cabeEn18L ? 42.8 : 65.4;
            double nAlto = cabeEn18L ? 28.4 : 51.8;
            double nLargo = cabeEn18L ? 31.3 : 64.2;
            double nPesoBase = cabeEn18L ? 2.5 : 4.0;

            Nevera nevera = new Nevera("N" + (contadorNeveras++), tipoNevera, nLargo, nAncho, nAlto, nPesoBase,
                    pedido.getOrdenParada());
            List<ContenedorInterno> contenedores = crearContenedoresInternos(tipoNevera, nAncho, nAlto, nLargo);

            // 2. Empaquetar secuencialmente (Llenar contenedor 0 completamente antes del 1,
            // 2, 3...)
            Iterator<Medicamento> it = unidadesAEmpacar.iterator();
            while (it.hasNext()) {
                Medicamento med = it.next();
                for (ContenedorInterno cont : contenedores) {
                    if (cont.intentarEmpacar(med)) {
                        it.remove();
                        break;
                    }
                }
            }

            // Convertir resultados a coordenadas globales de la nevera
            List<Medicamento> empacadosEnEstaNevera = new ArrayList<>();
            Map<String, Integer> conteoDTO = new LinkedHashMap<>();
            Map<String, Medicamento> plantillasDTO = new LinkedHashMap<>();
            double pesoMedicamentosGramos = 0;

            for (ContenedorInterno cont : contenedores) {
                List<Medicamento> convertidos = cont.convertirACoordenadasNevera();
                empacadosEnEstaNevera.addAll(convertidos);

                for (Medicamento m : convertidos) {
                    pesoMedicamentosGramos += m.getPesoGramos();
                    conteoDTO.put(m.getIdItem(), conteoDTO.getOrDefault(m.getIdItem(), 0) + 1);
                    plantillasDTO.put(m.getIdItem(), m);
                }
            }

            nevera.setMedicamentosEmpacados(empacadosEnEstaNevera);
            nevera.setPesoKg(nPesoBase + (pesoMedicamentosGramos / 1000.0));

            List<ItemMedicamentoDTO> resumenNevera = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : conteoDTO.entrySet()) {
                resumenNevera.add(new ItemMedicamentoDTO(plantillasDTO.get(entry.getKey()), entry.getValue()));
            }
            nevera.setContenidoMedicamentos(resumenNevera);

            pedido.agregarNevera(nevera);

            if (empacadosEnEstaNevera.isEmpty() && !unidadesAEmpacar.isEmpty()) {
                System.err.println("Advertencia: Hay un medicamento que excede el tamaño máximo del contenedor.");
                break;
            }
        }
    }

    private static List<ContenedorInterno> crearContenedoresInternos(Nevera.Tipo tipo, double nAncho, double nAlto,
            double nLargo) {
        List<ContenedorInterno> lista = new ArrayList<>();

        if (tipo == Nevera.Tipo.CAPACIDAD_56L) {
            double cW = C56_ANCHO, cH = C56_ALTO, cL = C56_LARGO;
            double posY = (nAlto / 2.0) - (cH / 2.0) - 1.5;
            double offX = cW / 2.0 + 0.6;
            double offZ = cL / 2.0 + 0.6;

            lista.add(new ContenedorInterno(-offX, posY, -offZ, cW, cH, cL));
            lista.add(new ContenedorInterno(offX, posY, -offZ, cW, cH, cL));
            lista.add(new ContenedorInterno(-offX, posY, offZ, cW, cH, cL));
            lista.add(new ContenedorInterno(offX, posY, offZ, cW, cH, cL));
        } else {
            double cW = C18_ANCHO, cH = C18_ALTO, cL = C18_LARGO;
            double posY = (nAlto / 2.0) - (cH / 2.0) - 1.5;

            lista.add(new ContenedorInterno(0, posY, 0, cW, cH, cL));
        }
        return lista;
    }
}