package pe.edu.upeu.autoandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.entity.Cliente;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.repository.OrdenRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.service.VehiculoService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculos;
    private final ClienteRepository clientes;
    private final OrdenRepository ordenes;
    private final AutoAndesMapper mapper;
    @Override
    public List<VehiculoResponseDTO> readAll() {
        return listarPorCliente(null);
    }

    @Override
    public List<VehiculoResponseDTO> listarPorCliente(Long clienteId) {
        var lista = clienteId == null ? vehiculos.findAll(Sort.by("placa")) : vehiculos.findByClienteIdOrderByPlacaAsc(clienteId);
        return lista.stream().map(mapper::vehiculo).toList();
    }
    @Override
    public VehiculoResponseDTO read(Long id) { return mapper.vehiculo(entidad(id)); }
    @Transactional
    @Override
    public VehiculoResponseDTO create(VehiculoRequestDTO r) {
        if (vehiculos.existsByPlaca(r.getPlaca())) throw new ReglaNegocioException("Ya existe un vehículo con placa " + r.getPlaca());
        Vehiculo v = new Vehiculo(); asignar(v, r);
        v = vehiculos.saveAndFlush(v);
        log.info("Vehículo registrado id={} placa={}", v.getId(), v.getPlaca());
        return mapper.vehiculo(v);
    }
    @Transactional
    @Override
    public VehiculoResponseDTO update(Long id, VehiculoRequestDTO r) {
        Vehiculo v = entidad(id);
        if (vehiculos.existsByPlacaAndIdNot(r.getPlaca(), id)) throw new ReglaNegocioException("Ya existe un vehículo con placa " + r.getPlaca());
        asignar(v, r); vehiculos.flush();
        log.info("Vehículo actualizado id={}", id);
        return mapper.vehiculo(v);
    }
    @Transactional
    @Override
    public void delete(Long id) {
        Vehiculo v = vehiculos.bloquearPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));
        if (ordenes.existsByVehiculoId(id)) throw new ReglaNegocioException("No se puede eliminar un vehículo con órdenes registradas");
        vehiculos.delete(v); vehiculos.flush();
        log.info("Vehículo eliminado id={}", id);
    }
    private Vehiculo entidad(Long id) {
        return vehiculos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));
    }
    private void asignar(Vehiculo v, VehiculoRequestDTO r) {
        var cliente = clientes.findById(r.getClienteId()).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + r.getClienteId()));
        v.setPlaca(r.getPlaca()); v.setMarca(r.getMarca().trim()); v.setModelo(r.getModelo().trim()); v.setAnio(r.getAnio()); v.setCliente(cliente);
    }

}
