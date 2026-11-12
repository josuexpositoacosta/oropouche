package com.oropouche.tasks.infrastructure.controllers.vigilancia;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.vigilancia.CriterioFecha;
import com.oropouche.tasks.domain.models.vigilancia.FiltroVigilancia;
import com.oropouche.tasks.domain.ports.in.AreaSaludUseCase;
import com.oropouche.tasks.domain.ports.in.VigilanciaUseCase;
import com.oropouche.tasks.infrastructure.controllers.vigilancia.dto.AreaResponse;
import com.oropouche.tasks.infrastructure.controllers.vigilancia.dto.ResumenResponse;
import com.oropouche.tasks.infrastructure.controllers.vigilancia.dto.SemanaResponse;

/**
 * Estadísticas de vigilancia epidemiológica (solo EPIDEMIOLOGO). Parámetros comunes, todos opcionales:
 * {@code desde}, {@code hasta} (ISO, por defecto las últimas 12 semanas), {@code areaSaludId} y
 * {@code criterio} (INICIO_SINTOMAS por defecto, o NOTIFICACION).
 */
@RestController
@RequestMapping("/api/v1/estadisticas")
@CrossOrigin(origins = {"http://localhost:4200"})
public class VigilanciaController {

    private final VigilanciaUseCase vigilancia;
    private final AreaSaludUseCase areas;

    public VigilanciaController(VigilanciaUseCase vigilancia, AreaSaludUseCase areas) {
        this.vigilancia = vigilancia;
        this.areas = areas;
    }

    @GetMapping("/resumen")
    public ResponseEntity<ResumenResponse> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long areaSaludId,
            @RequestParam(required = false) CriterioFecha criterio) {

        return ResponseEntity.ok(ResumenResponse.from(
                vigilancia.resumen(new FiltroVigilancia(desde, hasta, areaSaludId, criterio))));
    }

    /** Curva epidémica por semana epidemiológica. */
    @GetMapping("/semanas")
    public ResponseEntity<List<SemanaResponse>> semanas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long areaSaludId,
            @RequestParam(required = false) CriterioFecha criterio) {

        return ResponseEntity.ok(vigilancia.curvaEpidemica(new FiltroVigilancia(desde, hasta, areaSaludId, criterio))
                .stream().map(SemanaResponse::from).collect(Collectors.toList()));
    }

    @GetMapping("/areas")
    public ResponseEntity<List<AreaResponse>> porArea(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) CriterioFecha criterio) {

        return ResponseEntity.ok(vigilancia.porArea(new FiltroVigilancia(desde, hasta, null, criterio))
                .stream().map(AreaResponse::from).collect(Collectors.toList()));
    }

    /** Línea de casos en CSV, sin nombres ni documentos de los pacientes. */
    @GetMapping(value = "/casos.csv", produces = "text/csv")
    public ResponseEntity<byte[]> lineaDeCasos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long areaSaludId,
            @RequestParam(required = false) CriterioFecha criterio) {

        FiltroVigilancia filtro = vigilancia.normalizar(new FiltroVigilancia(desde, hasta, areaSaludId, criterio));
        Map<Long, String> codigos = areas.listarTodas().stream()
                .collect(Collectors.toMap(AreaSalud::getId, AreaSalud::getCodigo));

        byte[] csv = LineaCasosCsv.generar(vigilancia.lineaDeCasos(filtro), codigos).getBytes(StandardCharsets.UTF_8);
        String nombre = "casos-oropouche_" + filtro.desde() + "_" + filtro.hasta() + ".csv";

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(csv);
    }
}
