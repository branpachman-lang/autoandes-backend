package pe.edu.upeu.autoandes.dto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehiculoResponseDTO {

    private Long id;

    private String placa;

    private String marca;

    private String modelo;

    private Integer anio;

    private Long clienteId;

    private String clienteNombre;
}
