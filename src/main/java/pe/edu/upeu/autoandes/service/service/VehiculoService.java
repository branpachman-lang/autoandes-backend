package pe.edu.upeu.autoandes.service.service;

import java.util.List;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.service.generic.CrudService;

public interface VehiculoService extends CrudService<VehiculoRequestDTO, VehiculoResponseDTO, Long> {
    @Override
    List<VehiculoResponseDTO> readAll();

    List<VehiculoResponseDTO> listarPorCliente(Long clienteId);
}
