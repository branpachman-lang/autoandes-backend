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
public class ServicioResponseDTO {

    private Long id;

    private String codigo;

    private String nombre;

    private BigDecimal precio;

    private Integer duracionMin;

    private Boolean activo;
}
