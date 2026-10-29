package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VehiculoRequestDTO {

    @NotBlank
    @Pattern(regexp = "[A-Z]{3}-[0-9]{3}", message = "La placa debe tener el formato ABC-123")
    private String placa;

    @NotBlank
    @Size(max = 40)
    private String marca;

    @NotBlank
    @Size(max = 40)
    private String modelo;

    @NotNull
    @Min(1990)
    @Max(2027)
    private Integer anio;

    @NotNull
    @Positive
    private Long clienteId;
}
