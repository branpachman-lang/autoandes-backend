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
public class LoginResponseDTO {

    private String token;

    private String tipo;

    private long expiraEnSegundos;

    private String username;

    private String nombreCompleto;

    private List<String> roles;

    private List<String> permisos;
}
