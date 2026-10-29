package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdenResponseDTO {

    private Long id;

    private String numero;

    private LocalDateTime fecha;

    private Long vehiculoId;

    private String placa;

    private String clienteNombre;

    private Integer kilometraje;

    private String estado;

    private BigDecimal total;

    private String usuarioRegistro;

    private String motivoAnulacion;

    private List<DetalleOrdenResponseDTO> detalles;
}
