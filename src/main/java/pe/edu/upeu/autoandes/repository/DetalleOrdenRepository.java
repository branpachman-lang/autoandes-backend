package pe.edu.upeu.autoandes.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.entity.DetalleOrden;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {
    boolean existsByServicioId(Long servicioId);
    @Query("""
        select new pe.edu.upeu.autoandes.dto.reporte.IngresoPorServicioDTO(
            s.codigo, s.nombre, sum(d.cantidad), sum(d.subtotal))
        from DetalleOrden d join d.orden o join d.servicio s
        where o.estado = pe.edu.upeu.autoandes.enums.EstadoOrden.REGISTRADA
          and (:desde is null or o.fecha >= :desde)
          and (:hasta is null or o.fecha < :hasta)
        group by s.codigo, s.nombre
        order by sum(d.subtotal) desc, s.codigo asc
        """)
    List<IngresoPorServicioDTO> ingresosPorServicio(
    @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
