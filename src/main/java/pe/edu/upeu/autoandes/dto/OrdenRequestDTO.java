package pe.edu.upeu.autoandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrdenRequestDTO {

    @NotNull
    @Positive
    private Long vehiculoId;

    @NotNull
    @Min(0)
    @Max(9999999)
    private Integer kilometraje;

    @NotNull
    @Size(min = 1, max = 10)
    private List<@NotNull @Valid DetalleOrdenRequestDTO> detalles;
}
