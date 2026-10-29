package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetalleOrdenResponseDTO {

    private Long id;

    private Long servicioId;

    private String codigo;

    private String servicio;

    private Integer cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal subtotal;
}
