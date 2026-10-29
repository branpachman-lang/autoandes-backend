package pe.edu.upeu.autoandes.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponseDTO {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String mensaje;

    private String ruta;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> campos;
}
