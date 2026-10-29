package pe.edu.upeu.autoandes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.exception.Errores;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RespuestaErrorSeguridad {
    private final ObjectMapper mapper;
    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getWriter(), Errores.respuesta(status, mensaje, req.getRequestURI(), null));
    }
}
