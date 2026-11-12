package com.oropouche.tasks.infrastructure.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oropouche.tasks.domain.ports.in.DesenlaceUseCase;

/**
 * Carga los datos iniciales de los catálogos al arrancar (solo si están vacíos).
 */
@Configuration
public class DatosInicialesConfig {

    @Bean
    public ApplicationRunner inicializarCatalogos(DesenlaceUseCase desenlaceUseCase) {
        return args -> desenlaceUseCase.inicializarSiVacio();
    }
}
