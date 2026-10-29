package pe.edu.upeu.autoandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.exception.SolicitudInvalidaException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.DetalleOrdenRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.service.service.ServicioService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicios;
    private final DetalleOrdenRepository detalles;
    private final AutoAndesMapper mapper;
    @Override
    public List<ServicioResponseDTO> readAll() {
        return servicios.findAll(Sort.by("codigo")).stream().map(mapper::servicio).toList();
    }
    @Override
    public ServicioResponseDTO read(Long id) { return mapper.servicio(entidad(id)); }
    @Transactional
    @Override
    public ServicioResponseDTO create(ServicioRequestDTO r) {
        if (servicios.existsByCodigo(r.getCodigo())) throw new ReglaNegocioException("Ya existe el código de servicio " + r.getCodigo());
        Servicio s = new Servicio();
        asignar(s, r);
        s = servicios.saveAndFlush(s);
        log.info("Servicio registrado id={} codigo={}", s.getId(), s.getCodigo());
        return mapper.servicio(s);
    }
    @Transactional
    @Override
    public ServicioResponseDTO update(Long id, ServicioRequestDTO r) {
        Servicio s = entidad(id);
        if (servicios.existsByCodigoAndIdNot(r.getCodigo(), id)) throw new ReglaNegocioException("Ya existe el código de servicio " + r.getCodigo());
        asignar(s, r);
        servicios.flush();
        log.info("Servicio actualizado id={}", id);
        return mapper.servicio(s);
    }
    @Transactional
    @Override
    public ServicioResponseDTO cambiarEstado(Long id, boolean activo) {
        Servicio s = entidad(id);
        s.setActivo(activo);
        log.info("Estado de servicio cambiado id={} activo={}", id, activo);
        return mapper.servicio(s);
    }
    @Transactional
    @Override
    public void delete(Long id) {
        Servicio s = entidad(id);
        if (detalles.existsByServicioId(id)) throw new ReglaNegocioException("El servicio tiene órdenes. Use PATCH /servicios/" + id + "/estado para darlo de baja");
        servicios.delete(s);
        servicios.flush();
        log.info("Servicio eliminado id={}", id);
    }
    private Servicio entidad(Long id) {
        return servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + id));
    }
    private void asignar(Servicio s, ServicioRequestDTO r) {
        if (r.getNombre().trim().length() < 3) throw new SolicitudInvalidaException("El nombre debe tener al menos 3 caracteres sin contar espacios externos");
        s.setCodigo(r.getCodigo()); s.setNombre(r.getNombre().trim()); s.setPrecio(r.getPrecio());
        s.setDuracionMin(r.getDuracionMin()); s.setActivo(r.getActivo());
    }

}
