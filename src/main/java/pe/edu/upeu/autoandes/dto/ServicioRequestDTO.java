package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServicioRequestDTO {

    @NotBlank
    @Pattern(regexp = "SRV-[0-9]{3}", message = "El código debe tener el formato SRV-000")
    private String codigo;

    @NotBlank
    @Size(min = 3, max = 80)
    private String nombre;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precio;

    @NotNull
    @Min(15)
    @Max(480)
    private Integer duracionMin;

    @NotNull
    private Boolean activo;
}
