package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictamenRequest {

    private String diagnosticoPrincipal;
    private String diagnosticosDiferenciales;
    private String conclusion;
}
