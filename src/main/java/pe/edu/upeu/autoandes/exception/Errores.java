package pe.edu.upeu.autoandes.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import org.springframework.http.HttpStatus;
import pe.edu.upeu.autoandes.exception.dto.ErrorResponseDTO;

public final class Errores {
    private Errores() {}
    public static ErrorResponseDTO respuesta(HttpStatus status, String mensaje, String ruta, Map<String,String> campos) {
        return new ErrorResponseDTO(LocalDateTime.now(ZoneId.of("America/Lima")), status.value(),
        status.getReasonPhrase(), mensaje, ruta, campos);
    }
}
