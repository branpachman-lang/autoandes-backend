package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upeu.autoandes.exception.dto.ErrorResponseDTO;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private ResponseEntity<ErrorResponseDTO> error(HttpStatus status, String mensaje, HttpServletRequest req, Map<String, String> campos) {
        if (status.is4xxClientError()) {
            log.warn("Solicitud rechazada: {} {} - {}", req.getMethod(), req.getRequestURI(), mensaje);
        }
        return ResponseEntity.status(status).body(Errores.respuesta(status, mensaje, req.getRequestURI(), campos));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Datos inválidos", req, campos);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> inexistente(RuntimeException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> conflicto(RuntimeException ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> solicitud(RuntimeException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDTO> validacionDeMetodo(HandlerMethodValidationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> {
            if (resultado instanceof ParameterErrors errores) {
                errores.getFieldErrors().forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
            } else {
                String nombre = resultado.getMethodParameter().getParameterName();
                resultado.getResolvableErrors().forEach(e -> campos.putIfAbsent(nombre == null ? "parametro" : nombre, e.getDefaultMessage()));
            }
        });
        return error(HttpStatus.BAD_REQUEST, "Datos inválidos", req, campos);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponseDTO> malformada(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida: revise el JSON, los parámetros y sus formatos", req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> autenticacion(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> acceso(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> integridad(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "La operación entra en conflicto con un registro duplicado o relacionado", req, null);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponseDTO> http(ErrorResponseException ex, HttpServletRequest req) {
        var status = HttpStatus.valueOf(ex.getStatusCode().value());
        return error(status, status.getReasonPhrase(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> inesperado(Exception ex, HttpServletRequest req) {
        if (ex instanceof org.springframework.web.ErrorResponse respuesta) {
            var status = HttpStatus.valueOf(respuesta.getStatusCode().value());
            return error(status, status.getReasonPhrase(), req, null);
        }
        log.error("Error inesperado en {}", req.getRequestURI(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", req, null);
    }
}
