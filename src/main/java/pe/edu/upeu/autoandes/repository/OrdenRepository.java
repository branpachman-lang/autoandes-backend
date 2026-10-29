package pe.edu.upeu.autoandes.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.enums.EstadoOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;

public interface OrdenRepository extends JpaRepository<OrdenServicio, Long>, JpaSpecificationExecutor<OrdenServicio> {
    boolean existsByVehiculoId(Long vehiculoId);
    Optional<OrdenServicio> findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(Long vehiculoId, EstadoOrden estado);
    @Query(value = "select seq_orden_numero.nextval from dual", nativeQuery = true)
    Long siguienteNumero();

    @Query("select o.vehiculo.id from OrdenServicio o where o.id = :id")
    Optional<Long> vehiculoIdPorOrden(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenServicio o where o.id = :id")
    Optional<OrdenServicio> bloquearPorId(@Param("id") Long id);
}
