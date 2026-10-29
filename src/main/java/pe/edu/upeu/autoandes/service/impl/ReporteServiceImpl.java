package pe.edu.upeu.autoandes.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.service.service.ReporteService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final DetalleOrdenRepository detalles;
    @Override
    public List<IngresoPorServicioDTO> ingresosPorServicio(LocalDate desde, LocalDate hasta) {
        Fechas.validarRango(desde, hasta);
        return detalles.ingresosPorServicio(desde == null ? null : desde.atStartOfDay(), hasta == null ? null : hasta.plusDays(1).atStartOfDay());
    }

}
