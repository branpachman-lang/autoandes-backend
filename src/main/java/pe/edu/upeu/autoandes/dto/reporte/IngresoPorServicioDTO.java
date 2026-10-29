package pe.edu.upeu.autoandes.dto.reporte;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IngresoPorServicioDTO {

    private String codigo;

    private String servicio;

    private Long cantidadTotal;

    private BigDecimal importeTotal;
}
