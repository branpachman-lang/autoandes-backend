package pe.edu.upeu.autoandes.service.service;

import java.util.List;
import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.service.generic.CrudService;

public interface ServicioService extends CrudService<ServicioRequestDTO, ServicioResponseDTO, Long> {
    @Override
    List<ServicioResponseDTO> readAll();

    ServicioResponseDTO cambiarEstado(Long id, boolean activo);
}
