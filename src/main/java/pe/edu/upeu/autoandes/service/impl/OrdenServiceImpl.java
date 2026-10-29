package pe.edu.upeu.autoandes.service.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;
import pe.edu.upeu.autoandes.enums.EstadoOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.exception.ReglaNegocioException;
import pe.edu.upeu.autoandes.exception.SolicitudInvalidaException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.OrdenRepository;
import pe.edu.upeu.autoandes.repository.ServicioRepository;
import pe.edu.upeu.autoandes.repository.VehiculoRepository;
import pe.edu.upeu.autoandes.service.service.OrdenService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrdenServiceImpl implements OrdenService {

    private final OrdenRepository ordenes;
    private final VehiculoRepository vehiculos;
    private final ServicioRepository servicios;
    private final AutoAndesMapper mapper;
    private final Clock reloj;

    @Transactional
    @Override
    public OrdenResponseDTO registrar(OrdenRequestDTO r, String username) {
        // El bloqueo del vehículo hace que dos órdenes simultáneas comparen kilometraje de forma consistente.
        Vehiculo vehiculo = vehiculos.bloquearPorId(r.getVehiculoId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + r.getVehiculoId()));
        Set<Long> ids = new HashSet<>();
        for (var d : r.getDetalles()) {
            if (!ids.add(d.getServicioId())) throw new SolicitudInvalidaException("No se permite repetir el mismo servicio en una orden");
        }
        ordenes.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(vehiculo.getId(), EstadoOrden.REGISTRADA)
            .ifPresent(ultima -> {
                if (r.getKilometraje() < ultima.getKilometraje()) {
                    throw new ReglaNegocioException("El kilometraje no puede ser menor al de la última orden REGISTRADA: " + ultima.getKilometraje());
                }
            });
        OrdenServicio orden = new OrdenServicio();
        orden.setVehiculo(vehiculo); orden.setKilometraje(r.getKilometraje());
        orden.setFecha(LocalDateTime.now(reloj)); orden.setEstado(EstadoOrden.REGISTRADA);
        orden.setUsuarioRegistro(username);
        long correlativo = ordenes.siguienteNumero();
        if (correlativo > 999999) throw new ReglaNegocioException("Se agotó el rango de numeración de órdenes");
        orden.setNumero("OS-%06d".formatted(correlativo));
        BigDecimal total = BigDecimal.ZERO;
        for (var item : r.getDetalles()) {
            Servicio s = servicios.findById(item.getServicioId()).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado con id: " + item.getServicioId()));
            if (!Boolean.TRUE.equals(s.getActivo())) throw new ReglaNegocioException("El servicio " + s.getCodigo() + " está inactivo");
            DetalleOrden d = new DetalleOrden();
            d.setServicio(s); d.setCantidad(item.getCantidad()); d.setPrecioUnitario(s.getPrecio());
            d.setSubtotal(s.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
            orden.agregarDetalle(d); total = total.add(d.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0) throw new ReglaNegocioException("El total supera el importe máximo permitido");
        orden.setTotal(total);
        orden = ordenes.saveAndFlush(orden);
        log.info("Orden registrada numero={} vehiculo={} usuario={} total={}", orden.getNumero(), vehiculo.getPlaca(), username, total);
        return mapper.orden(orden);
    }
    @Override
    public OrdenResponseDTO buscar(Long id) { return mapper.orden(entidad(id)); }
    @Override
    public List<OrdenResponseDTO> listar(String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta) {
        Fechas.validarRango(desde, hasta);
        Specification<OrdenServicio> filtros = (o, q, cb) -> {
            var p = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (placa != null && !placa.isBlank()) {
                String texto = placa.trim().toUpperCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                p.add(cb.like(cb.upper(o.get("vehiculo").get("placa")), "%" + texto + "%", '!'));
            }
            if (estado != null) p.add(cb.equal(o.get("estado"), estado));
            if (desde != null) p.add(cb.greaterThanOrEqualTo(o.get("fecha"), desde.atStartOfDay()));
            if (hasta != null) p.add(cb.lessThan(o.get("fecha"), hasta.plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return ordenes.findAll(filtros, Sort.by(Sort.Direction.DESC, "fecha", "id")).stream().map(mapper::orden).toList();
    }
    @Transactional
    @Override
    public OrdenResponseDTO anular(Long id, AnularOrdenRequestDTO r) {
        // El mismo orden de bloqueo que el registro evita carreras entre registro y anulación.
        Long vehiculoId = ordenes.vehiculoIdPorOrden(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + id));
        vehiculos.bloquearPorId(vehiculoId);
        OrdenServicio orden = ordenes.bloquearPorId(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        if (orden.getEstado() != EstadoOrden.REGISTRADA) throw new ReglaNegocioException("Solo se puede anular una orden REGISTRADA");
        if (!orden.getFecha().toLocalDate().equals(LocalDate.now(reloj))) throw new ReglaNegocioException("Solo se puede anular una orden el mismo día de su registro");
        String motivo = r.getMotivo().trim();
        if (motivo.length() < 10) throw new SolicitudInvalidaException("El motivo debe tener entre 10 y 200 caracteres");
        orden.setEstado(EstadoOrden.ANULADA); orden.setMotivoAnulacion(motivo);
        log.info("Orden anulada numero={}", orden.getNumero());
        return mapper.orden(orden);
    }
    private OrdenServicio entidad(Long id) {
        return ordenes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + id));
    }

}
