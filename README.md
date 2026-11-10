# Oropouche

**Epidemiological Case Management System and Clinical Decision Support for Oropouche Fever**

[English](#english) | [Español](#español)

---

## English

### Overview

Oropouche is a backend software project designed to manage and track Oropouche fever cases across different healthcare areas.

Its purpose is to centralize epidemiological and clinical information, help healthcare professionals evaluate reported cases, document medical assessments, and organize patient care plans and follow-up activities.

The system is designed as a clinical decision-support tool. Healthcare professionals remain responsible for evaluating each patient and making clinical decisions.

### Main objectives

- Manage healthcare areas and their associated cases.
- Maintain patient information and epidemiological records.
- Organize suspected, probable, and confirmed cases.
- Support clinical assessment and medical documentation.
- Record medical opinions and care plans.
- Track patient follow-up and case progress.
- Manage users and access roles.
- Provide information to support epidemiological monitoring.

### Architecture

The project uses Hexagonal Architecture (Ports and Adapters), separating business logic from infrastructure and external technologies.

The main architectural layers are:

- **Domain:** Business models, rules, and ports.
- **Application:** Use cases and application services.
- **Infrastructure:** REST controllers, persistence adapters, database entities, and security.

### Technology stack

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- Maven
- MySQL
- H2 Database
- JUnit and Mockito

### Tests

The project includes automated tests for persistence components and REST controllers.

Latest recorded validation: 28 tests executed, with no failures or errors.

### Project status

**In development.**

The project is undergoing architectural improvements and domain modeling to evolve toward its epidemiological case-management objectives. Not all the functional modules described above are implemented yet.

### Clinical considerations

Oropouche is a tool for supporting epidemiological management and clinical assessment. It does not replace medical diagnosis, professional judgment, or treatment decisions.

### Repository

[GitHub - josuexpositoacosta/oropouche](https://github.com/josuexpositoacosta/oropouche)

---

## Español

### Descripción general

Oropouche es un proyecto de software backend diseñado para gestionar y realizar el seguimiento de casos de fiebre de Oropouche en las diferentes áreas de salud.

Su propósito es centralizar la información epidemiológica y clínica de los pacientes, facilitar el seguimiento de los casos y proporcionar herramientas de apoyo al personal médico para la evaluación, elaboración de dictámenes y planificación de la atención sanitaria.

El sistema está concebido como una herramienta de apoyo a la toma de decisiones clínicas. La evaluación de cada paciente y las decisiones médicas continúan siendo responsabilidad del personal sanitario.

### Objetivos principales

- Gestionar las áreas de salud y sus casos asociados.
- Mantener la información de los pacientes y sus registros epidemiológicos.
- Organizar los casos sospechosos, probables y confirmados.
- Apoyar la evaluación clínica y la documentación médica.
- Registrar dictámenes médicos y planes de atención.
- Dar seguimiento a los pacientes y a la evolución de los casos.
- Gestionar usuarios y roles de acceso.
- Proporcionar información para apoyar la vigilancia epidemiológica.

### Arquitectura

El proyecto adopta una arquitectura hexagonal (Puertos y Adaptadores), separando las reglas de negocio de los mecanismos de persistencia, comunicación y servicios externos.

Las principales capas arquitectónicas son:

- **Dominio:** Modelos de dominio, reglas y puertos.
- **Aplicación:** Servicios y casos de uso.
- **Infraestructura:** Controladores REST, adaptadores JPA, persistencia y seguridad.

### Tecnologías

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- Maven
- MySQL
- H2 Database
- JUnit y Mockito

### Pruebas

El proyecto dispone de pruebas automatizadas para componentes de persistencia y controladores REST.

Última validación registrada: 28 pruebas ejecutadas, sin fallos ni errores.

### Estado del proyecto

**En desarrollo.**

La base arquitectónica y los componentes existentes están evolucionando hacia el sistema integral de gestión epidemiológica de Oropouche. No todos los módulos funcionales descritos anteriormente están implementados todavía.

### Consideraciones clínicas

Oropouche es una herramienta de apoyo a la gestión epidemiológica y evaluación clínica. No sustituye el diagnóstico, el criterio ni las decisiones terapéuticas del personal médico.

### Repositorio

[GitHub - josuexpositoacosta/oropouche](https://github.com/josuexpositoacosta/oropouche)
