package com.oropouche.tasks.application.services.vigilancia;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.vigilancia.ConteoArea;
import com.oropouche.tasks.domain.models.vigilancia.ConteoSemana;
import com.oropouche.tasks.domain.models.vigilancia.CriterioFecha;
import com.oropouche.tasks.domain.models.vigilancia.FiltroVigilancia;
import com.oropouche.tasks.domain.models.vigilancia.ResumenVigilancia;
import com.oropouche.tasks.domain.models.vigilancia.SemanaEpidemiologica;
import com.oropouche.tasks.domain.ports.in.VigilanciaUseCase;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;

/**
 * Estadísticas de vigilancia epidemiológica. Java puro.
 */
public class VigilanciaService implements VigilanciaUseCase {

    static final int SEMANAS_POR_DEFECTO = 12;
    static final int MAX_DIAS = 3 * 366;

    private final CasoRepositoryPort casos;
    private final AreaSaludRepositoryPort areas;
    private final Clock clock;

    public VigilanciaService(CasoRepositoryPort casos, AreaSaludRepositoryPort areas, Clock clock) {
        this.casos = casos;
        this.areas = areas;
        this.clock = clock;
    }

    @Override
    public FiltroVigilancia normalizar(FiltroVigilancia filtro) {

        FiltroVigilancia f = filtro == null ? new FiltroVigilancia(null, null, null, null) : filtro;

        LocalDate hasta = f.hasta() == null ? LocalDate.now(clock) : f.hasta();
        LocalDate desde = f.desde() == null
                ? SemanaEpidemiologica.de(hasta).inicio().minusWeeks(SEMANAS_POR_DEFECTO - 1L)
                : f.desde();
        CriterioFecha criterio = f.criterio() == null ? CriterioFecha.INICIO_SINTOMAS : f.criterio();

        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) > MAX_DIAS) {
            throw new IllegalArgumentException("El periodo consultado no puede superar los 3 años");
        }

        return new FiltroVigilancia(desde, hasta, f.areaSaludId(), criterio);
    }

    @Override
    public ResumenVigilancia resumen(FiltroVigilancia filtro) {

        FiltroVigilancia f = normalizar(filtro);
        List<Caso> lista = buscar(f);
        Map<Clasificacion, Long> porClasificacion = contarPorClasificacion(lista);

        long total = lista.size();
        long confirmados = porClasificacion.get(Clasificacion.CONFIRMADO);
        double porcentaje = total == 0 ? 0 : Math.round(confirmados * 1000.0 / total) / 10.0;

        Map<String, Long> porDesenlace = lista.stream()
                .filter(c -> c.getEstado() == EstadoCaso.CERRADO && c.getDesenlace() != null)
                .collect(Collectors.groupingBy(c -> c.getDesenlace().getCodigo(), TreeMap::new, Collectors.counting()));

        return new ResumenVigilancia(
                f,
                total,
                porClasificacion,
                porcentaje,
                contar(lista, Caso::estaAbierto),
                contar(lista, c -> c.getEstado() == EstadoCaso.CERRADO),
                contar(lista, Caso::isClasificacionValidada),
                contar(lista, c -> !c.isClasificacionValidada()),
                contar(lista, c -> c.tieneLaboratorioDe(AgenteEtiologico.OROPOUCHE)),
                contar(lista, Caso::isEmbarazada),
                contar(lista, Caso::esNeuroinvasivo),
                contar(lista, Caso::isRecaida),
                contar(lista, Caso::fueHospitalizado),
                porDesenlace);
    }

    @Override
    public List<ConteoSemana> curvaEpidemica(FiltroVigilancia filtro) {

        FiltroVigilancia f = normalizar(filtro);
        Map<SemanaEpidemiologica, List<Caso>> porSemana = buscar(f).stream()
                .collect(Collectors.groupingBy(c -> SemanaEpidemiologica.de(fechaDe(c, f.criterio()))));

        List<ConteoSemana> curva = new ArrayList<>();
        SemanaEpidemiologica ultima = SemanaEpidemiologica.de(f.hasta());
        for (SemanaEpidemiologica s = SemanaEpidemiologica.de(f.desde());
             !s.inicio().isAfter(ultima.inicio()); s = s.siguiente()) {
            List<Caso> deLaSemana = porSemana.getOrDefault(s, List.of());
            curva.add(new ConteoSemana(s, deLaSemana.size(), contarPorClasificacion(deLaSemana)));
        }
        return curva;
    }

    @Override
    public List<ConteoArea> porArea(FiltroVigilancia filtro) {

        FiltroVigilancia f = normalizar(filtro);
        Map<Long, List<Caso>> porArea = buscar(f).stream().collect(Collectors.groupingBy(Caso::getAreaSaludId));

        return areas.findAll().stream()
                .filter(a -> f.areaSaludId() == null || f.areaSaludId().equals(a.getId()))
                .map(a -> conteoArea(a, porArea.getOrDefault(a.getId(), List.of())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Caso> lineaDeCasos(FiltroVigilancia filtro) {
        return buscar(normalizar(filtro));
    }

    private List<Caso> buscar(FiltroVigilancia f) {
        return casos.findEntreFechas(f.areaSaludId(), f.desde(), f.hasta(), f.criterio());
    }

    private static ConteoArea conteoArea(AreaSalud area, List<Caso> lista) {
        return new ConteoArea(area.getId(), area.getCodigo(), area.getNombre(), lista.size(),
                contar(lista, Caso::estaAbierto), contarPorClasificacion(lista));
    }

    static LocalDate fechaDe(Caso caso, CriterioFecha criterio) {
        return criterio == CriterioFecha.NOTIFICACION ? caso.getFechaNotificacion() : caso.getFechaInicioSintomas();
    }

    /** Todas las clasificaciones presentes, con 0 si no hay casos, para que el resultado sea estable. */
    private static Map<Clasificacion, Long> contarPorClasificacion(List<Caso> lista) {
        Map<Clasificacion, Long> mapa = new EnumMap<>(Clasificacion.class);
        for (Clasificacion c : Clasificacion.values()) {
            mapa.put(c, 0L);
        }
        lista.forEach(caso -> mapa.merge(caso.getClasificacion(), 1L, Long::sum));
        return mapa;
    }

    private static long contar(List<Caso> lista, Function<Caso, Boolean> condicion) {
        return lista.stream().filter(condicion::apply).count();
    }
}
