package pe.edu.upeu.autoandes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetalleOrdenRequestDTO {

    @NotNull
    @Positive
    private Long servicioId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer cantidad;
}
