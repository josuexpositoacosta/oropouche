package com.oropouche.tasks.infrastructure.controllers.vigilancia;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.vigilancia.SemanaEpidemiologica;

/**
 * Línea de casos en CSV (separador ";" para Excel en español, UTF-8 con BOM). No incluye nombres ni
 * documentos de los pacientes: solo su identificador interno.
 */
final class LineaCasosCsv {

    static final String BOM = "﻿";

    private static final String[] CABECERA = {
        "caso_id", "paciente_id", "area_codigo", "fecha_inicio_sintomas", "fecha_notificacion",
        "semana_epidemiologica", "clasificacion", "clasificacion_validada", "estado", "embarazada",
        "recaida", "neuroinvasivo", "hospitalizado", "manifestaciones", "desenlace", "fecha_cierre"
    };

    private LineaCasosCsv() {
    }

    static String generar(List<Caso> casos, Map<Long, String> codigosArea) {
        StringBuilder sb = new StringBuilder(BOM).append(String.join(";", CABECERA)).append("\r\n");
        for (Caso c : casos) {
            sb.append(String.join(";",
                    texto(c.getId()),
                    texto(c.getPacienteId()),
                    campo(codigosArea.getOrDefault(c.getAreaSaludId(), String.valueOf(c.getAreaSaludId()))),
                    texto(c.getFechaInicioSintomas()),
                    texto(c.getFechaNotificacion()),
                    c.getFechaInicioSintomas() == null ? "" : SemanaEpidemiologica.de(c.getFechaInicioSintomas()).etiqueta(),
                    texto(c.getClasificacion()),
                    siNo(c.isClasificacionValidada()),
                    texto(c.getEstado()),
                    siNo(c.isEmbarazada()),
                    siNo(c.isRecaida()),
                    siNo(c.esNeuroinvasivo()),
                    siNo(c.fueHospitalizado()),
                    campo(c.getManifestaciones().stream().map(Manifestacion::name).sorted()
                            .collect(Collectors.joining("|"))),
                    campo(c.getDesenlace() == null ? "" : c.getDesenlace().getCodigo()),
                    texto(c.getFechaCierre())))
              .append("\r\n");
        }
        return sb.toString();
    }

    private static String texto(Object valor) {
        return valor == null ? "" : campo(valor.toString());
    }

    private static String siNo(boolean valor) {
        return valor ? "SI" : "NO";
    }

    /** Entrecomilla si hace falta y neutraliza fórmulas (=, +, -, @) para evitar inyección en hojas de cálculo. */
    static String campo(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        String v = valor;
        if ("=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
