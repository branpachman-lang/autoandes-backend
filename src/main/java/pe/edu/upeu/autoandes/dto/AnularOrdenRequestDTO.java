package pe.edu.upeu.autoandes.dto;

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
public class AnularOrdenRequestDTO {

    @NotBlank
    @Size(min = 10, max = 200)
    private String motivo;
}
