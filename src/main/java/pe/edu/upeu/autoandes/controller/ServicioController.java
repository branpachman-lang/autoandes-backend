package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.EstadoServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.ServicioResponseDTO;
import pe.edu.upeu.autoandes.service.service.ServicioService;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicio;
    @GetMapping
    public List<ServicioResponseDTO> listar() { return servicio.readAll(); }
    @GetMapping("/{id}")
    public ServicioResponseDTO buscar(@PathVariable @Positive Long id) { return servicio.read(id); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ServicioResponseDTO> crear(@Valid @RequestBody ServicioRequestDTO r) {
        var respuesta = servicio.create(r);
        return ResponseEntity.created(URI.create("/api/v1/servicios/" + respuesta.getId())).body(respuesta);
    }
    @PutMapping("/{id}")
    public ServicioResponseDTO actualizar(@PathVariable @Positive Long id, @Valid @RequestBody ServicioRequestDTO r) { return servicio.update(id, r); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        servicio.delete(id); return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ServicioResponseDTO estado(@PathVariable @Positive Long id, @Valid @RequestBody EstadoServicioRequestDTO r) {
        return servicio.cambiarEstado(id, r.getActivo());
    }


}
