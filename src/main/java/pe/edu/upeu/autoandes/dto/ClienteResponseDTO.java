package pe.edu.upeu.autoandes.dto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClienteResponseDTO {

    private Long id;

    private String documento;

    private String nombres;

    private String telefono;
}
