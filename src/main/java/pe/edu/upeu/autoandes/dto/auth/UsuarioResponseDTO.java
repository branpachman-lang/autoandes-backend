package pe.edu.upeu.autoandes.dto.auth;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioResponseDTO {

    private Long id;

    private String username;

    private String nombreCompleto;

    private Boolean activo;

    private List<String> roles;

    private List<String> permisos;
}
