package pe.edu.upeu.autoandes.entity;

import pe.edu.upeu.autoandes.enums.EstadoOrden;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orden_servicio")
@Getter
@Setter
public class OrdenServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 9)
    private String numero;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;
    @Column(nullable = false)
    private Integer kilometraje;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoOrden estado;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    @Column(name = "usuario_registro", nullable = false, length = 30)
    private String usuarioRegistro;
    @Column(name = "motivo_anulacion", length = 200)
    private String motivoAnulacion;
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleOrden> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleOrden detalle) {
        detalles.add(detalle);
        detalle.setOrden(this);
    }

}
