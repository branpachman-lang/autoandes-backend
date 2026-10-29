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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.VehiculoRequestDTO;
import pe.edu.upeu.autoandes.dto.VehiculoResponseDTO;
import pe.edu.upeu.autoandes.service.service.VehiculoService;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService servicio;
    @GetMapping
    public List<VehiculoResponseDTO> listar(@RequestParam(required = false) @Positive Long clienteId) { return clienteId == null ? servicio.readAll() : servicio.listarPorCliente(clienteId); }
    @GetMapping("/{id}")
    public VehiculoResponseDTO buscar(@PathVariable @Positive Long id) { return servicio.read(id); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VehiculoResponseDTO> crear(@Valid @RequestBody VehiculoRequestDTO r) {
        var respuesta = servicio.create(r);
        return ResponseEntity.created(URI.create("/api/v1/vehiculos/" + respuesta.getId())).body(respuesta);
    }
    @PutMapping("/{id}")
    public VehiculoResponseDTO actualizar(@PathVariable @Positive Long id, @Valid @RequestBody VehiculoRequestDTO r) { return servicio.update(id, r); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        servicio.delete(id); return ResponseEntity.noContent().build();
    }


}
