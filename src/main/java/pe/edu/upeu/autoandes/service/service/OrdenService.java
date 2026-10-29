package pe.edu.upeu.autoandes.service.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.enums.EstadoOrden;

public interface OrdenService {
    OrdenResponseDTO registrar(OrdenRequestDTO request, String username);
    List<OrdenResponseDTO> listar(String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta);
    OrdenResponseDTO buscar(Long id);
    OrdenResponseDTO anular(Long id, AnularOrdenRequestDTO request);
}
