package pe.edu.upeu.autoandes.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
    List<Vehiculo> findByClienteIdOrderByPlacaAsc(Long clienteId);

    // Serializa los registros del mismo vehículo para proteger la regla de kilometraje.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehiculo v where v.id = :id")
    Optional<Vehiculo> bloquearPorId(@Param("id") Long id);
}
