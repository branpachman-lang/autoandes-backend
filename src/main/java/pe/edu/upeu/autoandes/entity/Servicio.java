package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "servicio")
@Getter
@Setter
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 7)
    private String codigo;
    @Column(nullable = false, length = 80)
    private String nombre;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @Column(name = "duracion_min", nullable = false)
    private Integer duracionMin;
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    @Column(nullable = false, precision = 1)
    private Boolean activo;

}
