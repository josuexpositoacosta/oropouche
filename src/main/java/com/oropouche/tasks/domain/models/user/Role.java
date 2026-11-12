package com.oropouche.tasks.domain.models.user;

/** Roles del sistema (aprobados el 2026-11-12). */
public enum Role {
    /** Gestiona usuarios, roles, áreas de salud y catálogos. No accede a datos clínicos. */
    ADMIN,
    /** Registra pacientes, casos y la atención clínica. Solo ve su área de salud. */
    MEDICO,
    /** Ve todas las áreas, valida clasificaciones y consulta estadísticas. */
    EPIDEMIOLOGO
}
