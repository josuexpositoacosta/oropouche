
# Oropouche

### Sistema de gestión epidemiológica y apoyo a la evaluación clínica de casos de fiebre de Oropouche

Oropouche es una plataforma backend diseñada para gestionar y realizar el seguimiento de casos de fiebre de Oropouche, organizados por áreas de salud.

Su propósito es centralizar la información epidemiológica y clínica de los pacientes, facilitar el seguimiento de los casos y proporcionar herramientas de apoyo al personal médico para la evaluación, elaboración de dictámenes y planificación de la atención sanitaria.

## Objetivo del proyecto

Contribuir a una mejor gestión de los casos de fiebre de Oropouche mediante una plataforma que permita organizar la información por áreas de salud, facilitar el trabajo del personal sanitario y apoyar la toma de decisiones clínicas.

## Funcionalidades del sistema

El alcance funcional contempla:

- **Gestión de áreas de salud:** organización territorial y administrativa de los casos.
- **Gestión de pacientes:** registro y consulta de información de los pacientes.
- **Gestión epidemiológica:** registro y clasificación de casos sospechosos, probables y confirmados.
- **Evaluación clínica:** documentación de síntomas, antecedentes y evolución del paciente.
- **Dictamen médico:** registro del criterio y evaluación del profesional sanitario.
- **Planes de atención:** documentación de las indicaciones y medidas de seguimiento establecidas por el médico.
- **Seguimiento de casos:** control de la evolución y estado de los pacientes.
- **Administración de usuarios y roles:** gestión de los diferentes perfiles del personal.
- **Estadísticas epidemiológicas:** información consolidada para analizar los casos por áreas de salud.

## Arquitectura

El proyecto adopta una arquitectura hexagonal (Ports and Adapters), buscando separar las reglas de negocio de los mecanismos de persistencia, comunicación y servicios externos.

La organización principal contempla:

- **Domain:** modelos de dominio y puertos.
- **Application:** servicios y casos de uso.
- **Infrastructure:** controladores REST, adaptadores JPA, persistencia y seguridad.

## Tecnologías

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- Maven
- MySQL
- H2 para pruebas
- JUnit y Mockito

## Pruebas

El proyecto dispone de pruebas automatizadas para componentes de persistencia y controladores REST.

Última validación registrada: 28 pruebas ejecutadas, sin fallos ni errores.

## Estado del proyecto

Proyecto en desarrollo. La base arquitectónica y los componentes de gestión de usuarios y tareas se encuentran en proceso de evolución hacia el sistema integral de gestión epidemiológica de Oropouche.

## Consideraciones clínicas

Oropouche es una herramienta de apoyo a la gestión y evaluación clínica. No sustituye el diagnóstico, el criterio ni las decisiones terapéuticas del personal médico.

## Repositorio

[GitHub - josuexpositoacosta/oropouche](https://github.com/josuexpositoacosta/oropouche)

