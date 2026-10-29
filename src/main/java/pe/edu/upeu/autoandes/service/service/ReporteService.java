package pe.edu.upeu.autoandes.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;

public interface ReporteService {
    List<IngresoPorServicioDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta);
}
