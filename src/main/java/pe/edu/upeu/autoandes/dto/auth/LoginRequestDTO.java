package pe.edu.upeu.autoandes.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank
    @Size(max = 30)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;
}
